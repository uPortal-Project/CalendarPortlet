/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var upcal = upcal || {};

if (!upcal.init) {
    
    upcal.init = function ($, _, Backbone) {

        /*
         * Whenever we get a valid collection of events from the server, hold 
         * on to it;  we will "replay" it if our next request is an ETag-match.
         * 
         * (NB:  Oddly this ETag business seems to affect different browsers 
         * different ways.  On FireFox, an ETag match seems provide the $.ajax() 
         * call with valid data;  on Chrome, nothing.)
         */
        var cache = new Array();
        
        var dataCache = {};
        var etagCache = {};
    	
        /* DATA MODELS */

        upcal.CalendarDay = Backbone.Model.extend({
            defaults: function () {
                return {
                    code: null,
                    displayName: null,
                    events: new upcal.CalendarEventList()
                };
            }
        });

        upcal.CalendarEvent = Backbone.Model.extend({
            defaults: function () {
                return {
                    summary: null,
                    description: null,
                    location: null,
                    startTime: null,
                    endTime: null,
                    dateStartTime: null,
                    dateEndTime: null,
                    startDate: null,
                    endDate: null,
                    allDay: false,
                    multiDay: false,
                    colorIndex: 0
                };
            }
        });

        upcal.CalendarError = Backbone.Model.extend({
            defaults: function () {
                return {
            
                };
            }
        });
        
        
        /* COLLECTIONS */

        upcal.CalendarDayList = Backbone.Collection.extend({
            model: upcal.CalendarDay
        });

        upcal.CalendarEventList = Backbone.Collection.extend({
            model: upcal.CalendarEvent
        });

        upcal.CalendarErrorList = Backbone.Collection.extend({
            model: upcal.CalendarError
        });


        /* VIEWS */

        upcal.EventListView = Backbone.View.extend({
            el: ".upcal-event-view",
            postRender: function () { },
            render: function () {
                // render the main feed detail template
                this.$(".upcal-event-list").html(this.template({ days: this.model.toJSON() }));

                var view = this;
                view.$(".upcal-event-list a").click(function () {
                    var link, feedId;
                    link = $(this);
            
                    var dayDiv = $(link.parents("div.day").get(0));
                    var dayIndex = view.$(".day").index(dayDiv);
            
                    var eventDiv = $(link.parents("div.upcal-event").get(0));
                    var eventIndex = $(dayDiv).find(".upcal-event").index(eventDiv);
            
                    var event = view.model.at(dayIndex).get("events").at(eventIndex);
                    view.trigger("eventSelected", event);
                });

                // add any jQM decorator classes
                this.postRender();
                return this;
            }
        });

        upcal.EventDetailView = Backbone.View.extend({
            el: ".upcal-event-details",
            postRender: function () { },
            render: function () {
                // render the main feed detail template
                this.$(".upcal-event-detail").html(this.template({ event: this.model.toJSON() }));

                var view = this;
                this.$(".titlebar a").click(function () { view.trigger("showList"); });
        
                // add any jQM decorator classes
                this.postRender();
                return this;
            }
        });
        
        upcal.CalendarParameterView = Backbone.View.extend({
        	render: function () {
        		this.$el.append(this.template(this.model));
        	}
        });

        upcal.CalendarView = Backbone.Model.extend({
            defaults: function () {
                return {
                	container: null,
                    detailView: new upcal.EventDetailView(),
                    listView: new upcal.EventListView(),
                    eventsUrl: null,
                    startDate: null,
                    days: 7
                };
            },
            initialize: function () {
                var view = this;
        
                // initialize the jQuery UI datepicker
                $(view.get("container")).find(".upcal-inline-calendar").datepicker({
                    inline: true,
                    changeMonth: false,
                    changeYear: false,
                    defaultDate: this.get("startDate"),
                    onSelect: function(date) {
                        // when a new date is selected, update the event list
                        view.set("startDate", date);
                        view.getEvents(view.get("startDate"), view.get("days"));
                    } 
                });
        
                // render the detail view when an event is selected
                this.get("listView").bind("eventSelected", function (event) {
                    view.get("listView").$el.hide();
                    view.get("detailView").$el.show();

                    view.get("detailView").model = event;
                    view.get("detailView").render();
                });

                // show the list view on event return
                this.get("detailView").$(".upcal-view-return").click(function () {
                    view.get("listView").$el.show();
                    view.get("detailView").$el.hide();
                });
        
                return this;
            },
            getEvents: function (startDate, days) {
                var view, listView;
                
                view = this;
                listView = view.get("listView");
                
                listView.$(".upcal-loading-message").show();
                listView.$(".upcal-event-list").hide();

                var errorNode = listView.$(".upcal-event-errors");
                errorNode.hide();
                errorNode.empty();

                // Build the URL for fetching events from the portlet
                var startDateToken = view.get("startDate").replace(/\//g, "");
                var daysToken = view.get("days");
                //dont know what this is for..... var refreshToken = (cache[startDateToken] && cache[startDateToken][daysToken]) ? 'false' : 'true';
                var url = view.get("eventsUrl")
                    .replace(/START/, startDateToken)
                    .replace(/DAYS/, daysToken);
                if(etagCache[startDateToken] && etagCache[startDateToken][daysToken]) {
                	var etagToken=etagCache[startDateToken][daysToken];
                    url=url.replace(/ETAG/, etagToken);
                }
                $.ajax({
                    url: url,
                    success: function (data,textStatus,xhr) {
                    	var etag=xhr.getResponseHeader('ETag');
                        var days = new upcal.CalendarDayList();
                        var day, dateMap, dateNames;
                        
                        // Display error messages, if any
                        if (data && data.errors && data.errors.length > 0) {
                            $(data.errors).each(function (idx, error) {
                                var errorMsg = $('<p></p>')
                                errorMsg.text(error);
                                errorNode.append(errorMsg);
                            });
                            errorNode.show();
                        }

                        // Did we receive new event information from the server?
                        if (data && data.dateMap && data.dateNames) {
                        	// Yes -- always replace what we have...
                        	dateMap = data.dateMap;
                        	dateNames = data.dateNames;
                        	if(!etagCache[startDateToken]){
                        		etagCache[startDateToken]={};
                        	}
                        	if(!dataCache[startDateToken]){
                        		dataCache[startDateToken]={};
                        	}
                        	etagCache[startDateToken][daysToken]=etag;
                        	//alert("etagcache: "+etagCache[startDateToken][daysToken]);
                        	dataCache[startDateToken][daysToken]=data;
                        } else {
                        	// No -- try to pull from cache...
                        	if(dataCache[startDateToken]&& dataCache[startDateToken][daysToken]) {
	                        	var cachedData = dataCache[startDateToken][daysToken];
	                        	dateMap = cachedData.dateMap;
	                        	dateNames = cachedData.dateNames;
                        	}
                        }
                        
                        // Is the event information we have (at this point, however we came by it) viable?
                        if (dateMap && dateNames) {
                        	// Yes -- add the events to the model...
                            $.each(dateMap, function (key, value) {
                                day = new upcal.CalendarDay({ code: key, displayName: dateNames[key] });
                                $(value).each(function (idx, event) {
                                    var params = event.event;
                                    params.colorIndex = event.colorIndex;
                                    day.get("events").add(new upcal.CalendarEvent(params));
                                });
                                days.add(day);
                            });
                        }

                        // render the event list view
                        listView.model = days;
                        listView.render();

                        // hide the loading message
                        listView.$(".upcal-loading-message").hide();
                        listView.$(".upcal-event-list").show();
                    }
                });
            }    
        });

    };

}
var upcal = upcal || {};

if (!upcal.init) {
    
    upcal.init = function ($, _, Backbone) {
        
        
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
        
                var url = view.get("eventsUrl")
                    .replace(/START/, view.get("startDate").replace(/\//g, ""))
                    .replace(/DAYS/, view.get("days"));
        
                $.ajax({
                    url: url,
                    success: function (data) {
                        var day, days;
                        
                        days = new upcal.CalendarDayList();
                        $(data.errors).each(function (idx, error) {

                        });
                        
                        $.each(data.dateMap, function (key, value) {
                            day = new upcal.CalendarDay({ code: key, displayName: data.dateNames[key] });
                            $(value).each(function (idx, event) {
                                var params = event.event;
                                params.colorIndex = event.colorIndex;
                                day.get("events").add(new upcal.CalendarEvent(params));
                            });
                            days.add(day);
                        });

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
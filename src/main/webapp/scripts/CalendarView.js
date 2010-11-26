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

var cal = cal || {};

(function($, fluid) {

    /**
     * Initialize a newly-created event list. This function will add
     * appropriate handlers to event detail links.
     */
    var initEventList = function(that) {
        that.locate("calendarEventLink").click(function(){
            var id = $(this).attr("eventIndex");
            showEvent(id, that); 
        });
    };

    /**
     * Return to the main calendar view.
     */
    var showCalendar = function(that) {
        that.locate("hideOnEvent").show();
        that.locate("hideOnCalendar").hide();
        that.locate("calendarEvent").hide();
    };

    /**
     * Show an individual event's details view.
     */
    var showEvent = function(id, that) {
        that.locate("hideOnEvent").hide();
        that.locate("hideOnCalendar").show();
        $('#eventDescription-' + id).show();
    };


    //start of creator function

    /**
     * Calendar view creator function
     * 
     * see http://wiki.fluidproject.org/display/fluid/The+creator+function
     */
    cal.CalendarView = function(container, options) {
        var that = fluid.initView("cal.CalendarView", container, options);
        
        /**
         * Update the event list to include the specified start date and 
         * number of days
         */
        that.updateEventList = function(startDate, days) {
            
            // update the state
            that.options.startDate = startDate;
            that.options.days = days;
            
            that.locate("eventList").html("");
            that.locate("loadingMessage").show();
            $.post(that.options.eventsUrl,
                { startDate: startDate, timePeriod: days }, 
                function(xml) {
                    that.locate("loadingMessage").hide();
                    that.locate("eventList").html(xml);
                    initEventList(that);
                }
            );        
        };
        
        that.updateEventList(that.options.startDate, that.options.days);
        that.locate("returnToCalendarLink").click(function(){ 
            showCalendar(that);
        });
        return that;
    };

    //end of creator function
    
    //start of defaults

    fluid.defaults("cal.CalendarView", {
        startDate: null,
        eventsUrl: null,
        days: 7,
        selectors: {
            hideOnEvent: '.upcal-hide-on-event',
            hideOnCalendar: '.upcal-hide-on-calendar',
            eventList: '.upcal-events',
            calendarEvent: '.upcal-event-detail',
            calendarEventLink: '.upcal-event-link',
            returnToCalendarLink: '.upcal-view-return',
            loadingMessage: '.upcal-loading-message'
        }
    });

    // end of defaults

})(jQuery, fluid);

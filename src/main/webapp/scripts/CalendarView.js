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
if (!cal.init) {
    
    cal.init = function ($, fluid) {
        
        var updateModel = function (that, json) {
            that.model.dates = [];
            that.model.events = [];
            that.model.errors = json.errors;
            for (var date in json.dateMap) {
                var dateObj = { name: json.dateNames[date], events: [] };
                $(json.dateMap[date]).each(function(idx, event) {
                    var ev = event.event;
                    var date = ev.startDate;                
                    var time = ev.startTime;
                    if (ev.multiDay) {
                        date += " - " + ev.endDate;
                        time = ev.startTime + " - " + ev.endTime + " " + ev.endDate;
                    } else if (ev.allDay) {
                        time = that.options.messages.allDay;
                    } else if (ev.endTime && (ev.endTime != ev.startTime || ev.startDate  != ev.endDate ) ) {
                        time = ev.startTime + " - " + ev.endTime;
                    } else {
                        time = ev.startTime;
                    }
                    ev.dateString = date;
                    ev.timeString = time;
                    ev.cssClass = "upcal-color-" + event.colorIndex;
                
                    that.model.events.push(ev);
                    dateObj.events.push(ev);
                });
                that.model.dates.push(dateObj);
            }
            if (that.model.events.length > 0) {
                that.model.event = that.model.events[0];
            } else {
                that.model.event = {};
            }
            console.log(that.model);
        };
        
        fluid.defaults("cal.CalendarView", {
            gradeNames: ["fluid.viewComponent", "autoInit"],
            startDate: null,
            eventsUrl: null,
            days: 7,
            messages: {
                allDay: "All Day"
            },
            selectors: {
                eventListContainer: ".upcal-event-list",
                eventDetailContainer: ".upcal-event-details",
                hideOnEvent: '.upcal-hide-on-event',
                hideOnCalendar: '.upcal-hide-on-calendar',
                returnToCalendarLink: '.upcal-view-return',
                loadingMessage: '.upcal-loading-message'
            },
            events: {
                onReady: null,
                onUpdateTimePeriod: null,
                onLoadEventList: null,
                onEventSelect: null,
                onEventReturn: null,
                showEvent: { 
                    event: "onEventSelect",
                    args: [ "{CalendarView}", "{arguments}.0" ]
                },
                showCalendar: { 
                    event: "onEventReturn",
                    args: [ "{CalendarView}" ]
                }
            },
            listeners: {
                showEvent: function (that, eventIndex) {
                    that.locate("hideOnEvent").hide();
                    that.locate("hideOnCalendar").show();
                },
                showCalendar: function (that) {
                    that.locate("hideOnEvent").show();
                    that.locate("hideOnCalendar").hide();
                }
            },
            components: {
                EventListView: {
                    type: "cal.EventListView",
                    createOnEvent: "onReady",
                    container: "{CalendarView}.dom.eventListContainer",
                    options: {
                        model: "{CalendarView}.model",
                        events: {
                            onLoadEventList: "{CalendarView}.events.onLoadEventList",
                            onEventSelect: "{CalendarView}.events.onEventSelect",
                            onEventReturn: "{CalendarView}.events.onEventReturn"
                        }
                    }
                },
                EventDetailView: {
                    type: "cal.EventDetailView",
                    createOnEvent: "onReady",
                    container: "{CalendarView}.dom.eventDetailContainer",
                    options: {
                        model: "{CalendarView}.model",
                        events: {
                            onEventSelect: "{CalendarView}.events.onEventSelect",
                            onEventReturn: "{CalendarView}.events.onEventReturn"
                        }
                    }
                }
            },
            finalInitFunction: function(that) {
                that.updateEventList = function (startDate, days) {
                    that.locate("loadingMessage").show();
                    $.get(that.options.eventsUrl,
                        { startDate: startDate, timePeriod: days }, 
                        function(json) {
                            updateModel(that, json);
                            that.options.startDate = startDate;
                            that.options.days = days;
                            that.locate("loadingMessage").hide();
                            that.events.onLoadEventList.fire();
                            that.events.onEventReturn.fire();
                        }, "json"
                    );
                };
                
                that.locate("returnToCalendarLink").live("click", function () {
                    that.events.onEventReturn.fire();
                });

                $.get(that.options.eventsUrl,
                    { startDate: that.options.startDate, timePeriod: that.options.days }, 
                    function(json) {
                        updateModel(that, json);
                        that.events.onReady.fire();
                        that.events.onLoadEventList.fire();
                        that.events.onEventReturn.fire();
                        that.locate("loadingMessage").hide();
                    }, "json"
                );
            }
        });
        
        fluid.defaults("cal.EventListView", {
            gradeNames: ["fluid.rendererComponent", "autoInit"],
            renderOnInit: true,
            selectors: {
                errors: ".upcal-errors",
                error: ".upcal-error",
                errorMessage: ".upcal-error-message",
                day: ".day",
                dayName: ".dayName",
                eventWrapper: ".upcal-event-wrapper",
                event: ".upcal-event",
                eventTime: ".upcal-event-time",
                eventLink: ".upcal-event-link",
                noEventsMessage: '.upcal-noevents'
            },
            events: {
                onLoadEventList: null,
                refreshList: {
                    event: "onLoadEventList",
                    args: [ "{EventListView}" ]
                }
            },
            listeners: {
                refreshList: function(that) {
                    that.refreshView();
                },
            },
            repeatingSelectors: [ "day", "eventWrapper", "error" ],
            // renderer proto-tree defining how data should be bound
            protoTree: {
                expander: [{
                    type: "fluid.renderer.condition",
                    condition: { funcName: "cal.truthy", args: "${errors}" },
                    trueTree: {
                        errors: {
                        },
                        expander: {
                            type: "fluid.renderer.repeat",
                            repeatID: "error",
                            controlledBy: "errors",
                            pathAs: "error",
                            tree: {
                                errorMessage: { value: "${{error}}" }
                            }
                        }
                    }
                },
                { 
                    type: "fluid.renderer.condition",
                    condition: { funcName: "cal.truthy", args: "${events}" },
                    falseTree: {
                        noEventsMessage: {}
                    }
                },
                {
                    type: "fluid.renderer.repeat",
                    repeatID: "day",
                    controlledBy: "dates",
                    pathAs: "day",
                    tree: {
                        dayName: { value: "${{day}.name}" },
                        expander: {
                            type: "fluid.renderer.repeat",
                            repeatID: "eventWrapper",
                            controlledBy: "{day}.events",
                            pathAs: "event",
                            valueAs: "eventValue",
                            tree: {
                                event: {
                                    decorators: [
                                         { type: "addClass", classes: "{eventValue}.cssClass" }
                                     ]
                                },
                                eventTime: { value: "${{event}.timeString}" },
                                eventLink: { target: "javascript:;", linktext: "${{event}.summary}" },
                            }
                        }
                    }
                }]
            },
            finalInitFunction: function (that) {
                that.locate("eventLink").live("click", function () {
                    var link = $(this);
                    var eventDiv = $(link.parents(that.options.selectors.event).get(0));
                    var eventIndex = eventDiv.index(that.options.selectors.event);
                    that.events.onEventSelect.fire(that.model.events[eventIndex]);
                });
                
            }
        });
        
        cal.truthy = function(arr) {
            return (arr ? true : false);
        }
        
        fluid.defaults("cal.EventDetailView", {
            gradeNames: ["fluid.rendererComponent", "autoInit"],
            renderOnInit: true,
            events: {
                onEventSelect: null,
                onShowEvent: {
                    event: "onEventSelect",
                    args: [ "{EventDetailView}", "{arguments}.0" ]
                }
            },
            listeners: {
                onShowEvent: function(that, event) {
                    that.model.event = event;
                    that.refreshView();
                },
            },
          selectors: {
                eventSummary: ".upcal-event-detail-summary",
                eventDay: ".upcal-event-detail-day",
                eventTime: ".upcal-event-detail-starttime",
                eventLocationDiv: ".upcal-event-detail-loc-div",
                eventLocation: ".upcal-event-detail-loc",
                eventDescriptionDiv: ".upcal-event-detail-desc-div",
                eventDescription: ".upcal-event-detail-desc",
                eventLinkDiv: ".upcal-event-detail-link-div",
                eventLink: ".upcal-event-detail-link"
            },
            protoTree: {
                eventSummary: { value: "${event.summary}" },
                eventDay: { value: "${event.dateString}" },
                eventTime: { value: "${event.timeString}" },
                expander: [{
                    type: "fluid.renderer.condition",
                    condition: "${event.description}",
                    trueTree: {
                        eventDescriptionDiv: {
                        },
                        eventDescription: {
                            value: "${event.description}"
                        }
                    }
                },
                {
                    type: "fluid.renderer.condition",
                    condition: "${event.location}",
                    trueTree: {
                        eventLocationDiv: {
                        },
                        eventLocation: {
                            value: "${event.location}"
                        }
                    }
                },
                {
                    type: "fluid.renderer.condition",
                    condition: "${event.link}",
                    trueTree: {
                        eventLinkDiv: {
                        },
                        eventLink: {
                            value: "${event.link}"
                        }
                    }
                }]
            }
        });
        
    };
    
};
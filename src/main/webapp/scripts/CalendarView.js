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

        
        var getParameterPath = function(name, that) {
            return that.options.parameterBindName + '[\'' + that.options.parameterNamePrefix + name + '\'].value';
        };

        var getAuxiliaryPath = function(name, that) {
            return that.options.auxiliaryBindName + '[\'' + that.options.parameterNamePrefix + name + '\'].value';
        };

        /**
         * Add a new parameter to the table.  This method will add a row to the 
         */
        var addParameter = function(form, that) {
            
            var tr, td, name, paramPath, checkbox;
            
            // get the new parameter name from the form
            name = $(form).find("input[name=name]").val();
            paramPath = getAuxiliaryPath(name, that);
            
            // create a new row in our parameters table
            tr = $(document.createElement("tr"));
            
            // add the parameter name
            tr.append($(document.createElement("td")).text(name));

            // create a new cell for the paramter value(s) and add it to our new row
            td = $(document.createElement("td"));
            tr.append(td);
            
            // add the parameter value input field
            if (that.options.multivalued) {
                
                // add a link for adding multiple parameter values
                td.append(
                    $(document.createElement("a")).attr("href", "javascript:;")
                        .addClass(that.options.displayClasses.addValueLink)
                        .text(that.options.messages.addValue)
                        .attr("paramName", name)
                        .click(function(){ addValue($(this), that); })
                );

                // add an input field for an initial single value
                addValue(td.find("." + that.options.displayClasses.addValueLink), that);

            } else {
                td.append(
                    $(document.createElement("input")).attr("name", paramPath)
                );
            }
            
            // add the parameter override checkbox
            if (that.options.useAuxiliaryCheckbox) {
                checkbox = $(document.createElement("input")).attr("type", "checkbox")
                    .attr("name", getAuxiliaryPath(name, that)).val("true");        
                tr.append($(document.createElement("td")).append(checkbox));
            }
            
            // add the remove parameter link
            tr.append($(document.createElement("td")).append(
                $(document.createElement("a")).text(that.options.messages.remove)
                    .addClass(that.options.displayClasses.removeItemLink)
                    .attr("href", "javascript:;")
                    .click(function(){ removeParameter($(this), that); })
            ));
            
            // append the new row to the table
            that.locate("preferencesTable").append(tr);
            that.options.dialog.dialog('close');
            return false;
        };

        var removeParameter = function(link, that) {
            $(link).parent().parent().remove();
        };

        var addValue = function(link, that) {
            var paramPath, div;
            
            link = $(link);
            paramPath = getParameterPath(link.attr("paramName"), that);
            link.before($(document.createElement("div"))
                .append(
                    $(document.createElement("input")).attr("name", paramPath)
                ).append(
                    $(document.createElement("a")).attr("href", "javascript:;")
                        .addClass(that.options.displayClasses.deleteValueLink)
                        .text(that.options.messages.remove)
                        .click(function(){ removeValue($(this), that); })
                )
            );
        };
        
        var removeValue = function(link, that) {
            $(link).parent().remove();
        };
        
        /**
         * Display the parameter adding dialog.  We ask a user to choose a parameter
         * name so we can appropriately set the name of the new input element.
         */
        var showAddParameterDialog = function(that) {
            var dialog = that.options.dialog;
            if (that.options.dialogInitialized) {
                // if the dialog has already been initialized, just open it
                dialog.dialog('open');
            } else {
                // set the dialog form to add the appropriate parameter
                dialog.find("form").submit(function (){ return addParameter(this, that); });
                
                // open the dialog and mark it as initialized
                dialog.dialog();
                that.options.dialogInitialized = true;
            }
        };
        
        cal.ParameterEditor = function(container, options) {
            var that = fluid.initView("cal.ParameterEditor", container, options);
            container = $(container);
            
            // initialize actions for parameter value adding and deletion
            container.find("." + that.options.displayClasses.deleteItemLink)
                .click(function(){ removeParameter(this, that); });
            container.find("." + that.options.displayClasses.deleteValueLink)
                .click(function(){ removeValue(this, that); });
            container.find("." + that.options.displayClasses.addValueLink)
                .click(function(){ addValue(this, that); });
            
            // initialize the action to add a new parameter
            container.find("." + that.options.displayClasses.addItemLink)
                .click(function(){ showAddParameterDialog(that); });
            
            // prepare the modal form dialog
            that.options.dialogInitialized = false;
        };

        
        // defaults
        fluid.defaults("cal.ParameterEditor", {
            parameterNamePrefix: '',
            parameterBindName: '',
            auxiliaryBindName: '',
            useAuxiliaryCheckbox: false,
            dialog: null,
            multivalued: false,
            displayClasses: {
                deleteItemLink: "delete-parameter-link",
                deleteValueLink: "delete-parameter-value-link",
                addItemLink: "add-parameter-link",
                addValueLink: "add-parameter-value-link"
            },
            messages: {
                remove: 'Remove',
                addValue: 'Add value'
            },
            selectors: {
                preferencesTable: 'tbody'
            }
        });

    };
    
};
<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${includeJQuery}">
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.4.2/jquery-1.4.2.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.8/jquery-ui-1.8.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/fluid/1.2.1/js/fluid-all-1.2.1-v2.min.js"/>"></script>
</c:if>
<script type="text/javascript" src="<c:url value="/scripts/CalendarView.min.js"/>"></script>

<script type="text/javascript">
    var ${n} = ${n} || {};
    ${n}.jQuery = jQuery.noConflict(${includeJQuery});
    <c:if test="${includeJQuery}">fluid = null; fluid_1_2 = null;</c:if>
    ${n}.cal = cal;
//    cal = null;
    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var cal = ${n}.cal;
        
        // The 'days' variable is used in functions beyond the CalendarView
        var days = ${model.days};

        var options = {
            eventsUrl: '<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>', 
            startDate: '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy"/>', 
            days: days,
            messages: {
                allDay: '<spring:message code="event.allday"/>'
            }
        };
        var calView = cal.CalendarView("#${n}container", options);

        var date = new Date();
        date.setFullYear(<fmt:formatDate value="${model.startDate}" pattern="yyyy"/>, Number(<fmt:formatDate value="${model.startDate}" pattern="M"/>)-1, <fmt:formatDate value="${model.startDate}" pattern="d"/>);
        $('#${n}inlineCalendar').datepicker(
            {
                inline: true,
                changeMonth: false,
                changeYear: false,
                defaultDate: date,
                onSelect: function(date) {
                    calView.updateEventList(date, days);
                } 
            }
        );

        $("#${n}container .upcal-range-day a").click(function(){
            days = $(this).attr("days");
            calView.updateEventList(date, days);
            $(".upcal-range-day a").removeClass("selected-range");
            $(this).addClass("selected-range");
        });

        var datepickerShowHide = function( showFlag ) {
            if ( showFlag == "true" ) {
                $('#${n}inlineCalendar').show();
            } else {
                $('#${n}inlineCalendar').hide();
            }
        }

        datepickerShowHide( "${model.showDatePicker}" );

        $("#${n}container .upcal-range-datepicker a").click(function(){
            datepickerShowHide( $(this).attr("show") );
            $(".upcal-range-datepicker a").removeClass("selected-range");
            $(this).addClass("selected-range");
        });

    });
</script>

<div id="${n}container" class="${n}upcal-miniview">

    <!-- Range Selector -->
    <div id="${n}calendarRangeSelector" class="upcal-range">
        <h3><spring:message code="view.main.range.header"/></h3>
        <span class="upcal-range-day" days="1">
            <a days="1" href="javascript:;" class="${ model.days == 1 ? "selected-range" : "" }">
                <spring:message code="calendar.range.day"/>
            </a>
        </span>
        <span class="upcal-pipe">|</span>
        <span class="upcal-range-day" days="7">
            <a days="7" href="javascript:;" class="${ model.days == 7 ? "selected-range" : "" }">
                <spring:message code="calendar.range.week"/>
            </a>
        </span>
        <span class="upcal-pipe">|</span>
        <span class="upcal-range-day" days="31">
            <a days="31" href="javascript:;" class="${ model.days == 31 ? "selected-range" : "" }">
                <spring:message code="calendar.range.month"/>
            </a>
        </span>
        <span class="upcal-pipe">&nbsp;&nbsp;&nbsp;</span>
        <h3><spring:message code="view.main.range.datepicker.header"/></h3>
        <span class="upcal-range-datepicker" show="true">
            <a show="true" href="javascript:;" class="${ model.showDatePicker == true ? "selected-range" : "" }">
                <spring:message code="view.main.range.datepicker.show"/>
            </a>
        </span>
        <span class="upcal-pipe">|</span>
        <span class="upcal-range-datepicker" show="false">
            <a show="false" href="javascript:;" class="${ model.showDatePicker == false ? "selected-range" : "" }">
                <spring:message code="view.main.range.datepicker.hide"/>
            </a>
        </span>
    </div>
    
    <!-- Mini-Calendar (jQuery) -->
    <div id="${n}inlineCalendar" class="jqueryui"></div>
    
    <!-- Calendar Events List -->
    <p class="upcal-loading-message"><spring:message code="eventlist.loading"/></p>
    <div class="upcal-events">
        <div class="upcal-events upcal-event-list upcal-hide-on-event" style="display:none">
            <div class="portlet-msg-error upcal-errors">
                <div class="upcal-error"></div>
            </div>
            <div class="portlet-msg-info upcal-noevents" style="display:none">
				<p><spring:message code="eventlist.noevents"/></p>
            </div>
            <div class="day">
                <h2 class="dayName">Today</h2>
                    <div class="upcal-event">
                        <div class="upcal-event-cal">
                            <span></span>
                        </div>
                        <span class="upcal-event-time">All Day</span>
                        <h3 class="upcal-event-title">
                            <a class="upcal-event-link" href="javascript:;">Event Summary</a>
                        </h3>
                    </div>
            </div>
        </div>
        
        <div class="upcal-event-details upcal-hide-on-calendar">

            <div class="upcal-event-detail">
                <!-- Event title -->
                <h2 class="upcal-event-detail-summary">Event Summary</h2>
          
                <!-- Calendar event is from -->
                <div class="upcal-event-detail-cal">
                    <span> <!-- Calendar name to go here. --> </span>
                </div>
          
                <!-- Event time -->
                <div class="event-detail-date">
                    <h3><spring:message code="event.date"/>:</h3>
                    <p>
                        <span class="upcal-event-detail-day">Today</span>
                        <span class="upcal-event-detail-starttime">2:00 PM - 3:00 PM</span>
                   </p>
                </div>

                <div class="upcal-event-detail-loc">
                    <h3><spring:message code="event.location"/>:</h3>
                    <p></p>
                </div>          
          
                <div class="upcal-event-detail-desc">
                    <h3><spring:message code="event.description"/>:</h3>
                    <p>Event description</p>
                </div>
          
                <div class="upcal-event-detail-link">
                    <h3><spring:message code="event.link"/>:</h3>
                    <p>
                        <a href="http://www.event.com" target="_blank">http://www.event.com</a>
                    </p>
                </div>
            </div>

        </div>
    </div><!-- // end:upcal-events -->

    <!-- View Links -->
    <div class="upcal-view-links">
        <a id="${n}viewMoreEventsLink" class="upcal-view-more upcal-hide-on-event" 
                href="<portlet:renderURL windowState="maximized"/>"
                title="<spring:message code="view.more.events.link.title"/>">
            <spring:message code="view.more.events.link.text"/>
        </a>
        
        <a id="${n}returnToCalendarLink" class="upcal-view-return upcal-hide-on-calendar" href="javascript:;" 
                style="display:none" title="<spring:message code="return.to.calendar.link.title"/>">
            <spring:message code="return.to.calendar.link.text"/>
        </a>
    </div>
  
</div>

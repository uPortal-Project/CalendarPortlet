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
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.4.2/jquery-1.4.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.8/jquery-ui-1.8.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/fluid/1.2.1/js/fluid-all-1.2.1-v2.min.js"/>"></script>
</c:if>
<script type="text/javascript" src="<c:url value="/scripts/CalendarView.min.js"/>"></script>

<script type="text/javascript">
    var ${n} = ${n} || {};
    ${n}.jQuery = jQuery.noConflict(${includeJQuery});
    <c:if test="${includeJQuery}">fluid = null; fluid_1_1 = null;</c:if>
    ${n}.cal = cal;
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

    });
</script>

<div id="${n}container" class="upcal-miniview">

    <!-- Mini-Calendar (jQuery) -->
    <div id="${n}inlineCalendar" class="jqueryui"></div>
    
    <!-- Calendar Events List -->
    <p class="upcal-loading-message"><spring:message code="eventlist.loading"/></p>
    <div class="upcal-events">
        <div class="upcal-events upcal-event-list upcal-hide-on-event" style="display:none">
            <div class="portlet-msg-error upcal-errors">
                <div class="upcal-error"></div>
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
    </div>
    
    <!-- View Links -->
    <div class="upcal-view-links">
        <a id="${n}returnToCalendarLink" class="upcal-view-return upcal-hide-on-calendar" href="javascript:;" 
                style="display:none" title="<spring:message code="return.to.calendar.link.title"/>">
            <spring:message code="return.to.calendar.link.text"/>
        </a>
    </div>
  
</div>

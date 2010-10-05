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
<fmt:setTimeZone value="${ model.timezone }"/>
<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${includeJQuery}">
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.4.2/jquery-1.4.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.8/jquery-ui-1.8.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/fluid/1.2.1/js/fluid-all-1.2.1.min.js"/>"></script>
</c:if>
<script type="text/javascript" src="<c:url value="/scripts/CalendarView.min.js"/>"></script>

<script type="text/javascript">
    var ${n} = ${n} || {};
    ${n}.jQuery = jQuery.noConflict(${includeJQuery});
    <c:if test="${includeJQuery}">fluid = null; fluid_1_1 = null;</c:if>
    ${n}.cal = cal;
    cal = null;
    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var cal = ${n}.cal;
        
        // The 'days' variable is used in functions beyond the CalendarView
        var days = ${model.days};

        var options = {
            eventsUrl: '<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>', 
            startDate: '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy"/>', 
            days: days
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
    </div>
    
    <!-- Mini-Calendar (jQuery) -->
    <div id="${n}inlineCalendar" class="jqueryui"></div>
    
    <!-- Calendar Events List -->
    <p class="upcal-loading-message"><spring:message code="eventlist.loading"/></p>
    <div class="upcal-events"></div>

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

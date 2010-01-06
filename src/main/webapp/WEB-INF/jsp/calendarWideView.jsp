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
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2.min.js"/>"></script>
</c:if>
<script type="text/javascript">
    var cal = cal || {};
    cal.jQuery = jQuery.noConflict(${includeJQuery});
    cal.jQuery(function(){
        var $ = cal.jQuery;
        var eventsUrl = '<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>';

        var updateEvents = function(date) {
            $("#${n}events").html("<br/><p>Loading . . . </p>");
            $.post(eventsUrl,
                { startDate: date }, 
                function(xml) { 
                    $("#${n}events").html(xml);
                    updateLinks(); 
                }
            );
        };
        
        var updateLinks = function() {
            $("#${n}events").find(".upcal-event-link").click(function(){
                    var link = $(this);
                    $('#${n}calendarRangeSelector').hide();
                    $('#${n}events .upcal-events').hide();
                    $('#${n}viewLinks').show();
                    $('#eventDescription-' + link.attr("eventIndex")).show();
                });
            $('#${n}returnToCalendarLink').click(function(){
                    $('[id^=eventDescription]').hide();
                    $('#${n}viewLinks').hide();
                    $('#${n}calendarRangeSelector').show();
                    $('#${n}events .upcal-events').show();
                });
        };
   
        $(document).ready(function(){
            var startDate = '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy"/>';
            updateEvents(startDate);
            var date = new Date();
            date.setFullYear(<fmt:formatDate value="${model.startDate}" pattern="yyyy"/>, Number(<fmt:formatDate value="${model.startDate}" pattern="M"/>)-1, <fmt:formatDate value="${model.startDate}" pattern="d"/>);
            $('#${n}inlineCalendar').datepicker(
                {
                    inline: true,
                    changeMonth: false,
                    changeYear: false,
                    defaultDate: date,
                    onSelect: function(date) {
                        updateEvents(date);
                    } 
                }
            );
        });
    });
</script>

<div class="upcal-fullview">

<c:if test="${ !model.guest }">
	<div class="upcal-edit-links">
	    <a href="<portlet:renderURL portletMode="edit"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>">Edit and Download Calendars</a>
        <c:if test="${ sessionScope.isAdmin }">
		    <span class="upcal-pipe">|</span>
		    <a href="<portlet:renderURL portletMode="edit"><portlet:param name="action" value="administration"/></portlet:renderURL>">Calendar Administration</a>
	    </c:if>
	</div>
</c:if>

<div id="calendarPortletHeader" class="fl-col-mixed3">

	<div class="fl-col-side fl-force-right">
		<div class="upcal-showcals upcal-list">
    		<h3>Show calendar:</h3>
    		<ul>
                <c:forEach items="${ model.calendars }" var="calendar">
                    <li class="color-${ model.colors[calendar.id] }">
                        <c:choose>
                            <c:when test="${ empty model.hiddenCalendars[calendar.id] }">
                                <portlet:renderURL var="url"><portlet:param name="hideCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                <a class="upcal-active" href="${ url }">
                                    <span>Active</span>
                                </a> 
                            </c:when>
                            <c:otherwise>
                                <portlet:renderURL var="url"><portlet:param name="showCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                <a class="upcal-inactive" href="${ url }">
                                    <span>Inactive</span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                        <span>${ calendar.calendarDefinition.name }</span>
                    </li>
                </c:forEach>
   		</ul>
		</div>
	</div>
	
	<div class="fl-col-side fl-force-left">
        <div id="<portlet:namespace/>inlineCalendar" class="jqueryui"></div>
        <div style="clear: both;"></div>
	</div>
	
	<div class="fl-col-main">
	
		<div id="${n}calendarRangeSelector" class="upcal-range">
            <h3>View:</h3>
            <span class="upcal-range-day">
                <c:choose>
                    <c:when test="${ model.days == 1 }">
                        Day
                </c:when>
                <c:otherwise>
                    <a href="<portlet:renderURL><portlet:param name="timePeriod" value="1"/></portlet:renderURL>">Day</a>
                </c:otherwise>
             </c:choose>
            </span>
            <span class="upcal-pipe">|</span>
            <span class="upcal-range-day">
                <c:choose>
                    <c:when test="${ model.days == 7 }">
                        Week
                    </c:when>
                    <c:otherwise>
                        <a href="<portlet:renderURL><portlet:param name="timePeriod" value="7"/></portlet:renderURL>">Week</a>
                    </c:otherwise>
                </c:choose>
            </span>
            <span class="upcal-pipe">|</span>
            <span class="upcal-range-day">
                <c:choose>
                    <c:when test="${ model.days == 31 }">
                        Month
                    </c:when>
                    <c:otherwise>
                        <a href="<portlet:renderURL><portlet:param name="timePeriod" value="31"/></portlet:renderURL>">Month</a>
                    </c:otherwise>
                </c:choose>
            </span>
		</div>
		
		<div class="upcal-event-list">
            <div id="${n}events"></div>
            
            <!-- View Links -->
            <div id="${n}viewLinks" class="upcal-view-links" style="display:none">
                <a id="${n}returnToCalendarLink" class="upcal-view-return" href="javascript:;">
                    Return to event list
                </a>
            </div>
            
		</div>
		
	</div>
</div>

</div>

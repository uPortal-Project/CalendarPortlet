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

<c:set var="n"><portlet:namespace/></c:set>

<rs:aggregatedResources path="${ model.usePortalJsLibs ? '/skin-mobile-shared.xml' : '/skin-mobile.xml' }"/>

<script type="text/javascript"><rs:compressJs>
    var ${n} = ${n} || {};
    <c:choose>
        <c:when test="${!model.usePortalJsLibs}">
            ${n}.jQuery = jQuery.noConflict(true);
            ${n}.fluid = fluid;
            fluid = null; 
            fluid_1_4 = null;
        </c:when>
        <c:otherwise>
            <c:set var="ns"><c:if test="${ not empty model.portalJsNamespace }">${ model.portalJsNamespace }.</c:if></c:set>
            ${n}.jQuery = ${ ns }jQuery;
            ${n}.fluid = ${ ns }fluid;
        </c:otherwise>
    </c:choose>
    if (!cal.initialized) cal.init(${n}.jQuery, ${n}.fluid);
    ${n}.cal = cal;
    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var cal = ${n}.cal;

        // The 'days' variable is used in functions beyond the CalendarView
        var days = ${model.days};
        
        var options = {
            eventsUrl: '<portlet:resourceURL/>', 
            startDate: '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy" timeZone="${ model.timezone }"/>', 
            days: days,
            messages: {
                allDay: '<spring:message code="all.day"/>'
            },
            eventListView: {
                type: "cal.EventListView",
                options: {
                    showEndTime: false
                }
            }
        };
        var calView = cal.CalendarView("#${n}container", options);
        
        var date = new Date();
        date.setFullYear(<fmt:formatDate value="${model.startDate}" pattern="yyyy" timeZone="${model.timezone}"/>, Number(<fmt:formatDate value="${model.startDate}" pattern="M" timeZone="${model.timezone}"/>)-1, <fmt:formatDate value="${model.startDate}" pattern="d" timeZone="${model.timezone}"/>);
        $('#${n}inlineCalendar').datepicker(
            {
                inline: true,
                changeMonth: false,
                changeYear: false,
                defaultDate: date,
                onSelect: function(date) {
                    calView.updateEventList(date, calView.options.days);
                } 
            }
        );

    });
</rs:compressJs></script>

<div id="${n}container" class="portlet ptl-calendar view-mobile">
	<div data-role="content" class="portlet-content">

	    <!-- Mini-Calendar (jQuery UI) -->
	    <div id="${n}inlineCalendar" class="upcal-inline-calendar upcal-hide-on-event"></div>
	    
	    <!-- Calendar Events List -->
	    <div class="upcal-loading-message portlet-msg-info portlet-msg info">
            <p><spring:message code="loading"/></p>
        </div>
	    <div class="upcal-events">
	        <div class="upcal-events upcal-event-list upcal-hide-on-event" style="display:none">
	            <div class="portlet-msg-error upcal-errors">
	                <div class="upcal-error"><span class="upcal-error-message"></span></div>
	            </div>
                <div class="portlet-msg-info upcal-noevents">
                    <p>No events</p>
                </div>
	            <div class="day">
	                <h2 class="dayName">Today</h2>
                        <div class="upcal-event-wrapper">
    	                    <div class="upcal-event">
    	                        <!--div class="upcal-event-cal">
    	                            <span></span>
    	                        </div-->
    	                        <div class="upcal-event-time">All Day</div>
    	                        <a class="upcal-event-link" href="javascript:;">
    		                        <h3 class="upcal-event-title">
    		                            Event Summary
    		                        </h3>
    	                        </a>
                            </div>
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
	                    <h3><spring:message code="date"/>:</h3>
	                    <p>
	                        <span class="upcal-event-detail-day">Today</span>
	                        <span class="upcal-event-detail-starttime">2:00 PM - 3:00 PM</span>
	                    </p>
	                </div>
	
	                <div class="upcal-event-detail-loc-div">
	                    <h3><spring:message code="location"/>:</h3>
	                    <p class="upcal-event-detail-loc"></p>
	                </div>          
	          
	                <div class="upcal-event-detail-desc-div">
	                    <h3><spring:message code="description"/>:</h3>
	                    <p class="upcal-event-detail-desc">Event description</p>
	                </div>
	
	                <div class="upcal-event-detail-link-div">
	                    <h3><spring:message code="link"/>:</h3>
	                    <p>
	                        <a class="upcal-event-detail-link" href="http://www.event.com" target="_blank">http://www.event.com</a>
	                    </p>
	                </div>
	            </div>
	            
	        </div>
	    </div>

        <div class="utilities upcal-view-links upcal-hide-on-calendar" style="display:none">
            <a id="${n}returnToCalendarLink" class="upcal-view-return" href="javascript:;" 
                    title="<spring:message code="return.to.calendar"/>" data-role="button">
                <spring:message code="return.to.calendar"/>
            </a>
        </div>

        <c:if test="${ !model.disablePreferences && !model.guest }">
            <div class="utilities upcal-view-links upcal-hide-on-event">
                <portlet:renderURL var="preferencesUrl" portletMode="edit"><portlet:param name="action" value="editPreferences"/></portlet:renderURL>
                <a data-role="button" href="${ preferencesUrl }" title="<spring:message code="edit.preferences"/>">
                    <spring:message code="preferences"/>
                </a>
            </div>
        </c:if>
        
  
    </div>
</div>
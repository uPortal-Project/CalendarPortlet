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

<div class="upcal-adminview">

    <div class="upcal-list">

	    <h2>Pre-configured Calendars</h2>
	    
        <ul>
            <c:forEach items="${ calendars }" var="calendar">
                <li>
                    <!-- Name/Edit -->
                    <portlet:renderURL var="editCalendarUrl">
                        <portlet:param name="action" value="editCalendarDefinition"/>
                        <portlet:param name="id" value="${ calendar.id }"/>
                    </portlet:renderURL>
                    <a class="upcal-edit" href="${ editCalendarUrl }" title="Edit calendar">
                        Edit
                    </a>
                    ${ calendar.name }
                    <!-- Delete -->
                    <portlet:actionURL var="deleteCalendarUrl">
                        <portlet:param name="action" value="deleteSharedCalendar"/>
                        <portlet:param name="calendarId" value="${ calendar.id }"/>
                    </portlet:actionURL>
                    <a class="upcal-delete" href="${ deleteCalendarUrl }" title="Delete calendar">
                        Delete
                    </a>
                </li>
            </c:forEach>
	    </ul>
	
	    <!-- Add Calendar -->
	    <portlet:renderURL var="addCalendarUrl">
	       <portlet:param name="action" value="editCalendarDefinition"/>
	    </portlet:renderURL>
	    <a class="upcal-add" href="${ addCalendarUrl }">Add a calendar</a>
	    
    </div>
     
    <div class="upcal-view-links">
        <portlet:renderURL var="returnUrl" portletMode="view"/>
        <a class="upcal-view-return" href="${ returnUrl }">
           Return to calendar
        </a>
    </div>
    
</div>
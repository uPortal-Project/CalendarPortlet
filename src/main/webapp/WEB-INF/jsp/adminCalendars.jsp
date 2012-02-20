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
<rs:aggregatedResources path="${ model.usePortalJsLibs ? '/skin-shared.xml' : '/skin.xml' }"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>
<c:if test="${!usePortalJsLibs}">
    <script type="text/javascript"><rs:compressJs>
            jQuery.noConflict(true);
            fluid = null; 
            fluid_1_4 = null;
    </rs:compressJs></script>
</c:if>

<div class="upcal-adminview">

    <div class="upcal-list">

	    <h2><spring:message code="calendar.administration"/></h2>
	    
        <ul>
            <c:forEach items="${ calendars }" var="calendar">
                <li>
                    <!-- Edit -->
                    <portlet:renderURL var="editCalendarUrl">
                        <portlet:param name="action" value="editCalendarDefinition"/>
                        <portlet:param name="id" value="${ calendar.id }"/>
                    </portlet:renderURL>
                    <a class="upcal-edit" href="${ editCalendarUrl }" 
                            title="<spring:message code="edit.calendar"/>">
                        <span><spring:message code="edit.calendar"/></span>
                    </a>
                    <!-- Calendar name -->
                    <spring:escapeBody htmlEscape="true">${ calendar.name }</spring:escapeBody>
                    <!-- Delete -->
                    <portlet:actionURL var="deleteCalendarUrl">
                        <portlet:param name="action" value="deleteSharedCalendar"/>
                        <portlet:param name="calendarId" value="${ calendar.id }"/>
                    </portlet:actionURL>
                    <a class="upcal-delete" href="${ deleteCalendarUrl }" 
                            title="<spring:message code="delete.calendar"/>">
                        <span><spring:message code="delete.calendar"/></span>
                    </a>
                </li>
            </c:forEach>
	    </ul>
	
	    <!-- Add Calendar -->
	    <portlet:renderURL var="addCalendarUrl">
	       <portlet:param name="action" value="createCalendarDefinition"/>
	    </portlet:renderURL>
	    <a class="upcal-add" href="${ addCalendarUrl }" 
	           title="<spring:message code="add.a.calendar"/>">
	       <spring:message code="add.a.calendar"/>
	    </a>
	    
    </div>
     
    <div class="upcal-view-links">
        <portlet:renderURL var="returnUrl" portletMode="view"/>
        <a class="upcal-view-return" href="${ returnUrl }" 
                title="<spring:message code="return.to.calendar"/>">
           <spring:message code="return.to.calendar"/>
        </a>
    </div>
    
</div>
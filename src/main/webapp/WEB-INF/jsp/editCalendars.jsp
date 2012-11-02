<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@ taglib prefix="up" uri="http://www.uportal.org/jsp/jstl/uportal/1.0" %>
<%@ taglib prefix="rs" uri="http://www.jasig.org/resource-server" %>
<%@ taglib prefix="antisamy" tagdir="/WEB-INF/tags/antisamy" %>
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
<rs:aggregatedResources path="${ usePortalJsLibs ? '/skin-shared.xml' : '/skin.xml' }"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<div class="upcal-editview">

<div class="upcal-edit-links">
    <portlet:renderURL var="preferencesUrl"><portlet:param name="action" value="editPreferences"/></portlet:renderURL>
    <a href="${ preferencesUrl }" title="<spring:message code="preferences"/>">
        <spring:message code="preferences"/>
    </a>
    <c:if test="${ sessionScope.isAdmin }">
        <span class="upcal-pipe">|</span>
        <portlet:renderURL var="adminUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
        <a href="${ adminUrl }" title="<spring:message code="calendar.administration"/>">
            <spring:message code="calendar.administration"/>
        </a>
    </c:if>
</div>

<h2><spring:message code="edit.calendars"/></h2>

<div class="fl-col-flex2">

    <div class="fl-col upcal-mycalendars upcal-list">
		<h3><spring:message code="my.calendars"/></h3>
		<ul>
            <c:forEach items="${ model.mycalendars }" var="calendar">
                <li>
                    <portlet:renderURL var="editCalendarUrl">
                        <portlet:param name="action" value="editUrl"/>
                        <portlet:param name="id" value="${ calendar.id }"/>
                    </portlet:renderURL>
		            <a href="${ editCalendarUrl }" class="upcal-edit"
                            title="<spring:message code="edit.calendar"/>">
                        <span><spring:message code="edit"/></span>
		            </a>
                    <span>
                        <spring:escapeBody htmlEscape="true">${ calendar.calendarDefinition.name }</spring:escapeBody>
                    </span>
                    <portlet:actionURL var="deleteCalendarUrl">
                        <portlet:param name="action" value="deleteUserCalendar"/>
                        <portlet:param name="configurationId" value="${ calendar.id }"/>
                    </portlet:actionURL>
		            <a href="${ deleteCalendarUrl }" class="upcal-delete"
                            title="<spring:message code="delete.calendar"/>">
                        <span><spring:message code="delete"/></span>
                    </a>
                    <portlet:resourceURL var="exportCalendarUrl" id="exportUserCalendar">
                        <portlet:param name="configurationId" value="${ calendar.id }"/>
                    </portlet:resourceURL>
                    <a href="${ exportCalendarUrl }" class="upcal-export"
                       title="<spring:message code='export.calendar'/>">
                    <span><spring:message code="export"/></span>
                    </a>
                </li>
            </c:forEach>
		</ul>
        <portlet:renderURL var="addCalendar">
            <portlet:param name="action" value="editUrl"/>
        </portlet:renderURL>
		<a class="upcal-add" href="${ addCalendar }"
               title="<spring:message code="add.a.calendar"/>">
           <spring:message code="add.a.calendar"/>
		</a>
	</div>
	
	<div class="fl-col upcal-builtin upcal-list">
		<h3><spring:message code="preconfigured.calendars"/></h3>
		<ul>
		    <c:forEach items="${ model.calendars }" var="calendar">
                <li>
		            <c:choose>
		                <c:when test="${ calendar.displayed }">
		                    <portlet:actionURL var="displayURL">
                                <portlet:param name="action" value="hideCalendar"/>
		                        <portlet:param name="configurationId" value="${ calendar.id }"/>
		                    </portlet:actionURL>
				            <a class="upcal-active" href="${ displayURL }"
                                    title="<spring:message code="hide.calendar"/>">
                                <span><spring:message code="hide"/></span>
				            </a>
		                </c:when>
		                <c:otherwise>
                            <portlet:actionURL var="displayURL">
                                <portlet:param name="action" value="showCalendar"/>
                                <portlet:param name="configurationId" value="${ calendar.id }"/>
                            </portlet:actionURL>
				            <a class="upcal-inactive" href="${ displayURL }"
				                    title="<spring:message code="show.calendar"/>">
                                <span><spring:message code="show"/></span>
				            </a>
                		</c:otherwise>
		            </c:choose>
		            <c:set var="editAction" value="${ model.predefinedEditActions[calendar.calendarDefinition.className] }"/>
		            <c:choose>
		                <c:when test="${ not empty editAction }">
	    	                <portlet:renderURL var="editCalendarUrl">
	                            <portlet:param name="action" value="${ editAction }"/>
	                            <portlet:param name="configurationId" value="${ calendar.id }"/>
	                        </portlet:renderURL>
	                        <a class="upcal-edit" href="${ editCalendarUrl }"
		                            title="<spring:message code="edit.calendar"/>">
		                        <span><spring:message code="edit"/></span>
	                        </a>
		                </c:when>
		                <c:otherwise>&nbsp;</c:otherwise>
		            </c:choose>                    
                    <span class="cal-name">
                        <spring:escapeBody htmlEscape="true">${ calendar.calendarDefinition.name }</spring:escapeBody>
                    </span>
                    <portlet:resourceURL var="exportCalendarUrl" id="exportUserCalendar">
                        <portlet:param name="configurationId" value="${ calendar.id }"/>
                    </portlet:resourceURL>
                    <a href="${ exportCalendarUrl }" class="upcal-export"
                       title="<spring:message code="export.calendar"/>">
                    <span><spring:message code="export"/></span>
                    </a>
                </li>
		    </c:forEach>
		    <c:forEach items="${ model.hiddencalendars }" var="calendar">
                <li>
                    <portlet:actionURL var="displayURL">
                        <portlet:param name="action" value="addSharedCalendar"/>
                        <portlet:param name="definitionId" value="${ calendar.id }"/>
                    </portlet:actionURL>
                    <a class="upcal-inactive" href="${ displayURL }" title="<spring:message code="show.calendar"/>">
                        <span><spring:message code="show"/></span>
                    </a>
                    <spring:escapeBody htmlEscape="true">${ calendar.name }</spring:escapeBody>
                </li>
		    </c:forEach>
		</ul>
	</div>
	
</div>

<c:if test="${model.timezoneReadOnly == false}">
    <h2><spring:message code="preferences"/></h2>

    <div id="${n}calendar-submission-success" class="portlet-msg-success fl-container-flex40" style="display: none;">
        <p>
            <spring:message code="your.preferences.have.been.saved.successfully"/>
            <a href="<portlet:renderURL portletMode="view"/>" title="<spring:message code="return.to.calendar"/>">
                <spring:message code="return.to.calendar"/>
            </a>
        </p>
    </div>

    <portlet:actionURL var="postUrl">
        <portlet:param name="action" value="editPreferences"/>
    </portlet:actionURL>
    <form:form name="calendar" commandName="calendarPreferencesCommand" action="${postUrl}">

        <p style="margin-top: 10px">
            <label class="portlet-form-field-label">
                <spring:message code="time.zone"/>
            </label>
            <form:select path="timezone">
                <c:forEach items="${timezones}" var="zone">
                    <spring:message var="message" code="timezone.${ zone }" text="${ zone }"/>
                    <form:option label="${message}" value="${ zone }"/>
                </c:forEach>
            </form:select>
            <form:errors path="timezone" cssClass="portlet-msg-error"/>
        </p>
        <br/>
        <p>
            <button type="submit" class="portlet-form-button">
                <spring:message code="save"/>
            </button>
        </p>
    
    </form:form>
</c:if>

<div class="upcal-view-links">
	<a class="upcal-view-return" href="<portlet:renderURL portletMode="view"/>"
            title="<spring:message code="return.to.calendar"/>">
       <spring:message code="return.to.calendar"/>
	</a>
</div>

<c:if test="${renderRequest.parameterMap['preferencesSaved'][0] == 'true'}">

    <script type="text/javascript"><rs:compressJs>
        ${n}.jQuery(function() {
            var $ = ${n}.jQuery;
            $("#${n}calendar-submission-success").slideDown("slow");
        });
    </rs:compressJs></script>
</c:if>

</div>

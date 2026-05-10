<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>
<c:set var="n"><portlet:namespace/></c:set>

<%-- skin/CSS bundle (the JS/Backbone init from scripts.jsp is intentionally
     omitted; the <calendar-portlet> element below provides its own UI.) --%>
<rs:aggregatedResources path="skin-shared.xml"/>

<div id="${n}container" class="container-fluid upcal-portlet upcal-wideview">

    <%-- Admin / preferences links (server-rendered; portlet action URLs). --%>
    <c:if test="${ !model.guest && !(model.disablePreferences && (!sessionScope.isAdmin || model.disableAdministration)) }">
        <div class="row">
            <div class="col-md-12">
                <c:if test="${ sessionScope.isAdmin && !model.disableAdministration }">
                    <portlet:renderURL var="adminUrl" portletMode="edit"><portlet:param name="action" value="administration"/></portlet:renderURL>
                    <a class="float-end" href="${ adminUrl }" title="<spring:message code="calendar.administration"/>">
                        <i class="fa fa-gears"></i>
                        <spring:message code="calendar.administration"/>
                    </a>
                </c:if>
                <c:if test="${ !model.disablePreferences && !model.guest }">
                    <span class="float-end">&nbsp;|&nbsp;</span>
                    <portlet:renderURL var="preferencesUrl" portletMode="edit"><portlet:param name="action" value="editPreferences"/></portlet:renderURL>
                    <a class="float-end" href="${ preferencesUrl }" title="<spring:message code="edit.calendar.preferences"/>">
                        <i class="fa fa-key"></i>
                        <spring:message code="preferences"/>
                    </a>
                </c:if>
            </div>
        </div>
    </c:if>

    <div class="row">
        <%-- Events column (the <calendar-portlet> renders the day/week/month
             buttons, the date input, and the events list). --%>
        <div class="col-lg-6 col-sm-12">
            <calendar-portlet
                events-url='<portlet:resourceURL id="START-DAYS"/>'
                start-date='<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy" timeZone="${ model.timezone }"/>'
                days="${ model.days }">
                <script type="application/json" class="upcal-i18n">
                {
                    "view": "<spring:message code='view' javaScriptEscape='true'/>",
                    "day": "<spring:message code='day' javaScriptEscape='true'/>",
                    "week": "<spring:message code='week' javaScriptEscape='true'/>",
                    "month": "<spring:message code='month' javaScriptEscape='true'/>",
                    "loading": "<spring:message code='loading' javaScriptEscape='true'/>",
                    "noEvents": "<spring:message code='no.events' javaScriptEscape='true'/>",
                    "allDay": "<spring:message code='all.day' javaScriptEscape='true'/>",
                    "location": "<spring:message code='location' javaScriptEscape='true'/>",
                    "description": "Description",
                    "link": "Link",
                    "returnToCalendar": "<spring:message code='return.to.calendar' javaScriptEscape='true'/>"
                }
                </script>
            </calendar-portlet>
        </div>

        <%-- My Calendars sidebar (server-rendered; show/hide/export trigger
             portlet action URLs that refresh the page). --%>
        <div class="col-lg-6 col-sm-12 upcal-my-calendars">
            <h3><spring:message code="my.calendars"/></h3>
            <ul>
                <c:forEach items="${ model.calendars }" var="calendar">
                    <li class="color-${ model.colors[calendar.id] }">
                        <c:choose>
                            <c:when test="${ empty model.hiddenCalendars[calendar.id] }">
                                <portlet:renderURL var="url"><portlet:param name="hideCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                <a href="${ url }">
                                    <span><i class="fa fa-eye-slash"></i> <spring:message code="hide"/></span>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <portlet:renderURL var="url"><portlet:param name="showCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                <a class="upcal-inactive" href="${ url }">
                                    <span><i class="fa fa-eye"></i> <spring:message code="show"/></span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                        <span><spring:escapeBody htmlEscape="true">${ calendar.calendarDefinition.name }</spring:escapeBody></span>
                        <portlet:resourceURL var="exportCalendarUrl" id="exportUserCalendar"><portlet:param name="configurationId" value="${ calendar.id }"/></portlet:resourceURL>
                        <a href="${ exportCalendarUrl }" class="ms-auto" title="<spring:message code='export.calendar'/>">
                            <span><i class="fa fa-download"></i> <spring:message code="export"/></span>
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
</div>

<script type="module" src="<c:url value='/scripts/calendar-portlet-element.js'/>"></script>

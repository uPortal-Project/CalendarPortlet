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

<%-- skin/CSS bundle (Bootstrap, FontAwesome, calendar.css). --%>
<rs:aggregatedResources path="skin-shared.xml"/>

<div id="${n}container" class="${n}upcal-miniview upcal-portlet upcal-narrowview">
    <div class="container-fluid upcal-events">
        <calendar-portlet
            events-url='<portlet:resourceURL id="START-DAYS"/>'
            start-date='<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy" timeZone="${ model.timezone }"/>'
            days="${ model.days }"
            view-more-events-url="<portlet:renderURL windowState='maximized'/>">
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
                "viewMoreEvents": "<spring:message code='view.more.events' javaScriptEscape='true'/>",
                "returnToCalendar": "<spring:message code='return.to.calendar' javaScriptEscape='true'/>"
            }
            </script>
        </calendar-portlet>
    </div>
</div>

<script type="module" src="<c:url value='/scripts/calendar-portlet-element.js'/>"></script>

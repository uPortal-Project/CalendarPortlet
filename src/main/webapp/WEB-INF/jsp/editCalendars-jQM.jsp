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
<portlet:defineObjects/>
<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="hideUrl" escapeXml="false"><portlet:param name="action" value="hideCalendar"/>
    <portlet:param name="configurationId" value="ID"/></portlet:actionURL>
<portlet:actionURL var="showUrl" escapeXml="false"><portlet:param name="action" value="showCalendar"/>
    <portlet:param name="configurationId" value="ID"/></portlet:actionURL>
<portlet:actionURL var="newUrl" escapeXml="false"><portlet:param name="action" value="addSharedCalendar"/>
    <portlet:param name="id" value="ID"/></portlet:actionURL>

<rs:aggregatedResources path="${ usePortalJsLibs ? '/skin-mobile-shared.xml' : '/skin-mobile.xml' }"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>
<c:if test="${!usePortalJsLibs}">
    <script type="text/javascript"><rs:compressJs>
            jQuery.noConflict(true);
            fluid = null; 
            fluid_1_4 = null;
    </rs:compressJs></script>
</c:if>

<div class="portlet">

    <div data-role="header" class="titlebar portlet-titlebar">
        <a href="<portlet:renderURL portletMode="view"/>" data-role="button" data-icon="back" data-inline="true">Back</a>
        <h2>Preferences</h2>
    </div>

    <div id="${n}" class="portlet-content" data-role="content">
        <div data-role="fieldcontain">
            <fieldset data-role="controlgroup">
                <legend>Which calendars should be displayed?</legend>
                <c:set var="count" value="0"/>
                <c:forEach items="${ model.calendars }" var="calendar" varStatus="status">
                    <input type="checkbox" name="${ calendar.id }" id="${n}${ count }" ${ calendar.displayed ? 'checked' : '' } />
                    <label calendarId="${ calendar.id }" included="${ calendar.displayed }" for="${n}${ count }">${ calendar.calendarDefinition.name }</label>
                    <c:set var="count" value="${ count+1 }"/>
                </c:forEach>
                <c:forEach items="${ model.hiddenCalendars }" var="calendar">
                    <input type="checkbox" name="${ calendar.id }" id="${n}${ count }" />
                    <label calendarId="${ feed.id }" included="new" for="${n}${ count }">${ calendar.name }</label>
                    <c:set var="count" value="${ count+1 }"/>
                </c:forEach>
            </fieldset>
        </div>
    </div>

</div>
<script type="text/javascript"><rs:compressJs>
    var ${n} = ${n} || {};
    <c:choose>
        <c:when test="${!model.usePortalJsLibs}">
            ${n}.jQuery = jQuery.noConflict(true);
        </c:when>
        <c:otherwise>
            <c:set var="ns"><c:if test="${ not empty model.portalJsNamespace }">${ model.portalJsNamespace }.</c:if></c:set>
            ${n}.jQuery = ${ ns }jQuery;
        </c:otherwise>
    </c:choose>
    ${n}.jQuery(function(){
        var $ = ${n}.jQuery;
        var newUrl = '${ newUrl }';
        var showUrl = '${ showUrl }';
        var hideUrl = '${ hideUrl }';

        var updateCalendarItem = function () {
            var link, url, included;
            link = $(this);
            included = link.attr("included");
            if (included == 'new') {
                url = newUrl;
            } else if (included == 'true') {
                url = hideUrl;
            } else {
                url = showUrl;
            }
            window.location = url.replace('ID', link.attr("calendarId"));
        };
        
        $(document).ready(function () {
            $("#${n} label").click(updateCalendarItem);
            $("#${n} label").live('touchstart', updateCalendarItem);
        });
    });
</rs:compressJs></script>


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
<portlet:actionURL var="hideUrl"><portlet:param name="action" value="hideCalendar"/>
    <portlet:param name="configurationId" value="ID"/></portlet:actionURL>
<portlet:actionURL var="showUrl"><portlet:param name="action" value="showCalendar"/>
    <portlet:param name="configurationId" value="ID"/></portlet:actionURL>

<script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.5/jquery-1.5.min.js"/>"></script>

<div class="portlet">

    <div data-role="header" class="titlebar portlet-titlebar">
        <a href="<portlet:renderURL portletMode="view"/>" data-role="button" data-icon="back" data-inline="true">Back</a>
        <h2>Preferences</h2>
    </div>

    <div id="${n}" class="portlet-content" data-role="content">
        <div data-role="fieldcontain">
            <fieldset data-role="controlgroup">
                <legend>Which calendars should be displayed?</legend>
                <c:forEach items="${ model.calendars }" var="calendar">
                    <input type="checkbox" name="${ calendar.id }" id="${n}${ calendar.id }" ${ calendar.displayed ? 'checked' : '' } />
                    <label calendarId="${ calendar.id }" included="${ calendar.displayed }" for="${n}${ calendar.id }">${ calendar.calendarDefinition.name }</label>
                </c:forEach>
            </fieldset>
        </div>
    </div>

</div>
<script type="text/javascript"><rs:compressJs>
    var ${n} = ${n} || {};
    ${n}.jQuery = jQuery.noConflict(true);
    ${n}.jQuery(function(){
        var $ = ${n}.jQuery;
        var showUrl = '${ showUrl }';
        var hideUrl = '${ hideUrl }';

        var updateCalendarItem = function () {
            var link, url;
            link = $(this);
            url = (link.attr("included") == 'true') ? hideUrl : showUrl;
            window.location = url.replace('ID', link.attr("calendarId")).replace('&amp;', '&');
        };
        
        $(document).ready(function () {
            $("#${n} label").click(updateCalendarItem);
            $("#${n} label").live('touchstart', updateCalendarItem);
        });
    });
</rs:compressJs></script>


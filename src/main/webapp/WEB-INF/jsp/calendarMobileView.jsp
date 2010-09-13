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
<c:set var="mobile" value="true"/>

<c:if test="${includeJQuery}">
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.8.4/jquery-ui-1.8.4.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/fluid/1.1.2/js/fluid-all-1.1.2.min.js"/>"></script>
</c:if>
<script type="text/javascript" src="<c:url value="/scripts/CalendarView.js"/>"></script>

<script type="text/javascript">
    var cal = cal || {};
    cal.jQuery = jQuery.noConflict(${includeJQuery});
    <c:if test="${includeJQuery}">fluid = null; fluid_1_1 = null;</c:if>
    cal.jQuery(function(){
        var $ = cal.jQuery;
        var eventsUrl = '<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>';
        var days = ${ model.days };
        var calView;

        $(document).ready(function(){
            var startDate = '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy"/>';
            calView = cal.CalendarView(".upcal-miniview", { eventsUrl: eventsUrl, startDate: startDate })
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
        });
    });
</script>

<div class="upcal-miniview">

    <!-- Mini-Calendar (jQuery) -->
    <div id="${n}inlineCalendar" class="jqueryui"></div>
    
    <!-- Calendar Events List -->
    <p class="upcal-loading-message"><spring:message code="eventlist.loading"/></p>
    <div class="upcal-events"></div>

    <!-- View Links -->
    <div class="upcal-view-links">
        <a id="${n}returnToCalendarLink" class="upcal-view-return upcal-hide-on-calendar" href="javascript:;" 
                style="display:none" title="<spring:message code="return.to.calendar.link.title"/>">
            <spring:message code="return.to.calendar.link.text"/>
        </a>
    </div>
  
</div>

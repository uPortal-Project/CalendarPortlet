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

<c:set var="n"><portlet:namespace/></c:set>

<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<div id="${n}container" class="upcal-fullview">
    <c:if test="${ !model.guest && !(model.disablePreferences && (!sessionScope.isAdmin || model.disableAdministration)) }">
        <div class="upcal-edit-links">
            <c:if test="${ !model.disablePreferences && !model.guest }">
                <portlet:renderURL var="preferencesUrl" portletMode="edit"><portlet:param name="action" value="editPreferences"/></portlet:renderURL>
                <a href="${ preferencesUrl }" title="<spring:message code="edit.calendar.preferences"/>">
                    <spring:message code="preferences"/>
                </a>
            </c:if>
            <c:if test="${ sessionScope.isAdmin && !model.disableAdministration }">
                <span class="upcal-pipe">|</span>
                <portlet:renderURL var="adminUrl" portletMode="edit"><portlet:param name="action" value="administration"/></portlet:renderURL>
                <a href="${ adminUrl }" title="<spring:message code="calendar.administration"/>">
                    <spring:message code="calendar.administration"/>
                </a>
            </c:if>
        </div>
    </c:if>
    
    <div id="calendarPortletHeader" class="fl-col-mixed3 row-fluid">
        <div class="fl-col-side fl-force-right span3 pull-right">
            <div class="upcal-showcals upcal-list">
                <h3><spring:message code="my.calendars"/></h3>
                <ul>
                    <c:forEach items="${ model.calendars }" var="calendar">
                        <li class="color-${ model.colors[calendar.id] }">
                            <c:choose>
                                <c:when test="${ empty model.hiddenCalendars[calendar.id] }">
                                    <portlet:renderURL var="url"><portlet:param name="hideCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                    <a class="upcal-active" href="${ url }">
                                        <span><spring:message code="hide"/></span>
                                    </a> 
                                </c:when>
                                <c:otherwise>
                                    <portlet:renderURL var="url"><portlet:param name="showCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                    <a class="upcal-inactive" href="${ url }">
                                        <span><spring:message code="show"/></span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                            <span><spring:escapeBody htmlEscape="true">${ calendar.calendarDefinition.name }</spring:escapeBody></span>
                            <portlet:resourceURL var="exportCalendarUrl" id="exportUserCalendar">
                                <portlet:param name="configurationId" value="${ calendar.id }"/>
                            </portlet:resourceURL>
                            <a href="${ exportCalendarUrl }" class="upcal-export" title="<spring:message code='export.calendar'/>">
                                <span><spring:message code="export"/></span>
                            </a>
                        </li>
                    </c:forEach>
            </ul>
            </div>
        </div>
        
        <div class="fl-col-side fl-force-left span4">
            <div class="upcal-inline-calendar"></div>
            <div style="clear: both;"></div>
        </div>
        
        <div class="fl-col-main span5">
        
            <div class="upcal-events">
                
                <div class="upcal-event-view">
                    <div id="${n}calendarRangeSelector" class="upcal-range upcal-hide-on-event">
                        <h3><spring:message code="view"/></h3>
                            <span class="upcal-range-day" days="1">
                                <a days="1" href="javascript:;" class="${ model.days == 1 ? "selected-range" : "" }">
                                    <spring:message code="day"/>
                                </a>
                            </span>
                            <span class="upcal-pipe">|</span>
                            <span class="upcal-range-day" days="7">
                                <a days="7" href="javascript:;" class="${ model.days == 7 ? "selected-range" : "" }">
                                    <spring:message code="week"/>
                                </a>
                            </span>
                            <span class="upcal-pipe">|</span>
                            <span class="upcal-range-day" days="31">
                                <a days="31" href="javascript:;" class="${ model.days == 31 ? "selected-range" : "" }">
                                    <spring:message code="month"/>
                                </a>
                            </span>
                    </div>
            
                    <p class="upcal-loading-message"><spring:message code="loading"/></p>
                    <div class="upcal-event-errors portlet-msg-error" style="display:none"></div>

                    <div class="upcal-event-list"></div>
                </div>
                
                <div class="upcal-event-details" style="display:none">
                    <div class="upcal-event-detail">
                    </div>

                    <div class="upcal-view-links">
                        <a id="${n}returnToCalendarLink" class="upcal-view-return" href="javascript:;"
                                title="<spring:message code="return.to.event.list"/>">
                           <spring:message code="return.to.event.list"/>
                        </a>
                    </div>
        
                </div>
            </div>
            
            <!-- View Links -->
                    
        </div>
    </div>

</div>

<!-- Templates -->

<script type="text/template" id="event-list-template">
    ${"<%"} if (_(days).length == 0) { ${"%>"}
        <div class="portlet-msg-info">
            <p>No events</p>
        </div>
    ${"<%"} } else { ${"%>"}
        ${"<%"} _(days).each(function(day) { ${"%>"}
            <div class="day">
                <h2>${"<%="} day.displayName ${"%>"}</h2>
                ${"<%"} day.events.each(function(event) { ${"%>"}
                    <div class="upcal-event-wrapper">
                        <div class="upcal-event upcal-color-${"<%="} event.attributes.colorIndex ${"%>"}">
                            <div class="upcal-event-cal">
                                <span></span>
                            </div>
                            <span class="upcal-event-time">
                                ${"<%"} if (event.attributes.allDay) { ${"%>"}
                                    All Day
                                ${"<%"} } else if (event.attributes.multiDay) { ${"%>"}
                                    ${"<%="} event.attributes.dateStartTime ${"%>"} - ${"<%="} event.attributes.dateEndTime ${"%>"}
                                ${"<%"} } else if (event.attributes.endTime && (event.attributes.endTime != event.attributes.startTime || event.attributes.startDate  != event.attributes.endDate ) ) { ${"%>"}
                                    ${"<%="} event.attributes.startTime ${"%>"} - ${"<%="} event.attributes.endTime ${"%>"}
                                ${"<%"} } else { ${"%>"}
                                    ${"<%="} event.attributes.startTime ${"%>"}
                                ${"<%"} } ${"%>"}
                            </span>
                            
                            <h3 class="upcal-event-title"><a href="javascript:;">
                                ${"<%="} event.attributes.summary ${"%>"}
                            </a></h3>
                    </div>
                ${"<%"} }); ${"%>"}
            </div>
        ${"<%"} }); ${"%>"}
        </div>
    ${"<%"} } ${"%>"}
    
</script>

<script type="text/template" id="event-detail-template">
    <!-- Event title -->
    <h2>${"<%="} event.summary ${"%>"}</h2>

    <!-- Event time -->
    <div class="event-detail-date">
        <h3>Date:</h3>
        <p>
            ${"<%"} if (event.multiDay) { ${"%>"}
                ${"<%="} event.startTime ${"%>"} ${"<%="} event.startDate ${"%>"} - ${"<%="} event.endTime ${"%>"} ${"<%="} event.endDate ${"%>"}
            ${"<%"} } else if (event.allDay) { ${"%>"}
                All Day ${"<%="} event.startDate ${"%>"}
            ${"<%"} } else if (event.endTime && (event.endTime != event.startTime || event.startDate  != event.endDate ) ) { ${"%>"}
                ${"<%="} event.startTime ${"%>"} ${"<%="} event.endTime ${"%>"} ${"<%="} event.startDate ${"%>"}
            ${"<%"} } else { ${"%>"}
                ${"<%="} event.startTime ${"%>"} ${"<%="} event.startDate ${"%>"}
            ${"<%"} } ${"%>"}
        
        </p>
    </div>
    
    ${"<%"} if (event.location) { ${"%>"}
        <div>
            <h3>Location:</h3>
            <p>${"<%="} event.location ${"%>"}</p>
        </div>
    ${"<%"} } ${"%>"}

    ${"<%"} if (event.description) { ${"%>"}
        <div>
            <h3>Description:</h3>
            <p>${"<%="} event.description ${"%>"}</p>
        </div>
    ${"<%"} } ${"%>"}

    ${"<%"} if (event.link) { ${"%>"}
        <div>
            <h3>Link:</h3>
            <p>
                <a href="${"<%="} event.link ${"%>"}" target="_blank">${"<%="} event.link ${"%>"}</a>
            </p>
        </div>
    ${"<%"} } ${"%>"}
    
</script>

<script type="text/javascript"><rs:compressJs>
    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var _ = ${n}._;
        var Backbone = ${n}.Backbone;
        var upcal = ${n}.upcal;
        
        var ListView = upcal.EventListView.extend({
            el: "#${n}container .upcal-event-view",
            template: _.template($("#event-list-template").html())
        });

        var DetailView = upcal.EventDetailView.extend({
            el: "#${n}container .upcal-event-details",
            template: _.template($("#event-detail-template").html())
        });
        
        var view = new upcal.CalendarView({
            container: "#${n}container",
            listView: new ListView(),
            detailView: new DetailView(),
            eventsUrl: '<portlet:resourceURL id="START_DAYS_ETAG"/>', 
            startDate: '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy" timeZone="${ model.timezone }"/>', 
            days: "${ model.days }"
        });
        
        $("#${n}container .upcal-range-day a").click(function () {
            var link, days;
            
            link = $(this);
            days = link.attr("days");
            
            $("#${n}container .upcal-range-day a").removeClass("selected-range");
            link.addClass("selected-range");
            
            view.set("days", $(this).attr("days"));
            view.getEvents();
        });
        view.getEvents();
        
    });
</rs:compressJs></script>

<div id="${n}container" class="upcal-fullview">


</div>

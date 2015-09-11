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
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<c:set var="n"><portlet:namespace/></c:set>

<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<div id="${n}container" class="container-fluid bootstrap-styles">
    <c:if test="${ !model.guest && !(model.disablePreferences && (!sessionScope.isAdmin || model.disableAdministration)) }">
        <div class="row">
            <div class="col-md-12">
                <c:if test="${ sessionScope.isAdmin && !model.disableAdministration }">
                    <portlet:renderURL var="adminUrl" portletMode="edit"><portlet:param name="action" value="administration"/></portlet:renderURL>
                    <a class="pull-right" href="${ adminUrl }" title="<spring:message code="calendar.administration"/>">
                        <i class="fa fa-gears"></i>
                        <spring:message code="calendar.administration"/>
                    </a>
                </c:if>
                <c:if test="${ !model.disablePreferences && !model.guest }">
                    <span class="pull-right">&nbsp;|&nbsp;</span>
                    <portlet:renderURL var="preferencesUrl" portletMode="edit"><portlet:param name="action" value="editPreferences"/></portlet:renderURL>
                    <a class="pull-right" href="${ preferencesUrl }" title="<spring:message code="edit.calendar.preferences"/>">
                        <i class="fa fa-key"></i>
                        <spring:message code="preferences"/>
                    </a>
                </c:if>
            </div>
        </div>
    </c:if>

    <div class="upcal-wideview row">
        <div class="upcal-calendar-grid">
            <!-- Range Selector -->
            <div id="${n}calendarRangeSelector" class="upcal-range">
                <div class="upcal-type-selector">
                    <h3><spring:message code="view"/></h3>
                    <div class="btn-group">
                        <button days="1" href="javascript:;" class="btn btn-default upcal-range-day">
                            <spring:message code="day"/>
                        </button>
                        <button days="7" href="javascript:;" class="btn btn-default upcal-range-day active">
                            <spring:message code="week"/>
                        </button>
                        <button days="31" href="javascript:;" class="btn btn-default upcal-range-day">
                            <spring:message code="month"/>
                        </button>
                    </div>
                </div>
                <div class="clearfix upcal-inline-calendar"></div>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6 col-sm-12">
                <div class="row">
                    <div class="col-md-12 upcal-events">
                        <div class="upcal-event-view">
                            <div class="upcal-loading-message">
                                <p class="text-center"><i class="fa fa-spinner fa-spin"></i> <spring:message code="loading"/></p>
                            </div>
                            <div class="alert alert-danger" style="display:none"></div>
                            <div class="upcal-event-list"></div>
                        </div>
                        <div class="upcal-event-details" style="display:none">
                            <div class="upcal-event-detail"></div>
                            <div class="upcal-view-links">
                                <a id="${n}returnToCalendarLink" class="upcal-view-return" href="javascript:;"
                                   title="<spring:message code="return.to.event.list"/>">
                                    <i class="fa fa-arrow-left"></i> <spring:message code="return.to.event.list"/>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
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
                            <a href="${ exportCalendarUrl }" class="pull-right" title="<spring:message code='export.calendar'/>">
                                <span><i class="fa fa-download"></i> <spring:message code="export"/></span>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
</div>

<!-- Templates -->

<script type="text/template" id="event-list-template">

    ${"<%"} if (_(days).size() === 0) { ${"%>"}
    <div class="row">
        <div class="col-md-12 events-alert">
            <div class="alert alert-warning">
                <h5><i class="fa fa-exclamation-circle"></i> <spring:message code="no.events"/></h5>
            </div>
        </div>
    </div>
    ${"<%"} } else { ${"%>"}
    ${"<%"} _(days).each(function(day) { ${"%>"}
    <div class="row day">
        <div class="col-md-12">
            <h3>${"<%="} day.displayName ${"%>"}</h3>
            ${"<%"} day.events.each(function(event) { ${"%>"}
            <div class="upcal-event-wrapper">
                <div class="upcal-event upcal-color-${"<%="} event.attributes.colorIndex ${"%>"}">
                <div class="upcal-event-cal">
                    <span></span>
                </div>
                            <span class="upcal-event-time">
                                ${"<%"} if (event.attributes.allDay) { ${"%>"}
                                <spring:message code="all.day"/>
                                ${"<%"} } else if (event.attributes.multiDay) { ${"%>"}
                                ${"<%="} event.attributes.dateStartTime ${"%>"} - ${"<%="} event.attributes.dateEndTime ${"%>"}
                                ${"<%"} } else if (event.attributes.endTime && (event.attributes.endTime != event.attributes.startTime || event.attributes.startDate  != event.attributes.endDate ) ) { ${"%>"}
                                ${"<%="} event.attributes.startTime ${"%>"} - ${"<%="} event.attributes.endTime ${"%>"}
                                ${"<%"} } else { ${"%>"}
                                ${"<%="} event.attributes.startTime ${"%>"}
                                ${"<%"} } ${"%>"}
                            </span>
                <h3 class="upcal-event-title"><a href="javascript:;">${"<%="} event.attributes.summary ${"%>"}</a></h3>
            </div>
        </div>
        ${"<%"} }); ${"%>"}
    </div>
    </div>
    ${"<%"} }); ${"%>"}
    ${"<%"} } ${"%>"}

</script>

<script type="text/template" id="event-detail-template">
    <!-- Event title -->
    <h3>${"<%="} event.summary ${"%>"}</h3>

    <!-- Event time -->
    <div class="event-detail-date">
        <h3><spring:message code="date"/>:</h3>
        <p>
            ${"<%"} if (event.multiDay) { ${"%>"}
            ${"<%="} event.startTime ${"%>"} ${"<%="} event.startDate ${"%>"} - ${"<%="} event.endTime ${"%>"} ${"<%="} event.endDate ${"%>"}
            ${"<%"} } else if (event.allDay) { ${"%>"}
            <spring:message code="all.day"/> ${"<%="} event.startDate ${"%>"}
            ${"<%"} } else if (event.endTime && (event.endTime != event.startTime || event.startDate  != event.endDate ) ) { ${"%>"}
            ${"<%="} event.startTime ${"%>"} ${"<%="} event.endTime ${"%>"} ${"<%="} event.startDate ${"%>"}
            ${"<%"} } else { ${"%>"}
            ${"<%="} event.startTime ${"%>"} ${"<%="} event.startDate ${"%>"}
            ${"<%"} } ${"%>"}

        </p>
    </div>

    ${"<%"} if (event.location) { ${"%>"}
        <div>
            <h3><spring:message code="location"/>:</h3>
            <p>${"<%="} event.location ${"%>"}</p>
        </div>
    ${"<%"} } ${"%>"}

    ${"<%"} if (event.description) { ${"%>"}
        <div>
            <h3><spring:message code="description"/>:</h3>
            <p>${"<%="} event.description ${"%>"}</p>
        </div>
    ${"<%"} } ${"%>"}

    ${"<%"} if (event.link) { ${"%>"}
        <div>
            <h3><spring:message code="link"/>:</h3>
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
        eventsUrl: '<portlet:resourceURL id="START-DAYS"/>',
        startDate: '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy" timeZone="${ model.timezone }"/>',
        days: "${ model.days }"
    });

    $("#${n}container .upcal-range-day").click(function () {
        var link, days;

        link = $(this);
        days = link.attr("days");

        $("#${n}container .upcal-range-day").removeClass("active");
        link.addClass("active");

        view.set("days", $(this).attr("days"));
        view.getEvents();
    });
    view.getEvents();

});
    </rs:compressJs></script>
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

<portlet:actionURL var="showDatePickerURL" escapeXml="false"><portlet:param name='action' value='showDatePicker'/><portlet:param name='show' value='true'/></portlet:actionURL>
<portlet:actionURL var="hideDatePickerURL" escapeXml="false"><portlet:param name='action' value='showDatePicker'/><portlet:param name='show' value='false'/></portlet:actionURL>

<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<div id="${n}container" class="${n}upcal-miniview">
    <div class="container-fluid bootstrap-styles upcal-events">
        <div class="upcal-event-view">
            <div class="row">
                <div class="col-md-12">
                    <!-- Range Selector -->
                    <div id="${n}calendarRangeSelector" class="row upcal-range">
                        <div class="col-md-6">
                            <h5><spring:message code="view"/></h5>
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
                        <div class="col-md-6">
                            <h5 class="text-right"><spring:message code="date.picker"/></h5>
                            <div class="btn-group pull-right">
                                <button type="button" show="true" href="javascript:;" id="${n}showDatePicker" class="btn btn-default upcal-range-datepicker">
                                    <spring:message code="show"/>
                                </button>
                                <button type="button" show="false" href="javascript:;" id="${n}hideDatePicker" class="btn btn-default upcal-range-datepicker">
                                    <spring:message code="hide"/>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <!-- Mini-Calendar (jQuery UI) -->
                    <div class="upcal-inline-calendar"></div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <!-- Calendar Events List -->
                    <div class="upcal-loading-message">
                        <p class="text-center"><i class="fa fa-spinner fa-spin"></i> <spring:message code="loading"/></p>
                    </div>

                    <div class="alert alert-danger" style="display:none"></div>

                    <div class="upcal-event-list" style="display:none"></div>
                </div>
            </div>
            <div class="row">
                <!-- View Links -->
                <div class="col-md-12 upcal-view-links">
                    <a id="${n}viewMoreEventsLink" class="btn btn-link pull-right"
                       href="<portlet:renderURL windowState="maximized"/>"
                       title="<spring:message code="view.more.events"/>">
                        <spring:message code="view.more.events"/> <i class="fa fa-arrow-right"></i>
                    </a>

                    <a id="${n}returnToCalendarLink" class="btn btn-link" href="javascript:;"
                       style="display:none" title="<spring:message code="return.to.calendar"/>">
                        <spring:message code="return.to.calendar"/> <i class="fa fa-calendar"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="upcal-event-details" style="display:none">

            <div class="row upcal-event-detail"></div>

            <div class="row utilities upcal-list-link">
                <div class="col-md-12">
                    <a class="upcal-view-return" href="javascript:;"
                       title="<spring:message code="return.to.calendar"/>" data-role="button">
                        <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
                    </a>
                </div>
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
                    <h4><i class="fa fa-exclamation-circle"></i> <spring:message code="no.events"/></h4>
                </div>
            </div>
        </div>
    ${"<%"} } else { ${"%>"}
        ${"<%"} _(days).each(function(day) { ${"%>"}
            <div class="row day">
                <div class="col-md-12">
                    <h4>${"<%="} day.displayName ${"%>"}</h4>
                    ${"<%"} day.events.each(function(event) { ${"%>"}
                        <div class="upcal-event-wrapper">
                            <div class="upcal-event upcal-color-${"<%="} event.attributes.colorIndex ${"%>"}">
                                <div class="upcal-event-cal">
                                    <span></span>
                                </div>
                                <span><strong>
                                    ${"<%"} if (event.attributes.allDay) { ${"%>"}
                                        <spring:message code="all.day"/>
                                    ${"<%"} } else if (event.attributes.multiDay) { ${"%>"}
                                        ${"<%="} event.attributes.dateStartTime ${"%>"} - ${"<%="} event.attributes.dateEndTime ${"%>"}
                                    ${"<%"} } else if (event.attributes.endTime && (event.attributes.endTime != event.attributes.startTime || event.attributes.startDate  != event.attributes.endDate ) ) { ${"%>"}
                                        ${"<%="} event.attributes.startTime ${"%>"} - ${"<%="} event.attributes.endTime ${"%>"}
                                    ${"<%"} } else { ${"%>"}
                                        ${"<%="} event.attributes.startTime ${"%>"}
                                    ${"<%"} } ${"%>"}
                                </span></strong>
                                <h5 class="upcal-event-title"><a href="javascript:;">${"<%="} event.attributes.summary ${"%>"}</a></h5>
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
    <h4>${"<%="} event.summary ${"%>"}</h4>
    <!-- Event time -->
    <div class="row event-detail-date">
        <div class="col-md-12">
            <div class="upcal-event-wrapper">
                <span><strong>
                    ${"<%"} if (event.multiDay) { ${"%>"}
                    ${"<%="} event.startTime ${"%>"} ${"<%="} event.startDate ${"%>"} - ${"<%="} event.endTime ${"%>"} ${"<%="} event.endDate ${"%>"}
                    ${"<%"} } else if (event.allDay) { ${"%>"}
                    <spring:message code="all.day"/> ${"<%="} event.startDate ${"%>"}
                    ${"<%"} } else if (event.endTime && (event.endTime != event.startTime || event.startDate  != event.endDate ) ) { ${"%>"}
                    ${"<%="} event.startTime ${"%>"} ${"<%="} event.endTime ${"%>"} ${"<%="} event.startDate ${"%>"}
                    ${"<%"} } else { ${"%>"}
                    ${"<%="} event.startTime ${"%>"} ${"<%="} event.startDate ${"%>"}
                    ${"<%"} } ${"%>"}
                </span></strong>
            </div>
        </div>
    </div>

    ${"<%"} if (event.location) { ${"%>"}
    <div class="row">
        <div class="col-md-12">
            <h5><spring:message code="location"/>:</h5>
            <p>${"<%="} event.location ${"%>"}</p>
        </div>
    </div>
    ${"<%"} } ${"%>"}

    ${"<%"} if (event.description) { ${"%>"}
    <div class="row">
        <div class="col-md-12">
            <h5>Description:</h5>
            <p>${"<%="} event.description ${"%>"}</p>
        </div>
    </div>
    ${"<%"} } ${"%>"}

    ${"<%"} if (event.link) { ${"%>"}
    <div class="row">
        <div class="col-md-12">
            <h5>Link:</h5>
            <p><a href="${"<%="} event.link ${"%>"}" target="_blank">${"<%="} event.link ${"%>"}</a></p>
        </div>
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

    $("#${n}container .upcal-range-datepicker").click(function(event){
        var show = $(event.target).attr("show");
        showDatePicker(show);
        $.ajax({
            url: show == "true" ? "${showDatePickerURL}" : "${hideDatePickerURL}",
            success: function (data) {
            }
        });
    });

    var showDatePicker = function(show) {
        if(show == "true") {
            $('#${n}container .upcal-inline-calendar').show();
            $('#${n}showDatePicker').addClass('active');
            $('#${n}hideDatePicker').removeClass('active');
        } else {
            $('#${n}container .upcal-inline-calendar').hide();
            $('#${n}hideDatePicker').addClass('active');
            $('#${n}showDatePicker').removeClass('active');
        }
    };

    showDatePicker("${model.showDatePicker}");
    view.getEvents();
});
</rs:compressJs></script>
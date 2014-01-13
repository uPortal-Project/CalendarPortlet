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

<c:set var="n"><portlet:namespace/></c:set>

<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<c:set var="mobile" value="${ true }"/>
<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<div id="${n}container" class="portlet ptl-calendar view-mobile">
	<div data-role="content" class="portlet-content">
        <div class="upcal-events">

            <div class="upcal-event-view">
                <!-- Mini-Calendar (jQuery UI) -->
                <div class="upcal-inline-calendar"></div>

                <!-- Calendar Events List -->
                <div class="upcal-loading-message portlet-msg-info portlet-msg info">
                    <p><spring:message code="loading"/></p>
                </div>

                <div class="upcal-event-errors portlet-msg-error" style="display:none"></div>
                <div class="upcal-event-list" style="display:none">
                </div>
                
                <c:if test="${ !model.disablePreferences && !model.guest }">
                    <div class="utilities upcal-view-links upcal-hide-on-event">
                        <portlet:renderURL var="preferencesUrl" portletMode="edit"><portlet:param name="action" value="editPreferences"/></portlet:renderURL>
                        <a data-role="button" href="${ preferencesUrl }" title="<spring:message code="edit.preferences"/>">
                            <spring:message code="preferences"/>
                        </a>
                    </div>
                </c:if>
                
            </div>

                <div class="upcal-event-details" style="display:none">

                    <div class="upcal-event-detail">
                    </div>

                    <div class="utilities upcal-list-link">
                        <a class="upcal-view-return" href="javascript:;" 
                                title="<spring:message code="return.to.calendar"/>" data-role="button">
                            <spring:message code="return.to.calendar"/>
                        </a>
                    </div>

                </div>
            </div>
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
                            <div class="upcal-event-time">
                                ${"<%"} if (event.attributes.allDay) { ${"%>"}
                                    All Day
                                ${"<%"} } else { ${"%>"}
                                    ${"<%="} event.attributes.dateStartTime ${"%>"}
                                ${"<%"} } ${"%>"}
                            </div>
                            
                            <a class="upcal-event-link ui-link" href="javascript:;">
                                ${"<%="} event.attributes.summary ${"%>"}
                            </a>
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
        view.getEvents();
        
    });
</rs:compressJs></script>

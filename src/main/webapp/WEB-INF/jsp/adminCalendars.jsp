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
<rs:aggregatedResources path="${ usePortalJsLibs ? '/skin-shared.xml' : '/skin.xml' }"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<div class="container-fluid bootstrap-styles upcal-adminview">
    <div class="row">
        <div class="col-md-12">
            <!-- Add Calendar -->
            <div class="pull-right">
                <portlet:renderURL var="addCalendarUrl"><portlet:param name="action" value="createCalendarDefinition"/></portlet:renderURL>
                <a href="${ addCalendarUrl }" title="<spring:message code="add.a.calendar"/>">
                    <i class="fa fa-plus"></i> <spring:message code="add.a.calendar"/>
                </a> |
                <portlet:renderURL var="returnUrl" portletMode="view"/>
                <a href="${ returnUrl }" title="<spring:message code="return.to.calendar"/>">
                    <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
                </a>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6 col-md-offset-3">
            <h4><spring:message code="calendar.administration"/></h4>
            <c:choose>
                <c:when test="${ empty calendars }">
                    <spring:message code="add.a.calendar" var="linkText"/>
                    <spring:message code="no.calendars.defined" arguments="<a href='${ addCalendarUrl }'>${ linkText }</a>"/>
                </c:when>
                <c:otherwise>
                    <table class="table tabale-condensed">
                        <c:forEach items="${ calendars }" var="calendar">
                            <tr>
                                <!-- Calendar name -->
                                <td>
                                    <spring:escapeBody htmlEscape="true">${ calendar.name }</spring:escapeBody>
                                </td>
                                <!-- Edit -->
                                <td>
                                    <portlet:renderURL var="editCalendarUrl">
                                        <portlet:param name="action" value="editCalendarDefinition"/>
                                        <portlet:param name="id" value="${ calendar.id }"/>
                                    </portlet:renderURL>
                                </td>
                                <td>
                                    <a class="upcal-edit" href="${ editCalendarUrl }" title="<spring:message code="edit.calendar"/>">
                                        <span><i class="fa fa-edit"></i> <spring:message code="edit.calendar"/></span>
                                    </a>
                                </td>
                                <td>
                                    <!-- Delete -->
                                    <portlet:actionURL var="deleteCalendarUrl">
                                        <portlet:param name="action" value="deleteSharedCalendar"/>
                                        <portlet:param name="calendarId" value="${ calendar.id }"/>
                                    </portlet:actionURL>
                                    <a class="upcal-delete" href="${ deleteCalendarUrl }" title="<spring:message code="delete.calendar"/>">
                                        <span><i class="fa fa-trash-o"></i> <spring:message code="delete.calendar"/></span>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
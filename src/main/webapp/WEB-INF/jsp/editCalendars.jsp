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
<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<portlet:renderURL portletMode="view" var="returnUrl"/>

<div class="container-fluid bootstrap-styles">

    <div class="row">
        <div class="col-md-4">
            <h4><spring:message code="edit.calendars"/></h4>
        </div>
        <div class="col-md-8">
            <c:if test="${ sessionScope.isAdmin }">
                <portlet:renderURL var="adminUrl" portletMode="edit"><portlet:param name="action" value="administration"/></portlet:renderURL>
                <a class="pull-right" href="${ adminUrl }" title="<spring:message code="calendar.administration"/>">
                    <i class="fa fa-gears"></i> <spring:message code="calendar.administration"/></a>
            </c:if>
        </div>
    </div>
    <div class="row">
        <div class="col-md-8 col-md-offset-2">
            <h5><spring:message code="my.calendars"/></h5>
        </div>
        <div class="col-md-8 col-md-offset-2">
            <table class="table tabale-condensed">
                <c:forEach items="${ model.mycalendars }" var="calendar">
                    <tr>
                        <!-- Calendar name -->
                        <td>
                            <spring:escapeBody htmlEscape="true">${ calendar.calendarDefinition.name }</spring:escapeBody>
                        </td>
                        <!-- Edit -->
                        <td>
                            <portlet:renderURL var="editCalendarUrl">
                                <portlet:param name="action" value="editUrl"/>
                                <portlet:param name="id" value="${ calendar.id }"/>
                            </portlet:renderURL>
                            <a href="${ editCalendarUrl }" title="<spring:message code="edit.calendar"/>">
                                <span><i class="fa fa-edit"></i> <spring:message code="edit.calendar"/></span>
                            </a>
                        </td>
                        <td>
                            <!-- Delete -->
                            <portlet:actionURL var="deleteCalendarUrl">
                                <portlet:param name="action" value="deleteUserCalendar"/>
                                <portlet:param name="configurationId" value="${ calendar.id }"/>
                            </portlet:actionURL>
                            <a href="${ deleteCalendarUrl }" title="<spring:message code="delete.calendar"/>">
                                <span><i class="fa fa-trash-o"></i> <spring:message code="delete.calendar"/></span>
                            </a>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
        <div class="col-md-8 col-md-offset-2">
            <portlet:renderURL var="addCalendar">
                <portlet:param name="action" value="editUrl"/>
            </portlet:renderURL>
            <a href="${ addCalendar }" title="<spring:message code="add.a.calendar"/>">
                <i class="fa fa-plus"></i> <spring:message code="add.a.calendar"/>
            </a>
        </div>
        <div class="col-md-8 col-md-offset-2">
            <h5><spring:message code="preconfigured.calendars"/></h5>
            <table class="table table-condensed">
                <c:forEach items="${ model.calendars }" var="calendar">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${ calendar.displayed }">
                                    <portlet:actionURL var="displayURL"><portlet:param name="action" value="hideCalendar"/><portlet:param name="configurationId" value="${ calendar.id }"/></portlet:actionURL>
                                    <a href="${ displayURL }">
                                        <span><i class="fa fa-eye-slash"></i> <spring:message code="hide"/></span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <portlet:actionURL var="displayURL"><portlet:param name="action" value="showCalendar"/><portlet:param name="configurationId" value="${ calendar.id }"/></portlet:actionURL>
                                    <a class="upcal-inactive" href="${ displayURL }">
                                        <span><i class="fa fa-eye"></i> <spring:message code="show"/></span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <span><spring:escapeBody htmlEscape="true">${ calendar.calendarDefinition.name }</spring:escapeBody></span>
                        </td>
                        <td>
                            <portlet:resourceURL var="exportCalendarUrl" id="exportUserCalendar">
                                <portlet:param name="configurationId" value="${ calendar.id }"/>
                            </portlet:resourceURL>
                            <c:set var="editAction" value="${ model.predefinedEditActions[calendar.calendarDefinition.className] }"/>
                            <c:choose>
                                <c:when test="${ not empty editAction }">
                                    <portlet:renderURL var="editCalendarUrl"><portlet:param name="action" value="${ editAction }"/><portlet:param name="configurationId" value="${ calendar.id }"/></portlet:renderURL>
                                    <a class="upcal-edit" href="${ editCalendarUrl }" title="<spring:message code="edit.calendar"/>">
                                        <span><i class="fa fa-edit"></i> <spring:message code="edit"/></span>
                                    </a>
                                </c:when>
                                <c:otherwise>&nbsp;</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:forEach items="${ model.hiddencalendars }" var="calendar">
                    <tr>
                        <td>
                            <portlet:actionURL var="displayURL">
                                <portlet:param name="action" value="addSharedCalendar"/>
                                <portlet:param name="definitionId" value="${ calendar.id }"/>
                            </portlet:actionURL>
                            <a class="upcal-inactive" href="${ displayURL }">
                                <span><i class="fa fa-eye"></i> <spring:message code="show"/></span>
                            </a>
                        </td>
                        <td>
                            <span><spring:escapeBody htmlEscape="true">${ calendar.name }</spring:escapeBody></span>
                        </td>
                        <td>
                            &nbsp;
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </div>
    <div class="row">
        <c:if test="${model.timezoneReadOnly == false}">
            <h4><spring:message code="preferences"/></h4>
            <div id="${n}calendar-submission-success" class="col=md-12" style="display: none;">
                <p>
                    <spring:message code="your.preferences.have.been.saved.successfully"/>
                    <a href="<portlet:renderURL portletMode="view"/>" title="<spring:message code="return.to.calendar"/>">
                        <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
                    </a>
                </p>
            </div>
            <portlet:actionURL var="postUrl"><portlet:param name="action" value="editPreferences"/></portlet:actionURL>
            <form:form name="calendar" commandName="calendarPreferencesCommand" action="${postUrl}" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-md-3 control-label"><spring:message code="time.zone"/></label>
                    <div class="col-md-3">
                        <form:select path="timezone" class="form-control">
                            <c:forEach items="${timezones}" var="zone">
                                <spring:message var="message" code="timezone.${ zone }" text="${ zone }"/>
                                <form:option label="${message}" value="${ zone }"/>
                            </c:forEach>
                        </form:select>
                        <form:errors path="timezone" cssClass="alert alert-danger"/>
                    </div>
                </div>
                <div class="upcal-button-group col-md-offset-3 col-md-6">
                    <button type="submit" class="btn btn-primary"><spring:message code="save.preferences"/></button>
                    <a class="btn btn-link" href="${ returnUrl }"><spring:message code="cancel"/></a>
                </div>
            </form:form>
        </c:if>
    </div>

    <c:if test="${renderRequest.parameterMap['preferencesSaved'][0] == 'true'}">

        <script type="text/javascript"><rs:compressJs>
        ${n}.jQuery(function() {
            var $ = ${n}.jQuery;
            $("#${n}calendar-submission-success").slideDown("slow");
        });
        </rs:compressJs></script>
    </c:if>

</div>

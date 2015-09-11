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

<div class="container-fluid bootstrap-styles" role="section">

    <!-- Portlet Titlebar -->
    <div class="row" role="sectionhead">
        <div class="col-md-6">
            <h4 role="heading">
                <spring:message code="add.a.calendar"/>
            </h4>
        </div>
        <div class="col-md-6">
            <portlet:renderURL var="returnUrl" portletMode="view"/>
            <a class="pull-right" href="${ returnUrl }" title="<spring:message code="return.to.calendar"/>">
                <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
            </a>
        </div>
    </div>
    <div class="row" role="main">
        <div class="col-md-6 col-md-offset-2">
            <portlet:renderURL escapeXml='false' var="postUrl"><portlet:param name="action" value="createCalendarDefinition2"/></portlet:renderURL>
            <form:form name="calendar" commandName="calendarDefinitionForm" action="${postUrl}" class="form-horizontal" role="form">
                <div class="row">
                    <div class="col-md-12">
                        <spring:hasBindErrors name="calendarDefinitionForm">
                            <div class="alert alert-danger" role="alert">
                                <form:errors path="*" element="div"/>
                            </div>
                        </spring:hasBindErrors>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label"><spring:message code="calendar.functional.name"/></label>
                        <div class="col-md-8">
                            <form:input class="form-control" path="fname"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label"><spring:message code="calendar.type"/></label>
                        <div class="col-md-8">
                            <form:select class="form-control" path="className">
                                <c:forEach items="${ adapters }" var="adapter">
                                    <spring:message code="${ adapter.value.titleKey }" var="label"/>
                                    <form:option value="${ adapter.key }" label="${ label }"/>
                                </c:forEach>
                            </form:select>
                        </div>
                        <div class="upcal-button-group col-md-offset-3 col-md-6">
                            <button type="submit" class="btn btn-primary"><spring:message code="next"/></button>
                            <portlet:renderURL var="returnToAdminUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
                            <a class="btn btn-link" href="${ returnToAdminUrl }"><spring:message code="cancel"/></a>
                        </div>
                    </div>
                </div>
            </form:form>
        </div>
    </div>
</div>
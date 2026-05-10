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
<%@ taglib prefix="editPreferences" tagdir="/WEB-INF/tags/edit-preferences" %>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<c:set var="n"><portlet:namespace/></c:set>

<%-- skin/CSS bundle. The legacy scripts.jsp include (which set up the
     up.jQuery / up._ / up.Backbone globals and a Backbone view for the
     role-params row) has been replaced below by a vanilla <template>
     element + addEventListener logic. --%>
<rs:aggregatedResources path="skin-shared.xml"/>

<div class="container-fluid" role="section">
    <div class="row">
        <div class="col-md-4">
            <h4 role="heading"><spring:message code="edit.calendar"/></h4>
        </div>
        <div class="col-md-8">
        <!-- Return to Calendar -->
            <div class="float-end">
                <portlet:renderURL var="returnUrl" portletMode="view"/>
                <a href="${ returnUrl }" title="<spring:message code="return.to.calendar"/>">
                    <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
                </a>
            </div>
        </div>
    </div>

    <div class="row" role="main">
        <portlet:actionURL var="postUrl" escapeXml="false"/>
        <form:form id="${n}parameters" name="calendar" commandName="calendarDefinitionForm" action="${postUrl}" class="form-horizontal" role="form">
                <input type="hidden" id="action" name="pP_action" value="editCalendarDefinition"/>
            <spring:hasBindErrors name="calendarDefinitionForm">
                <div class="col-md-12">
                    <div class="alert alert-danger" role="alert">
                        <form:errors path="*" element="div"/>
                    </div>
                </div>
            </spring:hasBindErrors>
            <form:hidden path="id"/>
            <form:hidden path="fname"/>
            <form:hidden path="className"/>

            <div class="form-group">
                <label class="col-md-3 control-label"><spring:message code="calendar.name"/></label>
                <div class="col-md-9">
                    <form:input path="name" class="form-control"/>
                </div>
            </div>
            <c:forEach items="${ adapter.parameters }" var="parameter">
                <c:set var="paramPath" value="parameters['${ parameter.name }'].value"/>
                <div class="form-group">
                    <label class="col-md-3 control-label"><spring:message code="${ parameter.labelKey }"/></label>
                    <div class="col-md-9">
                        <editPreferences:preferenceInput cssClass="form-control" input="${ parameter.input }" path="${ paramPath }"/>
                        <c:if test="${ not empty parameter.example }">
                            <p>Example: ${ parameter.example }</p>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
            <div class="form-group">
                <label class="col-md-3 control-label"><spring:message code="default.roles"/></label>
                <div class="col-md-6">
                    <form:checkboxes items="${ availableRoles }" path="role" element="div class='checkbox'"/>
                </div>
            </div>
            <div class="upcal-button-group offset-md-3 col-md-6">
                <button type="submit" class="btn btn-primary"><spring:message code="save.calendar"/></button>
                <portlet:renderURL var="returnToAdminUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
                <a class="btn btn-link" href="${ returnToAdminUrl }"><spring:message code="cancel"/></a>
            </div>
        </form:form>
    </div>
</div>


<template id="${n}roleParamTemplate">
    <div class="role-param-row">
        <div class="col-md-3">
            <input name="role" type="text" class="form-control"/>
        </div>
        <div class="col-md-3">
            <a class="delete-parameter-value-link" href="javascript:void(0)"><spring:message code="remove.role"/></a>
        </div>
    </div>
</template>

<script>
(function () {
    var ns = "${n}";
    var template = document.getElementById(ns + "roleParamTemplate");
    var paramsForm = document.getElementById(ns + "parameters");
    if (!template || !paramsForm) {
        return;
    }

    paramsForm.querySelectorAll(".role-params a.add-parameter-value-link").forEach(function (addLink) {
        addLink.addEventListener("click", function (ev) {
            ev.preventDefault();
            var clone = template.content.cloneNode(true);
            addLink.parentNode.insertBefore(clone, addLink);
        });
    });

    paramsForm.addEventListener("click", function (ev) {
        var deleteLink = ev.target.closest("a.delete-parameter-value-link");
        if (!deleteLink) return;
        ev.preventDefault();
        var row = deleteLink.closest(".role-param-row");
        if (row) row.remove();
    });
})();
</script>

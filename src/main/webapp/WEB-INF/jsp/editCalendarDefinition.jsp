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

<%@ taglib prefix="editPreferences" tagdir="/WEB-INF/tags/edit-preferences" %>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/scripts.jsp"/>

<script type="text/javascript"><rs:compressJs>
    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var _ = ${n}._;
        var Backbone = ${n}.Backbone;
        var upcal = ${n}.upcal;

        var RoleParamView = Backbone.View.extend({
            initialize: function() {
                this.render();
            },
            render: function( ){
                // Compile the template using underscore
                var template = _.template( $("#${n}roleParamTemplate").html(), {} );
                // Load the compiled HTML into the Backbone "el"
                this.$el.html( template );
            }
        });

        $("#${n}parameters .role-params").delegate("a.delete-parameter-value-link", "click", function() {
            var link = this;
            $(link).parent().remove();
        });

        $("#${n}parameters .role-params a.add-parameter-value-link").click(function() {
            var link = this;
            var roleParamView = new RoleParamView();
            console.log(roleParamView);
            $(link).before(roleParamView.$el);
        });

    });
</rs:compressJs></script>

<div class="container-fluid" role="section">
    <div class="row">
        <div class="col-md-4">
            <h2 role="heading"><spring:message code="edit.calendar"/></h2>
        </div>
        <div class="col-md-8">
        <!-- Add Calendar -->
            <div class="pull-right">
                <portlet:renderURL var="addCalendarUrl"><portlet:param name="action" value="createCalendarDefinition"/></portlet:renderURL>
                <a href="${ addCalendarUrl }" title="<spring:message code="add.a.calendar"/>">
                    <i class="fa fa-plus"></i> <spring:message code="add.a.calendar"/>
                </a> |
                <portlet:renderURL var="returnUrl" portletMode="view"/>
                <a href="${ returnUrl }" title="<spring:message code="return.to.calendar"/>">
                    <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
                </a> |
                <portlet:renderURL var="returnUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
                <a class="upcal-view-return" href="${ returnUrl }" title="<spring:message code="return.to.administration"/>">
                    <i class="fa fa-gear"></i> <spring:message code="return.to.administration"/>
                </a>
            </div>
        </div>
    </div>

    <div class="row" role="main">
    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editCalendarDefinition"/></portlet:actionURL>
    <form:form id="${n}parameters" name="calendar" commandName="calendarDefinitionForm" action="${postUrl}" class="form-horizontal" role="form">
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
            <div class="col-md-6">
                <form:input path="name" class="form-control"/>
            </div>
        </div>
        <c:forEach items="${ adapter.parameters }" var="parameter">
            <c:set var="paramPath" value="parameters['${ parameter.name }'].value"/>
            <div class="form-group">
                <label class="col-md-3 control-label"><spring:message code="${ parameter.labelKey }"/></label>
                <div class="col-md-6">
                    <editPreferences:preferenceInput input="${ parameter.input }" path="${ paramPath }"/>
                    <c:if test="${ not empty parameter.example }">
                        <p>Example: ${ parameter.example }</p>
                    </c:if>
                </div>
            </div>
        </c:forEach>
        <div class="form-group role-params">
            <label class="col-md-3 control-label"><spring:message code="default.roles"/></label>
            <div class="col-md-6">
                <c:forEach items="${ calendarDefinitionForm.role }" var="role">
                    <div class="col-md-3">
                        <input name="role" value="${ role }" type="text" class="form-control"/>
                    </div>
                    <div class="col-md-3">
                        <a class="delete-parameter-value-link" href="javascript:;"><i class="fa fa-trash-o"></i> <spring:message code="remove.role"/></a>
                    </div>
                </c:forEach>
            </div>
            <div class="col-md-6 col-md-offset-3">
                <a class="add-parameter-value-link" href="javascript:;" paramName="role"><i class="fa fa-plus"></i> <spring:message code="add.a.role"/></a>
            </div>
        </div>
        <div class="col-md-12">
            <button type="submit" class="btn btn-primary"><spring:message code="save.calendar"/></button>
        </div>
    </form:form>
</div>

<script id="${n}roleParamTemplate" type="text/template">
    <div class="col-md-3">
        <input name="role" type="text" class="form-control"/>
    </div>
    <div class="col-md-3">
        <a class="delete-parameter-value-link" href="javascript:;"><spring:message code="remove.role"/></a>
    </div>
</script>

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
//            console.log(roleParamView);
            $(link).before(roleParamView.$el);
        });

    });
</rs:compressJs></script>

<div class="container-fluid bootstrap-styles" role="section">
    <div class="row">
        <div class="col-md-4">
            <h4 role="heading"><spring:message code="edit.calendar"/></h4>
        </div>
        <div class="col-md-8">
        <!-- Return to Calendar -->
            <div class="pull-right">
                <portlet:renderURL var="returnUrl" portletMode="view"/>
                <a href="${ returnUrl }" title="<spring:message code="return.to.calendar"/>">
                    <i class="fa fa-arrow-left"></i> <spring:message code="return.to.calendar"/>
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
            <div class="upcal-button-group col-md-offset-3 col-md-6">
                <button type="submit" class="btn btn-primary"><spring:message code="save.calendar"/></button>
                <portlet:renderURL var="returnToAdminUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
                <a class="btn btn-link" href="${ returnToAdminUrl }"><spring:message code="cancel"/></a>
            </div>
        </form:form>
    </div>
</div>


<script id="${n}roleParamTemplate" type="text/template">
    <div class="col-md-3">
        <input name="role" type="text" class="form-control"/>
    </div>
    <div class="col-md-3">
        <a class="delete-parameter-value-link" href="javascript:;"><spring:message code="remove.role"/></a>
    </div>
</script>

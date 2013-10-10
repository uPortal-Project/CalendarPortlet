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
            $(document).ready(function(){
                var RoleParamView = Backbone.View.extend({
                    initialize: function(){
                        this.render();
                    },
                    render: function(){
                        // Compile the template using underscore
                        var template = _.template( $("#${n}roleParamTemplate").html(), {} );
                        // Load the compiled HTML into the Backbone "el"
                        this.$el.html( template );
                    }
                });
                

                $("#${n}parameters .role-params").delegate("a.delete-parameter-value-link", "click", function () {
                	var link = this;
                	$(link).parent().remove();
                });
                
                $("#${n}parameters .role-params a.add-parameter-value-link").click(function () {
                	var link = this;
                    var roleParamView = new RoleParamView();
                    console.log(roleParamView);
                	$(link).before(roleParamView.$el);
                });
                
            });
        });
    </rs:compressJs></script>

<div class="fl-widget portlet" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <spring:message code="edit.calendar"/>
        </h2>
    </div> <!-- end: portlet-titlebar -->

    <div class="fl-widget-content content portlet-content" role="main">

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editCalendarDefinition"/></portlet:actionURL>
    <form:form name="calendar" commandName="calendarDefinitionForm" action="${postUrl}">
    
        <spring:hasBindErrors name="calendarDefinitionForm">
            <div class="portlet-msg-error" role="alert">
                <form:errors path="*" element="div"/>
            </div> <!-- end: portlet-msg -->
        </spring:hasBindErrors>
    
       	<form:hidden path="id"/>
        <form:hidden path="fname"/>
        <form:hidden path="className"/>
        
        <table id="${n}parameters">
            <tbody>
		<tr>
			<td><label class="portlet-form-field-label">
                <spring:message code="calendar.name"/>:
			</label></td>
			<td><form:input path="name" size="50"/></td>
		</tr>
        <c:forEach items="${ adapter.parameters }" var="parameter">
            <c:set var="paramPath" value="parameters['${ parameter.name }'].value"/>            
            <tr>
                <td><spring:message code="${ parameter.labelKey }"/></td>
                <td>
                    <editPreferences:preferenceInput input="${ parameter.input }" 
                        path="${ paramPath }"/>
                    <c:if test="${ not empty parameter.example }">
                        <p>Example: ${ parameter.example }</p>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <tr>
            <td><label class="portlet-form-field-label"><spring:message code="default.roles"/></label></td>
            <td class="role-params">
                <c:forEach items="${ calendarDefinitionForm.role }" var="role">
                  <div>
                     <input name="role" value="${ role }" type="text"/>
                     <a class="delete-parameter-value-link" href="javascript:;"><spring:message code="remove.role"/></a>
                  </div>
                </c:forEach>
                <a class="add-parameter-value-link" href="javascript:;" paramName="role">
                    <spring:message code="add.a.role"/>
                </a>
            </td>
        </tr>
        </tbody>
        </table>        
        
        <div class="buttons">
            <button type="submit" class="portlet-form-button btn">
                <spring:message code="save.calendar"/>
            </button>
        </div>
    </form:form>

    <div class="upcal-view-links">
        <portlet:renderURL var="returnUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
        <a class="upcal-view-return" href="${ returnUrl }" title="<spring:message code="return.to.administration"/>">
           <spring:message code="return.to.administration"/>
        </a>
    </div>
    
</div>

<script id="${n}roleParamTemplate" type="text/template">
    <div>
        <input name="role" type="text"/>
        <a class="delete-parameter-value-link" href="javascript:;"><spring:message code="remove.role"/></a>
    </div>
</script>

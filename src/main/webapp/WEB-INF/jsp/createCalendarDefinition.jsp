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
<rs:aggregatedResources path="${ usePortalJsLibs ? '/skin-shared.xml' : '/skin.xml' }"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<div class="fl-widget portlet" role="section">

    <!-- Portlet Titlebar -->
    <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
        <h2 class="title" role="heading">
            <spring:message code="add.a.calendar"/>
        </h2>
    </div> <!-- end: portlet-titlebar -->

    <div class="fl-widget-content content portlet-content" role="main">

    <portlet:renderURL escapeXml='false' var="postUrl"><portlet:param name="action" value="createCalendarDefinition2"/></portlet:renderURL>
    <form:form name="calendar" commandName="calendarDefinitionForm" action="${postUrl}">
    
        <spring:hasBindErrors name="calendarDefinitionForm">
            <div class="portlet-msg-error portlet-msg error" role="alert">
                <form:errors path="*" element="div"/>
            </div> <!-- end: portlet-msg -->
        </spring:hasBindErrors>

        <table>
            <tbody>    
                <tr><td>
                    <label class="portlet-form-field-label">
                        <spring:message code="calendar.functional.name"/> <img src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/information.png"/>" title="<spring:message code="unique.programmatic.name.for.this.calendar.instruction"/>" />:
                    </label></td>
                    <td><form:input path="fname" size="50"/></td>
                </tr>
                <tr>
                    <td><label class="portlet-form-field-label">
                        <spring:message code="calendar.type"/>:
                    </label></td>
                    <td><form:select path="className">
                        <c:forEach items="${ adapters }" var="adapter">
                            <spring:message code="${ adapter.value.titleKey }" var="label"/>
                            <form:option value="${ adapter.key }" label="${ label }"/>
                        </c:forEach>
                    </form:select></td>
                </tr>
            </tbody>
        </table>
        
        <div class="buttons">
            <button type="submit" class="portlet-form-button">
                <spring:message code="next"/>
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

</div>
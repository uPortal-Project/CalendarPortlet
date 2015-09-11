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
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<c:set var="n"><portlet:namespace/></c:set>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<div class="upcal-edit-urlview container-fluid bootstrap-styles">

	<h4><spring:message code="edit.calendar"/></h4>

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editUrl"/></portlet:actionURL>
    <form:form name="calendar" cssClass="form-horizontal" commandName="userHttpIcalCalendarForm" action="${postUrl}">

	    <spring:hasBindErrors name="userHttpIcalCalendarForm">
	        <div class="portlet-msg-error" role="alert">
	            <form:errors path="*" element="div"/>
	        </div> <!-- end: portlet-msg -->
	    </spring:hasBindErrors>

        <form:hidden path="id"/>

        <div class="form-group">
			<label class="portlet-form-field-label col-md-3 control-label">
			    <spring:message code="calendar.name"/>:
			</label>
            <div class="col-md-9">
                <form:input cssClass="form-control" path="name" size="50"/>
            </div>
        </div>
        <div class="form-group">
            <label class="portlet-form-field-label col-md-3 control-label">
                <spring:message code="calendar.url"/>:
            </label>
            <div class="col-md-9">
                <form:input cssClass="form-control" path="url" size="50"/>
            </div>
        </div>
        <div class="upcal-button-group col-md-offset-3 col-md-6">
            <button type="submit" class="portlet-form-button btn btn-primary">
                <spring:message code="save.calendar"/>
            </button>
            <portlet:renderURL var="returnUrl"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>
            <a class="btn btn-link" href="${ returnUrl }" title="<spring:message code="return.to.preferences"/>">
                <spring:message code="cancel"/>
            </a>
        </div>
    </form:form>
</div>
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
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<div class="upcal-edit-urlview">

	<h2><spring:message code="view.calendaredit.header"/></h2>

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editUrl"/></portlet:actionURL>
    <form:form name="calendar" commandName="userHttpIcalCalendarForm" action="${postUrl}">

	    <spring:hasBindErrors name="userHttpIcalCalendarForm">
	        <div class="portlet-msg-error" role="alert">
	            <form:errors path="*" element="div"/>
	        </div> <!-- end: portlet-msg -->
	    </spring:hasBindErrors>

        <form:hidden path="id"/>

        <p>
			<label class="portlet-form-field-label">
			    <spring:message code="form.calendar.name"/>:
			</label>
            <form:input path="name" size="50"/>
        </p>
        <p>
            <label class="portlet-form-field-label">
                <spring:message code="form.calendar.url"/>:
            </label>
            <form:input path="url" size="50"/>
    	</p>
        <p>
            <button type="submit" class="portlet-form-button">
                <spring:message code="form.calendar.save"/>
            </button>
        </p>
        
    </form:form>
    
	<div class="upcal-view-links">
        <portlet:renderURL var="returnUrl"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>
	    <a class="upcal-view-return" href="${ returnUrl }" 
	           title="<spring:message code="return.to.preferences.link.title"/>">
	       <spring:message code="return.to.preferences.link.text"/>
	    </a>
	</div>

</div>
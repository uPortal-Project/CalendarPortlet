<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<div class="upcal-edit-urlview">

	<h3>Edit Calendar</h3>

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editUrl"/></portlet:actionURL>
    <form:form name="calendar" commandName="userHttpIcalCalendarForm" action="${postUrl}">

	    <spring:hasBindErrors name="userHttpIcalCalendarForm">
	        <div class="portlet-msg-error" role="alert">
	            <form:errors path="*" element="div"/>
	        </div> <!-- end: portlet-msg -->
	    </spring:hasBindErrors>

        <form:hidden path="id"/>

        <p>
			<label class="portlet-form-field-label">Calendar name:</label>
			<form:input path="name" size="50"/>
        </p>
        <p>
            <label class="portlet-form-field-label">Calendar URL:</label>
            <form:input path="url" size="50"/>
    	</p>
        <p>
           <button type="submit" class="portlet-form-button">Save calendar</button>
        </p>
        
    </form:form>
    
	<div class="upcal-view-links">
        <portlet:renderURL var="returnUrl"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>
	    <a class="upcal-view-return" href="${ returnUrl }">
	       Return to preferences
	    </a>
	</div>

</div>
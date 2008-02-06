<html xmlns="http://www.w3c.org/1999/xhtml" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:portlet="http://java.sun.com/portlet"
    xmlns:html="/WEB-INF/tags/html" xmlns:form="http://www.springframework.org/tags/form"
    xmlns:validator="http://www.springmodules.org/tags/commons-validator"
    xml:lang="en" lang="en">
    <jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
    <head>
        <script type="text/javascript">
        </script>
    </head>
    <body>
		<validator:javascript formName="calendarListingCommand"
		    staticJavascript="false" xhtml="true" cdata="false"/>
        <portlet:actionURL var="postUrl"><portlet:param name="action" value="editUrl"/></portlet:actionURL>

		<h3>Edit Calendar</h3>

        <form:form name="calendar" commandName="calendarListingCommand" action="${postUrl}" onsubmit="return validateMyForm(this)">
        	<form:hidden path="id"/>
			<p>
				<label class="portlet-form-field-label">Calendar name:</label>
				<form:input path="name" size="50"/>
				<form:errors path="name" cssClass="portlet-msg-error"/>
			</p>
           	<p>
                <label class="portlet-form-field-label">Calendar URL:</label>
                <form:input path="url" size="50"/>
				<form:errors path="url" cssClass="portlet-msg-error"/>
			</p>
			<br/>
			<p>
				Note: Calendar URLs should start with http:// or https://, not webcal://.
			</p>
			<br/>
            <p>
	            <button type="submit" class="portlet-form-button">Save calendar</button>
            </p>
        </form:form>
        <br />
        <hr />
        <p>
        	<a href="<portlet:renderURL><portlet:param name="action" value="editPrefences"/></portlet:renderURL>"><img src="<c:url value="/images/arrow_left.png"/>" style="vertical-align: middle"> Return to main edit page</a>
        </p>
    </body>
</html>

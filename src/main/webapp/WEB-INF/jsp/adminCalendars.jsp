<html xmlns="http://www.w3c.org/1999/xhtml" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:portlet="http://java.sun.com/portlet"
    xmlns:html="/WEB-INF/tags/html" xmlns:form="http://www.springframework.org/tags/form"
    xml:lang="en" lang="en">
    <jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
    <head>
        <script type="text/javascript">
        </script>
        
        <style type="text/css">
        	table.edit-calendar { width: 100%; }
        	table.edit-calendar td { font-size: 1.1em; }
        	table.edit-calendar td.instruction { color: #666; font-size: 1em; text-align: center; vertical-align: bottom; }
        </style>
    </head>
    <body>
        <portlet:actionURL var="postUrl"></portlet:actionURL>

        <table class="edit-calendar">
        	<tr>
        		<td><h3>Pre-configured Calendars</h3></td>
        		<td class="instruction">Edit</td>
        		<td class="instruction">Delete</td>
        	</tr>
        	<c:forEach items="${ model.calendars }" var="calendar">
	        	<tr>
	        		<td>${ calendar.name }</td>
	        		<td class="instruction">
	        			<a href="<portlet:renderURL><portlet:param name="action" value="editCalendarDefinition"/>
	        					<portlet:param name="id" value="${ calendar.id }"/></portlet:renderURL>"
	        					title="Edit calendar">
        					<img alt="edit" src="<c:url value="/images/calendar_edit.png"/>"/>
       					</a>
	        		</td>
	        		<td class="instruction">
	        			<a href="<portlet:actionURL><portlet:param name="action" value="administration"/>
	        					<portlet:param name="actionCode" value="delete"/>
	        					<portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>"
	        					title="Delete calendar">
	        				<img alt="delete" src="<c:url value="/images/calendar_delete.png"/>"/>
	        			</a>
	        		</td>
	        	</tr>
        	</c:forEach>
        </table>
        <p>
        	<a href="<portlet:renderURL><portlet:param name="action" value="editCalendarDefinition"/></portlet:renderURL>">
        		<img src="<c:url value="/images/calendar_add.png"/>" style="vertical-align: middle"/> 
        		add a calendar
        	</a>
        </p>
        
        <br />
        <hr />
        <p>
        	<a href="<portlet:renderURL portletMode="view"/>"><img src="<c:url value="/images/arrow_left.png"/>" style="vertical-align: middle"> Return to calendar</a>
        </p>
        
    </body>
</html>

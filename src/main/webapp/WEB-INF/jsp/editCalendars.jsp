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
        		<td colspan="2"><h3>My Calendars</h3></td>
	        	<c:choose>
	        		<c:when test="${ not empty model.mycalendars }">
		        		<td class="instruction">Edit</td>
		        		<td class="instruction">Delete</td>
		        		<td class="instruction">Displayed</td>
		        	</c:when>
		        	<c:otherwise>
		        		<td colspan="3">&nbsp;</td>
		        	</c:otherwise>
	        	</c:choose>
        	</tr>
        	<c:forEach items="${ model.mycalendars }" var="calendar">
	        	<tr>
	        		<td style="width: 7px;">&nbsp;</td>
	        		<td>${ calendar.calendarDefinition.name }</td>
	        		<td class="instruction">
	        			<a href="<portlet:renderURL><portlet:param name="action" value="editUrl"/>
	        					<portlet:param name="id" value="${ calendar.id }"/></portlet:renderURL>"
	        					title="Edit calendar">
	        				<img alt="edit" src="<c:url value="/images/calendar_edit.png"/>"/>
	        			</a>
	        		</td>
	        		<td class="instruction">
	        			<a href="<portlet:actionURL><portlet:param name="actionCode" value="delete"/>
	        					<portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>"
	        					title="Delete calendar">
	        				<img alt="delete" src="<c:url value="/images/calendar_delete.png"/>"/>
	        			</a>
	        		</td>
	        		<td class="instruction">
	        			<c:choose>
	        				<c:when test="${ calendar.displayed }">
	        					<portlet:actionURL var="displayURL"><portlet:param name="actionCode" value="hide"/>
	        						<portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>
			        			<a href="${ displayURL }" title="Hide calendar">
			        				<img alt="show" src="<c:url value="/images/select-active.gif"/>"/>
			        			</a>
	        				</c:when>
	        				<c:otherwise>
								 <portlet:actionURL var="displayURL"><portlet:param name="actionCode" value="show"/><portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>
			        			<a href="${ displayURL }" title="Show calendar">
			        				<img alt="show" src="<c:url value="/images/select-inactive.gif"/>"/>
			        			</a>
							</c:otherwise>
	        			</c:choose>
	        		</td>
	        	</tr>
        	</c:forEach>
        	<tr>
        		<td width="7px;">&nbsp;</td>
        		<td colspan="4" style="padding-top: 10px; padding-bottom: 15px; padding-left:5px;">
			        	<a href="<portlet:renderURL><portlet:param name="action" value="editUrl"/></portlet:renderURL>"><img src="<c:url value="/images/calendar_add.png"/>" style="vertical-align: middle"/> add a calendar</a>
			        	<br/>
        		</td>
        	</tr>
        	<tr>
        		<td colspan="2"><h3>YaleInfo Calendars</h3></td>
        		<td class="instruction">Edit</td>
        		<td class="instruction">&nbsp;</td>
        		<td class="instruction">Displayed</td>
        	</tr>
        	<c:forEach items="${ model.calendars }" var="calendar">
	        	<tr>
	        		<td>&nbsp;</td>
	        		<td>${ calendar.calendarDefinition.name }</td>
	        		<td class="instruction">
	        			<c:set var="editAction" value="${ model.predefinedEditActions[calendar.calendarDefinition.className] }"/>
						<c:choose>
							<c:when test="${ not empty editAction }">
			        			<a href="<portlet:renderURL><portlet:param name="action" value="${ editAction }"/>
			        					<portlet:param name="id" value="${ calendar.id }"/></portlet:renderURL>"
			        					title="Edit calendar">
		        					<img alt="edit" src="<c:url value="/images/calendar_edit.png"/>"/>
	        					</a>
							</c:when>
							<c:otherwise>&nbsp;</c:otherwise>
						</c:choose>
	        		</td>
	        		<td>&nbsp;</td>
	        		<td class="instruction">
	        			<c:choose>
	        				<c:when test="${ calendar.displayed }">
	        					<portlet:actionURL var="displayURL"><portlet:param name="actionCode" value="hide"/>
	        						<portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>
			        			<a href="${ displayURL }" title="Hide calendar">
			        				<img alt="show" src="<c:url value="/images/select-active.gif"/>"/>
			        			</a>
	        				</c:when>
	        				<c:otherwise>
								 <portlet:actionURL var="displayURL"><portlet:param name="actionCode" value="show"/>
								 	<portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>
			        			<a href="${ displayURL }" title="Show calendar">
			        				<img alt="show" src="<c:url value="/images/select-inactive.gif"/>"/>
			        			</a>
							</c:otherwise>
	        			</c:choose>
	        		</td>
	        	</tr>
        	</c:forEach>
        	<c:forEach items="${ model.hiddencalendars }" var="calendar">
        		<tr>
        			<td>&nbsp;</td>
	        		<td>${ calendar.name }</td>
	        		<td>&nbsp;</td>
	        		<td>&nbsp;</td>
	        		<td class="instruction">
       					<portlet:actionURL var="displayURL"><portlet:param name="actionCode" value="showNew"/><portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>
	        			<a href="${ displayURL }" title="Show calendar">
	        				<img alt="show" src="<c:url value="/images/select-inactive.gif"/>"/>
	        			</a>
	        		</td>
        		</tr>
        	</c:forEach>
        </table>
        
        <br/>
        <p><a href="http://www.yale.edu/yaleinfohelp/my-calendar.html" target="_blank">Need help?</a></p>
        
        <br />
        <hr />
        <p>
        	<a href="<portlet:renderURL portletMode="view"/>"><img src="<c:url value="/images/arrow_left.png"/>" style="vertical-align: middle"> Return to calendar</a>
        </p>
        
    </body>
</html>

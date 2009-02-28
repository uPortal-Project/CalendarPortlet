    <jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>

     <style type="text/css">
     	table.edit-calendar { width: 100%; }
     	table.edit-calendar td { font-size: 1.1em; }
     	table.edit-calendar td.instruction { color: #666; font-size: 1em; text-align: center; vertical-align: bottom; }
     </style>

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
     					<img alt="edit" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/calendar_edit.png"/>"/>
    					</a>
      		</td>
      		<td class="instruction">
      			<a href="<portlet:actionURL><portlet:param name="action" value="administration"/>
      					<portlet:param name="actionCode" value="delete"/>
      					<portlet:param name="id" value="${ calendar.id }"/></portlet:actionURL>"
      					title="Delete calendar">
      				<img alt="delete" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/calendar_delete.png"/>"/>
      			</a>
      		</td>
      	</tr>
     	</c:forEach>
     </table>
     <p>
     	<a href="<portlet:renderURL><portlet:param name="action" value="editCalendarDefinition"/></portlet:renderURL>">
     		<img src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/calendar_add.png"/>" style="vertical-align: middle"/> 
     		add a calendar
     	</a>
     </p>
     
     <br />
     <hr />
     <p>
     	<a href="<portlet:renderURL portletMode="view"/>"><img src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/arrow_left.png"/>" style="vertical-align: middle"> Return to calendar</a>
     </p>

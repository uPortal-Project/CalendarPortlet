    <jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
    <fmt:setTimeZone value="${ model.timezone }"/>
    <fmt:formatDate var="today" value="${model.today}" pattern="EEEE MMMM d"/>
    <fmt:formatDate var="tomorrow" value="${model.tomorrow}" pattern="EEEE MMMM d"/>
    
    <link rel="stylesheet" href="<c:url value="/css/calendar.css"/>" type="text/css"></link>
    <link rel="stylesheet" href="<c:url value="/css/datePicker.css"/>" type="text/css" media="screen"/>
    
    <script type="text/javascript" src="<c:url value="/scripts/ui.datepicker.js"/>"></script>
    <c:if test="${model.includeJQuery}">
        <script type="text/javascript" src="<c:url value="/scripts/jquery-1.2.3.min.js"/>"></script>
    </c:if>
    <script type="text/javascript">
	     $(document).ready(function(){
		    $('#<portlet:namespace/>inlineCalendar').datepicker(
		    	{ 
		    	    inline: true,
		    		changeMonth: false,
		    		changeYear: false,
				    onSelect: function(date) {
				        $("#<portlet:namespace/>events").html("<br/><p>Loading . . . </p>");
				        $.get(
				            '<c:url value="/listEvents"/>', 
				            { startDate: date }, 
				            function(xml){ $("#<portlet:namespace/>events").html(xml) }
				        );
				    } 
				}
			);
		}); 
    </script>

    <table width="100%">
        <tr>
        <td>
            <div id="<portlet:namespace/>inlineCalendar" class="jqueryui"></div>
        </td>
        <td style="padding: 5px;">
        
		<div style="padding-bottom: 3px;">
			Show calendar:
			<div style="padding-left: 7px; padding-right: 7px;">
				<c:forEach items="${ model.calendars }" var="calendar">
					<span class="color-${ model.colors[calendar.id] }">
						<c:choose>
							<c:when test="${ empty model.hiddenCalendars[calendar.id] }">
								<portlet:renderURL var="url"><portlet:param name="hideCalendar" value="${ calendar.id }"/></portlet:renderURL>
								<a href="${ url }">
									<img src="<c:url value="/images/select-active.gif"/>" style="vertical-align: middle; margin-left: 7px;"/>
								</a> 
							</c:when>
							<c:otherwise>
								<portlet:renderURL var="url"><portlet:param name="showCalendar" value="${ calendar.id }"/></portlet:renderURL>
								<a href="${ url }">
									<img src="<c:url value="/images/select-inactive.gif"/>" style="vertical-align: middle; margin-left: 7px;"/>
								</a>
							</c:otherwise>
						</c:choose>
						${ calendar.calendarDefinition.name }
	 				</span>
				</c:forEach>
			</div>
        </div>
         <div style="padding-bottom: 3px;">
			Go to calendar:
			<div style="padding-left: 7px; padding-right: 7px;">
				<c:forEach items="${ model.calendars }" var="calendar">
					<c:if test="${ empty model.hiddenCalendars[calendar.id] && not empty model.links[calendar.id]}">
						<a href="${ model.links[calendar.id] }" rel="popup" class="color-${ model.colors[calendar.id] }">
							${ calendar.calendarDefinition.name }	
						</a> |
					</c:if>
				</c:forEach>
			</div>
        </div>
		<div style="padding-bottom: 3px;">
        	Range:
			<div style="padding-left: 7px; padding-right: 7px;">
				<c:choose>
					<c:when test="${ model.days == 1 }">today</c:when>
					<c:otherwise>
			        	<a href="<portlet:renderURL><portlet:param name="timePeriod" value="1"/></portlet:renderURL>">today</a>
					</c:otherwise>
				</c:choose> |
				<c:choose>
					<c:when test="${ model.days == 2 }">2 days</c:when>
					<c:otherwise>
	        	<a href="<portlet:renderURL><portlet:param name="timePeriod" value="2"/></portlet:renderURL>">2 days</a>
					</c:otherwise>
				</c:choose> |
				<c:choose>
					<c:when test="${ model.days == 7 }">1 week</c:when>
					<c:otherwise>
	        	<a href="<portlet:renderURL><portlet:param name="timePeriod" value="7"/></portlet:renderURL>">1 week</a>
					</c:otherwise>
				</c:choose> |
				<c:choose>
					<c:when test="${ model.days == 14 }">2 weeks</c:when>
					<c:otherwise>
	        	<a href="<portlet:renderURL><portlet:param name="timePeriod" value="14"/></portlet:renderURL>">2 weeks</a>
					</c:otherwise>
				</c:choose>
			</div>
        </div> 
        </td>
        </tr>
    </table>
    
    <div id="<portlet:namespace/>events">
        
	<c:if test="${ not empty model.errors }">
		<p class="portlet-msg-error">
			<c:forEach items="${ model.errors }" var="error">${ error }<br/></c:forEach>
		</p>
	</c:if>
	
	<c:if test="${ empty model.events }">
    	<p>No events.</p>
	</c:if>
	
	<div id="calEvents" style="padding-bottom: 10px;">
	    <table id="calendarEventsListTable" class="yaleEvents"
	        style="align-vertical: top; width: 100%">
	        <c:forEach items="${model.events}" var="event" varStatus="status">
	           <fmt:formatDate var="startDate" value="${event.startDate.date}"
	               pattern="EEEE MMMM d"/>
	           <fmt:formatDate var="endDate" value="${event.endDate.date}"
	               pattern="EEEE MMMM d"/>
	           <c:if test="${startDate != lastDate}">
	               <c:set var="lastDate" value="${startDate}"/>
	               <tr>
	                   <td colspan="3">
	                       <h2 style="border-bottom: thin solid #999">
	                           <c:choose>
	                               <c:when test="${startDate == today}"> <span style="font-weight: bold">Today</span> (${ startDate }) </c:when>
	                               <c:when test="${startDate == tomorrow}"> Tomorrow </c:when>
	                               <c:otherwise> ${startDate} </c:otherwise>
	                           </c:choose>
	                       </h2>
	                   </td>
	               </tr>
	           </c:if>
	           <tr>
	               <fmt:formatDate var="startTime" value="${event.startDate.date}" pattern="h:mm a"/>
	               <fmt:formatDate var="endTime" value="${event.endDate.date}" pattern="h:mm a"/>
	               <c:set var="class" value="${ model.colors[event.calendarId] }"/>
	               <c:choose>
	                   <c:when test="${event.allDay}">
	                       <td class="time color-${ class }">All day</td>
	                   </c:when>
	                   <c:otherwise>
	                       <td class="time color-${ class }">${startTime} <c:if test="${ not empty event.endDate and event.startDate.date != event.endDate.date}"> - ${ endTime } </c:if></td>
	                   </c:otherwise>
	               </c:choose>
	               <td class="color-${ class }">
	               	<c:choose>
	               		<c:when test="${ not empty event.url }">
	               			<a href="${ event.url.value }" class="color-${ class }" target="_blank">${ event.summary.value }</a>
	               		</c:when>
	               		<c:otherwise>${ event.summary.value }</c:otherwise>
	               	</c:choose>
	              	</td>
	            <td class="color-${ class }">
	            	${ event.location.value }
	            </td>
	           </tr>
	       </c:forEach>
	    </table>
	</div>
	
	</div>
	
    <c:if test="${ !model.guest }">
	    <br />
	    <hr />
	    <p>
	        <a href="<portlet:renderURL portletMode="edit"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>">
	            <img src="<c:url value="/images/calendar_edit.png"/>" style="vertical-align: middle">
	            Edit and download calendars</a>
	     
		    <c:if test="${ sessionScope.isAdmin }">
		         | <a href="<portlet:renderURL portletMode="edit">
		             <portlet:param name="action" value="administration"/></portlet:renderURL>">
		             Calendar Administration
		         </a>
		    </c:if>
	    </p>
    </c:if>
    
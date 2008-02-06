<html xmlns="http://www.w3c.org/1999/xhtml" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
    xmlns:portlet="http://java.sun.com/portlet" xmlns:html="/WEB-INF/tags/html" xml:lang="en"
    lang="en">
    <jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
    <fmt:formatDate var="today" value="${model.today}" pattern="EEEE MMMM d"/>
    <fmt:formatDate var="tomorrow" value="${model.tomorrow}" pattern="EEEE MMMM d"/>
    
    <head>
        <title>View Calendar</title>
        <style type="text/css">
            table#calendar { width: 100%; text-align: center; }
            table.yaleEvents tr td { vertical-align: top; padding: 5px; }
            table.yaleEvents tr td.time {  }
            .othermonth { color: #999; padding: 6px; }
            .thismonth { padding: 6px; background-color: #eef2ff; }
            .calendarTitle { text-align: center; padding: 6px; font-size: 1.2em; }
            .today { background-color: #bcc6e4; font-weight: bold; }
            .selectedDate { background-color: #c4c6ce; }
			.color-0, a.color-0 { color: #1062a5; }
			.color-1, a.color-1 { color: #477300; }
			.color-2, a.color-2 { color: #990000; }
			.color-3, a.color-3 { color: #683099; }
			.color-4, a.color-4 { color: #8b4500; }
			.color-5, a.color-5 { color: #008b78; }
			.color-6, a.color-6 { color: #b62162; }
			.color-7, a.color-7 { color: #bb5b1d; }
			.color-8, a.color-8 { color: #7b7b7b; }
			.color-9, a.color-9 { color: #000000; }
        </style>
    </head>

    <body>
		<div style="padding-bottom: 3px;">
			Calendars:
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
        
        <c:if test="${ not empty model.errors }">
	        <p class="portlet-msg-error">
	        	<c:forEach items="${ model.errors }" var="error">
		        	${ error }<br/>
	        	</c:forEach>
	        </p>
        </c:if>
        
        <c:if test="${ empty model.events }">
        	<p>No events.</p>
        </c:if>
        
        <div id="calEvents" style="padding-top:10px; padding-bottom: 10px;">
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
                                <td class="time color-${ class }">${startTime} <c:if test="${ event.startDate.date != event.endDate.date}"> - ${ endTime } </c:if></td>
                            </c:otherwise>
                        </c:choose>
                        <td class="color-${ class }">
                        	<c:choose>
                        		<c:when test="${ not empty event.url }">
                        			<a href="http://${ event.url.value }" class="color-${ class }" target="_blank">${ event.summary.value }</a>
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
        
    </body>
    
</html>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<fmt:setTimeZone value="${ timezone }"/>
<c:set var="timezone" value="${timezone}" scope="page"/>
<fmt:formatDate var="today" value="${today}" pattern="EEEE MMMM d"/>
<fmt:formatDate var="tomorrow" value="${tomorrow}" pattern="EEEE MMMM d"/>
    
<c:if test="${ not empty errors }">
    <p class="portlet-msg-error">
        <c:forEach items="${ errors }" var="error">${ error }<br/></c:forEach>
    </p>
</c:if>

<c:if test="${ empty events }">
    <p>No events.</p>
</c:if>

<div class="upcal-events">
    <c:forEach items="${events}" var="event" varStatus="status">
        <fmt:formatDate var="startDate" value="${event.startDate.date}"
           pattern="EEEE MMMM d"/>
        <fmt:formatDate var="endDate" value="${event.endDate.date}"
           pattern="EEEE MMMM d"/>
        <c:if test="${startDate != lastDate}">
            <c:set var="lastDate" value="${startDate}"/>
            <h2>
                <c:choose>
                    <c:when test="${startDate == today}">
                        <span class="upcal-today-date">Today</span> (${ startDate }) 
                    </c:when>
                    <c:when test="${startDate == tomorrow}"> Tomorrow </c:when>
                    <c:otherwise> ${startDate} </c:otherwise>
                </c:choose>
            </h2>
        </c:if>
       
        <fmt:formatDate var="startTime" value="${event.startDate.date}" pattern="h:mm a"/>
        <fmt:formatDate var="endTime" value="${event.endDate.date}" pattern="h:mm a"/>
        <c:set var="class" value="upcal-color-${ colors[event.calendarId] }"/>
        <div class="upcal-event ${ class }">
	        <div class="upcal-event-cal">
	            <span></span>
			</div>
			<span class="upcal-event-time">
                <c:choose>
                    <c:when test="<%= ((org.jasig.portlet.calendar.CalendarEvent) pageContext.getAttribute(\"event\")).isAllDay((java.lang.String) pageContext.getAttribute(\"timezone\")) %>">
                        All day
                    </c:when>
                    <c:otherwise>
                        ${startTime} <c:if test="${ not empty event.endDate and event.startDate.date != event.endDate.date}"> - ${ endTime } </c:if>
                    </c:otherwise>
                </c:choose>
			</span>
			<h3 class="upcal-event-title">
                <a class="upcal-event-link" href="javascript:;" eventIndex="${ status.index }">
                    <c:out value="${ event.summary.value }"/>
                </a>
            </h3>
       </div>
       
    </c:forEach>
    
</div>

    <!-- Event Details -->
    <div class="upcal-event-details">
      <c:forEach items="${events}" var="event" varStatus="status">
        <c:set var="class" value="upcal-color-${ colors[event.calendarId] }"/>
        
        <div id="eventDescription-${status.index}" class="upcal-event-detail color-${ class }" style="display:none">
          
          <!-- Event title -->
          <h2>${ event.summary.value }</h2>
          
          <!-- Calendar event is from -->
          <div class="upcal-event-detail-cal">
            <span> <!-- Calendar name to go here. --> </span>
          </div>
          
          <!-- Event time -->
          <fmt:formatDate var="startDate" value="${event.startDate.date}" pattern="EEEE MMMM d"/>
          <div class="event-detail-date">
            <h3>Date:</h3>
            <p>
              <c:choose>
                <c:when test="${startDate == today}"> Today <span class="upcal-event-detail-startdate"> ${ startDate } </span></c:when>
                <c:when test="${startDate == tomorrow}"> Tomorrow (${ startDate })</c:when>
                <c:otherwise> ${startDate} </c:otherwise>
              </c:choose>
              <c:if test="<%= !((org.jasig.portlet.calendar.CalendarEvent) pageContext.getAttribute(\"event\")).isAllDay((java.lang.String) pageContext.getAttribute(\"timezone\")) %>">
                <span class="upcal-event-detail-starttime">
                  <fmt:formatDate value="${event.startDate.date}" pattern="h:mm a"/>
                  to <fmt:formatDate value="${event.startDate.date}" pattern="h:mm a"/>
                </span>
              </c:if>
            </p>
          </div>
          
          <!-- Event location -->
          <c:if test="${ not empty event.location }">
            <div class="upcal-event-detail-loc">
              <h3>Location:</h3>
              <p><spring:escapeBody htmlEscape="true">${ event.location.value }</spring:escapeBody></p>
            </div>
          </c:if>
          
          <!-- Event description -->
          <c:if test="${ not empty event.description }">
            <div class="upcal-event-detail-desc">
              <h3>Description:</h3>
              <p>${ event.description.value }</p>
            </div>
          </c:if>
          
          <!-- Event link (to authoring application) -->
          <c:if test="${ not empty event.url }">
            <div class="upcal-event-detail-link">
              <h3>Link:</h3>
              <p><a href="${ event.url.value }">${ event.url.value }</a></p>
            </div>
          </c:if>
          
        </div> <!-- end: cal-event-detail -->
      </c:forEach>
    </div> <!-- end: cal-event-details -->


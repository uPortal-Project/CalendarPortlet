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
    
    <div id="calEvents" style="padding-bottom: 10px;">
        <table id="calendarEventsListTable" class="yaleEvents"
            style="align-vertical: top; width: 100%">
            <c:forEach items="${events}" var="event" varStatus="status">
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
                   <c:set var="class" value="${ colors[event.calendarId] }"/>
                   <c:choose>
                       <c:when test="<%= ((org.jasig.portlet.calendar.CalendarEvent) pageContext.getAttribute(\"event\")).isAllDay((java.lang.String) pageContext.getAttribute(\"timezone\")) %>">
                           <td class="time color-${ class }">All day</td>
                       </c:when>
                       <c:otherwise>
                           <td class="time color-${ class }">${startTime} <c:if test="${ not empty event.endDate and event.startDate.date != event.endDate.date}"> - ${ endTime } </c:if></td>
                       </c:otherwise>
                   </c:choose>
                   <td class="color-${ class }">
                    <c:choose>
                        <c:when test="${ not empty event.url }">
                            <a href="${ event.url.value }" class="color-${ class }" target="_blank"><c:out value="${ event.summary.value }"/></a>
                        </c:when>
                        <c:otherwise><c:out value="${ event.summary.value }"/></c:otherwise>
                    </c:choose>
                    </td>
                <td class="color-${ class }">
                    <c:out value="${ event.location.value }"/>
                </td>
               </tr>
           </c:forEach>
        </table>
    </div>

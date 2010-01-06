<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<div class="upcal-editview">

<div class="upcal-edit-links">
    <a href="<portlet:renderURL><portlet:param name="action" value="editPreferences"/></portlet:renderURL>">Edit calendar preferences</a>
    <c:if test="${ sessionScope.isAdmin }">
        <span class="upcal-pipe">|</span>
        <a href="<portlet:renderURL><portlet:param name="action" value="administration"/></portlet:renderURL>">Calendar Administration</a>
    </c:if>
</div>

<h2>Edit Calendars</h2>

<div class="fl-col-flex2">

    <div class="fl-col upcal-mycalendars upcal-list">
		<h3>My Calendars</h3>
		<ul>
            <c:forEach items="${ model.mycalendars }" var="calendar">
                <li>
                    <portlet:renderURL var="editCalendarUrl">
                        <portlet:param name="action" value="editUrl"/>
                        <portlet:param name="id" value="${ calendar.id }"/>
                    </portlet:renderURL>
		            <a href="${ editCalendarUrl }" class="upcal-edit" title="Edit calendar">
		              <span>Edit</span>
		            </a>
                    <span>${ calendar.calendarDefinition.name }</span>
                    <portlet:actionURL var="deleteCalendarUrl">
                        <portlet:param name="action" value="deleteUserCalendar"/>
                        <portlet:param name="configurationId" value="${ calendar.id }"/>
                    </portlet:actionURL>
		            <a href="${ deleteCalendarUrl }" class="upcal-delete" title="Delete calendar">
		                <span>Delete</span>
		            </a>
                </li>
            </c:forEach>
		</ul>
		<portlet:renderURL var="addCalendar"><portlet:param name="action" value="editUrl"/></portlet:renderURL>
		<a class="upcal-add" href="${ addCalendar }">
            Add a calendar
		</a>
	</div>
	
	<div class="fl-col upcal-builtin upcal-list">
		<h3>Built-in Calendars</h3>
		<ul>
		    <c:forEach items="${ model.calendars }" var="calendar">
                <li>
		            <c:choose>
		                <c:when test="${ calendar.displayed }">
		                    <portlet:actionURL var="displayURL">
                                <portlet:param name="action" value="hideCalendar"/>
		                        <portlet:param name="configurationId" value="${ calendar.id }"/>
		                    </portlet:actionURL>
				            <a class="upcal-active" href="${ displayURL }" title="Hide calendar">
				                <span>Active</span>
				            </a>
		                </c:when>
		                <c:otherwise>
                            <portlet:actionURL var="displayURL">
                                <portlet:param name="action" value="showCalendar"/>
                                <portlet:param name="configurationId" value="${ calendar.id }"/>
                            </portlet:actionURL>
				            <a class="upcal-inactive" href="${ displayURL }" title="Show calendar">
				                <span>Inactive</span>
				            </a>
                		</c:otherwise>
		            </c:choose>
		            <c:set var="editAction" value="${ model.predefinedEditActions[calendar.calendarDefinition.className] }"/>
		            <c:choose>
		                <c:when test="${ not empty editAction }">
	    	                <portlet:renderURL var="editCalendarUrl">
	                            <portlet:param name="action" value="${ editAction }"/>
	                            <portlet:param name="configurationId" value="${ calendar.id }"/>
	                        </portlet:renderURL>
	                        <a class="upcal-edit" href="${ editCalendarUrl }" title="Edit calendar">
	                           <span>Edit</span>
	                        </a>
		                </c:when>
		                <c:otherwise>&nbsp;</c:otherwise>
		            </c:choose>                    
                    <span class="cal-name">${ calendar.calendarDefinition.name }</span>
                </li>
		    </c:forEach>
		    <c:forEach items="${ model.hiddencalendars }" var="calendar">
                <li>
                    <portlet:actionURL var="displayURL">
                        <portlet:param name="action" value="addSharedCalendar"/>
                        <portlet:param name="definitionId" value="${ calendar.id }"/>
                    </portlet:actionURL>
                    <a class="upcal-inactive" href="${ displayURL }" title="Show calendar">
                        <span>Show</span>
                    </a>
                    ${ calendar.name }
                </li>
		    </c:forEach>
		</ul>
	</div>
	
</div>

<portlet:actionURL var="postUrl"><portlet:param name="action" value="editPreferences"/></portlet:actionURL>

<h2>Edit Calendar Preferences</h2>

<form:form name="calendar" commandName="calendarPreferencesCommand" action="${postUrl}">

    <p>
        <label class="portlet-form-field-label">Time Zone:</label>
        <form:select path="timezone">
            <c:forEach items="${timezones}" var="zone">
                <spring:message var="message" code="timezone.${ zone }" text="${ zone }"/>
                <form:option label="${message}" value="${ zone }"/>
            </c:forEach>
        </form:select>
        <form:errors path="timezone" cssClass="portlet-msg-error"/>
    </p>
    <br/>
    <p>
       <button type="submit" class="portlet-form-button">Save preferences</button>
    </p>
    
</form:form>

<div class="upcal-view-links">
	<a class="upcal-view-return" href="<portlet:renderURL portletMode="view"/>">
	   Return to calendar
	</a>
</div>

</div>
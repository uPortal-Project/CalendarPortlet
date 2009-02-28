    <jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editPreferences"/></portlet:actionURL>

    <h3>Edit Calendar Preferences</h3>

    <form:form name="calendar" commandName="calendarPreferencesCommand" action="${postUrl}">

        <p>
            <label class="portlet-form-field-label">Time Zone:</label>
            <form:select path="timezone">
                <c:forEach items="${timezones}" var="zone">
                    <form:option value="${ zone }"/>
                </c:forEach>
            </form:select>
            <form:errors path="timezone" cssClass="portlet-msg-error"/>
        </p>
        <br/>
        <p>
           <button type="submit" class="portlet-form-button">Save preferences</button>
        </p>
        
    </form:form>
    
    <br />
    <hr />
    <p>
        <a href="<portlet:renderURL><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>">
            <img src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/arrow_left.png"/>" style="vertical-align: middle"> Return to main edit page</a>
    </p>

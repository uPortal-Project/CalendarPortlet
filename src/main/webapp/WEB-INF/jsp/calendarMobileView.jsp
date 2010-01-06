<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>
<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<fmt:setTimeZone value="${ model.timezone }"/>
<c:set var="n"><portlet:namespace/></c:set>

<c:if test="${includeJQuery}">
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2.min.js"/>"></script>
</c:if>
    <script type="text/javascript">
    	var cal = cal || {};
    	cal.jQuery = jQuery.noConflict(${includeJQuery});
    	cal.jQuery(function(){
    		var $ = cal.jQuery;
			$(document).ready(function(){
				$("#<portlet:namespace/>events").html("<br/><p>Loading . . . </p>");
				$.post('<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>',
						{ startDate: '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy"/>' },
						function(xml){ $("#<portlet:namespace/>events").html(xml) }
				);
				var date = new Date();
                date.setFullYear(<fmt:formatDate value="${model.startDate}" pattern="yyyy"/>, Number(<fmt:formatDate value="${model.startDate}" pattern="M"/>)-1, <fmt:formatDate value="${model.startDate}" pattern="d"/>);
			    $('#<portlet:namespace/>inlineCalendar').datepicker(
			    	{ 
			    	    inline: true,
			    		changeMonth: false,
			    		changeYear: false,
			    		defaultDate: date,
					    onSelect: function(date) {
					        $("#<portlet:namespace/>events").html("<br/><p>Loading . . . </p>");
					        $.post(
					        	'<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>',
					            //'<c:url value="/listEvents"/>', 
					            { startDate: date }, 
					            function(xml){ $("#<portlet:namespace/>events").html(xml) }
					        );
					    } 
					}
				);
			});
		});
    </script>

<div class="upcal-fullview">

<c:if test="${ !model.guest }">
	<div class="upcal-edit-links">
	    <a href="<portlet:renderURL portletMode="edit"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>">Edit and Download Calendars</a>
        <c:if test="${ sessionScope.isAdmin }">
		    <span class="upcal-pipe">|</span>
		    <a href="<portlet:renderURL><portlet:param name="action" value="administration"/></portlet:renderURL>">Calendar Administration</a>
	    </c:if>
	</div>
</c:if>

<div id="calendarPortletHeader" class="fl-col-mixed3">

	<div class="fl-col-side fl-force-right">
		<div class="upcal-showcals upcal-list">
    		<h3>Show calendar:</h3>
    		<ul>
                <c:forEach items="${ model.calendars }" var="calendar">
                    <li class="color-${ model.colors[calendar.id] }">
                        <c:choose>
                            <c:when test="${ empty model.hiddenCalendars[calendar.id] }">
                                <portlet:renderURL var="url"><portlet:param name="hideCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                <a class="upcal-active" href="${ url }">
                                    <span>Active</span>
                                </a> 
                            </c:when>
                            <c:otherwise>
                                <portlet:renderURL var="url"><portlet:param name="showCalendar" value="${ calendar.id }"/></portlet:renderURL>
                                <a class="upcal-inactive" href="${ url }">
                                    <span>Inactive</span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                        <span>${ calendar.calendarDefinition.name }</span>
                    </li>
                </c:forEach>
   		</ul>
		</div>
	</div>
	
	<div class="fl-col-side fl-force-left">
        <div id="<portlet:namespace/>inlineCalendar" class="jqueryui"></div>
        <div style="clear: both;"></div>
	</div>
	
	<div class="fl-col-main">
	
		<div id="${n}calendarRangeSelector" class="upcal-range">
            <h3>View:</h3>
            <span class="upcal-range-day">
                <c:choose>
                    <c:when test="${ model.days == 1 }">
                        Day
                </c:when>
                <c:otherwise>
                    <a href="<portlet:renderURL><portlet:param name="timePeriod" value="1"/></portlet:renderURL>">Day</a>
                </c:otherwise>
             </c:choose>
            </span>
            <span class="upcal-pipe">|</span>
            <span class="upcal-range-day">
                <c:choose>
                    <c:when test="${ model.days == 7 }">
                        Week
                    </c:when>
                    <c:otherwise>
                        <a href="<portlet:renderURL><portlet:param name="timePeriod" value="7"/></portlet:renderURL>">Week</a>
                    </c:otherwise>
                </c:choose>
            </span>
            <span class="upcal-pipe">|</span>
            <span class="upcal-range-day">
                <c:choose>
                    <c:when test="${ model.days == 31 }">
                        Month
                    </c:when>
                    <c:otherwise>
                        <a href="<portlet:renderURL><portlet:param name="timePeriod" value="31"/></portlet:renderURL>">Month</a>
                    </c:otherwise>
                </c:choose>
            </span>
		</div>
		
		<div id="${n}events">
		</div>
		
	</div>
</div>

</div>

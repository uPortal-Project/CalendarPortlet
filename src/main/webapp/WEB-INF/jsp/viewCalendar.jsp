<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<fmt:setTimeZone value="${ model.timezone }"/>
<fmt:formatDate var="today" value="${model.today}" pattern="EEEE MMMM d"/>
<fmt:formatDate var="tomorrow" value="${model.tomorrow}" pattern="EEEE MMMM d"/>
    
    <link rel="stylesheet" href="<c:url value="/css/calendar.css"/>" type="text/css"></link>
    
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

    <table width="100%">
        <tr>
        <td>
            <div id="<portlet:namespace/>inlineCalendar" class="jqueryui"></div>
        </td>
        <td style="padding: 5px; vertical-align: top">
        
		<div style="padding-bottom: 10px;">
			<span>Show calendar:</span>
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
        	<span>Range:</span>
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
       
	</div>
	
    <c:if test="${ !model.guest }">
	    <br />
	    <hr />
	    <p>
	        <a href="<portlet:renderURL portletMode="edit"><portlet:param name="action" value="editSubscriptions"/></portlet:renderURL>">
	            <img src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/calendar_edit.png"/>" style="vertical-align: middle">
	            Edit and download calendars</a>
	     
		    <c:if test="${ sessionScope.isAdmin }">
		         | <a href="<portlet:renderURL portletMode="edit">
		             <portlet:param name="action" value="administration"/></portlet:renderURL>">
		             Calendar Administration
		         </a>
		    </c:if>
	    </p>
    </c:if>
    

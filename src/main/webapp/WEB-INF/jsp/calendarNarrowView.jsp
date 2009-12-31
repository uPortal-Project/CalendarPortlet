<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<fmt:setTimeZone value="${ model.timezone }"/>
<c:set var="n"><portlet:namespace/></c:set>

<link rel="stylesheet" href="<c:url value="/css/calendar.css"/>" type="text/css"></link>
<style type="text/css">
    <jsp:directive.include file="/WEB-INF/jsp/dynamicCss.jsp"/>
</style>

<c:if test="${includeJQuery}">
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>"></script>
    <script type="text/javascript" src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2.min.js"/>"></script>
</c:if>
<script type="text/javascript">
   	var cal = cal || {};
   	cal.jQuery = jQuery.noConflict(${includeJQuery});
   	cal.jQuery(function(){
   	    var $ = cal.jQuery;
   	    var eventsUrl = '<portlet:actionURL><portlet:param name="action" value="events"/></portlet:actionURL>';

   	    var updateEvents = function(date) {
            $("#${n}events").html("<br/><p>Loading . . . </p>");
            $.post(eventsUrl,
                { startDate: date }, 
                function(xml) {
                     $("#${n}events").html(xml);
                     updateLinks(); 
                }
            );
   	    };

        var updateLinks = function() {
            $("#${n}events").find(".upcal-event-link").click(function(){
                    var link = $(this);
                    $('#${n}calendarRangeSelector').hide();
                    $('#${n}inlineCalendar').hide();
                    $('#${n}events .upcal-events').hide();
                    $('#${n}viewMoreEventsLink').hide();
                    $('#eventDescription-' + link.attr("eventIndex")).show();
                    $('#${n}returnToCalendarLink').show();
                });
            $('#${n}returnToCalendarLink').click(function(){
                    $('[id^=eventDescription]').hide();
                    $('#${n}returnToCalendarLink').hide();
                    $('#${n}calendarRangeSelector').show();
                    $('#${n}inlineCalendar').show();
                    $('#${n}events .upcal-events').show();
                    $('#${n}viewMoreEventsLink').show();
                });
        };
   
   	    
   	    $(document).ready(function(){
   	   	    var startDate = '<fmt:formatDate value="${model.startDate}" type="date" pattern="MM/dd/yyyy"/>';
   	   	    updateEvents(startDate);
			var date = new Date();
            date.setFullYear(<fmt:formatDate value="${model.startDate}" pattern="yyyy"/>, Number(<fmt:formatDate value="${model.startDate}" pattern="M"/>)-1, <fmt:formatDate value="${model.startDate}" pattern="d"/>);
		    $('#${n}inlineCalendar').datepicker(
		    	{
		    	    inline: true,
		    		changeMonth: false,
		    		changeYear: false,
		    		defaultDate: date,
				    onSelect: function(date) {
		    	        updateEvents(date);
				    } 
				}
			);
		});
	});
</script>

<div class="upcal-miniview">

    <!-- Range Selector -->
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
	
	<!-- Mini-Calendar (jQuery) -->
    <div id="${n}inlineCalendar" class="jqueryui"></div>
	
	<!-- Calendar Events List -->
	<div id="${n}events"></div>

    <!-- View Links -->
    <div class="upcal-view-links">
        <a id="${n}viewMoreEventsLink" class="upcal-view-more" href="<portlet:renderURL windowState="maximized"/>">
            View more events
        </a>
        
        <a id="${n}returnToCalendarLink" class="upcal-view-return" href="javascript:;" style="display:none">
            Return to calendar
        </a>
    </div>
  
</div>

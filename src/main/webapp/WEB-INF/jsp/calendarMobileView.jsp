<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<fmt:setTimeZone value="${ model.timezone }"/>
<c:set var="n"><portlet:namespace/></c:set>
<c:set var="mobile" value="true"/>

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
            $("#${n}events").html("<br/><p><spring:message code="eventlist.loading"/></p>");
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

	<!-- Mini-Calendar (jQuery) -->
    <div id="${n}inlineCalendar" class="jqueryui"></div>
	
	<!-- Calendar Events List -->
	<div id="${n}events"></div>

    <!-- View Links -->
    <div class="upcal-view-links">
        <a id="${n}returnToCalendarLink" class="upcal-view-return" href="javascript:;" 
                style="display:none" title="<spring:message code="return.to.calendar.link.title"/>">
            <spring:message code="return.to.calendar.link.text"/>
        </a>
    </div>
  
</div>

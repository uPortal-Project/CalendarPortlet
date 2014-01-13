package org.jasig.portlet.calendar.mvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.stereotype.Component;
@Component
public class UICalendarEventsBuilder {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public Map <String,Object> buildUIEvents(Set<CalendarDisplayEvent> calendarEvents, ResourceRequest request, List<String> errors){
	    final Map<String, Object> model = new HashMap<String, Object>();
	    final PortletSession session = request.getPortletSession();
        final String timezone = (String) session.getAttribute("timezone");
        final DateTimeZone tz = DateTimeZone.forID(timezone);
		int index = 0;
		final Set<JsonCalendarEventWrapper> events = new TreeSet<JsonCalendarEventWrapper>();
        for(CalendarDisplayEvent e : calendarEvents) {
            events.add(new JsonCalendarEventWrapper(e,index++));
        }
		/*
		 * Transform the event set into a map keyed by day.  This code is 
		 * designed to separate events by day according to the user's configured
		 * time zone.  This ensures that we keep complicated time-zone handling
		 * logic out of the JavaScript.
		 * 
		 * Events are keyed by a string uniquely representing the date that is 
		 * still orderable.  So that we can display a more user-friendly date
		 * name, we also create a map representing date display names for each
		 * date keyed in this response.
		 */
		// define a DateFormat object that uniquely identifies dates in a way 
		// that can easily be ordered 
        DateTimeFormatter orderableDf = new DateTimeFormatterBuilder()
                .appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2)
                .appendLiteral("-").appendDayOfMonth(2).toFormatter()
                .withZone(tz);
        DateTimeFormatter displayDf = new DateTimeFormatterBuilder()
                .appendDayOfWeekText().appendLiteral(" ")
                .appendMonthOfYearText().appendLiteral(" ").appendDayOfMonth(1)
                .toFormatter().withZone(tz);
        DateMidnight now = new DateMidnight(tz);
		String today = orderableDf.print(now);
		String tomorrow = orderableDf.print(now.plusDays(1));
		Map<String, String> dateDisplayNames = new HashMap<String, String>();
		Map<String, List<JsonCalendarEventWrapper>> eventsByDay = new LinkedHashMap<String, List<JsonCalendarEventWrapper>>();
		for (JsonCalendarEventWrapper event : events) {
			String day = orderableDf.print(event.getEvent().getDayStart());
	    	if (!eventsByDay.containsKey(day)) {
	    		eventsByDay.put(day, new ArrayList<JsonCalendarEventWrapper>());
	    		
	    		// Add an appropriate day name for this date to the date names
	    		// map.  If the day appears to be today or tomorrow display a 
	    		// special string value.  Otherwise, use the user-facing date
	    		// format object
	    		if (today.equals(day)) {
		    		dateDisplayNames.put(day, "Today");
	    		} else if (tomorrow.equals(day)) {
		    		dateDisplayNames.put(day, "Tomorrow");
	    		} else {
		    		dateDisplayNames.put(day, displayDf.print(event.getEvent().getDayStart()));
	    		}
	    	}
	    	eventsByDay.get(day).add(event);
		}
		if (log.isTraceEnabled()) {
	        log.trace("Prepared the following eventsByDay collection for user '" 
	                            + request.getRemoteUser() + "':" + eventsByDay);
		}
		model.put("dateMap", eventsByDay);
		model.put("dateNames", dateDisplayNames);
		model.put("viewName", "jsonView");
		model.put("errors", errors);
		return model;
	}
}

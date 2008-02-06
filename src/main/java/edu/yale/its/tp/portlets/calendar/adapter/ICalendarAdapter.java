package edu.yale.its.tp.portlets.calendar.adapter;

import java.util.Set;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.model.Period;
import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;

public interface ICalendarAdapter {

	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,
			Period period, PortletRequest request) throws CalendarException;

}

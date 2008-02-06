package edu.yale.its.tp.portlets.calendar;

import java.util.Calendar;

import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Wraps the VEvent calendar class to provide extra information.
 * 
 * @author Jen Bourey
 */
public class CalendarEvent extends VEvent {

	private static final long serialVersionUID = 1L;
	private Long calendarId;

	public CalendarEvent() {
		super();
	}

	public CalendarEvent(Date start, Date end, java.lang.String summary) {
		super(start, end, summary);
	}

	public CalendarEvent(Date start, Dur duration, java.lang.String summary) {
		super(start, duration, summary);
	}

	public CalendarEvent(Date start, java.lang.String summary) {
		super(start, summary);
	}

	public CalendarEvent(Long calendarId, PropertyList properties) {
		super(properties);
		this.calendarId = calendarId;
	}

	public CalendarEvent(PropertyList properties, ComponentList alarms) {
		super(properties, alarms);
	}
	
	public CalendarEvent(PropertyList properties) {
		super(properties);
	}

	public Long getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Long calendarId) {
		this.calendarId = calendarId;
	}

	public boolean isAllDay() {
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(this.getStartDate().getDate().getTime());
		long oneday = 1000 * 60 * 60 * 24;
		if (start.get(Calendar.HOUR_OF_DAY) == 0 && start.get(Calendar.MINUTE) == 0 && this.getEndDate().getDate().getTime() - this.getStartDate().getDate().getTime() >= oneday)
			return true;
		else
			return false;
	}
	
}

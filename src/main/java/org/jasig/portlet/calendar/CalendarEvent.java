/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar;

import java.util.TimeZone;

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

	/**
	 * Default constructor.
	 */
	public CalendarEvent() {
		super();
	}

	/**
	 * Construct a new CalendarEvent.
	 * 
	 * @param start the start date of the new event
	 * @param end the end date of the new event
	 * @param summary the event summary
	 */
	public CalendarEvent(Date start, Date end, java.lang.String summary) {
		super(start, end, summary);
	}

	/**
	 * Construct a new CalendarEvent.
	 * 
	 * @param start the start date of the new event
	 * @param duration the duration of the new event
	 * @param summary the event summary
	 */
	public CalendarEvent(Date start, Dur duration, java.lang.String summary) {
		super(start, duration, summary);
	}

	/**
	 * Construct a new CalendarEvent.
	 * 
	 * @param start the start date of the new event
	 * @param summary the event summary
	 */
	public CalendarEvent(Date start, java.lang.String summary) {
		super(start, summary);
	}

	/**
	 * Construct a new CalendarEvent.
	 * 
	 * @param calendarId id of the CalendarConfiguration that produced this event
	 * @param properties list of event properties
	 */
	public CalendarEvent(Long calendarId, PropertyList properties) {
		super(properties);
		this.calendarId = calendarId;
	}

	/**
	 * Construct a new CalendarEvent.
	 * 
	 * @param properties list of event properties
	 * @param alarms list of alarms
	 */
	public CalendarEvent(PropertyList properties, ComponentList alarms) {
		super(properties, alarms);
	}
	
	/**
	 * Construct a new CalendarEvent.
	 * 
	 * @param properties list of event properties
	 */
	public CalendarEvent(PropertyList properties) {
		super(properties);
	}

	/**
	 * Get the ID of the CalendarConfiguration that produced this event.
	 * 
	 * @return CalendarConfiguration id
	 */
	public Long getCalendarId() {
		return calendarId;
	}

	/**
	 * Set the ID of the CalendarConfiguration that produced this event.
	 * 
	 * @param CalendarConfiguration id
	 */
	public void setCalendarId(Long calendarId) {
		this.calendarId = calendarId;
	}

	/**
	 * Determines whether this event is an "all-day" event or not.
	 * All-day events are defined as starting at 12:00 AM and lasting
	 * for 24 hours or more.
	 * 
	 * @return
	 */
	public boolean isAllDay(String tz) {
		return AllDayUtil.isAllDayEvent(this, TimeZone.getTimeZone(tz));
	}
	
	/**
	 * Determines whether this event is an "all-day" event or not.
	 * All-day events are defined as starting at 12:00 AM and lasting
	 * for 24 hours or more.
	 * 
	 * @return
	 */
	public boolean isAllDay(TimeZone tz) {
		return AllDayUtil.isAllDayEvent(this, tz);
	}
	
}

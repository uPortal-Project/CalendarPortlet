/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.calendar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.TimeZone;

import org.jasig.portlet.calendar.util.AllDayUtil;

import net.fortuna.ical4j.model.Component;
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

    @Override
    public Component copy() throws ParseException, IOException,
            URISyntaxException {
        return new CalendarEvent(this.calendarId, super.copy().getProperties());
    }
	
	

}

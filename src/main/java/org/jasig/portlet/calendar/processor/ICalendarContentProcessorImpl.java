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

package org.jasig.portlet.calendar.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserImpl;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.adapter.CalendarException;


/**
 * Implementation of {@link IContentProcessor} that uses iCal4j to process
 * iCalendar-formatted data streams.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: ICalendarContentProcessorImpl.java Exp $
 */
public class ICalendarContentProcessorImpl implements IContentProcessor {

	protected final Log log = LogFactory.getLog(this.getClass());
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.ContentProcessor#getEvents(java.lang.Long, net.fortuna.ical4j.model.Period, java.io.InputStream)
	 */
	public Set<CalendarEvent> getEvents(Long calendarId, Period period, InputStream in) {
		try {
			log.debug("begin getEvents");
			CalendarBuilder builder = new CalendarBuilder(new CalendarParserImpl());
			Calendar calendar = builder.build(in);
			log.debug("calendar built");
			return convertCalendarToEvents(calendarId, calendar, period);
			
		} catch (IOException e) {
			log.error("IOException in getEvents", e);
			throw new CalendarException("caught IOException", e);
		} catch (ParserException e) {
			log.error("ParserException in getEvents", e);
			throw new CalendarException("caught ParserException", e);
		}
	}

	/**
	 * 
	 * @param calendarId
	 * @param calendar
	 * @param period
	 * @return
	 * @throws CalendarException
	 */
	@SuppressWarnings("unchecked")
	protected final Set<CalendarEvent> convertCalendarToEvents(Long calendarId,
			net.fortuna.ical4j.model.Calendar calendar, Period period)
			throws CalendarException {

		Set<CalendarEvent> events = new HashSet<CalendarEvent>();

		// if the calendar is null, return empty set
		if (calendar == null) {
			log.warn("calendar was empty, returning empty set");
			return Collections.emptySet();
		}
		
		// retrieve the list of events for this calendar within the
		// specified time period
		for (Iterator<Component> i = calendar.getComponents().iterator(); i
				.hasNext();) {
			Component component = i.next();
			if (component.getName().equals("VEVENT")) {
				VEvent event = (VEvent) component;
				log.trace("processing event " + event.getSummary());
				// calculate the recurrence set for this event
				// for the specified time period
				PeriodList periods = event.calculateRecurrenceSet(period);

				// add each recurrence instance to the event list
				for (Iterator<Period> iter = periods.iterator(); iter.hasNext();) {
					Period eventper = iter.next();
					log.debug("Found time period staring at " + eventper.getStart().isUtc() + ", " + eventper.getStart().getTimeZone() + ", " + event.getStartDate().getTimeZone() + ", " + event.getStartDate().isUtc());

					PropertyList props = event.getProperties();

					// create a new property list, setting the date
					// information to this event period
					PropertyList newprops = new PropertyList();
					DtStart start = new DtStart();
					start.setDate(eventper.getStart());
					start.setTimeZone(event.getStartDate().getTimeZone());
					start.setUtc(event.getStartDate().isUtc());
					newprops.add(start);
                    DtEnd end = new DtEnd();
                    end.setTimeZone(event.getEndDate().getTimeZone());
                    end.setDate(eventper.getEnd());
                    end.setUtc(event.getEndDate().isUtc());
					newprops.add(end);
					for (Iterator<Property> iter2 = props.iterator(); iter2
							.hasNext();) {
						Property prop = iter2.next();

						// only add non-date-related properties
						if (!(prop instanceof DtStart)
						        && !(prop instanceof DtEnd)
						        && !(prop instanceof Duration)
						        && !(prop instanceof RRule)
						        && !(prop instanceof RDate)
						        && !(prop instanceof ExRule)
						        && !(prop instanceof ExDate)) {
						    newprops.add(prop);
						}
					}

					// create the new event from our property list
					CalendarEvent newevent = new CalendarEvent(calendarId,
							newprops);
					events.add(newevent);
					log.trace("added event " + newevent);
				}
			}
		}

		return events;
	}
}

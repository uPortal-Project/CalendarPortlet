/*******************************************************************************
* Copyright 2008, The Board of Regents of the University of Wisconsin System.
* All rights reserved.
*
* A non-exclusive worldwide royalty-free license is granted for this Software.
* Permission to use, copy, modify, and distribute this Software and its
* documentation, with or without modification, for any purpose is granted
* provided that such redistribution and use in source and binary forms, with or
* without modification meets the following conditions:
*
* 1. Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
* acknowledgement:
*
* "This product includes software developed by The Board of Regents of
* the University of Wisconsin System.
*
*THIS SOFTWARE IS PROVIDED BY THE BOARD OF REGENTS OF THE UNIVERSITY OF
*WISCONSIN SYSTEM "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
*BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
*PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE BOARD OF REGENTS OF
*THE UNIVERSITY OF WISCONSIN SYSTEM BE LIABLE FOR ANY DIRECT, INDIRECT,
*INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
*LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
*OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
*ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
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
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
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

	private Log log = LogFactory.getLog(this.getClass());
	
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
				log.trace("processing event " + event.getSummary().getValue());
				// calculate the recurrence set for this event
				// for the specified time period
				PeriodList periods = event.calculateRecurrenceSet(period);

				// add each recurrence instance to the event list
				for (Iterator<Period> iter = periods.iterator(); iter.hasNext();) {
					Period eventper = iter.next();

					PropertyList props = event.getProperties();

					// create a new property list, setting the date
					// information to this event period
					PropertyList newprops = new PropertyList();
					newprops.add(new DtStart(eventper.getStart()));
					newprops.add(new DtEnd(eventper.getEnd()));
					for (Iterator<Property> iter2 = props.iterator(); iter2
							.hasNext();) {
						Property prop = iter2.next();

						// only add non-date-related properties
						if (!(prop instanceof DtStart)
								&& !(prop instanceof DtEnd)
								&& !(prop instanceof Duration)
								&& !(prop instanceof RRule))
							newprops.add(prop);
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

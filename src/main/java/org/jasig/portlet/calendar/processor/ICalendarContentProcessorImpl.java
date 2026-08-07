/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.calendar.processor;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserImpl;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.joda.time.Interval;

/**
 * Implementation of {@link IContentProcessor} that uses iCal4j to process iCalendar-formatted data
 * streams.
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: ICalendarContentProcessorImpl.java Exp $
 */
public class ICalendarContentProcessorImpl implements IContentProcessor<Calendar> {

  protected final Log log = LogFactory.getLog(this.getClass());

  public Calendar getIntermediateCalendar(Interval interval, InputStream in) {
    try {
      log.debug("begin getEvents");
      CalendarBuilder builder = new CalendarBuilder(new CalendarParserImpl());
      Calendar calendar = builder.build(in);
      log.debug("calendar built");
      return calendar;

    } catch (IOException e) {
      throw new CalendarException("caught IOException", e);
    } catch (ParserException e) {
      throw new CalendarException("caught ParserException", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jasig.portlet.calendar.adapter.ContentProcessor#getEvents(java.lang.Long, net.fortuna.ical4j.model.Period, java.io.InputStream)
   */
  public Set<VEvent> getEvents(Interval interval, Calendar calendar) {
    return convertCalendarToEvents(calendar, interval);
  }

  /**
   * @param calendar
   * @param interval
   * @return
   * @throws CalendarException
   */
  protected final Set<VEvent> convertCalendarToEvents(
      net.fortuna.ical4j.model.Calendar calendar, Interval interval) throws CalendarException {

    // Use ZonedDateTime for the period so that recurrence rules using calendar units
    // (YEARLY, MONTHLY, etc.) can be expanded correctly. Instant does not support
    // calendar-based arithmetic like plus(years).
    Period period =
        new Period(
            Instant.ofEpochMilli(interval.getStartMillis()).atZone(ZoneOffset.UTC),
            Instant.ofEpochMilli(interval.getEndMillis()).atZone(ZoneOffset.UTC));

    // Use identity-based set because ical4j 4.x VEvent.equals() uses UID equality,
    // which would collapse recurring event instances that share the same UID.
    Set<VEvent> events = Collections.newSetFromMap(new IdentityHashMap<>());

    // if the calendar is null, return empty set
    if (calendar == null) {
      log.warn("calendar was empty, returning empty set");
      return Collections.emptySet();
    }

    // retrieve the list of events for this calendar within the
    // specified time period
    for (Component component : calendar.getComponents()) {
      /*
       * CAP-143:  Log a warning and ignore events that cannot be
       * processed at this stage
       */
      try {
        if (component.getName().equals("VEVENT")) {
          VEvent event = (VEvent) component;
          if (log.isTraceEnabled()) {
            log.trace("processing event " + event.getSummary());
          }
          // calculate the recurrence set for this event
          // for the specified time period
          Set<Period> periods = event.calculateRecurrenceSet(period);

          // add each recurrence instance to the event list
          for (Period eventper : periods) {
            if (log.isDebugEnabled()) {
              log.debug("Found time period starting at " + eventper.getStart());
            }

            // create a new property list, setting the date
            // information to this event period
            PropertyList newprops = new PropertyList();
            newprops = newprops.add(new DtStart(eventper.getStart()));
            newprops = newprops.add(new DtEnd(eventper.getEnd()));

            for (Property prop : event.getProperties()) {
              // only add non-date-related properties
              if (!(prop instanceof DtStart)
                  && !(prop instanceof DtEnd)
                  && !(prop instanceof Duration)
                  && !(prop instanceof RRule)
                  && !(prop instanceof RDate)
                  && !(prop instanceof ExDate)) {
                newprops = newprops.add(prop);
              }
            }

            // create the new event from our property list
            VEvent newevent = new VEvent(newprops);
            events.add(newevent);
            log.trace("added event " + newevent);
          }
        }
      } catch (Exception e) {
        final String msg = "Failed to process the following ical4j component:  " + component;
        log.warn(msg, e);
      }
    }

    return events;
  }
}

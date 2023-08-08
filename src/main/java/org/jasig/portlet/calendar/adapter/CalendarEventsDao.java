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
package org.jasig.portlet.calendar.adapter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.portlet.PortletRequest;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class CalendarEventsDao {

  protected final Log log = LogFactory.getLog(getClass());

  private Cache cache;

  /** @param cache the cache to set */
  @Required
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  private Map<String, DateTimeFormatter> dateFormatters =
      new ConcurrentHashMap<String, DateTimeFormatter>();

  private Map<String, DateTimeFormatter> timeFormatters =
      new ConcurrentHashMap<String, DateTimeFormatter>();

  private MessageSource messageSource;
  /**
   * Setter of attribute messageSource.
   *
   * @param messageSource the attribute messageSource to set
   */
  @Required
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public Calendar getCalendar(
      ICalendarAdapter adapter,
      CalendarConfiguration calendarConfig,
      Interval interval,
      PortletRequest request) {

    // get the set of pre-timezone-corrected events for the requested period
    // Rely on the adapter to do caching
    final CalendarEventSet eventSet = adapter.getEvents(calendarConfig, interval, request);

    // Create a calendar from the events
    Calendar calendar = new Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);

    for (VEvent event : eventSet.getEvents()) {
      calendar.getComponents().add(event);
    }
    return calendar;
  }

  /**
   * Obtains the calendar events from the adapter and returns timezone-adjusted events within the
   * requested interval.
   *
   * @param adapter Adapter to invoke to obtain the calendar events
   * @param calendar Per-user Calendar configuration
   * @param interval Interval to return events for
   * @param request Portlet request
   * @param usersConfiguredDateTimeZone Timezone to adjust the calendar events to (typically the
   *     user's timezone)
   * @return Set of calendar events meeting the requested criteria
   */
  public Set<CalendarDisplayEvent> getEvents(
      ICalendarAdapter adapter,
      CalendarConfiguration calendar,
      Interval interval,
      PortletRequest request,
      DateTimeZone usersConfiguredDateTimeZone,
      int calendarIndex) {

    // Get the set of calendar events for the requested period.
    // We invoke the adapter before checking cache because we expect the adapter
    // to do the first-level caching of the events.
    final CalendarEventSet eventSet = adapter.getEvents(calendar, interval, request);

    // The calendar events from the adapter will reflect the timezone of the calendar
    // server in the event times.  The events need to be corrected to reflect the
    // requested timezone (typically the user's timezone). Adjusting the
    // event's timezone is an expensive operation so the JSON of the
    // timezone-adjusted events is cached per timezone.

    // Append the requested time zone id to the retrieve event set's cache
    // key to generate a timezone-aware cache key
    final String tzKey = eventSet.getKey().concat(usersConfiguredDateTimeZone.getID());

    // attempt to retrieve the timezone-aware event set from cache
    Element cachedElement = this.cache.get(tzKey);
    if (cachedElement != null) {
      if (log.isDebugEnabled()) {
        log.debug("Retrieving JSON timezone-aware event set from cache, key:" + tzKey);
      }
      @SuppressWarnings("unchecked")
      final Set<CalendarDisplayEvent> jsonEvents =
          (Set<CalendarDisplayEvent>) cachedElement.getValue();
      return jsonEvents;
    }

    // if the timezone-corrected event set is not available in the cache
    // generate a new set and cache it
    else {

      // for each event in the event set, generate a set of timezone-corrected
      // event occurrences and add it to the new set
      final Set<CalendarDisplayEvent> displayEvents = new HashSet<CalendarDisplayEvent>();
      for (VEvent event : eventSet.getEvents()) {
        try {
          displayEvents.addAll(
              getDisplayEvents(event, interval, request.getLocale(), usersConfiguredDateTimeZone, calendarIndex));
        } catch (ParseException e) {
          log.error("Exception parsing event", e);
        } catch (IOException e) {
          log.error("Exception parsing event", e);
        } catch (URISyntaxException e) {
          log.error("Exception parsing event", e);
        } catch (IllegalArgumentException e) {
          // todo fix the root problem. Just masking for the moment because no time to fix.
          log.info("Likely invalid event returned from exchangeAdapter; see CAP-159");
        }
      }

      // Cache and return the resulting event list.  If the event set
      // was cached, set the event list to expire at about the same time so it
      // doesn't live in cache beyond the time the data it is derived
      // from is considered up to date. Time to live is relative to the
      // time the item is put into cache so the resulting event list will typically
      // expire from 1 second before the event set to afterward by the amount
      // of execution time from the adapter to displayEvents computation
      // completing which should not be a big delta.
      cachedElement = new Element(tzKey, displayEvents);
      long currentTime = System.currentTimeMillis();
      if (eventSet.getExpirationTime() > currentTime) {
        long timeToLiveInMilliseconds = eventSet.getExpirationTime() - currentTime;
        int timeToLiveInSeconds = (int) timeToLiveInMilliseconds / 1000;
        cachedElement.setTimeToLive(timeToLiveInSeconds);
        if (log.isDebugEnabled()) {
          log.debug(
              "Storing JSON timezone-aware event set to cache, key:"
                  + tzKey
                  + " with expiration in "
                  + timeToLiveInSeconds
                  + " seconds to"
                  + " coincide with adapter's cache expiration time");
        }
      }
      this.cache.put(cachedElement);
      return displayEvents;
    }
  }

  /**
   * Get a JSON-appropriate representation of each recurrence of an event within the specified time
   * period.
   *
   * @param e
   * @param interval
   * @param usersConfiguredDateTimeZone
   * @return
   * @throws IOException
   * @throws URISyntaxException
   * @throws ParseException
   */
  protected Set<CalendarDisplayEvent> getDisplayEvents(
      VEvent e, Interval interval, Locale locale, DateTimeZone usersConfiguredDateTimeZone, int calendarIndex)
      throws IOException, URISyntaxException, ParseException {

    final VEvent event = (VEvent) e.copy();

    DateTime eventStart;
    DateTime eventEnd = null;

    if (event.getStartDate().getTimeZone() == null && !event.getStartDate().isUtc()) {
      if (log.isDebugEnabled()) {
        log.debug("Identified event " + event.getSummary() + " as a floating event");
      }

      int offset = usersConfiguredDateTimeZone.getOffset(event.getStartDate().getDate().getTime());
      eventStart =
          new DateTime(
              event.getStartDate().getDate().getTime() - offset, usersConfiguredDateTimeZone);
      if (event.getEndDate() != null) {
        eventEnd =
            new DateTime(
                event.getEndDate().getDate().getTime() - offset, usersConfiguredDateTimeZone);
      }

    } else {
      eventStart = new DateTime(event.getStartDate().getDate(), usersConfiguredDateTimeZone);
      if (event.getEndDate() != null) {
        eventEnd = new DateTime(event.getEndDate().getDate(), usersConfiguredDateTimeZone);
      }
    }

    if (eventEnd == null) {
      eventEnd = eventStart;
    }

    // Multi-day events may begin in the past;  make sure to choose a date in range for the first pass...
    final Date firstDayToProcess =
        interval.contains(event.getStartDate().getDate().getTime())
            ? event.getStartDate().getDate()
            : interval.getStart().toDate();

    DateMidnight startOfTheSpecificDay =
        new DateMidnight(firstDayToProcess, usersConfiguredDateTimeZone);
    DateMidnight endOfTheSpecificDay = startOfTheSpecificDay.plusDays(1);

    final DateTimeFormatter df = getDateFormatter(locale, usersConfiguredDateTimeZone);
    final DateTimeFormatter tf = getTimeFormatter(locale, usersConfiguredDateTimeZone);
    final Set<CalendarDisplayEvent> events = new HashSet<CalendarDisplayEvent>();
    final Interval eventInterval = new Interval(eventStart, eventEnd);

    do {
      final Interval theSpecificDay =
          new Interval(
              startOfTheSpecificDay.getMillis(),
              endOfTheSpecificDay.getMillis(),
              usersConfiguredDateTimeZone);

      /*
       * Test if the event interval abuts the start of the day or is within the day.
       * This start time check is needed for the corner case where a zero duration interval
       * is set for midnight.
       * The start times are tested directly as opposed to using abuts() because that method
       * also returns true if the intervals abut at the end of the day. We want to associate
       * instant events that start at midnight with the starting day, not the ending day.
       */
      if (theSpecificDay.getStart().isEqual(eventStart) || theSpecificDay.overlaps(eventInterval)) {
        final CalendarDisplayEvent json =
            new CalendarDisplayEvent(event, eventInterval, theSpecificDay, df, tf, calendarIndex);
        events.add(json);
      }

      startOfTheSpecificDay = startOfTheSpecificDay.plusDays(1);
      endOfTheSpecificDay = endOfTheSpecificDay.plusDays(1);

    } while (!startOfTheSpecificDay.isAfter(eventEnd) && interval.contains(startOfTheSpecificDay));

    return events;
  }

  protected DateTimeFormatter getDateFormatter(Locale locale, DateTimeZone timezone) {
    if (this.dateFormatters.containsKey(timezone.getID())) {
      return this.dateFormatters.get(timezone.getID());
    }
    final String displayPattern =
        this.messageSource.getMessage("date.formatter.display", null, "EEE MMM d", locale);
    DateTimeFormatter df =
        new DateTimeFormatterBuilder()
            .appendPattern(displayPattern)
            .toFormatter()
            .withZone(timezone);
    this.dateFormatters.put(timezone.getID(), df);
    return df;
  }

  protected DateTimeFormatter getTimeFormatter(Locale locale, DateTimeZone timezone) {
    if (this.timeFormatters.containsKey(timezone.getID())) {
      return this.timeFormatters.get(timezone.getID());
    }
    final String displayPattern =
        this.messageSource.getMessage("time.formatter.display", null, "h:mm a", locale);
    DateTimeFormatter tf =
        new DateTimeFormatterBuilder()
            .appendPattern(displayPattern)
            .toFormatter()
            .withZone(timezone);
    this.timeFormatters.put(timezone.getID(), tf);
    return tf;
  }
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/testContext.xml")
public class CalendarEventsDaoIntegrationTest {

  CalendarEventsDao eventsDao;
  IContentProcessor<Calendar> processor;
  Resource calendarFile;

  @Autowired(required = true)
  ApplicationContext context;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    eventsDao = new CalendarEventsDao();
    eventsDao.setMessageSource((MessageSource) context.getBean("messageSource"));
    processor = new ICalendarContentProcessorImpl();
    calendarFile = context.getResource("classpath:sampleEvents.ics");
  }

  @Test
  public void testGetEvents() throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
    DateMidnight start = new DateMidnight(2012, 7, 3, tz);
    Interval interval = new Interval(start, start.plusDays(7));

    Calendar c = processor.getIntermediateCalendar(interval, calendarFile.getInputStream());
    Set<VEvent> events = processor.getEvents(interval, c);

    assertEquals(2, events.size());

    List<CalendarDisplayEvent> displayEvents = new ArrayList<CalendarDisplayEvent>();

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz));
    }
    Collections.sort(displayEvents);

    CalendarDisplayEvent event = displayEvents.get(0);
    assertEquals("12:00 AM", event.getStartTime());

    event = displayEvents.get(1);
    assertEquals(9, event.getDayStart().getHourOfDay());
  }

  @Test
  public void testGetEventsAlternateTimezone()
      throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/Chicago");
    DateMidnight start = new DateMidnight(2012, 7, 3, tz);
    Interval interval = new Interval(start, start.plusDays(7));

    Calendar c = processor.getIntermediateCalendar(interval, calendarFile.getInputStream());
    Set<VEvent> events = processor.getEvents(interval, c);

    assertEquals(2, events.size());

    List<CalendarDisplayEvent> displayEvents = new ArrayList<CalendarDisplayEvent>();

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz));
    }
    Collections.sort(displayEvents);

    CalendarDisplayEvent event = displayEvents.get(0);
    assertEquals(0, event.getDayStart().getHourOfDay());

    event = displayEvents.get(1);
    assertEquals(11, event.getDayStart().getHourOfDay());
  }

  @Test
  public void testGetArizonaEvent() throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
    DateMidnight start = new DateMidnight(2013, 2, 3, tz);
    Interval interval = new Interval(start, start.plusDays(7));

    Calendar c = processor.getIntermediateCalendar(interval, calendarFile.getInputStream());
    Set<VEvent> events = processor.getEvents(interval, c);

    assertEquals(1, events.size());

    List<CalendarDisplayEvent> displayEvents = new ArrayList<CalendarDisplayEvent>();

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz));
    }
    Collections.sort(displayEvents);

    CalendarDisplayEvent event = displayEvents.get(0);
    assertEquals("9:00 AM", event.getStartTime());
  }

  @Test
  public void testGetUTCEvent() throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
    DateMidnight start = new DateMidnight(2013, 1, 3, tz);
    Interval interval = new Interval(start, start.plusDays(7));

    Calendar c = processor.getIntermediateCalendar(interval, calendarFile.getInputStream());
    Set<VEvent> events = processor.getEvents(interval, c);

    assertEquals(1, events.size());

    List<CalendarDisplayEvent> displayEvents = new ArrayList<CalendarDisplayEvent>();

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz));
    }
    Collections.sort(displayEvents);

    CalendarDisplayEvent event = displayEvents.get(0);
    assertEquals("2:00 PM", event.getStartTime());
  }

  @Test
  public void testGetBedeworkEvent() throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/Phoenix");
    DateMidnight start = new DateMidnight(2014, 2, 2, tz);
    Interval interval = new Interval(start, start.plusDays(1));

    Calendar c = processor.getIntermediateCalendar(interval, calendarFile.getInputStream());
    Set<VEvent> events = processor.getEvents(interval, c);

    assertEquals(1, events.size());

    List<CalendarDisplayEvent> displayEvents = new ArrayList<CalendarDisplayEvent>();

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz));
    }
    Collections.sort(displayEvents);

    CalendarDisplayEvent event = displayEvents.get(0);
    assertEquals("2:00 PM", event.getStartTime());
  }

  /**
   * {@code getCalendar} backs the "export calendar" (.ics download) action. In ical4j 4.x the lists
   * returned by {@code Calendar.getProperties()} and {@code getComponents()} are unmodifiable, so
   * the calendar must be assembled through the {@code Calendar} API rather than by adding to those
   * lists. The calendar must carry its required properties and every event, and must serialize.
   */
  @Test
  public void testGetCalendarAssemblesPropertiesAndEvents() throws Exception {

    DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
    DateMidnight start = new DateMidnight(2012, 7, 3, tz);
    Interval interval = new Interval(start, start.plusDays(7));

    Calendar source = processor.getIntermediateCalendar(interval, calendarFile.getInputStream());
    Set<VEvent> events = processor.getEvents(interval, source);
    assertEquals(2, events.size());

    ICalendarAdapter adapter = Mockito.mock(ICalendarAdapter.class);
    Mockito.when(adapter.getEvents(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new CalendarEventSet("test-key", events));

    Calendar calendar =
        eventsDao.getCalendar(adapter, new CalendarConfiguration(), interval, null);

    assertTrue(calendar.getProperty(Property.PRODID).isPresent());
    assertTrue(calendar.getProperty(Property.VERSION).isPresent());
    assertTrue(calendar.getProperty(Property.CALSCALE).isPresent());
    assertEquals(2, calendar.getComponents(Component.VEVENT).size());

    // The export action streams the calendar through CalendarOutputter
    StringWriter out = new StringWriter();
    new CalendarOutputter().output(calendar, out);
    String ics = out.toString();
    assertTrue(ics, ics.contains("BEGIN:VCALENDAR"));
    assertTrue(ics, ics.contains("VERSION:2.0"));
    assertTrue(ics, ics.contains("CALSCALE:GREGORIAN"));
    assertTrue(ics, ics.contains("BEGIN:VEVENT"));
  }

  /**
   * A floating DATE-TIME that does not exist in the user's zone (it falls in a spring-forward DST
   * gap) must still be displayed. Joda-Time throws IllegalInstantException when asked to build such
   * a local time, and the caller swallows that exception, so the event would silently vanish.
   *
   * <p>RFC 5545 §3.3.5: a local time that does not occur at a standard-to-daylight transition is
   * interpreted using the UTC offset before the gap. 2:30 AM on 2026-03-08 in New York is therefore
   * 07:30Z, which is 3:30 AM EDT.
   */
  @Test
  public void testGetFloatingEventInDstGap() throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/New_York");
    DateMidnight start = new DateMidnight(2026, 3, 8, tz);
    Interval interval = new Interval(start, start.plusDays(1));

    // Floating (no TZID): DTSTART 2026-03-08T02:30 (does not exist), DTEND 2026-03-08T04:00
    net.fortuna.ical4j.model.PropertyList props = new net.fortuna.ical4j.model.PropertyList();
    props = props.add(new net.fortuna.ical4j.model.property.Uid("gap0001"));
    props = props.add(new net.fortuna.ical4j.model.property.Summary("Gap Event"));
    props =
        props.add(
            new net.fortuna.ical4j.model.property.DtStart<>(
                java.time.LocalDateTime.of(2026, 3, 8, 2, 30)));
    props =
        props.add(
            new net.fortuna.ical4j.model.property.DtEnd<>(
                java.time.LocalDateTime.of(2026, 3, 8, 4, 0)));
    VEvent event = new VEvent(props);

    List<CalendarDisplayEvent> displayEvents =
        new ArrayList<CalendarDisplayEvent>(
            eventsDao.getDisplayEvents(event, interval, Locale.US, tz));

    assertEquals(1, displayEvents.size());
    assertEquals("3:30 AM", displayEvents.get(0).getStartTime());
  }

  /**
   * A floating all-day DATE in a zone whose DST transition skips midnight (Havana springs forward
   * at 00:00 to 01:00) has no local midnight. The event must still be displayed, starting at the
   * first valid instant of that day.
   */
  @Test
  public void testGetFloatingAllDayEventInMidnightDstGap()
      throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("America/Havana");
    // Havana has no 2026-03-08T00:00, so build the interval from a valid mid-day instant
    DateTime from = new DateTime(2026, 3, 7, 12, 0, tz);
    Interval interval = new Interval(from, from.plusDays(3));

    net.fortuna.ical4j.model.PropertyList props = new net.fortuna.ical4j.model.PropertyList();
    props = props.add(new net.fortuna.ical4j.model.property.Uid("gap0002"));
    props = props.add(new net.fortuna.ical4j.model.property.Summary("All Day Gap Event"));
    props =
        props.add(
            new net.fortuna.ical4j.model.property.DtStart<>(java.time.LocalDate.of(2026, 3, 8)));
    props =
        props.add(
            new net.fortuna.ical4j.model.property.DtEnd<>(java.time.LocalDate.of(2026, 3, 9)));
    VEvent event = new VEvent(props);

    List<CalendarDisplayEvent> displayEvents =
        new ArrayList<CalendarDisplayEvent>(
            eventsDao.getDisplayEvents(event, interval, Locale.US, tz));

    assertTrue("expected the all-day event to be displayed", displayEvents.size() >= 1);
    // The first valid instant of 2026-03-08 in Havana is 01:00
    assertEquals(1, displayEvents.get(0).getDayStart().getHourOfDay());
  }

  /**
   * An event may specify DURATION instead of DTEND (RFC 5545 §3.6.1). The end must be derived
   * from DTSTART + DURATION; otherwise the event collapses to zero length.
   *
   * <p>This test exercises {@code getDisplayEvents} with a raw, un-expanded DURATION-only event
   * (as an adapter such as Exchange/CalDav can supply). The iCalendar content processor rebuilds
   * events with an explicit DtEnd from the computed recurrence period, so a DURATION-only event
   * only reaches the end-date handling when it comes straight from an adapter rather than through
   * recurrence expansion. The event starts at 15:00 UTC with DURATION PT1H, so in UTC it runs
   * 3:00 PM - 4:00 PM.
   */
  @Test
  public void testGetDurationOnlyEvent() throws IOException, URISyntaxException, ParseException {

    DateTimeZone tz = DateTimeZone.forID("UTC");
    DateMidnight start = new DateMidnight(2015, 1, 15, tz);
    Interval interval = new Interval(start, start.plusDays(1));

    // A DURATION-only VEvent: DTSTART 2015-01-15T15:00:00Z, DURATION PT1H, no DTEND.
    java.time.Instant eventStart = java.time.Instant.parse("2015-01-15T15:00:00Z");
    net.fortuna.ical4j.model.PropertyList props = new net.fortuna.ical4j.model.PropertyList();
    props = props.add(new net.fortuna.ical4j.model.property.Uid("dur0001"));
    props = props.add(new net.fortuna.ical4j.model.property.Summary("Duration Only Event"));
    props = props.add(new net.fortuna.ical4j.model.property.DtStart<>(eventStart));
    props = props.add(
        new net.fortuna.ical4j.model.property.Duration(java.time.Duration.ofHours(1)));
    VEvent event = new VEvent(props);

    List<CalendarDisplayEvent> displayEvents =
        new ArrayList<CalendarDisplayEvent>(
            eventsDao.getDisplayEvents(event, interval, Locale.US, tz));
    Collections.sort(displayEvents);

    assertEquals(1, displayEvents.size());
    CalendarDisplayEvent displayEvent = displayEvents.get(0);
    assertEquals("3:00 PM", displayEvent.getStartTime());
    // The key assertion: the end is derived from DTSTART + DURATION, not collapsed to the start.
    assertEquals("4:00 PM", displayEvent.getEndTime());
  }
}

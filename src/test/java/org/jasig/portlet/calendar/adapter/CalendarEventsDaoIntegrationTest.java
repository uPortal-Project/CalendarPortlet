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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    int calendarIndex=0;

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz, calendarIndex));
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

    int calendarIndex=0;

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz, calendarIndex));
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

    int calendarIndex=0;

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz, calendarIndex));
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

    int calendarIndex=0;

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz, calendarIndex));
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

    int calendarIndex=0;

    for (VEvent event : events) {
      displayEvents.addAll(eventsDao.getDisplayEvents(event, interval, Locale.US, tz, calendarIndex));
    }
    Collections.sort(displayEvents);

    CalendarDisplayEvent event = displayEvents.get(0);
    assertEquals("2:00 PM", event.getStartTime());
  }
}

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.portlet.PortletRequest;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.courses.dao.ICoursesDao;
import org.jasig.portlet.courses.model.xml.CourseMeeting;
import org.jasig.portlet.courses.model.xml.Location;
import org.jasig.portlet.courses.model.xml.Term;
import org.jasig.portlet.courses.model.xml.TermList;
import org.jasig.portlet.courses.model.xml.personal.Course;
import org.jasig.portlet.courses.model.xml.personal.CoursesByTerm;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CoursesCalendarAdapterTest {

  private static final String TZ_ID = "America/Los_Angeles";
  private static final DateTimeZone TZ = DateTimeZone.forID(TZ_ID);
  private static final ZoneId ZONE = ZoneId.of(TZ_ID);

  private CoursesCalendarAdapter adapter;
  private PortletRequest request;
  private CalendarConfiguration config;
  private Term term;

  @Before
  public void setUp() {
    // Term: Mon 2026-03-02 through Mon 2026-03-30, in the user's zone. US daylight saving time
    // begins on Sun 2026-03-08, in the middle of the term.
    term = new Term();
    term.setCode("2026S");
    term.setDisplayName("Spring 2026");
    term.setStart(new DateTime(2026, 3, 2, 0, 0, TZ));
    term.setEnd(new DateTime(2026, 3, 30, 23, 59, TZ));

    // A course that meets Mondays 8:00 PM - 9:00 PM Pacific for the whole term
    final CourseMeeting meeting = new CourseMeeting();
    meeting.getDayIds().add("M");
    meeting.setStartDate(new DateMidnight(2026, 3, 2, TZ));
    meeting.setEndDate(new DateMidnight(2026, 3, 30, TZ));
    meeting.setStartTime(new LocalTime(20, 0));
    meeting.setEndTime(new LocalTime(21, 0));
    final Location location = new Location();
    location.setDisplayName("Room 101");
    meeting.setLocation(location);

    final Course course = new Course();
    course.setCode("CS-101");
    course.setTitle("Intro to Computing");
    course.getCourseMeetings().add(meeting);

    final CoursesByTerm coursesByTerm = new CoursesByTerm();
    coursesByTerm.setTermCode("2026S");
    coursesByTerm.getCourses().add(course);

    final TermList termList = new TermList();
    termList.getTerms().add(term);

    request = Mockito.mock(PortletRequest.class);
    config = new CalendarConfiguration();

    final ICoursesDao courseDao = Mockito.mock(ICoursesDao.class);
    Mockito.when(courseDao.getTermList(Mockito.any())).thenReturn(termList);
    Mockito.when(courseDao.getCoursesByTerm(Mockito.any(), Mockito.eq("2026S")))
        .thenReturn(coursesByTerm);

    final ICacheKeyGenerator keyGenerator = Mockito.mock(ICacheKeyGenerator.class);
    Mockito.when(
            keyGenerator.getKey(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString()))
        .thenReturn("test-key");

    adapter = new CoursesCalendarAdapter();
    adapter.setCourseDao(courseDao);
    adapter.setCacheKeyGenerator(keyGenerator);
    adapter.setCache(Mockito.mock(Cache.class)); // never hits, never throws
  }

  /**
   * The course calendar must be assembled with its required properties and one VEVENT per meeting,
   * and each event must carry its start, end and summary. In ical4j 4.x the lists returned by
   * {@code getProperties()}/{@code getComponents()} are unmodifiable and {@code
   * PropertyList.add()} returns a new list rather than mutating the receiver.
   */
  @Test
  public void testRetrieveCourseCalendarAssemblesEvents() {
    final Interval interval =
        new Interval(new DateTime(2026, 2, 28, 0, 0, TZ), new DateTime(2026, 4, 2, 0, 0, TZ));

    final Calendar calendar = adapter.retrieveCourseCalendar(request, interval, config, term);

    assertTrue(calendar.getProperty(Property.VERSION).isPresent());
    assertTrue(calendar.getProperty(Property.CALSCALE).isPresent());
    assertEquals(1, calendar.getComponents(Component.VEVENT).size());

    final VEvent event = (VEvent) calendar.getComponents(Component.VEVENT).get(0);
    assertNotNull("event has no DTSTART", event.getDateTimeStart());
    assertNotNull("event has no DTEND", event.getEndDate(true).orElse(null));
    assertEquals(
        "CS-101", event.getProperty(Property.SUMMARY).map(Property::getValue).orElse(null));
    assertTrue("event has no RRULE", event.getProperty(Property.RRULE).isPresent());
  }

  /**
   * A weekly course must keep meeting at the same local time. The course meets Mondays at 8:00 PM
   * Pacific; daylight saving time begins on 2026-03-08, so the recurrence must be computed in the
   * course's own zone rather than in UTC. The recurrence is expanded through the full adapter
   * (calendar -> content processor), so an event whose recurrence cannot be expanded shows up here
   * as missing instances.
   */
  @Test
  public void testWeeklyCourseKeepsLocalTimeAcrossDst() {
    final Interval interval =
        new Interval(new DateTime(2026, 2, 28, 0, 0, TZ), new DateTime(2026, 4, 2, 0, 0, TZ));

    final CalendarEventSet eventSet = adapter.getEvents(config, interval, request);

    // Mondays 3/2, 3/9, 3/16, 3/23, 3/30
    assertEquals(5, eventSet.getEvents().size());

    final List<ZonedDateTime> starts = new ArrayList<>();
    for (VEvent event : eventSet.getEvents()) {
      starts.add(toZoned(event.getDateTimeStart().getDate()));
    }
    Collections.sort(starts);

    for (ZonedDateTime start : starts) {
      assertEquals("day of week for " + start, DayOfWeek.MONDAY, start.getDayOfWeek());
      assertEquals("local time for " + start, java.time.LocalTime.of(20, 0), start.toLocalTime());
    }
  }

  private static ZonedDateTime toZoned(Temporal t) {
    if (t instanceof ZonedDateTime) {
      return ((ZonedDateTime) t).withZoneSameInstant(ZONE);
    }
    if (t instanceof Instant) {
      return ((Instant) t).atZone(ZONE);
    }
    return ((java.time.LocalDateTime) t).atZone(ZONE);
  }
}

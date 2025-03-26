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
package org.jasig.portlet.calendar.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import net.fortuna.ical4j.model.component.VEvent;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Test;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class JsonCalendarEventTest {

  @Test
  public void testStartDate() {
    DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");

    DateTimeFormatter df =
        new DateTimeFormatterBuilder()
            .appendDayOfWeekText()
            .appendLiteral(" ")
            .appendMonthOfYearText()
            .appendLiteral(" ")
            .appendDayOfMonth(1)
            .toFormatter()
            .withZone(tz)
            .withLocale(Locale.US);

    DateTimeFormatter tf =
        new DateTimeFormatterBuilder()
            .appendClockhourOfHalfday(1)
            .appendLiteral(":")
            .appendMinuteOfHour(2)
            .appendLiteral(" ")
            .appendHalfdayOfDayText()
            .toFormatter()
            .withZone(tz);

    DateTime date = new DateMidnight(2011, 1, 3, tz).toDateTime();

    DateTime start = new DateTime(2011, 1, 3, 4, 0, tz);
    DateTime end = start.plusDays(2);

    VEvent event =
        new VEvent(
            new net.fortuna.ical4j.model.DateTime(start.toDate()),
            new net.fortuna.ical4j.model.DateTime(end.toDate()),
            "Test Event");
    Interval eventInterval = new Interval(start, end);

    int calendarIndex=0;

    DateMidnight dateStart = new DateMidnight(date, tz);
    Interval day = new Interval(dateStart, dateStart.plusDays(1));
    CalendarDisplayEvent json = new CalendarDisplayEvent(event, eventInterval, day, df, tf, calendarIndex);
    assertEquals("4:00 AM", json.getDateStartTime());
    assertEquals("12:00 AM", json.getDateEndTime());
    assertEquals("Monday January 3", json.getStartDate());
    assertEquals("Wednesday January 5", json.getEndDate());
    assertEquals("4:00 AM", json.getStartTime());
    assertEquals("4:00 AM", json.getEndTime());
    assertTrue(json.isMultiDay());
    assertFalse(json.isAllDay());

    day = new Interval(dateStart.plusDays(1), dateStart.plusDays(2));
    json = new CalendarDisplayEvent(event, eventInterval, day, df, tf, calendarIndex);
    assertEquals("12:00 AM", json.getDateStartTime());
    assertEquals("12:00 AM", json.getDateEndTime());
    assertTrue(json.isMultiDay());
    assertTrue(json.isAllDay());

    day = new Interval(dateStart.plusDays(2), dateStart.plusDays(3));
    json = new CalendarDisplayEvent(event, eventInterval, day, df, tf, calendarIndex);
    assertEquals("12:00 AM", json.getDateStartTime());
    assertEquals("4:00 AM", json.getDateEndTime());
    assertTrue(json.isMultiDay());
    assertFalse(json.isAllDay());
  }
}

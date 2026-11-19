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

import java.time.Instant;
import java.util.Set;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Test;

public class CalDavCalendarAdapterTest {

  /**
   * The event instances produced from a CalDAV calendar must carry the start and end of the
   * recurrence period and the original event's other properties. In ical4j 4.x {@code
   * PropertyList.add()} returns a new list instead of mutating the receiver, so discarding the
   * result produces instances with no properties at all.
   */
  @Test
  public void testConvertCalendarToEventsCarriesProperties() {
    PropertyList props = new PropertyList();
    props = props.add(new Uid("caldav-1"));
    props = props.add(new Summary("Staff meeting"));
    props = props.add(new Location("Room 101"));
    props = props.add(new DtStart<>(Instant.parse("2026-03-02T15:00:00Z")));
    props = props.add(new DtEnd<>(Instant.parse("2026-03-02T16:00:00Z")));

    final Calendar calendar = new Calendar();
    calendar.add(new VEvent(props));

    final DateTimeZone tz = DateTimeZone.UTC;
    final Interval interval =
        new Interval(new DateTime(2026, 3, 1, 0, 0, tz), new DateTime(2026, 3, 8, 0, 0, tz));

    final Set<VEvent> events =
        new CalDavCalendarAdapter().convertCalendarToEvents(calendar, interval);

    assertEquals(1, events.size());
    final VEvent event = events.iterator().next();

    assertNotNull("instance has no DTSTART", event.getDateTimeStart());
    assertEquals(
        Instant.parse("2026-03-02T15:00:00Z"),
        Instant.from(event.getDateTimeStart().getDate()));
    assertEquals(
        Instant.parse("2026-03-02T16:00:00Z"),
        Instant.from(event.getEndDate(true).get().getDate()));
    // Non-date properties are copied from the source event
    assertEquals(
        "Staff meeting",
        event.getProperty(Property.SUMMARY).map(Property::getValue).orElse(null));
    assertEquals(
        "Room 101", event.getProperty(Property.LOCATION).map(Property::getValue).orElse(null));
  }
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.sun.syndication.feed.synd.SyndFeed;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Test;

public class RssContentProcessorTest {

  private static final String RSS =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<rss version=\"2.0\"><channel>"
          + "<title>Campus News</title><link>http://example.org/</link>"
          + "<description>News</description>"
          + "<item>"
          + "<title>In range</title>"
          + "<link>http://example.org/in-range</link>"
          + "<description>Inside the requested interval</description>"
          + "<guid>guid-in-range</guid>"
          + "<pubDate>Mon, 02 Mar 2026 15:00:00 GMT</pubDate>"
          + "</item>"
          + "<item>"
          + "<title>Out of range</title>"
          + "<link>http://example.org/out-of-range</link>"
          + "<description>Outside the requested interval</description>"
          + "<guid>guid-out-of-range</guid>"
          + "<pubDate>Mon, 02 Feb 2026 15:00:00 GMT</pubDate>"
          + "</item>"
          + "</channel></rss>";

  /**
   * The events built from an RSS feed must carry the entry's data. In ical4j 4.x {@code
   * PropertyList.add()} returns a new list instead of mutating the receiver, so discarding the
   * result produces events with no properties at all.
   */
  @Test
  public void testGetEventsCarriesEntryProperties() {
    final RssContentProcessorImpl processor = new RssContentProcessorImpl();
    final DateTimeZone tz = DateTimeZone.UTC;
    final Interval interval = new Interval(new DateTime(2026, 3, 1, 0, 0, tz), new DateTime(2026, 3, 8, 0, 0, tz));

    final SyndFeed feed =
        processor.getIntermediateCalendar(
            interval, new ByteArrayInputStream(RSS.getBytes(StandardCharsets.UTF_8)));
    assertNotNull(feed);

    final Set<VEvent> events = processor.getEvents(interval, feed);

    // Only the entry published inside the interval is returned
    assertEquals(1, events.size());
    final VEvent event = events.iterator().next();

    assertNotNull("event has no DTSTART", event.getDateTimeStart());
    assertEquals(
        Instant.parse("2026-03-02T15:00:00Z"),
        Instant.from(event.getDateTimeStart().getDate()));
    assertEquals(
        "In range", event.getProperty(Property.SUMMARY).map(Property::getValue).orElse(null));
    assertEquals(
        "Inside the requested interval",
        event.getProperty(Property.DESCRIPTION).map(Property::getValue).orElse(null));
    assertTrue(
        "event has no URL",
        event
            .getProperty(Property.URL)
            .map(p -> p.getValue().contains("example.org/in-range"))
            .orElse(false));
  }
}

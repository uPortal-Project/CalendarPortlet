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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeSet;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.io.IOUtils;
import org.jasig.portlet.calendar.VEventStartComparator;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/testContext.xml")
public class ICalendarContentProcessorTest {

  ICalendarContentProcessorImpl processor = new ICalendarContentProcessorImpl();

  @Autowired(required = true)
  ApplicationContext applicationContext;

  @Test
  public void test() throws IOException {

    Resource calendarFile = applicationContext.getResource("classpath:/sampleEvents.ics");

    DateMidnight start = new DateMidnight(2010, 1, 1, DateTimeZone.UTC);
    Interval interval = new Interval(start, start.plusYears(3));

    InputStream in = calendarFile.getInputStream();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    IOUtils.copyLarge(in, buffer);

    TreeSet<VEvent> events = new TreeSet<VEvent>(new VEventStartComparator());
    net.fortuna.ical4j.model.Calendar c =
        processor.getIntermediateCalendar(interval, new ByteArrayInputStream(buffer.toByteArray()));
    events.addAll(processor.getEvents(interval, c));

    assertEquals(5, events.size());

    Iterator<VEvent> iterator = events.iterator();
    VEvent event = iterator.next();
    assertEquals("Independence Day", event.getSummary().getValue());
    assertNull(event.getStartDate().getTimeZone());

    event = iterator.next();
    assertEquals("Vikings @ Saints  [NBC]", event.getSummary().getValue());
    DateTime eventStart = new DateTime(event.getStartDate().getDate(), DateTimeZone.UTC);
    assertEquals(0, eventStart.getHourOfDay());
    assertEquals(30, eventStart.getMinuteOfHour());

    event = iterator.next();
    assertEquals("Independence Day", event.getSummary().getValue());
    assertNull(event.getStartDate().getTimeZone());
  }
}

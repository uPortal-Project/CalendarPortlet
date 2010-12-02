/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.calendar.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeSet;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.apache.commons.io.IOUtils;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.VEventStartComparator;
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

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DATE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateTime start = new DateTime();
        start.setTime(cal.getTimeInMillis());
        
        cal.set(Calendar.YEAR, 2012);
        DateTime end = new DateTime(cal.getTime());

        Period period = new Period(start, end);

        InputStream in = calendarFile.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.copyLarge(in, buffer);
        
        TreeSet<CalendarEvent> events = new TreeSet<CalendarEvent>(new VEventStartComparator());
        events.addAll(processor.getEvents(Long.valueOf((long) 3), period, new ByteArrayInputStream(buffer.toByteArray())));
        
        assertEquals(3, events.size());
        
        Iterator<CalendarEvent> iterator = events.iterator();
        CalendarEvent event = iterator.next();
        assertEquals("Independence Day", event.getSummary().getValue());
        assertEquals(Long.valueOf((long) 3), event.getCalendarId());
        assertNull(event.getStartDate().getTimeZone());
        assertFalse(event.getStartDate().isUtc());
        
        event = iterator.next();
        assertEquals("Vikings @ Saints  [NBC]", event.getSummary().getValue());
        assertEquals(Long.valueOf((long) 3), event.getCalendarId());
        cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(event.getStartDate().getDate().getTime());
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertTrue(event.getStartDate().isUtc());
        
        event = iterator.next();
        assertEquals("Independence Day", event.getSummary().getValue());
        assertEquals(Long.valueOf((long) 3), event.getCalendarId());
        assertNull(event.getStartDate().getTimeZone());
        assertFalse(event.getStartDate().isUtc());
        
    }

}

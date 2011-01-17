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
package org.jasig.portlet.calendar.mvc.controller;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.mvc.JsonCalendarEvent;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class AjaxCalendarControllerTest {

	private AjaxCalendarController controller;
	
	@Before
	public void setUp() {
		controller = new AjaxCalendarController();
	}
	
	@Test
	public void testAddLongEventToDateMap() {
		TimeZone tz = TimeZone.getTimeZone("America/Los Angeles");
        DateFormat df = new SimpleDateFormat("EEEE MMMM d");
		df.setTimeZone(tz);
		
		Calendar cal = Calendar.getInstance(tz);
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 3);
		cal.set(Calendar.HOUR_OF_DAY, 4);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		
		cal.add(Calendar.DATE, 1);
		Date periodStart = cal.getTime();
		
		cal.add(Calendar.DATE, 1);
		Date end = cal.getTime();
		
		Period period = new Period(new DateTime(periodStart), new DateTime(end));
		
		CalendarEvent event = new CalendarEvent(new DateTime(start), new DateTime(end), "Test Event");
		List<JsonCalendarEvent> events = new ArrayList<JsonCalendarEvent>();
		events.addAll(controller.getJsonEvents(event, period, tz, 1));
		Collections.sort(events);
		assertEquals(2, events.size());
		
		assertEquals("Tuesday January 4", df.format(events.get(0).getDayStart()));
		assertEquals("Wednesday January 5", df.format(events.get(1).getDayStart()));
		
	}

	@Test
	public void testAddShortEventToDateMap() {
		TimeZone tz = TimeZone.getTimeZone("America/Los Angeles");
        DateFormat df = new SimpleDateFormat("EEEE MMMM d");
		df.setTimeZone(tz);
		
		Calendar cal = Calendar.getInstance(tz);
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 3);
		cal.set(Calendar.HOUR_OF_DAY, 4);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		
		cal.add(Calendar.HOUR, 12);
		Date end = cal.getTime();

		Period period = new Period(new DateTime(start), new DateTime(end));
	
		CalendarEvent event = new CalendarEvent(new DateTime(start), new DateTime(end), "Test Event");
		Set<JsonCalendarEvent> events = controller.getJsonEvents(event, period, tz, 1);
		assertEquals(1, events.size());

		Iterator<JsonCalendarEvent> dateIter = events.iterator();
		assertEquals("Monday January 3", df.format(dateIter.next().getDayStart()));
		
	}

}

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import net.fortuna.ical4j.model.DateTime;

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
		Map<Date, Set<JsonCalendarEvent>> dateMap = new TreeMap<Date, Set<JsonCalendarEvent>>();
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
		
		cal.add(Calendar.DATE, 2);
		Date end = cal.getTime();
		
		CalendarEvent event = new CalendarEvent(new DateTime(start), new DateTime(end), "Test Event");
		controller.addEventToDateMap(dateMap, event, tz, 1);
		assertEquals(3, dateMap.keySet().size());
		
		Iterator<Date> dateIter = dateMap.keySet().iterator();
		assertEquals("Monday January 3", df.format(dateIter.next()));
		assertEquals("Tuesday January 4", df.format(dateIter.next()));
		assertEquals("Wednesday January 5", df.format(dateIter.next()));
		
	}

	@Test
	public void testAddShortEventToDateMap() {
		Map<Date, Set<JsonCalendarEvent>> dateMap = new HashMap<Date, Set<JsonCalendarEvent>>();
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
		
		CalendarEvent event = new CalendarEvent(new DateTime(start), new DateTime(end), "Test Event");
		controller.addEventToDateMap(dateMap, event, tz, 1);
		assertEquals(1, dateMap.keySet().size());

		Iterator<Date> dateIter = dateMap.keySet().iterator();
		assertEquals("Monday January 3", df.format(dateIter.next()));
		
	}

}

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
package org.jasig.portlet.calendar.mvc;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;

import org.jasig.portlet.calendar.CalendarEvent;
import org.junit.Test;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class JsonCalendarEventTest {
	
	@Test
	public void testStartDate() {
		TimeZone tz = TimeZone.getTimeZone("America/Los Angeles");
		
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

		cal.set(Calendar.DATE, 3);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		Date date = cal.getTime();		
		JsonCalendarEvent json = new JsonCalendarEvent(event, date, tz, 1);
		assertEquals("4:00 AM", json.getDateStartTime());
		assertEquals("12:00 AM", json.getDateEndTime());
		
		cal.set(Calendar.DATE, 4);
		date = cal.getTime();		
		json = new JsonCalendarEvent(event, date, tz, 1);
		assertEquals("12:00 AM", json.getDateStartTime());
		assertEquals("12:00 AM", json.getDateEndTime());
		
		cal.set(Calendar.DATE, 5);
		date = cal.getTime();		
		json = new JsonCalendarEvent(event, date, tz, 1);
		assertEquals("12:00 AM", json.getDateStartTime());
		assertEquals("4:00 AM", json.getDateEndTime());
		
	}

}

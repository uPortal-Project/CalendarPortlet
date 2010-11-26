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

package org.jasig.portlet.calendar.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;

import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.util.AllDayUtil;
import org.junit.Test;

public class AllDayUtilTest {
	
	@Test
	public void testIsAllDayWesternEvent() throws ParseException {
		allDayEvent(TimeZone.getTimeZone("America/Los_Angeles"));
	}
	
	@Test
	public void testIsAllDayEasternEvent() throws ParseException {
		allDayEvent(TimeZone.getTimeZone("Europe/Moscow"));
	}
	
	public void allDayEvent(TimeZone timezone) throws ParseException {
		DateFormat df = new SimpleDateFormat("MM'/'dd'/'yyyy");
        
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("12/31/2009"));
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 1);
	    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
	    cal.add(Calendar.MILLISECOND, -timezone.getOffset(cal.getTimeInMillis()));

	    Date start = new DateTime(cal.getTime());
	    
	    cal.add(Calendar.DATE, 1);
	    Date end = new DateTime(cal.getTime());
	    
	    CalendarEvent event = new CalendarEvent(start, end, "test");
		
	    boolean isAllDay = AllDayUtil.isAllDayEvent(event, timezone);
	    assert isAllDay;
	}

}

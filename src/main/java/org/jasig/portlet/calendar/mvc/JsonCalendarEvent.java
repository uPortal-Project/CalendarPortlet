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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.util.AllDayUtil;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class JsonCalendarEvent implements Comparable<JsonCalendarEvent> {

	private final CalendarEvent event;
	private final Date dayStart;
	private final Date dayEnd;
	private final DateFormat tf;
	private final DateFormat df;
	private final boolean isAllDay;
	private final boolean isMultiDay;
	private final int colorIndex;
	
	public JsonCalendarEvent(CalendarEvent event, Date date, TimeZone tz, int colorIndex) {
		
		this.event = event;
		this.colorIndex = colorIndex;
		
		Calendar cal = Calendar.getInstance(tz);
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		boolean multi = false;
		if (event.getStartDate().getDate().before(cal.getTime())) {
			dayStart = cal.getTime();
			multi = true;
		} else {
			dayStart = event.getStartDate().getDate();
		}
		
		cal.add(Calendar.DATE, 1);
		
		if (event.getEndDate().getDate().after(cal.getTime())) {
			dayEnd = cal.getTime();
			multi = true;
		} else {
			dayEnd = event.getEndDate().getDate();
		}
		this.isMultiDay = multi;
		
		df = new SimpleDateFormat("EEEE MMMM d");
		df.setTimeZone(tz);
		
		tf = new SimpleDateFormat("h:mm a");
		tf.setTimeZone(tz);
		
		this.isAllDay = AllDayUtil.isAllDayEvent(event, tz);
		
	}
	
	public String getSummary() {
		if (this.event.getSummary() != null) {
			return this.event.getSummary().getValue();
		} else {
			return null;
		}
	}
	
	public String getDescription() {
		if (this.event.getDescription() != null) {
			return this.event.getDescription().getValue();
		} else return null;
	}
	
	public String getLocation() {
		if (this.event.getLocation() != null) {
			return this.event.getLocation().getValue();
		} else {
			return null;
		}
	}
	
	public Long getCalendarId() {
		return this.event.getCalendarId();
	}
	
	public String getDateStartTime() {
		return tf.format(this.dayStart);
	}
	
	public String getDateEndTime() {
		if (this.event.getEndDate() != null) {
			return tf.format(this.dayEnd);
		} else {
			return null;
		}
	}

	public String getStartTime() {
		return tf.format(this.event.getStartDate().getDate());
	}
	
	public String getEndTime() {
		if (this.event.getEndDate() != null) {
			return tf.format(this.event.getEndDate().getDate());
		} else {
			return null;
		}
	}

	public String getStartDate() {
		return df.format(this.event.getStartDate().getDate());
	}
	
	public String getEndDate() {
		if (this.event.getEndDate() != null) {
			return df.format(this.event.getEndDate().getDate());
		} else {
			return null;
		}
	}

	public boolean isAllDay() {
		return this.isAllDay;
	}
	
	public boolean isMultiDay() {
		return this.isMultiDay;
	}
	
	public int getColorIndex() {
		return this.colorIndex;
	}

	public int compareTo(JsonCalendarEvent event) {		
		return (new CompareToBuilder()).append(this.dayStart, event.dayStart)
				.append(this.dayEnd, event.dayEnd).toComparison();
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof JsonCalendarEvent)) {
			return false;
		}
		JsonCalendarEvent event = (JsonCalendarEvent) o;
		return (new EqualsBuilder()).append(this.dayStart, event.dayStart)
				.append(this.dayEnd, event.dayEnd).isEquals();
	}

}

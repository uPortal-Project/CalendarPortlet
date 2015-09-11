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
package org.jasig.portlet.calendar.util;

import java.text.ParseException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * @author Chris Waymire (chris@waymire.net)
 */
public class DateUtil {

	public static Interval getInterval(String startDate, int days,PortletRequest request) throws ParseException {
		final PortletSession session = request.getPortletSession();
		final String timezone = (String) session.getAttribute("timezone");
		final DateTimeZone tz = DateTimeZone.forID(timezone);
		final DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern("MMddyyyy").toFormatter().withZone(tz);
		final DateMidnight start = new DateMidnight(df.parseDateTime(startDate), tz);

		return getInterval(start,days);
	}

	public static Interval getInterval(DateMidnight start,int days) {
		Interval interval = new Interval(start, start.plusDays(days));
		return interval;

	}
	private DateUtil() { }
}

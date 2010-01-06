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
package org.jasig.portlet.calendar.url;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateFormatUtils;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarException;

import net.fortuna.ical4j.model.Period;

/**
 * {@link IUrlCreator} implementation specific for integrating
 * with CalendarKey (http://mywebspace.wisc.edu/npblair/calendarkey).
 * 
 * CalendarKey instances can expose a REST style web service for retrieving
 * individual user calendars.
 * These REST urls are built in the following way:
 * <ol>
 * <li>baseUrl, retrieved from the {@link CalendarConfiguration}, in a property named "baseUrl"</li>
 * <li>the current authenticated user's name (from PortletRequest.getRemoteUser() or HttpServletRequest.getRemoteUser())</li>
 * <li>The startDate and endDate, retrieved from the period</li>
 * </ol>
 * 
 * The following example url retrieves the calendar for user "netid" between Oct 9 2008 and Oct 10 2008:
<pre>
http://localhost:8080/calendarkey/ws/netid/20081009/20081010
</pre>
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: CalendarkeyUrlCreatorImpl.java Exp $
 */
public class CalendarkeyUrlCreatorImpl implements IUrlCreator {

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.servlet.http.HttpServletRequest, net.fortuna.ical4j.model.Period)
	 */
	public String constructUrl(CalendarConfiguration calendarListing,
			Period period, HttpServletRequest request) {
		String baseUrl = calendarListing.getCalendarDefinition().getParameters().get("baseUrl");
		StringBuilder finalUrl = new StringBuilder();
		finalUrl.append(baseUrl);
		if(!baseUrl.endsWith("/")) {
			finalUrl.append("/");
		}
		String username = request.getRemoteUser();
		if(null == username || "".equals(username)) {
			throw new CalendarException("user not logged in");
		}
		finalUrl.append(username);
		finalUrl.append("/");
		
		finalUrl.append(DateFormatUtils.format(period.getStart().getTime(), "yyyyMMdd"));
		finalUrl.append("/");
		finalUrl.append(DateFormatUtils.format(period.getEnd().getTime(), "yyyyMMdd"));
		return finalUrl.toString();
	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.portlet.PortletRequest, net.fortuna.ical4j.model.Period)
	 */
	public String constructUrl(CalendarConfiguration calendarListing,
			Period period, PortletRequest request) {
		String baseUrl = calendarListing.getCalendarDefinition().getParameters().get("baseUrl");
		StringBuilder finalUrl = new StringBuilder();
		finalUrl.append(baseUrl);
		if(!baseUrl.endsWith("/")) {
			finalUrl.append("/");
		}
		String username = request.getRemoteUser();
		if(null == username || "".equals(username)) {
			throw new CalendarException("user not logged in");
		}
		finalUrl.append(username);
		finalUrl.append("/");
		
		finalUrl.append(DateFormatUtils.format(period.getStart().getTime(), "yyyyMMdd"));
		finalUrl.append("/");
		finalUrl.append(DateFormatUtils.format(period.getEnd().getTime(), "yyyyMMdd"));
		return finalUrl.toString();
	}

}

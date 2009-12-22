/*******************************************************************************
* Copyright 2008, The Board of Regents of the University of Wisconsin System.
* All rights reserved.
*
* A non-exclusive worldwide royalty-free license is granted for this Software.
* Permission to use, copy, modify, and distribute this Software and its
* documentation, with or without modification, for any purpose is granted
* provided that such redistribution and use in source and binary forms, with or
* without modification meets the following conditions:
*
* 1. Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
* acknowledgement:
*
* "This product includes software developed by The Board of Regents of
* the University of Wisconsin System.
*
*THIS SOFTWARE IS PROVIDED BY THE BOARD OF REGENTS OF THE UNIVERSITY OF
*WISCONSIN SYSTEM "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
*BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
*PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE BOARD OF REGENTS OF
*THE UNIVERSITY OF WISCONSIN SYSTEM BE LIABLE FOR ANY DIRECT, INDIRECT,
*INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
*LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
*OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
*ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
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

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

import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarException;


/**
 * The default implementation for {@link IUrlCreator}; returns simply
 * the parameter named "url" from the {@link CalendarConfiguration}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: DefaultUrlCreatorImpl.java Exp $
 */
public class DefaultUrlCreatorImpl implements IUrlCreator {

	protected final Log log = LogFactory.getLog(this.getClass());
	
	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.servlet.http.HttpServletRequest, net.fortuna.ical4j.model.Period)
	 */
	public String constructUrl(CalendarConfiguration calendarListing,
			Period period, HttpServletRequest request) {
		return constructUrlInternal(calendarListing);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.portlet.PortletRequest, net.fortuna.ical4j.model.Period)
	 */
	public String constructUrl(CalendarConfiguration calendarListing,
			Period period, PortletRequest request) {
		return constructUrlInternal(calendarListing);
	}
	
	/**
	 * DefaultUrlCreatorImpl only needs to examine the CalendarConfiguration
	 * to retrive the url.
	 * 
	 * @param calendarListing
	 * @return
	 */
	protected String constructUrlInternal(CalendarConfiguration calendarListing) {
		String url = (String) calendarListing.getCalendarDefinition()
		.getParameters().get("url");
		if (url == null) {
			log.error("configuration with ID "
					+ calendarListing.getCalendarDefinition().getId()
					+ " has no URL parameter");
			throw new CalendarException("Calendar is not configured correctly");
		}
		return url;
	}

}

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

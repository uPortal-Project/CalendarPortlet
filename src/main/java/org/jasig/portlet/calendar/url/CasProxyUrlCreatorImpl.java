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
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.jasig.portlet.cas.CASProxyTicketServiceUserInfoImpl;
import org.jasig.portlet.cas.ICASProxyTicketService;

import edu.yale.its.tp.cas.client.CASReceipt;

/**
 * This {@link IUrlCreator} implementation requires injection
 * of a CAS {@link CASProxyTicketServiceUserInfoImpl}.
 * It retrieves a url from the {@link CalendarConfiguration}, in a
 * parameter named "url".
 * 
 * The CAS {@link CASProxyTicketServiceUserInfoImpl} is used to retrieve a proxy ticket for the currently
 * authenticated user, which is appended to the to the value of the
 * "url" parameter as an attribute named "ticket".
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: CasUrlCreatorImpl.java Exp $
 */
public class CasProxyUrlCreatorImpl implements IUrlCreator {

	private Log log = LogFactory.getLog(this.getClass());

	private ICASProxyTicketService proxyTicketService;

	/**
	 * 
	 * @param proxyTicketService
	 */
	public void setProxyTicketService(ICASProxyTicketService proxyTicketService) {
		this.proxyTicketService = proxyTicketService;
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.servlet.http.HttpServletRequest, net.fortuna.ical4j.model.Period)
	 */
	public String constructUrl(CalendarConfiguration calendarListing,
			Period period, HttpServletRequest request) {
		String configuredUrl = calendarListing.getCalendarDefinition()
		.getParameters().get("url");

		// get the session
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.warn("CasifiedICalFeed requested with a null session");
			throw new CalendarException();
		}

		// retrieve the CAS receipt for the current user's session
		CASReceipt receipt = (CASReceipt) session.getAttribute("CasReceipt");
		if (receipt == null) {
			log.warn("CasifiedICalFeed cannot find a CAS receipt object");
			throw new CalendarException();
		}

		String proxyTicket = proxyTicketService.getCasServiceToken(receipt,
				configuredUrl);

		StringBuilder finalUrl = new StringBuilder();
		finalUrl.append(configuredUrl);

		if (proxyTicket != null) {
			String separator = configuredUrl.contains("?") ? "&" : "?";
			finalUrl.append(separator);
			finalUrl.append("ticket=");
			finalUrl.append(proxyTicket);
		} else {
			log.warn("No CAS ticket could be obtained for " + configuredUrl
					+ ".  Returning empty event list.");
			throw new CalendarException();
		}
		return finalUrl.toString();
	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.portlet.PortletRequest, net.fortuna.ical4j.model.Period)
	 */
	public String constructUrl(CalendarConfiguration calendarListing,
			Period period, PortletRequest request) {
		String configuredUrl = calendarListing.getCalendarDefinition()
		.getParameters().get("url");

		// get the session
		PortletSession session = request.getPortletSession(false);
		if (session == null) {
			log.warn("CasifiedICalFeed requested with a null session");
			throw new CalendarException();
		}

		// retrieve the CAS receipt for the current user's session
		CASReceipt receipt = (CASReceipt) session.getAttribute("CasReceipt");
		if (receipt == null) {
			log.warn("CasifiedICalFeed cannot find a CAS receipt object");
			throw new CalendarException();
		}

		String proxyTicket = proxyTicketService.getCasServiceToken(receipt,
				configuredUrl);
		StringBuilder finalUrl = new StringBuilder();
		finalUrl.append(configuredUrl);
		if (proxyTicket != null) {
			String separator = configuredUrl.contains("?") ? "&" : "?";
			finalUrl.append(separator);
			finalUrl.append("ticket=");
			finalUrl.append(proxyTicket);
		} else {
			log.warn("No CAS ticket could be obtained for " + configuredUrl
					+ ".  Returning empty event list.");
			throw new CalendarException();
		}
		return finalUrl.toString();
	}

}

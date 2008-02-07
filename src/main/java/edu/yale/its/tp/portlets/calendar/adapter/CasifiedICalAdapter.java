/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.adapter;

import java.util.Set;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import net.fortuna.ical4j.model.Period;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.portlet.ProxyTicketService;
import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;

/**
 * CasifiedICalFeed is a CalendarAdapter for proxy-CAS-protected ICalendar .ics
 * feeds.
 * 
 * @author Jen Bourey
 */
public class CasifiedICalAdapter extends HttpICalAdapter {

	private static Log log = LogFactory.getLog(CasifiedICalAdapter.class);

	@Override
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarListing,
			Period period, PortletRequest request) throws CalendarException {

		// get the iCal feed's url from the calendar definition parameter list
		String url = calendarListing.getCalendarDefinition().getParameters()
				.get("url");

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
		
		net.fortuna.ical4j.model.Calendar calendar = null;

		// attempt to retrieve the calendar from the cache
		String key = getCacheKey(url, receipt.getUserName());
		Element cachedElement = cache.get(key);
		if (cachedElement != null) {
			calendar = (net.fortuna.ical4j.model.Calendar) cachedElement
					.getValue();
		} else {
			// get a proxy ticket for the feed's url and append it to the url
			String proxyTicket = proxyTicketService.getCasServiceToken(receipt,
					url);
			if (proxyTicket != null) {
				url = url.concat("?ticket=").concat(proxyTicket);

				// retrieve the calendar
				calendar = getCalendar(url);

				// add the calendar to the cache
				cachedElement = new Element(key, calendar);
				cache.put(cachedElement);

			} else {
				log.warn("No CAS ticket could be obtained for " + url
						+ ".  Returning empty event list.");
				throw new CalendarException();
			}
		}

		// extract events from the calendar
		return getEvents(calendarListing.getId(), calendar, period);

	}

	/**
	 * Get a cache key for this calendar request.
	 * 
	 * @param url URL of this calendar
	 * @param netid login id of the requesting user
	 * @return String representing this request
	 */
	private String getCacheKey(String url, String netid) {
		StringBuffer key = new StringBuffer();
		key.append("CasifiedICalFeed.");
		key.append("netid:");
		key.append(netid);
		key.append(".");
		key.append(url);
		return key.toString();
	}

	private ProxyTicketService proxyTicketService;

	public void setProxyTicketService(ProxyTicketService proxyTicketService) {
		this.proxyTicketService = proxyTicketService;
	}

	private Cache cache;

	public void setCache(Cache cache) {
		this.cache = cache;
	}

}


/*
 * CasifiedICalAdapter.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
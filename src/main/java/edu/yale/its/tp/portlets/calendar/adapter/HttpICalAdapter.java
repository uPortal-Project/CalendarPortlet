/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.RRule;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;

/**
 * ICalFeed is a CalendarAdapter for standard ICalendar .ics feeds available
 * online via http or https.
 * 
 * Note: This class isn't designed to access calendars via the webcal:// protocol.
 * 
 * @author Jen Bourey
 */
public class HttpICalAdapter implements ICalendarAdapter {

	private static Log log = LogFactory.getLog(HttpICalAdapter.class);

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.CalendarAdapter#getEvents(edu.yale.its.tp.portlets.calendar.CalendarDefinition, net.fortuna.ical4j.model.Period, java.util.Map)
	 */
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarListing, Period period, PortletRequest request) throws CalendarException { 

		String url = (String) calendarListing.getCalendarDefinition().getParameters().get("url");
		net.fortuna.ical4j.model.Calendar calendar = null;
		
		// try to get the cached calendar
		String key = getCacheKey(url);
		Element cachedElement = cache.get(key);
		if (cachedElement == null) {
			calendar = getCalendar(url);
			cachedElement = new Element(key, calendar);
			cache.put(cachedElement);
		} else
			calendar = (net.fortuna.ical4j.model.Calendar) cachedElement.getValue();
		
		// return the event list
		return getEvents(calendarListing.getId(), calendar, period);
	}
	
	/**
	 * Retrieve the entire .ics file at a specified URL and use it to
	 * build an iCal4j Calendar object.
	 * 
	 * @param url
	 * @return
	 */
	protected net.fortuna.ical4j.model.Calendar getCalendar(String url) throws CalendarException {

		CalendarBuilder builder = new CalendarBuilder();
		HttpClient client = new HttpClient();
		GetMethod get = null;

		try {

			if (log.isDebugEnabled())
				log.debug("Retrieving calendar " + url);
			get = new GetMethod(url);
			int rc = client.executeMethod(get);
			if (rc != HttpStatus.SC_OK) {
				log.warn("HttpStatus for " + url + ":" + rc);
			}

			// retrieve and parse the iCal document
			InputStream in = get.getResponseBodyAsStream();
			net.fortuna.ical4j.model.Calendar calendar = builder.build(in);
			return calendar;

		} catch (HttpException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed");
		} catch (IOException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed");
		} catch (ParserException e) {
			log.warn("Error parsing iCalendar feed: " + e.getMessage());
			throw new CalendarException("Error parsing iCalendar feed");
		} finally {
			log.debug("cleaning up");
			if (get != null)
				get.releaseConnection();
		}
		
	}
	
	/**
	 * Extract a list of events from an iCal4j Calendar for a specified
	 * time period.
	 * 
	 * @param calendarId
	 * @param calendar
	 * @param period
	 * @return
	 */
	protected Set<CalendarEvent> getEvents(Long calendarId, net.fortuna.ical4j.model.Calendar calendar, Period period) throws CalendarException {
		
		Set<CalendarEvent> events = new HashSet<CalendarEvent>();
		
		// if the calendar is null, throw an error
		if (calendar == null)
			throw new CalendarException();

		// retrieve the list of events for this calendar within the
		// specified time period
		for (Iterator<Component> i = calendar.getComponents().iterator(); i.hasNext();) {
			Component component = i.next();
			if (component.getName().equals("VEVENT")) {
				VEvent event = (VEvent) component;

				// calculate the recurrence set for this event
				// for the specified time period
				PeriodList periods = event.calculateRecurrenceSet(period);

				// add each recurrence instance to the event list
				for (Iterator<Period> iter = periods.iterator(); iter.hasNext();) {
					Period eventper = iter.next();

					PropertyList props = event.getProperties();

					// create a new property list, setting the date
					// information to this event period
					PropertyList newprops = new PropertyList();
					newprops.add(new DtStart(eventper.getStart()));
					newprops.add(new DtEnd(eventper.getEnd()));
					for (Iterator<Property> iter2 = props.iterator(); iter2.hasNext();) {
						Property prop = iter2.next();
						
						// only add non-date-related properties
						if (!(prop instanceof DtStart)
								&& !(prop instanceof DtEnd)
								&& !(prop instanceof Duration)
								&& !(prop instanceof RRule))
							newprops.add(prop);
					}

					// create the new event from our property list
					CalendarEvent newevent = new CalendarEvent(calendarId, newprops);
					events.add(newevent);
				}

			}
		}

		return events;
		
	}
	
	private String getCacheKey(String url) {
		StringBuffer key = new StringBuffer();
		key.append("ICalFeed.");
		key.append(url);
		return key.toString();
	}

	private Cache cache;
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
}


/*
 * HttpICalAdapter.java
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
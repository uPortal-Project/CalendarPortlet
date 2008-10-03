/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) The University of Manchester, Feb 13, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.adapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Version;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import oracle.calendar.soap.client.CalendarUtils;
import oracle.calendar.soap.client.CalendaringResponse;
import oracle.calendar.soap.client.Calendarlet;
import oracle.calendar.soap.client.Reply;
import oracle.calendar.soap.client.SearchCommand;
import oracle.calendar.soap.client.authentication.BasicAuth;
import oracle.calendar.soap.client.query.vQuery;
import oracle.calendar.soap.iCal.iCalendar;
import oracle.calendar.soap.iCal.vCalendar;
import oracle.calendar.soap.iCal.vEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;

/**
 * OracleICalAdapter is a CalendarAdapter for Oracle Calendar.
 * This adapter uses the Calendar Web Services Toolkit, See Chapter 9 of Oracle Calendar 
 * Application Developer's Guide (http://download-uk.oracle.com/docs/cd/B15595_01/calendar.101/b14477/adws_overview.htm)
 * 
 * Note: This class uses the BasicAuth method of logging in. Therefore the users credentials must be available from the Portal
 * and mapped via portlet.xml
 * 
 * @author Anthony Colebourne
 */
public class OracleICalAdapter implements ICalendarAdapter {

	private static Log log = LogFactory.getLog(OracleICalAdapter.class);
	
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,Period period, HttpServletRequest request) throws CalendarException {
		// get the session
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.warn("OracleICalAdapter requested with a null session");
			throw new CalendarException();
		}
		
		String username = (String) session.getAttribute("subscribeId");
		if (username == null) {
			log.error("OracleICalAdapter cannot find the subscribeId");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute("password");
		if (password == null) {
			log.error("OracleICalAdapter cannot find the users password, try configuring the CachedCredentialsInitializationService");
			throw new CalendarException();
		}
		
		return getEvents(calendar,period,username,password);
	}

	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar, Period period, PortletRequest request) throws CalendarException {
		// get the session
		PortletSession session = request.getPortletSession(false);
		if (session == null) {
			log.warn("OracleICalAdapter requested with a null session");
			throw new CalendarException();
		}
		
		String username = (String) session.getAttribute("subscribeId");
		if (username == null) {
			log.error("OracleICalAdapter cannot find the subscribeId");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute("password");
		if (password == null) {
			log.error("OracleICalAdapter cannot find the users password, try configuring the CachedCredentialsInitializationService");
			throw new CalendarException();
		}
		
		return getEvents(calendar,period,username,password);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.CalendarAdapter#getEvents(edu.yale.its.tp.portlets.calendar.CalendarDefinition, net.fortuna.ical4j.model.Period, java.util.Map)
	 */
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarConfig, Period period, String username, String password) throws CalendarException {
		
		net.fortuna.ical4j.model.Calendar calendar = null;

		// get the URL for this calendar
		String url = (String) calendarConfig.getCalendarDefinition().getParameters().get("url");

		// try to get the cached calendar
		String key = getCacheKey(url,username, period);
		Element cachedElement = cache.get(key);
		
		if(cachedElement == null) {
			// read in the calendar
			calendar = getCalendar(url,username,password,period);
			
			// save the calendar to the cache
			cachedElement = new Element(key, calendar);
			cache.put(cachedElement);

		} else {
			calendar = (net.fortuna.ical4j.model.Calendar) cachedElement.getValue();
		}
		
		// return the event list
		return getEvents(calendarConfig.getId(), calendar, period);
	}
	
		
	/**
	 * Retrieve calendar data from Oracle and use it to
	 * build an iCal4j Calendar object.
	 * 
	 * @param url URL of the calendar to be retrieved
	 * @param username Username to use to login to Oracle Calendar using Oracle's BasicAuth
	 * @param password Password to use to login to Oracle Calendar using Oracle's BasicAuth
	 * @return ical4j Calendar object
	 */
	protected net.fortuna.ical4j.model.Calendar getCalendar(String url, String username, String password, Period period) throws CalendarException {
		// initialize the authentication information and set the user id
		BasicAuth auth = new BasicAuth();

		auth.setName(username);
		auth.setPassword(password);

		// initialize the event search command and query
		SearchCommand search = new SearchCommand();
		search.setCmdId("uPortal");

		// create a query to retrieve unconfirmed events
		vQuery query = new vQuery();
		query.setFrom(vQuery.k_queryFromEvent);
		// query.setFrom(vQuery.k_queryFromTodo);

		//java.util.Calendar today = CalendarUtils.getToday(); 
		//java.util.Calendar begin = (java.util.Calendar) today.clone(); 
		
		Calendar beginPeriod = Calendar.getInstance();
		beginPeriod.setTime(period.getStart());
		beginPeriod.set(Calendar.HOUR_OF_DAY, 0);
		beginPeriod.set(Calendar.MINUTE, 0);
		beginPeriod.set(Calendar.SECOND, 0);
		beginPeriod.set(Calendar.MILLISECOND, 0);
		
		Calendar endPeriod = Calendar.getInstance();
		endPeriod.setTime(period.getEnd());
		endPeriod.set(Calendar.HOUR_OF_DAY, 0);
		endPeriod.set(Calendar.MINUTE, 0);
		endPeriod.set(Calendar.SECOND, 0);
		endPeriod.set(Calendar.MILLISECOND, 0);
	
		query.setWhere(CalendarUtils.getDateRangeQuery(beginPeriod, endPeriod));
		
		search.setQuery(query);

		// create the calendar client SOAP stub
		// and set the basic authentication header
		Calendarlet cws = new Calendarlet();

		// set the Web Services host URL
		cws.setEndPointURL(url);
		cws.setAuthenticationHeader(auth.getElement());
		cws.setWantIOBuffers(log.isDebugEnabled());			
		cws.setWantIOBuffers(true);

		// make the SOAP call
		try {
			CalendaringResponse response = cws.Search(search.getElement());
		
			if(log.isDebugEnabled()) {
				log.debug(response.getSendBuffer());
				log.debug("\n-----------------------------");
				log.debug(response.getReceiveBuffer());
			}

			// get the SOAP reply
			Reply reply = (Reply) response.getCalendarReply();
			if(reply == null) {
				throw new CalendarException("Response could not be parsed");
			}


			net.fortuna.ical4j.model.Calendar iCal4j = new net.fortuna.ical4j.model.Calendar();
			iCal4j.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
			iCal4j.getProperties().add(Version.VERSION_2_0);
			iCal4j.getProperties().add(CalScale.GREGORIAN);
			
			
			// traverse all the iCalendar objects
			Vector entries = reply.getEntries();
			Vector someiCalendars = iCalendar.unmarshallVector(entries);
			int numiCalendars = someiCalendars.size();
			log.debug(numiCalendars + " iCalendar entries found.");
			for (int i = 0; i < numiCalendars; i++) {
				iCalendar iCalObj = (iCalendar) someiCalendars.get(i);
				Vector somevCalendars = iCalObj.getvCalendars();
				int numvCalendars = somevCalendars.size();
				log.debug(numvCalendars+" vCalendar entries found.");
				for(int ii = 0; ii < numvCalendars; ii++) {
					vCalendar vCalObj = (vCalendar) somevCalendars.get(ii);
					Vector somevEvents = vCalObj.getComponents();
					int numvEvents = somevEvents.size();
					log.debug(numvEvents+" vEvents entries found.");

					for (int iii = 0; iii < numvEvents; iii++) {
						try {
							vEvent vEventObj = (vEvent) somevEvents.get(iii);
							log.debug("vEvents is "+vEventObj.getSummary());
							log.debug("vEvents DT start "+vEventObj.getDtStart());
							log.debug("vEvents DT end "+vEventObj.getDtEnd());
						
							if(vEventObj.getXEventType().equals(vEvent.k_eventTypeDayEvent)) {
								DateFormat parser = new SimpleDateFormat("yyyyMMdd");
								Calendar endEvent = Calendar.getInstance();
								endEvent.setTime(parser.parse(vEventObj.getDtStart()));
								endEvent.roll(Calendar.DATE, true);												
							
								VEvent iCal4jEvent = new VEvent(new Date(vEventObj.getDtStart()),new Date(endEvent.getTime()),vEventObj.getSummary());
								iCal4jEvent.getProperties().add(new Location(vEventObj.getLocation()));
								iCal4j.getComponents().add(iCal4jEvent);
							}
							else {
								VEvent iCal4jEvent = new VEvent(new DateTime(vEventObj.getDtStart()),new DateTime(vEventObj.getDtEnd()),vEventObj.getSummary());
								iCal4jEvent.getProperties().add(new Location(vEventObj.getLocation()));
								iCal4j.getComponents().add(iCal4jEvent);	
							}
						}
						catch (ParseException parseException) {
							log.error("Problem parsing DateTime");
						}
					}
				}
			}
			return iCal4j;
		}
		catch (Exception ex) {
			log.error("SOAP call failed",ex);
			throw new CalendarException("SOAP call failed");
		}
	}
	
	/**
	 * Extract a list of events from an iCal4j Calendar for a specified
	 * time period.
	 * 
	 * @param calendarId id of the CalendarConfiguration
	 * @param calendar ical4j calendar object
	 * @param period time period to retrieve events for
	 * @return Set of calendar events
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
	
	
	/**
	 * Get a cache key for this calendar request.
	 * 
	 * @param url URL of this calendar
	 * @param netid login id of the requesting user
	 * @return String representing this request
	 */
	private String getCacheKey(String url, String username, Period period) {
		StringBuffer key = new StringBuffer();
		// Unique to this class
		key.append("OracleiCal.");
		// Unique to the back end data source identified by url
		key.append(url);
		key.append(".");
		// Unique to this user identified by username
		key.append("username:");
		key.append(username);
		key.append(".");
		// Unique to the time period identified hash code
		key.append("period:");
		key.append(period.hashCode());
		return key.toString();
	}

	private Cache cache;
	public void setCache(Cache cache) {
		this.cache = cache;
	}

}

/*
 * OracleICalAdapter.java
 * 
 * Copyright (c) Feb 13, 2008 The University of Manchester. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * MANCHESTER UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 * software developed by The University of Manchester," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "The University of Manchester" and "Manchester University" must not be used to endorse or
 * promote products derived from this software.
 */

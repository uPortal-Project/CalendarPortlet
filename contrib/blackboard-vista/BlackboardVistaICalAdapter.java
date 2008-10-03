/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) The University of Manchester, Feb 13, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.adapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Version;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webct.platform.sdk.calendar.CalendarEntryVO;
import com.webct.platform.sdk.calendar.client.CalendarSDK;
import com.webct.platform.sdk.context.client.ContextSDK;
import com.webct.platform.sdk.context.exceptions.ContextException;
import com.webct.platform.sdk.context.gen.SessionVO;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;
import edu.yale.its.tp.portlets.calendar.adapter.CalendarException;

/**
 * BlackboardVistaICalAdapter is a CalendarAdapter for Blackboard Vista Learning System's Calendar.
 * This adapter uses the SOAP SDK, http://www.edugarage.com/display/BBDN/downloads
 * 
 * Note: This class uses the ContextSDK.login(String username, String password, String glcid) method of logging in. Subclasses
 * should override the doLogin(CalendarConfiguration calendarConfig, PortletRequest request) to provide a users session via a
 * DeployableComponent login.
 * 
 * @author Anthony Colebourne
 */
public class BlackboardVistaICalAdapter implements ICalendarAdapter {
	private static Log log = LogFactory.getLog(BlackboardVistaICalAdapter.class);
	

	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,Period period, HttpServletRequest request) throws CalendarException {
		// get the session
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.error("BlackboardVistaICalAdapter requested with a null session");
			throw new CalendarException();
		}

		String username = (String) session.getAttribute("subscribeId");
		if (username == null) {
			log.error("BlackboardVistaICalAdapter cannot find the subscribeId");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute("password");
		if (password == null) {
			log.error("BlackboardVistaICalAdapter cannot find the users password, try configuring the CachedCredentialsInitializationService");
			throw new CalendarException();
		}
		
		return getEvents(calendar,period,username,password);
	}

	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar, Period period, PortletRequest request) throws CalendarException {
		PortletSession session = request.getPortletSession(false);
		if (session == null) {
			log.warn("BlackboardVistaICalAdapter requested with a null session");
			throw new CalendarException();
		}
		
		String username = (String) session.getAttribute("subscribeId");
		if (username == null) {
			log.error("BlackboardVistaICalAdapter cannot find the subscribeId");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute("password");
		if (password == null) {
			log.error("BlackboardVistaICalAdapter cannot find the users password, try configuring the CachedCredentialsInitializationService");
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

		String url = (String) calendarConfig.getCalendarDefinition().getParameters().get("url");
		
		// try to get the cached calendar
		String key = getCacheKey(url,username, period);
		Element cachedElement = cache.get(key);
		
		if(cachedElement == null) {
			// read in the calendar
			SessionVO session = doLogin(calendarConfig,username,password);
			calendar = getCalendar(url,session,period);
			
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
	 * Login to Blackboard and return the session.
	 * 
	 * @param calendar calendar configuration for which to retrieve events
	 * @param request user's portlet request
	 * @return Session for the Blackboard user
	 * @throws CalendarException
	 */
	protected SessionVO doLogin(CalendarConfiguration calendarConfig, String username, String password) throws CalendarException {
		ContextSDK ctxt = null;
		SessionVO session = null;
		
		// get the URL for this calendar
		String url = (String) calendarConfig.getCalendarDefinition().getParameters().get("url");
		String glcid = (String) calendarConfig.getCalendarDefinition().getParameters().get("glcid");
		
		try {
			ctxt = new ContextSDK( new URL( url+"Context" ) );
			session = ctxt.login(username, password, glcid);
			return session;
		}
		catch (MalformedURLException mue) {
			throw new CalendarException("Problem with Context URL");
		}
		catch (RemoteException re) {
			throw new CalendarException("Problem logging in to Blackboard");
		}
		finally {
			if(ctxt != null) {
				log.debug("Logging out");
				//release resources associated with the session
				//logout does not delete session
				try {
					ctxt.logout(session) ;
				}
				catch (Exception exception) {}
			}
	 	}
	}
	
	/**
	 * Retrieve calendar data from Blackboard and use it to
	 * build an iCal4j Calendar object.
	 * 
	 * @param url URL of the calendar to be retrieved
	 * @param session A valid Blackboard session for the user
	 * @param period period time period to retrieve events for
	 * @return ical4j Calendar object
	 */
	protected net.fortuna.ical4j.model.Calendar getCalendar(String soapSvrUrl,SessionVO session, Period period) throws CalendarException {
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
	
		try {
			net.fortuna.ical4j.model.Calendar iCal4j = new net.fortuna.ical4j.model.Calendar();
			iCal4j.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
			iCal4j.getProperties().add(Version.VERSION_2_0);
			iCal4j.getProperties().add(CalScale.GREGORIAN);
	 	
			// Display the PersonId from Context SDK.
			long personID = session.getSubject().getPersonID();
			
			// Get the  Calendar SDK Service based on the passed in URL
			CalendarSDK calSvc = new CalendarSDK( new URL( soapSvrUrl+"Calendar" ) );
	
			// Get the Calendar entries for the person.
			log.info("Get Calendar entries for personID = " + personID);
			CalendarEntryVO[] calEntries = calSvc.getEntriesForUser(session,personID);
			if (calEntries != null && calEntries.length != 0) {
				for (int i = 0; i < calEntries.length; i++) {
					log.debug("id = "+calEntries[i].getEntryDetailId());
					log.debug("Summary: " + calEntries[i].getSummary());
					log.debug("Start: "+calEntries[i].getStartDateTime().getTime());
					log.debug("End: "+calEntries[i].getEndDateTime().getTime());
					log.debug("Details: "+calEntries[i].getDetail());
					
					if(calEntries[i].isAllDayEventFlag()) {
						log.debug("isAllDay");
											
						Calendar endEvent = (Calendar) calEntries[i].getStartDateTime().clone();
						endEvent.roll(Calendar.DATE, true);				
					
						VEvent iCal4jEvent = new VEvent(new Date(calEntries[i].getStartDateTime().getTime()),new Date(endEvent.getTime()),calEntries[i].getSummary());
						
						iCal4j.getComponents().add(iCal4jEvent);
					}
					else {					
						DateTime start = new DateTime(calEntries[i].getStartDateTime().getTime());
						DateTime end = new DateTime(calEntries[i].getStartDateTime().getTime());
						VEvent iCal4jEvent = new VEvent(start,end,calEntries[i].getSummary());
						iCal4j.getComponents().add(iCal4jEvent);
					}
				}
			} else {
				log.info("No calendar entries found for this user");
			}
			log.debug(iCal4j.toString());
			return iCal4j;
		}
		catch (MalformedURLException mue) {
			throw new CalendarException("Problem with Calendar URL");
		}
		catch (ContextException cx) {
			throw new CalendarException("Problem connecting to calendar");
		}
		catch (RemoteException re) {
			throw new CalendarException("Problem getting data from calendar");
		}
		catch (com.webct.platform.sdk.calendar.exceptions.CalendarException ce) {
			throw new CalendarException("Problem with data from calendar");
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
		key.append("BlackboardICal.");
		// Unique to the back end data source identified by url
		key.append(url);
		key.append(".");
		// Unique to this user identified by username
		key.append("username:");
		key.append(username);
		key.append(".");
		// Unique to the time period identified by hash code
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
 * BlackboardVistaICalAdapter.java
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

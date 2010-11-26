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

package org.jasig.portlet.calendar.adapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.credentials.DefaultCredentialsExtractorImpl;
import org.jasig.portlet.calendar.credentials.ICredentialsExtractor;
import org.jasig.portlet.calendar.url.DefaultUrlCreatorImpl;
import org.jasig.portlet.calendar.url.IUrlCreator;
import org.osaf.caldav4j.CalDAV4JException;
import org.osaf.caldav4j.CalDAVCalendarCollection;
import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;
import org.osaf.caldav4j.methods.HttpClient;


/**
 * Implementation of {@link ICalendarAdapter} that uses CalDAV
 * for retrieving {@link CalendarEvent}s.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: CalDavCalendarAdapter.java Exp $
 */
public class CalDavCalendarAdapter implements ICalendarAdapter {

	protected final Log log = LogFactory.getLog(this.getClass());

	private Cache cache;
	private IUrlCreator urlCreator = new DefaultUrlCreatorImpl();
	private ICredentialsExtractor credentialsExtractor = new DefaultCredentialsExtractorImpl();
	private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
	private String cacheKeyPrefix = "default";
	
	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public void setUrlCreator(IUrlCreator urlCreator) {
		this.urlCreator = urlCreator;
	}

	public void setCredentialsExtractor(ICredentialsExtractor credentialsExtractor) {
		this.credentialsExtractor = credentialsExtractor;
	}

	public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
		this.cacheKeyGenerator = cacheKeyGenerator;
	}

	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}

	public Set<CalendarEvent> getEvents(
			CalendarConfiguration calendarConfiguration, Period period,
			PortletRequest request) throws CalendarException {
		Set<CalendarEvent> events = new HashSet<CalendarEvent>();

		String url = this.urlCreator.constructUrl(calendarConfiguration, period, request);
		
		log.debug("generated url: " + url);
		
		// try to get the cached calendar
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(url));
		Element cachedElement = this.cache.get(key);
		if (cachedElement == null) {
			// read in the data
			// retrieve calendars for the current user
			net.fortuna.ical4j.model.Calendar calendar = retrieveCalendar(
					url, period, credentialsExtractor.getCredentials(request));

			// extract events from the calendars
				events.addAll(convertCalendarToEvents(
						calendarConfiguration.getId(), calendar, period));
			log.debug("contentProcessor found " + events.size() + " events");
			// save the CalendarEvents to the cache
			cachedElement = new Element(key, events);
			this.cache.put(cachedElement);
		} else {
			events = (Set<CalendarEvent>) cachedElement.getValue();
		}
		
		return events;
	}

	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarConfiguration,
			Period period, HttpServletRequest request) throws CalendarException {
		Set<CalendarEvent> events = new HashSet<CalendarEvent>();

		String url = this.urlCreator.constructUrl(calendarConfiguration, period, request);
		
		log.debug("generated url: " + url);
		
		// try to get the cached calendar
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(url));
		Element cachedElement = this.cache.get(key);
		if (cachedElement == null) {
			// read in the data
			// retrieve calendars for the current user
			net.fortuna.ical4j.model.Calendar calendar = retrieveCalendar(
					url, period, credentialsExtractor.getCredentials(request));

			// extract events from the calendars
				events.addAll(convertCalendarToEvents(
						calendarConfiguration.getId(), calendar, period));
			log.debug("contentProcessor found " + events.size() + " events");
			// save the CalendarEvents to the cache
			cachedElement = new Element(key, events);
			this.cache.put(cachedElement);
		} else {
			events = (Set<CalendarEvent>) cachedElement.getValue();
		}
		
		return events;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.ICalendarAdapter#getLink(org.jasig.portlet.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
	 */
	public String getLink(CalendarConfiguration calendar, Period period, PortletRequest request) throws CalendarLinkException {
		throw new CalendarLinkException("This calendar has no link");
	}
	
	protected final net.fortuna.ical4j.model.Calendar retrieveCalendar(
			String url, Period period, Credentials credentials) {

		try {
			
			// construct a HostConfiguration from the server URL
			URL hostUrl = new URL(url);
			int port = hostUrl.getPort();
			if (port == -1) {
				port = hostUrl.getDefaultPort();
			}
			
			HostConfiguration hostConfiguration = new HostConfiguration();
			hostConfiguration.setHost(hostUrl.getHost(), port, Protocol.getProtocol(hostUrl.getProtocol()));

			// construct a new calendar collection for our URL
			CalDAVCalendarCollection collection = new CalDAVCalendarCollection(
					url, hostConfiguration, new CalDAV4JMethodFactory(),
					org.osaf.caldav4j.CalDAVConstants.PROC_ID_DEFAULT);

			// construct a new HttpClient with the proper HostConfiguration and
			// set the authentication credentials if they are non-null
			HttpClient client = new HttpClient();
			client.setHostConfiguration(hostConfiguration);
			if (credentials != null) {
				client.getState().setCredentials(AuthScope.ANY, credentials);
			}

			// retrieve a list of calendars from the collection for the 
			// requested Period
			net.fortuna.ical4j.model.Calendar cal = collection.getCalendarByPath(client, hostUrl.getPath());

			return cal;

		} catch (CalDAV4JException e) {
			log.error("CalDAV exception: ", e);
			throw new CalendarException(e);
		} catch (MalformedURLException e) {
			throw new CalendarException(e);
		} catch (Exception e) {
		    throw new CalendarException("Unknown exception while retrieving calendar", e);
		}

	}

	protected final Set<CalendarEvent> convertCalendarToEvents(Long calendarId,
			net.fortuna.ical4j.model.Calendar calendar, Period period)
			throws CalendarException {

		Set<CalendarEvent> events = new HashSet<CalendarEvent>();

		// if the calendar is null, return empty set
		if (calendar == null) {
			log.warn("calendar was empty, returning empty set");
			return Collections.emptySet();
		}

		// retrieve the list of events for this calendar within the
		// specified time period
		for (Iterator<Component> i = calendar.getComponents().iterator(); i
				.hasNext();) {
			Component component = i.next();
			if (component.getName().equals("VEVENT")) {
				VEvent event = (VEvent) component;
				log.trace("processing event " + event.getSummary().getValue());
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
					for (Iterator<Property> iter2 = props.iterator(); iter2
							.hasNext();) {
						Property prop = iter2.next();

						// only add non-date-related properties
						if (!(prop instanceof DtStart)
								&& !(prop instanceof DtEnd)
								&& !(prop instanceof Duration)
								&& !(prop instanceof RRule))
							newprops.add(prop);
					}

					// create the new event from our property list
					CalendarEvent newevent = new CalendarEvent(calendarId,
							newprops);
					events.add(newevent);
					log.trace("added event " + newevent);
				}
			}
		}

		return events;
	}

}

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
package edu.yale.its.tp.portlets.calendar.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Period;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;
import edu.yale.its.tp.portlets.calendar.caching.DefaultCacheKeyGeneratorImpl;
import edu.yale.its.tp.portlets.calendar.caching.ICacheKeyGenerator;
import edu.yale.its.tp.portlets.calendar.credentials.DefaultCredentialsExtractorImpl;
import edu.yale.its.tp.portlets.calendar.credentials.ICredentialsExtractor;
import edu.yale.its.tp.portlets.calendar.processor.ICalendarContentProcessorImpl;
import edu.yale.its.tp.portlets.calendar.processor.IContentProcessor;
import edu.yale.its.tp.portlets.calendar.url.DefaultUrlCreatorImpl;
import edu.yale.its.tp.portlets.calendar.url.IUrlCreator;

/**
 * Implementation of {@link ICalendarAdapter} that uses Commons HttpClient
 * for retrieving {@link CalendarEvent}s.
 * 
 * This bean requires an EhCache {@link Cache} be provided.
 * This bean also depends on instances of 3 different interfaces 
 * (default implementation listed in parenthesis):
 * <ul>
 * <li>{@link IUrlCreator} (default configuration: {@link DefaultUrlCreatorImpl})</li>
 * <li>{@link ICredentialsExtractor} (default: {@link DefaultCredentialsExtractorImpl})</li>
 * <li>{@link IContentProcessor} (default: {@link ICalendarContentProcessorImpl})</li>
 * </ul>
 * 
 * By specifying alternate implementations for these interfaces, multiple instances of
 * this class can be configured to consume {@link CalendarEvent}s from a variety of different
 * end points, for example an RSS feed behind basic auth, a CalendarKey implementation behind
 * a shared secret, or behind CAS.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: RefactoredHttpICalendarAdapter.java Exp $
 */
public final class ConfigurableHttpCalendarAdapter implements ICalendarAdapter, ISingleEventSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Cache cache;
	private IUrlCreator urlCreator = new DefaultUrlCreatorImpl();
	private ICredentialsExtractor credentialsExtractor = new DefaultCredentialsExtractorImpl();
	private IContentProcessor contentProcessor = new ICalendarContentProcessorImpl();
	private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
	private String cacheKeyPrefix = "default";
	
	/**
	 * @param cache the cache to set
	 */
	@Required
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	/**
	 * @param urlCreator the urlCreator to set
	 */
	public void setUrlCreator(IUrlCreator urlCreator) {
		this.urlCreator = urlCreator;
	}
	
	/**
	 * @param credentialsExtractor the credentialsExtractor to set
	 */
	public void setCredentialsExtractor(ICredentialsExtractor credentialsExtractor) {
		this.credentialsExtractor = credentialsExtractor;
	}

	/**
	 * @param contentProcessor the contentProcessor to set
	 */
	public void setContentProcessor(IContentProcessor contentProcessor) {
		this.contentProcessor = contentProcessor;
	}

	/**
	 * @param cacheKeyPrefix the cacheKeyPrefix to set
	 */
	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}
	
	public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
		this.cacheKeyGenerator = cacheKeyGenerator;
	}

	/**
	 * Workflow for this implementation:
	 * 
	 * <ol>
	 * <li>consult the configured {@link IUrlCreator} for the url to request</li>
	 * <li>consult the cache to see if the fetch via HTTP is necessary (if not return the cached events)</li>
	 * <li>if the fetch is necessary, consult the {@link ICredentialsExtractor} for necessary {@link Credentials}</li>
	 * <li>Invoke retrieveCalendarHttp</li>
	 * <li>Pass the returned {@link InputStream} into the configured {@link IContentProcessor}</li>
	 * <li>Return the {@link CalendarEvent}s</li>
	 * </ol>
	 * 
	 *  (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter#getEvents(edu.yale.its.tp.portlets.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
	 */
	@SuppressWarnings("unchecked")
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarConfiguration,
			Period period, PortletRequest request) throws CalendarException {
		Set<CalendarEvent> events = Collections.emptySet();
		
		String url = this.urlCreator.constructUrl(calendarConfiguration, period, request);
		
		log.debug("generated url: " + url);
		
		// try to get the cached calendar
		Credentials credentials = credentialsExtractor.getCredentials(request);
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(url));
		Element cachedElement = this.cache.get(key);
		if (cachedElement == null) {
			// read in the data
			InputStream stream = retrieveCalendarHttp(url, credentials);
			// run the stream through the processor
			events = contentProcessor.getEvents(calendarConfiguration.getId(), period, stream);
			log.debug("contentProcessor found " + events.size() + " events");
			// save the CalendarEvents to the cache
			cachedElement = new Element(key, events);
			this.cache.put(cachedElement);
		} else {
			events = (Set<CalendarEvent>) cachedElement.getValue();
		}
		
		return events;
	}

	/**
	 * Workflow for this implementation:
	 * 
	 * <ol>
	 * <li>consult the configured {@link IUrlCreator} for the url to request</li>
	 * <li>consult the cache to see if the fetch via HTTP is necessary (if not return the cached events)</li>
	 * <li>if the fetch is necessary, consult the {@link ICredentialsExtractor} for necessary {@link Credentials}</li>
	 * <li>Invoke retrieveCalendarHttp</li>
	 * <li>Pass the returned {@link InputStream} into the configured {@link IContentProcessor}</li>
	 * <li>Return the {@link CalendarEvent}s</li>
	 * </ol>
	 * 
	 *  (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter#getEvents(edu.yale.its.tp.portlets.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.servlet.http.HttpServletRequest)
	 */
	@SuppressWarnings("unchecked")
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarConfiguration,
			Period period, HttpServletRequest request) throws CalendarException {
		Set<CalendarEvent> events = Collections.emptySet();
		String url = this.urlCreator.constructUrl(calendarConfiguration, period, request);
		
		log.debug("generated url: " + url);
		
		// try to get the cached calendar
		Credentials credentials = credentialsExtractor.getCredentials(request);
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(url));
		Element cachedElement = this.cache.get(key);
		if (cachedElement == null) {
			// read in the data
			InputStream stream = retrieveCalendarHttp(url, credentials);
			// run the stream through the processor
			events = contentProcessor.getEvents(calendarConfiguration.getId(), period, stream);
			log.debug("contentProcessor found " + events.size() + " events");
			// save the CalendarEvents to the cache
			cachedElement = new Element(key, events);
			this.cache.put(cachedElement);
		} else {
			events = (Set<CalendarEvent>) cachedElement.getValue();
		}
		
		return events;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ISingleEventSupport#getEvent(edu.yale.its.tp.portlets.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	public CalendarEvent getEvent(CalendarConfiguration calendar,
			Period period, String uid, String recurrenceId, HttpServletRequest request)
			throws CalendarException {
		Set<CalendarEvent> events = getEvents(calendar, period, request);
		for(CalendarEvent event : events) {
			if (event.getUid().toString().equals(uid) && (null == recurrenceId || event.getRecurrenceId().toString().equals(recurrenceId))) {
				return event;
			}
		}
		log.debug("event not found with uid " + uid + " and recurrence id " + recurrenceId);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ISingleEventSupport#getEvent(edu.yale.its.tp.portlets.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, java.lang.String, java.lang.String, javax.portlet.PortletRequest)
	 */
	public CalendarEvent getEvent(CalendarConfiguration calendar,
			Period period, String uid, String recurrenceId, PortletRequest request)
			throws CalendarException {
		Set<CalendarEvent> events = getEvents(calendar, period, request);
		for(CalendarEvent event : events) {
			if (event.getUid().toString().equals(uid) && (null == recurrenceId || event.getRecurrenceId().toString().equals(recurrenceId))) {
				return event;
			}
		}
		log.debug("event not found with uid " + uid + " and recurrence id " + recurrenceId);
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter#getLink(edu.yale.its.tp.portlets.calendar.CalendarConfiguration)
	 */
	public String getLink(CalendarConfiguration calendar, Period period, PortletRequest request) {
		throw new CalendarLinkException("This calendar has no link");
	}
	
	/**
	 * Uses Commons HttpClient to retrieve the specified url (optionally with the provided 
	 * {@link Credentials}.
	 * The response body is returned as an {@link InputStream}.
	 * 
	 * @param url URL of the calendar to be retrieved
	 * @param credentials {@link Credentials} to use with the request, if necessary (null is ok if credentials not required)
	 * @return the body of the http response as a stream
	 * @throws CalendarException wraps all potential {@link Exception} types 
	 */
	protected InputStream retrieveCalendarHttp(String url, Credentials credentials)
			throws CalendarException {
		HttpClient client = new HttpClient();
		if(null != credentials) {
			client.getState().setCredentials(AuthScope.ANY, credentials);
		}
		GetMethod get = null;

		try {

			if(log.isDebugEnabled()) {
				log.debug("Retrieving calendar " + url);
			}
			get = new GetMethod(url);
			int rc = client.executeMethod(get);
			if(rc == HttpStatus.SC_OK) {
				// return the response body
				log.debug("request completed successfully");
				InputStream in = get.getResponseBodyAsStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				IOUtils.copyLarge(in, buffer);
				return new ByteArrayInputStream(buffer.toByteArray());
			}
			else {
				log.warn("HttpStatus for " + url + ":" + rc);
				throw new CalendarException("non successful status code retrieving " + url + ", status code: " + rc);
			}
		} catch (HttpException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed", e);
		} catch (IOException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed", e);
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}

	}
	
}

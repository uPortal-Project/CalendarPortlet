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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
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
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.credentials.DefaultCredentialsExtractorImpl;
import org.jasig.portlet.calendar.credentials.ICredentialsExtractor;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;
import org.jasig.portlet.calendar.url.DefaultUrlCreatorImpl;
import org.jasig.portlet.calendar.url.IUrlCreator;
import org.springframework.beans.factory.annotation.Required;

import com.microsoft.exchange.types.CalendarEvent;


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
public final class ConfigurableHttpCalendarAdapter extends AbstractCalendarAdapter implements ICalendarAdapter {

	protected final Log log = LogFactory.getLog(this.getClass());
	
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
	 * @see org.jasig.portlet.calendar.adapter.ICalendarAdapter#getEvents(org.jasig.portlet.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
	 */
	public CalendarEventSet getEvents(CalendarConfiguration calendarConfiguration,
			Period period, PortletRequest request) throws CalendarException {
		CalendarEventSet eventSet;
		
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
			Set<VEvent> events = contentProcessor.getEvents(calendarConfiguration.getId(), period, stream);
			log.debug("contentProcessor found " + events.size() + " events");
			
			// save the VEvents to the cache
			eventSet = new CalendarEventSet(key, events);
            cachedElement = new Element(key, eventSet);
			this.cache.put(cachedElement);
		} else {
			eventSet = (CalendarEventSet) cachedElement.getValue();
		}
		
		return eventSet;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.ICalendarAdapter#getLink(org.jasig.portlet.calendar.CalendarConfiguration)
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

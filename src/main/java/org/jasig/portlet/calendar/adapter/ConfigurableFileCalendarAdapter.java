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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;


public class ConfigurableFileCalendarAdapter implements ICalendarAdapter {

	protected final Log log = LogFactory.getLog(this.getClass());

	private Cache cache;
	private IContentProcessor contentProcessor = new ICalendarContentProcessorImpl();
	private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
	private String cacheKeyPrefix = "default";

	
	public CalendarEventSet getEvents(CalendarConfiguration calendarConfiguration,
			Period period, PortletRequest request) throws CalendarException {
		Set<VEvent> events = Collections.emptySet();
		
		String fileName = calendarConfiguration.getCalendarDefinition().getParameters().get("file");
		
		// try to get the cached calendar
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(fileName));
		Element cachedElement = this.cache.get(key);
        CalendarEventSet eventSet;
		if (cachedElement == null) {
			// read in the data
			InputStream stream = retrieveCalendar(fileName);
			// run the stream through the processor
			events = contentProcessor.getEvents(calendarConfiguration.getId(), period, stream);
			log.debug("contentProcessor found " + events.size() + " events");
			// save the CalendarEvents to the cache
            eventSet = new CalendarEventSet(key, events);
            String timeAwareKey = key.concat(String.valueOf(System.currentTimeMillis()));
            cachedElement = new Element(timeAwareKey, eventSet);
            this.cache.put(cachedElement);
        } else {
            eventSet = (CalendarEventSet) cachedElement.getValue();
		}
		
		return eventSet;
	}

	protected InputStream retrieveCalendar(String fileName)
			throws CalendarException {

		if(log.isDebugEnabled()) {
			log.debug("Retrieving calendar " + fileName);
		}

		try {

			InputStream in = new FileInputStream(fileName);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			IOUtils.copyLarge(in, buffer);
			return new ByteArrayInputStream(buffer.toByteArray());

		} catch (HttpException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed", e);
		} catch (IOException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed", e);
		} finally {
		}

	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.ICalendarAdapter#getLink(org.jasig.portlet.calendar.CalendarConfiguration)
	 */
	public String getLink(CalendarConfiguration calendar, Period period, PortletRequest request) {
		throw new CalendarLinkException("This calendar has no link");
	}
	
	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public void setContentProcessor(IContentProcessor contentProcessor) {
		this.contentProcessor = contentProcessor;
	}

	public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
		this.cacheKeyGenerator = cacheKeyGenerator;
	}

	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}
	
}

package org.jasig.portlet.calendar.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Period;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;


public class ConfigurableFileCalendarAdapter implements ICalendarAdapter, ISingleEventSupport {

	private Log log = LogFactory.getLog(this.getClass());

	private Cache cache;
	private IContentProcessor contentProcessor = new ICalendarContentProcessorImpl();
	private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
	private String cacheKeyPrefix = "default";

	
	@SuppressWarnings("unchecked")
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarConfiguration,
			Period period, PortletRequest request) throws CalendarException {
		Set<CalendarEvent> events = Collections.emptySet();
		
		String fileName = calendarConfiguration.getCalendarDefinition().getParameters().get("file");
		
		// try to get the cached calendar
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(fileName));
		Element cachedElement = this.cache.get(key);
		if (cachedElement == null) {
			// read in the data
			InputStream stream = retrieveCalendar(fileName);
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

	@SuppressWarnings("unchecked")
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarConfiguration,
			Period period, HttpServletRequest request) throws CalendarException {
		Set<CalendarEvent> events = Collections.emptySet();
		String fileName = calendarConfiguration.getCalendarDefinition().getParameters().get("file");
		
		// try to get the cached calendar
		String key = cacheKeyGenerator.getKey(calendarConfiguration, period, request, cacheKeyPrefix.concat(".").concat(fileName));
		Element cachedElement = this.cache.get(key);
		if (cachedElement == null) {
			// read in the data
			InputStream stream = retrieveCalendar(fileName);
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
	 * @see org.jasig.portlet.calendar.adapter.ISingleEventSupport#getEvent(org.jasig.portlet.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
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
	 * @see org.jasig.portlet.calendar.adapter.ISingleEventSupport#getEvent(org.jasig.portlet.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, java.lang.String, java.lang.String, javax.portlet.PortletRequest)
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

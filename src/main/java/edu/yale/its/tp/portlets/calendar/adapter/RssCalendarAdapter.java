/*
 * Created on Feb 8, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;
import edu.yale.its.tp.portlets.calendar.jaxb.TRssChannel;
import edu.yale.its.tp.portlets.calendar.jaxb.TRss;
import edu.yale.its.tp.portlets.calendar.jaxb.TRssItem;

/**
 * RssCalendarAdapter is a generic CalendarAdapter for RSS-formatted event feeds.
 * This adapter assumes that the each item will represent one event, and that the 
 * event date and time will be represented by the item's pubDate element.  Since
 * feeds may vary in the exact date format used by the pubDate element, an optional 
 * parameter of "dateFormat" may be defined.
 *
 * @author Jen Bourey
 */
public class RssCalendarAdapter implements ICalendarAdapter {

	private static Log log = LogFactory.getLog(HttpICalAdapter.class);

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter#getEvents(edu.yale.its.tp.portlets.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
	 */
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,
			Period period, PortletRequest request) throws CalendarException {
		return getEvents(calendar, period);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter#getEvents(edu.yale.its.tp.portlets.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.servlet.http.HttpServletRequest)
	 */
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,
			Period period, HttpServletRequest request) throws CalendarException {
		return getEvents(calendar, period);
	}

	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,
			Period period) throws CalendarException {
		
		// get the URL for this calendar
		String url = (String) calendar.getCalendarDefinition().getParameters().get("url");
		
		// get the date format for the pubDate attribute used by this RSS feed
		String dateFormat = (String) calendar.getCalendarDefinition().getParameters().get("dateFormat");
		if (dateFormat == null)
			dateFormat = "EEE, dd MMMM yyyy H:mm:ss z";
		
		// return a list of calendar events for the requested time period
		return getEvents(calendar.getId(), url, dateFormat, period.getStart().getTime(), period.getEnd().getTime());

	}
	
	
	/**
	 * 
	 * @param calendarId 	unique id of the CalendarConfiguration producing these events
	 * @param url			RSS feed URL
	 * @param dateFormat	date format string for the pubDate element
	 * @param periodStart	start of the time period for which to retrieve events
	 * @param periodEnd		end date of the time period for which to retrieve events
	 * @return				Set of events in the specified time period
	 * @throws CalendarException
	 */
	protected Set<CalendarEvent> getEvents(long calendarId, String url, String dateFormat, long periodStart, long periodEnd) throws CalendarException {

		Set<CalendarEvent> events = new HashSet<CalendarEvent>();
		DateFormat df = new SimpleDateFormat(dateFormat);
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

			// retrieve the feed
			InputStream in = get.getResponseBodyAsStream();
			
			// unmarshall the feed as an RSS 2.0 object
			JAXBContext jaxbContext = JAXBContext
					.newInstance("edu.yale.its.tp.portlets.calendar.jaxb");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			JAXBElement<TRss> rss = (JAXBElement<TRss>) unmarshaller.unmarshal(in);

			// get the event items from the feed
			List<TRssItem> items = rss.getValue().getChannel().getItem();
			for (TRssItem item : items) {
				
				PropertyList props = new PropertyList();
				
				// Attempt to use the pubDate element as the start date for this
				// event.  RSS feeds don't really give us anything to use
				// for an end date.
				Date start = null;
				if (item.getPubDate() != null) {
					try {
						start = df.parse(item.getPubDate());
					} catch (ParseException ex) {
						log.debug("Failed to parse date " + item.getPubDate());
					}
				}
				
				// we only want to add this feed if it's in the desired time period
				if (start != null && start.getTime() >= periodStart && start.getTime() <= periodEnd) {

					props.add(new DtStart(new DateTime(start)));
					props.add(new Summary(item.getTitle()));
					props.add(new Description(item.getDescription()));

					// use the RSS item Guid as the Uid for this event
					if (item.getGuid() != null)
						props.add(new Uid(item.getGuid().getValue()));
					
					// try to find a link for this event
					if (item.getLink() != null) {
						try {
							props.add(new Url(new URI(item.getLink())));
						} catch (URISyntaxException e1) { }
					}
					
					// construct and add the new calendar event
					CalendarEvent event = new CalendarEvent(calendarId, props);
					events.add(event);
				}

			}

			// return the list of matching calendar events
			return events;

		} catch (HttpException e) {
			log.warn("Error fetching RSS calendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed");
		} catch (IOException e) {
			log.warn("Error fetching RSS calendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed");
		} catch (JAXBException e1) {
			log.warn("Error parsing RSS calendar feed", e1);
			throw new CalendarException("Error parsing iCalendar feed");
		} finally {
			if (get != null)
				get.releaseConnection();
		}

	
	}
}


/*
 * RssCalendarAdapter.java
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
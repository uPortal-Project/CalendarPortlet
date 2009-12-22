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
package org.jasig.portlet.calendar.processor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarEvent;

import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;


/**
 * This {@link IContentProcessor} implementation uses Rome to extract
 * {@link CalendarEvent}s from RSS formatted streams.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: RssContentProcessorImpl.java Exp $
 */
public class RssContentProcessorImpl implements IContentProcessor {

	private Log log = LogFactory.getLog(this.getClass());
	
	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.ContentProcessor#getEvents(java.lang.Long, net.fortuna.ical4j.model.Period, java.io.InputStream)
	 */
	public Set<CalendarEvent> getEvents(Long calendarId, Period period,
			InputStream in) {
		Set<CalendarEvent> events = new HashSet<CalendarEvent>();
		
		try {
			final SyndFeedInput input = new SyndFeedInput();
			final InputStreamReader reader = new InputStreamReader(in);
			final SyndFeed feed = input.build(reader);
			
			List<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();
			for (SyndEntry entry : entries) {
				PropertyList props = new PropertyList();
				
				// Attempt to use the pubDate element as the start date for this
				// event.  RSS feeds don't really give us anything to use
				// for an end date.
				Date start = null;
				if (entry.getPublishedDate() != null) {
					start = entry.getPublishedDate();
				}
				
				// we only want to add this feed if it's in the desired time period
				if (start != null && start.after(period.getStart()) && start.before(period.getEnd())) {

					props.add(new DtStart(new DateTime(start)));
					props.add(new Summary(entry.getTitle()));
					props.add(new Description(entry.getDescription().getValue()));

					// use the RSS item Guid as the Uid for this event
					String guid = null;
					if (entry instanceof Item && ((Item) entry).getGuid() != null) {
						guid = ((Item) entry).getGuid().getValue();
						props.add(new Uid(guid));
					}
					
					// try to find a link for this event
					if (entry.getLink() != null) {
						try {
							props.add(new Url(new URI(entry.getLink())));
						} catch (URISyntaxException e1) { }
					}
					
					// construct and add the new calendar event
					CalendarEvent event = new CalendarEvent(calendarId, props);
					events.add(event);
				}
				
			}
			
		} catch (IllegalArgumentException e) {
			log.error(e);
		} catch (FeedException e) {
			log.error(e);
		}
		
		// return the list of matching calendar events
		return events;
	}

}

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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class CalendarEventsDao {

    protected final Log log = LogFactory.getLog(getClass());

    private Cache cache;

    /**
     * @param cache the cache to set
     */
    @Required
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    private Map<String, DateTimeFormatter> dateFormatters = new ConcurrentHashMap<String, DateTimeFormatter>();

    private Map<String, DateTimeFormatter> timeFormatters = new ConcurrentHashMap<String, DateTimeFormatter>();

    /**
     * Obtains the calendar events from the adapter and returns timezone-adjusted
     * events within the requested interval.
     * @param adapter Adapter to invoke to obtain the calendar events
     * @param calendar Per-user Calendar configuration
     * @param interval Interval to return events for
     * @param request Portlet request
     * @param tz Timezone to adjust the calendar events to (typically the user's timezone)
     * @return Set of calendar events meeting the requested criteria
     */
    public Set<CalendarDisplayEvent> getEvents(ICalendarAdapter adapter, CalendarConfiguration calendar,
            Interval interval, PortletRequest request, DateTimeZone tz) {

        // Get the set of calendar events for the requested period.
        // We invoke the adapter before checking cache because we expect the adapter
        // to do the first-level caching of the events.
        final CalendarEventSet eventSet = adapter.getEvents(calendar, interval, request);

        // The calendar events from the adapter will reflect the timezone of the calendar
        // server in the event times.  The events need to be corrected to reflect the
        // requested timezone (typically the user's timezone). Adjusting the
        // event's timezone is an expensive operation so the JSON of the
        // timezone-adjusted events is cached per timezone.

        // Append the requested time zone id to the retrieve event set's cache
        // key to generate a timezone-aware cache key
        final String tzKey = eventSet.getKey().concat(tz.getID());

        // attempt to retrieve the timezone-aware event set from cache
        Element cachedElement = this.cache.get(tzKey);
        if (cachedElement != null) {
            @SuppressWarnings("unchecked")
            final Set<CalendarDisplayEvent> jsonEvents = (Set<CalendarDisplayEvent>) cachedElement.getValue();
            return jsonEvents;
        }
        
        // if the timezone-corrected event set is not available in the cache
        // generate a new set and cache it
        else {

            // for each event in the event set, generate a set of timezone-corrected
            // event occurrences and add it to the new set
            final Set<CalendarDisplayEvent> displayEvents = new HashSet<CalendarDisplayEvent>();
            for (VEvent event : eventSet.getEvents()) {
                try {
                    displayEvents.addAll(getDisplayEvents(event, interval, tz));
                } catch (ParseException e) {
                    log.error("Exception parsing event", e);
                } catch (IOException e) {
                    log.error("Exception parsing event", e);
                } catch (URISyntaxException e) {
                    log.error("Exception parsing event", e);
                }
            }
            
            // Cache and return the resulting event list.  If the event set
            // was cached, set the event list to expire at about the same time so it
            // doesn't live in cache beyond the time the data it is derived
            // from is considered up to date. Time to live is relative to the
            // time the item is put into cache so the resulting event list will typically
            // expire from 1 second before the event set to afterward by the amount
            // of execution time from the adapter to displayEvents computation
            // completing which should not be a big delta.
            cachedElement = new Element(tzKey, displayEvents);
            long currentTime = System.currentTimeMillis();
            if (eventSet.getExpirationTime() > currentTime) {
                long timeToLiveInMilliseconds =
                        eventSet.getExpirationTime() - currentTime;
                int timeToLiveInSeconds = (int)timeToLiveInMilliseconds/1000;
                cachedElement.setTimeToLive(timeToLiveInSeconds);
            }
            this.cache.put(cachedElement);
            return displayEvents;
        } 
        
    }
    
    
    /**
     * Get a JSON-appropriate representation of each recurrence of an event
     * within the specified time period.
     * 
     * @param event
     * @param period
     * @param tz
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ParseException
     */
    protected Set<CalendarDisplayEvent> getDisplayEvents(VEvent e, Interval interval, DateTimeZone timezone) throws IOException, URISyntaxException, ParseException {

        final VEvent event = (VEvent) e.copy();

        DateTime eventStart;
        DateTime eventEnd = null;
        
        if (event.getStartDate().getTimeZone() == null && !event.getStartDate().isUtc()) {
            if (log.isDebugEnabled()) {
                log.debug("Identified event " + event.getSummary() + " as a floating event");
            }
            
            int offset = timezone.getOffset(event.getStartDate().getDate().getTime());
            eventStart = new DateTime(event.getStartDate().getDate().getTime()-offset, timezone);
            if (event.getEndDate() != null) {
                eventEnd = new DateTime(event.getEndDate().getDate().getTime()-offset, timezone);
            }
            
        } else {
            eventStart = new DateTime(event.getStartDate().getDate(), timezone);
            if (event.getEndDate() != null) {
                eventEnd = new DateTime(event.getEndDate().getDate(), timezone);
            }
        }
        
        if (eventEnd == null) {
            eventEnd = eventStart;
        }

        
        DateMidnight dayStart = new DateMidnight(event.getStartDate().getDate(), timezone);
        DateMidnight dayEnd = dayStart.plusDays(1);

        final DateTimeFormatter df = getDateFormatter(timezone);        
        final DateTimeFormatter tf = getTimeFormatter(timezone);
        final Set<CalendarDisplayEvent> events = new HashSet<CalendarDisplayEvent>();
        
        do {
            final Interval day = new Interval(dayStart, dayEnd);
            final Interval eventInterval = new Interval(eventStart, eventEnd);

            final CalendarDisplayEvent json = new CalendarDisplayEvent(event, eventInterval, day, df, tf);
            

            // if the adjusted event still falls within the 
            // indicated period go ahead and add it to our list

            // the event starts exactly at the day boundary
            if (day.getStart().isEqual(eventStart)) {
                events.add(json);
            }
            
            // the event day overlaps this day
            else if (day.overlaps(eventInterval)) {
                events.add(json);
            }

            dayStart = dayStart.plusDays(1);
            dayEnd = dayEnd.plusDays(1);

        } while (dayStart.isBefore(eventEnd) && interval.contains(dayStart));
        
        return events;
    }


    protected DateTimeFormatter getDateFormatter(DateTimeZone timezone) {
        if (this.dateFormatters.containsKey(timezone.getID())) {
            return this.dateFormatters.get(timezone.getID());
        } else {
            DateTimeFormatter df = new DateTimeFormatterBuilder().appendDayOfWeekText()
                .appendLiteral(" ").appendMonthOfYearText().appendLiteral(" ")
                .appendDayOfMonth(1).toFormatter().withZone(timezone);        
            this.dateFormatters.put(timezone.getID(), df);
            return df;
        }
    }
    
    protected DateTimeFormatter getTimeFormatter(DateTimeZone timezone) {
        if (this.timeFormatters.containsKey(timezone.getID())) {
            return this.timeFormatters.get(timezone.getID());
        } else {
            DateTimeFormatter tf = new DateTimeFormatterBuilder().appendClockhourOfHalfday(1)
                .appendLiteral(":").appendMinuteOfHour(2).appendLiteral(" ")
                .appendHalfdayOfDayText().toFormatter().withZone(timezone);
            this.timeFormatters.put(timezone.getID(), tf);
            return tf;
        }
    }
    
}

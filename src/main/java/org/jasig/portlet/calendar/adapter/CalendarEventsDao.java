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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.mvc.JsonCalendarEvent;
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

    public Set<JsonCalendarEvent> getEvents(ICalendarAdapter adapter, CalendarConfiguration calendar,
            Period period, PortletRequest request, TimeZone tz) {

        // get the set of pre-timezone-corrected events for the requested period
        final CalendarEventSet eventSet = adapter.getEvents(calendar, period, request);

        // append the requested time zone id to the retrieve event set's cache
        // key to generate a timezone-aware cache key
        final TimeZoneRegistryFactory tzFactory = new DefaultTimeZoneRegistryFactory();
        final TimeZoneRegistry tzRegistry = tzFactory.createRegistry();
        final String tzKey = eventSet.getKey().concat(tz.getID());

        // attempt to retrieve the timezone-aware event set from cache
        Element cachedElement = this.cache.get(tzKey);
        if (cachedElement != null) {
            @SuppressWarnings("unchecked")
            final Set<JsonCalendarEvent> jsonEvents = (Set<JsonCalendarEvent>) cachedElement.getValue();
            return jsonEvents;
        }
        
        // if the timezone-corrected event set is not availble in the cache
        // generate a new set and cache it
        else {

            // for each event in the event set, generate a set of timezone-corrected
            // event occurrences and add it to the new set
            final Set<JsonCalendarEvent> jsonEvents = new HashSet<JsonCalendarEvent>();
            for (VEvent event : eventSet.getEvents()) {
                try {
                    jsonEvents.addAll(getTimeZoneAwareEvents(event, tz, tzRegistry, period));
                } catch (ParseException e) {
                    log.error("Exception parsing event", e);
                } catch (IOException e) {
                    log.error("Exception parsing event", e);
                } catch (URISyntaxException e) {
                    log.error("Exception parsing event", e);
                }
            }
            
            // cache and return the resulting event list
            cachedElement = new Element(tzKey, jsonEvents);
            this.cache.put(cachedElement);
            return jsonEvents;
        } 
        
    }
    
    protected Set<JsonCalendarEvent> getTimeZoneAwareEvents(final VEvent e, final TimeZone tz, final TimeZoneRegistry tzRegistry, final Period period) throws ParseException, IOException, URISyntaxException {
        
        final VEvent event = (VEvent) e.copy();

        /*
         * Provide special handling for events with "floating"
         * timezones.
         */
        if (event.getStartDate().getTimeZone() == null && !event.getStartDate().isUtc()) {
            // first adjust the event to have the correct start
            // and end times for the user's timezone
            if (log.isDebugEnabled()) {
                log.debug("Identified event " + event.getSummary() + " as a floating event");
            }
            int offset = tz.getOffset(event.getStartDate().getDate().getTime());
            event.getStartDate().getDate().setTime(event.getStartDate().getDate().getTime()-offset);
            if (event.getEndDate() != null) {
                event.getEndDate().getDate().setTime(event.getEndDate().getDate().getTime()-offset);
            }
            
        // if the event is UTC, ensure that the event timezone is
        // set appropriately
        } else if (event.getStartDate().isUtc()) {
            if (log.isDebugEnabled()) {
                log.debug("Setting time zone to UTC for  event " + event.getSummary());
            }
            event.getStartDate().setTimeZone(tzRegistry.getTimeZone("UTC"));
            if (event.getEndDate() != null) {
                event.getEndDate().setTimeZone(tzRegistry.getTimeZone("UTC"));
            }
        }

        final Set<JsonCalendarEvent> events = getJsonEvents(event, period, tz);
        return events;

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
    protected Set<JsonCalendarEvent> getJsonEvents(VEvent event, Period period, TimeZone tz) throws IOException, URISyntaxException, ParseException {

        final Calendar dayStart = Calendar.getInstance(tz);
        dayStart.setTime(event.getStartDate().getDate());
        dayStart.set(Calendar.HOUR, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 1);
        
        final Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DATE, 1);

        final Calendar eventEnd = Calendar.getInstance(tz);
        if (event.getEndDate() != null) {
            eventEnd.setTime(event.getEndDate().getDate());
        }

        final Set<JsonCalendarEvent> events = new HashSet<JsonCalendarEvent>();
        
        do {
            final JsonCalendarEvent json = new JsonCalendarEvent(event, dayStart.getTime(), tz);

            // if the adjusted event still falls within the 
            // indicated period go ahead and add it to our list
            if (period.includes(json.getDayStart(), Period.INCLUSIVE_START) 
                    || period.includes(json.getDayEnd(), Period.INCLUSIVE_END)) {

                events.add(json);
            }

            dayStart.add(Calendar.DATE, 1);
            dayEnd.add(Calendar.DATE, 1);

        } while (dayStart.before(eventEnd));
        
        return events;
    }


}

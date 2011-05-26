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

        CalendarEventSet eventSet = adapter.getEvents(calendar, period, request);

        TimeZoneRegistryFactory tzFactory = new DefaultTimeZoneRegistryFactory();
        TimeZoneRegistry tzRegistry = tzFactory.createRegistry();

        String tzKey = eventSet.getKey().concat(tz.getID());
        Element cachedElement = this.cache.get(eventSet.getKey());
        Set<JsonCalendarEvent> jsonEvents;
        if (cachedElement == null) {

            jsonEvents = new HashSet<JsonCalendarEvent>();
            Set<VEvent> events = eventSet.getEvents();
            for (VEvent event : events) {
                try {
                    jsonEvents.addAll(getStuff(event, tz, tzRegistry, period));
                } catch (ParseException e) {
                    log.error("Exception parsing event", e);
                } catch (IOException e) {
                    log.error("Exception parsing event", e);
                } catch (URISyntaxException e) {
                    log.error("Exception parsing event", e);
                }
            }
            
            cachedElement = new Element(tzKey, eventSet);
            this.cache.put(cachedElement);
        } else {
            jsonEvents = (Set<JsonCalendarEvent>) cachedElement.getValue();
        }
        
        return jsonEvents;
    }
    
    protected Set<JsonCalendarEvent> getStuff(VEvent e, TimeZone tz, TimeZoneRegistry tzRegistry, Period period) throws ParseException, IOException, URISyntaxException {
        
        VEvent event = (VEvent) e.copy();

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

        Set<JsonCalendarEvent> events = getJsonEvents(event, period, tz);
        return events;

    }
    
    protected Set<JsonCalendarEvent> getJsonEvents(VEvent event, Period period, TimeZone tz) throws IOException, URISyntaxException, ParseException {

        Calendar dayStart = Calendar.getInstance(tz);
        dayStart.setTime(event.getStartDate().getDate());
        dayStart.set(Calendar.HOUR, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 1);
        
        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DATE, 1);

        Calendar eventEnd = Calendar.getInstance(tz);
        if (event.getEndDate() != null) {
            eventEnd.setTime(event.getEndDate().getDate());
        } else {
            eventEnd.setTime(event.getStartDate().getDate());
        }

        Set<JsonCalendarEvent> events = new HashSet<JsonCalendarEvent>();
        
        do {
            JsonCalendarEvent json = new JsonCalendarEvent(event, dayStart.getTime(), tz);

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

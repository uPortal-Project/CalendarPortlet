package org.jasig.portlet.calendar.adapter;

import java.util.Set;

import net.fortuna.ical4j.model.component.VEvent;

/**
 * CalendarEventSet represents a set of cacheable calendar events.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class CalendarEventSet {

    private final String key;
    private final Set<VEvent> events;
    
    public CalendarEventSet(String key, Set<VEvent> events) {
        this.key = key;
        this.events = events;
    }

    public String getKey() {
        return key;
    }

    public Set<VEvent> getEvents() {
        return events;
    }

}

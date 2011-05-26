package org.jasig.portlet.calendar.mvc;


/**
 * JsonCalendarEventWrapper wraps a shared/cached JsonCalendarEvent and provides
 * a way to add user-specific information such as a color code.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class JsonCalendarEventWrapper implements Comparable<JsonCalendarEventWrapper> {
    
    private final JsonCalendarEvent event;
    private final int colorIndex;
    
    public JsonCalendarEventWrapper(JsonCalendarEvent event, int colorIndex) {
        this.event = event;
        this.colorIndex = colorIndex;
    }

    public JsonCalendarEvent getEvent() {
        return event;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    @Override
    public int compareTo(JsonCalendarEventWrapper wrapper) {
        return this.event.compareTo(wrapper.event);
    }

    @Override
    public boolean equals(Object o) {
        return this.event.equals(o);
    }

}

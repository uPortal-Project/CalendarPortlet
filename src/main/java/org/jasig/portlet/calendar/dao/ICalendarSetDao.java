package org.jasig.portlet.calendar.dao;

import javax.portlet.PortletRequest;

import org.jasig.portlet.calendar.CalendarSet;

public interface ICalendarSetDao {
    
    public CalendarSet<?> getCalendarSet(PortletRequest request);

}

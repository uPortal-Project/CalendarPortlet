package org.jasig.portlet.calendar.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.jasig.portlet.calendar.service.SessionSetupInitializationService;
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class HibernateCalendarSetDao implements ICalendarSetDao {
    
    private CalendarStore calendarStore;
    
    @Required
    @Resource(name="calendarStore")
    public void setCalendarStore(CalendarStore calendarStore) {
        this.calendarStore = calendarStore;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.calendar.dao.ICalendarSetDao#getCalendarSet(javax.portlet.PortletRequest)
     */
    public CalendarSet<?> getCalendarSet(PortletRequest request) {
        
        // Get the username from the portlet session.  This implementation
        // assumes that the session initialization service is run at login time
        PortletSession session = request.getPortletSession();
        String username = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
        
        // retrieve a list of calendar configurations for this user
        List<UserDefinedCalendarConfiguration> cals = calendarStore
            .getCalendarConfigurations(username);
        
        Set<UserDefinedCalendarConfiguration> calendars = new HashSet<UserDefinedCalendarConfiguration>();
        calendars.addAll(cals);
        
        CalendarSet<UserDefinedCalendarConfiguration> set = new CalendarSet<UserDefinedCalendarConfiguration>();
        set.setConfigurations(calendars);
        return set;
    }

}

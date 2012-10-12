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

package org.jasig.portlet.calendar.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
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
        
        final String username = getUsername(request);
                
        // retrieve a list of calendar configurations for this user
        final List<UserDefinedCalendarConfiguration> cals = calendarStore
            .getCalendarConfigurations(username);
        
        final Set<UserDefinedCalendarConfiguration> calendars = new HashSet<UserDefinedCalendarConfiguration>();
        calendars.addAll(cals);
        
        final CalendarSet<UserDefinedCalendarConfiguration> set = new CalendarSet<UserDefinedCalendarConfiguration>();
        set.setConfigurations(calendars);
        return set;
    }

    @Override
    public List<PredefinedCalendarConfiguration> getAvailablePredefinedCalendarConfigurations(PortletRequest request) {

        final String username = getUsername(request);

        // For this ICalendarSetDao, the list is the unfiltered collection from CalendarStore...
        return calendarStore.getPredefinedCalendarConfigurations(username, false);
    }
    
    /*
     * Implementation
     */
    
    private String getUsername(PortletRequest request) {
        // Get the username from the portlet session.  This implementation
        // assumes that the session initialization service is run at login time
        final PortletSession session = request.getPortletSession();
        final String rslt = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
        return rslt;
    }

}

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
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarDefinition;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.springframework.beans.factory.annotation.Required;

/**
 * PortletPreferencesCalendarSetDao provides a portlet preference-based 
 * implementation of the ICalendarSetDao interface.  This implementation is
 * currently very limited and does not support the addition of user editing or
 * configuration or interact with any of the roles features.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class PortletPreferencesCalendarSetDao implements ICalendarSetDao {
    
    protected final String CALENDAR_FNAME_KEY = "calendarFnames";
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private CalendarStore calendarStore;
    
    @Resource(name="calendarStore")
    @Required
    public void setCalendarStore(CalendarStore calendarStore) {
        this.calendarStore = calendarStore;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.calendar.dao.ICalendarSetDao#getCalendarSet(javax.portlet.PortletRequest)
     */
    public CalendarSet<PredefinedCalendarConfiguration> getCalendarSet(PortletRequest request) {

        // get the calendar fname array from the portlet preferences
        PortletPreferences preferences = request.getPreferences();
        String[] calendarFnames = preferences.getValues(CALENDAR_FNAME_KEY, new String[]{});
        
        // for each configured fname, attempt to find the relevant predefined
        // calendar definition and create a default configuration for it
        Set<PredefinedCalendarConfiguration> calendars = new HashSet<PredefinedCalendarConfiguration>();
        for (String fname : calendarFnames) {
            
            // attempt to locate the calendar definition associated with
            // this fname
            CalendarDefinition definition = null;
            try {
                definition = calendarStore.getCalendarDefinition(fname);
            } catch (Exception e) {
                log.error("Failed to retrieve calendar definition with fname " + fname);
            }
            
            // if we found a definition, add a configuration to the list
            if (definition != null) {
                PredefinedCalendarConfiguration config = new PredefinedCalendarConfiguration();
                config.setCalendarDefinition(definition);
                calendars.add(config);
            }
        }
        
        // create a new calendar set from these configurations
        CalendarSet<PredefinedCalendarConfiguration> set = new CalendarSet<PredefinedCalendarConfiguration>();
        set.setConfigurations(calendars);
        return set;
    }

}

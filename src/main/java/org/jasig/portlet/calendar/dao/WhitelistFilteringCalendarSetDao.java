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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Decorates another instance of {@link ICalendarSetDao} and filters the results 
 * it provides by the specified whitelist, unless the list is empty.  In that
 * case all results will be displayed.
 * 
 * @author awills
 */
public class WhitelistFilteringCalendarSetDao implements ICalendarSetDao {
    
    private ICalendarSetDao enclosedCalendarSetDao;
    
    public void setEnclosedCalendarSetDao(ICalendarSetDao enclosedCalendarSetDao) {
        this.enclosedCalendarSetDao = enclosedCalendarSetDao;
    }

    /**
     * If this preference is non-empty, only calendar-definitions whose fnames 
     * appear in the whitelist will be shown.
     */
    private static final String CALENDAR_WHITELIST_PREFERENCE = "calendarWhitelist";

    @Override
    public CalendarSet<?> getCalendarSet(PortletRequest req) {
        
        final CalendarSet<? extends CalendarConfiguration> unmodifiedSet = 
                enclosedCalendarSetDao.getCalendarSet(req);

        final List<String> whitelist = getWhitelist(req);
        if (whitelist.size() == 0) {
            // No filtering to do...
            return unmodifiedSet;
        }
        
        /*
         * A whitelist of allowed calender-definition fnames has been defined, 
         * so we must filter out calendar-definitions that are not on the list
         */
        
        final Set<CalendarConfiguration> configurations = new HashSet<CalendarConfiguration>();

        final Set<? extends CalendarConfiguration> rawSet = unmodifiedSet.getConfigurations(); 
        for (CalendarConfiguration config : rawSet) {
            if (config.getCalendarDefinition() instanceof PredefinedCalendarDefinition) {
                final PredefinedCalendarDefinition pdef = (PredefinedCalendarDefinition) 
                        config.getCalendarDefinition();
                // Make sure it appears on the whitelist
                if (whitelist.contains(pdef.getFname())) {
                    configurations.add(config);
                }
            } else {
                // User-defined calendar-definitions always pass through;  if you 
                // intend to prevent them, set disablePreferences to 'true'
                configurations.add(config);
            }
        }
        
        final CalendarSet<CalendarConfiguration> rslt = new CalendarSet<CalendarConfiguration>();
        rslt.setConfigurations(configurations);
        return rslt;
    
    }

    @Override
    public List<PredefinedCalendarConfiguration> getAvailablePredefinedCalendarConfigurations(
            PortletRequest req) {

        final List<PredefinedCalendarConfiguration> unmodifiedList = 
                enclosedCalendarSetDao.getAvailablePredefinedCalendarConfigurations(req);

        final List<String> whitelist = getWhitelist(req);
        if (whitelist.size() == 0) {
            // No filtering to do...
            return unmodifiedList;
        }

        /*
         * A whitelist of allowed calender-definition fnames has been defined, 
         * so we must filter out calendar-definitions that are not on the list
         */

        final List<PredefinedCalendarConfiguration> rslt = new ArrayList<PredefinedCalendarConfiguration>();

        for (PredefinedCalendarConfiguration config :  unmodifiedList) {
            final PredefinedCalendarDefinition pdef = (PredefinedCalendarDefinition) 
                    config.getCalendarDefinition();
            if (whitelist.contains(pdef.getFname())) {
                rslt.add(config);
            }
        }

        return rslt;
        
    }

    /*
     * Implementation
     */
    
    private List<String> getWhitelist(PortletRequest req) {
        final PortletPreferences prefs = req.getPreferences();
        @SuppressWarnings("unchecked")
        final List<String> rslt = Arrays.asList(prefs.getValues(CALENDAR_WHITELIST_PREFERENCE, new String[0]));
        return rslt;
    }

}

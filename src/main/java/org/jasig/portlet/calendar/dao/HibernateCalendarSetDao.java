/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.calendar.dao;

import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class HibernateCalendarSetDao implements ICalendarSetDao {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

        final Set<UserDefinedCalendarConfiguration> calendars = new HashSet<UserDefinedCalendarConfiguration>();
        final CalendarSet<UserDefinedCalendarConfiguration> set
                = new CalendarSet<UserDefinedCalendarConfiguration>(calendars);

        final String username = getUsername(request);
        if (username != null) {

            // retrieve a list of calendar configurations for this user
            final List<UserDefinedCalendarConfiguration> cals = calendarStore
                .getCalendarConfigurations(username);

            calendars.addAll(cals);

        } else {
            log.warn("username is null -- returning empty calendar set");
        }
        return set;
    }

    @Override
    public List<PredefinedCalendarConfiguration> getAvailablePredefinedCalendarConfigurations(PortletRequest request) {

        final String username = getUsername(request);

        if (username != null) {
            // For this ICalendarSetDao, the list is the unfiltered collection from CalendarStore...
            return calendarStore.getPredefinedCalendarConfigurations(username, false);
        } else {
            log.warn("username is null -- returning empty calendar configuration list");
            return new ArrayList<PredefinedCalendarConfiguration>();
        }
    }

    /*
     * Implementation
     */

    private String getUsername(PortletRequest request) {
        // Get the username from the portlet session.  This implementation
        // assumes that the session initialization service is run at login time
        final PortletSession session = request.getPortletSession();
        final String rslt = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
        if (rslt == null) {
            log.warn("Username not found in session");
        }
        return rslt;
    }

}

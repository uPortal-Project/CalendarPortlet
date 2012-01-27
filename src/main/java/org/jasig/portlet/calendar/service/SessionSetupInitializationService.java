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

package org.jasig.portlet.calendar.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Required;

/**
 * SessionSetupInitializationService is a core implementation of 
 * IInitializationService that performs critical session initialization tasks
 * for the calendar portlet. 
 * 
 * This service initializes the following session variables:
 * - subscribeId
 * - userRoles
 * - isAdmin
 * - hiddenCalendars
 * - startDate 
 * - timezone
 * - days
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class SessionSetupInitializationService implements IInitializationService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public static final String USERNAME_KEY = "username";

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.service.IInitializationService#initialize(javax.portlet.PortletRequest)
	 */
	public void initialize(PortletRequest request) {

		PortletSession session = request.getPortletSession(true);
		
		/**
		 * Set the subscribe ID used for associating calendar data with a user
		 */
		
		String subscribeId = null;
		if(userToken == null || userToken.equalsIgnoreCase("")) {
			subscribeId = request.getRemoteUser();
    	}
    	else {
    		// get the credentials for this portlet from the UserInfo map
    		@SuppressWarnings("unchecked")
    		Map<String,String> userinfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);
    		subscribeId = (String) userinfo.get(userToken);    		
    	}
		
		// default to guest
		if (subscribeId == null) {
			subscribeId = "guest";
		}
		session.setAttribute(USERNAME_KEY, subscribeId);

		
		/**
		 * Set the list of calendar roles belonging to this user
		 */
		
		// get a set of all role names currently configured for
		// default calendars
		List<String> allRoles = calendarStore.getUserRoles();
		
		// determine which of the above roles the user belongs to
		// and store the resulting list in the session
		Set<String> userRoles = new HashSet<String>();
		for (String role : allRoles) {
			if (request.isUserInRole(role))
				userRoles.add(role);
		}
		session.setAttribute("userRoles", userRoles);
		
		
		/**
		 * Set whether this user is an admin
		 */
		
		// determine if this user belongs to the defined calendar
		// administration group and store the result in the session
		session.setAttribute("isAdmin", 
				request.isUserInRole("calendarAdmin"), PortletSession.APPLICATION_SCOPE);

		
		/**
		 *  Update the user's calendar subscriptions to include
		 *  any calendars that have been associated with his or 
		 *  her role
		 */
		
		calendarStore.initCalendar(subscribeId, userRoles);

		/**
		 * Create a list of hidden calendars for the session
		 */
		HashMap<Long, String> hiddenCalendars = new HashMap<Long, String>();
		session.setAttribute("hiddenCalendars", hiddenCalendars);

		/**
		 * Initialize the start date, timezone, and duration of the calendar
		 */
		
		// get the timezone
		PortletPreferences prefs = request.getPreferences();
		String timezone = prefs.getValue("timezone", "America/New_York");
		session.setAttribute("timezone", timezone);

		// set now as the starting date
		final DateMidnight start = new DateMidnight(DateTimeZone.forID(timezone));
		session.setAttribute("startDate", start);

		// set the default number of days to display
		// get days from preferences, or use the default if not found
		final String prefDays = prefs.getValue( "days", String.valueOf( defaultDays ) );
		final int tempDays = Integer.parseInt( prefDays );
		session.setAttribute("days", tempDays);
		
		// mark this session as initialized
		session.setAttribute("initialized", "true");
		
	}

	private CalendarStore calendarStore;
	
	@Required
	@Resource(name="calendarStore")
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

	private int defaultDays = 7;
	public void setDefaultDays(int defaultDays) {
		this.defaultDays = defaultDays;
	}
	
	private String userToken = null;
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

}

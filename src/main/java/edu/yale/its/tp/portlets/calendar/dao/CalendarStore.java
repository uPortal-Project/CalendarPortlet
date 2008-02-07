/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.dao;

import java.util.List;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarDefinition;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarConfiguration;

/**
 * CalendarStore provides a data store for calendar listings and configurations.
 *
 * @author Jen Bourey
 */
public interface CalendarStore {

	/**
	 * Save or update a calendar definition.
	 * 
	 * @param definition	CalendarDefinition to be persisted
	 */
	public void storeCalendarDefinition(CalendarDefinition definition);

	/**
	 * Retrieve a calendar definition.
	 * 
	 * @param id ID of the calendar definition to be retrieved
	 * @return
	 */
	public CalendarDefinition getCalendarDefinition(Long id);

	/**
	 * Save or update a calendar configuration.
	 * 
	 * @param configuration	CalendarConfiguration to be persisted
	 */
	public void storeCalendarConfiguration(CalendarConfiguration configuration);

	/**
	 * Retrieve a calendar configuration.
	 * 
	 * @param id ID of the calendar configuration to be retrieved
	 * @return
	 */
	public CalendarConfiguration getCalendarConfiguration(Long id);

	/**
	 * Retrieve a list of calendar configurations for the specified portlet.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @return
	 */
	public List<CalendarConfiguration> getCalendarConfigurations(
			String subscribeId);

	/**
	 * Retrieve a list of user-defined calendar configurations for 
	 * the specified portlet.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @param visibleOnly <code>true</code> to retrieve only non-hidden calendar
	 * 			configurations, <code>false</code> otherwise
	 * @return
	 */
	public List<UserDefinedCalendarConfiguration> getUserDefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly);

	/**
	 * Retrieve a list of pre-defined calendar configurations for 
	 * the specified portlet.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @param visibleOnly <code>true</code> to retrieve only non-hidden calendar
	 * 			configurations, <code>false</code> otherwise
	 * @return
	 */
	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly);

	/**
	 * Remove a calendar configuration from the data store
	 * 
	 * @param configuration configuration to be removed
	 */
	public void deleteCalendarConfiguration(CalendarConfiguration configuration);

	/**
	 * Initialize calendar subscriptions for a given portlet subscription and role.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @param role user role to use to find default calendars
	 */
	public void initCalendar(String subscribeId, String role);

	/**
	 * Retrieve a list of hidden predefined calendars for this portlet subscription
	 * and role.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @param role user role to use to find default calendars
	 * @return
	 */
	public List<PredefinedCalendarDefinition> getHiddenPredefinedCalendarDefinitions(
			String subscribeId, String role);

}


/*
 * CalendarStore.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
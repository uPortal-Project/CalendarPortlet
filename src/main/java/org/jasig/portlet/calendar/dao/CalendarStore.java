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

import java.util.List;
import java.util.Set;

import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarDefinition;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;


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
	
	public CalendarDefinition getCalendarDefinition(String fname);

	/**
	 * Retrieves all pre-defined calendar definition
	 * 
	 * @return
	 */
	public List<PredefinedCalendarDefinition> getPredefinedCalendarDefinitions();
	
	/**
	 * Retrieve a pre-defined calendar definition based on the name field.
	 * 
	 * @param name Name of the calendar definition to be retrieved
	 * @return
	 */
	public PredefinedCalendarDefinition getPredefinedCalendarDefinitionByName(String name);
	
	/**
	 * Retrieve a pre-defined calendar definition
	 * 
	 * @param id ID of the calendar definition to be retrieved
	 * @return
	 */
	public PredefinedCalendarDefinition getPredefinedCalendarDefinition(Long id);

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
	public List<UserDefinedCalendarConfiguration> getCalendarConfigurations(
			String subscribeId);

    /**
     * Retrieve a list of all user-defined calendar configurations.
     *
     * @param subscribeId Unique ID for this portlet subscription
     * @param name Name of the desired calendar
     * @return
     */
    public UserDefinedCalendarConfiguration getUserDefinedCalendarConfiguration(
            String subscribeId, String name);

	/**
	 * Retrieve a list of all user-defined calendar configurations.
	 *
	 * @return
	 */
	public List<UserDefinedCalendarConfiguration> getUserDefinedCalendarConfigurations();
	
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
     * Retrieve a <code>PredefinedCalendarConfiguration</code> for the specified 
     * subscribeId and name.
     * 
     * @param subscribeId unique ID for this portlet subscription
     * @param name Name of the corresponding <code>PredefinedCalendarDefinition</code>
     * @return
     */
    public PredefinedCalendarConfiguration getPredefinedCalendarConfiguration(
            String subscribeId, String name);

    /**
	 * Retrieve a list of all pre-defined calendar configurations.
	 * 
	 * @return
	 */
	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations();

	/**
	 * Remove a calendar configuration from the data store
	 * 
	 * @param configuration configuration to be removed
	 */
	public void deleteCalendarConfiguration(CalendarConfiguration configuration);
	
	/**
	 * Remove a calendar definition from the data store.
	 * 
	 * @param definition definition to be removed
	 */
	public void deleteCalendarDefinition(CalendarDefinition definition);

	/**
	 * Initialize calendar subscriptions for a given portlet subscription and role.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @param roles user roles to use to find default calendars
	 */
	public void initCalendar(String subscribeId, Set<String> roles);

	/**
	 * Retrieve a list of hidden predefined calendars for this portlet subscription
	 * and role.
	 * 
	 * @param subscribeId unique ID for this portlet subscription
	 * @param role user role to use to find default calendars
	 * @return
	 */
	public List<PredefinedCalendarDefinition> getHiddenPredefinedCalendarDefinitions(
			String subscribeId, Set<String> role);
	
	/**
	 * Get a list of all user roles currently in use.
	 * 
	 * @return
	 */
	public List<String> getUserRoles();

}

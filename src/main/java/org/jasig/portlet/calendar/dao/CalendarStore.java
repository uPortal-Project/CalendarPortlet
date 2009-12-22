/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
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

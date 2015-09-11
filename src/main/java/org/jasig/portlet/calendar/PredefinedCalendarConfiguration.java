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
package org.jasig.portlet.calendar;

import java.util.HashMap;
import java.util.Map;

/**
 * PredefinedCalendarConfiguration represents a user configuration of a built-in
 * calendar definition.  There may be many configurations for each predefined
 * calendar definition.  This class defines an extra place to put user-specific
 * configuration information and preferences.
 *
 * @author Jen Bourey
 */
public class PredefinedCalendarConfiguration extends CalendarConfiguration {

	private Map<String, String> preferences = new HashMap<String, String>();
	
	/**
	 * Default constructor
	 */
	public PredefinedCalendarConfiguration() {
		super();
		setCalendarDefinition(new PredefinedCalendarDefinition());
	}
	
	/**
	 * Get the user-specific preferences for this configuration.
	 * 
	 * @return
	 */
	public Map<String, String> getPreferences() {
		return preferences;
	}
	
	/**
	 * Set the user-specific preferences for this configuration.
	 * 
	 * @param preferences
	 */
	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
	
	/**
	 * Add a user preference for this configuration.
	 * 
	 * @param name		parameter name (key)
	 * @param value		value to be stored
	 */
	public void addPreference(String name, String value) {
		this.preferences.put(name, value);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.CalendarConfiguration#setCalendarDefinition(org.jasig.portlet.calendar.CalendarDefinition)
	 */
	@Override
	public void setCalendarDefinition(CalendarDefinition definition) {
	    if (!(definition instanceof PredefinedCalendarDefinition)) {
	        throw new IllegalArgumentException("Predefined calendar configurations may only point to a predefined calendar definition");
	    }
		super.setCalendarDefinition(definition);
	}
	
}

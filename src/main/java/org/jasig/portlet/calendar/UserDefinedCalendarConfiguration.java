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

package org.jasig.portlet.calendar;

/**
 * UserDefinedCalendarConfiguration represents a user-created calendar configuration.
 * There should only be one calendar configuration for each user-defined calendar
 * definition.  Since this is a one-to-one relationship, the 
 * UserDefinedCalendarConfiguration doesn't need to define an extra bucket for
 * user-specific configuration information.
 *
 * @author Jen Bourey
 */
public class UserDefinedCalendarConfiguration extends CalendarConfiguration {

	private UserDefinedCalendarDefinition calendarDefinition;

	/**
	 * Default Constructor
	 */
	public UserDefinedCalendarConfiguration() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.CalendarConfiguration#getCalendarDefinition()
	 */
	public UserDefinedCalendarDefinition getCalendarDefinition() {
		return calendarDefinition;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.CalendarConfiguration#setCalendarDefinition(org.jasig.portlet.calendar.CalendarDefinition)
	 */
	public void setCalendarDefinition(
			UserDefinedCalendarDefinition calendarDefinition) {
		this.calendarDefinition = calendarDefinition;
	}
	
}

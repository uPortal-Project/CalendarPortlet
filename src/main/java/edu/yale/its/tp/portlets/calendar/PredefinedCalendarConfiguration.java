/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PredefinedCalendarConfiguration represents a user configuration of a built-in
 * calendar definition.  There may be many configurations for each predefined
 * calendar definition.  This class defines an extra place to put user-specific
 * configuration information and preferences.
 *
 * @author Jen Bourey
 */
public class PredefinedCalendarConfiguration extends CalendarConfiguration {

	private static Log log = LogFactory.getLog(PredefinedCalendarConfiguration.class);

	private PredefinedCalendarDefinition calendarDefinition;
	private Map<String, String> preferences = new HashMap<String, String>();
	
	/**
	 * Default constructor
	 */
	public PredefinedCalendarConfiguration() {
		super();
		this.calendarDefinition = new PredefinedCalendarDefinition();
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
	 * @see edu.yale.its.tp.portlets.calendar.CalendarConfiguration#getCalendarDefinition()
	 */
	public PredefinedCalendarDefinition getCalendarDefinition() {
		return calendarDefinition;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.CalendarConfiguration#setCalendarDefinition(edu.yale.its.tp.portlets.calendar.CalendarDefinition)
	 */
	public void setCalendarDefinition(PredefinedCalendarDefinition definition) {
		this.calendarDefinition = definition;
	}
	
}

/*
 * PredefinedCalendarConfiguration.java
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
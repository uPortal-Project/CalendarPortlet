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

import java.util.HashMap;
import java.util.Map;

/**
 * CalendarDefinition represents the base class for calendar registrations.  
 * Information required to retrieve the calendar, such as the calendar's URL
 * or important default system-wide configuration information may be stored 
 * in the parameters map.  In order to add this calendar for a specific user,
 * a CalendarConfiguration referencing this calendar definition must be 
 * created.
 * 
 * @author Jen Bourey
 */
public class CalendarDefinition {

	private Long id = new Long(-1);
	private String className;
	private String name;
	private Map<String, String> parameters = new HashMap<String, String>();
	
	/**
	 * Default constructor.
	 */
	public CalendarDefinition() {
		super();
	}
	
	public CalendarDefinition(Long id, String className, String name) {
		this.id = id;
		this.className = className;
		this.name = name;
	}
	
	/**
	 * Return the unique id of this calendar.
	 * 
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Set the unique id for this calendar.
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the adapter class for this calendar which will
	 * determine how the calendar is retrieved.  This id must match a 
	 * calendar adapter registered in the spring context files.
	 * 
	 * @return
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Set the name of the adapter class for this calendar which will
	 * determine how the calendar is retrieved.  This id must match a
	 * calendar adapter registered in the spring context files.
	 * 
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Return the display name for this calendar.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the display name for this calendar.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return the map of calendar parameters.  These parameters can hold
	 * any extra information needed by the particular adapter used by
	 * this calendar, such as a URL. 
	 * 
	 * @return parameter map
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Set the map of calendar parameters.  These parameters can hold
	 * any extra information needed by the particular adapter used by
	 * this calendar, such as a URL.
	 * 
	 * @param parameters parameter map
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Add an individual calendar parameter.  These parameters can hold
	 * any extra information needed by the particular adapter used by
	 * this calendar, such as a URL.
	 * 
	 * @param name		parameter name (key)
	 * @param value		value to be stored
	 */
	public void addParameter(String name, String value) {
		this.parameters.put(name, value);
	}
	
	@Override
	public String toString() {
		return "id: " + this.id + ", class: " + this.className + ", name: " + this.name;
	}
	

	
}

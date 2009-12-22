/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
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

/*
 * CalendarDefinition.java
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
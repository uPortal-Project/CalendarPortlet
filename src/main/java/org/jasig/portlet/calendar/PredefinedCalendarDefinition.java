/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar;

import java.util.HashSet;
import java.util.Set;

/**
 * PredefinedCalendarDefinition represents a built-in calendar definition.  These
 * definitions are defined in the database by an administrator, and can be 
 * automatically added to user's calendar registration lists based on user
 * role.
 *
 * @author Jen Bourey
 */
public class PredefinedCalendarDefinition extends CalendarDefinition {
	
	private Set<PredefinedCalendarConfiguration> userConfigurations = new HashSet<PredefinedCalendarConfiguration>();
	private Set<String> defaultRoles;
	
	/**
	 * Default constructor
	 */
	public PredefinedCalendarDefinition() {
		super();
	}
	
	/**
	 * Construct a new predefined calendar definition
	 * 
	 * @param id
	 * @param className
	 * @param name
	 */
	public PredefinedCalendarDefinition(Long id, String className, String name) {
		super(id, className, name);
	}
	
	/**
	 * Get all the user configurations for this calendar definition
	 * 
	 * @return
	 */
	public Set<PredefinedCalendarConfiguration> getUserConfigurations() {
		return userConfigurations;
	}
	
	/**
	 * Set the user configurations for this calendar definition
	 * 
	 * @param configurations
	 */
	public void setUserConfigurations(Set<PredefinedCalendarConfiguration> configurations) {
		this.userConfigurations = configurations;
	}
	
	/**
	 * Get the set of user roles who should get this calendar by default.
	 * 
	 * @return set of default user roles
	 */
	public Set<String> getDefaultRoles() {
		return defaultRoles;
	}
	
	/**
	 * Set the user roles should should get this calendar by default.
	 * 
	 * @param defaultRoles	set of default user roles
	 */
	public void setDefaultRoles(Set<String> defaultRoles) {
		this.defaultRoles = defaultRoles;
	}
	
	/**
	 * Add a user configuration for this calendar definition
	 * 
	 * @param config
	 */
	public void addUserConfiguration(PredefinedCalendarConfiguration config) {
		this.userConfigurations.add(config);
	}
	
	/**
	 * Add a user role to the set of roles that should get this calendar
	 * by default.
	 * 
	 * @param role	user role to be added
	 */
	public void addDefaultRole(String role) {
		this.defaultRoles.add(role);
	}
	
}

/*
 * PredefinedCalendarDefinition.java
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
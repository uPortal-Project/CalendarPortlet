/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar;

import java.util.HashSet;
import java.util.Set;

public class PredefinedCalendarDefinition extends CalendarDefinition {
	
	private Set<PredefinedCalendarConfiguration> userConfigurations = new HashSet<PredefinedCalendarConfiguration>();
	private Set<String> defaultRoles;
	
	public PredefinedCalendarDefinition() {
		super();
	}
	
	public PredefinedCalendarDefinition(Long id, String className, String name) {
		super(id, className, name);
	}
	
	public Set<PredefinedCalendarConfiguration> getUserConfigurations() {
		return userConfigurations;
	}
	public void setUserConfigurations(Set<PredefinedCalendarConfiguration> config) {
		this.userConfigurations = config;
	}
	public Set<String> getDefaultRoles() {
		return defaultRoles;
	}
	public void setDefaultRoles(Set<String> defaultRoles) {
		this.defaultRoles = defaultRoles;
	}
	
	public void addUserConfiguration(PredefinedCalendarConfiguration config) {
		this.userConfigurations.add(config);
	}
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
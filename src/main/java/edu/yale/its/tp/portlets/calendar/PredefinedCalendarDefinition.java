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

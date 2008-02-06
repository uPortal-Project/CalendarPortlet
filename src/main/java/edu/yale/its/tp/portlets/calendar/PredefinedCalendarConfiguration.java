package edu.yale.its.tp.portlets.calendar;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.dao.HibernateCalendarStore;

public class PredefinedCalendarConfiguration extends CalendarConfiguration {

	private static Log log = LogFactory.getLog(PredefinedCalendarConfiguration.class);

	private PredefinedCalendarDefinition calendarDefinition;
	private Map<String, String> preferences = new HashMap<String, String>();
	
	public PredefinedCalendarConfiguration() {
		super();
		this.calendarDefinition = new PredefinedCalendarDefinition();
	}
	
	public Map<String, String> getPreferences() {
		return preferences;
	}
	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
	
	public void addPreference(String name, String value) {
		this.preferences.put(name, value);
	}
	public PredefinedCalendarDefinition getCalendarDefinition() {
		return calendarDefinition;
	}
	public void setCalendarDefinition(PredefinedCalendarDefinition definition) {
		this.calendarDefinition = definition;
	}
	
}


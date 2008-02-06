package edu.yale.its.tp.portlets.calendar.dao;

import java.util.List;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarDefinition;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarConfiguration;

public interface CalendarStore {

	/**
	 * Save or update a calendar definition.
	 * 
	 * @param definition	CalendarDefinition to be persisted
	 */
	public void storeCalendarDefinition(CalendarDefinition definition);

	public List<CalendarDefinition> getCalendarDefinitions(String subscribeId);

	public CalendarDefinition getCalendarDefinition(Long id);

	public void storeCalendarConfiguration(CalendarConfiguration configuration);

	public CalendarConfiguration getCalendarConfiguration(Long id);

	public List<CalendarConfiguration> getCalendarConfigurations(
			String subscribeId);

	public List<UserDefinedCalendarConfiguration> getUserDefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly);

	public List<PredefinedCalendarConfiguration> getPredefinedCalendarConfigurations(
			String subscribeId, boolean visibleOnly);

	public void deleteCalendarConfiguration(CalendarConfiguration configuration);

	public void initCalendar(String subscribeId, String role);

	public List<PredefinedCalendarDefinition> getHiddenPredefinedCalendarDefinitions(
			String subscribeId, String role);

}

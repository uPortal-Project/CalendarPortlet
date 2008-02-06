package edu.yale.its.tp.portlets.calendar;

public class UserDefinedCalendarConfiguration extends CalendarConfiguration {

	private UserDefinedCalendarDefinition calendarDefinition;
	
	public UserDefinedCalendarConfiguration() {
		super();
	}

	public UserDefinedCalendarDefinition getCalendarDefinition() {
		return calendarDefinition;
	}

	public void setCalendarDefinition(
			UserDefinedCalendarDefinition calendarDefinition) {
		this.calendarDefinition = calendarDefinition;
	}
	
}

package edu.yale.its.tp.portlets.calendar.mvc;

public class CalendarPreferences {
	
	private String showCalendar;
	private String[] calendarUrls;
	
	public CalendarPreferences() { }
	
	public String[] getCalendarUrls() {
		return calendarUrls;
	}
	public void setCalendarUrls(String[] calendarUrls) {
		this.calendarUrls = calendarUrls;
	}
	public String getShowCalendar() {
		return showCalendar;
	}
	public void setShowCalendar(String showCalendar) {
		this.showCalendar = showCalendar;
	}
	
	

}

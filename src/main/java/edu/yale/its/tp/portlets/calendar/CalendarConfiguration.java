package edu.yale.its.tp.portlets.calendar;



public class CalendarConfiguration {
	
	private Long id = new Long(-1);
	private CalendarDefinition calendarDefinition;
	private boolean displayed = true;
	private String subscribeId;
	
	public boolean isDisplayed() {
		return displayed;
	}
	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}
	public String getSubscribeId() {
		return subscribeId;
	}
	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public CalendarDefinition getCalendarDefinition() {
		return calendarDefinition;
	}
	public void setCalendarDefinition(CalendarDefinition definition) {
		this.calendarDefinition = definition;
	}
	
}

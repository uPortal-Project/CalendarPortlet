package edu.yale.its.tp.portlets.calendar.mvc;

public class YaleEventPreferences {

	private String[] categories;
	private String days;
	
	public YaleEventPreferences() { }

	public String[] getCategories() {
		return categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}
	
}

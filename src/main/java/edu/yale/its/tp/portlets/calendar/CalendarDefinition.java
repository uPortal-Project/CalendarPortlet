package edu.yale.its.tp.portlets.calendar;

import java.util.HashMap;
import java.util.Map;

/**
 * CalendarDefinition represents the base class for calendar registrations.
 * 
 * @author Jen Bourey
 */
public class CalendarDefinition {

	private Long id = new Long(-1);
	private String className;
	private String name;
	private Map<String, String> parameters = new HashMap<String, String>();
	
	public CalendarDefinition() {
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
	 * Returns the name of the adapter class for this calendar.
	 * 
	 * @return
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Set the name of the adapter class for this calendar.  The adapter will
	 * determine how the calendar is retrieved.
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
	 * @return
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Set the map of calendar parameters.  These parameters can hold
	 * any extra information needed by the particular adapter used by
	 * this calendar, such as a URL.
	 * 
	 * @param parameters
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
	
	public String toString() {
		return "id: " + this.id + ", class: " + this.className + ", name: " + this.name;
	}
	

	
}

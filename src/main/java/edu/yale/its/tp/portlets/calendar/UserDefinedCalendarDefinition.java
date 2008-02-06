package edu.yale.its.tp.portlets.calendar;

public class UserDefinedCalendarDefinition extends CalendarDefinition{

	private UserDefinedCalendarConfiguration userConfiguration;

	public UserDefinedCalendarDefinition() {
		super();
	}

	public UserDefinedCalendarDefinition(Long id, String className, String name) {
		super(id, className, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "id: " + getId() + ", name: " + getName() + ", parameters: "
				+ getParameters().toString();
	}

	public UserDefinedCalendarConfiguration getUserConfiguration() {
		return userConfiguration;
	}

	public void setUserConfiguration(
			UserDefinedCalendarConfiguration userConfiguration) {
		this.userConfiguration = userConfiguration;
	}

}

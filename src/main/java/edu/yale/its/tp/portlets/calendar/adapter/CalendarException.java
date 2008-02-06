package edu.yale.its.tp.portlets.calendar.adapter;

public class CalendarException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CalendarException() {
		super();
	}

	public CalendarException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalendarException(String message) {
		super(message);
	}

	public CalendarException(Throwable cause) {
		super(cause);
	}

}

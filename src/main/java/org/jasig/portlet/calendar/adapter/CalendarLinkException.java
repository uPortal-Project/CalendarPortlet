package org.jasig.portlet.calendar.adapter;

/**
 * CalendarLinkException represents an exception related to 
 * calendar hyper links.
 *
 * @author Anthony Colebourne
 */
public class CalendarLinkException extends CalendarException {
	private static final long serialVersionUID = 2L;

	public CalendarLinkException() {
		super();
	}

	public CalendarLinkException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalendarLinkException(String message) {
		super(message);
	}

	public CalendarLinkException(Throwable cause) {
		super(cause);
	}
}

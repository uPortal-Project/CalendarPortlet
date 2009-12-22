/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.adapter;

/**
 * CalendarException represents a generic Calendar exception.
 *
 * @author Jen Bourey
 */
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

 /*******************************************************************************
* Copyright 2008, The Board of Regents of the University of Wisconsin System.
* All rights reserved.
*
* A non-exclusive worldwide royalty-free license is granted for this Software.
* Permission to use, copy, modify, and distribute this Software and its
* documentation, with or without modification, for any purpose is granted
* provided that such redistribution and use in source and binary forms, with or
* without modification meets the following conditions:
*
* 1. Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
* acknowledgement:
*
* "This product includes software developed by The Board of Regents of
* the University of Wisconsin System.
*
*THIS SOFTWARE IS PROVIDED BY THE BOARD OF REGENTS OF THE UNIVERSITY OF
*WISCONSIN SYSTEM "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
*BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
*PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE BOARD OF REGENTS OF
*THE UNIVERSITY OF WISCONSIN SYSTEM BE LIABLE FOR ANY DIRECT, INDIRECT,
*INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
*LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
*OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
*ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
package org.jasig.portlet.calendar.adapter;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarEvent;

import net.fortuna.ical4j.model.Period;

/**
 * Similar to {@link ICalendarAdapter}, interface defining methods for 
 * retrieving a single {@link CalendarEvent} (rather than a {@link Set}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: SingleEventSupport.java Exp $
 */
public interface ISingleEventSupport {

	/**
	 * 
	 * @param calendar
	 * @param period
	 * @param uid
	 * @param recurrenceId
	 * @param request
	 * @return
	 * @throws CalendarException
	 */
	CalendarEvent getEvent(CalendarConfiguration calendar, Period period, 
			String uid, String recurrenceId, HttpServletRequest request) throws CalendarException;
	
	/**
	 * 
	 * @param calendar
	 * @param period
	 * @param uid
	 * @param recurrenceId
	 * @param request
	 * @return
	 * @throws CalendarException
	 */
	CalendarEvent getEvent(CalendarConfiguration calendar, Period period, 
			String uid, String recurrenceId, PortletRequest request) throws CalendarException;
}

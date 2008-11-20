/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.adapter;

import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Period;
import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;

/**
 * ICalendarAdapter defines an interface for retrieving calendar event data.
 * All new calendar types must define an adapter using this interface, then be 
 * registered in the spring context files.
 *
 * @author Jen Bourey
 */
public interface ICalendarAdapter {

	/**
	 * Get events for the defined calendar and time period.  The user's 
	 * PortletRequest is made available to give the calendar adapter access
	 * to useful information such as the UserInfo map, session data, etc.
	 * These items can be used to identify the user, provide access to 
	 * authentication resources, or other useful operations.
	 * 
	 * @param calendar calendar configuration for which to retrieve events
	 * @param period time period for which to retrieve events
	 * @param request user's portlet request
	 * @return Set of events for this calendar and time period
	 * @throws CalendarException
	 */
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,
			Period period, PortletRequest request) throws CalendarException;

	/**
	 * Get events for the defined calendar and time period.  The user's 
	 * HttpServletRequest is made available to give the calendar adapter access
	 * to useful information such as the request parameters, session data, etc.
	 * These items can be used to identify the user, provide access to 
	 * authentication resources, or other useful operations.
	 *  
	 * @param calendar calendar configuration for which to retrieve events
	 * @param period time period for which to retrieve events
	 * @param request user's servlet request
	 * @return Set of events for this calendar and time period
	 * @throws CalendarException
	 */
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendar,
			Period period, HttpServletRequest request) throws CalendarException;

	/**
	 * Get hyper link for the defined calendar. In cases where the calendar resource
	 * has a web interface, this method allows provides access to the url.
	 *  
	 * @param calendar calendar configuration for which to retrieve events
	 * @param period time period for which to retrieve events
	 * @param request user's servlet request
	 * @return Set of events for this calendar and time period
	 * @throws CalendarException
	 */
	public String getLink(CalendarConfiguration calendar,
			Period period, PortletRequest request) throws CalendarLinkException;
	
}


/*
 * ICalendarAdapter.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
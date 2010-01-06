/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

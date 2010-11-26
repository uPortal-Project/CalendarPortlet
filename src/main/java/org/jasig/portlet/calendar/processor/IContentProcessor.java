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

package org.jasig.portlet.calendar.processor;

import java.io.InputStream;
import java.util.Set;

import org.jasig.portlet.calendar.CalendarEvent;

import net.fortuna.ical4j.model.Period;

/**
 * This interface defines a mechanism for converting an
 * {@link InputStream} into a {@link Set} of {@link CalendarEvent}s.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: ContentProcessor.java Exp $
 */
public interface IContentProcessor {

	/**
	 * Implementations should not return null (at a minimum return
	 * Collections.emptySet()).
	 * 
	 * @param calendarId
	 * @param period
	 * @param in
	 * @return
	 */
	Set<CalendarEvent> getEvents(Long calendarId, Period period, InputStream in);
	
}

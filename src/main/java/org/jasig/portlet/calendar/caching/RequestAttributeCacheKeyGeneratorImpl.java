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

package org.jasig.portlet.calendar.caching;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portlet.calendar.CalendarConfiguration;

import net.fortuna.ical4j.model.Period;

/**
 * RequestAttributeCacheKeyGeneratorImpl is an implementation of ICacheKeyGenerator
 * that appends the current session username to the generated key.  This implementation
 * requires the username to be added to the session on the user's first login and must
 * therefore by used with one of the initialization services.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: RequestAttributeCacheKeyGenerator.java Exp $
 */
public class RequestAttributeCacheKeyGeneratorImpl implements ICacheKeyGenerator {

	public String getKey(CalendarConfiguration configuration,
			Period period, HttpServletRequest request, String calendarIdentifier) {
		StringBuffer key = new StringBuffer();
		// use the unique calendar identifier as the base of the key
		key.append(calendarIdentifier);
		key.append(".");
		// add the period start and end dates to the key
		key.append(period.getStart().toString());
		key.append(period.getEnd().toString());
		// add the username to the key
		key.append((String) request.getSession().getAttribute("username"));
		return key.toString();
	}

	public String getKey(CalendarConfiguration configuration,
			Period period, PortletRequest request, String calendarIdentifier) {
		StringBuffer key = new StringBuffer();
		// use the unique calendar identifier as the base of the key
		key.append(calendarIdentifier);
		key.append(".");
		// add the period start and end dates to the key
		key.append(period.getStart().toString());
		key.append(period.getEnd().toString());
		// add the username to the key
		key.append((String) request.getPortletSession().getAttribute("username"));
		return key.toString();
	}

}

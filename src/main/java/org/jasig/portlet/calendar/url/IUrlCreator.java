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

package org.jasig.portlet.calendar.url;

import javax.portlet.PortletRequest;

import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.ConfigurableHttpCalendarAdapter;
import org.joda.time.Interval;

/**
 * This interface defines operations for constructing the URL to be retrieved
 * by the {@link ConfigurableHttpCalendarAdapter}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: UrlCreator.java Exp $
 */
public interface IUrlCreator {

	/**
	 * 
	 * @param configuration
	 * @param period
	 * @param request
	 * @return
	 */
	String constructUrl(CalendarConfiguration configuration,
			Interval interval, PortletRequest request);
	
}

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

package org.jasig.portlet.calendar.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

/**
 * ViewSelectorDefaultImpl provides a default implementation of IViewSelector
 * that returns JSP view names based on a combination of the browser user agent
 * string and the portlet window state.  Requests which indicate that the user
 * is interacting with the portlet via a mobile device, or that the portlet is
 * currently not in maximized mode will result in a "narrow" view of the 
 * calendar.  Non-mobile devices using the portlet in the maximized window state
 * will be shown the "wide" view of the portlet.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class ViewSelectorDefaultImpl implements IViewSelector {
	
	private final String CALENDAR_WIDE_VIEW = "calendarWideView";
	private final String CALENDAR_NARROW_VIEW = "calendarNarrowView";
	private final String CALENDAR_MOBILE_VIEW = "calendarMobileView";
	
	private List<Pattern> mobileDeviceRegexes = null;
	
	/**
	 * Set a list of regex patterns for user agents which should be considered
	 * to be mobile devices.
	 * 
	 * @param patterns
	 */
	@Resource(name="mobileDeviceRegexes")
	public void setMobileDeviceRegexes(List<String> patterns) {
		this.mobileDeviceRegexes = new ArrayList<Pattern>();
		for (String pattern : patterns) {
			this.mobileDeviceRegexes.add(Pattern.compile(pattern));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.mvc.IViewSelector#getCalendarViewName(javax.portlet.PortletRequest)
	 */
	public String getCalendarViewName(PortletRequest request) {
		String userAgent = request.getProperty("user-agent");
		
		// check to see if this is a mobile device
		if (this.mobileDeviceRegexes != null && userAgent != null) {
			for (Pattern regex : this.mobileDeviceRegexes) {
				if (regex.matcher(userAgent).matches()) {
					return CALENDAR_MOBILE_VIEW;
				}
			}
		}
		
		// otherwise check the portlet window state
		WindowState state = request.getWindowState();
        if (WindowState.MAXIMIZED.equals(state) || "DETACHED".equalsIgnoreCase(state.toString())) {
			return CALENDAR_WIDE_VIEW;
		} else {
			return CALENDAR_NARROW_VIEW;
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.mvc.IViewSelector#getEventListViewName(javax.portlet.PortletRequest)
	 */
	public String getEventListViewName(PortletRequest request) {
		return "ajaxEventList";
	}

}

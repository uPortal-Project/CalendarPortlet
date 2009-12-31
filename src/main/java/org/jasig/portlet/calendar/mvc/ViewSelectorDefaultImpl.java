package org.jasig.portlet.calendar.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

public class ViewSelectorDefaultImpl implements IViewSelector {
	
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
				regex.matcher(userAgent).matches();
			}
		}
		
		// otherwise check the portlet window state
		WindowState state = request.getWindowState();
		if (WindowState.MAXIMIZED.equals(state)) {
			return "calendarWideView";
		} else {
			return "calendarNarrowView";
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

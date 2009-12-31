package org.jasig.portlet.calendar.mvc;

import javax.portlet.PortletRequest;

public interface IViewSelector {
	
	public String getCalendarViewName(PortletRequest request);

	public String getEventListViewName(PortletRequest request);

}

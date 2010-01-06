package org.jasig.portlet.calendar.mvc;

import javax.portlet.PortletRequest;

/**
 * IViewSelector assists the Calendar Portlet in choosing appropriate JSP views
 * for the main calendar view.  Views may be chosen based on variables like the 
 * portlet window state or the browser's user agent string.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IViewSelector {
	
	/**
	 * Return the JSP view name of the main calendar view for this request.
	 * 
	 * @param request
	 * @return
	 */
	public String getCalendarViewName(PortletRequest request);

	/**
	 * Return the JSP view name of the AJAX event list for this request.
	 * 
	 * @param request
	 * @return
	 */
	public String getEventListViewName(PortletRequest request);

}

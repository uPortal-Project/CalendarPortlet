package org.jasig.portlet.calendar.caching;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portlet.calendar.CalendarConfiguration;

import net.fortuna.ical4j.model.Period;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: ICacheKeyGenerator.java Exp $
 */
public interface ICacheKeyGenerator {

	/**
	 * Returns a cache key for the calendar.
	 * 
	 * @param configuration
	 * @param period
	 * @param request
	 * @param calendarIdentifier
	 * @return
	 */
	public String getKey(CalendarConfiguration configuration,
			Period period, HttpServletRequest request, String calendarIdentifier);

	/**
	 * Returns a cache key for the calendar.
	 * 
	 * @param configuration
	 * @param period
	 * @param request
	 * @param calendarIdentifier
	 * @return
	 */
	public String getKey(CalendarConfiguration configuration,
			Period period, PortletRequest request, String calendarIdentifier);

}
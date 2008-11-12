package edu.yale.its.tp.portlets.calendar.caching;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Period;
import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;

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
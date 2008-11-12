package edu.yale.its.tp.portlets.calendar.caching;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Period;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: DefaultCacheKeyGenerator.java Exp $
 */
public class DefaultCacheKeyGeneratorImpl implements ICacheKeyGenerator {

	public String getKey(CalendarConfiguration configuration,
			Period period, HttpServletRequest request, String calendarIdentifier) {
		StringBuffer key = new StringBuffer();
		key.append(calendarIdentifier);
		key.append(".");
		key.append(period.getStart().toString());
		key.append(period.getEnd().toString());
		return key.toString();
	}

	public String getKey(CalendarConfiguration configuration,
			Period period, PortletRequest request, String calendarIdentifier) {
		StringBuffer key = new StringBuffer();
		key.append(calendarIdentifier);
		key.append(period.getStart().toString());
		key.append(period.getEnd().toString());
		return key.toString();
	}

}

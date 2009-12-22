package org.jasig.portlet.calendar.url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.fortuna.ical4j.model.Period;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarException;


/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: StringTemplateUrlCreatorImpl.java Exp $
 */
public class StringTemplateUrlCreatorImpl implements IUrlCreator {

	private Log log = LogFactory.getLog(this.getClass());

	private final String USERNAME_TOKEN = "@USERNAME@";
	private final String START_DATE_TOKEN = "@STARTDATE@";
	private final String END_DATE_TOKEN = "@ENDDATE@";

	private final String URL_ENCODING = "UTF-8";
	private final String DEFAULT_DATE_FORMAT = "yyyyMMdd";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jasig.portlet.calendar.url.IUrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration,
	 *      net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
	 */
	public String constructUrl(CalendarConfiguration configuration,
			Period period, PortletRequest request) {

		// get the current username from the session
		PortletSession session = request.getPortletSession();
		if (session == null) {
			throw new CalendarException();
		}
		String username = (String) session.getAttribute("username");

		return constructUrlInternal(configuration, period, username);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jasig.portlet.calendar.url.IUrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration,
	 *      net.fortuna.ical4j.model.Period,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	public String constructUrl(CalendarConfiguration configuration,
			Period period, HttpServletRequest request) {

		// get the current username from the session
		HttpSession session = request.getSession();
		if (session == null) {
			throw new CalendarException();
		}
		String username = (String) session.getAttribute("username");

		return constructUrlInternal(configuration, period, username);
	}

	/**
	 * 
	 * @param configuration
	 * @param period
	 * @param username
	 * @return
	 */
	public String constructUrlInternal(CalendarConfiguration configuration,
			Period period, String username) {

		// get the template url from the calendar configuration
		String url = (String) configuration.getCalendarDefinition()
				.getParameters().get("url");

		try {

			// replace the username in the url
			url = url.replace(USERNAME_TOKEN, URLEncoder.encode(username,
					URL_ENCODING));

			// replace the start and end dates in the url, using the configured
			// date format
			if (url.contains(START_DATE_TOKEN) || url.contains(END_DATE_TOKEN)) {

				// get the configured date format from the calendar
				// configuration, or if none is configured, use the
				// default date format
				String urlDateFormat = (String) configuration
						.getCalendarDefinition().getParameters().get(
								"urlDateFormat");
				if (urlDateFormat == null) {
					urlDateFormat = DEFAULT_DATE_FORMAT;
				}

				// replace the start date in the url
				String startString = URLEncoder.encode(DateFormatUtils.format(
						period.getStart().getTime(), urlDateFormat),
						URL_ENCODING);
				url = url.replace(START_DATE_TOKEN, startString);
				
				// replace the end date in the url
				String endString = URLEncoder
						.encode(DateFormatUtils.format(period.getEnd()
								.getTime(), urlDateFormat), URL_ENCODING);
				url = url.replace(END_DATE_TOKEN, endString);
				
			}

		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}

		return url;
	}

}

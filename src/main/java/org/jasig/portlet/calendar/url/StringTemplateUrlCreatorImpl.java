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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;


/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: StringTemplateUrlCreatorImpl.java Exp $
 */
public class StringTemplateUrlCreatorImpl implements IUrlCreator {

	protected final Log log = LogFactory.getLog(this.getClass());

	private final String USERNAME_TOKEN = "@USERNAME@";
	private final String START_DATE_TOKEN = "@STARTDATE@";
	private final String END_DATE_TOKEN = "@ENDDATE@";

	private final String URL_ENCODING = "UTF-8";
	private final String DEFAULT_DATE_FORMAT = "yyyyMMdd";

    private Map<String, DateTimeFormatter> dateFormatters = new ConcurrentHashMap<String, DateTimeFormatter>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jasig.portlet.calendar.url.IUrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration,
	 *      net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
	 */
	public String constructUrl(CalendarConfiguration configuration,
			Interval interval, PortletRequest request) {

		// get the current username from the session
		PortletSession session = request.getPortletSession();
		if (session == null) {
			throw new CalendarException();
		}
		String username = (String) session.getAttribute("username");

		return constructUrlInternal(configuration, interval, username);
	}

	/**
	 * 
	 * @param configuration
	 * @param period
	 * @param username
	 * @return
	 */
	public String constructUrlInternal(CalendarConfiguration configuration,
			Interval interval, String username) {

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
                String startString = URLEncoder.encode(
                        getDateFormatter(urlDateFormat).print(
                                interval.getStart()), URL_ENCODING);
				url = url.replace(START_DATE_TOKEN, startString);
				
				// replace the end date in the url
                String endString = URLEncoder.encode(
                        getDateFormatter(urlDateFormat)
                                .print(interval.getEnd()), URL_ENCODING);
				url = url.replace(END_DATE_TOKEN, endString);
				
			}

		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}

		return url;
	}

    protected DateTimeFormatter getDateFormatter(String format) {
        if (this.dateFormatters.containsKey(format)) {
            return this.dateFormatters.get(format);
        } else {
            DateTimeFormatter df = new DateTimeFormatterBuilder()
                    .appendPattern(format).toFormatter();
            this.dateFormatters.put(format, df);
            return df;
        }
    }

}

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
package org.jasig.portlet.calendar.mvc.controller;

import java.io.IOException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.adapter.CalendarLinkException;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.IViewSelector;
import org.jasig.portlet.calendar.service.IInitializationService;
import org.jasig.portlet.calendar.service.SessionSetupInitializationService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

@Controller
@RequestMapping("VIEW")
public class CalendarController implements ApplicationContextAware {
    
    public static final String PREFERENCE_DISABLE_PREFERENCES = "disablePreferences";
    public static final String PREFERENCE_DISABLE_ADMINISTRATION = "disableAdministration";

	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping()
	public ModelAndView getCalendar(PortletRequest request) {

		
		/**
		 * If this is a new session, perform any necessary 
		 * portlet initialization.
		 */

		PortletSession session = request.getPortletSession(true);
		if (session.getAttribute("initialized") == null) {
			
			// perform any other configured initialization tasks
			for (IInitializationService service : initializationServices) {
				service.initialize(request);
			}
		}

        PortletPreferences prefs = request.getPreferences();

		Map<String, Object> model = new HashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		
		// get the list of hidden calendars
		@SuppressWarnings("unchecked")
		HashMap<Long, String> hiddenCalendars = (HashMap<Long, String>) session
				.getAttribute("hiddenCalendars");
		String username = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);

		if ("guest".equalsIgnoreCase(username)) {
			model.put("guest", true);
		} else {
			model.put("guest", false);
		}
		
		/**
		 * Add and remove calendars from the hidden list.  Hidden calendars
		 * will be fetched, but rendered invisible in the view.
		 */

		// check the request parameters to see if we need to add any
		// calendars to the list of hidden calendars
		String hideCalendar = request.getParameter("hideCalendar");
		if (hideCalendar != null) {
			hiddenCalendars.put(Long.valueOf(hideCalendar), "true");
			session.setAttribute("hiddenCalendars", hiddenCalendars);
		}

		// check the request parameters to see if we need to remove
		// any calendars from the list of hidden calendars
		String showCalendar = request.getParameter("showCalendar");
		if (showCalendar != null) {
			hiddenCalendars.remove(Long.valueOf(showCalendar));
			session.setAttribute("hiddenCalendars", hiddenCalendars);
		}

		/**
		 * Find our desired starting and ending dates.
		 */

		//StartDate can only be changed via an AJAX request
		Date startDate = (Date) session.getAttribute("startDate");
		log.debug("startDate from session is: "+startDate);
		cal.setTime(startDate);
		model.put("startDate", startDate);

		// find how many days into the future we should display events
		int days = (Integer) session.getAttribute("days");
		//check whether the number of days has been changed in this request
		String timePeriod = (String) request.getParameter("timePeriod");
		if (timePeriod != null && !timePeriod.equals("")) {
			try {
				days = Integer.parseInt(timePeriod);
				session.setAttribute("days", days);

				if ( prefs.isReadOnly( "days" ) == false ) {
					prefs.setValue( "days", Integer.toString( days ) );
					prefs.store();
				}
			} catch (NumberFormatException ex) {
				log.warn("Failed to parse desired time period", ex);
			} catch (ReadOnlyException ex) {
				log.error("Failed to set 'days' preference because it is read only", ex);
			} catch (IOException ex) {
				log.warn("Failed to store the 'days' preference", ex);
			} catch (ValidatorException ex) {
				log.warn("Failed to store the 'days' preference", ex);
			}
		}
		model.put("days", days);

		// set the end date based on our desired time period
		cal.add(Calendar.DATE, days);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date endDate = cal.getTime();
		model.put("endDate", endDate);

		Period period = new Period(new DateTime(startDate), new DateTime(
				endDate));

		// define "today" and "tomorrow" so we can display these specially in the
		// user interface
		cal = Calendar.getInstance();
		model.put("today", cal.getTime());
		cal.add(Calendar.DATE, 1);
		model.put("tomorrow", cal.getTime());

		/**
		 * retrieve the calendars defined for this portlet instance
		 */
		
        CalendarSet<?> set = calendarSetDao.getCalendarSet(request);
        Collection<? extends CalendarConfiguration> calendars = set.getConfigurations();
		model.put("calendars", calendars);

		Map<Long, Integer> colors = new HashMap<Long, Integer>();
		Map<Long, String> links = new HashMap<Long, String>();
		int index = 0;
		for (CalendarConfiguration callisting : calendars) {

			// don't bother to fetch hidden calendars
			if (hiddenCalendars.get(callisting.getId()) == null) {

				try {
	
					// get an instance of the adapter for this calendar
					ICalendarAdapter adapter = (ICalendarAdapter) applicationContext.getBean(callisting
							.getCalendarDefinition().getClassName());
	
					//get hyperlink to calendar
					links.put(callisting.getId(), adapter.getLink(callisting, period, request));
					
				} catch (NoSuchBeanDefinitionException ex) {
					log.error("Calendar class instance could not be found: " + ex.getMessage());
				} catch (CalendarLinkException linkEx) {
					log.warn(linkEx);
				} catch (Exception ex) {
					log.error(ex);
				}
			}

			// add this calendar's id to the color map
			colors.put(callisting.getId(), index);
			index++;

		}

		model.put("timezone", session.getAttribute("timezone"));
		model.put("colors", colors);
		model.put("links", links);
		model.put("hiddenCalendars", hiddenCalendars);
		
		/*
		 * Check if we need to disable either the preferences and/or administration links
		 */
		
        Boolean disablePrefs = Boolean.valueOf(prefs.getValue(PREFERENCE_DISABLE_PREFERENCES, "false"));
        model.put(PREFERENCE_DISABLE_PREFERENCES, disablePrefs);
        Boolean disableAdmin = Boolean.valueOf(prefs.getValue(PREFERENCE_DISABLE_ADMINISTRATION, "false"));
        model.put(PREFERENCE_DISABLE_ADMINISTRATION, disableAdmin);

		return new ModelAndView(viewSelector.getCalendarViewName(request), "model", model);
	}

    private ICalendarSetDao calendarSetDao;
    
    @Autowired(required = true)
    public void setCalendarSetDao(ICalendarSetDao calendarSetDao) {
        this.calendarSetDao = calendarSetDao;
    }

	private List<IInitializationService> initializationServices;
	
	@Required
	@Resource(name="initializationServices")
	public void setInitializationServices(List<IInitializationService> services) {
		this.initializationServices = services;
	}
	
	private IViewSelector viewSelector;
	
	@Autowired(required=true)
	public void setViewSelector(IViewSelector viewSelector) {
		this.viewSelector = viewSelector;
	}
	
	private ApplicationContext applicationContext;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}

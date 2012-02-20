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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarConfigurationByNameComparator;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.adapter.CalendarLinkException;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.IViewSelector;
import org.jasig.portlet.calendar.service.IInitializationService;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;

@Controller
@RequestMapping("VIEW")
public class CalendarController implements ApplicationContextAware {
    
    public static final String PREFERENCE_DISABLE_PREFERENCES = "disablePreferences";
    public static final String PREFERENCE_DISABLE_ADMINISTRATION = "disableAdministration";

	protected final Log log = LogFactory.getLog(this.getClass());

	@ActionMapping
	public void defaultAction() { 
	    // default action mapping
	}
	
	@RequestMapping
	public ModelAndView getCalendar(RenderRequest request) {
		
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
		
		// get the list of hidden calendars
		@SuppressWarnings("unchecked")
		HashMap<Long, String> hiddenCalendars = (HashMap<Long, String>) session
				.getAttribute("hiddenCalendars");

		// indicate if the current user is a guest (unauthenticated) user
		model.put("guest", request.getRemoteUser() == null);
		
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

        // See if we're configured to show or hide the jQueryUI DatePicker.
        // By default, we assume we are to show the DatePicker because that's
        // the classic behavior.
        String showDatePicker = prefs.getValue( "showDatePicker", "true" );
        model.put( "showDatePicker", showDatePicker );

		/**
		 * Find our desired starting and ending dates.
		 */

		//StartDate can only be changed via an AJAX request
		DateMidnight startDate = (DateMidnight) session.getAttribute("startDate");
		log.debug("startDate from session is: "+startDate);
		model.put("startDate", startDate.toDate());

		// find how many days into the future we should display events
		int days = (Integer) session.getAttribute("days");
		model.put("days", days);

		// set the end date based on our desired time period
		DateMidnight endDate = startDate.plusDays(days);
		model.put("endDate", endDate.toDate());

		Interval interval = new Interval(startDate, endDate);

		// define "today" and "tomorrow" so we can display these specially in the
		// user interface
        // get the user's configured time zone
        String timezone = (String) session.getAttribute("timezone");
		DateMidnight today = new DateMidnight(DateTimeZone.forID(timezone));
		model.put("today", today.toDate());
		model.put("tomorrow", today.plusDays(1).toDate());

		/**
		 * retrieve the calendars defined for this portlet instance
		 */
		
        CalendarSet<?> set = calendarSetDao.getCalendarSet(request);
        List<CalendarConfiguration> calendars = new ArrayList<CalendarConfiguration>();
        calendars.addAll(set.getConfigurations());
        Collections.sort(calendars, new CalendarConfigurationByNameComparator());
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
					links.put(callisting.getId(), adapter.getLink(callisting, interval, request));
					
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

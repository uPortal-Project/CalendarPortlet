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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.VEventStartComparator;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.IViewSelector;
import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("VIEW")
public class AjaxCalendarController implements ApplicationContextAware {

	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping(params = "action=events")
	public void getEventList(ActionRequest request,
			ActionResponse response) throws Exception {
		
		PortletSession session = request.getPortletSession();
		Map<String, Object> model = new HashMap<String, Object>();
		
		PortletPreferences prefs = request.getPreferences();

		// get the list of hidden calendars
		@SuppressWarnings("unchecked")
		HashMap<Long, String> hiddenCalendars = (HashMap<Long, String>) session
			.getAttribute("hiddenCalendars");

		// if the user requested a specific date, use it instead
		Calendar cal = null;
        String timezone = (String) session.getAttribute("timezone");
		Date startDate = (Date) session.getAttribute("startDate");
		DateFormat df = new SimpleDateFormat("MM'/'dd'/'yyyy");
		String requestedDate = (String) request.getParameter("startDate");
		if (requestedDate != null && !requestedDate.equals("")) {
			try {
                startDate = df.parse(requestedDate);
                cal = Calendar.getInstance();
                cal.setTime(startDate);
        	    cal.set(Calendar.HOUR_OF_DAY, 0);
        	    cal.set(Calendar.MINUTE, 0);
        	    cal.set(Calendar.SECOND, 0);
        	    cal.set(Calendar.MILLISECOND, 1);
        	    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        	    cal.add(Calendar.MILLISECOND, -TimeZone.getTimeZone(timezone).getOffset(cal.getTimeInMillis()));
                startDate = cal.getTime();
                session.setAttribute("startDate", cal.getTime());
			} catch (ParseException ex) {
				log.warn("Failed to parse starting date for event", ex);
			}
		}

        if (cal == null) {
            cal = Calendar.getInstance();
            cal.setTime((Date) session.getAttribute("startDate"));
    	    cal.set(Calendar.HOUR_OF_DAY, 0);
    	    cal.set(Calendar.MINUTE, 0);
    	    cal.set(Calendar.SECOND, 0);
    	    cal.set(Calendar.MILLISECOND, 1);
    	    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    	    cal.add(Calendar.MILLISECOND, -TimeZone.getTimeZone(timezone).getOffset(cal.getTimeInMillis()));
	    }
	    
	    startDate = cal.getTime();
	    log.debug("start date: " + cal);

		// find how many days into the future we should display events
		int days = (Integer) session.getAttribute("days");
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

		// set the end date based on our desired time period
		cal.add(Calendar.DATE, days);
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
		 * Get all the events for this user, and add them to our event list
		 */

		// retrieve the calendars defined for this portlet instance
		CalendarSet<?> set = calendarSetDao.getCalendarSet(request);
		Collection<? extends CalendarConfiguration> calendars = set.getConfigurations();
		model.put("calendars", calendars);

		TreeSet<VEvent> events = new TreeSet<VEvent>(new VEventStartComparator());
		Map<Long, Integer> colors = new HashMap<Long, Integer>();
		int index = 0;
		List<String> errors = new ArrayList<String>();
        TimeZone userTz = TimeZone.getTimeZone(timezone);
		for (CalendarConfiguration callisting : calendars) {

			// don't bother to fetch hidden calendars
			if (hiddenCalendars.get(callisting.getId()) == null) {

				try {
	
					// get an instance of the adapter for this calendar
					ICalendarAdapter adapter = (ICalendarAdapter) applicationContext.getBean(callisting
							.getCalendarDefinition().getClassName());
	
                    for (CalendarEvent event : adapter.getEvents(callisting, period, request)) {
                    	
                    	/*
                    	 * Provide special handling for events with "floating"
                    	 * timezones.
                    	 */
                        if (event.getStartDate().getTimeZone() == null) {
                        	// first adjust the event to have the correct start
                        	// and end times for the user's timezone
                            int offset = userTz.getOffset(event.getStartDate().getDate().getTime());
                            event.getStartDate().getDate().setTime(event.getStartDate().getDate().getTime()-offset);
                            event.getEndDate().getDate().setTime(event.getEndDate().getDate().getTime()-offset);
                            
                            // if the adjusted event still falls within the 
                            // indicated period go ahead and add it to our list
                            if (period.includes(event.getStartDate().getDate(), Period.INCLUSIVE_START) 
                            		|| period.includes(event.getEndDate().getDate(), Period.INCLUSIVE_END)) {
                                events.add(event);                            	
                            }
                        } 
                        
                        // if the event has a regular time zone, just add it
                        else {
                            events.add(event);
                        }
                    }
	
				} catch (NoSuchBeanDefinitionException ex) {
					log.error("Calendar class instance could not be found: " + ex.getMessage());
				} catch (Exception ex) {
					log.warn(ex);
					errors.add("The calendar \"" + callisting.getCalendarDefinition().getName() + "\" is currently unavailable.");
				}

			}

			// add this calendar's id to the color map
			colors.put(callisting.getId(), index);
			index++;

		}
		log.debug("events: " + events.size());

		model.put("timezone", session.getAttribute("timezone"));
		model.put("events", events);
		model.put("colors", colors);
		model.put("hiddenCalendars", hiddenCalendars);
		model.put("errors", errors);
		model.put("viewName", viewSelector.getEventListViewName(request));
	
		ajaxPortletSupportService.redirectAjaxResponse("ajax/jspView", model, request, response);
	}
	
	private ICalendarSetDao calendarSetDao;
	
	@Autowired(required = true)
	public void setCalendarSetDao(ICalendarSetDao calendarSetDao) {
	    this.calendarSetDao = calendarSetDao;
	}

	private IViewSelector viewSelector;
	
	@Autowired(required=true)
	public void setViewSelector(IViewSelector viewSelector) {
		this.viewSelector = viewSelector;
	}

	private ApplicationContext applicationContext;
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	private AjaxPortletSupportService ajaxPortletSupportService;
	
    /**
     * Set the service for handling portlet AJAX requests.
     * 
     * @param ajaxPortletSupportService
     */
    @Autowired(required = true)
    public void setAjaxPortletSupportService(
                    AjaxPortletSupportService ajaxPortletSupportService) {
            this.ajaxPortletSupportService = ajaxPortletSupportService;
    }

}

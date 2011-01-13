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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarConfigurationByNameComparator;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.adapter.UserFeedbackCalendarException;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.JsonCalendarEvent;
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
		
		// get the list of hidden calendars
		@SuppressWarnings("unchecked")
		HashMap<Long, String> hiddenCalendars = (HashMap<Long, String>) session
			.getAttribute("hiddenCalendars");

		// if the user requested a specific date, use it instead
		Calendar cal = null;
        String timezone = (String) session.getAttribute("timezone");
        TimeZone tz = TimeZone.getTimeZone(timezone);

		// define "today" and "tomorrow" so we can display these specially in the
		// user interface
		cal = Calendar.getInstance(tz);
		model.put("today", cal.getTime());
		cal.add(Calendar.DATE, 1);
		model.put("tomorrow", cal.getTime());
		
		Period period = getPeriod(request);

		/**
		 * Get all the events for this user, and add them to our event list
		 */

		// retrieve the calendars defined for this portlet instance
		CalendarSet<?> set = calendarSetDao.getCalendarSet(request);
        List<CalendarConfiguration> calendars = new ArrayList<CalendarConfiguration>();
        calendars.addAll(set.getConfigurations());
        Collections.sort(calendars, new CalendarConfigurationByNameComparator());

		int index = 0;
		List<String> errors = new ArrayList<String>();
        TreeMap<Date, Set<JsonCalendarEvent>> dateMap = new TreeMap<Date, Set<JsonCalendarEvent>>();
        DateFormat df = new SimpleDateFormat("EEEE MMMM d");
        df.setTimeZone(tz);
		for (CalendarConfiguration callisting : calendars) {

			// don't bother to fetch hidden calendars
			if (hiddenCalendars.get(callisting.getId()) == null) {

				try {
	
					// get an instance of the adapter for this calendar
					ICalendarAdapter adapter = (ICalendarAdapter) applicationContext.getBean(callisting
							.getCalendarDefinition().getClassName());
	
                    for (CalendarEvent event : adapter.getEvents(callisting, period, request)) {

                    	log.debug("Event " + event.getSummary().getValue() + ", " + event.getStartDate().getTimeZone() + ", " + event.getStartDate().isUtc() + ", " + event.getStartDate().getValue());
                    	/*
                    	 * Provide special handling for events with "floating"
                    	 * timezones.
                    	 */
                        if (event.getStartDate().getTimeZone() == null && !event.getStartDate().isUtc()) {
                        	// first adjust the event to have the correct start
                        	// and end times for the user's timezone
                            int offset = tz.getOffset(event.getStartDate().getDate().getTime());
                            event.getStartDate().getDate().setTime(event.getStartDate().getDate().getTime()-offset);
                            if (event.getEndDate() != null) {
                            	event.getEndDate().getDate().setTime(event.getEndDate().getDate().getTime()-offset);
                            }
                            
                        } else if (event.getStartDate().isUtc()) {
                        	TimeZoneRegistryFactory tzFactory = new DefaultTimeZoneRegistryFactory();
                        	TimeZoneRegistry tzRegistry = tzFactory.createRegistry();
                        	event.getStartDate().setTimeZone(tzRegistry.getTimeZone("UTC"));
                        }

                        // if the adjusted event still falls within the 
                        // indicated period go ahead and add it to our list
                        if (period.includes(event.getStartDate().getDate(), Period.INCLUSIVE_START) 
                        		|| period.includes(event.getEndDate().getDate(), Period.INCLUSIVE_END)) {
	
                        	addEventToDateMap(dateMap, event, tz, index);

                        }
                    }
	
                } catch (NoSuchBeanDefinitionException ex) {
                    log.error("Calendar class instance could not be found: " + ex.getMessage());
                } catch (UserFeedbackCalendarException sce) {
                    // This CalendarException subclass carries a payload fot the UI...
                    StringBuilder msg = new StringBuilder();
                    msg.append(callisting.getCalendarDefinition().getName())
                                .append(":  ").append(sce.getUserFeedback());                    
                    errors.add(msg.toString());
                } catch (Exception ex) {
                    log.warn("Unknown Error", ex);
                    errors.add("The calendar \"" + callisting.getCalendarDefinition().getName() + "\" is currently unavailable.");
                }

			}

			index++;

		}
		
		Map<String, Set<JsonCalendarEvent>> events = new LinkedHashMap<String, Set<JsonCalendarEvent>>();
		for (Map.Entry<Date, Set<JsonCalendarEvent>> dateEntry : dateMap.entrySet()) {
			events.put(df.format(dateEntry.getKey()), dateEntry.getValue());
		}

		model.put("dateMap", events);
		model.put("viewName", "jsonView");
		model.put("errors", errors);
	
		ajaxPortletSupportService.redirectAjaxResponse("ajax/jsonView", model, request, response);
	}
	
	protected void addEventToDateMap(Map<Date, Set<JsonCalendarEvent>> dateMap, CalendarEvent event, TimeZone tz, int index) {

		Calendar dayStart = Calendar.getInstance(tz);
        dayStart.setTime(event.getStartDate().getDate());
        dayStart.set(Calendar.HOUR, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 1);
        
        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DATE, 1);

    	Calendar eventEnd = Calendar.getInstance(tz);
    	eventEnd.setTime(event.getEndDate().getDate());
    	
    	do {
        	Date date = dayStart.getTime();
        	if (!dateMap.containsKey(date)) {
        		dateMap.put(date, new TreeSet<JsonCalendarEvent>());
        	}
        	dateMap.get(date).add(new JsonCalendarEvent(event, dayStart.getTime(), tz, index));
        	
        	dayStart.add(Calendar.DATE, 1);
        	dayEnd.add(Calendar.DATE, 1);

        } while (dayStart.before(eventEnd));
    	
	}
	
	protected Period getPeriod(ActionRequest request) {
		
		PortletSession session = request.getPortletSession();
		PortletPreferences prefs = request.getPreferences();
		
		// if the user requested a specific date, use it instead
		Calendar cal = null;
        String timezone = (String) session.getAttribute("timezone");
        TimeZone tz = TimeZone.getTimeZone(timezone);
        
		Date startDate = (Date) session.getAttribute("startDate");
		DateFormat df = new SimpleDateFormat("MM'/'dd'/'yyyy");
		df.setTimeZone(tz);
		String requestedDate = (String) request.getParameter("startDate");
		if (requestedDate != null && !requestedDate.equals("")) {
			try {
                startDate = df.parse(requestedDate);
                cal = Calendar.getInstance(tz);
                cal.setTime(startDate);
        	    cal.set(Calendar.HOUR_OF_DAY, 0);
        	    cal.set(Calendar.MINUTE, 0);
        	    cal.set(Calendar.SECOND, 0);
        	    cal.set(Calendar.MILLISECOND, 1);
                startDate = cal.getTime();
                session.setAttribute("startDate", cal.getTime());
			} catch (ParseException ex) {
				log.warn("Failed to parse starting date for event", ex);
			}
		}

        if (cal == null) {
            cal = Calendar.getInstance(tz);
            cal.setTime((Date) session.getAttribute("startDate"));
    	    cal.set(Calendar.HOUR_OF_DAY, 0);
    	    cal.set(Calendar.MINUTE, 0);
    	    cal.set(Calendar.SECOND, 0);
    	    cal.set(Calendar.MILLISECOND, 1);
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
        cal.set(Calendar.MILLISECOND, 1);
        Date endDate = cal.getTime();

		return new Period(new DateTime(startDate), new DateTime(
				endDate));
	}
	
	private ICalendarSetDao calendarSetDao;
	
	@Autowired(required = true)
	public void setCalendarSetDao(ICalendarSetDao calendarSetDao) {
	    this.calendarSetDao = calendarSetDao;
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

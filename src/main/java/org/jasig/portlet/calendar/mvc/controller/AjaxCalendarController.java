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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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
		

		/*
		 * Retrieve the calendar configurations defined for this user request
		 * and sort them by display name.  This sorting operation ensures that
		 * the CSS color indices assigned to calendar events will be consistent
		 * with the colors assigned in the main controller.
		 */
		
		// retrieve the calendars defined for this portlet instance
		CalendarSet<?> set = calendarSetDao.getCalendarSet(request);
        List<CalendarConfiguration> calendars = new ArrayList<CalendarConfiguration>();
        calendars.addAll(set.getConfigurations());
        
        // sort the calendars
        Collections.sort(calendars, new CalendarConfigurationByNameComparator());

		// get the list of hidden calendars
		@SuppressWarnings("unchecked")
		HashMap<Long, String> hiddenCalendars = (HashMap<Long, String>) session
			.getAttribute("hiddenCalendars");

        /*
         * For each unhidden calendar, get the list of associated events for 
         * the requested time period.
         */

        // get the period for this request
		Period period = getPeriod(request);

		// get the user's configured time zone
        String timezone = (String) session.getAttribute("timezone");
        TimeZone tz = TimeZone.getTimeZone(timezone);
		
		int index = 0;
		List<String> errors = new ArrayList<String>();
		Set<JsonCalendarEvent> events = new TreeSet<JsonCalendarEvent>();
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
                        if (event.getStartDate().getTimeZone() == null && !event.getStartDate().isUtc()) {
                        	// first adjust the event to have the correct start
                        	// and end times for the user's timezone
                        	if (log.isDebugEnabled()) {
                        		log.debug("Identified event " + event.getSummary() + " as a floating event");
                        	}
                            int offset = tz.getOffset(event.getStartDate().getDate().getTime());
                            event.getStartDate().getDate().setTime(event.getStartDate().getDate().getTime()-offset);
                            if (event.getEndDate() != null) {
                            	event.getEndDate().getDate().setTime(event.getEndDate().getDate().getTime()-offset);
                            }
                            
                        // if the event is UTC, ensure that the event timezone is
                        // set appropriately
                        } else if (event.getStartDate().isUtc()) {
                        	if (log.isDebugEnabled()) {
                        		log.debug("Setting time zone to UTC for  event " + event.getSummary());
                        	}
                        	TimeZoneRegistryFactory tzFactory = new DefaultTimeZoneRegistryFactory();
                        	TimeZoneRegistry tzRegistry = tzFactory.createRegistry();
                        	if (event.getEndDate() != null) {
	                        	event.getStartDate().setTimeZone(tzRegistry.getTimeZone("UTC"));
	                        	event.getEndDate().setTimeZone(tzRegistry.getTimeZone("UTC"));
                        	}
                        }

                      	events.addAll(getJsonEvents(event, period, tz, index));
                    }
	
                } catch (NoSuchBeanDefinitionException ex) {
                    log.error("Calendar class instance could not be found: " + ex.getMessage());
                } catch (UserFeedbackCalendarException sce) {
                    // This CalendarException subclass carries a payload for the UI...
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

		
		/*
		 * Transform the event set into a map keyed by day.  This code is 
		 * designed to separate events by day according to the user's configured
		 * time zone.  This ensures that we keep complicated time-zone handling
		 * logic out of the JavaScript.
		 * 
		 * Events are keyed by a string uniquely representing the date that is 
		 * still orderable.  So that we can display a more user-friendly date
		 * name, we also create a map representing date display names for each
		 * date keyed in this response.
		 */

		// define a DateFormat object that uniquely identifies dates in a way 
		// that can easily be ordered 
        DateFormat orderableDf = new SimpleDateFormat("yyyy-MM-dd");
        orderableDf.setTimeZone(tz);

        // define a DateFormat object that can produce user-facing display 
        // names for dates
        DateFormat displayDf = new SimpleDateFormat("EEEE MMMM d");
        displayDf.setTimeZone(tz);

		// define "today" and "tomorrow" so we can display these specially in the
		// user interface
		Calendar cal = Calendar.getInstance(tz);
		String today = orderableDf.format(cal.getTime());
		cal.add(Calendar.DATE, 1);
		String tomorrow = orderableDf.format(cal.getTime());

		Map<String, String> dateDisplayNames = new HashMap<String, String>();
		Map<String, List<JsonCalendarEvent>> eventsByDay = new LinkedHashMap<String, List<JsonCalendarEvent>>();
		for (JsonCalendarEvent event : events) {
			String day = orderableDf.format(event.getDayStart());
			
			// if we haven't seen this day before, add entries to the event
			// and date name maps
	    	if (!eventsByDay.containsKey(day)) {
	    		
	    		// add a list for this day to the eventsByDay map
	    		eventsByDay.put(day, new ArrayList<JsonCalendarEvent>());
	    		
	    		// Add an appropriate day name for this date to the date names
	    		// map.  If the day appears to be today or tomorrow display a 
	    		// special string value.  Otherwise, use the user-facing date
	    		// format object
	    		if (today.equals(day)) {
		    		dateDisplayNames.put(day, "Today");
	    		} else if (tomorrow.equals(day)) {
		    		dateDisplayNames.put(day, "Tomorrow");
	    		} else {
		    		dateDisplayNames.put(day, displayDf.format(event.getDayStart()));
	    		}
	    	}
	    	
	    	// add the event to the by-day map
	    	eventsByDay.get(day).add(event);
		}

		model.put("dateMap", eventsByDay);
		model.put("dateNames", dateDisplayNames);
		model.put("viewName", "jsonView");
		model.put("errors", errors);
	
		ajaxPortletSupportService.redirectAjaxResponse("ajax/jsonView", model, request, response);
	}
	
	/**
	 * Get the set of Json-formatted events associated with a CalendarEvent in 
	 * the given time period.
	 * 
	 * @param event
	 * @param period
	 * @param tz
	 * @param index
	 * @return
	 */
	protected Set<JsonCalendarEvent> getJsonEvents(CalendarEvent event, Period period, TimeZone tz, int index) {

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

    	Set<JsonCalendarEvent> events = new HashSet<JsonCalendarEvent>();
    	
    	do {
        	JsonCalendarEvent json = new JsonCalendarEvent(event, dayStart.getTime(), tz, index);

            // if the adjusted event still falls within the 
            // indicated period go ahead and add it to our list
            if (period.includes(json.getDayStart(), Period.INCLUSIVE_START) 
            		|| period.includes(json.getDayEnd(), Period.INCLUSIVE_END)) {

            	events.add(json);
            }

        	dayStart.add(Calendar.DATE, 1);
        	dayEnd.add(Calendar.DATE, 1);

        } while (dayStart.before(eventEnd));
    	
    	return events;
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

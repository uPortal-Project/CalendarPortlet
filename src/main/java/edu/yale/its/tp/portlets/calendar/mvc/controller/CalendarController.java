/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.adapter.CalendarLinkException;
import edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter;
import edu.yale.its.tp.portlets.calendar.dao.CalendarStore;
import edu.yale.its.tp.portlets.calendar.service.IInitializationService;

public class CalendarController extends AbstractController {

	private static Log log = LogFactory.getLog(CalendarController.class);

	public ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {

		Map<String, Object> model = new HashMap<String, Object>();
		PortletSession session = request.getPortletSession(true);
		HashMap<Long, String> hiddenCalendars = null;
		Calendar cal = Calendar.getInstance();
		String subscribeId = null;
		
		/**
		 * If this is a new session, perform any necessary 
		 * portlet initialization.
		 */

		if (session.getAttribute("initialized") == null) {
			
			if(userToken == null || userToken.equalsIgnoreCase("")) {
				subscribeId = request.getRemoteUser();
	    	}
	    	else {
	    		// get the credentials for this portlet from the UserInfo map
	    		Map userinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
	    		subscribeId = (String) userinfo.get(userToken);    		
	    	}
			
			//default to guest
			if (subscribeId == null) {
				subscribeId = "guest";
			}
			session.setAttribute("subscribeId", subscribeId);

			// get a set of all role names currently configured for
			// default calendars
			List<String> allRoles = calendarStore.getUserRoles();
			log.debug("all roles: " + allRoles);
			
			// determine which of the above roles the user belongs to
			// and store the resulting list in the session
			Set<String> userRoles = new HashSet<String>();
			for (String role : allRoles) {
				if (request.isUserInRole(role))
					userRoles.add(role);
			}
			session.setAttribute("userRoles", userRoles);
			
			// determine if this user belongs to the defined calendar
			// administration group and store the result in the session
			session.setAttribute("isAdmin", 
					request.isUserInRole("calendarAdmin"), PortletSession.APPLICATION_SCOPE);

			// update the user's calendar subscriptions to include
			// any calendars that have been associated with his or 
			// her role
			calendarStore.initCalendar(subscribeId, userRoles);

			// create a list of hidden calendars
			hiddenCalendars = new HashMap<Long, String>();
			session.setAttribute("hiddenCalendars", hiddenCalendars);

			// set now as the starting date
			session.setAttribute("startDate", cal.getTime());
			
			// set the default number of days to display
			session.setAttribute("days", defaultDays);

			// perform any other configured initialization tasks
			for (IInitializationService service : initializationServices) {
				service.initialize(request);
			}

			// mark this session as initialized
			session.setAttribute("initialized", "true");
			session.setMaxInactiveInterval(60*60*2);
			
			PortletPreferences prefs = request.getPreferences();
			String timezone = prefs.getValue("timezone", "America/New_York");
			session.setAttribute("timezone", timezone);

		} else {
			// get the list of hidden calendars
			hiddenCalendars = (HashMap<Long, String>) session.getAttribute("hiddenCalendars");
			subscribeId = (String) session.getAttribute("subscribeId");
		}

		if ("guest".equalsIgnoreCase(subscribeId)) {
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
			} catch (NumberFormatException ex) {
				log.warn("Failed to parse desired time period", ex);
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
		
		List<CalendarConfiguration> calendars = calendarStore
				.getCalendarConfigurations(subscribeId);
		model.put("calendars", calendars);

		ApplicationContext ctx = this.getApplicationContext();
		Map<Long, Integer> colors = new HashMap<Long, Integer>();
		Map<Long, String> links = new HashMap<Long, String>();
		int index = 0;
		for (CalendarConfiguration callisting : calendars) {

			// don't bother to fetch hidden calendars
			if (hiddenCalendars.get(callisting.getId()) == null) {

				try {
	
					// get an instance of the adapter for this calendar
					ICalendarAdapter adapter = (ICalendarAdapter) ctx.getBean(callisting
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
		model.put("includeJQuery", includeJQuery);

		return new ModelAndView("/viewCalendar", "model", model);
	}

	private CalendarStore calendarStore;
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

	private String userToken = null;
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	
	private int defaultDays = 2;
	public void setDefaultDays(int defaultDays) {
		this.defaultDays = defaultDays;
	}
	
	private boolean includeJQuery = true;
	public void setIncludeJQuery(boolean includeJQuery) {
		this.includeJQuery = includeJQuery;
	}

	private List<IInitializationService> initializationServices;
	public void setInitializationServices(List<IInitializationService> services) {
		this.initializationServices = services;
	}

}


/*
 * CalendarController.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
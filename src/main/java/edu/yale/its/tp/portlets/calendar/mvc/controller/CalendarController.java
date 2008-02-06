package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.VEventStartComparator;
import edu.yale.its.tp.portlets.calendar.adapter.CalendarException;
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
		Map userinfo = (Map) request.getAttribute("javax.portlet.userinfo");

		// get the user's role
		String role = (String) userinfo.get(roleToken);

		// get this portlet's unique subscription id
		String subscribeId = (String) userinfo.get("user.login.id");

		/**
		 * If this is a new session, perform any necessary 
		 * portlet initialization.
		 */

		if (session.getAttribute("initialized") == null) {

			// update the user's calendar subscriptions to include
			// any calendars that have been associated with his or 
			// her role
			calendarStore.initCalendar(subscribeId, role);

			// create a list of hidden calendars
			hiddenCalendars = new HashMap<Long, String>();
			session.setAttribute("hiddenCalendars", hiddenCalendars);

			// set the default number of days to display
			session.setAttribute("days", defaultDays);

			// perform any other configured initialization tasks
			for (IInitializationService service : initializationServices) {
				service.initialize(request);
			}

			// mark this session as initialized
			session.setAttribute("initialized", "true");
			session.setMaxInactiveInterval(60*60*2);

		} else {
			// get the list of hidden calendars
			hiddenCalendars = (HashMap<Long, String>) session
					.getAttribute("hiddenCalendars");
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

		// construct a default starting date of today
		Calendar cal = Calendar.getInstance();
		Date startDate = cal.getTime();
		model.put("startDate", startDate);

		// if the user requested a specific date, use it instead
		DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd");
		String requestedDate = (String) request.getParameter("date");
		if (requestedDate != null && !requestedDate.equals("")) {
			try {
				startDate = df.parse(requestedDate);
				cal.setTime(startDate);
			} catch (ParseException ex) {
				log.warn("Failed to parse starting date for event", ex);
			}
		}

		// find how many days into the future we should display events
		int days = (Integer) session.getAttribute("days");
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
		 * Get all the events for this user, and add them to our event list
		 */

		// retrieve the calendars defined for this portlet instance
		List<CalendarConfiguration> calendars = calendarStore
				.getCalendarConfigurations(subscribeId);
		model.put("calendars", calendars);

		ApplicationContext ctx = this.getApplicationContext();
		TreeSet<VEvent> events = new TreeSet<VEvent>(new VEventStartComparator());
		Map<Long, Integer> colors = new HashMap<Long, Integer>();
		int index = 0;
		List<String> errors = new ArrayList<String>();
		for (CalendarConfiguration callisting : calendars) {

			// don't bother to fetch hidden calendars
			if (hiddenCalendars.get(callisting.getId()) == null) {

				try {
	
					// get an instance of the adapter for this calendar
					ICalendarAdapter adapter = (ICalendarAdapter) ctx.getBean(callisting
							.getCalendarDefinition().getClassName());
	
					// retrieve a list of events for this calendar for the desired
					// time period
					events.addAll(adapter.getEvents(callisting, period, request));
	
				} catch (NoSuchBeanDefinitionException ex) {
					log.error("Calendar class instance could not be found: " + ex.getMessage());
				} catch (CalendarException ex) {
					log.warn(ex);
					errors.add("The calendar \"" + callisting.getCalendarDefinition().getName() + "\" is currently unavailable.");
				}

			}

			// add this calendar's id to the color map
			colors.put(callisting.getId(), index);
			index++;

		}

		model.put("events", events);
		model.put("colors", colors);
		model.put("hiddenCalendars", hiddenCalendars);
		model.put("errors", errors);

		return new ModelAndView("/viewCalendar", "model", model);
	}

	private CalendarStore calendarStore;

	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

	private String roleToken = "contentGroup";

	public void setRoleToken(String roleToken) {
		this.roleToken = roleToken;
	}
	
	private int defaultDays = 2;
	public void setDefaultDays(int defaultDays) {
		this.defaultDays = defaultDays;
	}

	private List<IInitializationService> initializationServices;

	public void setInitializationServices(List<IInitializationService> services) {
		this.initializationServices = services;
	}

}

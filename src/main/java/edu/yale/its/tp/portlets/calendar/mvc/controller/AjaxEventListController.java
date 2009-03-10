package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.VEventStartComparator;
import edu.yale.its.tp.portlets.calendar.adapter.CalendarException;
import edu.yale.its.tp.portlets.calendar.adapter.ICalendarAdapter;
import edu.yale.its.tp.portlets.calendar.dao.CalendarStore;

public class AjaxEventListController extends AbstractController {

	private static Log log = LogFactory.getLog(AjaxEventListController.class);


	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		HttpSession session = request.getSession(false);
		Map<String, Object> model = new HashMap<String, Object>();
		
		// get the list of hidden calendars
		HashMap<Long, String> hiddenCalendars = (HashMap<Long, String>) session
			.getAttribute("hiddenCalendars");

		Date startDate = (Date) session.getAttribute("startDate");
		log.debug("startDate from session is: "+startDate);

		// if the user requested a specific date, use it instead
		DateFormat df = new SimpleDateFormat("MM'/'dd'/'yyyy");
		String requestedDate = (String) request.getParameter("startDate");
		if (requestedDate != null && !requestedDate.equals("")) {
			try {
				startDate = df.parse(requestedDate);
				session.setAttribute("startDate", startDate);
				
				log.debug("adding new start date to session: "+startDate);
			} catch (ParseException ex) {
				log.warn("Failed to parse starting date for event", ex);
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);

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
				.getCalendarConfigurations((String) session.getAttribute("subscribeId"));
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
				} catch (Exception e) {
					log.error("Undefined error: "+e.getClass());
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

		return new ModelAndView("/ajaxEventList", "model", model);
	}

	private CalendarStore calendarStore;
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}


}

/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;

import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * AdminCalendarController provides a main administrative view for the calendar
 * portlet.  The page is available to users in the configured "calendarAdmin" 
 * role.
 *
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
public class AdminCalendarController {

	private CalendarStore calendarStore;
	
	@Required
	@Resource(name="calendarStore")
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

	/**
	 * Show the main administrative view.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "action=administration")
	public String showAdministration(RenderRequest request) {
		return "/adminCalendars";
	}
	
	/**
	 * Delete a predefined calendar from the calendar store.
	 * 
	 * @param request
	 * @param response
	 * @param id		ID of the calendar to be deleted
	 */
	@RequestMapping(params = "action=deleteSharedCalendar")
	public void deleteSharedCalendar(ActionRequest request,
			ActionResponse response, @RequestParam("calendarId") Long id) {
		
		// delete the calendar
		PredefinedCalendarDefinition def = calendarStore.getPredefinedCalendarDefinition(id);
		calendarStore.deleteCalendarDefinition(def);
		
		// send the user back to the main administration page
		response.setRenderParameter("action", "administration");
	}

	/**
	 * Get a list of all currently-defined predefined calendars.
	 * 
	 * @return list of calendars
	 */
	@ModelAttribute("calendars")
	public List<PredefinedCalendarConfiguration> getPredefinedCalendars() {
		return calendarStore.getPredefinedCalendarConfigurations();
	}
	
}

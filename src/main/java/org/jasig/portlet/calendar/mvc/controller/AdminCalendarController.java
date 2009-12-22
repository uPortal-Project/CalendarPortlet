/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;


/**
 * AdminCalendarController provides a main administrative view for the calendar
 * portlet.  The page is available to users in the configured "calendarAdmin" 
 * role.
 *
 * @author Jen Bourey
 */
public class AdminCalendarController  extends AbstractController {

	@Override
	public ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {

		Map<String, Object> model = new HashMap<String, Object>();
		
		// get a list of all predefined calendars
		model.put("calendars", calendarStore.getPredefinedCalendarConfigurations());
		
		return new ModelAndView("/adminCalendars", "model", model);

	}
	
	@Override
	protected void handleActionRequestInternal(ActionRequest request,
			ActionResponse response) throws Exception {
		Long id = Long.parseLong(request.getParameter("id"));
		String actionCode = request.getParameter("actionCode");
		if (actionCode.equals("delete")) {
			PredefinedCalendarDefinition def = calendarStore.getPredefinedCalendarDefinition(id);
			calendarStore.deleteCalendarDefinition(def);
		}
	}

	private CalendarStore calendarStore;
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

}

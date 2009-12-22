/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;


/**
 * EditCalendarPreferencesController provides the main edit page for the calendars
 * portlet.  The page allows users to view, add, delete and edit all available
 * calendars.
 * 
 * @author Jen Bourey
 */
public class EditCalendarSubscriptionsController extends AbstractController {

	private static Log log = LogFactory.getLog(EditCalendarSubscriptionsController.class);

	@Override
	public ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {

		Map<String, Object> model = new HashMap<String, Object>();
		PortletSession session = request.getPortletSession();

		// get user information
		String subscribeId = (String) session.getAttribute("subscribeId");
		if ("guest".equalsIgnoreCase(subscribeId)) {
			model.put("guest", true);
		} else {
			model.put("guest", false);
		}

		// add the user-defined calendars to the model
		List<UserDefinedCalendarConfiguration> mycalendars = calendarStore.getUserDefinedCalendarConfigurations(subscribeId, false);
		model.put("mycalendars", mycalendars);

		// add the predefined calendars to the model
		List<PredefinedCalendarConfiguration> calendars = calendarStore.getPredefinedCalendarConfigurations(subscribeId, false);
		model.put("calendars", calendars);
		
		// get the user's role listings
		@SuppressWarnings("unchecked")
		Set<String> userRoles = (Set<String>) session.getAttribute("userRoles");

		// get a list of predefined calendars the user doesn't 
		// currently have configured
		List<PredefinedCalendarDefinition> definitions = calendarStore.getHiddenPredefinedCalendarDefinitions(subscribeId, userRoles);
		model.put("hiddencalendars", definitions);
		
		model.put("predefinedEditActions", predefinedEditActions);
		
		// return the edit view
		return new ModelAndView("/editCalendars", "model", model);
	}
	
	@Override
	protected void handleActionRequestInternal(ActionRequest request,
			ActionResponse response) throws Exception {
		Long id = Long.parseLong(request.getParameter("id"));
		String actionCode = request.getParameter("actionCode");
		PortletSession session = request.getPortletSession();
		if (actionCode.equals("delete")) {
			CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
			calendarStore.deleteCalendarConfiguration(config);
			@SuppressWarnings("unchecked")
			Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
			hidden.remove(config.getId());
		} else if (actionCode.equals("show")) {
			CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
			config.setDisplayed(true);
			calendarStore.storeCalendarConfiguration(config);
			@SuppressWarnings("unchecked")
			Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
			hidden.remove(config.getId());
		} else if (actionCode.equals("hide")) {
			CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
			config.setDisplayed(false);
			calendarStore.storeCalendarConfiguration(config);
			@SuppressWarnings("unchecked")
			Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
			hidden.remove(config.getId());
		} else if (actionCode.equals("showNew")) {
			// get user information
			String subscribeId = (String) session.getAttribute("subscribeId");
			PredefinedCalendarDefinition definition = (PredefinedCalendarDefinition) calendarStore.getCalendarDefinition(id);
			log.debug("definition to save " + definition.toString());
			PredefinedCalendarConfiguration config = new PredefinedCalendarConfiguration();
			config.setSubscribeId(subscribeId);
			config.setCalendarDefinition(definition);
			calendarStore.storeCalendarConfiguration(config);
		}
	}

	private Map predefinedEditActions;
	public void setPredefinedEditActions(Map predefinedEditActions) {
		this.predefinedEditActions = predefinedEditActions;
	}

	private CalendarStore calendarStore;
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

}

/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc.controller;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.jasig.portlet.calendar.UserDefinedCalendarDefinition;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.mvc.CalendarListingCommand;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * EditCalendarDefinitionController allows a user to add or edit a user-defined
 * calendar definition.
 * 
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
public class EditUserHttpICalController {

    private static final String FORM_NAME = "calendarListingCommand";

	private CalendarStore calendarStore;

	private static Log log = LogFactory.getLog(EditUserHttpICalController.class);

	@Required
	@Resource(name="calendarStore")
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

	@ModelAttribute(FORM_NAME)
	public CalendarListingCommand getHttpCalendarForm(PortletRequest request) throws Exception {
		PortletSession session = request.getPortletSession();

		// if we're editing a calendar, retrieve the calendar definition from
		// the database and add the information to the form
		String id = request.getParameter("id");
		if (id != null && !id.equals("")) {
			Long configurationId = Long.parseLong(id);
			if (configurationId > -1) {
				CalendarConfiguration listing = (CalendarConfiguration) calendarStore
						.getCalendarConfiguration(configurationId);
				log.debug("retrieved " + listing.toString());
				CalendarListingCommand command = new CalendarListingCommand();
				command.setId(listing.getId());
				command.setName(listing.getCalendarDefinition().getName());
				command.setUrl(listing.getCalendarDefinition().getParameters().get("url"));
				command.setSubscribeId(listing.getSubscribeId());
				command.setDisplayed(listing.isDisplayed());
			
				return command;
			} else {
				// otherwise, construct a brand new form

				// get user information
				// get user information
				String subscribeId = (String) session.getAttribute("subscribeId");
				
				// create the form
				CalendarListingCommand command =  new CalendarListingCommand();
				command.setSubscribeId(subscribeId);
				return command;
			}

		} else {
			// otherwise, construct a brand new form

			// get user information
			// get user information
			String subscribeId = (String) session.getAttribute("subscribeId");
			
			// create the form
			CalendarListingCommand command =  new CalendarListingCommand();
			command.setSubscribeId(subscribeId);
			return command;
		}
	}

    @RequestMapping(params = "action=editUrl")
    public String getHttpCalendarFormView(RenderRequest request) {
    	return "/editCalendarUrl";
    }
    

    @RequestMapping(params = "action=editUrl")
    public void updateHttpCalendar(ActionRequest request, ActionResponse response, 
    		@ModelAttribute(FORM_NAME) CalendarListingCommand form)
			throws Exception {
		
		// construct a calendar definition from the form data
		UserDefinedCalendarConfiguration config = null;
		UserDefinedCalendarDefinition definition = null;
		
		if (form.getId() > -1) {
			
			config = (UserDefinedCalendarConfiguration) calendarStore.getCalendarConfiguration(form.getId());
			definition = config.getCalendarDefinition();
			definition.addParameter("url", form.getUrl());
			definition.setName(form.getName());
			
		} else {
			
			definition = new UserDefinedCalendarDefinition();
			definition.setClassName("httpIcalAdapter");
			definition.addParameter("url", form.getUrl());
			definition.setName(form.getName());
			calendarStore.storeCalendarDefinition(definition);
	
			config = new UserDefinedCalendarConfiguration();
			config.setCalendarDefinition(definition);
			config.setSubscribeId(form.getSubscribeId());
			config.setDisplayed(form.isDisplayed());

		}

		// save the calendar
		calendarStore.storeCalendarConfiguration(config);

		// send the user back to the main edit page
		response.setRenderParameter("action", "editSubscriptions");

	}

}

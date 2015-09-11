/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.jasig.portlet.calendar.mvc.UserHttpIcalCalendarForm;
import org.jasig.portlet.calendar.service.SessionSetupInitializationService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.portlet.bind.annotation.ActionMapping;


/**
 * EditCalendarDefinitionController allows a user to add or edit a user-defined
 * calendar definition.
 * 
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
@SessionAttributes("userHttpIcalCalendarForm")
public class EditUserHttpICalController {

    private static final String FORM_NAME = "userHttpIcalCalendarForm";

	protected final Log log = LogFactory.getLog(this.getClass());

	private CalendarStore calendarStore;

	@Required
	@Resource(name="calendarStore")
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

	/**
	 * Show the Calendar editing form
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(params = "action=editUrl")
	public String showHttpCalendarForm(RenderRequest request, Model model) {
		if (!model.containsAttribute(FORM_NAME)) {
			model.addAttribute(FORM_NAME, getHttpCalendarForm(request));
		}
		return "/editCalendarUrl";
	}

	/**
	 * Update the calendar in the data store.
	 * 
	 * @param request
	 * @param response
	 * @param form
	 * @param result
	 * @param status
	 * @throws Exception
	 */
	@ActionMapping(params = "action=editUrl")
	public void updateHttpCalendar(ActionRequest request, ActionResponse response, 
			@ModelAttribute(FORM_NAME) UserHttpIcalCalendarForm form, 
			BindingResult result, SessionStatus status)
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
		status.setComplete();
	
	}

	/**
	 * Generate a new calendar form.
	 * 
	 * @param request
	 * @return
	 */
	protected UserHttpIcalCalendarForm getHttpCalendarForm(PortletRequest request) {
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
				UserHttpIcalCalendarForm command = new UserHttpIcalCalendarForm();
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
				String subscribeId = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
				
				// create the form
				UserHttpIcalCalendarForm command =  new UserHttpIcalCalendarForm();
				command.setSubscribeId(subscribeId);
				return command;
			}

		} else {
			// otherwise, construct a brand new form

			// get user information
			// get user information
			String subscribeId = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
			
			// create the form
			UserHttpIcalCalendarForm command =  new UserHttpIcalCalendarForm();
			command.setSubscribeId(subscribeId);
			return command;
		}
	}
    
}

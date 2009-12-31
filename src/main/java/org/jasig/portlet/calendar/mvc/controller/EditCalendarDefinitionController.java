/*
 * Created on Feb 13, 2008
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

import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.mvc.CalendarDefinitionForm;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * EditCalendarDefinitionController provides a GUI for adding and editing 
 * predefined calendars.
 *
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
public class EditCalendarDefinitionController {

	private static final String FORM_NAME = "calendarDefinitionForm";

	@ModelAttribute(FORM_NAME)
	public CalendarDefinitionForm formBackingObject(PortletRequest request) {
		// if we're editing a calendar, retrieve the calendar definition from
		// the database and add the information to the form
		String id = request.getParameter("id");
		if (id != null && !id.equals("")) {
			Long definitionId = Long.parseLong(id);
			if (definitionId > -1) {
				PredefinedCalendarDefinition definition = calendarStore.getPredefinedCalendarDefinition(definitionId);
				CalendarDefinitionForm command = new CalendarDefinitionForm();
				command.setId(definition.getId());
				command.setName(definition.getName());
				command.setClassName(definition.getClassName());
				command.setRole(definition.getDefaultRoles());
				command.addParameters(definition.getParameters());
				return command;
			} else {
				// otherwise, construct a brand new form
				// create the form
				return new CalendarDefinitionForm();
			}

		} else {
			// otherwise, construct a brand new form
			// create the form
			return new CalendarDefinitionForm();
		}
	}
	
	@RequestMapping(params = "action=editCalendarDefinition")
	public String getCalendarDefinitionForm(PortletRequest request){
		return "/editCalendarDefinition";
	}
	
	@RequestMapping(params = "action=editCalendarDefinition")
	public void updateCalendarDefinition(ActionRequest request, 
			ActionResponse response, @ModelAttribute CalendarDefinitionForm form) {
		
		// construct a calendar definition from the form data
		PredefinedCalendarDefinition definition = null;
		
		// If an id was submitted, retrieve the calendar definition we're
		// trying to edit.  Otherwise, create a new definition. 
		if (form.getId() > -1)
			definition = calendarStore.getPredefinedCalendarDefinition(form.getId());
		else
			definition = new PredefinedCalendarDefinition();

		// set the calendar definition properties based on the 
		// submitted form
		definition.setClassName(form.getClassName());
		definition.setDefaultRoles(form.getRole());
		definition.setName(form.getName());
		definition.setParameters(form.getParameters());

		// save the calendar definition
		calendarStore.storeCalendarDefinition(definition);
		
		// send the user back to the main administration page
		response.setRenderParameter("action", "administration");

	}

	private CalendarStore calendarStore;
	
	@Required
	@Resource(name="calendarStore")
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

}

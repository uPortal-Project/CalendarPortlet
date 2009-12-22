/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc.controller;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.mvc.CalendarDefinitionForm;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;


/**
 * EditCalendarDefinitionController provides a GUI for adding and editing 
 * predefined calendars.
 *
 * @author Jen Bourey
 */
public class EditCalendarDefinitionController extends SimpleFormController {

	public EditCalendarDefinitionController() { }
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.portlet.mvc.AbstractFormController#formBackingObject(javax.portlet.PortletRequest)
	 */
	protected Object formBackingObject(PortletRequest request) throws Exception {
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
	
	@Override
	protected void onSubmitAction(ActionRequest request,
			ActionResponse response, Object command, BindException errors)
			throws Exception {
		
		// get the form data
		CalendarDefinitionForm form = (CalendarDefinitionForm) command;

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
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

}

/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.mvc.controller;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import edu.yale.its.tp.portlets.calendar.PredefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.dao.CalendarStore;
import edu.yale.its.tp.portlets.calendar.mvc.CalendarDefinitionForm;

/**
 * EditCalendarDefinitionController provides a GUI for adding and editing 
 * predefined calendars.
 *
 * @author Jen Bourey
 */
public class EditCalendarDefinitionController extends SimpleFormController {

	private static Log log = LogFactory
			.getLog(EditCalendarDefinitionController.class);

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

/*
 * EditCalendarDefinitionController.java
 * 
 * Copyright (c) Feb 13, 2008 Yale University. All rights reserved.
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
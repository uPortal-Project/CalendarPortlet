/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.dao.CalendarStore;
import edu.yale.its.tp.portlets.calendar.mvc.CalendarListingCommand;

/**
 * EditCalendarDefinitionController allows a user to add or edit a user-defined
 * calendar definition.
 * 
 * @author Jen Bourey
 */
public class EditUserHttpICalController extends SimpleFormController {

	private static Log log = LogFactory
			.getLog(EditUserHttpICalController.class);
	private CalendarStore calendarStore;

	public EditUserHttpICalController() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.portlet.mvc.AbstractFormController#formBackingObject(javax.portlet.PortletRequest)
	 */
	protected Object formBackingObject(PortletRequest request) throws Exception {

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
				Map userinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
				String subscribeId = (String) userinfo.get(userToken);
				
				// create the form
				CalendarListingCommand command =  new CalendarListingCommand();
				command.setSubscribeId(subscribeId);
				return command;
			}

		} else {
			// otherwise, construct a brand new form

			// get user information
			Map userinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
			String subscribeId = (String) userinfo.get(userToken);
			
			// create the form
			CalendarListingCommand command =  new CalendarListingCommand();
			command.setSubscribeId(subscribeId);
			return command;
		}
	}

	@Override
	protected void onSubmitAction(ActionRequest request,
			ActionResponse response, Object command, BindException errors)
			throws Exception {
		
		// get the form data
		CalendarListingCommand form = (CalendarListingCommand) command;

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

	private String userToken = "user.login.id";
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

}


/*
 * EditUserHttpICalController.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
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
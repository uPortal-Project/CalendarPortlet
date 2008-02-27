/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.PredefinedCalendarDefinition;
import edu.yale.its.tp.portlets.calendar.UserDefinedCalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.dao.CalendarStore;

/**
 * EditCalendarPreferencesController provides the main edit page for the calendars
 * portlet.  The page allows users to view, add, delete and edit all available
 * calendars.
 * 
 * @author Jen Bourey
 */
public class EditCalendarPreferencesController extends AbstractController {

	private static Log log = LogFactory.getLog(EditCalendarPreferencesController.class);

	@Override
	public ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {

		Map<String, Object> model = new HashMap<String, Object>();

		// get user information
		Map userinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
		String subscribeId = (String) userinfo.get(userToken);

		// add the user-defined calendars to the model
		List<UserDefinedCalendarConfiguration> mycalendars = calendarStore.getUserDefinedCalendarConfigurations(subscribeId, false);
		model.put("mycalendars", mycalendars);

		// add the predefined calendars to the model
		List<PredefinedCalendarConfiguration> calendars = calendarStore.getPredefinedCalendarConfigurations(subscribeId, false);
		model.put("calendars", calendars);
		
		// get the user's role listings
		PortletSession session = request.getPortletSession();
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
			Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
			hidden.remove(config.getId());
		} else if (actionCode.equals("show")) {
			CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
			config.setDisplayed(true);
			calendarStore.storeCalendarConfiguration(config);
			Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
			hidden.remove(config.getId());
		} else if (actionCode.equals("hide")) {
			CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
			config.setDisplayed(false);
			calendarStore.storeCalendarConfiguration(config);
			Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
			hidden.remove(config.getId());
		} else if (actionCode.equals("showNew")) {
			// get user information
			Map userinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
			String subscribeId = (String) userinfo.get(userToken);
			PredefinedCalendarDefinition definition = (PredefinedCalendarDefinition) calendarStore.getCalendarDefinition(id);
			log.debug("definition to save " + definition.toString());
			PredefinedCalendarConfiguration config = new PredefinedCalendarConfiguration();
			config.setSubscribeId(subscribeId);
			config.setCalendarDefinition(definition);
			calendarStore.storeCalendarConfiguration(config);
		}
	}


	private String userToken = "user.login.id";
	public void setUserToken(String userToken) {
		this.userToken = userToken;
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


/*
 * EditCalendarPreferencesController.java
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
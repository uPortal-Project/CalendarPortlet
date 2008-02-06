package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
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
		Map userinfo = (Map) request.getAttribute("javax.portlet.userinfo");
		String role = (String) userinfo.get("contentGroup");
		String subscribeId = (String) userinfo.get("user.login.id");

		// add the user-defined calendars to the model
		List<UserDefinedCalendarConfiguration> mycalendars = calendarStore.getUserDefinedCalendarConfigurations(subscribeId, false);
		model.put("mycalendars", mycalendars);

		// add the predefined calendars to the model
		List<PredefinedCalendarConfiguration> calendars = calendarStore.getPredefinedCalendarConfigurations(subscribeId, false);
		model.put("calendars", calendars);
		
		List<PredefinedCalendarDefinition> definitions = calendarStore.getHiddenPredefinedCalendarDefinitions(subscribeId, role);
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
			Map userinfo = (Map) request.getAttribute("javax.portlet.userinfo");
			String subscribeId = (String) userinfo.get("user.login.id");
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

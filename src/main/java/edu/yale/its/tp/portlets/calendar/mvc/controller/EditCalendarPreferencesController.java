package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.portlet.mvc.SimpleFormController;

import edu.yale.its.tp.portlets.calendar.mvc.CalendarPreferencesCommand;

public class EditCalendarPreferencesController extends SimpleFormController {

	private static Log log = LogFactory.getLog(AdminCalendarController.class);

	@Override
	protected void onSubmitAction(ActionRequest request,
			ActionResponse response, Object command, BindException errors)
			throws Exception {
		
		CalendarPreferencesCommand form = (CalendarPreferencesCommand) command;

		PortletPreferences prefs = request.getPreferences();
		prefs.setValue("timezone", form.getTimezone());
		prefs.store();

		PortletSession session = request.getPortletSession();
		session.setAttribute("timezone", form.getTimezone(), PortletSession.APPLICATION_SCOPE);

		// send the user back to the main edit page
		response.setRenderParameter("action", "editSubscriptions");
		

	}

	@Override
	protected Map referenceData(PortletRequest request, Object command,
			Errors errors) throws Exception {
		
		Map data = super.referenceData(request, command, errors);
		if (data == null) {
			data = new HashMap();
		}

		data.put("timezones", TimeZone.getAvailableIDs());
		return data;
	}

	@Override
	protected Object formBackingObject(PortletRequest request) throws Exception {
		CalendarPreferencesCommand form = new CalendarPreferencesCommand();
		PortletPreferences prefs = request.getPreferences();
		form.setTimezone(prefs.getValue("timezone", "America/Los_Angeles"));
		return form;
	}

}

package org.jasig.portlet.calendar.mvc.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.jasig.portlet.calendar.mvc.CalendarPreferencesCommand;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.portlet.mvc.SimpleFormController;


public class EditCalendarPreferencesController extends SimpleFormController {

	@Override
	protected void onSubmitAction(ActionRequest request,
			ActionResponse response, Object command, BindException errors)
			throws Exception {
		
		CalendarPreferencesCommand form = (CalendarPreferencesCommand) command;

		PortletPreferences prefs = request.getPreferences();
		prefs.setValue("timezone", form.getTimezone());
		prefs.store();

		PortletSession session = request.getPortletSession();
		session.setAttribute("timezone", form.getTimezone());

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

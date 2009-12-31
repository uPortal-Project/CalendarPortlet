package org.jasig.portlet.calendar.mvc.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.jasig.portlet.calendar.mvc.CalendarPreferencesCommand;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("EDIT")
public class EditCalendarPreferencesController {

	private static final String FORM_NAME = "calendarPreferencesCommand";
	
    private List<String> timeZones = null;
    
    /**
     * Set the list of time zone IDs that should be presented as options for
     * user time zones.
     * 
     * @param timeZones
     */
    @Required
    @Resource(name="timeZones")
    public void setTimeZones(List<String> timeZones) {
            this.timeZones = timeZones;
    }


    /**
     * Return the form view.
     * 
     * @param request
     * @return
     */
    @RequestMapping(params = "action=editPreferences")
    public String getPreferencesForm(RenderRequest request) {
    	return "/editCalendarPreferences";
    }
    
    /**
     * Process the preferences update request.
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @RequestMapping(params = "action=editPreferences")
	public void updatePreferences(ActionRequest request,
			ActionResponse response, @ModelAttribute(FORM_NAME) CalendarPreferencesCommand form)
			throws Exception {
		
		PortletPreferences prefs = request.getPreferences();
		prefs.setValue("timezone", form.getTimezone());
		prefs.store();

		PortletSession session = request.getPortletSession();
		session.setAttribute("timezone", form.getTimezone());

		// send the user back to the main edit page
		response.setRenderParameter("action", "editSubscriptions");

	}

    /**
     * Return the list of available time zone IDs.
     * 
     * @return
     */
	@ModelAttribute("timezones")
	public List<String> getTimeZones() {
		return this.timeZones;
	}

	/**
	 * Return a pre-populated preferences form for the current user.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ModelAttribute(FORM_NAME)
	public CalendarPreferencesCommand getForm(PortletRequest request) throws Exception {
		CalendarPreferencesCommand form = new CalendarPreferencesCommand();
		PortletPreferences prefs = request.getPreferences();
		form.setTimezone(prefs.getValue("timezone", "America/New_York"));
		return form;
	}

}

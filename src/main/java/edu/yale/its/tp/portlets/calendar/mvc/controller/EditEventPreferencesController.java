package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;

import edu.yale.its.tp.portlets.calendar.mvc.YaleEventPreferences;

public class EditEventPreferencesController extends SimpleFormController {

	private static Log log = LogFactory
			.getLog(EditEventPreferencesController.class);

	public EditEventPreferencesController() {
		setCommandClass(YaleEventPreferences.class);
		setCommandName("prefs");
	}

	protected Object formBackingObject(PortletRequest request)
			throws Exception {
		PortletPreferences prefs = request.getPreferences();
		YaleEventPreferences eventPrefs = new YaleEventPreferences();
		String[] categories = prefs.getValues("category", new String[] {
				"Music", "Theater", "Talks" });
		eventPrefs.setCategories(categories);
		String days = prefs.getValue("days", "1");
		eventPrefs.setDays(days);
		return eventPrefs;
	}
	
	protected Map referenceData(PortletRequest request, Object command, Errors errors) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		List<String> categories = new ArrayList<String>();
		categories.add("Music");
		categories.add("Theater");
		categories.add("Talks");
		categories.add("Films");
		categories.add("Conferences/Symposia");
		categories.add("Biomedical Sciences");
		categories.add("Sports");
		categories.add("Tours");
		categories.add("Exhibitions");
		categories.add("Meetings");
		categories.add("Language Tables");
		categories.add("Religion");
		categories.add("Training");
		categories.add("And...");
		model.put("categories", categories);
		return model;
	}

	protected void processFormSubmission(ActionRequest request,
			ActionResponse response, Object command, BindException errors) {

		log.debug("Entering edit form");
		log.debug(command);
		PortletPreferences prefs = request.getPreferences();
		YaleEventPreferences eventPrefs = (YaleEventPreferences) command;
		log.debug("Found categories from command object "
				+ eventPrefs.getCategories().length);
		try {
			prefs.setValues("category", eventPrefs.getCategories());
			prefs.setValue("days", eventPrefs.getDays());
			prefs.store();
		} catch (ValidatorException e) {
			log.error("Failed to validate events preferences", e);
		} catch (IOException e) {
			log.error("Failed to store events preferences", e);
		} catch (ReadOnlyException e) {
			log.error("Failed to set read only exception", e);
		}
		try {
			response.setPortletMode(PortletMode.VIEW);
		} catch (PortletModeException e) {
			log.error("Error setting events portlet back to view mode", e);
		}

	}

	protected ModelAndView renderFormSubmission(RenderRequest request,
			RenderResponse response, Object command, BindException errors)
			throws Exception {
		log.debug("Rendering form");
		return showForm(request, response, errors);
	}

}

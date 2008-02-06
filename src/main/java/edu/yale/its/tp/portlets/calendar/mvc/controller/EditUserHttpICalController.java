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
import edu.yale.its.tp.portlets.calendar.adapter.HttpICalAdapter;
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
				Map userinfo = (Map) request.getAttribute("javax.portlet.userinfo");
				String subscribeId = (String) userinfo.get("user.login.id");
				
				// create the form
				CalendarListingCommand command =  new CalendarListingCommand();
				command.setSubscribeId(subscribeId);
				return command;
			}

		} else {
			// otherwise, construct a brand new form

			// get user information
			Map userinfo = (Map) request.getAttribute("javax.portlet.userinfo");
			String subscribeId = (String) userinfo.get("user.login.id");
			
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
		
		log.debug("called onsubmitaction");

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
			definition.setClassName(HttpICalAdapter.class.getName());
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
		response.setRenderParameter("action", "editPreferences");

	}

	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}
	

}

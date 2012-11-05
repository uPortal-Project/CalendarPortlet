/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.calendar.mvc.controller;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarEventsDao;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.CalendarPreferencesCommand;
import org.jasig.portlet.calendar.mvc.IViewSelector;
import org.jasig.portlet.calendar.service.SessionSetupInitializationService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * EditCalendarPreferencesController provides the main edit page for the calendars
 * portlet.  The page allows users to view, add, delete and edit all available
 * calendars.
 * 
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
public class EditCalendarSubscriptionsController implements ApplicationContextAware {

	private static final String FORM_NAME = "calendarPreferencesCommand";	

	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping
	public ModelAndView viewEditOptions(RenderRequest request, RenderResponse response) {
		return viewSubscriptions(request, response);
	}
	
    @ActionMapping
    public void defaultAction(ActionRequest request) {
        log.debug("executing default action request");
        // default action mapping
    }
    
    @RequestMapping(params = "action=editSubscriptions")
	public ModelAndView viewSubscriptions(RenderRequest request,
			RenderResponse response) {

		Map<String, Object> model = new HashMap<String, Object>();
		PortletSession session = request.getPortletSession();

		// get user information
		String subscribeId = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
		if ("guest".equalsIgnoreCase(subscribeId)) {
			model.put("guest", true);
		} else {
			model.put("guest", false);
		}

        // See if the timezone is a read-only preference, or not. If so, we
        // do not want them to be able to try and edit that value.
        PortletPreferences prefs = request.getPreferences();
        model.put( "timezoneReadOnly", prefs.isReadOnly( "timezone" ) );

		// add the user-defined calendars to the model
		List<UserDefinedCalendarConfiguration> mycalendars = calendarStore.getUserDefinedCalendarConfigurations(subscribeId, false);
		model.put("mycalendars", mycalendars);

		// add the predefined calendars to the model
		List<PredefinedCalendarConfiguration> calendars = calendarSetDao.getAvailablePredefinedCalendarConfigurations(request);
		model.put("calendars", calendars);
		
		// get the user's role listings
		@SuppressWarnings("unchecked")
		Set<String> userRoles = (Set<String>) session.getAttribute("userRoles");

		// get a list of predefined calendars the user doesn't 
		// currently have configured
		List<PredefinedCalendarDefinition> definitions = calendarStore.getHiddenPredefinedCalendarDefinitions(subscribeId, userRoles);
		model.put("hiddencalendars", definitions);
		
		model.put("predefinedEditActions", predefinedEditActions);
		
		// return the edit view
		String view = viewSelector.getEditViewName(request);
		return new ModelAndView(view, "model", model);
	}
    
    @ActionMapping(params = "action=deleteUserCalendar")
    public void removeSubscription(ActionRequest request, 
    		ActionResponse response, @RequestParam("configurationId") Long id) {
		CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
		calendarStore.deleteCalendarConfiguration(config);
		
		// remove the calendar from the hidden calendars list
    	PortletSession session = request.getPortletSession();
		@SuppressWarnings("unchecked")
		Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
		hidden.remove(config.getId());
		
		response.setRenderParameter("action", "editSubscriptions");
    }

    @ResourceMapping(value = "exportUserCalendar")
    public String exportCalendar(ResourceRequest request,
                                   ResourceResponse response, @RequestParam("configurationId") Long id) {
        CalendarConfiguration calendarConfig = calendarStore.getCalendarConfiguration(id);

        CalendarException exception = null;
        try {

            // get an instance of the adapter for this calendar
            ICalendarAdapter adapter = (ICalendarAdapter) applicationContext.getBean(calendarConfig
                    .getCalendarDefinition().getClassName());

            DateTime intervalStart = new DateTime().minusYears(1);
            DateTime intervalEnd = new DateTime().plusYears(1);
            Interval interval = new Interval(intervalStart, intervalEnd);
            Calendar calendar = calendarEventsDao.getCalendar(adapter, calendarConfig, interval, request);

            // Calendars should be fairly small, so no need to save file to disk or
            // buffer to calculate size.
            response.setContentType("text/calendar");
            response.addProperty("Content-disposition", "attachment; filename=calendar.ics");

            CalendarOutputter calendarOut = new CalendarOutputter();
            calendarOut.output(calendar, response.getWriter());
            response.flushBuffer();
            return null;

        } catch (NoSuchBeanDefinitionException ex) {
            exception = new CalendarException("Calendar adapter class instance could not be found", ex);
        } catch (Exception ex) {
            exception = new CalendarException ("Error sending calendar "
                    + calendarConfig.getCalendarDefinition().getName() + " to user for downloading", ex);
        }

        // Allow container to handle exceptions and give HTTP error
        throw exception;
    }

    @ActionMapping(params = "action=showCalendar")
    public void showCalendar(ActionRequest request, 
    		ActionResponse response, @RequestParam("configurationId") Long id) {
		CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
		config.setDisplayed(true);
		calendarStore.storeCalendarConfiguration(config);
		
		// remove the calendar from the hidden calendars list
    	PortletSession session = request.getPortletSession();
		@SuppressWarnings("unchecked")
		Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
		hidden.remove(config.getId());    	
		
		response.setRenderParameter("action", "editSubscriptions");
    }
    
    @ActionMapping(params = "action=hideCalendar")
    public void hideCalendar(ActionRequest request, 
    		ActionResponse response, @RequestParam("configurationId") Long id) {
		CalendarConfiguration config = calendarStore.getCalendarConfiguration(id);
		config.setDisplayed(false);
		calendarStore.storeCalendarConfiguration(config);
		
		// remove the calendar from the hidden calendars list
    	PortletSession session = request.getPortletSession();
		@SuppressWarnings("unchecked")
		Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenCalendars");
		hidden.remove(config.getId());    	
		
		response.setRenderParameter("action", "editSubscriptions");
    }
    
    @ActionMapping(params = "action=addSharedCalendar")
    public void addSharedCalendar(ActionRequest request,  
    		ActionResponse response, @RequestParam("definitionId") Long id) {
		PortletSession session = request.getPortletSession();
		String subscribeId = (String) session.getAttribute(SessionSetupInitializationService.USERNAME_KEY);
		PredefinedCalendarDefinition definition = (PredefinedCalendarDefinition) calendarStore.getCalendarDefinition(id);
		log.debug("definition to save " + definition.toString());
		PredefinedCalendarConfiguration config = new PredefinedCalendarConfiguration();
		config.setSubscribeId(subscribeId);
		config.setCalendarDefinition(definition);
		calendarStore.storeCalendarConfiguration(config);
		
		response.setRenderParameter("action", "editSubscriptions");
    }

    /**
     * Process the preferences update request.
     * 
     * @param request
     * @param response
     * @param form
     * @throws Exception
     */
    @ActionMapping(params = "action=editPreferences")
	public void updatePreferences(ActionRequest request,
			ActionResponse response, @ModelAttribute(FORM_NAME) CalendarPreferencesCommand form)
			throws Exception {
		
		PortletPreferences prefs = request.getPreferences();

        // If the timezone preference is read only don't try to change it.
        // Pluto will throw an exception if you do.
        if ( prefs.isReadOnly( "timezone" ) == false ) {
            prefs.setValue("timezone", form.getTimezone());
            prefs.store();

            PortletSession session = request.getPortletSession();
            session.setAttribute("timezone", form.getTimezone());
        }

		// send the user back to the main edit page
		response.setRenderParameter("action", "editSubscriptions");
		
		// provide feedback indicating the preferences were saved successfully
        response.setRenderParameter("preferencesSaved", "true");

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

	private Map<String, String> predefinedEditActions = new HashMap<String, String>();
	
	@Required
	@Resource(name="predefinedEditActions")
	public void setPredefinedEditActions(Map<String, String> predefinedEditActions) {
		this.predefinedEditActions = predefinedEditActions;
	}

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

	private CalendarStore calendarStore;

    private CalendarEventsDao calendarEventsDao;

    @Autowired(required = true)
    public void setCalendarEventsDao(CalendarEventsDao calendarEventsDao) {
        this.calendarEventsDao = calendarEventsDao;
    }

    @Required
	@Resource(name="calendarStore")
	public void setCalendarStore(CalendarStore calendarStore) {
		this.calendarStore = calendarStore;
	}

    private ICalendarSetDao calendarSetDao;
    
    @Autowired(required = true)
    public void setCalendarSetDao(ICalendarSetDao calendarSetDao) {
        this.calendarSetDao = calendarSetDao;
    }

    private IViewSelector viewSelector;

    @Autowired(required = true)
    public void setViewSelector(IViewSelector viewSelector) {
        this.viewSelector = viewSelector;
    }

    private ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarEventsDao;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.mvc.CalendarHelper;
import org.jasig.portlet.calendar.mvc.UICalendarEventsBuilder;
import org.jasig.portlet.calendar.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

@Controller
@RequestMapping("VIEW")
public class AjaxCalendarController implements ApplicationContextAware {
	@Autowired
	private UICalendarEventsBuilder uiCalendarEventBuiler;

    @Autowired(required = true)
    private CalendarHelper helper;

    @Autowired(required = true)
    private CalendarEventsDao calendarEventsDao;

    @Autowired(required = true)
	private ICalendarSetDao calendarSetDao;


    private CalendarStore calendarStore;
    private ApplicationContext applicationContext;
	protected final Log log = LogFactory.getLog(this.getClass());


    @ActionMapping(params = "action=showDatePicker")
    public void toggleShowDatePicker(@RequestParam(value = "show") String show,
                                     ActionRequest request,
                                     ActionResponse response) {
        try {

            request.getPreferences().setValue("showDatePicker",show);
            request.getPreferences().store();
        } catch(Exception exception) {
            log.info("Exception encountered saving preference: PREFERENCE=showDatePicker, EXCEPTION="+exception);
        }
    }

	@ResourceMapping
	public ModelAndView getEventList(ResourceRequest request,
			ResourceResponse response) throws Exception {
        final String resourceId = request.getResourceID();
        final String[] resourceIdTokens = resourceId.split("_");        
        final String startDate = resourceIdTokens[0];
        final int days = Integer.parseInt(resourceIdTokens[1]);
        final String requestEtag  = resourceIdTokens.length > 2
                ?resourceIdTokens[2]: "";
        final long startTime = System.currentTimeMillis();
        final List<String> errors = new ArrayList<String>();
        final Interval interval = DateUtil.getInterval(startDate, days,request);
        final Set<CalendarDisplayEvent> calendarEvents = helper.getEventList(errors,interval,request);
        Map <String,Object> model = uiCalendarEventBuiler.buildUIEvents(calendarEvents,request,errors);
		String etag = String.valueOf(model.hashCode());
		response.getCacheControl().setETag(etag);
		response.getCacheControl().setUseCachedContent(false);
		response.getCacheControl().setExpirationTime(0);
        if (!requestEtag.isEmpty() && etag.equals(requestEtag)) {
            if (log.isTraceEnabled()) {
                log.trace("Sending an empty response (due to matched ETag and " 
                            + "refresh=false) for user '" 
                            + request.getRemoteUser() + "'");
            }
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_NOT_MODIFIED));
            // returning null appears to cause the response to be committed
            // before returning to the portal, so just use an empty view
            return new ModelAndView("empty", Collections.<String,String>emptyMap());
        }
        if (log.isTraceEnabled()) {
            log.trace("Sending a full response for user '" + request.getRemoteUser());
        }
        long overallTime = System.currentTimeMillis() - startTime;
        log.debug("AjaxCalendarController took " + overallTime + " ms to produce JSON model");
        return new ModelAndView("json", model);
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

    @Required
    @Resource(name="calendarStore")
    public void setCalendarStore(CalendarStore calendarStore) {
        this.calendarStore = calendarStore;
    }

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}

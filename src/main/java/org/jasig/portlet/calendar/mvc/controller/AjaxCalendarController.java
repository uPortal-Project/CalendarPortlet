/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.calendar.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarEventsDao;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.mvc.CalendarHelper;
import org.jasig.portlet.calendar.mvc.JsonCalendarEventWrapper;
import org.jasig.portlet.calendar.util.DateUtil;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  @ActionMapping(params = "action=showDatePicker")
  public void toggleShowDatePicker(
      @RequestParam(value = "show") String show, ActionRequest request) {
    try {

      request.getPreferences().setValue("showDatePicker", show);
      request.getPreferences().store();
    } catch (Exception exception) {
      log.info(
          "Exception encountered saving preference: PREFERENCE=showDatePicker, EXCEPTION="
              + exception);
    }
  }

  @ResourceMapping
  public ModelAndView getEventList(ResourceRequest request, ResourceResponse response)
      throws Exception {

    // Pull parameters out of the resourceId
    final String resourceId = request.getResourceID();
    final String[] resourceIdTokens = resourceId.split("-");
    final String startDate = resourceIdTokens[0];
    final int days = Integer.parseInt(resourceIdTokens[1]);

    final long startTime = System.currentTimeMillis();
    final List<String> errors = new ArrayList<String>();
    final Map<String, Object> model = new HashMap<String, Object>();
    final PortletSession session = request.getPortletSession();
    // get the user's configured time zone
    final String timezone = (String) session.getAttribute("timezone");
    final DateTimeZone tz = DateTimeZone.forID(timezone);

    // get the period for this request
    final Interval interval = DateUtil.getInterval(startDate, days, request);

    final Set<CalendarDisplayEvent> calendarEvents = helper.getEventList(errors, interval, request);


    final Set<JsonCalendarEventWrapper> events = new TreeSet<JsonCalendarEventWrapper>();
    for (CalendarDisplayEvent e : calendarEvents) {
      events.add(new JsonCalendarEventWrapper(e, e.getCalendarIndex()));
    }

    /*
     * Transform the event set into a map keyed by day.  This code is
     * designed to separate events by day according to the user's configured
     * time zone.  This ensures that we keep complicated time-zone handling
     * logic out of the JavaScript.
     *
     * Events are keyed by a string uniquely representing the date that is
     * still orderable.  So that we can display a more user-friendly date
     * name, we also create a map representing date display names for each
     * date keyed in this response.
     */

    // define a DateFormat object that uniquely identifies dates in a way
    // that can easily be ordered
    DateTimeFormatter orderableDf =
        new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter().withZone(tz);

    // define a DateFormat object that can produce user-facing display
    // as user-facing get it from i18N
    final String displayPattern =
        this.applicationContext.getMessage(
            "date.formatter.display", null, "EEE MMM d", request.getLocale());
    // names for dates
    DateTimeFormatter displayDf =
        new DateTimeFormatterBuilder().appendPattern(displayPattern).toFormatter().withZone(tz);

    // define "today" and "tomorrow" so we can display these specially in the user interface
    DateMidnight now = new DateMidnight(tz);
    String today = orderableDf.print(now);
    String tomorrow = orderableDf.print(now.plusDays(1));

    Map<String, String> dateDisplayNames = new HashMap<String, String>();
    Map<String, List<JsonCalendarEventWrapper>> eventsByDay =
        new LinkedHashMap<String, List<JsonCalendarEventWrapper>>();
    for (JsonCalendarEventWrapper event : events) {
      String day = orderableDf.print(event.getEvent().getDayStart());

      // if we haven't seen this day before, add entries to the event and date name maps
      if (!eventsByDay.containsKey(day)) {

        // add a list for this day to the eventsByDay map
        eventsByDay.put(day, new ArrayList<JsonCalendarEventWrapper>());

        // Add an appropriate day name for this date to the date names map.
        // If the day appears to be today or tomorrow display a special string value.
        // Otherwise, use the user-facing date format object.
        if (today.equals(day)) {
          dateDisplayNames.put(
              day, applicationContext.getMessage("today", null, "Today", request.getLocale()));
        } else if (tomorrow.equals(day)) {
          dateDisplayNames.put(
              day,
              this.applicationContext.getMessage(
                  "tomorrow", null, "Tomorrow", request.getLocale()));
        } else {
          dateDisplayNames.put(day, displayDf.print(event.getEvent().getDayStart()));
        }
      }

      // add the event to the by-day map
      eventsByDay.get(day).add(event);
    }

    log.trace(
        "Prepared the following eventsByDay collection for user {}: {}",
        request.getRemoteUser(),
        eventsByDay);

    model.put("dateMap", eventsByDay);
    model.put("dateNames", dateDisplayNames);
    model.put("viewName", "jsonView");
    model.put("errors", errors);

    // eTag processing, see https://wiki.jasig.org/display/UPM41/Portlet+Caching
    String etag = String.valueOf(model.hashCode());
    String requestEtag = request.getETag();
    // if the request ETag matches the hash for this response, send back
    // an empty response indicating that cached content should be used
    if (etag.equals(requestEtag)) {
      log.debug(
          "Sending an empty response (due to matched ETag {} for user {})'",
          requestEtag,
          request.getRemoteUser());

      // Must communicate new expiration time > 0 per Portlet Spec 22.2
      response.getCacheControl().setExpirationTime(1);
      response
          .getCacheControl()
          .setUseCachedContent(true); // Portal will return cached content or HTTP 304
      // Return null so response is not committed before portal decides to return HTTP 304 or cached response.
      return null;
    }

    log.trace("Sending a full response for user {}", request.getRemoteUser());

    // create new content with new validation tag
    response.getCacheControl().setETag(etag);
    // Must have expiration time > 0 to use response.getCacheControl().setUseCachedContent(true)
    response.getCacheControl().setExpirationTime(1);

    long overallTime = System.currentTimeMillis() - startTime;
    log.debug("AjaxCalendarController took {} ms to produce JSON model", overallTime);

    return new ModelAndView("json", model);
  }

  @ResourceMapping(value = "exportUserCalendar")
  public String exportCalendar(
      ResourceRequest request,
      ResourceResponse response,
      @RequestParam("configurationId") Long id) {
    CalendarConfiguration calendarConfig = calendarStore.getCalendarConfiguration(id);

    CalendarException exception = null;
    try {

      // get an instance of the adapter for this calendar
      ICalendarAdapter adapter =
          (ICalendarAdapter)
              applicationContext.getBean(calendarConfig.getCalendarDefinition().getClassName());

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
      exception =
          new CalendarException(
              "Error sending calendar "
                  + calendarConfig.getCalendarDefinition().getName()
                  + " to user for downloading",
              ex);
    }

    // Allow container to handle exceptions and give HTTP error
    throw exception;
  }

  @Autowired(required = true)
  private CalendarHelper helper;

  @Autowired(required = true)
  private CalendarEventsDao calendarEventsDao;

  @Autowired(required = true)
  private ICalendarSetDao calendarSetDao;

  private CalendarStore calendarStore;

  @Required
  @Resource(name = "calendarStore")
  public void setCalendarStore(CalendarStore calendarStore) {
    this.calendarStore = calendarStore;
  }

  private ApplicationContext applicationContext;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}

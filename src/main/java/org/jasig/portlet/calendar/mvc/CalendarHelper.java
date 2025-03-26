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
package org.jasig.portlet.calendar.mvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarConfigurationByNameComparator;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.adapter.CalendarEventsDao;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.adapter.UserFeedbackCalendarException;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/** @author Chris Waymire (chris@waymire.net) */
@Component
public class CalendarHelper implements ApplicationContextAware {

  protected static final Log log = LogFactory.getLog(CalendarHelper.class);

  @Autowired(required = true)
  private CalendarEventsDao calendarEventsDao;

  @Autowired(required = true)
  private ICalendarSetDao calendarSetDao;

  private ApplicationContext applicationContext;

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public Set<CalendarDisplayEvent> getEventList(
      final List<String> errors, final Interval interval, final PortletRequest request) {

    final PortletSession session = request.getPortletSession();

    /*
     * Retrieve the calendar configurations defined for this user request
     * and sort them by display name.  This sorting operation ensures that
     * the CSS color indices assigned to calendar events will be consistent
     * with the colors assigned in the main controller.
     */

    // retrieve the calendars defined for this portlet instance
    CalendarSet<?> set = calendarSetDao.getCalendarSet(request);
    List<CalendarConfiguration> calendars = new ArrayList<CalendarConfiguration>();
    calendars.addAll(set.getConfigurations());

    // sort the calendars
    Collections.sort(calendars, new CalendarConfigurationByNameComparator());

    // get the list of hidden calendars
    @SuppressWarnings("unchecked")
    HashMap<Long, String> hiddenCalendars =
        (HashMap<Long, String>) session.getAttribute("hiddenCalendars");

    /*
     * For each unhidden calendar, get the list of associated events for
     * the requested time period.
     */

    // get the user's configured time zone
    String timezone = (String) session.getAttribute("timezone");
    DateTimeZone tz = DateTimeZone.forID(timezone);
    Set<CalendarDisplayEvent> events = new TreeSet<CalendarDisplayEvent>();

    int calendarIndex=0; //to keep the color of the calendar consistent with the order in the main controller
    for (CalendarConfiguration callisting : calendars) {
      // don't bother to fetch hidden calendars
      if (hiddenCalendars.get(callisting.getId()) == null) {
        try {
          // get an instance of the adapter for this calendar
          ICalendarAdapter adapter =
              (ICalendarAdapter)
                  applicationContext.getBean(callisting.getCalendarDefinition().getClassName());
          events.addAll(calendarEventsDao.getEvents(adapter, callisting, interval, request, tz, calendarIndex));
        } catch (NoSuchBeanDefinitionException ex) {
          log.error("Calendar class instance could not be found: " + ex.getMessage());
        } catch (UserFeedbackCalendarException sce) {
          // This CalendarException subclass carries a payload for the UI...
          StringBuilder msg = new StringBuilder();
          msg.append(callisting.getCalendarDefinition().getName())
              .append(":  ")
              .append(sce.getUserFeedback());
          errors.add(msg.toString());
        } catch (Exception ex) {
          log.warn("Unknown Error", ex);
          errors.add(
              "The calendar \""
                  + callisting.getCalendarDefinition().getName()
                  + "\" is currently unavailable.");
        }
      }
      calendarIndex++;
    }
    return events;
  }
}

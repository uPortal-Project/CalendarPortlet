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

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequest;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockPortletSession;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.portlet.ModelAndView;

public class CalendarControllerTest {

  private ICalendarSetDao calendarDao;
  private CalendarSet<UserDefinedCalendarConfiguration> calendarSet;
  private CalendarController testee;
  private MockRenderRequest mockRequest;
  private MockPortletSession mockSession;

  @Before
  public void startUp() {
    mockRequest = new MockRenderRequest();
    mockSession = new MockPortletSession();
    initMocks(this);
    calendarDao =
        new ICalendarSetDao() {

          @Override
          public CalendarSet<?> getCalendarSet(PortletRequest request) {
            return calendarSet;
          }

          @Override
          public List<PredefinedCalendarConfiguration> getAvailablePredefinedCalendarConfigurations(
              PortletRequest request) {
            return null;
          }
        };
    calendarSet = new CalendarSet<UserDefinedCalendarConfiguration>(Collections.emptySet());
    testee = new CalendarController();
    mockRequest.setSession(mockSession);
    ReflectionTestUtils.setField(testee, "calendarSetDao", calendarDao);
  }

  @Test
  public void testDatePickerIsShownByDefault() {
    mockSession.setAttribute("startDate", new DateMidnight());
    mockSession.setAttribute("days", 1);
    ModelAndView mv = testee.getCalendar(null, mockRequest);
    Map<String, Object> model = (Map<String, Object>) mv.getModel().get("model");
    assertEquals("true", model.get("showDatePicker"));
  }
}

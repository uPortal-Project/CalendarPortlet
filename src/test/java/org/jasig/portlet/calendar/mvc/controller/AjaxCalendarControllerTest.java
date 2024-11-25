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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.portlet.ResourceRequest;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.mvc.CalendarHelper;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.portlet.MockResourceRequest;
import org.springframework.mock.web.portlet.MockResourceResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.portlet.ModelAndView;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class AjaxCalendarControllerTest {
  // Hashcode of model object that used an empty CalendarDisplayEvent set.
  private static final String emptyEventsModelHashcode = "-971201553";

  private AjaxCalendarController testee;
  private MockResourceRequest request;
  private MockResourceResponse response;
  private Set<CalendarDisplayEvent> events;
  private Set<CalendarDisplayEvent> emptyEvents;
  @Mock private CalendarHelper mockHelper;
  @Mock private Map<String, Object> nonEmptyMap;
  @Mock private ApplicationContext appContext;

  @Before
  public void setUp() {
    initMocks(this);
    testee = new AjaxCalendarController();
    request = new MockResourceRequest();
    response = new MockResourceResponse();
    nonEmptyMap = new HashMap<String, Object>();
    nonEmptyMap.put("aaa", new Object());
    request.setResourceID("01032011-7");
    ReflectionTestUtils.setField(testee, "helper", mockHelper);

    emptyEvents = new HashSet<CalendarDisplayEvent>();
    events = new HashSet<CalendarDisplayEvent>();

    when(appContext.getMessage(
            eq("date.formatter.display"), any(Object[].class), anyString(), any(Locale.class)))
        .thenReturn("EEE MMM d");
    testee.setApplicationContext(appContext);
  }

  @Test
  public void testWhenRequestEtagMatchesModelEtagThenEmptyModelAndViewReturned() throws Exception {
    request.setProperty(ResourceRequest.ETAG, emptyEventsModelHashcode);
    when(mockHelper.getEventList(any(List.class), any(Interval.class), eq(request)))
        .thenReturn(emptyEvents);
    ModelAndView result = testee.getEventList(request, response);
    assertEquals(1, response.getCacheControl().getExpirationTime());
    assertTrue(response.getCacheControl().useCachedContent());
    assertNull("Null should be returned for eTag match", result);
  }

  @Test
  public void testWhenRequestEtagDoesNotMatchModelEtagThenPopulatedModelAndViewReturned()
      throws Exception {
    events.add(createEvent(null, null));
    when(mockHelper.getEventList(any(List.class), any(Interval.class), eq(request)))
        .thenReturn(events);
    ModelAndView result = testee.getEventList(request, response);
    assertEquals("json", result.getViewName());
    assertFalse(result.getModel().isEmpty());
    assertEquals(1, ((Map) result.getModel().get("dateMap")).size());
    assertEquals(1, ((Map) result.getModel().get("dateNames")).size());
  }

  /**
   * Returns a <code>CalendarDisplayEvent</code> at the indicated start and duration. Either of the
   * parameters can be null which would use default values. Currently eventStart defaults to use the
   * default timezone.
   *
   * @param eventStart Start of the event. If null defaults to Jan 3, 2011 at 4:00am
   * @param duration Duration of the event. If null defaults to 1 hour.
   * @return CalendarDisplayEvent
   */
  private CalendarDisplayEvent createEvent(org.joda.time.DateTime eventStart, Duration duration) {
    if (eventStart == null) {
      eventStart = new org.joda.time.DateTime(2011, 1, 3, 4, 0);
    }

    org.joda.time.DateTime eventEnd =
        eventStart.plus(duration != null ? duration : new Duration(1000 * 60 * 60));

    VEvent event =
        new VEvent(
            new DateTime(eventStart.toDate()), new DateTime(eventEnd.toDate()), "Test Event 1 day");

    org.joda.time.DateTime startOfTheSpecificDay = eventStart.withTimeAtStartOfDay();
    org.joda.time.DateTime endOfTheSpecificDay = eventStart.plusDays(1).withTimeAtStartOfDay();
    Interval specificDayInterval = new Interval(startOfTheSpecificDay, endOfTheSpecificDay);

    // event interval only goes to the end of the day if it extends beyond the day
    Interval eventInterval =
        new Interval(
            eventStart, eventEnd.isAfter(endOfTheSpecificDay) ? endOfTheSpecificDay : eventEnd);
    DateTimeFormatter dateFormatter =
        new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
    DateTimeFormatter timeFormatter =
        new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter();

    int calendarIndex=0;

    return new CalendarDisplayEvent(
        event, eventInterval, specificDayInterval, dateFormatter, timeFormatter, calendarIndex);
  }
}

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
package org.jasig.portlet.calendar.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.exchange.messages.GetUserAvailabilityResponse;
import com.microsoft.exchange.types.CalendarEventDetails;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.portlet.PortletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.VEventStartComparator;
import org.jasig.portlet.calendar.adapter.exchange.IExchangeCredentialsInitializationService;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceOperations;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/exchangeTestContext.xml")
public class ExchangeCalendarAdapterTest {

  protected final Log log = LogFactory.getLog(getClass());

  @Autowired(required = true)
  ApplicationContext applicationContext;

  @Autowired(required = true)
  Jaxb2Marshaller marshaller;

  @Autowired(required = true)
  Cache cache;

  @Mock CalendarConfiguration config;
  @Mock WebServiceOperations webService;
  @Mock PortletRequest request;
  @Mock ICacheKeyGenerator keyGenerator;
  @Mock IExchangeCredentialsInitializationService credentialsService;

  ExchangeCalendarAdapter adapter = new ExchangeCalendarAdapter();
  String user = "user1";
  Resource sampleExchangeResponse;
  Interval interval;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);

    adapter.setWebServiceOperations(webService);
    adapter.setCache(cache);
    adapter.setCredentialsService(credentialsService);
    adapter.setCacheKeyGenerator(keyGenerator);

    sampleExchangeResponse =
        applicationContext.getResource("classpath:/sampleExchangeResponse.xml");

    when(keyGenerator.getKey(
            any(CalendarConfiguration.class),
            any(Interval.class),
            any(PortletRequest.class),
            anyString()))
        .thenReturn("key");
    when(credentialsService.getNtlmDomain(request)).thenReturn("ed.ac.uk");
    when(credentialsService.usesExchangeImpersonation(request)).thenReturn(false);
    when(credentialsService.getImpersonatedAccountId(request)).thenReturn(null);

    Map<String, String> userInfo = new HashMap<String, String>();
    userInfo.put("user.login.id", user);
    userInfo.put("mail", "foo@mail.edu");
    when(request.getAttribute(PortletRequest.USER_INFO)).thenReturn(userInfo);

    DateTime start = new DateTime(2010, 10, 1, 0, 0, DateTimeZone.UTC);
    interval = new Interval(start, start.plusMonths(1));

    Source source = new StreamSource(sampleExchangeResponse.getInputStream());
    GetUserAvailabilityResponse response =
        (GetUserAvailabilityResponse) marshaller.unmarshal(source);
    when(webService.marshalSendAndReceive(any(Object.class), any(WebServiceMessageCallback.class)))
        .thenReturn(response);
  }

  // Test commented out because it works, but it causes failures in whatever test runs after it for some reason.
  //    @Test
  public void testCache() throws DatatypeConfigurationException {

    Cache cacheSpy = spy(cache);
    adapter.setCache(cacheSpy);
    adapter.getEvents(config, interval, request);
    Element cachedItem = cacheSpy.get("key");
    adapter.getEvents(config, interval, request);
    verify(cacheSpy, times(3)).get("key");
    verify(cacheSpy, times(1)).put(cachedItem);
    cacheSpy = null;
    adapter.setCache(cache);
  }

  @Test
  public void testRetrieveEvents() throws IOException, DatatypeConfigurationException {

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    List<VEvent> events = new ArrayList<VEvent>();
    events.addAll(adapter.getEvents(config, interval, request).getEvents());

    Collections.sort(events, new VEventStartComparator());
    assertEquals(2, events.size());

    VEvent event = events.get(0);
    assertEquals("Eat Lunch", event.getSummary().getValue());
    assertEquals("Somewhere Tasty", event.getLocation().getValue());
    cal.setTimeInMillis(event.getStartDate().getDate().getTime());
    assertEquals(cal.get(Calendar.YEAR), 2010);
    assertEquals(cal.get(Calendar.MONTH), 10);
    assertEquals(cal.get(Calendar.DATE), 16);
    assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12);
    assertEquals(cal.get(Calendar.MINUTE), 0);
    cal.setTimeInMillis(event.getEndDate().getDate().getTime());
    assertEquals(cal.get(Calendar.YEAR), 2010);
    assertEquals(cal.get(Calendar.MONTH), 10);
    assertEquals(cal.get(Calendar.DATE), 16);
    assertEquals(cal.get(Calendar.HOUR_OF_DAY), 13);
    assertEquals(cal.get(Calendar.MINUTE), 0);

    event = events.get(1);
    assertEquals("Wake Up", event.getSummary().getValue());
    assertNull(event.getLocation());
    cal.setTimeInMillis(event.getStartDate().getDate().getTime());
    assertEquals(cal.get(Calendar.HOUR_OF_DAY), 7);
    cal.setTimeInMillis(event.getStartDate().getDate().getTime());
    assertEquals(cal.get(Calendar.YEAR), 2010);
    assertEquals(cal.get(Calendar.MONTH), 10);
    assertEquals(cal.get(Calendar.DATE), 18);
    assertEquals(cal.get(Calendar.HOUR_OF_DAY), 7);
    assertEquals(cal.get(Calendar.MINUTE), 0);
  }

  @Test
  public void testGetInternalEvent() throws DatatypeConfigurationException {
    com.microsoft.exchange.types.CalendarEvent msEvent =
        new com.microsoft.exchange.types.CalendarEvent();

    // set the test event start time to 4AM on November 1, 2010
    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
    XMLGregorianCalendar start = datatypeFactory.newXMLGregorianCalendar();
    start.setYear(2010);
    start.setMonth(11);
    start.setDay(1);
    start.setTime(4, 0, 0, 0);
    msEvent.setStartTime(start);

    // set the test event end time to 5AM on November 1, 2010
    XMLGregorianCalendar end = datatypeFactory.newXMLGregorianCalendar();
    end.setYear(2010);
    end.setMonth(12);
    end.setDay(1);
    end.setTime(5, 0, 0, 0);
    msEvent.setEndTime(end);

    // set the event tname and location
    CalendarEventDetails details = new CalendarEventDetails();
    details.setSubject("Naptime");
    details.setLocation("My house");
    msEvent.setCalendarEventDetails(details);

    // transform the Microsoft calendar event into a calendar portlet event
    VEvent event = adapter.getInternalEvent(3, msEvent);

    // ensure the calendar id, summary, and location are all set correctly
    assertEquals("Naptime", event.getSummary().getValue());
    assertEquals("My house", event.getLocation().getValue());

    // check the start time
    Calendar cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
    cal.setTimeInMillis(event.getStartDate().getDate().getTime());
    assertEquals(4, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(java.util.TimeZone.getTimeZone("UTC"), cal.getTimeZone());
    assertTrue(event.getStartDate().isUtc());
    assertNull(event.getStartDate().getTimeZone());

    // check the end time
    cal.setTimeInMillis(event.getEndDate().getDate().getTime());
    assertEquals(5, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(java.util.TimeZone.getTimeZone("UTC"), cal.getTimeZone());
    assertTrue(event.getEndDate().isUtc());
    assertNull(event.getEndDate().getTimeZone());
  }

  @Test
  public void testGetXmlDate() throws DatatypeConfigurationException {
    // construct a calendar representing 4:30PM on June 4, 2010
    DateTime date = new DateTime(2010, 6, 3, 16, 30, DateTimeZone.UTC);

    XMLGregorianCalendar xmlCal = adapter.getXmlDate(date);
    assertEquals(2010, xmlCal.getYear());
    assertEquals(6, xmlCal.getMonth());
    assertEquals(3, xmlCal.getDay());
    assertEquals(16, xmlCal.getHour());
    assertEquals(30, xmlCal.getMinute());
    assertEquals(0, xmlCal.getSecond());
    assertEquals(0, xmlCal.getFractionalSecond().intValue());
  }
}

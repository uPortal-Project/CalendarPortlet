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

package org.jasig.portlet.calendar.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.portlet.PortletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.VEventStartComparator;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
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

import com.microsoft.exchange.messages.GetUserAvailabilityResponse;
import com.microsoft.exchange.types.CalendarEventDetails;

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

    ExchangeCalendarAdapter adapter = spy(new ExchangeCalendarAdapter());  
    String emailAddress = "user1@school.edu";
    Resource sampleExchangeResponse;
    Period period;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        sampleExchangeResponse = applicationContext.getResource("classpath:/sampleExchangeResponse.xml"); 
        adapter.setWebServiceOperations(webService);
        adapter.setCache(cache);
        
        when(keyGenerator.getKey(any(CalendarConfiguration.class), any(Period.class), any(PortletRequest.class), anyString())).thenReturn("key");
        adapter.setCacheKeyGenerator(keyGenerator);
        
        adapter.setEmailAttribute("email");
        when(request.getAttribute(PortletRequest.USER_INFO)).thenReturn(Collections.singletonMap("email", emailAddress));

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 10);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateTime start = new DateTime();
        start.setTime(cal.getTimeInMillis());
        
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 1);
        DateTime end = new DateTime(cal.getTime());

        period = new Period(start, end);

        Source source = new StreamSource(sampleExchangeResponse.getInputStream());
        GetUserAvailabilityResponse response = (GetUserAvailabilityResponse) marshaller.unmarshal(source);
        when(webService.marshalSendAndReceive(any(), any(WebServiceMessageCallback.class))).thenReturn(response);
    }
    
    @Test
    public void testCache() throws DatatypeConfigurationException {
        doReturn(Collections.<VEvent>emptySet()).when(adapter).retrieveExchangeEvents(config, period, emailAddress);
        adapter.getEvents(config, period, request);
        adapter.getEvents(config, period, request);
//        verify(adapter, times(1)).retrieveExchangeEvents(config, period, emailAddress);
    }
 
    @Test 
    public void testRetrieveEvents() throws IOException, DatatypeConfigurationException {
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        List<VEvent> events = new ArrayList<VEvent>();
        events.addAll(adapter.retrieveExchangeEvents(config, period, emailAddress));
        
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
        com.microsoft.exchange.types.CalendarEvent msEvent = new com.microsoft.exchange.types.CalendarEvent();

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
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DATE, 3);
        DateTime date = new DateTime();
        date.setUtc(true);
        date.setTime(cal.getTimeInMillis());
        
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

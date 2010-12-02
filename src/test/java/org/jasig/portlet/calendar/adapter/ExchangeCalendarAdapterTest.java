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

import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.fortuna.ical4j.model.DateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.microsoft.exchange.types.CalendarEventDetails;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/exchangeTestContext.xml")
public class ExchangeCalendarAdapterTest {

    protected final Log log = LogFactory.getLog(getClass());

    ExchangeCalendarAdapter adapter = new ExchangeCalendarAdapter();
    
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
        CalendarEvent event = adapter.getInternalEvent(3, msEvent);

        // ensure the calendar id, summary, and location are all set correctly
        assertEquals(3, event.getCalendarId().intValue(), 3);
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
        Calendar cal = Calendar.getInstance();
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

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

import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.fortuna.ical4j.model.Date;

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

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar start = datatypeFactory.newXMLGregorianCalendar(); 
        start.setYear(2010);
        start.setMonth(11);
        start.setDay(1);
        start.setTime(0, 0, 0, 0);
        msEvent.setStartTime(start);
        
        XMLGregorianCalendar end = datatypeFactory.newXMLGregorianCalendar();
        end.setYear(2010);
        end.setMonth(12);
        end.setDay(1);
        end.setTime(0, 0, 0, 0);
        msEvent.setEndTime(end);

        CalendarEventDetails details = new CalendarEventDetails();
        details.setSubject("Naptime");
        details.setLocation("My house");
        msEvent.setCalendarEventDetails(details);
        
        CalendarEvent event = adapter.getInternalEvent(3, msEvent);
        assertEquals(3, event.getCalendarId().intValue(), 3);
        assertEquals("Naptime", event.getSummary().getValue());
        assertEquals("My house", event.getLocation().getValue());
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
        Date date = new Date(cal.getTimeInMillis());
        
        XMLGregorianCalendar xmlCal = adapter.getXmlDate(date);
//        assertEquals(2010, xmlCal.getYear());
//        assertEquals(6, xmlCal.getMonth());
//        assertEquals(3, xmlCal.getDay());
//        assertEquals(16, xmlCal.getHour());
//        assertEquals(30, xmlCal.getMinute());
//        assertEquals(0, xmlCal.getSecond());
//        assertEquals(0, xmlCal.getFractionalSecond());
    }
    
}

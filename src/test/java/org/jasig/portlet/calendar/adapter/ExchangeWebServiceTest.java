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

import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;

import javax.portlet.PortletRequest;
import javax.xml.datatype.DatatypeConfigurationException;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.sf.ehcache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.CalendarEvent;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.client.core.WebServiceOperations;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/exchangeTestContext.xml")
public class ExchangeWebServiceTest {
    
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired(required = true)
    WebServiceOperations webServiceOperations;

    @Mock ICacheKeyGenerator cacheKeyGenerator;
    @Mock Cache cache;
    @Mock PortletRequest request;
    @Mock CalendarConfiguration config;
    ExchangeCalendarAdapter adapter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        when(config.getId()).thenReturn((long) 3);
        when(request.getAttribute(PortletRequest.USER_INFO)).thenReturn(
                Collections.<String, String> singletonMap("mail",
                        "test@school.edu"));
        
        adapter = new ExchangeCalendarAdapter();
        adapter.setCache(cache);
        adapter.setCacheKeyGenerator(cacheKeyGenerator);
        adapter.setWebServiceOperations(webServiceOperations);
    }

    @Test
    public void test() throws DatatypeConfigurationException {

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 10);
        cal.set(Calendar.DATE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateTime start = new DateTime();
        start.setTime(cal.getTimeInMillis());
        
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 0);
        DateTime end = new DateTime(cal.getTime());

        Period period = new Period(start, end);
        
//        Set<CalendarEvent> events = adapter.retrieveExchangeEvents(config, period, "test@school.edu");

    }
    
}

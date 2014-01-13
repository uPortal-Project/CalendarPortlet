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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.portlet.ResourceResponse;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;

import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.mvc.CalendarHelper;
import org.jasig.portlet.calendar.mvc.UICalendarEventsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.portlet.MockResourceRequest;
import org.springframework.mock.web.portlet.MockResourceResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.portlet.ModelAndView;


/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class AjaxCalendarControllerTest {

	private AjaxCalendarController testee;
	private MockResourceRequest request;
	private MockResourceResponse response;
	private String partialWellFormedResourceID = "1122104_7_";
	@Mock
	private CalendarHelper mockHelper;
	@Mock
	private UICalendarEventsBuilder mockUICalendarEventsBuilder;
	private Map<String, Object> nonEmptyMap;
	private String modelEtag;
	
	@Before
	public void setUp() {
		initMocks(this);
		testee = new AjaxCalendarController();
		request = new MockResourceRequest();
		response = new MockResourceResponse();
		nonEmptyMap = new HashMap<String, Object>();
		nonEmptyMap.put("aaa", new Object());
		modelEtag = ""+nonEmptyMap.hashCode();
		request.setResourceID(partialWellFormedResourceID+modelEtag);
		ReflectionTestUtils.setField(testee, "helper", mockHelper);
		ReflectionTestUtils.setField(testee, "uiCalendarEventBuiler", mockUICalendarEventsBuilder);
	}
	
	@Test
	public void testWhenRequestEtagMatchesModelEtagThenEmptyModelAndViewReturned() throws Exception{
		when(mockUICalendarEventsBuilder.buildUIEvents(any(Set.class), eq(request), any(List.class))).thenReturn(nonEmptyMap);
		ModelAndView result=testee.getEventList(request, response);
		assertEquals("empty",result.getViewName());
		assertTrue(result.getModel().isEmpty());
	}
	
	@Test
	public void testWhenRequestEtagMatchesModelEtagThenResponseIs304() throws Exception{
		when(mockUICalendarEventsBuilder.buildUIEvents(any(Set.class), eq(request), any(List.class))).thenReturn(nonEmptyMap);
		ModelAndView result=testee.getEventList(request, response);
		assertEquals("304",response.getProperty(ResourceResponse.HTTP_STATUS_CODE));
	}
	
	@Test
	public void testWhenRequestEtagDoesNotMatchModelEtagThenPopulatedModelAndViewReturned() throws Exception{
		request.setResourceID(partialWellFormedResourceID+modelEtag+"67676767");
		when(mockUICalendarEventsBuilder.buildUIEvents(any(Set.class), eq(request), any(List.class))).thenReturn(nonEmptyMap);
		ModelAndView result=testee.getEventList(request, response);
		assertEquals("json",result.getViewName());
		assertFalse(result.getModel().isEmpty());
	}
	
	@Test
	public void testAddLongEventToDateMap() throws IOException, URISyntaxException, ParseException {
		TimeZone tz = TimeZone.getTimeZone("America/Los Angeles");
        DateFormat df = new SimpleDateFormat("EEEE MMMM d");
		df.setTimeZone(tz);
		
		Calendar cal = Calendar.getInstance(tz);
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 3);
		cal.set(Calendar.HOUR_OF_DAY, 4);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		
		cal.add(Calendar.DATE, 1);
		Date periodStart = cal.getTime();
		
		cal.add(Calendar.DATE, 1);
		Date end = cal.getTime();
		
		Period period = new Period(new DateTime(periodStart), new DateTime(end));
		
		VEvent event = new VEvent(new DateTime(start), new DateTime(end), "Test Event");
		List<CalendarDisplayEvent> events = new ArrayList<CalendarDisplayEvent>();
//		events.addAll(controller.getJsonEvents(event, period, tz));
//		Collections.sort(events);
//		assertEquals(2, events.size());
//		
//		assertEquals("Tuesday January 4", df.format(events.get(0).getDayStart()));
//		assertEquals("Wednesday January 5", df.format(events.get(1).getDayStart()));
		
	}

	@Test
	public void testAddShortEventToDateMap() throws IOException, URISyntaxException, ParseException {
		TimeZone tz = TimeZone.getTimeZone("America/Los Angeles");
        DateFormat df = new SimpleDateFormat("EEEE MMMM d");
		df.setTimeZone(tz);
		
		Calendar cal = Calendar.getInstance(tz);
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 3);
		cal.set(Calendar.HOUR_OF_DAY, 4);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		
		cal.add(Calendar.HOUR, 12);
		Date end = cal.getTime();

		Period period = new Period(new DateTime(start), new DateTime(end));
	
		VEvent event = new VEvent(new DateTime(start), new DateTime(end), "Test Event");
//		Set<CalendarDisplayEvent> events = controller.getJsonEvents(event, period, tz);
//		assertEquals(1, events.size());
//
//		Iterator<CalendarDisplayEvent> dateIter = events.iterator();
//		assertEquals("Monday January 3", df.format(dateIter.next().getDayStart()));
		
	}

}

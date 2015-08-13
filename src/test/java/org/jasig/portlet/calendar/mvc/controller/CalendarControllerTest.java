package org.jasig.portlet.calendar.mvc.controller;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import edu.emory.mathcs.backport.java.util.Collections;
import org.jasig.portlet.calendar.CalendarSet;
import org.jasig.portlet.calendar.PredefinedCalendarConfiguration;
import org.jasig.portlet.calendar.UserDefinedCalendarConfiguration;
import org.jasig.portlet.calendar.dao.ICalendarSetDao;
import org.jasig.portlet.calendar.mvc.IViewSelector;
import org.jasig.portlet.calendar.service.IInitializationService;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.portlet.MockPortletSession;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.portlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CalendarControllerTest {

	@Mock
	private IInitializationService mockIInitializationService;
	@Mock
	private IViewSelector viewSelectorMock;
	private ICalendarSetDao calendarDao;
	private CalendarSet<UserDefinedCalendarConfiguration> calendarSet;
	private CalendarController testee;
	private MockRenderRequest mockRequest;
	private MockPortletSession mockSession;

	@Before
	public void startUp(){
		mockRequest = new MockRenderRequest();
		mockSession = new MockPortletSession();
		initMocks(this);
		calendarDao = new ICalendarSetDao() {

			@Override
			public CalendarSet<?> getCalendarSet(PortletRequest request) {
				return calendarSet;
			}

			@Override
			public List<PredefinedCalendarConfiguration> getAvailablePredefinedCalendarConfigurations(PortletRequest request) {
				return null;
			}
		};
		calendarSet = new CalendarSet<UserDefinedCalendarConfiguration>(Collections.emptySet());
		testee = new CalendarController();
		mockRequest.setSession(mockSession);
		ReflectionTestUtils.setField(testee,"initializationServices",Collections.singletonList(mockIInitializationService));
		ReflectionTestUtils.setField(testee,"calendarSetDao",calendarDao);
		ReflectionTestUtils.setField(testee,"viewSelector",viewSelectorMock);
	}
	@Test
	public void testDatePickerIsShownByDefault(){
		when(viewSelectorMock.getCalendarViewName(eq(mockRequest))).thenReturn("calendar");
		mockSession.setAttribute("startDate", new DateMidnight());
		mockSession.setAttribute("days", 1);
		ModelAndView mv = testee.getCalendar(null,mockRequest);
		Map<String,Object> model = (Map<String, Object>) mv.getModel().get("model");
		assertEquals("true",model.get("showDatePicker"));

	}
}

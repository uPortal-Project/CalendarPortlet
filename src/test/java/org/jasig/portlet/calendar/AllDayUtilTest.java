package org.jasig.portlet.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;

import org.junit.Test;

public class AllDayUtilTest {
	
	@Test
	public void testIsAllDayWesternEvent() throws ParseException {
		allDayEvent(TimeZone.getTimeZone("America/Los_Angeles"));
	}
	
	@Test
	public void testIsAllDayEasternEvent() throws ParseException {
		allDayEvent(TimeZone.getTimeZone("Europe/Moscow"));
	}
	
	public void allDayEvent(TimeZone timezone) throws ParseException {
		DateFormat df = new SimpleDateFormat("MM'/'dd'/'yyyy");
        
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("12/31/2009"));
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 1);
	    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
	    cal.add(Calendar.MILLISECOND, -timezone.getOffset(cal.getTimeInMillis()));

	    Date start = new DateTime(cal.getTime());
	    
	    cal.add(Calendar.DATE, 1);
	    Date end = new DateTime(cal.getTime());
	    
	    CalendarEvent event = new CalendarEvent(start, end, "test");
		
	    boolean isAllDay = AllDayUtil.isAllDayEvent(event, timezone);
	    assert isAllDay;
	}

}

package edu.yale.its.tp.portlets.calendar.mvc.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ViewCalendarMonthController extends AbstractController {

	private static Log log = LogFactory.getLog(ViewCalendarMonthController.class);

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {

		Calendar cal = Calendar.getInstance();
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("today", cal.getTime());

		String monthNum = arg0.getParameter("month");
		if (monthNum != null) {
			cal.set(Calendar.MONTH, Integer.parseInt(monthNum) - 1);
		}
		model.put("month", cal.getTime());
		
		cal.set(Calendar.DATE, 1);
		// get the day the first day of the month falls on
		int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK);
		// get the number of days in this month
		int daysInMonth = cal.getActualMaximum(Calendar.DATE);
		// get the number of days in the last month
		cal.add(Calendar.MONTH, -1);
		int daysInLastMonth = cal.getActualMaximum(Calendar.DATE);
		
		model.put("firstDayOfMonth", firstDayOfMonth);
		model.put("lastDayInMonth", daysInMonth);
		model.put("lastDayInLastMonth", daysInLastMonth);
		model.put("linebreak", new String("</tr><tr>"));
		log.debug("Displaying month " + model.get("month"));

		return new ModelAndView("/month", "model", model);
	}

}

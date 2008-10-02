package edu.yale.its.tp.portlets.calendar.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Period;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.CalendarConfiguration;
import edu.yale.its.tp.portlets.calendar.CalendarEvent;

public class BasicAuthHttpICalAdapter extends HttpICalAdapter {

	private static Log log = LogFactory.getLog(BasicAuthHttpICalAdapter.class);

	@Override
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarListing,
			Period period, PortletRequest request) throws CalendarException {

		// get the session
		PortletSession session = request.getPortletSession(false);
		if (session == null) {
			log.error("BasicAuthHttpICalAdapter requested with a null session");
			throw new CalendarException();
		}

		// retrieve the user's credentials
		String username = (String) session.getAttribute("subscribeId");
		if (username == null) {
			log.error("BasicAuthHttpICalAdapter cannot find the subscribeId");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute("password");
		if (password == null) {
			log.error("BasicAuthHttpICalAdapter cannot find the users password, try configuring the CachedCredentialsInitializationService");
			throw new CalendarException();
		}
		
		return getEvents(calendarListing, period,new UsernamePasswordCredentials(username, password));

	}

	@Override
	public Set<CalendarEvent> getEvents(CalendarConfiguration calendarListing,
			Period period, HttpServletRequest request) throws CalendarException {
		
		// get the session
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.warn("BasicAuthHttpICalAdapter requested with a null session");
			throw new CalendarException();
		}
		
		String username = (String) session.getAttribute("subscribeId");
		if (username == null) {
			log.error("BasicAuthHttpICalAdapter cannot find the subscribeId");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute("password");
		if (password == null) {
			log.error("BasicAuthHttpICalAdapter cannot find the users password, try configuring the CachedCredentialsInitializationService");
			throw new CalendarException();
		}

		return getEvents(calendarListing, period,
				new UsernamePasswordCredentials(username, password));

	}

	private Set<CalendarEvent> getEvents(CalendarConfiguration calendarListing,
			Period period, Credentials credentials) throws CalendarException {

		// get the URL for this calendar
		String url = (String) calendarListing.getCalendarDefinition()
				.getParameters().get("url");
		if (url == null) {
			log.error("HttpICalAdapter with ID "
					+ calendarListing.getCalendarDefinition().getId()
					+ " has no URL parameter");
			throw new CalendarException("Calendar is not configured correctly");
		}

		CalendarBuilder builder = new CalendarBuilder();
		HttpClient client = new HttpClient();
		client.getState().setCredentials(new AuthScope(AuthScope.ANY),
				credentials);
		GetMethod get = null;

		try {

			if (log.isDebugEnabled())
				log.debug("Retrieving calendar " + url);

			get = new GetMethod(url);
			int rc = client.executeMethod(get);
			if (rc != HttpStatus.SC_OK) {
				log.warn("HttpStatus for " + url + ":" + rc);
			}

			// retrieve and parse the iCal document
			InputStream in = get.getResponseBodyAsStream();
			net.fortuna.ical4j.model.Calendar calendar = builder.build(in);
			return getEvents(calendarListing.getId(), calendar, period);

		} catch (HttpException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed");
		} catch (IOException e) {
			log.warn("Error fetching iCalendar feed", e);
			throw new CalendarException("Error fetching iCalendar feed");
		} catch (ParserException e) {
			// the URL was successfully retrieved, but doesn't represent
			// a valid iCal feed
			log.warn("Error parsing iCalendar feed: " + e.getMessage());
			throw new CalendarException("Error parsing iCalendar feed");
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}

}

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

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.exchange.ExchangeWebServiceCallBack;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceOperations;

import com.microsoft.exchange.messages.FreeBusyResponseType;
import com.microsoft.exchange.messages.GetUserAvailabilityRequest;
import com.microsoft.exchange.messages.GetUserAvailabilityResponse;
import com.microsoft.exchange.types.ArrayOfCalendarEvent;
import com.microsoft.exchange.types.ArrayOfMailboxData;
import com.microsoft.exchange.types.DayOfWeekType;
import com.microsoft.exchange.types.Duration;
import com.microsoft.exchange.types.FreeBusyViewOptions;
import com.microsoft.exchange.types.Mailbox;
import com.microsoft.exchange.types.MailboxData;
import com.microsoft.exchange.types.MeetingAttendeeType;
import com.microsoft.exchange.types.SerializableTimeZoneTime;
import com.microsoft.exchange.types.TimeZone;

/**
 * Queries Exchange Web Services API for calendar events.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExchangeCalendarAdapter extends AbstractCalendarAdapter implements ICalendarAdapter {
    private String localDomainName;
    public void setLocalDomainName(String localDomainName) {
		this.localDomainName = localDomainName;
	}

	protected final static String AVAILABILITY_SOAP_ACTION = "http://schemas.microsoft.com/exchange/services/2006/messages/GetUserAvailability";
    protected final static String UTC = "UTC";
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private WebServiceOperations webServiceOperations;
    
    public void setWebServiceOperations(WebServiceOperations webServiceOperations) {
        this.webServiceOperations = webServiceOperations;
    }

    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    private ICacheKeyGenerator cacheKeyGenerator;

    public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    private String cacheKeyPrefix = "exchange";

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }
    
    private String emailAttribute = "mail";
    private String userNameAttribute="uid";
    
    public void setEmailAttribute(String emailAttribute) {
        this.emailAttribute = emailAttribute;
    }
    
    private String requestServerVersion = "Exchange2010_SP1";  // see configuration.properties which sets this value
    
    public void setRequestServerVersion(final String requestServerVersion) {
        this.requestServerVersion = requestServerVersion;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.calendar.adapter.ICalendarAdapter#getEvents(org.jasig.portlet.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
     */
    public CalendarEventSet getEvents(CalendarConfiguration calendarConfiguration,
            Interval interval, PortletRequest request) throws CalendarException {
        
        // try to get the cached calendar
        
        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String username = userInfo.get(this.userNameAttribute);// guaranteed unique as it is the UNN from single sign on
        String impersonationID =  username +"@"+ localDomainName;
        String key = cacheKeyGenerator.getKey(calendarConfiguration, interval, request, cacheKeyPrefix.concat(".").concat(username));
        Element cachedElement = this.cache.get(key);
        CalendarEventSet eventSet;
        if (cachedElement == null) {
            log.debug("Retreiving exchange events for account " + username);
            Set<VEvent> events = retrieveExchangeEvents(calendarConfiguration, interval, impersonationID,username);
            log.debug("Exchange adapter found " + events.size() + " events");

            // save the calendar event set to the cache
            eventSet = insertCalendarEventSetIntoCache(this.cache, key, events);
        } else {
            eventSet = (CalendarEventSet) cachedElement.getValue();
        }
        
        return eventSet;
    }

    /**
     * Retrieve a set of CalendarEvents from the Exchange server for the specified
     * period and email address.
     * 
     * @param calendar
     * @param interval
     * @param emailAddress
     * @param username 
     * @return
     * @throws CalendarException
     */
    public Set<VEvent> retrieveExchangeEvents(CalendarConfiguration calendar,
            Interval interval, String emailAddress, final String username) throws CalendarException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving exchange events for period: " + interval);
        }
        Set<VEvent> events = new HashSet<VEvent>();
        
        try {
            
            // construct the SOAP request object to use
            GetUserAvailabilityRequest soapRequest = getAvailabilityRequest(interval, emailAddress);
            final WebServiceMessageCallback customCallback = new ExchangeWebServiceCallBack(username,AVAILABILITY_SOAP_ACTION,requestServerVersion,localDomainName);
            // use the request to retrieve data from the Exchange server
            GetUserAvailabilityResponse response = (GetUserAvailabilityResponse) webServiceOperations
                    .marshalSendAndReceive(soapRequest, customCallback);
            
            // for each FreeBusy response, parse the list of events
            for (FreeBusyResponseType freeBusy : response
                    .getFreeBusyResponseArray().getFreeBusyResponses()) {
                
                ArrayOfCalendarEvent eventArray = freeBusy.getFreeBusyView().getCalendarEventArray();
                if (eventArray != null && eventArray.getCalendarEvents() != null) {
                    List<com.microsoft.exchange.types.CalendarEvent> msEvents = eventArray.getCalendarEvents();
                    for (com.microsoft.exchange.types.CalendarEvent msEvent : msEvents) {
                        // add the new event to the list
                        VEvent event = getInternalEvent(calendar.getId(), msEvent);
                        events.add(event);
                    }
                }
            }
            
        } catch (DatatypeConfigurationException e) {
            throw new CalendarException(e);
        }
        return events;
    }

    public String getLink(CalendarConfiguration calendar, Interval interval,
            PortletRequest request) throws CalendarLinkException {
        // TODO Auto-generated method stub
        return "";
    }
    
    protected GetUserAvailabilityRequest getAvailabilityRequest(Interval interval, String emailAddress) throws DatatypeConfigurationException {

        // construct the SOAP request object to use
        GetUserAvailabilityRequest soapRequest = new GetUserAvailabilityRequest();

        // create an array of mailbox data representing the current user
        ArrayOfMailboxData mailboxes = new ArrayOfMailboxData();
        MailboxData mailbox = new MailboxData();
        Mailbox address = new Mailbox();
        address.setAddress(emailAddress);
        address.setName("");
        mailbox.setAttendeeType(MeetingAttendeeType.REQUIRED);
        mailbox.setExcludeConflicts(false);
        mailbox.setEmail(address);            
        mailboxes.getMailboxDatas().add(mailbox);
        soapRequest.setMailboxDataArray(mailboxes);

        // create a FreeBusyViewOptions representing the specified period
        FreeBusyViewOptions view = new FreeBusyViewOptions();
        view.setMergedFreeBusyIntervalInMinutes(60);
        view.getRequestedView().add("DetailedMerged");
        
        Duration dur = new Duration();
        
        XMLGregorianCalendar start = getXmlDate(interval.getStart()); 
        XMLGregorianCalendar end = getXmlDate(interval.getEnd()); 
        dur.setEndTime(end);
        dur.setStartTime(start);
        
        view.setTimeWindow(dur);
        soapRequest.setFreeBusyViewOptions(view);
        
        // set the bias to the start time's timezone offset (in minutes 
        // rather than milliseconds)
        TimeZone tz = new TimeZone();
        java.util.TimeZone tZone = java.util.TimeZone.getTimeZone(UTC);
        tz.setBias(tZone.getRawOffset() / 1000 / 60 );
        
        // TODO: time zone standard vs. daylight info is temporarily hard-coded
        SerializableTimeZoneTime standard = new SerializableTimeZoneTime();
        standard.setBias(0);            
        standard.setDayOfWeek(DayOfWeekType.SUNDAY);
        standard.setDayOrder((short)1);
        standard.setMonth((short)11);
        standard.setTime("02:00:00");
        SerializableTimeZoneTime daylight = new SerializableTimeZoneTime();
        daylight.setBias(0);
        daylight.setDayOfWeek(DayOfWeekType.SUNDAY);
        daylight.setDayOrder((short)1);
        daylight.setMonth((short)3);
        daylight.setTime("02:00:00");
        tz.setStandardTime(standard);
        tz.setDaylightTime(daylight);
        
        soapRequest.setTimeZone(tz);
        
        return soapRequest;
    }
    
    /**
     * Return an internal API CalendarEvent for an Microsoft CalendarEvent object.
     * 
     * @param calendarId
     * @param msEvent
     * @return
     * @throws DatatypeConfigurationException 
     */
    protected VEvent getInternalEvent(long calendarId, com.microsoft.exchange.types.CalendarEvent msEvent) throws DatatypeConfigurationException {
        DatatypeFactory factory = DatatypeFactory.newInstance();

        // create a new UTC-based DateTime to represent the event start time
        net.fortuna.ical4j.model.DateTime eventStart = new net.fortuna.ical4j.model.DateTime();
        eventStart.setUtc(true);
        Calendar startCal = msEvent.getStartTime().toGregorianCalendar(
                java.util.TimeZone.getTimeZone(UTC), Locale.getDefault(),
                factory.newXMLGregorianCalendar());
        eventStart.setTime(startCal.getTimeInMillis());

        // create a new UTC-based DateTime to represent the event ent time
        net.fortuna.ical4j.model.DateTime eventEnd = new net.fortuna.ical4j.model.DateTime();
        eventEnd.setUtc(true);
        Calendar endCal = msEvent.getEndTime().toGregorianCalendar(
                java.util.TimeZone.getTimeZone(UTC), Locale.getDefault(),
                factory.newXMLGregorianCalendar());
        eventEnd.setTime(endCal.getTimeInMillis());

        // create a property list representing the new event
        PropertyList newprops = new PropertyList();
        newprops.add(new Uid(msEvent.getCalendarEventDetails().getID()));
        newprops.add(new DtStamp());
        
        newprops.add(new DtStart(eventStart));
        newprops.add(new DtEnd(eventEnd));
        newprops.add(new Summary(msEvent.getCalendarEventDetails().getSubject()));
        if (StringUtils.isNotBlank(msEvent.getCalendarEventDetails().getLocation())) {
            newprops.add(new Location(msEvent.getCalendarEventDetails().getLocation()));
        }
        
        VEvent event = new VEvent(newprops);
        return event;
        
    }

    /**
     * Get an XMLGregorianCalendar for the specified date.
     * 
     * @param date
     * @return
     * @throws DatatypeConfigurationException
     */
    protected XMLGregorianCalendar getXmlDate(DateTime date) throws DatatypeConfigurationException {
        // construct an XMLGregorianCalendar
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar start = datatypeFactory.newXMLGregorianCalendar(); 
        start.setYear(date.getYear());
        start.setMonth(date.getMonthOfYear());
        start.setTime(date.getHourOfDay(), date.getMinuteOfHour(),
                date.getSecondOfMinute(), date.getMillisOfSecond());
        start.setDay(date.getDayOfMonth());
        return start;
    }
    
}

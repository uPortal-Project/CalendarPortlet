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

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletRequest;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.RRule;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.credentials.DefaultCredentialsExtractorImpl;
import org.jasig.portlet.calendar.credentials.ICredentialsExtractor;
import org.jasig.portlet.calendar.url.DefaultUrlCreatorImpl;
import org.jasig.portlet.calendar.url.IUrlCreator;
import org.joda.time.Interval;
import org.osaf.caldav4j.CalDAVCollection;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;
import org.osaf.caldav4j.methods.HttpClient;
import org.osaf.caldav4j.model.request.CalendarQuery;
import org.osaf.caldav4j.util.GenerateQuery;
import org.osaf.caldav4j.util.UrlUtils;

/**
 * Implementation of {@link ICalendarAdapter} that uses CalDAV
 * for retrieving ical-based {@link CalendarEventSet}s.
 *
 * Useful background articles:
 * <a href="http://blogs.nologin.es/rickyepoderi/index.php?/archives/14-Introducing-CalDAV-Part-I.html"/>
 * <a href="http://blogs.nologin.es/rickyepoderi/index.php?/archives/15-Introducing-CalDAV-Part-II.html"/>
 *
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: CalDavCalendarAdapter.java Exp $
 */
public class CalDavCalendarAdapter extends AbstractCalendarAdapter implements ICalendarAdapter {

    protected final Log log = LogFactory.getLog(this.getClass());

    private Cache cache;
    private IUrlCreator urlCreator = new DefaultUrlCreatorImpl();
    private ICredentialsExtractor credentialsExtractor = new DefaultCredentialsExtractorImpl();
    private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
    private String cacheKeyPrefix = "default";

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setUrlCreator(IUrlCreator urlCreator) {
        this.urlCreator = urlCreator;
    }

    public void setCredentialsExtractor(ICredentialsExtractor credentialsExtractor) {
        this.credentialsExtractor = credentialsExtractor;
    }

    public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

    public CalendarEventSet getEvents(
            CalendarConfiguration calendarConfiguration, Interval interval,
            PortletRequest request) throws CalendarException {
        Set<VEvent> events = new HashSet<VEvent>();

        String url = this.urlCreator.constructUrl(calendarConfiguration, interval, request);

        if (log.isDebugEnabled()) {
            log.debug("generated url: " + url);
        }

        // try to get the cached calendar
        String key = cacheKeyGenerator.getKey(calendarConfiguration, interval, request, cacheKeyPrefix.concat(".").concat(url));
        Element cachedElement = this.cache.get(key);
        CalendarEventSet eventSet;
        if (cachedElement == null) {
            Credentials credentials = credentialsExtractor.getCredentials(request);
            // retrieve calendars for the current user
            net.fortuna.ical4j.model.Calendar calendar = retrieveCalendar(
                    url, interval, credentials);

            // extract events from the calendars
                events.addAll(convertCalendarToEvents(calendar, interval));
            log.debug("contentProcessor found " + events.size() + " events");
            // save the CalendarEvents to the cache
            eventSet = new CalendarEventSet(key, events);
            cachedElement = new Element(key, eventSet);
            this.cache.put(cachedElement);
        } else {
            eventSet = (CalendarEventSet) cachedElement.getValue();
        }

        return eventSet;
    }

    /* (non-Javadoc)
     * @see org.jasig.portlet.calendar.adapter.ICalendarAdapter#getLink(org.jasig.portlet.calendar.CalendarConfiguration, net.fortuna.ical4j.model.Period, javax.portlet.PortletRequest)
     */
    public String getLink(CalendarConfiguration calendar, Interval interval, PortletRequest request) throws CalendarLinkException {
        throw new CalendarLinkException("This calendar has no link");
    }

    // Have not gotten queries working so this method returns all events in
    // the calendar.
    protected final net.fortuna.ical4j.model.Calendar retrieveCalendar(
            String url, Interval interval, Credentials credentials) {

        try {

            // construct a HostConfiguration from the server URL
            URL hostUrl = new URL(url);
            int port = hostUrl.getPort();
            if (port == -1) {
                port = hostUrl.getDefaultPort();
            }

            HttpClient httpClient = new HttpClient();
            httpClient.getHostConfiguration()
                    .setHost(hostUrl.getHost(), port, Protocol.getProtocol(hostUrl.getProtocol()));

            if (log.isDebugEnabled()) {
                log.debug("connecting to calDAV host "
                        + httpClient.getHostConfiguration().getHost());
            }
            if (credentials != null) {
                httpClient.getState().setCredentials(AuthScope.ANY, credentials);
                httpClient.getParams().setAuthenticationPreemptive(true);
            }

            String calendarPath = hostUrl.getPath();
            CalDAVCollection calDAVCollection = new CalDAVCollection(
                    calendarPath, httpClient.getHostConfiguration(),
                    new CalDAV4JMethodFactory(), CalDAVConstants.PROC_ID_DEFAULT);

//            log.debug(calDAVCollection.testConnection(httpClient));
            if (log.isDebugEnabled()) {
                log.debug(calDAVCollection.getCalendarCollectionRoot());
            }

              // Haven't gotten queries working
//            GenerateQuery gq = new GenerateQuery();
//            gq.setTimeRange(new net.fortuna.ical4j.model.Date(interval
//                    .getStart().toDate()), new net.fortuna.ical4j.model.Date(
//                    interval.getEnd().toDate()));
//            CalendarQuery query = gq.generate();

            // Haven't gotten queries working even for single calendar.
            // Also I don't think Google supports viewing
            // or querying multiple calendars at once.
//            List<Calendar> cals = calDAVCollection.queryCalendars(client, query);
//            return cals.get(0);

            Calendar cal = calDAVCollection.getCalendar(httpClient, "");
            if (log.isDebugEnabled()) {
                log.debug(cal);
            }
            return cal;

        } catch (CalDAV4JException e) {
            log.error("CalDAV exception: ", e);
            throw new CalendarException(e);
        } catch (Exception e) {
            log.error(e);
            throw new CalendarException("Unknown exception while retrieving calendar", e);
        }

    }

    protected final Set<VEvent> convertCalendarToEvents(
            net.fortuna.ical4j.model.Calendar calendar, Interval interval)
            throws CalendarException {

        Period period = new Period(new net.fortuna.ical4j.model.DateTime(
                interval.getStartMillis()),
                new net.fortuna.ical4j.model.DateTime(interval.getEndMillis()));

        Set<VEvent> events = new HashSet<VEvent>();

        // if the calendar is null, return empty set
        if (calendar == null) {
            log.info("calendar empty, returning empty set");
            return Collections.emptySet();
        }

        // retrieve the list of events for this calendar within the
        // specified time period
        for (Iterator<Component> i = calendar.getComponents().iterator(); i
                .hasNext();) {
            Component component = i.next();
            if (component.getName().equals("VEVENT")) {
                VEvent event = (VEvent) component;
                if (log.isTraceEnabled()) {
                    log.trace("processing event " + event.getSummary().getValue());
                }
                // calculate the recurrence set for this event
                // for the specified time period
                PeriodList periods = event.calculateRecurrenceSet(period);

                // add each recurrence instance to the event list
                for (Iterator<Period> iter = periods.iterator(); iter.hasNext();) {
                    Period eventper = iter.next();

                    PropertyList props = event.getProperties();

                    // create a new property list, setting the date
                    // information to this event period
                    PropertyList newprops = new PropertyList();
                    newprops.add(new DtStart(eventper.getStart()));
                    newprops.add(new DtEnd(eventper.getEnd()));
                    for (Iterator<Property> iter2 = props.iterator(); iter2
                            .hasNext();) {
                        Property prop = iter2.next();

                        // only add non-date-related properties
                        if (!(prop instanceof DtStart)
                                && !(prop instanceof DtEnd)
                                && !(prop instanceof Duration)
                                && !(prop instanceof RRule))
                            newprops.add(prop);
                    }

                    // create the new event from our property list
                    VEvent newevent = new VEvent(newprops);
                    events.add(newevent);
                    if (log.isTraceEnabled()) {
                        log.trace("added event " + newevent);
                    }
                }
            }
        }

        return events;
    }

}

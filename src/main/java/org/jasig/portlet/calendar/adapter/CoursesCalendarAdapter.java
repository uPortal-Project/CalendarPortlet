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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.caching.RequestAttributeCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;
import org.jasig.portlet.courses.dao.ICoursesDao;
import org.jasig.portlet.courses.model.xml.CourseMeeting;
import org.jasig.portlet.courses.model.xml.Term;
import org.jasig.portlet.courses.model.xml.TermList;
import org.jasig.portlet.courses.model.xml.personal.Course;
import org.jasig.portlet.courses.model.xml.personal.CoursesByTerm;
import org.joda.time.Interval;

import javax.portlet.PortletRequest;

import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link org.jasig.portlet.calendar.adapter.ICalendarAdapter} that creates
 * a single calendar in a {@link org.jasig.portlet.calendar.adapter.CalendarEventSet} using data
 * from a user's courses for the term.
 *
 * The implementation expects that a term has a start and end date specified.
 *
 * @author James Wennmacher, jameswennmacher@gmail.com
 * @version $Id$
 */
public class CoursesCalendarAdapter extends AbstractCalendarAdapter implements ICalendarAdapter {

    protected final Log log = LogFactory.getLog(this.getClass());

    private Cache cache;
    private ICoursesDao courseDao;
    private ICacheKeyGenerator cacheKeyGenerator = new RequestAttributeCacheKeyGeneratorImpl();
    private IContentProcessor contentProcessor = new ICalendarContentProcessorImpl();
    private String cacheKeyPrefix = "courseDao";
    private final UidGenerator uidGenerator;
    
    public CoursesCalendarAdapter() {
        try {
            this.uidGenerator = new UidGenerator("uidGen");
        } catch (SocketException e) {
            throw new RuntimeException("Failed to create UidGenerator", e);
        }
    }

    /**
     * Map dayCode strings from courses-portlet-api to iCal4j WeekDay objects.
     */
    private enum DAYS {

        Su(WeekDay.SU), 
        M(WeekDay.MO), 
        T(WeekDay.TU), 
        W(WeekDay.WE), 
        Th(WeekDay.TH), 
        F(WeekDay.FR), 
        Sa(WeekDay.SA);
        
        private final WeekDay icalWeekDay;

        private DAYS(final WeekDay icalWeekDay) {
            this.icalWeekDay = icalWeekDay;
        }

        public WeekDay getIcalWeekDay() {
            return icalWeekDay;
        }

    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setCourseDao(ICoursesDao courseDao) {
        this.courseDao = courseDao;
    }

    public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

    public void setContentProcessor(IContentProcessor contentProcessor) {
        this.contentProcessor = contentProcessor;
    }

    public CalendarEventSet getEvents(
            CalendarConfiguration calendarConfiguration, Interval interval,
            PortletRequest request) throws CalendarException {

        String intervalCacheKey = cacheKeyGenerator.getKey(calendarConfiguration,
                interval, request, cacheKeyPrefix.concat(".") + interval.toString());

        // Get the calendar event set for the set of terms from cache
        CalendarEventSet eventSet;
        Element cachedElement = this.cache.get(intervalCacheKey);
        if (cachedElement != null) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving calendar event set from cache, termCacheKey:" + intervalCacheKey);
            }
            return (CalendarEventSet) cachedElement.getValue();
        }

        // Get the terms that overlap the requested interval.  Current implementation
        // requires the terms to have the start date and end date present in the
        // term.
        TermList allTerms = courseDao.getTermList(request);
        Set<VEvent> calendarEventSet = new HashSet<VEvent>();
        for (Term term : allTerms.getTerms()) {

            // todo determine if term ending Fri 10/31 (which means THROUGH 10/31 to 23:59:59)
            // and interval starting Fri 10/31 (meaning 10/31 12:00am) works as expected.

            // Determine if the interval overlaps any terms.
            if (interval.getStart().isBefore(term.getEnd().getTimeInMillis())
                    && interval.getEnd().isAfter(term.getStart().getTimeInMillis())) {

                Calendar calendar = retrieveCourseCalendar(request, interval, calendarConfiguration, term);
                Set<VEvent> events = contentProcessor.getEvents(interval, calendar);
                log.debug("contentProcessor found " + events.size() + " events");
                calendarEventSet.addAll(events);
            }
        }

        // Save the calendar event set to the cache.
        eventSet = new CalendarEventSet(intervalCacheKey, calendarEventSet);
        cachedElement = new Element(intervalCacheKey, eventSet);
        if (log.isDebugEnabled()) {
            log.debug("Storing calendar event set to cache, key:" + intervalCacheKey);
        }
        cache.put(cachedElement);

        return eventSet;
    }

    public String getLink(CalendarConfiguration calendar, Interval interval,
                          PortletRequest request) throws CalendarLinkException {
        throw new CalendarLinkException("This calendar has no link");
    }

    /**
     * Return the full set of events (class schedule) for all the user's courses
     * for the indicated term.
     *
     * @param request portlet request
     * @param interval requested interval
     * @param calendarConfiguration calendar config
     * @param term term to get class schedule for
     * @return User's schedule of classes for the indicated term, represented as calendar events
     */
    protected final Calendar retrieveCourseCalendar(PortletRequest request, Interval interval,
                                                    CalendarConfiguration calendarConfiguration,
                                                    Term term) {

        // Try to get the cached calendar for the specified term
        String termCacheKey = cacheKeyGenerator.getKey(calendarConfiguration,
                interval, request, cacheKeyPrefix.concat(".").concat(term.getCode()));

        Element cachedCalendar = this.cache.get(termCacheKey);
        if (cachedCalendar != null) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving calendar from cache, key:" + termCacheKey);
            }
            return (Calendar) cachedCalendar.getValue();
        }

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        java.util.Calendar termStartDate = term.getStart();
        java.util.Calendar termEndDate = term.getEnd();

        CoursesByTerm coursesByTerm = courseDao.getCoursesByTerm(request, term.getCode());
        if (coursesByTerm == null) {
            log.info("User " + request.getRemoteUser() + " does not have any courses" +
                    " for term " + term + " or invalid term code " + term);
            return calendar;
        }
        List<Course> courses = coursesByTerm.getCourses();

        // For each course obtain the list of meeting schedule times and create
        // events for it.
        for (Course course : courses) {
            for (CourseMeeting meeting : course.getCourseMeetings()) {

                VEvent meetingEvent = createEvent(course, meeting,
                        termStartDate, termEndDate);
                if (meetingEvent != null) {  // Will be null if we can't parse it
                    calendar.getComponents().add(meetingEvent);
                }
            }
        }

        // save the calendar to the cache
        cachedCalendar = new Element(termCacheKey, calendar);
        this.cache.put(cachedCalendar);
        if (log.isDebugEnabled()) {
            log.debug("Storing calendar cache, key:" + termCacheKey);
        }
        return calendar;
    }

    private VEvent createEvent(Course course, CourseMeeting courseMeeting,
                               java.util.Calendar termStartDate,
                               java.util.Calendar termEndDate) {

        // Treat meetings without a start date or end date as invalid.
        if (termStartDate == null && courseMeeting.getStartDate() == null) {
            log.error("Course " + course.getCode() + " must have a term start date"
                    + " or class meeting end date");
            return null;
        }
        if (termEndDate == null && courseMeeting.getEndTime() == null) {
            log.error("Course " + course.getCode() + " must have a term end date"
                    + " or class meeting end date");
            return null;
        }
        // Treat meetings without a start or end time as invalid
        if (courseMeeting.getStartTime() == null || courseMeeting.getEndTime() == null) {
            log.error("Course " + course.getCode() + " must have start time and end time specified");
            return null;
        }
        
        TimeZone tz = selectTimeZone(courseMeeting, termStartDate); 

        java.util.Calendar firstMeetingStartTime = calculateFirstMeetingStartTime(courseMeeting, termStartDate);
        java.util.Calendar firstMeetingEndTime = calculateFirstMeetingEndTime(firstMeetingStartTime, courseMeeting);
        java.util.Calendar recurrenceEndDate = calculateRecurrenceEndDate(courseMeeting, termEndDate);

        if (firstMeetingStartTime.after(firstMeetingEndTime)) {
            log.error("Course " + course.getCode() + " start time is after end time");
            return null;
        }

        if (firstMeetingStartTime.after(recurrenceEndDate)) {
            log.error("Course " + course.getCode() + " start date is after end date");
            return null;
        }

        // Currently assuming you always have at least one day of week specified; e.g. no course
        // meeting with start/end date and time specified but not day of week

        DateTime eventStart = new DateTime();
        eventStart.setTime(firstMeetingStartTime.getTimeInMillis());
        eventStart.setTimeZone(tz);

        DateTime eventEnd = new DateTime();
        eventEnd.setTime(firstMeetingEndTime.getTimeInMillis());
        eventStart.setTimeZone(tz);

        DateTime recurUntil = new DateTime();
        recurUntil.setTime(recurrenceEndDate.getTimeInMillis());
        eventStart.setTimeZone(tz);

        // create a property list representing the event
        PropertyList props = new PropertyList();
        
        props.add(uidGenerator.generateUid());
        props.add(new DtStamp());

        props.add(new DtStart(eventStart));
        props.add(new DtEnd(eventEnd));

        props.add(new Summary(StringUtils.isNotBlank(course.getCode()) ?
                course.getCode() : "course"));

        if (StringUtils.isNotBlank(course.getTitle())) {
            props.add(new Description(course.getTitle()));
        }
        if (StringUtils.isNotBlank(courseMeeting.getLocation().getDisplayName())) {
            props.add(new Location(courseMeeting.getLocation().getDisplayName()));
        }

        List<String> courseDays = courseMeeting.getDayIds();
        if (courseDays != null && courseDays.size() > 0) {
            Recur recur = new Recur(Recur.WEEKLY, recurUntil);
            for (String dayOfWeek : courseDays) {
                WeekDay day = DAYS.valueOf(dayOfWeek).getIcalWeekDay();
                if (day != null) {
                    recur.getDayList().add(day);
                } else {
                    log.warn("Invalid course day of week string " + dayOfWeek);
                }
            }
            RRule rrule = new RRule(recur);
            props.add(rrule);
        }

        VEvent event = new VEvent(props);
        return event;
    }

    private TimeZone selectTimeZone(CourseMeeting courseMeeting, java.util.Calendar termStartDate) {
        java.util.TimeZone jdkTz = null;
        if (courseMeeting.getStartDate() != null) {
            jdkTz = courseMeeting.getStartDate().getTimeZone();
        } else if (termStartDate != null) {
            jdkTz = termStartDate.getTimeZone();
        } else {
            throw new IllegalArgumentException("Arguments courseMeeting and termStartDate cannot both be null");
        }
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone rslt = registry.getTimeZone(jdkTz.getID()); 
        return rslt;
    }

    private java.util.Calendar combineDateAndTime(java.util.Calendar datePart, java.util.Calendar timePart) {

        // Start with the first argument
        java.util.Calendar rslt = (java.util.Calendar) datePart.clone();

        // Update time fields from the second
        rslt.set(java.util.Calendar.HOUR_OF_DAY, timePart.get(java.util.Calendar.HOUR_OF_DAY));
        rslt.set(java.util.Calendar.MINUTE, timePart.get(java.util.Calendar.MINUTE));
        rslt.set(java.util.Calendar.SECOND, timePart.get(java.util.Calendar.SECOND));
        rslt.set(java.util.Calendar.MILLISECOND, timePart.get(java.util.Calendar.MILLISECOND));

        return rslt;

    }

    private java.util.Calendar calculateFirstMeetingStartTime(CourseMeeting courseMeeting, java.util.Calendar termStartDate) {

        java.util.Calendar cmStartDate = courseMeeting.getStartDate();
        java.util.Calendar candidateStartDate = cmStartDate != null && !cmStartDate.getTime().before(termStartDate.getTime())
                ? courseMeeting.getStartDate() 
                : termStartDate;
        
        // We have to calculate the first time this course meets...
        List<String> courseDays = courseMeeting.getDayIds();
        java.util.Calendar actualStartDate = null; 
        while (true) {  // Must reach the break statement
            int calendarDayOfWeek = candidateStartDate.get(java.util.Calendar.DAY_OF_WEEK);
            for (String dayCode : courseDays) {
                WeekDay weekDay = DAYS.valueOf(dayCode).getIcalWeekDay();
                if (calendarDayOfWeek == WeekDay.getCalendarDay(weekDay)) {
                    // This course meets on this day;  proceed...
                    actualStartDate = (java.util.Calendar) candidateStartDate.clone();
                }
            }
            if (actualStartDate != null) {
                break;
            } else {
                // Advance & try again...
                candidateStartDate.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }
        }

        java.util.Calendar meetingStartTime = courseMeeting.getStartTime().toGregorianCalendar();
        java.util.Calendar rslt = combineDateAndTime(actualStartDate, meetingStartTime);

        return rslt;

    }

    // For recurring events end date date is the event end time on the event start date.
    private java.util.Calendar calculateFirstMeetingEndTime(java.util.Calendar firstMeetingStartTime,
                                                          CourseMeeting courseMeeting) {
        java.util.Calendar meetingEndTime = courseMeeting.getEndTime().toGregorianCalendar();
        return combineDateAndTime(firstMeetingStartTime, meetingEndTime);
    }

    // End date is the end date specified in the meeting or the term end date.
    private java.util.Calendar calculateRecurrenceEndDate(CourseMeeting courseMeeting,
                                                     java.util.Calendar termDate) {
        java.util.Calendar meetingDate = courseMeeting.getEndDate();
        java.util.Calendar meetingTime = courseMeeting.getEndTime().toGregorianCalendar();

        java.util.Calendar endDate = meetingDate != null? meetingDate : termDate;
        return combineDateAndTime(endDate, meetingTime);
    }

}

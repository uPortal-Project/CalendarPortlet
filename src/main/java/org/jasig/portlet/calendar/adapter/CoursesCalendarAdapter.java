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

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
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
import java.util.*;

/**
 * Implementation of {@link org.jasig.portlet.calendar.adapter.ICalendarAdapter} that creates
 * a single calendar in a {@link org.jasig.portlet.calendar.adapter.CalendarEventSet} using data
 * from a user's courses for the term.
 *
 * @author James Wennmacher, jameswennmacher@gmail.com
 * @version $Id$
 */
public class CoursesCalendarAdapter extends AbstractCalendarAdapter implements ICalendarAdapter {

    protected final Log log = LogFactory.getLog(this.getClass());

    private Cache cache;
    private ICoursesDao courseDao;
    private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
    private IContentProcessor contentProcessor = new ICalendarContentProcessorImpl();
    private String cacheKeyPrefix = "courseDao";

    private Map<String, WeekDay> eventDayOfWeekMap = createEventDaysOfWeekMap(defaultCourseDaysOfWeek());
    private List<String> courseDayOfWeekList = defaultCourseDaysOfWeek();

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

    private static List<String> defaultCourseDaysOfWeek() {
        List<String> daysOfWeek = new ArrayList<String>();
        daysOfWeek.add("Su");
        daysOfWeek.add("M");
        daysOfWeek.add("T");
        daysOfWeek.add("W");
        daysOfWeek.add("Th");
        daysOfWeek.add("F");
        daysOfWeek.add("Sa");
        return daysOfWeek;
    }

    private static Map<String, WeekDay> createEventDaysOfWeekMap(List<String> courseDayOfWeekList) {
        if (courseDayOfWeekList.size() != 7) {
            throw new IllegalArgumentException("Days of week list must be 7 items; one per day of week");
        }
        for (String dayofWeek : courseDayOfWeekList) {
            if (dayofWeek == null || dayofWeek.length() == 0) {
                throw new IllegalArgumentException("Days of week list cannot contain nulls or empty strings");
            }
        }
        HashMap<String,WeekDay> courseDayOfWeekMap = new HashMap<String,WeekDay>();
        courseDayOfWeekMap.put(courseDayOfWeekList.get(0), WeekDay.SU);
        courseDayOfWeekMap.put(courseDayOfWeekList.get(1), WeekDay.MO);
        courseDayOfWeekMap.put(courseDayOfWeekList.get(2), WeekDay.TU);
        courseDayOfWeekMap.put(courseDayOfWeekList.get(3), WeekDay.WE);
        courseDayOfWeekMap.put(courseDayOfWeekList.get(4), WeekDay.TH);
        courseDayOfWeekMap.put(courseDayOfWeekList.get(5), WeekDay.FR);
        courseDayOfWeekMap.put(courseDayOfWeekList.get(6), WeekDay.SA);
        return courseDayOfWeekMap;
    }

    public void setCourseDayOfWeekList(List<String> courseDayOfWeekList) {
        this.eventDayOfWeekMap = createEventDaysOfWeekMap(courseDayOfWeekList);
        this.courseDayOfWeekList = courseDayOfWeekList;
    }

    public CalendarEventSet getEvents(
            CalendarConfiguration calendarConfiguration, Interval interval,
            PortletRequest request) throws CalendarException {

        // Future nice to have may be to pick term based on interval start date?
        Map<String,String> parameters = calendarConfiguration.getCalendarDefinition().getParameters();
        String termCode = parameters.get("termCode");
        if (StringUtils.isBlank(termCode)) {
            throw new CalendarException("The Courses Calendar does not have a termCode parameter entry");
        }

        // Stage 1 caches the entire calendar.
        //
        // Stage 2 filters the cached calendar down to the requested interval and
        // caches the calendar events for that interval.

        // Stage 1: Try to get the cached calendar.
        String intermediateCacheKey = cacheKeyGenerator.getKey(calendarConfiguration,
                interval, request, cacheKeyPrefix.concat(".").concat(termCode));
        Calendar calendar;
        Element cachedCalendar = this.cache.get(intermediateCacheKey);
        if (cachedCalendar == null) {

            // retrieve course calendar for the current user and configured term
            calendar = retrieveCourseCalendar(request, termCode);

            // save the VEvents to the cache
            cachedCalendar = new Element(intermediateCacheKey, calendar);
            this.cache.put(cachedCalendar);
            if (log.isDebugEnabled()) {
                log.debug("Storing calendar cache, key:" + intermediateCacheKey);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving calendar from cache, key:" + intermediateCacheKey);
            }
            calendar = (Calendar) cachedCalendar.getValue();
        }

        String processorCacheKey = intermediateCacheKey + "." + interval.toString();
        // Stage 2: Get the calendar event set for the requested interval from cache
        // or generate it from the calendar from stage 1.
        CalendarEventSet eventSet;
        Element cachedElement = this.cache.get(processorCacheKey);
        if (cachedElement == null) {
            Set<VEvent> events = contentProcessor.getEvents(interval, calendar);
            log.debug("contentProcessor found " + events.size() + " events");

            // Save the calendar event set to the cache.  Calculate how long
            // this event set should survive.  We don't want this event set to
            // survive beyond the expiration of the calendar from stage 1 or you
            // have the potential of having two sets of events with different
            // overlapping intervals displaying different data, assuming getting
            // the calendar in stage 1 returns the whole calendar and not just
            // the portion of the calendar within the desired interval.
            //
            // For instance this inconsistency in calendar event sets can happen
            // when you get the calendar and display a week, then
            // near the expiration of the calendar from stage 1 get events for a
            // month that contains the week.  If you then display the week again
            // after the calendar (stage 1) has expired, you could get a changed
            // calendar and generate different calendar events than what you'd see
            // in the month view until the stage-2-month calendar event set expires
            // and builds a calendar event set based on the same data as the week
            // was generated with.
            int timeToLiveInSeconds = -1;
            long currentTime = System.currentTimeMillis();
            if (cachedCalendar.getExpirationTime() > currentTime) {
                long timeToLiveInMilliseconds =
                        cachedCalendar.getExpirationTime() - currentTime;
                timeToLiveInSeconds = (int)timeToLiveInMilliseconds/1000;
            }
            eventSet = insertCalendarEventSetIntoCache(this.cache, processorCacheKey, events,
                    timeToLiveInSeconds > 0 ? timeToLiveInSeconds : -1);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving calendar event set from cache, intermediateCacheKey:" + processorCacheKey);
            }
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

    /**
     * Return the full set of events (class schedule) for all the user's courses
     * for the indicated term.
     *
     *
     * @param request portlet request
     * @param termCode term code to get class schedule for
     * @return User's schedule of classes for the indicated term, represented as calendar events
     */
    protected final Calendar retrieveCourseCalendar(PortletRequest request, String termCode) {

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        java.util.Calendar termStartDate = null;
        java.util.Calendar termEndDate = null;
        TermList termList = courseDao.getTermList(request);
        Term term = termList.getTerm(termCode);
        if (term != null) {
            termStartDate = term.getStart() != null ? term.getStart() : null;
            termEndDate = term.getEnd() != null ? term.getEnd() : null;
        }

        CoursesByTerm coursesByTerm = courseDao.getCoursesByTerm(request, termCode);
        List<Course> courses = coursesByTerm.getCourses();

        // For each course obtain the list of meeting schedule times and create
        // events for it.
        for (Course course : courses) {
            for (CourseMeeting meeting : course.getCourseMeetings()) {

                VEvent meetingEvent = createEvent(course, meeting,
                        termStartDate, termEndDate);
                if (meetingEvent != null) {
                    calendar.getComponents().add(meetingEvent);
                }

            }
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

        java.util.Calendar recurrenceStartTime = determineRecurrenceStartTime(courseMeeting, termStartDate);
        java.util.Calendar recurrenceEndTime = determineRecurrenceEndTime(recurrenceStartTime, courseMeeting);
        java.util.Calendar recurrenceEndDate = determineRecurrenceEndDate(courseMeeting, termEndDate);

        if (recurrenceStartTime.after(recurrenceEndTime)) {
            log.error("Course " + course.getCode() + " start time is after end time");
            return null;
        }

        if (recurrenceStartTime.after(recurrenceEndDate)) {
            log.error("Course " + course.getCode() + " start date is after end date");
            return null;
        }

        // Currently assuming you always have at least one day of week specified; e.g. no course
        // meeting with start/end date and time specified but not day of week

        // create a new UTC-based DateTime to represent the event start time
        // NOTE:  This assumes this uPortal server's timezone is the same as the
        //        university's timezone.
        DateTime eventStart = new DateTime();
        eventStart.setUtc(true);
        eventStart.setTime(recurrenceStartTime.getTimeInMillis());

        // create a new UTC-based DateTime to represent the event end time
        DateTime eventEnd = new DateTime();
        eventEnd.setUtc(true);
        eventEnd.setTime(recurrenceEndTime.getTimeInMillis());

        DateTime recurUntil = new DateTime(recurrenceEndDate.getTimeInMillis());

        // create a property list representing the event
        PropertyList props = new PropertyList();

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
                WeekDay day = eventDayOfWeekMap.get(dayOfWeek);
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

    private java.util.Calendar createDateTime(java.util.Calendar date, java.util.Calendar time) {
        java.util.Calendar classTime = java.util.Calendar.getInstance();
        classTime.set(
                date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH),
                date.get(java.util.Calendar.DAY_OF_MONTH),
                time.get(java.util.Calendar.HOUR_OF_DAY),
                time.get(java.util.Calendar.MINUTE),
                time.get(java.util.Calendar.SECOND)
        );
        return classTime;
    }

    // For recurring events the start date is the event start on the first day
    // the event would occur; e.g. if Term starts on Sunday but the first class
    // is on Tuesday, the start date is Tuesday.
    private java.util.Calendar adjustStartDateForDayOfWeek(java.util.Calendar startDate,
                                                           CourseMeeting courseMeeting) {
        List<String> courseDays = courseMeeting.getDayIds();
        
        // If we don't have courseDays specified return the start date.
        if (courseDays == null || courseDays.size() == 0 
                || StringUtils.isBlank(courseDays.get(0))) {
            return startDate;
        }

        // Determine the first day of the week of the course and translate that
        // to a Calendar day of week value.
        // Assume the first entry in courseDays is the earliest in the week.
        int dayOfWeek = -1;
        String firstDayOfClass = courseDays.get(0);
        for (int i = 0; i < courseDayOfWeekList.size(); i++) {
            if (firstDayOfClass.equals(courseDayOfWeekList.get(i))) {
                dayOfWeek = i;
                break;
            }
        }
        if (dayOfWeek == -1) {
            log.warn("Day of week string " + courseDays.get(0)
                    + " in course meeting is not a valid day of week string.");
            return startDate;
        }
        int offset = dayOfWeek - startDate.get(java.util.Calendar.DAY_OF_WEEK) + 1;

        java.util.Calendar date = (java.util.Calendar) startDate.clone();
        date.add(java.util.Calendar.DAY_OF_YEAR, offset);
        return date;
    }

    private java.util.Calendar determineRecurrenceStartTime(CourseMeeting courseMeeting,
                                                            java.util.Calendar termDate) {
        java.util.Calendar meetingDate = courseMeeting.getStartDate();
        java.util.Calendar meetingTime = courseMeeting.getStartTime().toGregorianCalendar();
        java.util.Calendar date = meetingDate != null? meetingDate : termDate;

        java.util.Calendar roughStart = createDateTime(date, meetingTime);
        return adjustStartDateForDayOfWeek(roughStart, courseMeeting);
    }

    // For recurring events end date date is the event end time on the event start date.
    private java.util.Calendar determineRecurrenceEndTime(java.util.Calendar startDate,
                                                          CourseMeeting courseMeeting) {
        java.util.Calendar meetingTime = courseMeeting.getEndTime().toGregorianCalendar();
        return createDateTime(startDate, meetingTime);
    }

    // End date is the end date specified in the meeting or the term end date.
    private java.util.Calendar determineRecurrenceEndDate(CourseMeeting courseMeeting,
                                                     java.util.Calendar termDate) {
        java.util.Calendar meetingDate = courseMeeting.getEndDate();
        java.util.Calendar meetingTime = courseMeeting.getEndTime().toGregorianCalendar();

        java.util.Calendar endDate = meetingDate != null? meetingDate : termDate;
        return createDateTime(endDate, meetingTime);
    }

}

/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.calendar.mvc;

import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

/**
 * Object that summarizes the portion of an event on a specific day. If the event spans multiple
 * days, a <code>CalendarDisplayEvent</code> instance would be created for each day the event occurs
 * on.
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CalendarDisplayEvent implements Comparable<CalendarDisplayEvent> {

  private final DateTime dayStart;
  private final DateTime dayEnd;
  private final boolean isAllDay;
  private final boolean isMultiDay;

  private final String summary;
  private final String description;
  private final String location;

  private final String dateStartTime;
  private final String dateEndTime;
  private final String startTime;
  private final String endTime;
  private final String startDate;
  private final String endDate;

  private final int calendarIndex;

  /**
   * Constructs an object from specified data.
   *
   * @param event "Raw" Event object
   * @param eventInterval Interval portion of the event that applies to this specific day
   * @param theSpecificDay Interval of the specific day in question
   * @param df date formatter to represent date displays
   * @param tf time formatter to represent time displays
   */
  public CalendarDisplayEvent(
      VEvent event,
      Interval eventInterval,
      Interval theSpecificDay,
      DateTimeFormatter df,
      DateTimeFormatter tf,
      int calendarIndex) {
    assert theSpecificDay.abuts(eventInterval) || theSpecificDay.overlaps(eventInterval)
        : "Event interval is not in the specified day!";

    this.summary = event.getSummary() != null ? event.getSummary().getValue() : null;
    this.description = event.getDescription() != null ? event.getDescription().getValue() : null;
    this.location = event.getLocation() != null ? event.getLocation().getValue() : null;

    this.calendarIndex = calendarIndex;

    boolean multi = false;
    if (eventInterval.getStart().isBefore(theSpecificDay.getStart())) {
      dayStart = theSpecificDay.getStart();
      multi = true;
    } else {
      dayStart = eventInterval.getStart();
    }

    if (event.getEndDate() == null) {
      dayEnd = dayStart;
    } else if (eventInterval.getEnd().isAfter(theSpecificDay.getEnd())) {
      dayEnd = theSpecificDay.getEnd();
      multi = true;
    } else {
      dayEnd = eventInterval.getEnd();
    }
    this.isMultiDay = multi;

    this.dateStartTime = tf.print(dayStart);
    this.startTime = tf.print(eventInterval.getStart());
    this.startDate = df.print(eventInterval.getStart());

    if (event.getEndDate() != null) {
      this.dateEndTime = tf.print(dayEnd);
      this.endTime = tf.print(eventInterval.getEnd());
      this.endDate = df.print(eventInterval.getEnd());
    } else {
      this.dateEndTime = null;
      this.endTime = null;
      this.endDate = null;
    }

    Interval dayEventInterval = new Interval(dayStart, dayEnd);
    this.isAllDay = dayEventInterval.equals(theSpecificDay);
  }

  public String getSummary() {
    return this.summary;
  }

  public String getDescription() {
    return this.description;
  }

  public String getLocation() {
    return this.location;
  }

  public String getDateStartTime() {
    return this.dateStartTime;
  }

  public String getDateEndTime() {
    return this.dateEndTime;
  }

  public String getStartTime() {
    return this.startTime;
  }

  public String getEndTime() {
    return this.endTime;
  }

  public String getStartDate() {
    return this.startDate;
  }

  public String getEndDate() {
    return this.endDate;
  }

  public boolean isAllDay() {
    return this.isAllDay;
  }

  public boolean isMultiDay() {
    return this.isMultiDay;
  }

  public DateTime getDayStart() {
    return this.dayStart;
  }

  public DateTime getDayEnd() {
    return this.dayEnd;
  }

  public int getCalendarIndex(){ return this.calendarIndex; }

  public int compareTo(CalendarDisplayEvent event) {
    // Order events by start date, then end date, then summary.
    // If all properties are equal, use the calendar and event ids to
    // ensure similar events from different calendars are not misinterpreted
    // as identical.
    return (new CompareToBuilder())
        .append(this.dayStart, event.dayStart)
        .append(this.dayEnd, event.dayEnd)
        .append(this.getSummary(), event.getSummary())
        // The UID class doesn't implement comparable and will give
        // rise to a ClassCastException if it's actually tested.
        // .append(this.event.getUid(), event.event.getUid())
        .toComparison();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof CalendarDisplayEvent)) {
      return false;
    }
    CalendarDisplayEvent event = (CalendarDisplayEvent) o;
    return (new EqualsBuilder())
        .append(this.dayStart, event.dayStart)
        .append(this.dayEnd, event.dayEnd)
        .append(this.getSummary(), event.getSummary())
        // The UID class doesn't implement comparable and will give
        // rise to a ClassCastException if it's actually tested.
        // .append(this.event.getUid(), event.event.getUid())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
        .append(this.dayStart)
        .append(this.dayEnd)
        .append(this.getSummary())
        .toHashCode();
  }
}

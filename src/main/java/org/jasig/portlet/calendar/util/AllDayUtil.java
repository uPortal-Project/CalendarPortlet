/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.calendar.util;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * AllDayUtil determines whether a particular event should be classified as an "all-day" event in
 * the user's time zone. This implementation classifies an all-day event as starting at 12:00:00 AM
 * in the user's time zone, ends at 12:00:00 AM, and lasts approximately one day.
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class AllDayUtil {

  private static final String TIME_FORMAT = "Hms";
  private static final String EXPECTED_TIME = "000";

  private static final int MIN_DAY = 22 * 60 * 60 * 1000; // 22 hours
  private static final int MAX_DAY = 26 * 60 * 60 * 1000; // 26 hours

  /** Internal cache of timezone-specific date format objects */
  private static ConcurrentHashMap<TimeZone, FastDateFormat> dateFormatCache =
      new ConcurrentHashMap<TimeZone, FastDateFormat>();

  /**
   * Determine if a given event is an "all-day" event in the specified time zone.
   *
   * @param event
   * @param timezone
   * @return <code>true</code> for all-day events, <code>false</code> otherwise
   */
  public static boolean isAllDayEvent(Date startDate, Date endDate, TimeZone timezone) {

    /**
     * Get a DateFormat instance for the current user's time zone from the cache. If none exists,
     * create a new one and add it to the cache
     */
    FastDateFormat df;
    if (dateFormatCache.contains(timezone)) {
      df = dateFormatCache.get(timezone);
    } else {
      df = FastDateFormat.getInstance(TIME_FORMAT, timezone);
      dateFormatCache.put(timezone, df);
    }

    /**
     * Check if this event starts at 12:00:00 AM in the user's time zone. We currently convert the
     * event start date to a short string that encodes the hour, minute, and second in the indicated
     * timezone, then compare that to the expected string.
     */
    String start = df.format(startDate);
    if (!EXPECTED_TIME.equals(start)) {
      return false;
    }

    /**
     * Check if the event ends at 12:00:00 AM the next day and if the duration of the event suggests
     * the end date is midnight one day after the start date.
     *
     * <p>Note: We've elected to use this approach rather than simply checking the length of the
     * event against exactly 24 hours to handle potential complications like daylight savings time
     * changes and leap seconds.
     */
    if (endDate == null) {
      return true;
    }

    // check the end time of the event
    String end = df.format(endDate);
    if (!EXPECTED_TIME.equals(end)) {
      return true;
    }

    // get the duration of this event in milliseconds
    long duration = endDate.getTime() - startDate.getTime();

    // check the duration against our max and min fields
    if (duration < MIN_DAY || duration > MAX_DAY) {
      return false;
    }

    // if the tests above passed, return true
    return true;
  }
}

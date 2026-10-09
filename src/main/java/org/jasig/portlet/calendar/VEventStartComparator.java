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
package org.jasig.portlet.calendar;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Comparator;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;

/**
 * VEventStartComparator compares to VEvents and orders them by starting date. For events that start
 * at the time, whichever event ends first will be considered "first".
 *
 * @author Jen Bourey
 */
public class VEventStartComparator implements Comparator<VEvent> {

  /*
   * (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(VEvent event1, VEvent event2) {

    DtStart s1 = event1.getDateTimeStart();
    DtStart s2 = event2.getDateTimeStart();
    Instant start1 = s1 != null ? toInstant(s1.getDate()) : null;
    Instant start2 = s2 != null ? toInstant(s2.getDate()) : null;

    if (start1 == null || start2 == null) {
      return start1 == null ? (start2 == null ? 0 : -1) : 1;
    }

    int comp = start1.compareTo(start2);
    if (comp != 0) return comp;

    // Same start — compare end dates
    DtEnd e1 = event1.getDateTimeEnd();
    DtEnd e2 = event2.getDateTimeEnd();
    Instant end1 = e1 != null ? toInstant(e1.getDate()) : null;
    Instant end2 = e2 != null ? toInstant(e2.getDate()) : null;

    if (end1 == null && end2 == null) return 0;
    else if (end1 == null) return -1;
    else if (end2 == null) return 1;

    comp = end1.compareTo(end2);
    if (comp != 0) return comp;

    if (event1.getSummary() != null && event2.getSummary() != null) {
      comp = event1.getSummary().getValue().compareTo(event2.getSummary().getValue());
      if (comp != 0) return comp;
    }
    if (event1.getName() != null && event2.getName() != null) {
      comp = event1.getName().compareTo(event2.getName());
      if (comp != 0) return comp;
    }
    if (event1.getDescription() != null && event2.getDescription() != null) {
      comp = event1.getDescription().getValue().compareTo(event2.getDescription().getValue());
      if (comp != 0) return comp;
    }
    return 0;
  }

  private static Instant toInstant(Temporal temporal) {
    if (temporal == null) return null;
    if (temporal instanceof Instant) return (Instant) temporal;
    if (temporal instanceof java.time.LocalDate) {
      return ((java.time.LocalDate) temporal).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
    }
    if (temporal instanceof java.time.LocalDateTime) {
      return ((java.time.LocalDateTime) temporal).atZone(java.time.ZoneOffset.UTC).toInstant();
    }
    return Instant.from(temporal);
  }
}

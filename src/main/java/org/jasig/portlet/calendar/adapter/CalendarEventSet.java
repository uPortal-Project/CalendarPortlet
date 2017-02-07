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
package org.jasig.portlet.calendar.adapter;

import java.io.Serializable;
import java.util.Set;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * CalendarEventSet represents a set of cacheable calendar events.
 *
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class CalendarEventSet implements Serializable {

  private final String key;
  private final Set<VEvent> events;
  private long expirationTime;

  public CalendarEventSet(String key, Set<VEvent> events) {
    this.key = key;
    this.events = events;
  }

  public String getKey() {
    return key;
  }

  public Set<VEvent> getEvents() {
    return events;
  }

  /**
   * Return the expiration time of this CalendarEventSet. This property does not control the
   * expiration time but merely allows communicating the set expiration time through application
   * layers so each layer does not need to know if or how a CalendarEventSet was cached.
   *
   * @return Expiration time in absolute milliseconds when this CalendarEventSet is expected to be
   *     evicted from cache. 0 if not set.
   */
  public long getExpirationTime() {
    return expirationTime;
  }

  /**
   * Set the time in absolute milliseconds when this CalendarEventSet should be evicted from cache.
   *
   * @param expirationTime
   */
  public void setExpirationTime(long expirationTime) {
    this.expirationTime = expirationTime;
  }
}

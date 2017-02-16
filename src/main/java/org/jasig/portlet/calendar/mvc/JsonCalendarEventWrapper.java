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
package org.jasig.portlet.calendar.mvc;

/**
 * JsonCalendarEventWrapper wraps a shared/cached CalendarDisplayEvent and provides a way to add
 * user-specific information such as a color code.
 *
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class JsonCalendarEventWrapper implements Comparable<JsonCalendarEventWrapper> {

  private final CalendarDisplayEvent event;
  private final int colorIndex;

  public JsonCalendarEventWrapper(CalendarDisplayEvent event, int colorIndex) {
    this.event = event;
    this.colorIndex = colorIndex;
  }

  public CalendarDisplayEvent getEvent() {
    return event;
  }

  public int getColorIndex() {
    return colorIndex;
  }

  @Override
  public int compareTo(JsonCalendarEventWrapper wrapper) {
    return this.event.compareTo(wrapper.event);
  }

  @Override
  public boolean equals(Object o) {
    return this.event.equals(o);
  }

  @Override
  public int hashCode() {
    return this.event.hashCode();
  }
}

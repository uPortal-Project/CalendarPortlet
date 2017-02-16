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
package org.jasig.portlet.calendar;

/**
 * CalendarConfiguration represents a user-specific registration and configuration for a
 * CalendarDefinition.
 *
 * @author Jen Bourey
 */
public class CalendarConfiguration {

  private Long id = new Long(-1);
  private CalendarDefinition calendarDefinition;
  private boolean displayed = true;
  private String subscribeId;

  /**
   * Determine whether this calendar should be displayed or hidden.
   *
   * @return
   */
  public boolean isDisplayed() {
    return displayed;
  }

  /**
   * Set whether this calendar should be displayed or hidden.
   *
   * @param displayed
   */
  public void setDisplayed(boolean displayed) {
    this.displayed = displayed;
  }

  /**
   * Get the unique ID for this portlet subscription.
   *
   * @return
   */
  public String getSubscribeId() {
    return subscribeId;
  }

  /**
   * Set the unique ID for this portlet subscription.
   *
   * @param subscribeId
   */
  public void setSubscribeId(String subscribeId) {
    this.subscribeId = subscribeId;
  }

  /**
   * Get the unique ID for this CalendarConfiguration.
   *
   * @return
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the unique ID for this CalendarConfiguration.
   *
   * @param id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the CalendarDefinition for the calendar being configured.
   *
   * @return
   */
  public CalendarDefinition getCalendarDefinition() {
    return calendarDefinition;
  }

  /**
   * Set the CalendarDefinition for the calendar being configured.
   *
   * @param definition
   */
  public void setCalendarDefinition(CalendarDefinition definition) {
    this.calendarDefinition = definition;
  }
}

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
package org.jasig.portlet.calendar.adapter;

/**
 * Specialized {@link CalendarException} subclass that carries a user-facing message payload. When
 * one of these is thrown by an adaptor implementation, the portlet will display the value of <code>
 * getPayload</code>. This exception type is an extension point for custom adaptor implementors.
 *
 * @author Drew Wills
 */
public class UserFeedbackCalendarException extends CalendarException {

  private static final long serialVersionUID = 1L;
  private final String userFeedback;

  public UserFeedbackCalendarException(String message, String userFeedback) {
    this(message, null, userFeedback);
  }

  public UserFeedbackCalendarException(Throwable cause, String userFeedback) {
    this(null, cause, userFeedback);
  }

  public UserFeedbackCalendarException(String message, Throwable cause, String userFeedback) {
    super(message, cause);

    // Assertions.
    if (userFeedback == null) {
      String msg = "Argument 'userFeedback' cannot be null";
      throw new IllegalArgumentException(msg);
    }

    this.userFeedback = userFeedback;
  }

  public String getUserFeedback() {
    return userFeedback;
  }
}

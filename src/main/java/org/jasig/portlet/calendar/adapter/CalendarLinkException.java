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
 * CalendarLinkException represents an exception related to 
 * calendar hyper links.
 *
 * @author Anthony Colebourne
 */
public class CalendarLinkException extends CalendarException {
	private static final long serialVersionUID = 2L;

	public CalendarLinkException() {
		super();
	}

	public CalendarLinkException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalendarLinkException(String message) {
		super(message);
	}

	public CalendarLinkException(Throwable cause) {
		super(cause);
	}
}

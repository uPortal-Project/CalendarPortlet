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

import javax.portlet.PortletRequest;

/**
 * IViewSelector assists the Calendar Portlet in choosing appropriate JSP views for the main
 * calendar view. Views may be chosen based on variables like the portlet window state or the
 * browser's user agent string.
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IViewSelector {

  /**
   * Return the JSP view name of the main calendar view for this request.
   *
   * @param request
   * @return
   */
  public String getCalendarViewName(PortletRequest request);

  public String getEditViewName(PortletRequest request);

  /**
   * Return the JSP view name of the AJAX event list for this request.
   *
   * @param request
   * @return
   */
  public String getEventListViewName(PortletRequest request);
}

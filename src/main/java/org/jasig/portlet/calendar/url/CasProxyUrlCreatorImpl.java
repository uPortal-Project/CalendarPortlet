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
package org.jasig.portlet.calendar.url;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.jasig.portlet.cas.CASProxyTicketServiceUserInfoImpl;
import org.jasig.portlet.cas.ICASProxyTicketService;
import org.joda.time.Interval;

/**
 * This {@link IUrlCreator} implementation requires injection of a CAS {@link
 * CASProxyTicketServiceUserInfoImpl}. It retrieves a url from the {@link CalendarConfiguration}, in
 * a parameter named "url".
 *
 * <p>The CAS {@link CASProxyTicketServiceUserInfoImpl} is used to retrieve a proxy ticket for the
 * currently authenticated user, which is appended to the to the value of the "url" parameter as an
 * attribute named "ticket".
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: CasUrlCreatorImpl.java Exp $
 */
public class CasProxyUrlCreatorImpl implements IUrlCreator {

  protected final Log log = LogFactory.getLog(this.getClass());

  private ICASProxyTicketService proxyTicketService;

  /** @param proxyTicketService */
  public void setProxyTicketService(ICASProxyTicketService proxyTicketService) {
    this.proxyTicketService = proxyTicketService;
  }

  /* (non-Javadoc)
   * @see org.jasig.portlet.calendar.adapter.UrlCreator#constructUrl(org.jasig.portlet.calendar.CalendarConfiguration, javax.portlet.PortletRequest, net.fortuna.ical4j.model.Period)
   */
  public String constructUrl(
      CalendarConfiguration calendarListing, Interval interval, PortletRequest request) {
    String configuredUrl = calendarListing.getCalendarDefinition().getParameters().get("url");

    // get the session
    PortletSession session = request.getPortletSession(false);
    if (session == null) {
      log.warn("CasifiedICalFeed requested with a null session");
      throw new CalendarException();
    }

    // retrieve the CAS receipt for the current user's session
    Assertion receipt = (Assertion) session.getAttribute("CasReceipt");
    if (receipt == null) {
      log.warn("CasifiedICalFeed cannot find a CAS receipt object");
      throw new CalendarException();
    }

    String proxyTicket = proxyTicketService.getCasServiceToken(receipt, configuredUrl);
    StringBuilder finalUrl = new StringBuilder();
    finalUrl.append(configuredUrl);
    if (proxyTicket != null) {
      String separator = configuredUrl.contains("?") ? "&" : "?";
      finalUrl.append(separator);
      finalUrl.append("ticket=");
      finalUrl.append(proxyTicket);
    } else {
      log.warn(
          "No CAS ticket could be obtained for "
              + configuredUrl
              + ".  Returning empty event list.");
      throw new CalendarException();
    }
    return finalUrl.toString();
  }
}

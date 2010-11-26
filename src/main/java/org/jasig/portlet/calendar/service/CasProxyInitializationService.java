/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.calendar.service;

import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portlet.cas.ICASProxyTicketService;

/**
 * CasProxyInitializationService initializes authentication resources when a 
 * user creates a new session with the portlet.  It's important that this 
 * initialization is performed when the portlet session is first created, rather
 * than waiting until the first CAS-protected calendar is requested, since the 
 * proxy ticket might otherwise expire before it's validated.
 *
 * @author Jen Bourey
 */
public class CasProxyInitializationService implements IInitializationService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private int sessionLength = 60*60*2;
	
	/**
	 * Set the length of logged-in sessions
	 * 
	 * @param sessionLength
	 */
	public void setSessionLength(int sessionLength) {
		this.sessionLength = sessionLength;
	}
	
	private ICASProxyTicketService proxyTicketService;
	
	/**
	 * Set the proxy ticket service to use for retrieving CAS receipts
	 * 
	 * @param proxyTicketService
	 */
	public void setProxyTicketService(ICASProxyTicketService proxyTicketService) {
		this.proxyTicketService = proxyTicketService;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.service.IInitializationService#initialize(javax.portlet.PortletRequest)
	 */
	public void initialize(javax.portlet.PortletRequest request) {
		
		PortletSession session = request.getPortletSession();
		
		// attempt to retrieve a CAS receipt from the proxy ticket service
		Assertion receipt = proxyTicketService.getProxyTicket(request);
		
		// save the receipt and username to the session
		if (receipt != null) {
			session.setAttribute("CasReceipt", receipt);
			session.setAttribute("username", receipt.getPrincipal().getName());
		} else {
			log.debug("no CAS ticket received from portal");
		}
		
		// increase the length of the portlet session to outlast the portal's session
		session.setMaxInactiveInterval(sessionLength);

	}

}

/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
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
	
	private static Log log = LogFactory.getLog(CasProxyInitializationService.class);
	
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

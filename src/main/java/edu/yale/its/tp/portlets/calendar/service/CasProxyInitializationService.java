/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.service;

import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.portlet.ICASProxyTicketService;

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
	 * @see edu.yale.its.tp.portlets.calendar.service.IInitializationService#initialize(javax.portlet.PortletRequest)
	 */
	public void initialize(javax.portlet.PortletRequest request) {
		
		PortletSession session = request.getPortletSession();
		
		// attempt to retrieve a CAS receipt from the proxy ticket service
		CASReceipt receipt = proxyTicketService.getProxyTicket(request);
		
		// save the receipt and username to the session
		if (receipt != null) {
			session.setAttribute("CasReceipt", receipt);
			session.setAttribute("username", receipt.getUserName());
		} else {
			log.debug("no CAS ticket received from portal");
		}
		
		// increase the length of the portlet session to outlast the portal's session
		session.setMaxInactiveInterval(sessionLength);

	}

}


/*
 * CasProxyInitializationService.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
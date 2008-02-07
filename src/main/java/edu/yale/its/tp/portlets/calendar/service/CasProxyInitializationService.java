/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.service;

import java.io.IOException;
import java.util.Map;

import javax.portlet.PortletSession;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.portlet.ProxyTicketService;

public class CasProxyInitializationService implements IInitializationService {
	
	private static Log log = LogFactory.getLog(CasProxyInitializationService.class);

	public void initialize(javax.portlet.PortletRequest request) {
		PortletSession session = request.getPortletSession();
		Map userinfo = (Map) request.getAttribute("javax.portlet.userinfo");
		if (proxyTicketService != null) {
			String ticket = (String) userinfo.get("casProxyTicket");
			if (ticket != null) {
				try {
					CASReceipt receipt = proxyTicketService.getProxyTicket(ticket);
					session.setAttribute("CasReceipt", receipt);
				} catch (IOException e) {
					log.error(e);
				} catch (SAXException e) {
					log.error(e);
				} catch (ParserConfigurationException e) {
					log.error(e);
				}
			} else {
				log.debug("no CAS ticket received from portal");
			}
		}
		session.setMaxInactiveInterval(60*60*2);

	}

	private ProxyTicketService proxyTicketService;
	public void setProxyTicketService(ProxyTicketService proxyTicketService) {
		this.proxyTicketService = proxyTicketService;
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
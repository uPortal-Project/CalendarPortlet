/*
 * Created on May 19, 2008
 *
 * Copyright(c) The University of Manchester, May 19, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.service;

import java.io.IOException;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.portlet.ProxyTicketService;

/**
 * CachedCredentialsInitializationService initializes authentication resources when a 
 * user creates a new session with the portlet.
 *
 * @author Anthony Colebourne
 */
public class CachedCredentialsInitializationService implements IInitializationService {
	
	private static Log log = LogFactory.getLog(CachedCredentialsInitializationService.class);

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.portlets.calendar.service.IInitializationService#initialize(javax.portlet.PortletRequest)
	 */
	public void initialize(javax.portlet.PortletRequest request) {
		
		// get the UserInfo map from the portlet session
		PortletSession session = request.getPortletSession();
		Map userinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
		
		// get the credentials for this portlet from the UserInfo map
		String password = (String) userinfo.get("password");
			
		session.setAttribute("password", password, PortletSession.APPLICATION_SCOPE);
	}	
}

/*
 * CachedCredentialsInitializationService.java
 * 
 * Copyright (c) Feb 13, 2008 The University of Manchester. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * MANCHESTER UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 * software developed by The University of Manchester," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "The University of Manchester" and "Manchester University" must not be used to endorse or
 * promote products derived from this software.
 */
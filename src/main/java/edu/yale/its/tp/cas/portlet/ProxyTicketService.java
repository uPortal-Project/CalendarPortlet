/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.portlet;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.yale.its.tp.cas.client.CASReceipt;
import edu.yale.its.tp.cas.client.ProxyTicketValidator;
import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

public class ProxyTicketService {
	
	private static Log log = LogFactory.getLog(ProxyTicketService.class);

	private String casValidateUrl = "https://secure.its.yale.edu/cas/proxyValidate";
	private String serviceUrl = "https://portaltest.its.yale.edu/CalendarPortlet";
	private String urlOfProxyCallbackServlet = "https://portaltest.its.yale.edu/CalendarPortlet/CasProxyCallback";

	
	public CASReceipt getProxyTicket(String ticket) throws IOException, SAXException, ParserConfigurationException {

		String errorCode = null;
		String errorMessage = null;
		String xmlResponse = null;

		log.trace("validateURL: " + this.casValidateUrl + ", serviceURL: " + this.serviceUrl + ", ticket: " + ticket + ", callbackUrl: " + this.urlOfProxyCallbackServlet);
		
		/* instantiate a new ProxyTicketValidator */
		ProxyTicketValidator pv = new ProxyTicketValidator();

		/* set its parameters */
		pv.setCasValidateUrl(this.casValidateUrl);
		pv.setService(this.serviceUrl);
		pv.setServiceTicket(ticket);
		pv.setProxyCallbackUrl(this.urlOfProxyCallbackServlet);

		/* contact CAS and validate */
		pv.validate();

		/* if we want to look at the raw response, we can use getResponse() */
		xmlResponse = pv.getResponse();
		log.trace("response: " + xmlResponse);

		/* read the response */
		// Yes, this method is misspelled in this way 
		// in the ServiceTicketValidator implementation. 
		// Sorry.
		if (pv.isAuthenticationSuccesful()) {
			log.trace("authentication successful");
		} else {
			errorCode = pv.getErrorCode();
			errorMessage = pv.getErrorMessage();
			/* handle the error */
			log.trace("cas error! " + errorCode + ": " + errorMessage);
		}
		
		CASReceipt receipt = new CASReceipt();
		receipt.setPgtIou(pv.getPgtIou());
		receipt.setUserName(pv.getUser());
		
		return receipt;

	}
	
	public String getCasServiceToken(CASReceipt receipt, String target) {
		String pgtIou = receipt.getPgtIou();
        if (log.isTraceEnabled()) {
            log.trace("entering getCasServiceToken(" + target
                    + "), previously cached receipt=["
                    + pgtIou + "]");
        }
        if (pgtIou == null){
            if (log.isDebugEnabled()){
                log.debug("Returning null CAS Service Token because cached receipt does not include a PGTIOU.");
            }
            return null;
        }
        String proxyTicket;
        try {
            proxyTicket = ProxyTicketReceptor.getProxyTicket(pgtIou, target);
        } catch (IOException e) {
            log.error("Error contacting CAS server for proxy ticket", e);
            return null;
        }
        if (proxyTicket == null){
            log.error("Failed to obtain proxy ticket using receipt [" + pgtIou + "], has the Proxy Granting Ticket referenced by the pgtIou expired?");
            return null;
        }
        if (log.isTraceEnabled()) {
            log.trace("returning from getCasServiceToken(), returning proxy ticket ["
                    + proxyTicket + "]");
        }
        return proxyTicket;
	}

	public void setCasValidateUrl(String casValidateUrl) {
		this.casValidateUrl = casValidateUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public void setUrlOfProxyCallbackServlet(String urlOfProxyCallbackServlet) {
		this.urlOfProxyCallbackServlet = urlOfProxyCallbackServlet;
	}

}

/*
 * ProxyTicketService.java
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
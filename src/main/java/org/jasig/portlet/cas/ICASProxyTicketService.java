package org.jasig.portlet.cas;

import javax.portlet.PortletRequest;

import edu.yale.its.tp.cas.client.CASReceipt;

/**
 * IProxyTicketService provides an interface for procuring proxy tickets.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 */
public interface ICASProxyTicketService {

	/**
	 * Retrieve a CAS receipt for the specified portlet request.
	 * 
	 * @param request
	 * @param ticket
	 * @return
	 */
	public CASReceipt getProxyTicket(PortletRequest request);
	
	
	/**
	 * Return a proxy ticket for a CAS receipt and URL target.
	 * 
	 * @param receipt CAS receipt for the current user
	 * @param target URL of the service to be proxied
	 * @return
	 */
	public String getCasServiceToken(CASReceipt receipt, String target);

}

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

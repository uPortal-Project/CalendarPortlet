/*
 * Created on May 19, 2008
 *
 * Copyright(c) The University of Manchester, May 19, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.service;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

/**
 * CachedCredentialsInitializationService initializes authentication resources when a 
 * user creates a new session with the portlet.
 *
 * @author Anthony Colebourne
 */
public class CachedCredentialsInitializationService implements IInitializationService {
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portlet.calendar.service.IInitializationService#initialize(javax.portlet.PortletRequest)
	 */
	public void initialize(javax.portlet.PortletRequest request) {
		
		// get the UserInfo map from the portlet session
		PortletSession session = request.getPortletSession();
		@SuppressWarnings("unchecked")
		Map<String,String> userinfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);
		
		// get the credentials for this portlet from the UserInfo map
		String password = (String) userinfo.get("password");
		session.setAttribute("password", password);
	}	
}

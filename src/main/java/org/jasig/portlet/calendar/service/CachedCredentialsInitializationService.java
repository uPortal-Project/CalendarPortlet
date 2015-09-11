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

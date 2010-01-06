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
package org.jasig.portlet.calendar.credentials;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.adapter.CalendarException;


/**
 * This {@link ICredentialsExtractor} implementation can retrieve the necessary
 * username and password from named attributes within the user's {@link HttpSession}
 * or {@link PortletSession}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: RequestAttributeCredentialsExtractorImpl.java Exp $
 */
public class RequestAttributeCredentialsExtractorImpl implements ICredentialsExtractor {

	protected final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Default constructor
	 */
	public RequestAttributeCredentialsExtractorImpl() { }
	
	/**
	 * @param passwordAttribute
	 * @param usernameAttribute
	 */
	public RequestAttributeCredentialsExtractorImpl(String passwordAttribute,
			String usernameAttribute) {
		this.passwordAttribute = passwordAttribute;
		this.usernameAttribute = usernameAttribute;
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.CredentialsExtractor#getCredentials(javax.servlet.http.HttpServletRequest)
	 */
	public Credentials getCredentials(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			log.error("null session");
			throw new CalendarException();
		}
		
		String username = (String) session.getAttribute(usernameAttribute);
		if (username == null) {
			log.error("username attribute (" + usernameAttribute + ") does not exist in session");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute(passwordAttribute);
		if (password == null) {
			log.error("password attribute (" + passwordAttribute + ") does not exist in session");
			throw new CalendarException();
		}
		return new UsernamePasswordCredentials(username, password);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portlet.calendar.adapter.CredentialsExtractor#getCredentials(javax.portlet.PortletRequest)
	 */
	public Credentials getCredentials(PortletRequest request) {
		// get the session
		PortletSession session = request.getPortletSession(false);
		if (session == null) {
			log.error("null session");
			throw new CalendarException();
		}

		// retrieve the user's credentials
		String username = (String) session.getAttribute(usernameAttribute);
		if (username == null) {
			log.error("username attribute (" + usernameAttribute + ") does not exist in session");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute(passwordAttribute);
		if (password == null) {
			log.error("password attribute (" + passwordAttribute + ") does not exist in session");
			throw new CalendarException();
		}
		return new UsernamePasswordCredentials(username, password);
	}

	private String usernameAttribute = "username";
	public void setUsernameAttribute(String usernameAttribute) {
		this.usernameAttribute = usernameAttribute;
	}

	private String passwordAttribute = "password";
	public void setPasswordAttribute(String passwordAttribute) {
		this.passwordAttribute = passwordAttribute;
	}

	
}

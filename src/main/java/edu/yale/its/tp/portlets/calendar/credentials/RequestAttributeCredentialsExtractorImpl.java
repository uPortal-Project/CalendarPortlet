/*******************************************************************************
* Copyright 2008, The Board of Regents of the University of Wisconsin System.
* All rights reserved.
*
* A non-exclusive worldwide royalty-free license is granted for this Software.
* Permission to use, copy, modify, and distribute this Software and its
* documentation, with or without modification, for any purpose is granted
* provided that such redistribution and use in source and binary forms, with or
* without modification meets the following conditions:
*
* 1. Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
* acknowledgement:
*
* "This product includes software developed by The Board of Regents of
* the University of Wisconsin System.
*
*THIS SOFTWARE IS PROVIDED BY THE BOARD OF REGENTS OF THE UNIVERSITY OF
*WISCONSIN SYSTEM "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
*BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
*PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE BOARD OF REGENTS OF
*THE UNIVERSITY OF WISCONSIN SYSTEM BE LIABLE FOR ANY DIRECT, INDIRECT,
*INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
*LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
*OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
*ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
package edu.yale.its.tp.portlets.calendar.credentials;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.adapter.CalendarException;

/**
 * This {@link ICredentialsExtractor} implementation can retrieve the necessary
 * username and password from named attributes within the user's {@link HttpSession}
 * or {@link PortletSession}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: RequestAttributeCredentialsExtractorImpl.java Exp $
 */
public class RequestAttributeCredentialsExtractorImpl implements ICredentialsExtractor {

	private Log log = LogFactory.getLog(this.getClass());
	
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
	 * @see edu.yale.its.tp.portlets.calendar.adapter.CredentialsExtractor#getCredentials(javax.servlet.http.HttpServletRequest)
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
	 * @see edu.yale.its.tp.portlets.calendar.adapter.CredentialsExtractor#getCredentials(javax.portlet.PortletRequest)
	 */
	public Credentials getCredentials(PortletRequest request) {
		// get the session
		PortletSession session = request.getPortletSession(false);
		if (session == null) {
			log.error("null session");
			throw new CalendarException();
		}

		// retrieve the user's credentials
		String username = (String) session.getAttribute(usernameAttribute, PortletSession.APPLICATION_SCOPE);
		if (username == null) {
			log.error("username attribute (" + usernameAttribute + ") does not exist in session");
			throw new CalendarException();
		}
		String password = (String) session.getAttribute(passwordAttribute, PortletSession.APPLICATION_SCOPE);
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

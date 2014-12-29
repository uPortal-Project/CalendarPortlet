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

package org.jasig.portlet.calendar.adapter.exchange;

import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.jasig.portlet.calendar.service.IInitializationService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

/**
 * ExchangeCredentialsInitializationService creates a Credentials object from the user's login id and
 * cached password and saves it to a ThreadLocal for later use.
 * This class was enhanced to allow for an institution to have an exchange adapter connected to an on-premise
 * Exchange server (username is simple username, ntlm domain required) and another exchange adapter connected to
 * Office365 (username is email address, no ntlm domain) with the behavior determined by a combination of
 * portlet preferences and bean configuration.
 *
 * ExchangeCredentialsInitializationService stores a username and password (taken from read-only portlet preferences which
 * will be used to authenticate against exchange.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExchangeCredentialsInitializationService implements IInitializationService {
    public final static String PREFS_NTDOMAIN = "exchangeNtlmDomain";

    private String usernameAttribute = "user.login.id";
    private String passwordAttribute = "password";
    private String mailAttribute = "mail";
    private String ntlmDomain = null;

    /**
     * Set the name of the user attribute to be used for retrieving the Exchange
     * authentication username from the portlet UserInfo map. 
     *
     * @param usernameAttribute
     */
    public void setUsernameAttribute(String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    /**
     * Set the name of the user attribute to be used for retrieving the Exchange
     * authentication password from the portlet UserInfo map.
     *
     * @param passwordAttribute
     */
    public void setPasswordAttribute(String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

    /**
     * Set the name of the user attribute used for retrieving the user's email address.  This is used for
     * Office365 integration.
     * @param mailAttribute
     */
    public void setMailAttribute(String mailAttribute) {
        this.mailAttribute = mailAttribute;
    }

    /**
     * Set the domain () of this machine for NTLM authentication.
     *
     * @param ntlmDomain NT Domain
     */
    public void setNtlmDomain(String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    public void initialize(PortletRequest request) {

        // get the username and password from the UserInfo map
        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String username = userInfo.get(usernameAttribute);
        String password = userInfo.get(passwordAttribute);
        if (password == null) {
            throw new CalendarException("Required user attribute password is null. Insure user-attribute password"
                    + " is enabled in portlet.xml and CAS ClearPass is properly configured");
        }

        // Get the NTLM Domain from portlet preferences if specified, else from the configuration properties.
        PortletPreferences prefs = request.getPreferences();
        String ntlmDomain = prefs.getValue(PREFS_NTDOMAIN, "");
        ntlmDomain = StringUtils.isBlank(ntlmDomain) ? this.ntlmDomain : ntlmDomain;

        // Construct a credentials object from the username and password.
        // If the domain is specified, we are authenticating to a domain so we need to return NT credentials
        Object credentials;
        if (StringUtils.isNotBlank(ntlmDomain)) {
            credentials = createNTCredentials(ntlmDomain, username, password);
        } else {
            String emailAddress = userInfo.get(this.mailAttribute);
            if (emailAddress == null) {
                throw new CalendarException("Required user attribute email address is null. Insure user-attribute mail"
                        + " is enabled in portlet.xml and populated via LDAP or other approach");
            }
            credentials= new UsernamePasswordCredentials(emailAddress, password);

            // TODO Revise for exchange impersonation
            //do not fill in the domain field else authentication fails with a 503, service not available response
            credentials = new NTCredentials(prefs.getValue("wsUser", ""), prefs.getValue("wsPassword", ""),
                    "paramDoesNotSeemToMatter", "");

        }

        // cache the credentials object to this thread
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            requestAttributes = new PortletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
        requestAttributes.setAttribute(
                ExchangeWsCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE,
                credentials, RequestAttributes.SCOPE_SESSION);
    }

    protected Credentials createNTCredentials(String ntlmDomain, String username, String password) {
        // For Exchange domain integration, only the username is applicable, not the email address.  If present
        // remove the @domain part of an email address in case the user or admin specified an email address
        // and a password in the user config UI.
        if (username == null) {
            throw new CalendarException("Required user attribute username is null. Insure user-attribute user.login.id"
                    + " is enabled in portlet.xml");
        }
        int index = username.indexOf("@");
        username = index > 0 ? username.substring(0, index) : username;

        // construct a credentials object from the username and password
        return new NTCredentials(username, password, "paramDoesNotSeemToMatter", ntlmDomain);
    }

}

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

import javax.portlet.PortletRequest;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.jasig.portlet.calendar.service.IInitializationService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

/**
 * ExchangeCredentialsInitializationService creates a Credentials object from the 
 * user's login id and cached password and saves it to a ThreadLocal for
 * later use.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExchangeCredentialsInitializationService implements
        IInitializationService {

    private String usernameAttribute = "user.login.id";
    
    /**
     * Set the name of the user attribute to be used for retrieving the Exchange
     * authentication username from the portlet UserInfo map. 
     * 
     * @param usernameAttribute
     */
    public void setUsernameAttribute(String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }
    
    private String passwordAttribute = "password";
    
    /**
     * Set the name of the user attribute to be used for retrieving the Exchange
     * authentication password from the portlet UserInfo map.
     * 
     * @param passwordAttribute
     */
    public void setPasswordAttribute(String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

    private String ntlmDomain = null;
    
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

        // construct a credentials object from the username and password
        Credentials credentials = new NTCredentials(username, password, "paramDoesNotSeemToMatter", ntlmDomain);
        
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

}

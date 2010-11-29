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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockPortletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

public class ExchangeCredentialsInitializationServiceTest {

    PortletRequest request;
    ExchangeCredentialsInitializationService service;
    
    @Before
    public void setUp() {
        Map<String,String> userInfo = new HashMap<String,String>();
        userInfo.put("username", "user");
        userInfo.put("pass", "pass");

        request = new MockPortletRequest();
        request.setAttribute(PortletRequest.USER_INFO, userInfo);
        service = new ExchangeCredentialsInitializationService();
        service.setPasswordAttribute("pass");
        service.setUsernameAttribute("username");
    }
    
    @Test
    public void testInitialize() {
        service.initialize(request);
        
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) requestAttributes.getAttribute(ExchangeHttpWebServiceMessageSender.EXCHANGE_CREDENTIALS_ATTRIBUTE, RequestAttributes.SCOPE_SESSION);
        assertEquals("user", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
    }

    
    @Test
    public void testWithExistingRequestInitialize() {
        final RequestAttributes requestAttributes = new PortletRequestAttributes(request);
        requestAttributes.setAttribute("testAttr", "testVal", RequestAttributes.SCOPE_SESSION);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        
        service.initialize(request);
        
        final RequestAttributes newRequestAttributes = RequestContextHolder.getRequestAttributes();
        final UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) newRequestAttributes.getAttribute(ExchangeHttpWebServiceMessageSender.EXCHANGE_CREDENTIALS_ATTRIBUTE, RequestAttributes.SCOPE_SESSION);
        assertEquals("user", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
        assertEquals("testVal", newRequestAttributes.getAttribute("testAttr", RequestAttributes.SCOPE_SESSION));
    }

}

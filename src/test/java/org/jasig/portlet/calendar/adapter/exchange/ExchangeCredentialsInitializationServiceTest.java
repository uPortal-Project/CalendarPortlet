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

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockPortletPreferences;
import org.springframework.mock.web.portlet.MockPortletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExchangeCredentialsInitializationServiceTest {

    PortletRequest request;
    ExchangeCredentialsInitializationService service;

    @Before
    public void setUp() {
        Map<String,String> userInfo = new HashMap<String,String>();
        userInfo.put("username", "user");
        userInfo.put("pass", "pass");
        userInfo.put("email", "email@foo.net");

        request = new MockPortletRequest();
        request.setAttribute(PortletRequest.USER_INFO, userInfo);
        service = new ExchangeCredentialsInitializationService();
        service.setPasswordAttribute("pass");
        service.setUsernameAttribute("username");
        service.setMailAttribute("email");
    }

    private UsernamePasswordCredentials getRegularCredentials() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return (UsernamePasswordCredentials) requestAttributes.getAttribute(ExchangeWsCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE, RequestAttributes.SCOPE_SESSION);
    }

    private NTCredentials getNTCredentials() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return (NTCredentials) requestAttributes.getAttribute(ExchangeWsCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE, RequestAttributes.SCOPE_SESSION);
    }

    @Test
    public void testInitializeNoDomain() {
        service.initialize(request);

        final UsernamePasswordCredentials credentials = getRegularCredentials();
        assertEquals("email@foo.net", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
    }

    @Test
    public void testInitializeWithDomain() {
        service.setNtlmDomain("testDomain");
        service.initialize(request);

        final NTCredentials credentials = getNTCredentials();
        assertEquals("user", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
        assertEquals("testDomain".toUpperCase(), credentials.getDomain());
    }

    @Test
    public void testWithExistingRequestInitialize() {
        service.setNtlmDomain("testDomain");
        final RequestAttributes requestAttributes = new PortletRequestAttributes(request);
        requestAttributes.setAttribute("testAttr", "testVal", RequestAttributes.SCOPE_SESSION);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        service.initialize(request);

        final NTCredentials credentials = getNTCredentials();
        assertEquals("user", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
        assertEquals("testDomain".toUpperCase(), credentials.getDomain());

        final RequestAttributes newRequestAttributes = RequestContextHolder.getRequestAttributes();
        assertEquals("testVal", newRequestAttributes.getAttribute("testAttr", RequestAttributes.SCOPE_SESSION));
    }

    @Test
    public void testExchangeImpersonation() throws Exception {
        service.setNtlmDomain("testDomain");

        request = new MockPortletRequest();
        MockPortletPreferences mockPreferences = new MockPortletPreferences();
        mockPreferences.setValue("exchangeImpersonationUsername", "blah@ed.ac.uk");
        mockPreferences.setValue("exchangeImpersonationPassword", "rand");
        ((MockPortletRequest)request).setPreferences(mockPreferences);

        assertTrue(service.usesExchangeImpersonation(request));

        service.initialize(request);
        final NTCredentials credentials = getNTCredentials();
        assertEquals("blah@ed.ac.uk", credentials.getUserName());
        assertEquals("rand", credentials.getPassword());
        assertEquals("", credentials.getDomain());
    }
}

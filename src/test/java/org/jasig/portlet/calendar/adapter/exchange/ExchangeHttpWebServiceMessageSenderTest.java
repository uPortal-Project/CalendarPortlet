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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockPortletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

public class ExchangeHttpWebServiceMessageSenderTest {

    ExchangeHttpWebServiceMessageSender sender;
    RequestAttributes attr;
    
    @Before
    public void setUp() throws Exception {
        sender = new ExchangeHttpWebServiceMessageSender();
        sender.setConnectionTimeout(1000);
        sender.setMaxConnections(5);
        sender.setReadTimeout(1000);
        sender.afterPropertiesSet();
        
        attr = new PortletRequestAttributes(new MockPortletRequest());
        attr.setAttribute(ExchangeHttpWebServiceMessageSender.EXCHANGE_CREDENTIALS_ATTRIBUTE, new UsernamePasswordCredentials("user", "pass"), RequestAttributes.SCOPE_SESSION);
        RequestContextHolder.setRequestAttributes(attr);
        
    }
    
    @Test
    public void testGetCredentials() throws URISyntaxException, IOException {
        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) sender.getCredentials();
        assertEquals("user", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
    }
    
    @Test
    public void testGetClient() {
        HttpClient client = sender.getClient();
        assertNotNull(client);
        assertEquals(1000, client.getHttpConnectionManager().getParams().getConnectionTimeout());
        assertEquals(1000, client.getHttpConnectionManager().getParams().getSoTimeout());
        assertEquals(5, client.getHttpConnectionManager().getParams().getMaxTotalConnections());
        assertEquals(5, client.getHttpConnectionManager().getParams().getDefaultMaxConnectionsPerHost());
        
        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) client.getState().getCredentials(AuthScope.ANY);
        assertEquals("user", credentials.getUserName());
        assertEquals("pass", credentials.getPassword());
    }
    
    @Test
    public void testGetExistingClient() {
        final HttpClient client = sender.getClient();
        final HttpClient client2 = sender.getClient();
        assertSame(client, client2);
    }
    
    @Test
    public void testUpdateConnectionParameters() {
        sender.setConnectionTimeout(2000);
        sender.setReadTimeout(2000);
        sender.setMaxConnections(10);
        
        HttpClient client = sender.getClient();
        assertNotNull(client);
        assertEquals(2000, client.getHttpConnectionManager().getParams().getConnectionTimeout());
        assertEquals(2000, client.getHttpConnectionManager().getParams().getSoTimeout());
        assertEquals(10, client.getHttpConnectionManager().getParams().getMaxTotalConnections());
        assertEquals(10, client.getHttpConnectionManager().getParams().getDefaultMaxConnectionsPerHost());
    }
    
    @After
    public void tearDown() throws Exception {
        sender.destroy();
    }
    
}

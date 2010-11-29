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

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.AbstractHttpWebServiceMessageSender;
import org.springframework.ws.transport.http.HttpTransportConstants;

/**
 * ExchangeHttpWebServiceMessageSender is a subclass of 
 * AbstractHttpWebServiceMessageSender designed to connect to an Exchange web
 * service protected by user-specific basic authentication.  The default
 * Spring implementations of Http-based message senders only allow for performing
 * basic authentication with shared credentials.
 * 
 * This implementation is dependent on the user credentials object existing in a
 * ThreadLocal managed by a Spring RequestContextHolder.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExchangeHttpWebServiceMessageSender extends
        AbstractHttpWebServiceMessageSender implements InitializingBean,
        DisposableBean {

    protected static final String EXCHANGE_CREDENTIALS_ATTRIBUTE = "exchangeCredentials";
    protected static final String EXCHANGE_CLIENT_ATTRIBUTE = "exchangeHttpClient";
    
    private int connectionTimeout = (60 * 1000);
    
    /**
     * Set the connection timeout.
     * 
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        if (connectionManager != null) {
            this.connectionManager.getParams().setConnectionTimeout(connectionTimeout);
        }
    }
    
    private int readTimeout = (60 * 1000);
    
    /**
     * Set the read timeout.
     * 
     * @param readTimeout
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        if (connectionManager != null) {
            this.connectionManager.getParams().setSoTimeout(readTimeout);
        }
    }
    
    private int maxConnections = 200;
    
    /**
     * Set the max connections for the overall client.  The max connections 
     * per host will also be set to this same value, since the expected usage
     * pattern involves an http connection manager making connections to only
     * one host.
     * 
     * @param maxConnections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        if (connectionManager != null) {
            this.connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnections);
            this.connectionManager.getParams().setMaxTotalConnections(maxConnections);
        }
    }
    
    private MultiThreadedHttpConnectionManager connectionManager;

    /* (non-Javadoc)
     * @see org.springframework.ws.transport.WebServiceMessageSender#createConnection(java.net.URI)
     */
    public WebServiceConnection createConnection(URI uri) throws IOException {
        
        // construct a PostMethod for the supplied URI
        final PostMethod postMethod = new PostMethod(uri.toString());
        if (isAcceptGzipEncoding()) {
            postMethod.addRequestHeader(
                    HttpTransportConstants.HEADER_ACCEPT_ENCODING,
                    HttpTransportConstants.CONTENT_ENCODING_GZIP);
        }

        // attempt to find a cached HttpClient instance in the ThreadLocal
        final HttpClient client = getClient();

        return new CommonsHttpConnection(client, postMethod);
    }
    
    /**
     * Return an HttpClient instance configured with authentication credentials
     * for the current user.  This implementation caches HttpClient instances
     * in a RequestContextHolder for later use by the same user.
     * 
     * @return HttpClient authenticated client
     */
    protected HttpClient getClient() {
        // attempt to find a cached HttpClient instance in the ThreadLocal
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();        
        HttpClient client = (HttpClient) requestAttributes.getAttribute(
                EXCHANGE_CLIENT_ATTRIBUTE, RequestAttributes.SCOPE_SESSION);

        // if no client was found, create a new one using the cached credentials
        if (client == null) {
            client = new HttpClient(connectionManager);
            
            // configure basic authentication using the current user's credentials
            Credentials credentials = getCredentials();
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getParams().setAuthenticationPreemptive(true);

            // save the HttpClient in a ThreadLocal
            requestAttributes.setAttribute(EXCHANGE_CLIENT_ATTRIBUTE,
                    client, RequestAttributes.SCOPE_SESSION);
        }
        
        return client;
    }
    
    /**
     * Get the credentials to use with the HttpClient.  This implementation
     * pulls an existing crednetials object from a Spring RequestContextHolder.
     * 
     * @return
     */
    protected Credentials getCredentials() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final Credentials credentials = (Credentials) requestAttributes.getAttribute(EXCHANGE_CREDENTIALS_ATTRIBUTE, RequestAttributes.SCOPE_SESSION);            
        return credentials;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        // create a new connection manager with the configured connection parameters
        this.connectionManager = new MultiThreadedHttpConnectionManager();
        this.connectionManager.getParams().setConnectionTimeout(connectionTimeout);
        this.connectionManager.getParams().setSoTimeout(readTimeout);
        this.connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnections);
        this.connectionManager.getParams().setMaxTotalConnections(maxConnections);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        // shut down the connection manager
        connectionManager.shutdown();
    }

}

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

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class NtlmAuthHttpClient extends DefaultHttpClient {
    
    public NtlmAuthHttpClient() {
        setup();
    }

    public NtlmAuthHttpClient(ClientConnectionManager conman) {
        super(conman);
        setup();
    }

    public NtlmAuthHttpClient(ClientConnectionManager conman, HttpParams params) {
        super(conman, params);
        setup();
    }

    public NtlmAuthHttpClient(HttpParams params) {
        super(params);
        setup();
    }
    
    private void setup() {
        addRequestInterceptor(new RemoveSoapHeadersInterceptor(), 0);
        AuthSchemeFactory fac = new NTLMSchemeFactory();
        AuthSchemeFactory bfac = new BasicSchemeFactory();
        getAuthSchemes().register(AuthPolicy.BASIC, bfac);
        getAuthSchemes().register(AuthPolicy.NTLM, fac);
        getAuthSchemes().register(AuthPolicy.SPNEGO, fac);
    }

    /**
     * HttpClient {@link org.apache.http.HttpRequestInterceptor} implementation that removes {@code Content-Length} and
     * {@code Transfer-Encoding} headers from the request. Necessary, because SAAJ and other SOAP implementations set these
     * headers themselves, and HttpClient throws an exception if they have been set.
     */
   private static final class RemoveSoapHeadersInterceptor implements HttpRequestInterceptor {
 
       @Override
       public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
           if (request instanceof HttpEntityEnclosingRequest) {
               if (request.containsHeader(HTTP.TRANSFER_ENCODING)) {
                   request.removeHeaders(HTTP.TRANSFER_ENCODING);
               }
               if (request.containsHeader(HTTP.CONTENT_LEN)) {
                   request.removeHeaders(HTTP.CONTENT_LEN);
               }
           }
       }

   }

   private static final class NTLMSchemeFactory implements AuthSchemeFactory {

       @Override
       public AuthScheme newInstance(final HttpParams params) {
           return new NTLMScheme(new JCIFSEngine());
       }

   }

}

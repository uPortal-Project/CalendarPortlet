/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.calendar.credentials;

import javax.portlet.PortletRequest;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

/**
 * This {@link ICredentialsExtractor} implementation allows deployers to specify a "shared" set of
 * credentials that are used for ALL requests.
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: SharedSecretCredentialsExtractorImpl.java Exp $
 */
public class SharedSecretCredentialsExtractorImpl implements ICredentialsExtractor {

  private final String username;
  private final String password;

  /**
   * @param password
   * @param username
   */
  public SharedSecretCredentialsExtractorImpl(String password, String username) {
    this.password = password;
    this.username = username;
  }

  /* (non-Javadoc)
   * @see org.jasig.portlet.calendar.adapter.CredentialsExtractor#getCredentials(javax.portlet.PortletRequest)
   */
  public Credentials getCredentials(PortletRequest request) {
    return new UsernamePasswordCredentials(username, password);
  }
}

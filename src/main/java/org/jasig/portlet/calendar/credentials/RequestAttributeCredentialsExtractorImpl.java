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

import java.util.Map;
import javax.portlet.PortletRequest;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This {@link ICredentialsExtractor} implementation retrieves the necessary username and password
 * from named attributes within the USER_INFO map bound to the {@link javax.portlet.PortletRequest}
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: RequestAttributeCredentialsExtractorImpl.java Exp $
 */
public final class RequestAttributeCredentialsExtractorImpl implements ICredentialsExtractor {

  private final Log log = LogFactory.getLog(getClass());

  /** Default constructor */
  public RequestAttributeCredentialsExtractorImpl() {}

  /**
   * @param passwordAttribute
   * @param usernameAttribute
   */
  public RequestAttributeCredentialsExtractorImpl(
      String passwordAttribute, String usernameAttribute) {
    this.passwordAttribute = passwordAttribute;
    this.usernameAttribute = usernameAttribute;
  }

  /* (non-Javadoc)
   * @see org.jasig.portlet.calendar.adapter.CredentialsExtractor#getCredentials(javax.portlet.PortletRequest)
   */
  public Credentials getCredentials(PortletRequest request) {
    Map<String, String> userInfo =
        (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
    String username = userInfo.get(usernameAttribute);
    if (StringUtils.isBlank(username)) {
      log.warn(
          "No username found in the USER_INFO map on the PortletRequest;  attribute name was:  "
              + usernameAttribute);
    }
    String password = userInfo.get(passwordAttribute);
    if (StringUtils.isBlank(password)) {
      log.warn(
          "No password found in the USER_INFO map on the PortletRequest;  attribute name was:  "
              + passwordAttribute);
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

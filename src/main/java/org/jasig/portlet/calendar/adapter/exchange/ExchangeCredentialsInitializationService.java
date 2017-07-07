/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

/**
 * ExchangeCredentialsInitializationService creates a Credentials object from the first one of the
 * following sources (in priority order) and saves it to a ThreadLocal for later use:
 *
 * <ol>
 *   <li>The configured Exchange Impersonation trusted service account credentials if the username
 *       and password are configured in portlet preferences
 *   <li>The user's username (login id) and cached password if the Exchange NTLM domain is
 *       specified. Typically used for an on-premise Exchange server without using Exchange
 *       Impersonation.
 *   <li>The user's email address and cached password if the Exchange NTLM domain is not specified.
 *       Typically used for Office365 integration without using Exchange Impersonation.
 * </ol>
 *
 * <br>
 * To allow for multiple exchange destinations (on-premise Exchange Server and off-premise
 * Office365) and potentially different environments (different credentials for test environment)
 * the credentials are obtained from a combination of portlet preferences and bean configuration
 * (property files).
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExchangeCredentialsInitializationService
    implements IExchangeCredentialsInitializationService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  public static final String PREFS_NTDOMAIN = "exchangeNtlmDomain";
  public static final String PREFS_IMPERSONATION_USERNAME = "exchangeImpersonationUsername";
  public static final String PREFS_IMPERSONATION_PASSWORD = "exchangeImpersonationPassword";

  private String usernameAttribute = "user.login.id";
  private String passwordAttribute = "password";
  private String mailAttribute = "mail";
  private String ntlmDomain = null;

  /**
   * Set the name of the user attribute to be used for retrieving the Exchange authentication
   * username from the portlet UserInfo map.
   *
   * @param usernameAttribute
   */
  public void setUsernameAttribute(String usernameAttribute) {
    this.usernameAttribute = usernameAttribute;
  }

  /**
   * Set the name of the user attribute to be used for retrieving the Exchange authentication
   * password from the portlet UserInfo map.
   *
   * @param passwordAttribute
   */
  public void setPasswordAttribute(String passwordAttribute) {
    this.passwordAttribute = passwordAttribute;
  }

  /**
   * Set the name of the user attribute used for retrieving the user's email address. This is used
   * for Office365 integration when not using Exchange Impersonation.
   *
   * @param mailAttribute name of the user attribute to obtain the user's email address from.
   */
  public void setMailAttribute(String mailAttribute) {
    this.mailAttribute = mailAttribute;
  }

  /**
   * Return the configured NTLM Domain if set. May be null or empty string. Portlet preference value
   * overrides bean configuration value to allow for multiple EWS integrations (on premise plus
   * off-premise).
   *
   * @param request Portlet Request
   * @return Configured NTLM Domain (may be null or empty string).
   */
  @Override
  public String getNtlmDomain(PortletRequest request) {
    PortletPreferences prefs = request.getPreferences();
    String ntlmDomain = prefs.getValue(PREFS_NTDOMAIN, "");
    return StringUtils.isBlank(ntlmDomain) ? this.ntlmDomain : ntlmDomain;
  }

  /**
   * Set the domain to use NTLM authentication.
   *
   * @param ntlmDomain NT Domain
   */
  public void setNtlmDomain(String ntlmDomain) {
    this.ntlmDomain = ntlmDomain;
  }

  public void initialize(PortletRequest request) {

    Object credentials;
    PortletPreferences prefs = request.getPreferences();

    // 1. Exchange Impersonation
    if (usesExchangeImpersonation(request)) {
      String exchangeImpersonationUsername = prefs.getValue(PREFS_IMPERSONATION_USERNAME, "");
      String exchangeImpersonationPassword = prefs.getValue(PREFS_IMPERSONATION_PASSWORD, "");

      //do not fill in the domain field else authentication fails with a 503, service not available response
      credentials =
          new NTCredentials(
              exchangeImpersonationUsername,
              exchangeImpersonationPassword,
              "paramDoesNotSeemToMatter",
              "");
      logger.debug("Creating Exchange Impersonation credentials for EWS call");
    } else {

      // Get the password from the UserInfo map.
      @SuppressWarnings("unchecked")
      Map<String, String> userInfo =
          (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
      String password = userInfo.get(passwordAttribute);
      if (password == null) {
        throw new CalendarException(
            "Required user attribute password is null. Insure user-attribute password"
                + " is enabled in portlet.xml and CAS ClearPass is configured properly");
      }

      // 2. If the domain is specified, return NT credentials from the username, password, and domain.
      String ntDomain = getNtlmDomain(request);
      if (StringUtils.isNotBlank(ntDomain)) {
        String username = userInfo.get(usernameAttribute);
        credentials = createNTCredentials(ntDomain, username, password);
        logger.debug("Creating NT credentials for {}", username);
      } else {
        // 3. Otherwise construct credentials from the email address and password for Office365 integration.
        String emailAddress = userInfo.get(this.mailAttribute);
        if (emailAddress == null) {
          throw new CalendarException(
              "Required user attribute email address is null. Insure user-attribute mail"
                  + " is enabled in portlet.xml and populated via LDAP or other approach");
        }
        credentials = new UsernamePasswordCredentials(emailAddress, password);
        logger.debug("Creating simple username/password credentials for {}", emailAddress);
      }
    }

    // cache the credentials object to this thread
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes == null) {
      requestAttributes = new PortletRequestAttributes(request);
      RequestContextHolder.setRequestAttributes(requestAttributes);
    }
    requestAttributes.setAttribute(
        ExchangeWsCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE,
        credentials,
        RequestAttributes.SCOPE_SESSION);
  }

  @Override
  public boolean usesExchangeImpersonation(PortletRequest request) {
    PortletPreferences prefs = request.getPreferences();
    String exchangeImpersonationUsername = prefs.getValue(PREFS_IMPERSONATION_USERNAME, "");
    String exchangeImpersonationPassword = prefs.getValue(PREFS_IMPERSONATION_PASSWORD, "");
    if (StringUtils.isBlank(exchangeImpersonationUsername)
        != StringUtils.isBlank(exchangeImpersonationPassword)) {
      logger.error(
          "Both {} and {} must be set if using Exchange Web Services (EWS) Impersonation, or"
              + " both must be blank if not using EWS Impersonation",
          PREFS_IMPERSONATION_USERNAME,
          PREFS_IMPERSONATION_PASSWORD);
    }
    return StringUtils.isNotBlank(exchangeImpersonationUsername)
        && StringUtils.isNotBlank(exchangeImpersonationPassword);
  }

  /**
   * Calculate the account ID string of the user whose account is being accessed if Exchange
   * Impersonation is being used, else null if not using Exchange Impersonation.
   *
   * @param request Portlet Request
   * @return account ID of the user whose account is being accessed with Exchange Impersonation,
   *     else null
   */
  @Override
  public String getImpersonatedAccountId(PortletRequest request) {
    if (usesExchangeImpersonation(request)) {
      @SuppressWarnings("unchecked")
      Map<String, String> userInfo =
          (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
      String username = userInfo.get(usernameAttribute);
      String domainName = getNtlmDomain(request);
      if (StringUtils.isBlank(username)) {
        throw new CalendarException(
            "Null Username obtained from user attribute "
                + usernameAttribute
                + ". It must be non-null when using Exchange Impersonation");
      }
      if (StringUtils.isBlank(domainName)) {
        throw new CalendarException(
            "Domain name must be specified in properties file or portlet "
                + " preferences when using Exchange Impersonation");
      }
      String impersonatedId = username + "@" + domainName;
      logger.debug("Returning Impersonated ID {}", impersonatedId);
      return impersonatedId;
    }
    return null;
  }

  protected Credentials createNTCredentials(String ntlmDomain, String username, String password) {
    if (username == null) {
      throw new CalendarException(
          "Required user attribute username is null. Insure user-attribute user.login.id"
              + " is present in the <user-attribute> section of portlet.xml");
    }

    // For Exchange domain integration, only the username is applicable, not the domain name. This allows the
    // rare case of using the email address rather than loginId for the credentials.  If a domain is present
    // in the username remove the @domain part.
    int index = username.indexOf("@");
    username = index > 0 ? username.substring(0, index) : username;

    // construct a credentials object from the username and password
    return new NTCredentials(username, password, "paramDoesNotSeemToMatter", ntlmDomain);
  }
}

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

import javax.portlet.PortletRequest;

import org.jasig.portlet.calendar.service.IInitializationService;

/**
 * Initialization service specific to Exchange credentials-related information.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public interface IExchangeCredentialsInitializationService extends IInitializationService {

    /**
     * Return the configured NTLM Domain if set.  May be null or empty string.
     *
     * @param request Portlet Request
     * @return Configured NTLM Domain (may be null or empty string).
     */
    String getNtlmDomain(PortletRequest request);

    /**
     * Returns true if this portlet is configured to use Exchange Impersonation, else false.
     * @param request Portlet Request
     * @return true if this portlet is configured to use Exchange Impersonation, else false.
     */
    boolean usesExchangeImpersonation(PortletRequest request);

    /**
     * Calculate the NT account ID string in the form of username@NTDomain of the user whose account is being
     * accessed if Exchange Impersonation is being used, else null if not using Exchange Impersonation.
     *
     * @param request Portlet Request
     * @return account ID of the user whose account is being accessed with Exchange Impersonation, else null
     */
    String getImpersonatedAccountId(PortletRequest request);
}

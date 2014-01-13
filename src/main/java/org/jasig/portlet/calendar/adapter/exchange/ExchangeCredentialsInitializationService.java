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

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.NTCredentials;
import org.jasig.portlet.calendar.service.IInitializationService;

/**
 * ExchangeCredentialsInitializationService stores a username and password (taken from read-only portlet preferences which
 * will be used to authenticate against exchange.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ExchangeCredentialsInitializationService implements
        IInitializationService {
	protected final Log log = LogFactory.getLog(getClass());
    public static volatile NTCredentials credentials;

	public void initialize(PortletRequest request) {
        PortletPreferences prefs= request.getPreferences();
       
        //do not fill in the domain field else authentication fails with a 503, service not available response
        credentials = new NTCredentials(prefs.getValue("wsUser", ""), prefs.getValue("wsPassword", ""),
        		"paramDoesNotSeemToMatter", "");
    }

}

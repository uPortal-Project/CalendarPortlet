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

package org.jasig.portlet.calendar.util;

import java.io.InputStream;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class StringCleaningUtil implements InitializingBean,
		ApplicationContextAware {

	private Policy policy;

	private String filePath;

	/**
	 * Set the file path to the Anti-samy policy file to be used for cleaning
	 * strings.
	 * 
	 * @param path
	 */
	public void setSecurityFile(String path) {
		this.filePath = path;
	}

	/**
	 * Clean a string using the configured Anti-samy policy file
	 * 
	 * @param dirtyInput
	 * @return
	 * @throws ScanException
	 * @throws PolicyException
	 */
	public String getCleanString(String dirtyInput) throws ScanException,
			PolicyException {
		AntiSamy as = new AntiSamy();
		CleanResults cr = as.scan(dirtyInput, policy);
		return cr.getCleanHTML();
	}

	private ApplicationContext ctx;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		InputStream stream = ctx.getResource(filePath).getInputStream();
		policy = Policy.getInstance(stream);
	}

}

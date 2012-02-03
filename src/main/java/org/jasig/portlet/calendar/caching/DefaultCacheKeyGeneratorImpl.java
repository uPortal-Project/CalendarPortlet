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

package org.jasig.portlet.calendar.caching;

import javax.portlet.PortletRequest;

import org.jasig.portlet.calendar.CalendarConfiguration;
import org.joda.time.Interval;


/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Header: DefaultCacheKeyGenerator.java Exp $
 */
public class DefaultCacheKeyGeneratorImpl implements ICacheKeyGenerator {
    
    private boolean includePeriod = false;
    
    public void setIncludePeriod(boolean includePeriod) {
        this.includePeriod = includePeriod;
    }
    
    public boolean isIncludePeriod() {
        return this.includePeriod;
    }
    
	public String getKey(CalendarConfiguration configuration,
			Interval interval, PortletRequest request, String calendarIdentifier) {
		final StringBuffer key = new StringBuffer();
		key.append(calendarIdentifier);
		if (includePeriod) {
		    key.append(".");
    		key.append(interval.toString());
		}
		return key.toString();
	}

}

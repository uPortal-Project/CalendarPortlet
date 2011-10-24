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

package org.jasig.portlet.calendar.adapter;

import java.io.Serializable;
import java.util.Set;

import net.fortuna.ical4j.model.component.VEvent;

/**
 * CalendarEventSet represents a set of cacheable calendar events.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class CalendarEventSet implements Serializable {

    private final String key;
    private final Set<VEvent> events;
    
    public CalendarEventSet(String key, Set<VEvent> events) {
        this.key = key;
        this.events = events;
    }

    public String getKey() {
        return key;
    }

    public Set<VEvent> getEvents() {
        return events;
    }

}

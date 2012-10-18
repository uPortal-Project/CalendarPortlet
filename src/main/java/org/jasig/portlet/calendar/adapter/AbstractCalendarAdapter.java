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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasig.portlet.form.parameter.Parameter;

/**
 * AbstractCalendarAdapter provides a base representation of a calendar
 * adapter, without any implementation-specfic functionality.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public abstract class AbstractCalendarAdapter implements ICalendarAdapter {

    private String titleKey;
    private String descriptionKey;
    private List<Parameter> parameters = Collections.emptyList();  // For adapters that don't need parameters
    
    @Override
    public String getTitleKey() {
        return this.titleKey;
    }

    @Override
    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    /**
     * Creates a CalendarEventSet from a set of calendar events, inserts it into
     * the cache, and copies the cached element's expiration time into the
     * CalendarEventSet.
     *
     * @param cache Cache to insert the event set into
     * @param processorCacheKey Key for the event set
     * @param events set of calendar events to cache
     * @return Cached CalendarEventSet with cache expiration indication
     */
    protected CalendarEventSet insertCalendarEventSetIntoCache(
            Cache cache, String processorCacheKey, Set<VEvent> events) {
        CalendarEventSet eventSet = new CalendarEventSet(processorCacheKey, events);
        Element cachedElement = new Element(processorCacheKey, eventSet);
        cache.put(cachedElement);
        eventSet.setExpirationTime(cachedElement.getExpirationTime());
        return eventSet;
    }
}

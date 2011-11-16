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

import java.util.List;

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
    private List<Parameter> parameters;
    
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
    
}

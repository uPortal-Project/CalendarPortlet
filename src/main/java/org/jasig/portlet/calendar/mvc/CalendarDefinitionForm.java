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

package org.jasig.portlet.calendar.mvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.map.LazyMap;
import org.jasig.portlet.form.attribute.Attribute;
import org.jasig.portlet.form.attribute.AttributeFactory;

public class CalendarDefinitionForm {
	
	private Long id = new Long(-1);
	
	private String className;
	
	private String name;
	
    private String fname;

    private Set<String> role = new HashSet<String>();

    @SuppressWarnings("unchecked")
    private Map<String, Attribute> parameters = LazyMap.decorate(
            new HashMap<String, Attribute>(), new AttributeFactory());

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

    public String getFname() {
        return fname;
    }
    
    public void setFname(String fname) {
        this.fname = fname;
    }

    public Set<String> getRole() {
		return role;
	}

	public void setRole(Set<String> role) {
		this.role = role;
	}
	
	public void addParameter(String name, String value) {
	    this.parameters.put(name, new Attribute(value));
	}
	
	public void addParameters(Map<String,String> map) {
		Set<Entry<String,String>> entries = map.entrySet();
		for (Entry<String,String> entry : entries) {
		    this.parameters.put(entry.getKey(), new Attribute(entry.getValue()));
		}
	}
	
	public Map<String,Attribute> getParameters() {
	    return this.parameters;
	}
	
}

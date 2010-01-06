/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

public class CalendarDefinitionForm {
	
	private Long id = new Long(-1);
	
	@NotBlank
	private String className;
	
	@NotBlank
	private String name;
	
	private Set<String> role = new HashSet<String>();
	private List<String> parameterName = new ArrayList<String>();
	private List<String> parameterValue = new ArrayList<String>();

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

	public Set<String> getRole() {
		return role;
	}

	public void setRole(Set<String> role) {
		this.role = role;
	}
	
	public List<String> getParameterName() {
		return parameterName;
	}

	public void setParameterName(List<String> parameterName) {
		this.parameterName = parameterName;
	}

	public List<String> getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(List<String> parameterValue) {
		this.parameterValue = parameterValue;
	}

	public void addParameter(Entry<String,String> entry) {
		this.parameterName.add(entry.getKey());
		this.parameterValue.add(entry.getValue());
	}
	
	public void addParameters(Map<String,String> map) {
		Set<Entry<String,String>> entries = map.entrySet();
		for (Entry<String,String> entry : entries) {
			this.addParameter(entry);
		}
	}
	
	public Map<String,String> getParameters() {
		
		// create a new map to hold our parameters in
		Map<String,String> map = new HashMap<String,String>();

		// add each parameter to the map
		int pos = 0;
		for (String key : this.parameterName) {
			map.put(key, this.parameterValue.get(pos));
			pos++;
		}
		
		return map;
		
	}
	
}

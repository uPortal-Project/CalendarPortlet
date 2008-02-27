/*
 * Created on Feb 13, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar.mvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class CalendarDefinitionForm {
	
	private Long id = new Long(-1);
	private String className;
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
		for (Entry entry : entries) {
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

/*
 * CalendarDefinitionForm.java
 * 
 * Copyright (c) Feb 13, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
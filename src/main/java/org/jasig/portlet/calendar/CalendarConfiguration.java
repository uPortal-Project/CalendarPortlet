/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar;

/**
 * CalendarConfiguration represents a user-specific registration and configuration 
 * for a CalendarDefinition.
 *
 * @author Jen Bourey
 */
public class CalendarConfiguration {
	
	private Long id = new Long(-1);
	private CalendarDefinition calendarDefinition;
	private boolean displayed = true;
	private String subscribeId;
	
	/**
	 * Determine whether this calendar should be displayed or hidden.
	 * 
	 * @return
	 */
	public boolean isDisplayed() {
		return displayed;
	}
	
	/**
	 * Set whether this calendar should be displayed or hidden.
	 * 
	 * @param displayed
	 */
	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}
	
	/**
	 * Get the unique ID for this portlet subscription.
	 * 
	 * @return
	 */
	public String getSubscribeId() {
		return subscribeId;
	}
	
	/**
	 * Set the unique ID for this portlet subscription.
	 * 
	 * @param subscribeId
	 */
	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}
	
	/**
	 * Get the unique ID for this CalendarConfiguration.
	 * 
	 * @return
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * Set the unique ID for this CalendarConfiguration.
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Get the CalendarDefinition for the calendar being configured.
	 * 
	 * @return
	 */
	public CalendarDefinition getCalendarDefinition() {
		return calendarDefinition;
	}
	
	/**
	 * Set the CalendarDefinition for the calendar being configured.
	 * 
	 * @param definition
	 */
	public void setCalendarDefinition(CalendarDefinition definition) {
		this.calendarDefinition = definition;
	}
	
}

/*
 * CalendarConfiguration.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
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
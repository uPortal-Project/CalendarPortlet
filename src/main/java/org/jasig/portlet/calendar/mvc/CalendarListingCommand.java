/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc;

public class CalendarListingCommand {
	
	private Long id;
	private String subscribeId;
	private String name;
	private String url;
	private boolean displayed;
	
	
	public CalendarListingCommand() {
		id = new Long(-1);
		displayed = true;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSubscribeId() {
		return subscribeId;
	}
	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isDisplayed() {
		return displayed;
	}
	public void setDisplayed(boolean displayed) {
		this.displayed = displayed;
	}

}

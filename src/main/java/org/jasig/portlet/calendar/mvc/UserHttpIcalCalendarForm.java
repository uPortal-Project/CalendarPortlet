/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.mvc;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

/**
 * Form bean for adding new user-defined http-based iCalendar feeds.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class UserHttpIcalCalendarForm {
	
	private Long id;
	
	private String subscribeId;
	
	@NotBlank
	private String name;
	
	// RegEx copied from commons validation library
	@NotBlank
	@RegExp("^http[s]?:(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?")
	private String url;
	
	private boolean displayed;
	
	public UserHttpIcalCalendarForm() {
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

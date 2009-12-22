/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package org.jasig.portlet.calendar.service;

import javax.portlet.PortletRequest;

/**
 * IInitializationService defines an interface for performing actions when a 
 * new portlet session is created.
 *
 * @author Jen Bourey
 */
public interface IInitializationService {
	
	/**
	 * Perform some action.
	 * 
	 * @param request user's portlet request
	 */
	public void initialize(PortletRequest request);

}

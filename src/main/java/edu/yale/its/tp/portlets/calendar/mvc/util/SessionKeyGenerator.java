/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package edu.yale.its.tp.portlets.calendar.mvc.util;

import javax.portlet.PortletSession;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface SessionKeyGenerator {
    /**
     * Returns a random, unique session key for the user's session
     */
    public String getNextSessionKey(PortletSession session);
}

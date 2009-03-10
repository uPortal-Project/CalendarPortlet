/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package edu.yale.its.tp.portlets.calendar.mvc.util;

import java.util.Random;

import javax.portlet.PortletSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DefaultSessionKeyGenerator implements SessionKeyGenerator {
    public static final String UNIQUE_KEY_SEQ = DefaultSessionKeyGenerator.class.getName() + ".UNIQUE_KEY_SEQ";
    
    private final Random random = new Random(System.currentTimeMillis());
    
    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    /* (non-Javadoc)
     * @see org.jasig.portlet.notepad.util.SessionKeyGenerator#getNextSessionKey(javax.portlet.PortletSession)
     */
    public String getNextSessionKey(PortletSession session) {
        final Long nextId;
        synchronized (session) {
            final Long lastId = (Long)session.getAttribute(UNIQUE_KEY_SEQ);
            nextId = lastId != null ? lastId + 1 : 0;
            session.setAttribute(UNIQUE_KEY_SEQ, nextId);
        }
        
        //Generate random part of key
        final byte[] randomBytes = new byte[15];
        this.random.nextBytes(randomBytes);

        //Generate sequence id part of key
        final byte[] sequenceBytes = Long.toString(nextId, 36).getBytes();
        final byte[] keyBytes = ArrayUtils.addAll(randomBytes, sequenceBytes);
        
        //Base64 encode for readability
        final byte[] encodedKey = Base64.encodeBase64(keyBytes);
        return new String(encodedKey);
    }
}

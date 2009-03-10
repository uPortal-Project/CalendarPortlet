/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package edu.yale.its.tp.portlets.calendar.mvc;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.portlet.mvc.AbstractController;

import edu.yale.its.tp.portlets.calendar.mvc.servlet.AjaxResponseController;
import edu.yale.its.tp.portlets.calendar.mvc.util.SessionKeyGenerator;

/**
 * Base class for AJAX portlet callbacks that handles passing the model to the ajax response
 * servlet via a random session attribute.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractAjaxController extends AbstractController {
    private SessionKeyGenerator sessionKeyGenerator;
    private String ajaxServletName = "ajaxResponse";
    
    /**
     * @return the sessionKeyGenerator
     */
    public SessionKeyGenerator getSessionKeyGenerator() {
        return this.sessionKeyGenerator;
    }
    /**
     * @param sessionKeyGenerator the sessionKeyGenerator to set
     */
    @Required
    public void setSessionKeyGenerator(SessionKeyGenerator sessionKeyGenerator) {
        Validate.notNull(sessionKeyGenerator);
        this.sessionKeyGenerator = sessionKeyGenerator;
    }
    /**
     * @return the ajaxServletName
     */
    public String getAjaxServletName() {
        return this.ajaxServletName;
    }
    /**
     * @param ajaxServletName the ajaxServletName to set
     */
    public void setAjaxServletName(String ajaxServletName) {
        Validate.notNull(ajaxServletName);
        this.ajaxServletName = ajaxServletName;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.portlet.mvc.AbstractController#handleActionRequestInternal(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    @Override
    protected final void handleActionRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
        Map model;
        try {
            model = this.handleAjaxRequestInternal(request, response);
        }
        catch (Exception e) {
            model = new HashMap<String, String>();
            this.logger.warn("An exception occurred during handleAjaxRequestInternal()", e);
            model.put("AJAX_HANDLER_ERROR", e.getMessage());
        }

        //Add the response object to the session with the generated session key
        final PortletSession session = request.getPortletSession();
        final String sessionKey = this.sessionKeyGenerator.getNextSessionKey(session);
        session.setAttribute(sessionKey, model, PortletSession.APPLICATION_SCOPE);
        
        //Generate the response servlet URL
        final String contextPath = request.getContextPath();
        
        final String characterEncoding = request.getCharacterEncoding();
        final String encodedKeyParam = URLEncoder.encode(AjaxResponseController.SESSION_KEY, characterEncoding);
        final String encodedSessionKey = URLEncoder.encode(sessionKey, characterEncoding);
        
        final String ajaxServletUrl = response.encodeURL(contextPath + "/" + this.ajaxServletName + "?" + encodedKeyParam + "=" + encodedSessionKey);
        response.sendRedirect(ajaxServletUrl);
    }

    /**
     * Called by {@link #handleActionRequestInternal(ActionRequest, ActionResponse)} to handle the ajax callback. The returned model will
     * be passed via the session and a redirect to the {@link AjaxResponseController}
     * 
     * @see AbstractAjaxController#handleActionRequestInternal(ActionRequest, ActionResponse)
     */
    protected abstract Map handleAjaxRequestInternal(ActionRequest request, ActionResponse response) throws Exception;
}

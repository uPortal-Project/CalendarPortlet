/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package edu.yale.its.tp.portlets.calendar.mvc.servlet;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AjaxResponseController extends AbstractController {
    public static final String SESSION_KEY = "key";
    
    private String viewName = "jsonView";
    public void setViewName(String viewName) {
    	this.viewName = viewName;
    }
    
    public AjaxResponseController() {
        this.setRequireSession(true);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String key = ServletRequestUtils.getRequiredStringParameter(request, SESSION_KEY);
        
        final HttpSession session = request.getSession(false);
        final Map<?, ?> model = (Map<?, ?>)session.getAttribute(key);
        session.removeAttribute(key);
        
        if (model == null) {
            //TODO send 403 in this case
            throw new ServletException("No model exists in the session for key '" + key + "'");
        }

        return new ModelAndView(viewName, model);
    }
}

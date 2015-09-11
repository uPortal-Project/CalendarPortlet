/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.calendar.mvc;

import org.jasig.portlet.calendar.service.IInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.ModelAndView;

import javax.portlet.*;
import java.util.List;


/**
 * {code PortletFilter} to run initialization code when a user session is first created.
 *
 * @author Benito J. Gonzalez <bgonzalez2@unicon.net>
 * @since 3.0.1
 */
public class UserSessionInitializer implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private List<IInitializationService> initializationServices;

    public void setInitializationServices(List<IInitializationService> initializationServices) {
        this.initializationServices = initializationServices;
    }

    private void callInitializers(PortletRequest request) {
        PortletSession session = request.getPortletSession(true);
        if (session.getAttribute("initialized") == null) {
            log.info("initializing session for {}", request.getRemoteUser());
            for (IInitializationService service : initializationServices) {
                try {
                    log.info("calling initialize(request) on {}", service.getClass().toString());
                    service.initialize(request);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Issue with initialize call in filter: ", service.getClass());
                }
            }
        }

    }

    @Override
    public boolean preHandleAction(ActionRequest actionRequest, ActionResponse actionResponse, Object o) throws Exception {
        callInitializers(actionRequest);
        return true;
    }

    @Override
    public void afterActionCompletion(ActionRequest actionRequest, ActionResponse actionResponse, Object o, Exception e) throws Exception {

    }

    @Override
    public boolean preHandleRender(RenderRequest renderRequest, RenderResponse renderResponse, Object o) throws Exception {
        callInitializers(renderRequest);
        return true;
    }

    @Override
    public void postHandleRender(RenderRequest renderRequest, RenderResponse renderResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterRenderCompletion(RenderRequest renderRequest, RenderResponse renderResponse, Object o, Exception e) throws Exception {

    }

    @Override
    public boolean preHandleResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse, Object o) throws Exception {
        callInitializers(resourceRequest);
        return true;
    }

    @Override
    public void postHandleResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterResourceCompletion(ResourceRequest resourceRequest, ResourceResponse resourceResponse, Object o, Exception e) throws Exception {

    }

    @Override
    public boolean preHandleEvent(EventRequest eventRequest, EventResponse eventResponse, Object o) throws Exception {
        callInitializers(eventRequest);
        return true;
    }

    @Override
    public void afterEventCompletion(EventRequest eventRequest, EventResponse eventResponse, Object o, Exception e) throws Exception {

    }
}

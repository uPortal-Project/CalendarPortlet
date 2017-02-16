/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.calendar.util;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ViewRendererServlet;
import org.springframework.web.servlet.view.AbstractView;

public class MockView extends AbstractView {

  /** Default content type. Copied from AbstractView. */
  private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

  private Map<String, Object> model;

  @Override
  public String getContentType() {
    return DEFAULT_CONTENT_TYPE;
  }

  @Override
  protected void renderMergedOutputModel(
      Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    this.model = model;

    System.out.println(request.getAttribute(ViewRendererServlet.MODEL_ATTRIBUTE));
  }

  public Map<String, Object> getModel() {
    if (model == null) {
      return new HashMap<String, Object>();
    }
    return model;
  }
}

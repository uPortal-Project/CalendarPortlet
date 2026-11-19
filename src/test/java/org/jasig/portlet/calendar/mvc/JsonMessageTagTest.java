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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class JsonMessageTagTest {

  private GenericWebApplicationContext context;

  @Before
  public void setUp() {
    // The portlet's own message bundles, as applicationContext.xml loads them.
    ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
    messages.setBasename("i18n.messages");
    messages.setDefaultEncoding("UTF-8");

    context = new GenericWebApplicationContext();
    context.setServletContext(new MockServletContext());
    context.getBeanFactory().registerSingleton("messageSource", messages);
    context.refresh();
  }

  @Test
  public void frenchLabelWithAnApostropheIsValidJson() throws Exception {
    String json = "{\"viewMoreEvents\": \"" + render("view.more.events", Locale.FRENCH) + "\"}";

    Map<?, ?> labels = new ObjectMapper().readValue(json, Map.class);

    assertEquals("Voir plus d'évènements", labels.get("viewMoreEvents"));
  }

  @Test
  public void escapedTextReadsBackUnchanged() throws Exception {
    String text = "l'été \"quoted\" back\\slash </script> <!-- tab\t line\n \u000B";

    String json = "\"" + JsonMessageTag.escape(text) + "\"";

    assertEquals(text, new ObjectMapper().readValue(json, String.class));
    assertFalse(json.contains("<"));
    assertFalse(json.contains(">"));
  }

  private String render(String code, Locale locale) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(context.getServletContext());
    request.addPreferredLocale(locale);
    request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
    MockHttpServletResponse response = new MockHttpServletResponse();

    JsonMessageTag tag = new JsonMessageTag();
    tag.setPageContext(new MockPageContext(context.getServletContext(), request, response));
    tag.setCode(code);
    tag.doStartTag();
    tag.doEndTag();

    return response.getContentAsString();
  }
}

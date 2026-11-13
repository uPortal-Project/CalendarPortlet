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

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import java.io.IOException;
import org.springframework.web.servlet.tags.MessageTag;

/**
 * A <code>spring:message</code> for text that goes into a JSON string in a <code>
 * &lt;script type="application/json"&gt;</code> element.
 *
 * <p><code>spring:message</code> with <code>javaScriptEscape="true"</code> doesn't fit there: it
 * writes an apostrophe as <code>\'</code>, which JSON doesn't allow.
 */
public class JsonMessageTag extends MessageTag {

  private static final long serialVersionUID = 1L;

  @Override
  protected void writeMessage(String msg) throws IOException {
    pageContext.getOut().write(escape(msg));
  }

  /**
   * Escapes text for a JSON string. It also escapes <code>&lt;</code> and <code>&gt;</code>, so
   * the text can't end the script element.
   */
  static String escape(String text) {
    return new String(JsonStringEncoder.getInstance().quoteAsString(text))
        .replace("<", "\\u003C")
        .replace(">", "\\u003E");
  }
}

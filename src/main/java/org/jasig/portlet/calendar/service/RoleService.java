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
package org.jasig.portlet.calendar.service;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Reads security roles (effectively uPortal groups) from the portlet.xml file.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class RoleService implements IRoleService, ServletContextAware {
  private static final String PORTLET_XML_PATH = "/WEB-INF/portlet.xml";
  private static final String ROLES_XPATH = "//security-role-ref/role-link/text()";

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private ServletContext context;
  private Set<String> cachedRoles;

  public void setServletContext(ServletContext context) {
    this.context = context;
  }

  /**
   * Get the list of known roles from the portlet.xml file. Will cache the role list after the first
   * lookup.
   *
   * @return the list of role names.
   */
  @Override
  public synchronized Set<String> getKnownRoles() {
    if (cachedRoles != null) {
      return cachedRoles;
    }

    cachedRoles = readRolesFromPortletXml();
    return cachedRoles;
  }

  /**
   * Do the real work of reading the role list.
   *
   * @return the set of role names.
   */
  private Set<String> readRolesFromPortletXml() {
    try {
      URL portletXmlUrl = context.getResource(PORTLET_XML_PATH);
      InputSource is = new InputSource(portletXmlUrl.openStream());

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      Document doc = builder.parse(is);

      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      XPathExpression xPathExpression = xpath.compile(ROLES_XPATH);

      NodeList nodeList = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);

      Set<String> roles = new LinkedHashSet<String>();
      for (int i = 0; i < nodeList.getLength(); i++) {
        String role = nodeList.item(i).getNodeValue();
        roles.add(role);
      }

      return roles;

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException("Error reading roles from portlet.xml", e);
    }
  }
}

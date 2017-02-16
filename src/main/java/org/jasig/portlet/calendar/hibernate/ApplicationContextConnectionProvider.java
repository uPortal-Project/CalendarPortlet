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
package org.jasig.portlet.calendar.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;
import org.jasig.portlet.calendar.spring.PortletApplicationContextLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * When the hibernate3-maven-plugin:hbm2ddl goal is executed, this class provides connections from
 * the Spring ApplicationContext, which is capable of using encrypted database connection settings
 * (in datasource.properties).
 *
 * @author drewwills
 */
public class ApplicationContextConnectionProvider implements ConnectionProvider {

  private static final String DATA_SOURCE_BEAN_NAME = "dataSource";

  private ApplicationContext context;

  private final Logger logger = Logger.getLogger(getClass());

  @Override
  public void close() throws HibernateException {
    if (context != null) {
      ((ConfigurableApplicationContext) context).close();
    }
  }

  @Override
  public void closeConnection(Connection conn) throws SQLException {
    conn.close();
  }

  @Override
  public void configure(Properties props) throws HibernateException {
    /*
     * Configuration is handled by the ApplicationContext itself;  there is
     * nothing to do here.
     */
  }

  @Override
  public Connection getConnection() throws SQLException {

    if (context == null) {
      init();
    }

    final DataSource dataSource = context.getBean(DATA_SOURCE_BEAN_NAME, DataSource.class);
    final Connection rslt = dataSource.getConnection();
    logger.info("Providing the following connection to hbm2ddl:  " + rslt);
    return rslt;
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false; // WTF?
  }

  /*
   * Implementation
   */

  private synchronized void init() {

    if (context != null) {
      // Already done...
      return;
    }

    try {
      context =
          PortletApplicationContextLocator.getApplicationContext(
              PortletApplicationContextLocator.DATABASE_CONTEXT_LOCATION);
    } catch (Exception e) {
      logger.error(
          "Unable to load the application context from "
              + PortletApplicationContextLocator.DATABASE_CONTEXT_LOCATION,
          e);
    }
  }
}

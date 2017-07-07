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
package org.jasig.portlet.calendar.spring;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

public class StringEncryptorFactoryBean implements FactoryBean<StringEncryptor> {

  public static final String JAYSYPT_ENCRYPTION_KEY_VARIABLE = "UP_JASYPT_KEY";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public StringEncryptor getObject() throws Exception {

    final StandardPBEStringEncryptor rslt = new StandardPBEStringEncryptor();

    /*
     * If properties file encryption is used in this deployment, the
     * encryption key will be made available to the application as an
     * environment variable called UP_JASYPT_KEY.
     */
    final String encryptionKey = System.getenv(JAYSYPT_ENCRYPTION_KEY_VARIABLE);

    if (encryptionKey != null) {

      logger.info("Jasypt support for encrypted property values ENABLED");

      rslt.setPassword(encryptionKey);

    } else {

      logger.info(
          "Jasypt support for encrypted property values DISABLED;  "
              + "specify environment variable {} to use this feature",
          JAYSYPT_ENCRYPTION_KEY_VARIABLE);

      /*
       * According to the API docs, not setting a password (on the StandardPBEStringEncryptor)
       * will result in an EncryptionInitializationException being thrown during initialization,
       * which will occur on the first call to decrypt(...);
       */

    }

    return rslt;
  }

  @Override
  public Class<StringEncryptor> getObjectType() {
    return StringEncryptor.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}

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
package org.jasig.portlet.calendar.adapter;

import static org.junit.Assert.assertTrue;

import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.util.MockWebApplication;
import org.jasig.portlet.calendar.util.MockWebApplicationContextLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/testApplicationContext.xml"},
  loader = MockWebApplicationContextLoader.class
)
@MockWebApplication(name = "CalendarPortlet")
public class ExchangeCalendarAdapterIntegrationTest {

  @Autowired private ExchangeCalendarAdapter testee;

  @Test
  public void testCacheGeneratorUserPeriodIsTrue() {
    DefaultCacheKeyGeneratorImpl testeeCacheGenerator =
        (DefaultCacheKeyGeneratorImpl) ReflectionTestUtils.getField(testee, "cacheKeyGenerator");
    assertTrue(testeeCacheGenerator.isIncludePeriod());
  }
}

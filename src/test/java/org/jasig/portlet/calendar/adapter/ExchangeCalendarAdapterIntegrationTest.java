package org.jasig.portlet.calendar.adapter;

import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.util.MockWebApplication;
import org.jasig.portlet.calendar.util.MockWebApplicationContextLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/testApplicationContext.xml" },loader=MockWebApplicationContextLoader.class)
@MockWebApplication(name="CalendarPortlet")
public class ExchangeCalendarAdapterIntegrationTest {
	
	@Autowired
	private ExchangeCalendarAdapter testee;
	
	@Test
	public void testCacheGeneratorUserPeriodIsTrue(){
		DefaultCacheKeyGeneratorImpl testeeCacheGenerator =  
				(DefaultCacheKeyGeneratorImpl) ReflectionTestUtils.getField(testee, "cacheKeyGenerator");
		assertTrue(testeeCacheGenerator.isIncludePeriod());
	}
}

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
package org.jasig.portlet.calendar;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CalendarConfigurationByNameComparatorTest {

    @Test
    public void testComparator() {
        CalendarConfiguration config1 = new CalendarConfiguration();
        CalendarDefinition def1 = new CalendarDefinition();
        def1.setName("First Calendar");
        config1.setCalendarDefinition(def1);
        
        CalendarConfiguration config2 = new CalendarConfiguration();
        CalendarDefinition def2 = new CalendarDefinition();
        def2.setName("Second Calendar");
        config2.setCalendarDefinition(def2);

        List<CalendarConfiguration> list = new ArrayList<CalendarConfiguration>();
        list.add(config2);
        list.add(config1);
        Collections.sort(list, new CalendarConfigurationByNameComparator());
        
        assertEquals("First Calendar", list.get(0).getCalendarDefinition().getName());
    }
    
}

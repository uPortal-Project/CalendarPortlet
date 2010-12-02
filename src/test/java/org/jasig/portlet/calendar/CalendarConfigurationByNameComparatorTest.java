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

package org.jasig.portlet.calendar;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CalendarConfigurationByNameComparator implements Comparator<CalendarConfiguration> {

    public int compare(CalendarConfiguration left, CalendarConfiguration right) {
        return new CompareToBuilder().append(
                left.getCalendarDefinition().getName(),
                right.getCalendarDefinition().getName()).toComparison();
    }

}

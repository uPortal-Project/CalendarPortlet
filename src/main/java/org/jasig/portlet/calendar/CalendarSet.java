package org.jasig.portlet.calendar;

import java.util.Set;

/**
 * CalendarSet represents a collection of calendar configurations.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CalendarSet<T extends CalendarConfiguration> {
    
    private Set<T> configurations;

    public Set<T> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Set<T> configurations) {
        this.configurations = configurations;
    }
    
}

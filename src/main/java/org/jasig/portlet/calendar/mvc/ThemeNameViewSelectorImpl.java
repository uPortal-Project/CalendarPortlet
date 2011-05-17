package org.jasig.portlet.calendar.mvc;

import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

/**
 * ThemeNameViewSelectorImpl determines appropriate views by examining a "themeName"
 * portlet request property and comparing it to known mobile theme names.  This
 * implementation allows the portlet to delegate user agent inspection to the 
 * portal and also accounts for a potential user choice to use a portal version 
 * that does not match the automatic assignment.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class ThemeNameViewSelectorImpl implements IViewSelector {
    
    private final String CALENDAR_WIDE_VIEW = "calendarWideView";
    private final String CALENDAR_NARROW_VIEW = "calendarNarrowView";
    private final String CALENDAR_MOBILE_VIEW = "calendarMobileView";

    protected static final String THEME_NAME_PROPERTY = "themeName";
    protected static final String MOBILE_THEMES_KEY = "mobileThemes";
    protected static final String[] MOBILE_THEMES_DEFAULT = new String[]{ "UniversalityMobile" };

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.calendar.mvc.IViewSelector#getCalendarViewName(javax.portlet.PortletRequest)
     */
    public String getCalendarViewName(PortletRequest request) {
        if (isMobile(request)) {
            return CALENDAR_MOBILE_VIEW;
        }
        
        // otherwise check the portlet window state
        WindowState state = request.getWindowState();
        if (WindowState.MAXIMIZED.equals(state)) {
            return CALENDAR_WIDE_VIEW;
        } else {
            return CALENDAR_NARROW_VIEW;
        }
        
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.calendar.mvc.IViewSelector#getEventListViewName(javax.portlet.PortletRequest)
     */
    public String getEventListViewName(PortletRequest request) {
        return "ajaxEventList";
    }

    protected boolean isMobile(PortletRequest request) {
        String[] mobileThemes = request.getPreferences().getValues(MOBILE_THEMES_KEY, MOBILE_THEMES_DEFAULT);
        String themeName = request.getProperty(THEME_NAME_PROPERTY);
        if (themeName == null) {
            return false;
        }
        
        for (String theme : mobileThemes) {
            if (themeName.equals(theme)) {
                return true;
            }
        }
        
        return false;
    }

}

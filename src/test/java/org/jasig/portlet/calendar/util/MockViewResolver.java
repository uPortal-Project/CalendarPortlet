package org.jasig.portlet.calendar.util;

import java.util.Locale;
import java.util.Map;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

public class MockViewResolver implements ViewResolver {

	private String viewName;
	private MockView view;

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		this.viewName = viewName;
		this.view = new MockView();
		return view;
	}

	public String getViewName() {
		return viewName;
	}

	public Map<String, Object> getModel() {
		return view.getModel();
	}
}

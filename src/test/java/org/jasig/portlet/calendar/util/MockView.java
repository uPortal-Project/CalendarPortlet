package org.jasig.portlet.calendar.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ViewRendererServlet;
import org.springframework.web.servlet.view.AbstractView;

public class MockView extends AbstractView {

	/** Default content type. Copied from AbstractView. */
	private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

	private Map<String, Object> model;

	@Override
	public String getContentType() {
		return DEFAULT_CONTENT_TYPE;
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		this.model = model;

		System.out.println(request.getAttribute(ViewRendererServlet.MODEL_ATTRIBUTE));
	}

	public Map<String, Object> getModel() {
		if (model == null) {
			return new HashMap<String, Object>();
		}
		return model;
	}

}

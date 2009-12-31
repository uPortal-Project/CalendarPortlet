package org.jasig.portlet.calendar.mvc.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/ajax/jspView")
public class AjaxJspViewController {

	private Log log = LogFactory.getLog(AjaxJspViewController.class);

	private AjaxPortletSupportService ajaxPortletSupportService;

	@Autowired(required = true)
	public void setAjaxPortletSupportService(
			AjaxPortletSupportService ajaxPortletSupportService) {
		this.ajaxPortletSupportService = ajaxPortletSupportService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView get(HttpServletRequest request,
			HttpServletResponse response) {

		try {

			// get the ModelAndView object associated with this AJAX request
			Map<Object, Object> model = ajaxPortletSupportService.getAjaxModel(
					request, response);
			String view = (String) model.get("viewName");
			assert view != null;

			return new ModelAndView(view, model);

		} catch (Exception e) {
			log.error("Exception occurred while performing AJAX response", e);
			// TODO: create consistent error behavior
			return new ModelAndView();
		}
	}

}

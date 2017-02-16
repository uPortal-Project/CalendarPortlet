/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.calendar.mvc.controller;

import java.util.Map;
import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import org.jasig.portlet.calendar.PredefinedCalendarDefinition;
import org.jasig.portlet.calendar.adapter.ICalendarAdapter;
import org.jasig.portlet.calendar.dao.CalendarStore;
import org.jasig.portlet.calendar.mvc.CalendarDefinitionForm;
import org.jasig.portlet.calendar.service.IRoleService;
import org.jasig.portlet.form.parameter.Parameter;
import org.jasig.portlet.form.parameter.SingleValuedParameterInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.portlet.bind.annotation.ActionMapping;

/**
 * EditCalendarDefinitionController provides a GUI for adding and editing predefined calendars.
 *
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
public class EditCalendarDefinitionController {

  private static final String FORM_NAME = "calendarDefinitionForm";

  private CalendarStore calendarStore;
  private IRoleService roleService;

  @Required
  @Resource(name = "calendarStore")
  public void setCalendarStore(CalendarStore calendarStore) {
    this.calendarStore = calendarStore;
  }

  @Autowired
  public void setRoleService(final IRoleService roleService) {
    this.roleService = roleService;
  }

  private ApplicationContext context;

  @Autowired(required = true)
  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  @Autowired private Map<String, ICalendarAdapter> adapters;

  @RequestMapping(params = "action=createCalendarDefinition")
  public String showNewCalendarDefinitionForm(PortletRequest request, Model model) {
    if (!model.containsAttribute(FORM_NAME)) {
      final CalendarDefinitionForm form = new CalendarDefinitionForm();
      model.addAttribute(FORM_NAME, form);
    }
    model.addAttribute("adapters", adapters);
    return "/createCalendarDefinition";
  }

  @RequestMapping(params = "action=createCalendarDefinition2")
  public String showEditCalendarDefinitionForm2(
      PortletRequest request, Model model, @ModelAttribute(FORM_NAME) CalendarDefinitionForm form) {
    ICalendarAdapter adapter = context.getBean(form.getClassName(), ICalendarAdapter.class);
    for (Parameter pref : adapter.getParameters()) {
      SingleValuedParameterInput input = (SingleValuedParameterInput) pref.getInput();
      form.addParameter(pref.getName(), input.getDefaultValue());
    }
    model.addAttribute("adapter", context.getBean(form.getClassName()));
    model.addAttribute("availableRoles", roleService.getKnownRoles());
    return "/editCalendarDefinition";
  }

  @RequestMapping(params = "action=editCalendarDefinition")
  public String showEditCalendarDefinitionForm(PortletRequest request, Model model) {
    CalendarDefinitionForm form = (CalendarDefinitionForm) model.asMap().get(FORM_NAME);
    if (form == null) {
      form = getCalendarDefinitionForm(request);
      model.addAttribute(FORM_NAME, form);
    }
    model.addAttribute("adapter", context.getBean(form.getClassName(), ICalendarAdapter.class));
    model.addAttribute("availableRoles", roleService.getKnownRoles());
    return "/editCalendarDefinition";
  }

  @ActionMapping(params = "action=editCalendarDefinition")
  public void updateCalendarDefinition(
      ActionRequest request,
      ActionResponse response,
      @ModelAttribute(FORM_NAME) CalendarDefinitionForm form,
      BindingResult result,
      SessionStatus status) {

    // construct a calendar definition from the form data
    PredefinedCalendarDefinition definition = null;

    // If an id was submitted, retrieve the calendar definition we're
    // trying to edit.  Otherwise, create a new definition.
    if (form.getId() > -1) definition = calendarStore.getPredefinedCalendarDefinition(form.getId());
    else definition = new PredefinedCalendarDefinition();

    // set the calendar definition properties based on the
    // submitted form
    definition.setClassName(form.getClassName());
    definition.setDefaultRoles(form.getRole());
    definition.setName(form.getName());
    definition.setFname(form.getFname());

    ICalendarAdapter adapter = context.getBean(definition.getClassName(), ICalendarAdapter.class);
    for (Parameter pref : adapter.getParameters()) {
      definition
          .getParameters()
          .put(pref.getName(), form.getParameters().get(pref.getName()).getValue());
    }

    // save the calendar definition
    calendarStore.storeCalendarDefinition(definition);

    // send the user back to the main administration page
    response.setRenderParameter("action", "administration");
  }

  protected CalendarDefinitionForm getCalendarDefinitionForm(PortletRequest request) {
    // if we're editing a calendar, retrieve the calendar definition from
    // the database and add the information to the form
    String id = request.getParameter("id");
    if (id != null && !id.equals("")) {
      Long definitionId = Long.parseLong(id);
      if (definitionId > -1) {
        PredefinedCalendarDefinition definition =
            calendarStore.getPredefinedCalendarDefinition(definitionId);
        CalendarDefinitionForm command = new CalendarDefinitionForm();
        command.setId(definition.getId());
        command.setName(definition.getName());
        command.setFname(definition.getFname());
        command.setClassName(definition.getClassName());
        command.setRole(definition.getDefaultRoles());
        command.addParameters(definition.getParameters());
        ICalendarAdapter adapter =
            context.getBean(definition.getClassName(), ICalendarAdapter.class);
        for (Parameter pref : adapter.getParameters()) {
          SingleValuedParameterInput input = (SingleValuedParameterInput) pref.getInput();
          String value;
          if (definition.getParameters().containsKey(pref.getName())) {
            value = definition.getParameters().get(pref.getName());
          } else {
            value = input.getDefaultValue();
          }
          command.addParameter(pref.getName(), value);
        }
        return command;
      } else {
        final CalendarDefinitionForm form = new CalendarDefinitionForm();
        ICalendarAdapter adapter = adapters.get(0);
        for (Parameter pref : adapter.getParameters()) {
          SingleValuedParameterInput input = (SingleValuedParameterInput) pref.getInput();
          form.addParameter(pref.getName(), input.getDefaultValue());
        }
        return form;
      }

    } else {
      // otherwise, construct a brand new form
      // create the form
      final CalendarDefinitionForm form = new CalendarDefinitionForm();
      ICalendarAdapter adapter = adapters.get(0);
      for (Parameter pref : adapter.getParameters()) {
        SingleValuedParameterInput input = (SingleValuedParameterInput) pref.getInput();
        form.addParameter(pref.getName(), input.getDefaultValue());
      }
      return form;
    }
  }
}

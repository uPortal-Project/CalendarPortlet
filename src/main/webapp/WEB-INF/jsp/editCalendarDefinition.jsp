<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<jsp:directive.include file="/WEB-INF/jsp/css.jsp"/>

    <script type="text/javascript">
		function addRole(id) {
			var div = document.getElementById(id);
			var container = document.createElement('div');
			container.style.padding = "5px";
			var input = document.createElement('input');
			input.name = 'role';
			input.type = 'text';
			input.size = '20';
			container.appendChild(input);
			var remove = document.createElement('a');
			remove.href = 'javascript:;';
			remove.onclick = function(){removeRole(this)};
			remove.appendChild(document.createTextNode(' '));
			var img = document.createElement('img');
			img.src = '<rs:resourceURL value="/rs/famfamfam/silk/1.3/delete.png"/>';
			img.style.verticalAlign = 'middle';
			remove.appendChild(img);
			container.appendChild(remove);
			div.appendChild(container);
		}
		
		function removeRole(link) {
			var div = link.parentNode;
			div.parentNode.removeChild(div);
		}

		function addParameter(id) {
			var div = document.getElementById(id);
			var container = document.createElement('div');
			container.style.padding = "5px";
			var input = document.createElement('input');
			input.name = 'parameterName';
			input.type = 'text';
			input.size = '20';
			container.appendChild(input);
			input = document.createElement('input');
			input.name = 'parameterValue';
			input.type = 'text';
			input.size = '20';
			container.appendChild(input);
			var remove = document.createElement('a');
			remove.href = 'javascript:;';
			remove.onclick = function(){removeRole(this)};
			remove.appendChild(document.createTextNode(' '));
			var img = document.createElement('img');
			img.src = '<rs:resourceURL value="/rs/famfamfam/silk/1.3/delete.png"/>';
			img.style.verticalAlign = 'middle';
			remove.appendChild(img);
			container.appendChild(remove);
			div.appendChild(container);
		}

		function removeParameter(link) {
			var div = link.parentNode;
			div.parentNode.removeChild(div);
		}
    </script>

<div class="">
    <h2><spring:message code="view.calendaredit.header"/></h2>

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editCalendarDefinition"/></portlet:actionURL>
    <form:form name="calendar" commandName="calendarDefinitionForm" action="${postUrl}">
    
        <spring:hasBindErrors name="calendarDefinitionForm">
            <div class="portlet-msg-error" role="alert">
                <form:errors path="*" element="div"/>
            </div> <!-- end: portlet-msg -->
        </spring:hasBindErrors>
    
       	<form:hidden path="id"/>
		<p>
			<label class="portlet-form-field-label">
                <spring:message code="form.calendar.name"/>:
			</label>
			<form:input path="name" size="50"/>
		</p>
        <p>
            <label class="portlet-form-field-label">
                <spring:message code="form.calendar.beanname"/>:
            </label>
            <form:input path="className" size="50"/>
		</p>
		<br/>
		<p id="<portlet:namespace/>role-list">
			<label class="portlet-form-field-label"><spring:message code="form.calendar.roles"/>:</label><br />
			<c:forEach items="${ calendarDefinitionForm.role }" var="role">
				<div style="padding-left: 5px;">
					<input name="role" type="text" value="${ role }" size="20"/>
					<a class="upcal-delete" href="javascript:;" onclick="removeRole(this)">
					   Delete
					</a>
				</div>
			</c:forEach>
			<a class="upcal-add" href="javascript:;" onclick="addRole('<portlet:namespace/>role-list')">
                <spring:message code="form.calendar.addrole"/>
            </a>
		</p>
		<div id="<portlet:namespace/>parameter-list">
			<label class="portlet-form-field-label"><spring:message code="form.calendar.parameters"/>:</label><br />
			<c:forEach items="${ calendarDefinitionForm.parameterName }" var="paramName" varStatus="status">
				<div>
					<input name="parameterName" type="text" value="${ paramName }" size="20"/>
					<input name="parameterValue" type="text" value="${ calendarDefinitionForm.parameterValue[status.index] }" size="20"/>
					<a class="upcal-delete" href="javascript:;" onclick="removeParameter(this)">
					   Delete
					</a>
				</div>
			</c:forEach>
			<a class="upcal-add" href="javascript:;" onclick="addParameter('<portlet:namespace/>parameter-list')">
				<spring:message code="form.calendar.addparameter"/>
            </a>
		</div>
        <p>
            <button type="submit" class="portlet-form-button">
                <spring:message code="form.calendar.save"/>
            </button>
        </p>
    </form:form>

    <div class="upcal-view-links">
        <portlet:renderURL var="returnUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
        <a class="upcal-view-return" href="${ returnUrl }" title="<spring:message code="return.to.administration.link.title"/>">
           <spring:message code="return.to.administration.link.text"/>
        </a>
    </div>
    
</div>
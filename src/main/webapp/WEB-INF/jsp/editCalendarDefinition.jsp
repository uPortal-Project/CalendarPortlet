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
    <h2>Edit Calendar</h2>

    <portlet:actionURL var="postUrl"><portlet:param name="action" value="editCalendarDefinition"/></portlet:actionURL>
    <form:form name="calendar" commandName="calendarDefinitionForm" action="${postUrl}">
    
        <spring:hasBindErrors name="calendarDefinitionForm">
            <div class="portlet-msg-error" role="alert">
                <form:errors path="*" element="div"/>
            </div> <!-- end: portlet-msg -->
        </spring:hasBindErrors>
    
       	<form:hidden path="id"/>
		<p>
			<label class="portlet-form-field-label">Calendar Display Name:</label>
			<form:input path="name" size="50"/>
		</p>
        <p>
            <label class="portlet-form-field-label">Context Bean Name:</label>
            <form:input path="className" size="50"/>
		</p>
		<br/>
		<p id="<portlet:namespace/>role-list">
			<label class="portlet-form-field-label">Calendar default roles:</label><br />
			<c:forEach items="${ calendarDefinitionForm.role }" var="role">
				<div style="padding-left: 5px;">
					<input name="role" type="text" value="${ role }" size="20"/>
					<a href="javascript:;" onclick="removeRole(this)">
						<img style="vertical-align: middle;" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/delete.png"/>"/>
					</a>
				</div>
			</c:forEach>
			<div style="padding: 5px;">
				<a href="javascript:;" onclick="addRole('<portlet:namespace/>role-list')">
					<img style="vertical-align: middle;" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/add.png"/>"/>
					add a role</a>
			</div>
		</p>
		<p id="<portlet:namespace/>parameter-list">
			<label class="portlet-form-field-label">Calendar parameters:</label><br />
			<c:forEach items="${ calendarDefinitionForm.parameterName }" var="paramName" varStatus="status">
				<div style="padding-left: 5px">
					<input name="parameterName" type="text" value="${ paramName }" size="20"/>
					<input name="parameterValue" type="text" value="${ calendarDefinitionForm.parameterValue[status.index] }" size="20"/>
					<a href="javascript:;" onclick="removeParameter(this)">
						<img style="vertical-align: middle;" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/delete.png"/>"/>
					</a>
				</div>
			</c:forEach>
			<div style="padding: 5px;">
				<a href="javascript:;" onclick="addParameter('<portlet:namespace/>parameter-list')">
					<img style="vertical-align: middle;" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/add.png"/>"/>
					add a parameter</a>
			</div>
		</p>
        <p>
            <button type="submit" class="portlet-form-button">Save calendar</button>
        </p>
    </form:form>

    <div class="upcal-view-links">
        <portlet:renderURL var="returnUrl"><portlet:param name="action" value="administration"/></portlet:renderURL>
        <a class="upcal-view-return" href="${ returnUrl }">
           Return to administration
        </a>
    </div>
    
</div>
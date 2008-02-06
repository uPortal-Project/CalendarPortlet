<%--
	- errors
	- 
	- Output a list of errors for the command or bean specified in the
	- 'path' attribute. The markup enclosing each error message can be
	- customized by editing this tag file directly.
	-
	- @param path the name of the field to bind to (required)
	- @param fields whether the individual fields should also be checked,
	-     specifically '${path}.*' (optional)
	--%>
<%@ tag dynamic-attributes="attributes" isELIgnored="false" body-content="empty" %>
<%@ include file="include.jsp" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="fields" required="false"%>
<spring:hasBindErrors name="${path}">
	<div style="color:#A00000">
		<p>Please correct the following errors:</p>
		<ul class="errors">
			<spring:bind path="${path}">
				<c:forEach items="${status.errorMessages}" var="error">
					<li><c:out value="${error}"/></li>
				</c:forEach>
			</spring:bind>
			<c:if test="${fields}">
				<spring:bind path="${path}.*">
					<c:forEach items="${status.errorMessages}" var="error">
						<li><c:out value="${error}"/></li>
					</c:forEach>
				</spring:bind>
			</c:if>
		<ul>
	</div>
</spring:hasBindErrors>
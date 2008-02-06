<%--
	- textarea
	- 
	- Display an html textarea and bind it to the attribute of a command or
	- bean. The var attribute specifies a variable that may be used to place
	- the value in the body of the tag.
	- Accepts dynamic attributes.
	-
	- @param path the name of the field to bind to (required)
	- @param clear set to "true" to override the value with a blank value.
	--%>
<%@ tag dynamic-attributes="attributes" isELIgnored="false" %>
<%@ include file="include.jsp" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="clear" %>
<spring:bind path="${path}">
	<html:attributes var="attrString" attributeMap="${attributes}" name="${status.expression}">
		<c:if test="${clear != \"true\"}">
			<jsp:doBody var="value" />
			<c:if test="${empty value}">
				<c:set var="value" value="${status.value}" />
			</c:if>
		</c:if>
		<textarea ${attrString}>${value}</textarea>
	</html:attributes>
	<span style="color:#A00000">${status.errorMessage}</span>
</spring:bind>
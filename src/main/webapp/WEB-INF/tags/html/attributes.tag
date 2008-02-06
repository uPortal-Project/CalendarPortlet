<%--
	- attributes
	-
	- Expose a string of HTML attributes from the given map of attributes.
	- Dynamic attributes specified will be added to the string if they do
	- not already exist in the map. This tag exposes a variable with the
	- name specified in the 'var' attribute.
	-
	- @param var the variable in which the string attributes will be exposed.
	-     (required)
	- @param attributeMap a map of attributes to convert to a string of
	-     name/value pairs.
	--%>
<%@ tag dynamic-attributes="attributes" isELIgnored="false" %>
<%@ include file="include.jsp" %>
<%@ attribute name="var" required="true" rtexprvalue="false" %>
<%@ attribute name="attributeMap" type="java.util.Map" %>
<%@ variable name-from-attribute="var" alias="attrString" declare="false" %>
<c:forEach var="attr" items="${attributeMap}">
	<c:set var="attrString">
	    <c:out escapeXml="false" value="${attrString} ${attr.key}=\""/><c:out value="${attr.value}"/><c:out escapeXml="false" value="\""/>
	</c:set>
</c:forEach>
<c:forEach var="attr" items="${attributes}">
	<c:if test="${empty attributeMap[attr.key]}">
		<c:set var="attrString">
	    	<c:out escapeXml="false" value="${attrString} ${attr.key}=\""/><c:out value="${attr.value}"/><c:out escapeXml="false" value="\""/>
		</c:set>
	</c:if>
</c:forEach>
<jsp:doBody />
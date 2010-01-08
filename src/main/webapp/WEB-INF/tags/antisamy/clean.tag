<%@ tag isELIgnored="false" dynamic-attributes="attributes" body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="value" required="true" %>
<%@ tag import="org.springframework.web.context.WebApplicationContext" %>
<%@ tag import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ tag import="org.jasig.portlet.calendar.util.StringCleaningUtil" %>
<% 
	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
	StringCleaningUtil util = (StringCleaningUtil) ctx.getBean("stringCleaningUtil"); 
%>
<%= util.getCleanString(value) %>

<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>
<link rel="stylesheet" href="<c:url value="/css/calendar.css"/>" type="text/css"></link>

<style type="text/css">
	.upcal-active { background:transparent url(<rs:resourceURL value="/rs/famfamfam/silk/1.3/tick.png"/>) 0 0.2em no-repeat; } 
	.upcal-inactive { background:transparent url(<c:url value="/images/tick_grey.png"/>) 0 0.2em no-repeat; }
	
	.upcal-add { background:transparent url(<rs:resourceURL value="/rs/famfamfam/silk/1.3/add.png"/>) center left no-repeat; }
	.upcal-edit { background:transparent url(<rs:resourceURL value="/rs/famfamfam/silk/1.3/page_edit.png"/>) center left no-repeat;}
	.upcal-delete { background:transparent url(<rs:resourceURL value="/rs/famfamfam/silk/1.3/cross.png"/>) 0 3px no-repeat; }
	
	.upcal-view-links .upcal-view-more { background:transparent url(<rs:resourceURL value="/rs/famfamfam/silk/1.3/arrow_right.png"/>) top right no-repeat; }
	.upcal-view-links .upcal-view-return { background:transparent url(<rs:resourceURL value="/rs/famfamfam/silk/1.3/arrow_left.png"/>) top left no-repeat; }
</style>
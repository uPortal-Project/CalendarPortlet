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
<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<rs:aggregatedResources path="skin${ mobile ? '-mobile' : '' }${ usePortalJsLibs ? '-shared' : '' }.xml"/>

<script type="text/javascript">
    <rs:compressJs>
        var ${n} = ${n} || {};
        <c:choose>
            <c:when test="${!usePortalJsLibs}">
                ${n}.jQuery = jQuery.noConflict(true);
                ${n}._ = _.noConflict();
                ${n}.Backbone = Backbone.noConflict();
            </c:when>
            <c:otherwise>
                <c:set var="ns"><c:if test="${ not empty portalJsNamespace }">${ portalJsNamespace }.</c:if></c:set>
                ${n}.jQuery = ${ ns }jQuery;
                ${n}._ = ${ ns }_;
                ${n}.Backbone = Backbone.noConflict();
            </c:otherwise>
        </c:choose>
        if (!upcal.initialized) upcal.init(${n}.jQuery, ${n}._, ${n}.Backbone);
        ${n}.upcal = upcal;
    </rs:compressJs>
</script>
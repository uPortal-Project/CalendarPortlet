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
<rs:aggregatedResources path="skin-shared.xml"/>

<script type="text/javascript">
    var ${n} = ${n} || {};
    ${n}.jQuery = up.jQuery;
    ${n}._ = up._;
    ${n}.Backbone = up.Backbone;
    // uPortal's respondr.xsl resets the shared up._.templateSettings to
    // Mustache-style sigils. Our Underscore templates use the standard
    // sigils, so each _.template() call must pass these settings
    // explicitly to avoid the raw template source leaking into the
    // rendered output. The view JSPs already use the ${"<%"}/${"%>"}
    // EL trick to emit literal Underscore sigils into the rendered HTML;
    // we use the same trick here so the regex literals survive JSP's
    // own scriptlet parser.
    ${n}.upcalTemplateSettings = {
        interpolate: /${"<%"}=([\s\S]+?)${"%>"}/g,
        evaluate:    /${"<%"}([\s\S]+?)${"%>"}/g,
        escape:      /${"<%"}-([\s\S]+?)${"%>"}/g
    };
    if (!upcal.initialized) upcal.init(${n}.jQuery, ${n}._, ${n}.Backbone);
    ${n}.upcal = upcal;
</script>

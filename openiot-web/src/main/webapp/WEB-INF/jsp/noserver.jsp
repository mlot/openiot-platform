<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<c:set var="sitewhere_title" value="Server Startup Failed" />
<c:set var="sitewhere_section" value="sites" />
<%@ include file="includes/top.inc"%>

<style>
</style>

<!-- Title Bar -->
<div class="sw-title-bar content k-header">
	<h1 data-i18n="noserver.title">
	</h1>
</div>
<div class="alert alert-error" data-i18n="noserver.loaderrorms">
</div>
<%@ include file="includes/bottom.inc"%>
<script type="text/javascript">
    /** Set sitewhere_title */
    sitewhere_i18next.sitewhere_title = "noserver.title";
</script>
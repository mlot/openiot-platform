<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Device group small template -->
<script type="text/x-kendo-tmpl" id="devicegroup-entry-small">
	<div class="asset-entry gradient-bg" id="group-#:token#">
		<div class="asset-entry-logowrapper">
			<img class="asset-entry-logo" src="${pageContext.request.contextPath}/img/devicegroup.png"/>
		</div>
		<div class="ellipsis"><b>#:name#</b></div>
		<div class="ellipsis">#:description#)</div>
	</div>
</script>
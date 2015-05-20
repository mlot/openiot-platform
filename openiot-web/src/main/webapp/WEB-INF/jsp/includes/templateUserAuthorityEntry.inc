<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<style>
.sw-user-auth-entry {
	margin-bottom: 5px;
}
</style>

<!-- User authority list item template -->
<script type="text/x-kendo-tmpl" id="tpl-user-authority-entry">
	<div class="sw-user-auth-entry">
		<input type="checkbox" value="#:authority#" style="margin-right: 5px; vertical-align: middle; margin-top: 0px;">
		<span>#:description#</span>
	</div>
</script>
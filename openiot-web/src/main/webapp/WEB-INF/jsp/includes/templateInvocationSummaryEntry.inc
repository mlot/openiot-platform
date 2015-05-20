<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for command invocation parameter summary entry -->
<style>
.sw-iv-invocation-params-col-name {
	width: 222px;
}
.sw-iv-invocation-params-col-value {
	width: 200px;
}
.sw-iv-invocation-params-required {
	font-weight: bold;
}
</style>
<script type="text/x-kendo-tmpl" id="tpl-inv-param-summary-entry">
	<tr class="sw-list-entry">
		<td class="sw-iv-invocation-params-col-name">
# if (required) { #
			<span class="sw-iv-invocation-params-required">
# } #
			#:name# (#:type#)
# if (required) { #
			</span>
# } #
		</td>
		<td class="sw-iv-invocation-params-col-value">#:value#</td>
	</tr>
</script>
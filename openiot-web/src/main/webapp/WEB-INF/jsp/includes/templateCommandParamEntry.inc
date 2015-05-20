<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for command parameter -->
<style>
.sw-command-param-actions {
	text-align: right;
}
</style>
<script type="text/x-kendo-tmpl" id="tpl-command-param-entry">
	<tr class="sw-list-entry">
		<td class="sw-cc-command-params-col-name">#:name#</td>
		<td class="sw-cc-command-params-col-type">#:type#</td>
# if (data.required) { #
		<td class="sw-cc-command-params-col-req"><i class="icon-check sw-button-icon"></i></td>
# } else { #
		<td class="sw-cc-command-params-col-req"></td>
# } #
		<td class="sw-cc-command-params-col-add">
			<div class="sw-command-param-actions">
				<i class="icon-remove sw-action-glyph sw-delete-glyph" title="Delete Parameter"
					onclick="onDeleteParameter('#:data.name#')"></i>
			</div>
		</td>
	</tr>
</script>
<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for a role entry -->
<style>
</style>
<script type="text/x-kendo-tmpl" id="tpl-element-role-entry">
	<tr class="sw-list-entry">
		<td>#= data #</td>
		<td>
			<div style="text-align: right;">
				<i class="icon-remove sw-action-glyph sw-delete-glyph" title="#= i18next("includes.DeleteRole") #"
					onclick="geaDeleteRole('#= data #')"></i>
			</div>
		</td>
	</tr>
</script>
<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for device group element row -->
<style>
.grp-elm-icon {
	margin-right: 7px;
	vertical-align: middle;
}
</style>
<script type="text/x-kendo-tmpl" id="tpl-device-group-element-entry">
	<tr class="sw-list-entry row-gradient">
# if (data.device) { #
		<td class="ellipsis"><i class="icon-desktop icon-white grp-elm-icon"></i>#:type# [#:elementId#]</td>
#   if (data.device.assignment) { #
		<td class="ellipsis">#:device.specification.assetName# (#:device.assignment.assetName#)</td>
#   } else { #
		<td class="ellipsis">#:device.specification.assetName#</td>
#   } #
# } else if (data.deviceGroup) { #
		<td class="ellipsis"><i class="icon-sitemap icon-white grp-elm-icon"></i>#:type# [#:elementId#]</td>
		<td class="ellipsis">#:deviceGroup.name#</td>
# } else { #
		<td class="ellipsis"><i class="icon-bolt icon-white grp-elm-icon"></i>#:type# [Invalid Reference]</td>
		<td class="ellipsis">Invalid</td>
# }#
		<td class="ellipsis">#:cdRoles#</td>
		<td>
			<div style="width: 100%; text-align: left">
				<i class="icon-remove sw-action-glyph sw-delete-glyph" title="#= i18next("includes.i18next.DeleteGroupElement") #"
					onclick="onDeleteDeviceGroupElement(event, '#:type#', '#:elementId#')"></i>
			</div>
		</td>
	</tr>
</script>
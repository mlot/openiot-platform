<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for measurements row -->
<script type="text/x-kendo-tmpl" id="tpl-measurements-entry">
	<tr class="sw-list-entry">
		<td class="ellipsis" style="max-width: 90px;" title="#:assetName#">
			<div>
				<span class="ellipsis" style="max-width: 150px; display: inline-block; vertical-align: middle; line-height: normal">
					#:assetName#</span>
				<i class="icon-circle-arrow-right sw-action-glyph sw-view-glyph" title="#= i18next("public.ViewAssignment") #"
					style="vertical-align: middle;" onclick="onViewAssignment(event, '#:deviceAssignmentToken#')"></i>
			</div>
		</td>
		<td>
			<div>#= measurementsSummary #</div>
		</td>
		<td>#= formattedDate(eventDate) #</td>
		<td>#= formattedDate(receivedDate) #</td>
	</tr>
</script>
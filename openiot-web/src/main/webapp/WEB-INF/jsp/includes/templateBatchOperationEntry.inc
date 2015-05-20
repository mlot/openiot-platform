<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for batch operation row -->
<script type="text/x-kendo-tmpl" id="tpl-batch-operation-entry">
	<tr class="sw-list-entry row-gradient">
		<td>#:operationType#</td>
		<td>#:processingStatus#</td>
		<td>#= formattedDate(createdDate) #</td>
		<td>#= formattedDate(processingStartedDate) #</td>
		<td>#= formattedDate(processingEndedDate) #</td>
		<td>
			<i class="icon-circle-arrow-right sw-action-glyph sw-view-glyph" title="View Batch Details"
				style="vertical-align: middle;" onclick="viewBatchOperation('#:operationType#', '#:token#')"></i>
		</td>
	</tr>
</script>
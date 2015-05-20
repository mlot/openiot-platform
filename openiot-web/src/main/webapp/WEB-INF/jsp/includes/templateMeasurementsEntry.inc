<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for measurements row -->
<script type="text/x-kendo-tmpl" id="tpl-measurements-entry">
	<tr class="sw-list-entry row-gradient">
		<td>
			<div>#= measurementsSummary #</div>
		</td>
		<td>#= formattedDate(eventDate) #</td>
	</tr>
</script>
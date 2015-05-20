<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for alert row -->
<script type="text/x-kendo-tmpl" id="tpl-alert-entry">
	<tr class="sw-list-entry row-gradient">
		<td class="ellipsis" style="max-width: 90px;" title="#:type#">#:type#</td>
		<td class="ellipsis" style="max-width: 250px;" title="#:message#">#:message#</td>
		<td class="ellipsis">#:source#</td>
		<td>#= formattedDate(eventDate) #</td>
	</tr>
</script>
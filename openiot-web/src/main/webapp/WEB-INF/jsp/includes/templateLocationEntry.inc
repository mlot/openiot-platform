<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for location row -->
<script type="text/x-kendo-tmpl" id="tpl-location-entry">
	<tr class="sw-list-entry row-gradient">
		<td>
			<div>#= kendo.toString(latitude, "\\#\\#\\#.000000")#</div>
		</td>
		<td>
			<div>#= kendo.toString(longitude, "\\#\\#\\#.000000")#</div>
		</td>
		<td>
			<div>#= kendo.toString(elevation, "\\#\\#\\#.000000")#</div>
		</td>
		<td>#= formattedDate(eventDate) #</td>
	</tr>
</script>
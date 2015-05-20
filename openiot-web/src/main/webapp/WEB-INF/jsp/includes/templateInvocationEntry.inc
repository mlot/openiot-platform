<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<!-- Template for alert row -->
<style>
.sw-spec-command-desc {
	color: #060;
	font-style: italic;
	max-width: 90%;
}
.sw-spec-command-name {
	color: #333;
	font-weight: bold;
}
.sw-spec-command-param-required {
	font-weight: normal;
}
.sw-spec-command-param-name {
	color: #333;
}
.sw-spec-command-param-type {
	padding-left: 3px;
	color: #333;
	font-weight: bold;
}
</style>
<script type="text/x-kendo-tmpl" id="tpl-invocation-entry">
	<tr class="sw-list-entry row-gradient">
		<td title="#:command.name#">#= asHtml #</td>
		<td title="#:initiator#(#:initiatorId#)">#:initiator#(#:initiatorId#)</td>
		<td title="#:target#">#:target#</td>
		<td>#= formattedDate(eventDate) #</td>
		<td>
			<div style="width: 100%; text-align: left">
				<i class="icon-info-sign sw-action-glyph sw-edit-glyph" title="#= i18next("includes.templateInvocationEntry.ViewInvocation") #"
					onclick="onViewInvocation('#:id#')"></i>
			</div>
		</td>
	</tr>
</script>
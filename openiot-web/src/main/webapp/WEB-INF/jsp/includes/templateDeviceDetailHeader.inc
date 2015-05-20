<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<style>
.sw-device-header {
	clear: both;
	border: 1px solid #dcdcdc;
	padding: 10px;
	margin-bottom: 15px;
	font-size: 10pt;
	text-align: left;
	display: block;
	min-height: 95px;
}

.sw-device-header-label {
	font-size: 10pt;
	font-weight: bold;
	min-width: 200px;
	display: inline-block;
	line-height: 1.5;
}

.sw-device-header-logowrapper {
	float: left;
	margin-right: 40px;
	margin-left: 10px;
	width: 130px;
	height: 130px;
	position: relative;
}

.sw-device-header-logo-tag {
	position: absolute;
	top: -2px;
	left: -4px;
}
</style>

<!-- Header for device detail page -->
<script type="text/x-kendo-tmpl" id="tpl-device-detail-header">
	<div class="sw-device-header gradient-bg">
		<div class="sw-device-header-logowrapper"
			style="background-image: url(#:specification.asset.imageUrl#); background-size: contain; background-repeat: no-repeat; background-position: 50% 50%;">
			<span class="label label-info sw-device-header-logo-tag">#= i18next("public.Device") #</span>
		</div>
		<div>
			<div>
				<span class="sw-device-header-label" title="Unique device hardware id">#= i18next("public.HardwareId") #:</span>
				<span title="#:hardwareId#">#:hardwareId#</span>
			</div>
			<div>
				<span class="sw-device-header-label" title="Device specification used">#= i18next("public.DeviceSpecification") #:</span>
				<span title="#:specification.name#"><a href='/admin/specifications/detail.html?token=#:specification.token#'>#:specification.name#</a></span>
			</div>
			<div>
				<span class="sw-device-header-label" title="Site where device is deployed">#= i18next("public.DeployedSite") #:</span>
				<span title="#:site.name#"><a href='/admin/sites/detail.html?siteToken=#:site.token#'>#:site.name#</a></span>
			</div>
			<div>
				<span class="sw-device-header-label" title="Currently assigned asset">#= i18next("public.CurrentAssignment") #:</span>
# if (data.assignment) { #
# if (assignment.assignmentType != 'Unassociated') { #
				<span title="#:assignment.assetName#"><a href='/admin/assignments/detail.html?token=#:assignment.token#'>#:assignment.assetName#</a></span>
# } else { #
				<span title="Not associated with an asset">#= i18next("public.DeviceNotAssociated") #</span>
# } #
# } else { #
				<span title="Device is not assigned">Device is not assigned</span>
# } #
			</div>
			<div>
				<span class="sw-device-header-label" title="#= i18next("includes.i18next.DDWATTS") #">#= i18next("public.CreatedDate") #:</span>
				<span title="#= formattedDate(createdDate) #">#= formattedDate(createdDate) #</span>
			</div>
			<div>
				<span class="sw-device-header-label" title="#= i18next("includes.i18next.DDWLU") #">#= i18next("public.UpdatedDate") #:</span>
				<span title="#= formattedDate(createdDate) #">#= formattedDate(updatedDate) #</span>
			</div>
		</div>
	</div>
</script>

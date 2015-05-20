<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<script type="text/x-kendo-template" id="tpl-site-scenario-panel">
<div class="sw-title-bar content k-header">
    <h1 class="ellipsis" data-i18n="devices.list.title">#= i18next("portal.ChooseScenario") #</h1>
    <div class="sw-title-bar-right">
        <a id="btn-readonly-results" class="btn" href="javascript:void(0)" data-i18n="public.FilterResults">#= i18next("portal.ReadOnlyMode") #</a>
        <a id="btn-update-results" class="btn" href="javascript:void(0)" data-i18n="public.FilterResults">#= i18next("portal.UpgradeMode") #</a>
        <a id="btn-maintain-command" class="btn" href="javascript:void(0)" data-i18n="devices.list.BatchCommand">#= i18next("portal.MaintenanceMode") #</a>
        <a id="btn-batch-operation" class="btn" href="javascript:void(0)" data-i18n="devices.list.AddNewDevice">#= i18next("portal.BatchNotificationMode") #</a>
        <a id="btn-batch-control" class="btn" href="javascript:void(0)" data-i18n="devices.list.AddNewDevice">#= i18next("portal.BatchControlMode") #</a>
    </div>
</div>
</script>
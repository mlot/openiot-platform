<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<style>
    .sw-device-list-entry-small {
        clear: both;
        height: 30px;
        border: 1px solid #dcdcdc;
        padding: 4px;
        font-size: 8pt;
        text-align: left;
        display: block;
    }

    .sw-device-list-entry-small.k-state-selected {
        padding: 5px;
        border: 2px solid #dc0000;
        color: #1a1a1a;
    }

    .sw-device-list-entry-small-logowrapper {
        float: left;
        margin-right: 15px;
        width: 30px;
        height: 30px;
        position: relative;
        border: 1px solid #ddd;
    }

    .sw-device-list-entry-small-logo {
        display: block;
        margin-left: auto;
        margin-right: auto;
        max-width: 70px;
        max-height: 70px;
        border: 1px solid rgb(221, 221, 221);
    }
</style>

<!-- Assignment template -->
<script type="text/x-kendo-tmpl" id="tpl-assignment-entry-small">
# if (data.status == 'Active') { #
    # if (data.associatedPerson) { #
            <div class="sw-device-list-entry-small gradient-bg sw-list-entry">
                <div class="sw-device-list-entry-small-logowrapper"
                    style="background-image: url(#:associatedPerson.imageUrl#); background-size: contain; background-repeat: no-repeat; background-position: 50% 50%;">
                </div>
                <div>
                    <div class="sw-device-list-entry-heading ellipsis"><span title="#:associatedPerson.name#">#:associatedPerson.name#</span></div>
                    <div class="ellipsis"><span class="sw-device-list-entry-label" style="min-width: 85px; margin-top: 7px;">Email:</span><span title="#:associatedPerson.emailAddress#">#:associatedPerson.emailAddress#</span></div>
                </div>
            </div>
    # } else if (data.associatedHardware) { #
            <div class="sw-device-list-entry-small gradient-bg sw-list-entry">
                <div class="sw-device-list-entry-small-logowrapper"
                    style="background-image: url(#:associatedHardware.imageUrl#); background-size: contain; background-repeat: no-repeat; background-position: 50% 50%;">
                </div>
                <div>
                    <div class="sw-device-list-entry-heading ellipsis"><span title="#:associatedHardware.name#">#:associatedHardware.name#</span></div>
                    <div class="ellipsis"><span class="sw-device-list-entry-label" style="min-width: 85px; margin-top: 7px;"> SKU:</span><span title="#:associatedHardware.sku#">#:associatedHardware.sku#</span></div>
                </div>
            </div>
    # } else if (data.associatedLocation) { #
            <div class="sw-device-list-entry-small gradient-bg sw-list-entry">
                <div class="sw-device-list-entry-small-logowrapper"
                    style="background-image: url(#:associatedLocation.imageUrl#); background-size: contain; background-repeat: no-repeat; background-position: 50% 50%;">
                </div>
                <div>
                    <div class="sw-device-list-entry-heading ellipsis"><span title="#:associatedLocation.name#">#:associatedLocation.name#</span></div>
                    <div class="ellipsis"><span class="sw-device-list-entry-label" style="min-width: 85px; margin-top: 7px;"></span><span title="#:associatedLocation.name#"></span></div>
                </div>
            </div>
    # } else if ((data.assignmentType == 'Unassociated') && (data.device)) { #
            <div class="sw-device-list-entry-small gradient-bg sw-list-entry">
                <div class="sw-device-list-entry-small-logowrapper"
                    style="background-image: url(#:device.assetImageUrl#); background-size: contain; background-repeat: no-repeat; background-position: 50% 50%;">
                </div>
                <div>
                    <div class="sw-device-list-entry-heading ellipsis"><span title="#:device.assetName#">#:device.assetName#</span></div>
                    <div class="ellipsis"><span class="sw-device-list-entry-label" style="min-width: 85px; margin-top: 7px;">Id:</span><span title="#:deviceHardwareId#">#:deviceHardwareId#</span></div>
                </div>
            </div>
    # } #
# } #


</script>

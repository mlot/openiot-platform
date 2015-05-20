<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<style>

    .kpi-title span,
    .first dd {
        font-weight: 700;
    }

    .kpi-subtitle {
        font-size: 13px;
        font-weight: 700;
        margin-bottom: 0;
    }

    .metric {
        display: block;
        text-transform: none;
        font-weight: 300;
        font-size: 12px;
        color: #727f8e;
        margin: 0;
    }

    .stat .head-kpi {
        font-size: 80px;
        line-height: 1em;
        letter-spacing: -2px;
        text-indent: -8px;
        color: #454447;
    }

    .stat .volume {
        font-size: 72px;
        color: #454447;
    }

    .stat dd {
        margin: 0;
    }

    .stat,
    .relative-value-pie,
    .site-trends {
        margin: 0;
        display: inline-block;
        *display: inline;
        zoom: 1;
        vertical-align: top;
    }

    .site-trends {
        margin-left: 28px;
    }

    .trend-container {
        background: transparent none no-repeat 100% 29px;
        width: 90px;
        margin-right: 18px;
    }

    /*.lowest-container {*/
    /*background: transparent url("../content/dataviz/dashboards/lowest.png") no-repeat 100% 37px;*/
    /*}*/

    /*.highest-container {*/
    /*background: transparent url("../content/dataviz/dashboards/highest.png") no-repeat 100% 29px;*/
    /*}*/

    dd.active,
    dd.inactive,
    dd.missed {
        margin-top: 8px;
        font-size: 36px;
        font-weight: 700;
    }

    .active {
        color: #639514;
    }

    .inactive {
        color: #4da3d5;
    }

    .missed {
        color: #cd151e;
    }

    dd.sparkline {
        height: 30px;
        width: 100px;
        margin-top: 8px;
    }

    .relative-value-pie {
        margin-left: 0;
    }

</style>

<script type="text/x-kendo-template" id="tpl-site-kpi-panel">
    <dl class="stat first">
        <dt class="kpi-subtitle">
            #= i18next("portal.RegisteredDeviceNum") #
            <span class="metric">#= i18next("portal.ByPiece") #</span>
        </dt>
        <dd class="head-kpi">5288</dd>
    </dl>

    <div class="site-trends">
        <div class="kpi-subtitle">
            #= i18next("portal.DeviceActivityTrends") #
            <span class="metric">#= i18next("portal.ByYear") #</span>
        </div>

        <dl class="stat trend-container highest-container">
            <dt class="kpi-subtitle">#= i18next("portal.ActiveDeviceNum") #</dt>
            <dd class="active">3453</dd>
            <dd id="active-sparkline" class="sparkline"></dd>
        </dl>

        <dl class="stat trend-container lowest-container">
            <dt class="kpi-subtitle">#= i18next("portal.InActiveDeviceNum") #</dt>
            <dd class="inactive">1823</dd>
            <dd id="inactive-sparkline" class="sparkline"></dd>
        </dl>

        <dl class="stat trend-container">
            <dt class="kpi-subtitle">#= i18next("portal.MissedDeviceNum") #</dt>
            <dd class="missed">12</dd>
            <dd id="missed-sparkline" class="sparkline"></dd>
        </dl>

        <div id="relative-value-pie" class="relative-value-pie"></div>
    </div>

</script>

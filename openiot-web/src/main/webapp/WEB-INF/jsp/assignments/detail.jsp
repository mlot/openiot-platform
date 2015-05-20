<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>

<c:set var="sitewhere_title" value="View Assignment" />
<c:set var="sitewhere_section" value="sites" />
<c:set var="use_flot" value="true" />

<%@ include file="../includes/top.inc"%>
<style>
    .event-pager {
        margin-top: 10px;
    }
    .chart{
        overflow: hidden;
        height: 300px;
    }
    .main {
        height: 465px;
        width: 98%;
    }

    .left {
        float: left;
        height:600px;
        width: 24%;
    }

    .right {
        float: left;
        height:600px;
        width: 70%;
    }

</style>

<!-- Title Bar -->
<div class="sw-title-bar content k-header" style="margin-bottom: -1px;">
    <h1 class="ellipsis" data-i18n="assignments.detail.title"></h1>
    <div class="sw-title-bar-right">
        <a id="btn-emulator" class="btn" href="emulator.html?token=<c:out value="${assignment.token}"/>" data-i18n="assignments.detail.EmulateAssignment">
            <i class="icon-bolt sw-button-icon"></i></a>
        <a id="btn-edit-assignment" class="btn" href="javascript:void(0)" data-i18n="public.EditAssignment">
            <i class="icon-edit sw-button-icon"></i></a>
    </div>
</div>

<!-- Detail panel for selected assignment -->
<div id="assignment-details" style="line-height: normal;"></div>

<!-- Tab panel -->
<div id="tabs">
    <ul>
        <li class="k-state-active" >&nbsp;<font data-i18n="public.Locations"></font></li>
        <li >&nbsp;<font data-i18n="public.Measurements"></font></li>
        <li >&nbsp;<font data-i18n="public.Alerts"></font></li>
        <li >&nbsp;<font data-i18n="public.CommandInvocations"></font></li>
        <li >&nbsp;<font data-i18n="public.RealtimeCharts"></font></li>
    </ul>
    <div>
        <div class="k-header sw-button-bar">
            <div class="sw-button-bar-title" data-i18n="public.DeviceLocations"></div>
            <div>
                <a id="btn-filter-locations" class="btn" href="javascript:void(0)" data-i18n="public.FilterResults">
                    <i class="icon-search sw-button-icon"></i></a>
                <a id="btn-refresh-locations" class="btn" href="javascript:void(0)" data-i18n="public.Refresh">
                    <i class="icon-refresh sw-button-icon"></i></a>
            </div>
        </div>
        <table id="locations">
            <colgroup>
                <col style="width: 20%;"/>
                <col style="width: 20%;"/>
                <col style="width: 20%;"/>
                <col style="width: 20%;"/>
            </colgroup>
            <thead>
            <tr>
                <th data-i18n="public.Latitude"></th>
                <th data-i18n="public.Longitude"></th>
                <th data-i18n="public.Elevation"></th>
                <th data-i18n="public.EventDate"></th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="5"></td></tr>
            </tbody>
        </table>
        <div id="locations-pager" class="k-pager-wrap event-pager"></div>
    </div>
    <div>
        <div class="k-header sw-button-bar">
            <div class="sw-button-bar-title" data-i18n="public.DeviceMeasurements"></div>
            <div>
                <a id="btn-filter-measurements" class="btn" href="javascript:void(0)" data-i18n="public.FilterResults">
                    <i class="icon-search sw-button-icon"></i></a>
                <a id="btn-refresh-measurements" class="btn" href="javascript:void(0)" data-i18n="public.Refresh">
                    <i class="icon-refresh sw-button-icon"></i></a>
            </div>
        </div>
        <table id="measurements">
            <colgroup>
                <col style="width: 37%;"/>
                <col style="width: 20%;"/>
            </colgroup>
            <thead>
            <tr>
                <th data-i18n="public.Measurements"></th>
                <th data-i18n="public.EventDate"></th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="3"></td></tr>
            </tbody>
        </table>
        <div id="measurements-pager" class="k-pager-wrap event-pager"></div>
    </div>
    <div>
        <div class="k-header sw-button-bar">
            <div class="sw-button-bar-title" data-i18n="public.DeviceAlerts"></div>
            <div>
                <a id="btn-filter-alerts" class="btn" href="javascript:void(0)" data-i18n="public.FilterResults">
                    <i class="icon-search sw-button-icon"></i></a>
                <a id="btn-refresh-alerts" class="btn" href="javascript:void(0)" data-i18n="public.Refresh">
                    <i class="icon-refresh sw-button-icon"></i></a>
            </div>
        </div>
        <table id="alerts">
            <colgroup>
                <col style="width: 10%;"/>
                <col style="width: 20%;"/>
                <col style="width: 10%;"/>
                <col style="width: 20%;"/>
            </colgroup>
            <thead>
            <tr>
                <th data-i18n="public.Type"></th>
                <th data-i18n="public.Message"></th>
                <th data-i18n="public.Source"></th>
                <th data-i18n="public.EventDate"></th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="5"></td></tr>
            </tbody>
        </table>
        <div id="alerts-pager" class="k-pager-wrap event-pager"></div>
    </div>
    <div>
        <div class="k-header sw-button-bar">
            <div class="sw-button-bar-title" data-i18n="assignments.detail.DeviceCommandInvocations"></div>
            <div>
                <a id="btn-filter-invocations" class="btn" href="javascript:void(0)" data-i18n="public.FilterResults">
                    <i class="icon-search sw-button-icon"></i></a>
                <a id="btn-refresh-invocations" class="btn" href="javascript:void(0)" data-i18n="public.Refresh">
                    <i class="icon-refresh sw-button-icon"></i></a>
                <a id="btn-create-invocation" class="btn" href="javascript:void(0)" data-i18n="assignments.detail.InvokeCommand">
                    <i class="icon-bolt sw-button-icon"></i></a>
            </div>
        </div>
        <table id="invocations">
            <colgroup>
                <col style="width: 32%;"/>
                <col style="width: 15%;"/>
                <col style="width: 12%;"/>
                <col style="width: 20%;"/>
                <col style="width: 8%;"/>
            </colgroup>
            <thead>
            <tr>
                <th data-i18n="public.Command"></th>
                <th data-i18n="public.Source"></th>
                <th data-i18n="assignments.detail.Target"></th>
                <th data-i18n="public.EventDate"></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="5"></td></tr>
            </tbody>
        </table>
        <div id="invocations-pager" class="k-pager-wrap event-pager"></div>
    </div>

    <div class="main" style="padding:0.2em 0.2em">
        <div class="left">
            <div style="border-right:1px solid #ddd;">
                <div class="page-sidebar-menu">
                    <div class="disd" onclick="display_div(1)">
                        <li class="sideways padding">
                            <div id="cpu" class="chart cpuborder" style="width:95px;height:65px;background-color: #fff;margin:0 auto"></div>
                        </li>

                        <li class="sideways">
                            <span class="font" data-i18n="public.CPU"></span> <br>
                            <span class="cpu-now">N/A</span><span> 600MHz</span>
                        </li>
                    </div><br>
                    <div class="disd" onclick="display_div(2)">
                        <li class="sideways padding">
                            <div id="memory" class="chart memoryborder" style="width:95px;height:65px;background-color: #fff;margin:0 auto"></div>
                        </li>
                        <li class="sideways ">
                            <span class="font" data-i18n="public.Memory"></span> <br>
                            <span class="mem-now">N/A</span><span> 64MB</span>
                        </li>
                    </div><br>
                    <div class="disd" onclick="display_div(3)">
                        <li class="sideways padding ">
                            <div id="disk" class="chart diskborder" style="width:95px;height:65px;background-color: #fff;margin:0 auto"></div>
                        </li>
                        <li class="sideways">
                            <span class="font" data-i18n="public.DiskSpace"></span> <br>
                            <span class="disk-now">N/A </span>
                        </li>
                    </div><br>
                    <div class="disd" onclick="display_div(4)">
                        <li class=" sideways padding ">
                            <div id="wlan" class="chart wlanborder" style="width:95px;height:65px;background-color: #fff;margin:0 auto"></div>
                        </li>
                        <li class="sideways">
                            <span class="font" data-i18n="public.WLAN"></span> <br>
                            <span class="wlan-now">N/A</span>
                        </li>
                    </div>
                </div>
            </div>
        </div>
        <div class="right" id="ch1" >
            <div class="padding"><span class="font1 " data-i18n="public.CPU"></span><span class="font padd">MTK MT7620N 600MHz MIPS</span>                                                               </div>
            <div id="charts_cpu" style="width:650px;height:300px;margin:0 auto"></div>
            <div style="margin:auto;  text-align: center;padding:10px 20px">
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Utilization"></span> <br>
                    <span class="cpu-now">N/A</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Frequency"></span> <br>
                    <span>600Mhz</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Processes"></span> <br>
                    <span>N/A</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Threads"></span> <br>
                    <span>N/A</span>
                </li>
            </div>
        </div>
        <div class="right" id="ch2" style="display:none;">
            <div class="padding"><span class="font1 " data-i18n="public.Memory"></span><span class="font padd">64MByte DDR3</span>                                                               </div>
            <div id="charts_memory" style="width:650px;height:300px;margin:0 auto"></div>
            <div style="margin:auto;  text-align: center;padding:10px 20px">
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.InUse"></span> <br>
                    <span >N/A MByte</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Available"></span> <br>
                    <span>N/A MByte</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Allocated"></span> <br>
                    <span>N/A MByte</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Cached"></span> <br>
                    <span>N/A MByte</span>
                </li>
            </div>
        </div>
        <div class="right" id="ch3" style="display:none;">
            <div class="padding"><span class="font1 " data-i18n="public.DiskSpace"></span><span class="font padd">ST1000DM003-1ER162</span>                                                                </div>
            <div id="charts_disk" style="width:650px;height:300px;margin:0 auto"></div>
            <div style="margin:auto;  text-align: center;padding:10px 20px">
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.ActiveTime"></span> <br>
                    <span >0%</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.AverageResponseTime"></span> <br>
                    <span>N/A ms</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Reads"></span> <br>
                    <span>N/A KB/s</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Writes"></span> <br>
                    <span>N/A KB/s</span>
                </li>
            </div>
        </div>
        <div class="right" id="ch4" style="display:none;">
            <div class="padding">
                <span class="font1 " data-i18n="public.WLAN"></span>
                <span class="font padd">300Mbps Wi-Fi 2T2R 802.11n 2.4 GHz</span>
            </div>
            <div id="charts_wlan" style="width:650px;height:300px;margin:0 auto"></div>
            <div style="margin:auto;  text-align: center;padding:10px 20px">
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Sent"></span> <br>
                    <span >N/A Kbps</span>
                </li>
                <li class="sideways tpadding">
                    <span class="font pad" data-i18n="public.Speed"></span> <br>
                    <span>N/A Kbps</span>
                </li>
            </div>
        </div>
    </div>

</div>

<%@ include file="../includes/assignmentUpdateDialog.inc"%>
<%@ include file="../includes/commandInvokeDialog.inc"%>
<%@ include file="../includes/invocationViewDialog.inc"%>
<%@ include file="../includes/templateAssignmentDetailHeader.inc"%>
<%@ include file="../includes/templateAssignmentEntry.inc"%>
<%@ include file="../includes/templateInvocationEntry.inc"%>
<%@ include file="../includes/templateInvocationSummaryEntry.inc"%>
<%@ include file="../includes/templateResponseSummaryEntry.inc"%>
<%@ include file="../includes/templateLocationEntry.inc"%>
<%@ include file="../includes/templateMeasurementsEntry.inc"%>
<%@ include file="../includes/templateAlertEntry.inc"%>
<%@ include file="../includes/commonFunctions.inc"%>

<script>
    /** Set sitewhere_title */
    sitewhere_i18next.sitewhere_title = "assignments.detail.title";

    /** Assignment token */
    var token = '<c:out value="${assignment.token}"/>';

    /** Device specification token */
    var specificationToken = '<c:out value="${assignment.device.specificationToken}"/>';

    /** Datasource for invocations */
    var invocationsDS;

    /** Datasource for locations */
    var locationsDS;

    /** Datasource for measurements */
    var measurementsDS;

    /** Datasource for alerts */
    var alertsDS;

    /** Reference to tab panel */
    var tabs;

    /** Size of pages from server */
    var pageSize = 100;

    /** Height of event grids */
    var gridHeight = 350;

    /** Called when 'edit assignment' is clicked */
    function onEditAssignment(e, token) {
        var event = e || window.event;
        event.stopPropagation();
        auOpen(token, onEditAssignmentComplete);
    }

    /** Called after successful edit assignment */
    function onEditAssignmentComplete() {
        // Handle reload.
    }

    /** Called when 'release assignment' is clicked */
    function onReleaseAssignment(e, token) {
        var event = e || window.event;
        event.stopPropagation();
        swReleaseAssignment(token, onReleaseAssignmentComplete);
    }

    /** Called after successful release assignment */
    function onReleaseAssignmentComplete() {
        loadAssignment();
    }

    /** Called when 'missing assignment' is clicked */
    function onMissingAssignment(e, token) {
        var event = e || window.event;
        event.stopPropagation();
        swAssignmentMissing(token, onMissingAssignmentComplete);
    }

    /** Called after successful missing assignment */
    function onMissingAssignmentComplete() {
        loadAssignment();
    }

    /** Called to view an invocation */
    function onViewInvocation(id) {
        ivOpen(id);
    }

    $(document).ready(function() {

        /** Create AJAX datasource for locations list */
        locationsDS = new kendo.data.DataSource({
            transport : {
                read : {
                    url : "${pageContext.request.contextPath}/api/assignments/" + token + "/locations",
                    dataType : "json",
                }
            },
            schema : {
                data: "results",
                total: "numResults",
                parse: parseEventResults,
            },
            serverPaging: true,
            serverSorting: true,
            pageSize: pageSize,
        });

        /** Create the location list */
        $("#locations").kendoGrid({
            dataSource : locationsDS,
            rowTemplate: kendo.template($("#tpl-location-entry").html()),
            scrollable: true,
            height: gridHeight,
        });

        $("#locations-pager").kendoPager({
            dataSource: locationsDS
        });

        $("#btn-refresh-locations").click(function() {
            locationsDS.read();
        });
        $('#btn-filter-locations').attr('disabled', true);

        /** Create AJAX datasource for measurements list */
        measurementsDS = new kendo.data.DataSource({
            transport : {
                read : {
                    url : "${pageContext.request.contextPath}/api/assignments/" + token + "/measurements",
                    dataType : "json",
                }
            },
            schema : {
                data: "results",
                total: "numResults",
                parse: parseEventResults,
            },
            serverPaging: true,
            serverSorting: true,
            pageSize: pageSize,
        });

        /** Create the measurements list */
        $("#measurements").kendoGrid({
            dataSource : measurementsDS,
            rowTemplate: kendo.template($("#tpl-measurements-entry").html()),
            scrollable: true,
            height: gridHeight,
        });

        $("#measurements-pager").kendoPager({
            dataSource: measurementsDS
        });

        $("#btn-refresh-measurements").click(function() {
            measurementsDS.read();
        });
        $('#btn-filter-measurements').attr('disabled', true);

        /** Create AJAX datasource for alerts list */
        alertsDS = new kendo.data.DataSource({
            transport : {
                read : {
                    url : "${pageContext.request.contextPath}/api/assignments/" + token + "/alerts",
                    dataType : "json",
                }
            },
            schema : {
                data: "results",
                total: "numResults",
                parse: parseEventResults,
            },
            serverPaging: true,
            serverSorting: true,
            pageSize: pageSize,
        });

        /** Create the alerts list */
        $("#alerts").kendoGrid({
            dataSource : alertsDS,
            rowTemplate: kendo.template($("#tpl-alert-entry").html()),
            scrollable: true,
            height: gridHeight,
        });

        $("#alerts-pager").kendoPager({
            dataSource: alertsDS
        });

        $("#btn-refresh-alerts").click(function() {
            alertsDS.read();
        });
        $('#btn-filter-alerts').attr('disabled', true);

        /** Create AJAX datasource for invocations list */
        invocationsDS = new kendo.data.DataSource({
            transport : {
                read : {
                    url : "${pageContext.request.contextPath}/api/assignments/" + token + "/invocations",
                    dataType : "json",
                }
            },
            schema : {
                data: "results",
                total: "numResults",
                parse: parseEventResults,
            },
            serverPaging: true,
            serverSorting: true,
            pageSize: pageSize,
        });

        /** Create the invocations list */
        $("#invocations").kendoGrid({
            dataSource : invocationsDS,
            rowTemplate: kendo.template($("#tpl-invocation-entry").html()),
            scrollable: true,
            height: gridHeight,
        });

        $("#btn-refresh-invocations").click(function() {
            invocationsDS.read();
        });
        $('#btn-filter-invocations').attr('disabled', true);

        $("#btn-create-invocation").click(function() {
            ciOpen(token, specificationToken, onInvokeCommandSuccess);
        });

        $("#invocations-pager").kendoPager({
            dataSource: invocationsDS
        });

        $("#btn-edit-assignment").click(function() {
            auOpen(token, onAssignmentEditSuccess);
        });

        /** Create the tab strip */
        tabs = $("#tabs").kendoTabStrip({
            animation: false,
            activate: onActivate
        }).data("kendoTabStrip");

        loadAssignment();
    });

    /** Force grid refresh on first tab activate (KendoUI bug) */
    function onActivate(e) {
        var tabName = e.item.textContent;
        if (!e.item.swInitialized) {
            if (tabName =="Locations") {
                locationsDS.read();
                e.item.swInitialized = true;
            } else if (tabName =="Measurements") {
                measurementsDS.read();
                e.item.swInitialized = true;
            } else if (tabName =="Alerts") {
                alertsDS.read();
                e.item.swInitialized = true;
            } else if (tabName =="Command Invocations") {
                invocationsDS.read();
                e.item.swInitialized = true;
            }
        }
    };

    /** Loads information for the selected assignment */
    function loadAssignment() {
        $.getJSON("${pageContext.request.contextPath}/api/assignments/" + token,
                loadGetSuccess, loadGetFailed);
    }

    /** Called on successful assignment load request */
    function loadGetSuccess(data, status, jqXHR) {
        var template = kendo.template($("#tpl-assignment-detail-header").html());
        parseAssignmentData(data);
        data.inDetailView = true;
        $('#assignment-details').html(template(data));
    }

    /** Handle error on getting assignment data */
    function loadGetFailed(jqXHR, textStatus, errorThrown) {
        handleError(jqXHR, "Unable to load assignment data.");
    }

    /** Parses event response records to format dates */
    function parseEventResults(response) {
        $.each(response.results, function (index, item) {
            parseEventData(item);
        });
        return response;
    }

    /** Called after successful edit of assignment */
    function onAssignmentEditSuccess() {
        loadAssignment();
    }

    /** Called after successful edit of assignment */
    function onInvokeCommandSuccess() {
        invocationsDS.read();
    }

    /////////////////////////////////// Realtime charts ////////////////////////////////

    $(document).ready(function () {
        var defaultUpdateInterval = 10000; //10 seconds
        // cpu chart
        var cpu_placeholder = $("#charts_cpu");
        var cpu_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'cpu.utils',
            observer:'cpu-now',
            updateInterval: defaultUpdateInterval};
        var cpu_chartOptions = {label:i18next("public.CPU"), color:'#4599CA'};

        var cpu_chart = new MetricChart(cpu_placeholder,cpu_dataOptions,cpu_chartOptions);
        cpu_chart.draw();

        // memory chart
        var mem_placeholder = $("#charts_memory");
        var mem_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'mem.utils',
            observer:'mem-now',
            updateInterval: defaultUpdateInterval};
        var mem_chartOptions = {label:i18next("public.Memory"), color:'#AA51C4'};

        var mem_chart = new MetricChart(mem_placeholder,mem_dataOptions,mem_chartOptions);
        mem_chart.draw();

        // disk space chart
        var disk_placeholder = $("#charts_disk");
        var disk_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'disk.utils',
            observer:'disk-now',
            updateInterval: defaultUpdateInterval};
        var disk_chartOptions = {label:i18next("public.DiskSpace"), color:'#85C258'};

        var disk_chart = new MetricChart(disk_placeholder,disk_dataOptions,disk_chartOptions);
        disk_chart.draw();

        // wlan chart
        var wlan_placeholder = $("#charts_wlan");
        var wlan_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'wlan.utils',
            observer:'wlan-now',
            updateInterval: defaultUpdateInterval};
        var wlan_chartOptions = {label:i18next("public.WLAN"), color:'#BF7F46'};

        var wlan_chart = new MetricChart(wlan_placeholder,wlan_dataOptions,wlan_chartOptions);
        wlan_chart.draw();



        // cpu mini chart
        var mini_cpu_placeholder = $("#cpu");
        var mini_cpu_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'cpu.utils',
            updateInterval: defaultUpdateInterval};
        var mini_cpu_chartOptions = {color:'#4599CA'};

        var mini_cpu_chart = new MetricChart(mini_cpu_placeholder,mini_cpu_dataOptions,mini_cpu_chartOptions);
        mini_cpu_chart.draw_mini();

        // mem mini chart
        var mini_mem_placeholder = $("#memory");
        var mini_mem_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'mem.utils',
            updateInterval: defaultUpdateInterval};
        var mini_mem_chartOptions = {color:'#AA51C4'};

        var mini_mem_chart = new MetricChart(mini_mem_placeholder,mini_mem_dataOptions,mini_mem_chartOptions);
        mini_mem_chart.draw_mini();

        // disk mini chart
        var mini_disk_placeholder = $("#disk");
        var mini_disk_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'disk.utils',
            updateInterval: defaultUpdateInterval};
        var mini_disk_chartOptions = {color:'#85C258'};

        var mini_disk_chart = new MetricChart(mini_disk_placeholder,mini_disk_dataOptions,mini_disk_chartOptions);
        mini_disk_chart.draw_mini();

        // wlan mini chart
        var mini_wlan_placeholder = $("#wlan");
        var mini_wlan_dataOptions = {url:"${pageContext.request.contextPath}/api/assignments/" + token + "/measurements?page=1&pageSize=1",
            key:'wlan.utils',
            updateInterval: defaultUpdateInterval};
        var mini_wlan_chartOptions = {color:'#BF7F46'};

        var mini_wlan_chart = new MetricChart(mini_wlan_placeholder,mini_wlan_dataOptions,mini_wlan_chartOptions);
        mini_wlan_chart.draw_mini();

    });

    function MetricChart(placeholder, dataOptions, chartOptions){

        this.placeholder = placeholder;
        this.dataOptions = dataOptions;
        this.chartOptions = chartOptions;
        this.metricSequence = [];
        //this.now = new Date().getTime();
        this.graphOptions = {};
    }

    MetricChart.prototype._update = function(){
        var self = this;
        FlyJSONP.init({debug:false});
        FlyJSONP.get({
            url: self.dataOptions.url,
            success: function(_data){
                var lastMetric = _data.results[0].measurements[self.dataOptions.key];
                //console.info(lastMetric);
                //console.info(self.dataOptions.observer);
                if (lastMetric && self.dataOptions.observer){
                    $('.'+self.dataOptions.observer).text(lastMetric+'%');
                }

                var now = new Date().getTime();
                var temp = [now, lastMetric];

                self.metricSequence.shift();
                self.metricSequence.push(temp);

                var dataset;
                if (self.graphOptions.series.label != null && lastMetric != null) {
                    dataset = [{ label: self.graphOptions.series.label + ":" + lastMetric + "%", data: self.metricSequence}];
                } else {
                    dataset = [{data: self.metricSequence}];
                }

                $.plot(self.placeholder, dataset, self.graphOptions);

                setTimeout(function(){self._update();}, self.dataOptions.updateInterval);

            },
            error: function(){
                setTimeout(function(){self._update();}, self.dataOptions.updateInterval);
            }
        });
    }

    MetricChart.prototype.draw = function(){

        var self = this;
        self.graphOptions = {
            series: {
                label: self.chartOptions.label,
                color: self.chartOptions.color,
                lines: {show: true,
                    fill: true,
                    lineWidth:1,
                    fillColor: {colors: [{opacity: 0.1}, {opacity: 0.1}]}
                },
                shadowSize:0
            },
            xaxis: {
                mode: "time",
                timezone: "browser",
                timeformat: "%H:%M:%S",
                tickLength: 0,
                //tickSize: [10, "second"],

                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 10
            },
            yaxes: [{
                min: 0,
                max: 100,
                tickSize: 10,
                tickFormatter: function (v, axis) {
                    if (v % 10 == 0) {
                        return v + "%";
                    } else {
                        return "";
                    }
                },
                position:"right",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 6
            }],
            legend: {
                labelBoxBorderColor: "#ffffff",
                noColumns: 0,
                position:"nw"
            },
            grid: {
                borderWidth: 1,
                color: self.chartOptions.color,
                backgroundColor:"#ffffff"
            }
        };

        //init data
        var totalPoints = 100;
        var now = new Date().getTime();
        for (var i = totalPoints; i > 0; i--) {
            var temp = [(now - i * self.dataOptions.updateInterval), 0];
            self.metricSequence.push(temp);
        }

        var dataset = [{data: self.metricSequence}];
        $.plot(self.placeholder, dataset, self.graphOptions);
        self._update();
        //setTimeout(function(){self._update();}, self.dataOptions.updateInterval);
    }

    MetricChart.prototype.draw_mini = function(){

        var self = this;
        self.graphOptions = {
            series: {
                color: self.chartOptions.color,
                lines: {show: true,
                    fill: true,
                    lineWidth:1,
                    fillColor: {colors: [{opacity: 0.2}, {opacity: 0.2}]}
                },
                shadowSize:0
            },
            xaxis: {
                mode: "time",
                timezone: "browser",
                timeformat: "%H:%M:%S",
                tickLength: 0,
                //tickSize: [10, "second"]
            },
            yaxes: [{
                min: 0,
                max: 100,
                tickSize: 10,
                tickFormatter: function (v, axis) {
                    if (v % 10 == 0) {
                        return v + "%";
                    } else {
                        return "";
                    }
                },
                position:"right",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 6
            }],
            legend: {
                noColumns: 0,
                position:"nw"
            },
            grid: {
                show:false
            }
        };

        //init data
        var now = new Date().getTime();
        var totalPoints = 100;
        for (var i = totalPoints; i > 0; i--) {
            var temp = [(now - i * self.dataOptions.updateInterval), 0];
            self.metricSequence.push(temp);
        }

        var dataset = [{data: self.metricSequence}];
        $.plot(self.placeholder, dataset, self.graphOptions);
        self._update();
        //setTimeout(function(){self._update();}, self.dataOptions.updateInterval);
    }

    function display_div(can){

        switch(can){
            case 1:{
                $("#ch1").css('display','block');
                $("#ch2").css('display','none');
                $("#ch3").css('display','none');
                $("#ch4").css('display','none');

                break;
            }
            case 2:{
                $("#ch1").css('display','none');
                $("#ch2").css('display','block');
                $("#ch3").css('display','none');
                $("#ch4").css('display','none');

                break;
            }
            case 3:{
                $("#ch1").css('display','none');
                $("#ch2").css('display','none');
                $("#ch3").css('display','block');
                $("#ch4").css('display','none');

                break;
            }
            case 4:{
                $("#ch1").css('display','none');
                $("#ch2").css('display','none');
                $("#ch3").css('display','none');
                $("#ch4").css('display','block');

                break;
            }
            default:
        }
    }

</script>

<%@ include file="../includes/bottom.inc"%>
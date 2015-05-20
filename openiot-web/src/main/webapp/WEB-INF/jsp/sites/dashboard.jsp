<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  ~ Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
  ~
  ~ The software in this package is published under the terms of the CPAL v1.0
  ~ license, a copy of which has been included with this distribution in the
  ~ LICENSE.txt file.
  --%>set var="sitewhere_title" value="View Site" />
<c:set var="sitewhere_section" value="sites" />
<c:set var="use_map_includes" value="true" />
<c:set var="use_color_picker_includes" value="true" />
<c:set var="use_sparkline" value="true" />

<%@ include file="../includes/top.inc"%>
<style>
    .site-mini-map {
        height: 210px;
        /*width: 350px;*/
        border: 1px solid #cccccc;
    }
    .assignments-map {
        height: 400px;
        /*width: 580px;*/
        margin-top: 10px;
        border: 1px solid #cccccc;
        /*cursor: crosshair;*/
    }
    .kpi-panel {
        /*width: 560px;*/
        margin: 0;
        padding: 8px;
    }
    #left-panel{
        float: left;
        margin-bottom: 5px;
        width: 29%
    }
    #right-panel {
        float: right;
        margin-bottom: 5px;
        width: 70%
    }
    /* WIDGETS */
    .widget {
        margin-bottom: 5px;
        padding: 0;
        background-color: #ffffff;
        border: 1px solid #e7e7e7;
        border-radius: 3px;
    }

    /*.widget div {*/
        /*padding: 10px;*/
        /*min-height: 50px;*/
    /*}*/

    .widget h3 {
        margin: 0;
        font-size: 12px;
        padding-left: 8px;
        text-transform: uppercase;
        border-bottom: 1px solid #e7e7e7;
        line-height: 28px
    }

    .widget h3 span {
        float: right;
    }
    .widget h3 span:hover {
        cursor: pointer;
        background-color: #e7e7e7;
        border-radius: 20px;
    }
</style>

<div id="left-panel">

    <div id="site-map" class="widget">
        <h3><c:out value="${site.name}"/></h3>
        <div id="site-mini-map" class="site-mini-map"></div>
    </div>

    <!-- Tab panel -->
    <div id="tabs">
        <ul>
            <li class="k-state-active" >&nbsp;<font data-i18n="sites.detail.Assignments"></font></li>
        </ul>
        <div>
            <div id="assignments" class="sw-assignment-list"></div>
            <div id="assignments-pager" class="k-pager-wrap"></div>
        </div>

    </div>
</div>

<div id="right-panel">

    <div id="header-panel" class="widget">
        <h3 data-i18n="portal.KPIPanel"></h3>
        <div id="kpi-panel" class="kpi-panel"></div>
    </div>
    <div id="customized-panel"  class="widget">
        <h3 data-i18n="portal.DetailMap"></h3>
        <div id="assignments-map" class="assignments-map"></div>
    </div>
    <div id="command-panel" class="widget">
        <h3 data-i18n="portal.ScenarioPanel"></h3>
        <div id="scenario-panel" class="scenario-pane"></div>
    </div>
</div>

<%@ include file="../includes/templateAssignmentEntrySmall.inc"%>
<%@ include file="../includes/siteKpiPanel.inc"%>
<%@ include file="../includes/siteScenarioPanel.inc"%>
<%@ include file="../includes/commonFunctions.inc"%>

<script>
    /** Set sitewhere_title */
    sitewhere_i18next.sitewhere_title = "sites.detail.title";

    var siteToken = '<c:out value="${site.token}"/>';

    /** Current site */
    var site;

    /** Datasource for assignments */
    var assignmentsDS

    /** Datasource for site */
    var siteDS;

    /** Reference to tab panel */
    var tabs;

    /** Size of pages from server */
    var pageSize = 100;

    /** Height of event grids */
    var gridHeight = 350;

    /** Mini map to show the site location */
    var miniMap;

    /** Big map to show the assignments on site */
    var assignmentsMap;

    $(document).ready(function() {

        loadSite();

        loadAssignments();

        loadKpiPanel();

        loadScenarioPanel();

    });

    /** Loads information for the selected site */
    function loadSite() {
        <%--$.getJSON("${pageContext.request.contextPath}/api/sites/" + siteToken,--%>
                <%--loadGetSuccess, loadGetFailed);--%>
        if (miniMap) {
            miniMap.remove();
        }
        miniMap = L.Map.siteWhere('site-mini-map', {
            siteWhereApi: '${pageContext.request.contextPath}/api/',
            siteToken: siteToken,
            showZones: false,
            showSiteMarker: true
        });
    }

    function loadAssignments(){
        /** Create AJAX datasource for assignments list */
        assignmentsDS = new kendo.data.DataSource({
            transport : {
                read : {
                    url : "${pageContext.request.contextPath}/api/sites/" + siteToken +
                    "/assignments?includeDevice=true&includeAsset=true",
                    dataType : "json"
                }
            },
            schema : {
                data: "results",
                total: "numResults",
                parse:function (response) {
                    $.each(response.results, function (index, item) {
                        parseAssignmentData(item);
                    });
                    return response;
                }
            },
            serverPaging: true,
            serverSorting: true,
            pageSize: 10
        });


        /** Create the assignments list */
        $("#assignments").kendoListView({
            dataSource : assignmentsDS,
            template : kendo.template($("#tpl-assignment-entry-small").html())
        });

        $("#assignments-pager").kendoPager({
            dataSource: assignmentsDS
        });

        /** Create the tab strip */
        tabs = $("#tabs").kendoTabStrip({
            animation: false,
            activate: onActivate
        }).data("kendoTabStrip");

        assignmentsDS.fetch(function(){
            assignmentsMap = L.Map.siteWhere('assignments-map', {
                siteWhereApi: '${pageContext.request.contextPath}/api/',
                siteToken: siteToken,
                onZonesLoaded: loadAssignmentsMap
            });
        });
    }

        /** Force grid refresh on first tab activate (KendoUI bug) */
        function onActivate(e) {
        }

        var assignmentsLayer;

        /** Initialize the map */
        function loadAssignmentsMap() {

            // Create layer for Assignments' last location
            <%--assignmentsLayer = L.FeatureGroup.OpenIoT.assignmentsLocationInSite({--%>
            <%--siteWhereApi: '${pageContext.request.contextPath}/api/',--%>
            <%--siteToken: siteToken,--%>
            <%--onAssignmentsLoaded: onAssignmentsUpdated--%>
            <%--});--%>
            assignmentsLayer = L.FeatureGroup.OpenIoT.assignmentsLocationInSite({
                dataSource: assignmentsDS,
                onAssignmentsLoaded: onAssignmentsUpdated
            });
            assignmentsMap.addLayer(assignmentsLayer);
            assignmentsLayer.addListener(assignmentsMap);
        }

            function onAssignmentsUpdated() {
                assignmentsLayer.panToLastLocation(assignmentsMap);
                assignmentsDS.bind("change",assignmentsDSChanged);
            }

            function assignmentsDSChanged(){
                if (assignmentsLayer){
                    assignmentsLayer.refreshView();
                }
            }


    function loadKpiPanel(){
        /** load kpi panel */
        var kpiPanel = kendo.template($("#tpl-site-kpi-panel").html());
        $("#kpi-panel").html(kpiPanel);
        $("#active-sparkline").sparkline([5,6,7,9,9,5,3,2,2,4,6,7], {
            type: 'line',
            width: '100',
            height: '30',
            lineColor: '#639514',
//            fillColor: '#ffffff',
            lineWidth: 2
//            spotColor: '#639514',
//            minSpotColor: '#639514',
//            maxSpotColor: '#639514'
        });
        $("#inactive-sparkline").sparkline([1,6,0,9,6,5,6,2,4,4,6,7], {
            type: 'line',
            width: '100',
            height: '30',
            lineColor: '#4da3d5',
            lineWidth: 2
        });
        $("#missed-sparkline").sparkline([1,6,0,9,6,5,6,2,4,4,6,7], {
            type: 'line',
            width: '100',
            height: '30',
            lineColor: '#cd151e',
            lineWidth: 2
        });
        $("#relative-value-pie").sparkline([8,2,1], {
            type: 'pie',
            width: '80',
            height: '80',
            sliceColors: ['#639514','#4da3d5','#cd151e']
        });
    }

    function loadScenarioPanel(){
        /** load scenario panel */
        var scenarioPanel = kendo.template($("#tpl-site-scenario-panel").html());
        $("#scenario-panel").html(scenarioPanel);
    }

</script>

<%@ include file="../includes/bottom.inc"%>
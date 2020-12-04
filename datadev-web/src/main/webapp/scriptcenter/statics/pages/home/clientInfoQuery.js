$(function () {
    var operateHistory = {};
    operateHistory.isInit = false;
    operateHistory.init = function initData() {
        this.init = true;
        var _colModel = [
            {
                name:'id',
                label:"id",
                sortable:false,
            },
            {
                name:'ip',
                label:"ip",
                sortable:false,
            },
            {
                name:'jobLimit',
                label:"job_limit",
                sortable:false,
            },
            {
                name:'deleted',
                label:"deleted",
                sortable:false,
            },
            {
                name:'activeScriptRunDetail',
                label:"active_script_run_detail",
                sortable:false,
            },
            {
                name:'lastActiveTime',
                label:"last_active_time",
                sortable:false,
            },
            {
                name:'cpu',
                label:"cpu",
                sortable:false,
            },
            {
                name:'disk',
                label:"disk",
                sortable:false,
            },
        ];

        var pager_selector = "#client-info-pager";
        var clientInfoList = $("#clientInfo").text();
        var clientInfoListJson = JSON.parse(clientInfoList);
        jQuery("#client-info-table").jqGrid({
            datatype:"local",
            data: clientInfoListJson,
            colModel:_colModel,
            viewrecords:true,
            rowList:[5,10,20,50],
            pager:pager_selector,
            altRows:true,
            width:'100%',
            autowidth:true,
            autoencode:true,
            height:'100%',
            shrinkToFit: true,
            rownumbers:true,
            scrollOffset:6,
            forceFit:true,
            loadComplete:function (data) {
                jqGrid.initWidth(jQuery, "#client-info-table", "#clientInfo-jd-table-parent");
                jqGrid.reset(jQuery);
                $("#client-info-table").setGridHeight($("#clientInfo-jd-table-parent").height());
            }
        });
    }

    $("#clientInfoDetail").data("init", operateHistory);
    $("#clientInfoDetail").data("init").init();

})


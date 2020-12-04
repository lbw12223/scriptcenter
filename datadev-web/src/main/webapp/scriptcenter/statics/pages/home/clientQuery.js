$(function () {

    $("#submit").click(function () {

        var key = $("#key").val().trim();
        var value = $("#value").val().trim();
        $.ideDoRequest("/scriptcenter/setCacheValue.ajax",{key:key,value:value},function () {
            $.successMsg("修改成功");
            window.location.reload();
        })
    })

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
                formatter: function (cellvalue, options, record) {
                    /*return "<a href='/scriptcenter/clientInfo.html?ip="+ record['ip'] + "'>" + "</a>";*/
                    return "<a href='http://mdc.jd.com/monitor/chart?ip="+ cellvalue + "' target=\"_blank\" >" + cellvalue + "</a>";
                },
            },
            {
                name:'jobLimit',
                label:"job_limit",
                sortable:false,
            },
            {
                name:'status',
                label:"状态",
                sortable:false,
                formatter:function (cellvalue, options, record) {
                    return "<span  data-id='"+record.id+"' data-status='1'class='"+(record.status==1?"valid opt_span":"opt_span")+"'>启用</span><span data-id='"+record.id+"' data-status='0' class='"+(record.status!=1?"valid opt_span":"opt_span")+"'>禁用</span>"
                }
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
            }
        ];

        var pager_selector = "#client-base-pager";
        jQuery("#client-base-table").jqGrid({
            datatype: "json",
            url: '/scriptcenter/clientQuery.ajax',
            mtype: 'POST',
            colModel:_colModel,
            viewrecords:true,
            rowList:[5,10,20],
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
                jqGrid.initWidth(jQuery, "#client-base-table", "#clientBase-jd-table-parent");
                jqGrid.reset(jQuery);
                $("#client-base-table").setGridHeight($("#clientBase-jd-table-parent").height());
            }
        });
    }

    $("#clientBaseInfomation").data("init", operateHistory);
    $("#clientBaseInfomation").data("init").init();


    $("#clientBaseInfomation").on("click",".opt_span",function () {
        var id = $(this).attr("data-id");
        var status = $(this).attr("data-status");
        commonAjaxEvents.commonPostAjax("/scriptcenter/modifyStatus.ajax",{
            id:id,
            status:status
        },null,function () {
            jQuery("#client-base-table").trigger("reloadGrid");
        })
    })

})


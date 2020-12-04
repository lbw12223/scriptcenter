$(function () {
    var initStatus = false;
    $("#scriptDetailDiv").JdDataDevTab({
        afterClickCallBack: function (event, obj, args, li) {
            if (li.attr("target") == "operate-detail-content") {
                initData();
            }
        }
    });
    $("#scriptDetailDiv").JdDataDevTab("active", 0);
    $('#queryStartTime').datetimepicker({
        autoclose: true,
        language: "zh-CN",
        minView: 0,
        minuteStep: 5,
        format: 'yyyy-mm-dd HH:ii:00',
        todayBtn: true,
        clearBtn: true
    });
    $('#queryEndTime').datetimepicker({
        autoclose: true,
        language: "zh-CN",
        minView: 0,
        minuteStep: 5,
        format: 'yyyy-mm-dd HH:ii:59',
        todayBtn: true,
        clearBtn: true
    });
    $("#queryStartTime,#queryEndTime").on("change", function () {
        jQuery("#detail-grid-table").jqGrid("setGridParam", {
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                startTime: $("#queryStartTime").val(),
                endTime: $("#queryEndTime").val(),
            }
        })
    })
    $("#queryButton").click(function () {
        jQuery("#detail-grid-table").trigger("reloadGrid");
    })

     function initData() {
        if (!initStatus) {
            initStatus = true;
            var _colModel = [
                {
                    name: 'gitVersion',
                    label: "脚本版本",
                    sortable: false,
                },
                {
                    name: 'created',
                    label: "保存时间",
                    sortable: false,
                },
                {
                    name: 'sysCreatorName',
                    label: "修改人",
                    sortable: false,
                },
                {
                    name: 'commitMessage',
                    label: "修改内容",
                    sortable: false,
                },
            ];
            var pager_selector = "#detail-grid-pager";

            jQuery("#detail-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/project/projectCommits.ajax',
                mtype: 'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                    startTime: $("#queryStartTime").val(),
                    endTime: $("#queryEndTime").val(),
                },
                colModel: _colModel,
                viewrecords: true,
                rowList: [5, 10, 20, 50, 100],
                pager: pager_selector,
                altRows: true,
                width: '100%',
                autowidth: true,
                autoencode: true,
                height: "100%",
                shrinkToFit: true,
                rownumbers: true,
                scrollOffset: 6,
                forceFit:true,
                loadComplete: function (data) {
                    jqGrid.initWidth(jQuery, '#detail-grid-table', "#detail-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#detail-grid-table").setGridHeight($("#detail-jd-table-parent").height()-140);
                }
            });
             $(window).resize(function () {
                 $("#detail-grid-table").setGridWidth($("#detail-jd-table-parent").width());
                 $("#detail-grid-table").setGridHeight($("#detail-jd-table-parent").height()-140);
             });
        } else {
            $("#detail-grid-table").setGridWidth($("#detail-jd-table-parent").width());
            $("#detail-grid-table").setGridHeight($("#detail-jd-table-parent").height()-140);
        }
     }

})

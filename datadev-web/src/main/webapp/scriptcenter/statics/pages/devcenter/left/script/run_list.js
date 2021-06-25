

function openScriptContent(gitProjectId, gitProjectFilePath, version) {
    var gitProjectId = $.trim(gitProjectId);
    var gitProjectFilePath = $.trim(gitProjectFilePath)
    gitProjectFilePath = encodeURIComponent(encodeURIComponent(gitProjectFilePath));
    var url = "/scriptcenter/script/scriptContent.html?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath + "&version=" + version;
    $.dialog.open(url, {
        title: "脚本内容",
        lock: true,
        width: "70%",
        height: "80%",
        opacity: 0.5,
        esc: false,
        close: function () {
        }
    })
    $.dialog.data("openScriptContent", openScriptContent);
}


var ScriptRunListClass = /** @class */ (function () {
    function ScriptRunListClass(cfg) {
        this.config = cfg;
        this.state = {
            tableName: 'runListGridTable',
            getRunListApi: "/scriptcenter/runList.ajax",
            getDataLogApi: "/scriptcenter/devcenter/home_data_log.html",
            batchStopApi: "/scriptcenter/script/batchStop.ajax",
            stopApi: "/scriptcenter/script/stop.ajax",
            getMarketApi: "/scriptcenter/config/getMarketByErp.ajax"
        };
        this.isInstance();
    }

    // 校验是否实例
    ScriptRunListClass.prototype.isInstance = function () {
        if (!(this instanceof ScriptRunListClass)) {
            throw TypeError("Class constructor An cannot be invoked without 'new'");
        }
    };

    ScriptRunListClass.prototype.run = function () {
        this.initForm();
        this.initMarket();
        this.bindEvent();
        this.initTable();
    }


    /**
     * 初始化表格列
     */
    ScriptRunListClass.prototype.initColumns = function () {
        var baseColumns = [
            {
                key: 'operator',
                text: '运行人',
                width: 80
            },
            {
                key: 'marketName',
                text: '集市名称',
                width: 80,
                template: function (cell, row) {
                    return cell && cell.length > 0 ? cell : "--";
                }
            },
            {
                key: 'id',
                width: 80,
                text: '运行ID'
            },
            {
                key: 'gitProjectPath',
                text: '项目名称',
                width: 80,
            },
            {
                key: 'gitProjectFilePath',
                text: '脚本路径',
                width: 80,
            },
            {
                key: 'version',
                text: '版本号',
                width: 100,
                template: function (cell, row, index, key) {
                    var id = row.id;
                    var isTmp = row.runTmp;
                    var name = "";
                    var str = '<span data-index="' + id +
                        '" class="run-item run-script-version ' + (isTmp ? "tmp-version" : "") +
                        '" data-id="' + id +
                        '" data-name="' + name +
                        '" data-version="' + cell +
                        '" data-ProjectFilePath="' + row.gitProjectFilePath +
                        '" data-projectId="' + row.gitProjectId +
                        '"  >' + cell +
                        '</span>';
                    return str;
                }
            },
            {
                key: 'statusStr',
                text: '状态',
                width: 80,
            },
            {
                key: 'startTime',
                text: '开始时间',
                width: 160,
            },
            {
                key: 'endTime',
                text: '结束时间',
                width: 160,
            },
            {
                key: 'timePeriod',
                text: '耗时',
                width: 80
            },
            {
                key: 'scriptFileId',
                text: '操作',
                width: 100,
                fixed: 'right',
                template: function (cell, row, index, key) {
                    var id = row.id;
                    var type = row.type;
                    var str = '';
                    if (type == 1 && row.dataCount > 1) {
                        str = '<span class="run-item run-data-log" data-type="1" data-id="' + id + '">日志</span><span class="run-item run-data-log" data-type="2" data-id="' + id + '">结果</span>';
                    } else if (row.status == 1 || row.status == 2) {
                        str = '<span class="run-item run-data-log" data-type="1" data-id="' + id + '">日志</span><span class="run-item stop-script" data-id="' + id + '">停止</span>';
                    } else {
                        str = '<span class="run-item run-data-log" data-type="1" data-id="' + id + '">日志</span>';
                    }
                    return str;
                }
            }
        ]
        var scriptType = $("#scriptType").val();
        if (scriptType - 1 === 0) {
            baseColumns.splice(-2, 0, {
                key: 'engineType',
                text: '引擎类型'
            })
        }
        return baseColumns;
    }

    /**
     * 生成请求参数
     */
    ScriptRunListClass.prototype.generateParam = function (page) {
        var status = "";
        $("input.script-run-history-checkbox:checked").each(function (index, element) {
            status += "," + $(element).val();
        })
        var param = {
            statusStr: status,
            startTimeFrom: $("#runStartTimeFrom").val(),
            startTimeTo: $("#runStartTimeTo").val(),
            endTimeFrom: $("#runEndTimeFrom").val(),
            endTimeTo: $("#runEndTimeTo").val(),
            operator: $("#operatorSelect").val(),
            marketId: $("#queryMarketSelect").val(),
            id: $("#runId").val()
        }
        if (page) {
            param.page = GridManager.get(this.state.tableName).pageData.page;
            param.rows = GridManager.get(this.state.tableName).pageData.rows;
        }
        return param
    }

    /**
     * 初始化表格
     */
    ScriptRunListClass.prototype.initTable = function () {
        var initParam = this.generateParam()
        $('#run-grid-table').GM(
            {
                gridManagerName: this.state.tableName,
                height: "400px",
                width: "100%",
                query: initParam,
                ajaxData: this.state.getRunListApi,
                responseHandler: function (response) {
                    response.data = response.rows;
                    return response;
                },
                ajaxType: 'POST',
                supportAjaxPage: true,
                supportAutoOrder: false,
                totalsKey: 'records',
                currentPageKey: 'page',
                pageSizeKey: 'rows',
                supportAdjust: true,
                supportDrag: false,
                columnData: this.initColumns()
            }
        )
    }

    /**
     * 初始化market
     */
    ScriptRunListClass.prototype.initMarket = function () {
        commonAjaxEvents.commonPostAjax(this.state.getMarketApi, {}, $("#marketSelect"), function (node, data) {
            $("#queryMarketSelect").empty();
            var options = "<option value='-1'>选择集市</option>";
            if (data && data.obj) {
                for (var index = 0; index < data.obj.length; index++) {
                    var market = data.obj[index];
                    options += "<option data-ugdap='" + market.isUgdap + "' data-cluster='" + market.clusterCode + "' data-id='" + market.marketId + "' data-market-user='" + market.marketUser + "' value='" + market.marketId + "'>" + market.marketName + "</option>";
                }
            }
            $("#queryMarketSelect").append(options);
            $("#queryMarketSelect").select2({
                placeholder: '请选择集市'
            });
        });
    }

    ScriptRunListClass.prototype.initForm = function () {
        datadev_user_common._int($("#operatorSelect"), false);
        $('.start-time').datetimepicker({
            autoclose: true,
            language: "zh-CN",
            minView: 0,
            minuteStep: 5,
            format: 'yyyy-mm-dd HH:ii:00',
            todayBtn: true,
            clearBtn: true
        });
        $('.end-time').datetimepicker({
            autoclose: true,
            language: "zh-CN",
            minView: 0,
            minuteStep: 5,
            format: 'yyyy-mm-dd HH:ii:59',
            todayBtn: true,
            clearBtn: true
        });
    }

    ScriptRunListClass.prototype.bindEvent = function () {
        this.bindForceStopEvent();
        this.bindQueryEvent();
        this.bindQueryButtonEvent();
        this.bindShowLogEvent();
        this.bindVersionEvent();
        this.bindStopEvent();
    }


    /**
     * 强制停止事件绑定
     */
    ScriptRunListClass.prototype.bindForceStopEvent = function () {
        var _this = this;
        $("#stop-mutil").click(function () {
            var rowDatas = GridManager.getCheckedData(_this.state.tableName);
            if (!(rowDatas && rowDatas.length > 0)) {
                return;
            }
            var runningIds = [];
            rowDatas.forEach(item => {
                (item.status * 1 === 2) && runningIds.push(item.id);
            })
            if (runningIds.length === 0) {
                $.errorMsg("请选中【执行中】的记录！");
                return;
            }
            commonAjaxEvents.commonAllPostAjax(_this.state.batchStopApi, {
                runDetailIds: runningIds.join(","),
            }, $("#stop-mutil"), function (node, data) {
                $.successMsg("强制停止成功!")
                GridManager.refreshGrid(_this.state.tableName, false);
            }, function () {
                $.errorMsg("强制停止失败!")
            })
        })
    }

    /**
     * 表单change事件绑定
     */
    ScriptRunListClass.prototype.bindQueryEvent = function () {
        var _this = this;
        $("#gitProjectFilePath,#runStartTimeFrom,#runStartTimeTo,#runEndTimeFrom,#runEndTimeTo,input.script-run-history-checkbox,#runId,#operatorSelect,#queryMarketSelect").on("change", function () {
            var param = _this.generateParam();
            GridManager.setQuery(_this.state.tableName, param, true, null);
        })
    }

    /**
     * 表单查询按钮事件绑定
     */
    ScriptRunListClass.prototype.bindQueryButtonEvent = function () {
        var _this = this;
        $("#run-history-query").click(function () {
            GridManager.refreshGrid(_this.state.tableName, true, null);
        })
    }

    /**
     * 绑定查询日志事件
     */
    ScriptRunListClass.prototype.bindShowLogEvent = function () {
        var _this = this;
        $("#run-jd-table-parent").on("click", "span.run-data-log", function () {
            var url = _this.state.getDataLogApi + "?runDetailId=" + $(this).attr("data-id") + "&dataLog=" + $(this).attr("data-type")
            $.dialog.open(url, {
                title: "运行日志",
                lock: true,
                width: "70%",
                height: "80%",
                opacity: 0.5,
                esc: false,
                close: function () {
                }
            })
        })
    }

    /**
     * 绑定版本点击事件
     */
    ScriptRunListClass.prototype.bindVersionEvent = function () {
        $("#run-jd-table-parent").on("click", "span.run-script-version", function () {
            var gitProjectId = $(this).attr("data-projectId");
            var gitProjectFilePath = $(this).attr("data-ProjectFilePath");
            if (!$(this).hasClass("tmp-version")) {
                var version = $(this).attr("data-version");
                openScriptContent(gitProjectId, gitProjectFilePath, version);
            }
        })
    }

    /**
     * 单独停止事件
     */
    ScriptRunListClass.prototype.bindStopEvent = function () {
        var _this = this;
        $("#run-jd-table-parent").on("click", "span.stop-script", function () {
            var runDetailId = $(this).attr("data-id");
            commonAjaxEvents.commonPostAjax(_this.state.stopApi, {runDetailId: runDetailId}, $(this), function (node, data) {
                GridManager.refreshGrid(_this.state.tableName, false, null);
            })

        })
    }


    return ScriptRunListClass;
}())

$(function () {
    var scriptRunListInstance = new ScriptRunListClass();
    scriptRunListInstance.run()
})







// 下面是老代码,测试通过后可以删除
// var stopUrl = "/scriptcenter/script//batchStop.ajax";
//
//
// ///scriptcenter/devcenter/home_data_log.html?runDetailId=20629&dataLog=1
// $(function () {
//     var runHistory = {};
//     var getDataLogUrl = "/scriptcenter/devcenter/home_data_log.html";
//     runHistory.init = function () {
//         this.isInit = true;
//         var _colModel = [
//             {
//                 name: 'status',
//                 label: "status",
//                 sortable: false,
//                 width: 80,
//                 hidden: true
//             },
//             {
//                 name: 'gitProjectId',
//                 label: "gitProjectId",
//                 sortable: false,
//                 width: 80,
//                 hidden: true
//             },
//             {
//                 name: 'gitProjectFilePath',
//                 label: "gitProjectFilePath",
//                 sortable: false,
//                 width: 80,
//                 hidden: true
//             },
//             {
//                 name: 'operator',
//                 label: "运行人",
//                 sortable: false,
//                 width: 80
//             },
//             {
//                 name: 'marketName',
//                 label: "集市名称",
//                 sortable: false,
//                 width: 80,
//                 formatter: function (cellvalue, options, record) {
//                     return cellvalue && cellvalue.length > 0 ? cellvalue : "--";
//                 }
//             },
//             {
//                 name: 'id',
//                 label: "运行ID",
//                 sortable: false,
//                 width: 80
//             },
//             {
//                 name: 'gitProjectPath',
//                 label: "项目名称",
//                 sortable: false,
//                 width: 80
//             },
//             {
//                 name: 'gitProjectFilePath',
//                 label: "脚本路径",
//                 sortable: false,
//                 width: 80
//             },
//             {
//                 name: 'version',
//                 label: "版本号",
//                 sortable: false,
//                 width: 100,
//                 formatter: function (cellvalue, options, record) {
//                     var id = record.id;
//                     var isTmp = record.runTmp;
//                     var name = "";
//                     var str = '<span data-index="' + id + '" class="run-item run-script-version ' + (isTmp ? "tmp-version" : "") + '" data-id="' + id + '" data-name="' + name + '" data-version="' + record.version + '"  >' + record.version + '</span>';
//                     return str;
//                 }
//             },
//             {
//                 name: 'statusStr',
//                 label: "状态",
//                 sortable: false,
//                 width: 80
//             },
//             {
//                 name: 'startTime',
//                 label: "开始时间",
//                 sortable: false,
//                 width: 158
//             },
//             {
//                 name: 'endTime',
//                 label: "结束时间",
//                 sortable: false,
//                 width: 158
//             },
//             {
//                 name: 'timePeriod',
//                 label: "耗时",
//                 sortable: false,
//                 width: 83
//             },
//             {
//                 name: 'scriptFileId',
//                 label: "操作",
//                 sortable: false,
//                 width: 100,
//                 formatter: function (cellvalue, options, record) {
//                     var id = record.id;
//                     var type = record.type;
//                     var str = '';
//                     if (type == 1 && record.dataCount > 1) {
//                         str = '<span class="run-item run-data-log" data-type="1" data-index="' + id + '">日志</span><span class="run-item run-data-log" data-type="2" data-index="' + id + '">结果</span>';
//                     } else if (record.status == 1 || record.status == 2) {
//                         str = '<span class="run-item run-data-log" data-type="1" data-index="' + id + '">日志</span><span class="run-item stop-script" data-index="' + id + '">停止</span>';
//                     } else {
//                         str = '<span class="run-item run-data-log" data-type="1" data-index="' + id + '">日志</span>';
//                     }
//                     return str;
//                 }
//             },
//
//         ];
//         var scripttype = $("#scriptType").val();
//         if (scripttype == 1) {
//             _colModel.splice(-2, 0, {
//                 name: 'engineType',
//                 label: "引擎类型",
//                 sortable: false,
//                 width: 83
//             })
//         }
//         var pager_selector = "#run-grid-pager";
//         var status = "";
//         $("input.script-run-history-checkbox:checked").each(function (index, element) {
//             status += "," + $(element).val();
//         })
//
//         jQuery("#run-grid-table").jqGrid({
//             datatype: "json",
//             url: '/scriptcenter/runList.ajax',
//             mtype: 'POST',
//             postData: {
//                 statusStr: status,
//                 startTimeFrom: $("#runStartTimeFrom").val(),
//                 startTimeTo: $("#runStartTimeTo").val(),
//                 endTimeFrom: $("#runEndTimeFrom").val(),
//                 endTimeTo: $("#runEndTimeTo").val(),
//                 operator: $("#operatorSelect").val(),
//                 marketId: $("#queryMarketSelect").val(),
//                 id: $("#runId").val()
//             },
//             colModel: _colModel,
//             viewrecords: true,
//             rowList: [5, 10, 20, 50, 100],
//             rowNum: 20,
//             pager: pager_selector,
//             altRows: true,
//             width: '100%',
//             autowidth: true,
//             autoencode: true,
//             height: "100%",
//             shrinkToFit: true,
//             rownumbers: true,
//             scrollOffset: 6,
//             multiselect: true,
//             loadComplete: function (data) {
//                 jqGrid.initWidth(jQuery, '#run-grid-table', "#run-jd-table-parent");
//                 jqGrid.reset(jQuery);
//                 $("#run-grid-table").setGridHeight($("#run-jd-table-parent").height() - 70);
//             }
//         });
//
//
//     };
//     runHistory.initMarket = function () {
//         commonAjaxEvents.commonPostAjax("/scriptcenter/config/getMarketByErp.ajax", {}, $("#marketSelect"), function (node, data) {
//             $("#queryMarketSelect").empty();
//             var options = "<option value='-1'>选择集市</option>";
//             if (data && data.obj) {
//                 for (var index = 0; index < data.obj.length; index++) {
//                     var market = data.obj[index];
//                     options += "<option data-ugdap='" + market.isUgdap + "' data-cluster='" + market.clusterCode + "' data-id='" + market.marketId + "' data-market-user='" + market.marketUser + "' value='" + market.marketId + "'>" + market.marketName + "</option>";
//                 }
//             }
//             $("#queryMarketSelect").append(options);
//             $("#queryMarketSelect").select2({
//                 placeholder: '请选择集市'
//             });
//         });
//     }
//     runHistory.init();
//     runHistory.initMarket();
//     datadev_user_common._int($("#operatorSelect"), false);
//
//     $("#stop-mutil").click(function () {
//
//         var runningIds = [];
//         var rowIds = $("#run-grid-table").jqGrid('getGridParam', 'selarrrow');
//         if (rowIds && rowIds.length > 0) {
//             for (var index = 0; index < rowIds.length; index++) {
//                 var rowData = $("#run-grid-table").jqGrid('getRowData', rowIds[index]);
//                 if (rowData["status"] * 1 == 2) {
//                     runningIds.push(rowIds[index]);
//                 }
//             }
//         }
//         if (runningIds.length > 0) {
//             commonAjaxEvents.commonAllPostAjax(stopUrl, {
//                 runDetailIds: runningIds.join(","),
//             }, $("#stop-mutil"), function (node, data) {
//                 $.successMsg("强制停止成功!")
//                 jQuery("#run-grid-table").trigger("reloadGrid");
//             }, function () {
//                 $.errorMsg("强制停止失败!")
//             })
//         } else {
//             $.errorMsg("请选中【执行中】的记录！")
//         }
//
//     })
//     $('.start-time').datetimepicker({
//         autoclose: true,
//         language: "zh-CN",
//         minView: 0,
//         minuteStep: 5,
//         format: 'yyyy-mm-dd HH:ii:00',
//         todayBtn: true,
//         clearBtn: true
//     });
//     $('.end-time').datetimepicker({
//         autoclose: true,
//         language: "zh-CN",
//         minView: 0,
//         minuteStep: 5,
//         format: 'yyyy-mm-dd HH:ii:59',
//         todayBtn: true,
//         clearBtn: true
//     });
//
//
//     $("#gitProjectFilePath,#runStartTimeFrom,#runStartTimeTo,#runEndTimeFrom,#runEndTimeTo,input.script-run-history-checkbox,#runId,#operatorSelect,#queryMarketSelect").on("change", function () {
//         var status = "";
//         $("input.script-run-history-checkbox:checked").each(function (index, element) {
//             status += "," + $(element).val();
//         })
//         jQuery("#run-grid-table").jqGrid("setGridParam", {
//             postData: {
//                 statusStr: status,
//                 startTimeFrom: $("#runStartTimeFrom").val(),
//                 startTimeTo: $("#runStartTimeTo").val(),
//                 endTimeFrom: $("#runEndTimeFrom").val(),
//                 endTimeTo: $("#runEndTimeTo").val(),
//                 operator: $("#operatorSelect").val(),
//                 marketId: $("#queryMarketSelect").val(),
//                 id: $("#runId").val()
//             }
//         })
//     })
//
//     $("#run-history-query").click(function () {
//         jQuery("#run-grid-table").jqGrid('setGridParam', {
//             page: 1,
//         }).trigger("reloadGrid");
//
//     })
//     $("#run-jd-table-parent").on("click", "span.run-data-log", function (event) {
//         var runDetailId = $(this).attr("data-index");
//         var dataLogType = $(this).attr("data-type");
//
//
//         var url = getDataLogUrl + "?runDetailId=" + runDetailId + "&dataLog=" + dataLogType
//         $.dialog.open(url, {
//             title: "运行日志",
//             lock: true,
//             width: "70%",
//             height: "80%",
//             opacity: 0.5,
//             esc: false,
//             close: function () {
//             }
//         })
//     })
//     $("#run-jd-table-parent").on("click", "span.stop-script", function (event) {
//         var runDetailId = $(this).attr("data-index");
//         commonAjaxEvents.commonPostAjax("/scriptcenter/script/stop.ajax", {runDetailId: runDetailId}, $(this), function (node, data) {
//             jQuery("#run-grid-table").trigger("reloadGrid");
//         })
//
//     })
//     $("#run-jd-table-parent").on("click", "span.run-script-version", function (event) {
//         var runDetailId = $(this).attr("data-index");
//
//
//         var rowData = $("#run-grid-table").jqGrid('getRowData', runDetailId);
//
//
//         var gitProjectId = rowData["gitProjectId"];
//         var gitProjectFilePath = rowData["gitProjectFilePath"];
//
//         if (!$(this).hasClass("tmp-version")) {
//             var version = $(this).attr("data-version");
//             openScriptContent(gitProjectId, gitProjectFilePath, version);
//         }
//     })
//
// })

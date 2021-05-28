$(function () {
    var runHistory = {};
    var getDataLogUrl = "/scriptcenter/devcenter/home_data_log.html";
    runHistory.isInit = false;
    runHistory.init = function initGrace() {
        if (this.isInit) {
            jQuery("#run-grid-table").jqGrid('setGridParam', {
                page: 1
            }).trigger("reloadGrid");

        } else {
            this.isInit = true;
            var _colModel = [
                {
                    name: 'id',
                    label: "运行ID",
                    sortable: false,
                    width: 80
                },
                {
                    name: 'version',
                    label: "版本号",
                    sortable: false,
                    width: 100,
                    formatter: function (cellvalue, options, record) {
                        var id = record.id;
                        var isTmp = record.runTmp;
                        var name = "";
                        var str = '<span class="run-item run-script-version '+(isTmp?"tmp-version":"")+'" data-id="'+id+'" data-name="'+name+'" data-version="' + record.version + '"  >' + record.version + '</span>';
                        return str;
                    }
                },
                {
                    name: 'statusStr',
                    label: "状态",
                    sortable: false,
                    width: 80
                },
                {
                    name: 'startTime',
                    label: "开始时间",
                    sortable: false,
                    width: 158
                },
                {
                    name: 'endTime',
                    label: "结束时间",
                    sortable: false,
                    width: 158
                },
                {
                    name: 'timePeriod',
                    label: "耗时",
                    sortable: false,
                    width: 83
                },
                {
                    name: 'scriptFileId',
                    label: "操作",
                    sortable: false,
                    width: 100,
                    formatter: function (cellvalue, options, record) {
                        var id = record.id;
                        var type = record.type;
                        var str = '';
                        if (type == 1 && record.dataCount > 1) {
                            str = '<span class="run-item run-data-log" data-type="1" data-index="' + id + '">日志</span><span class="run-item run-data-log" data-type="2" data-index="' + id + '">结果</span>';
                        } else if (record.status == 1 || record.status == 2) {
                            str = '<span class="run-item run-data-log" data-type="1" data-index="' + id + '">日志</span><span class="run-item stop-script" data-index="' + id + '">停止</span>';
                        } else {
                            str = '<span class="run-item run-data-log" data-type="1" data-index="' + id + '">日志</span>';
                        }
                        return str;
                    }
                },

            ];
            var scripttype = $("#scriptType").val();
            if (scripttype == 1) {
                _colModel.splice(-2, 0, {
                    name: 'engineType',
                    label: "引擎类型",
                    sortable: false,
                    width: 83
                })
            }
            var pager_selector = "#run-grid-pager";
            var status = "";
            $("input.script-run-history-checkbox:checked").each(function (index, element) {
                status += "," + $(element).val();
            })
            jQuery("#run-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/scriptFile/getRunHistory.ajax',
                mtype: 'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                    gitProjectFilePath: $.trim($("#gitProjectFilePath").val()),
                    statusStr: status,
                    startTimeFrom: $("#runStartTimeFrom").val(),
                    startTimeTo: $("#runStartTimeTo").val(),
                    endTimeFrom: $("#runEndTimeFrom").val(),
                    endTimeTo: $("#runEndTimeTo").val()
                },
                colModel: _colModel,
                viewrecords: true,
                rowList: [5, 10, 20, 50, 100],
                rowNum: 10,
                pager: pager_selector,
                altRows: true,
                width: '100%',
                autowidth: true,
                autoencode: true,
                height: "100%",
                shrinkToFit: true,
                rownumbers: true,
                scrollOffset: 6,
                loadComplete: function (data) {
                    jqGrid.initWidth(jQuery, '#run-grid-table', "#run-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#run-grid-table").setGridHeight($("#run-jd-table-parent").height() - 70);
                }
            });
        }

    };
    $("#run-history").data("init", runHistory);
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
    var checkTimeFormat = function(time) {
        var r = time.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/);
        if(r==null)
            return false;
        var d = new Date(r[1], r[3]-1,r[4],r[5],r[6],r[7]);
        return (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]&&d.getHours()==r[5]&&d.getMinutes()==r[6]&&d.getSeconds()==r[7]);
    };
    var validateOperateTimeRange = function(elem, fromElem, toElem) {
        // $("#script-operate-history-validator").text("");
        elem.text("");

        var startTime = fromElem.val();
        var startTimeIsValid = checkTimeFormat(startTime);
        if (startTime !== "" && !startTimeIsValid) {
            elem.text("起始时间格式错误");
            console.error("起始时间格式错误");
            return false;
        }

        var endTime = toElem.val();
        var endTimeIsValid = checkTimeFormat(endTime);
        if (endTime !== "" && !endTimeIsValid) {
            elem.text("终止时间格式错误");
            console.error("终止时间格式错误");
            return false;
        }

        if (startTime !== "" && endTime !== "" && endTime < startTime) {
            elem.text("起始时间不能晚于终止时间");
            console.error("起始时间不能晚于终止时间");
            return false;
        }
        return true;
    };
    $("#gitProjectFilePath,#runStartTimeFrom,#runStartTimeTo,#runEndTimeFrom,#runEndTimeTo,input.script-run-history-checkbox").on("change", function () {
        // 检验开始时间和结束时间
        var startValidate = validateOperateTimeRange($("#script-run-history-start-validator"), $("#runStartTimeFrom"), $("#runStartTimeTo"));
        var endValidate = validateOperateTimeRange($("#script-run-history-end-validator"), $("#runEndTimeFrom"), $("#runEndTimeTo"));
        if (!startValidate || !endValidate) {
            return;
        }

        var status = "";
        $("input.script-run-history-checkbox:checked").each(function (index, element) {
            status += "," + $(element).val();
        })
        jQuery("#run-grid-table").jqGrid("setGridParam", {
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                gitProjectFilePath: $.trim($("#gitProjectFilePath").val()),
                statusStr: status,
                startTimeFrom: $("#runStartTimeFrom").val(),
                startTimeTo: $("#runStartTimeTo").val(),
                endTimeFrom: $("#runEndTimeFrom").val(),
                endTimeTo: $("#runEndTimeTo").val()
            }
        })
    })

    $("#run-history-query").click(function () {
        // 检验开始时间和结束时间
        var startValidate = validateOperateTimeRange($("#script-run-history-start-validator"), $("#runStartTimeFrom"), $("#runStartTimeTo"));
        var endValidate = validateOperateTimeRange($("#script-run-history-end-validator"), $("#runEndTimeFrom"), $("#runEndTimeTo"));
        if (!startValidate || !endValidate) {
            return;
        }

        jQuery("#run-grid-table").jqGrid('setGridParam', {
            page: 1
        }).trigger("reloadGrid");

    })
    $("#run-jd-table-parent").on("click", "span.run-data-log", function (event) {
        var runDetailId = $(this).attr("data-index");
        var dataLogType = $(this).attr("data-type");
        $.dialog.open(getDataLogUrl + "?runDetailId=" + runDetailId + "&dataLog=" + dataLogType, {
            title: "日志",
            lock: true,
            width: "70%",
            height: "80%",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
    })
    $("#run-jd-table-parent").on("click", "span.stop-script", function (event) {
        var runDetailId = $(this).attr("data-index");
        if(getRunMode() == 1 && runDetailId == debug.detailId){
            debug.endDebug(runDetailId,function () {
                jQuery("#run-grid-table").trigger("reloadGrid");
            });
        }else {
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/stop.ajax", {runDetailId: runDetailId}, $(this), function (node, data) {
                jQuery("#run-grid-table").trigger("reloadGrid");
            })
        }

    })
    $("#run-jd-table-parent").on("click", "span.run-script-version", function (event) {
        if(!$(this).hasClass("tmp-version")){
            var version = $(this).attr("data-version");
            openScriptContent($("#gitProjectId").val(),$("#gitProjectFilePath").val(),version);
        }
    })

})

function openScriptContent(gitProjectId,gitProjectFilePath,version) {
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
    $.dialog.data("openScriptContent",openScriptContent);
}

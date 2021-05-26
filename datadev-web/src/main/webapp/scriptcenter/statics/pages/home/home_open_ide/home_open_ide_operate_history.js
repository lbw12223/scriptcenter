$(function () {
    var operateHistory = {};
    var checkTimeFormat = function(time) {
        var r = time.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/);
        if(r==null)
            return false;
        var d = new Date(r[1], r[3]-1,r[4],r[5],r[6],r[7]);
        return (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]&&d.getHours()==r[5]&&d.getMinutes()==r[6]&&d.getSeconds()==r[7]);
    };
    $('#operateStartTime').datetimepicker({
        autoclose: true,
        language: "zh-CN",
        minView: 0,
        minuteStep: 5,
        format: 'yyyy-mm-dd HH:ii:00',
        todayBtn: true,
        clearBtn: true
    });
    $('#operateEndTime').datetimepicker({
        autoclose: true,
        language: "zh-CN",
        minView: 0,
        minuteStep: 5,
        format: 'yyyy-mm-dd HH:ii:59',
        todayBtn: true,
        clearBtn: true
    });
    var validateOperateTimeRange = function() {
        $("#script-operate-history-validator").text("");

        var startTime = $("#operateStartTime").val();
        var startTimeIsValid = checkTimeFormat(startTime);
        if (startTime !== "" && !startTimeIsValid) {
            $("#script-operate-history-validator").text("起始时间格式错误");
            console.error("起始时间格式错误");
            return false;
        }

        var endTime = $("#operateEndTime").val();
        var endTimeIsValid = checkTimeFormat(endTime);
        if (endTime !== "" && !endTimeIsValid) {
            $("#script-operate-history-validator").text("终止时间格式错误");
            console.error("终止时间格式错误");
            return false;
        }

        if (startTime !== "" && endTime !== "" && endTime < startTime) {
            $("#script-operate-history-validator").text("起始时间不能晚于终止时间");
            console.error("起始时间不能晚于终止时间");
            return false;
        }
        return true;
    };
    $("#gitProjectFilePath,#operateStartTime,#operateEndTime,#scriptOperateMo").on("change", function () {
        if (!validateOperateTimeRange()) {
            return;
        }

        jQuery("#operate-grid-table").jqGrid("setGridParam", {
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                gitProjectFilePath: $.trim($("#gitProjectFilePath").val()),
                startTime: $("#operateStartTime").val(),
                endTime: $("#operateEndTime").val(),
                creator: $("#scriptOperateMo").val()
            }
        })
    })

    //datadev_editor_common._int($("#scriptOperateMo"), false);
    
    $("#operate-history-query").click(function () {
        console.log("#operate-history-query click");
        if (!validateOperateTimeRange()) {
            console.error("保存时间 参数校验失败");
            return;
        }
        jQuery("#operate-grid-table").jqGrid('setGridParam',{
            page:1
        }).trigger("reloadGrid");
    })
    operateHistory.isInit = false;
    operateHistory.init = function initData() {
        initSelect();
        if(this.isInit){
            jQuery("#operate-grid-table").jqGrid('setGridParam',{
                page:1
            }).trigger("reloadGrid");
        }else {
            this.isInit = true;
            var _colModel = [
                {
                    name: 'version',
                    label: "版本号",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        var str = '<span class="run-item run-script-version" data-version="' + record.version + '"  >' + record.version + '</span>';
                        return str;
                    }
                },
                {
                    name: 'creator',
                    label: "修改人",
                    sortable: false,
                },
                {
                    name: 'scriptOperatorTypeStr',
                    label: "操作类型",
                    sortable: false,
                },
                {
                    name: 'created',
                    label: "保存时间",
                    sortable: false,
                },
            ];
            var pager_selector = "#operate-grid-pager";
            jQuery("#operate-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/scriptFile/getOperateHistory.ajax',
                mtype: 'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                    gitProjectFilePath: $.trim($("#gitProjectFilePath").val()),
                    startTime: $("#operateStartTime").val(),
                    endTime: $("#operateEndTime").val(),
                    creator: $("#scriptOperateMo").val()
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
                    jqGrid.initWidth(jQuery, '#operate-grid-table', "#operate-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#operate-grid-table").setGridHeight($("#operate-jd-table-parent").height() - 70);
                }
            });
        }

    }
    $("#operate-history").data("init", operateHistory);


    var gitProjectId = $.trim($("#gitProjectId").val());
    var editorUrl = "/scriptcenter/project/gitMembers.ajax?gitProjectId=" + gitProjectId;

    function initSelect() {
        $("#scriptOperateMo").empty();
        commonAjaxEvents.commonPostAjax(editorUrl, {}, $("#scriptOperateMo"), function (node, data) {
            if (data.code == 0 && data.obj) {
                var options = "<option value=''></option>";
                for (var index = 0; index < data.obj.length; index++) {
                    var node = data.obj[index];
                    options += "<option value='" + node.gitMemberUserName + "' >" + node.systemUserName + "</option>"
                }
                $("#scriptOperateMo").append(options);
                $("#scriptOperateMo").select2(
                    {placeholder: '请选择修改人'}
                );
            }
        })
        // $("#scriptOperateMo").on("select2-loaded",function (ar1,ar2) {
        //     var uls=$("ul.select2-results:visible");
        //     if(uls.length>0){
        //         for(var i=0;i<uls.length;i++){
        //             var scrollBar=Scrollbar.get(uls[i]);
        //             scrollBar && scrollBar.update();
        //         }
        //     }
        //
        // })
    }

    $("#operate-grid-table").on("click", "span.run-script-version", function (event) {
        var version = $(this).attr("data-version");
        openScriptContent($("#gitProjectId").val(),$("#gitProjectFilePath").val(),version);
    })

})



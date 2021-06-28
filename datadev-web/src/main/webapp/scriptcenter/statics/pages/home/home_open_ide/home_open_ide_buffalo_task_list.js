$(function () {
    var buffaloTaskList = {};
    $("#buffalo_task_key_word,#buffalo_task_type,#buffalo_appgroup").on("change", function () {
        jQuery("#buffalo-list-grid-table").jqGrid("setGridParam", {
            postData: {
                taskVersion: $("#buffalo_task_type").val(),
                buffaloKeyWord: $("#buffalo_task_key_word").val(),
                appGroupId: $("#buffalo_appgroup").val(),
                gitProjectId: $("#gitProjectId").val(),
                gitProjectFilePath: $("#gitProjectFilePath").val()
            }
        })
    })

    $("#buffalo_task_list_query").click(function () {
        jQuery("#buffalo-list-grid-table").jqGrid('setGridParam', {
            page: 1
        }).trigger("reloadGrid");
    })
    initSelect();
    buffaloTaskList.isInit = false;
    buffaloTaskList.init = function initData() {
        if (buffaloTaskList.isInit) {
            jQuery("#buffalo-list-grid-table").jqGrid('setGridParam', {
                page: 1
            }).trigger("reloadGrid");
        } else {
            this.isInit = true;
            var _colModel = [
                {
                    name: 'datadevVersion',
                    label: "版本号",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        var str = '<span href="javascript:void(0);" class="run-item run-script-version" data-version="' + cellvalue + '"  >' + cellvalue + '</span>';
                        return str;
                    },
                    width: 60
                },
                {
                    name: 'taskVersionStr',
                    label: "产品名称",
                    sortable: false,
                    width: 80
                },
                {
                    name: 'appGroupName',
                    label: "项目空间",
                    sortable: false,
                    width: 100
                },
                {
                    name: 'approveStatusStr',
                    label: "任务审批状态",
                    sortable: false,
                    width: 80
                },
                {
                    name: 'taskName',
                    label: "任务名称",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        if (record.taskVersion == 1) {
                            return "<span data-link='" + _bdpDomain + "/buffalo4/task/detail.html?taskId=" + record.taskId + "'class='run-item buffalo-task-detail' >" + cellvalue + "</span>";
                        } else {
                            return "<span data-link='" + _bdpDomain + "/buffalo/task/detail.html?id=" + record.taskId + "&businessType=001&taskName=" + cellvalue + "' class='run-item buffalo-task-detail' >" + cellvalue + "</span>";
                        }
                    },
                    width: 140

                },
                {
                    name: 'managersName',
                    label: "任务负责人",
                    sortable: false,
                    width: 80
                },
                {
                    name: 'description',
                    label: "描述",
                    sortable: false,
                    width: 80
                },
            ];
            var pager_selector = "#buffalo-list-grid-pager";
            jQuery("#buffalo-list-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/buffalo/getJqGridBuffaloTasks.ajax',
                mtype: 'POST',
                postData: {
                    taskVersion: $("#buffalo_task_type").val(),
                    buffaloKeyWord: $("#buffalo_task_key_word").val(),
                    appGroupId: $("#buffalo_appgroup").val(),
                    gitProjectId: $("#gitProjectId").val(),
                    gitProjectFilePath: $("#gitProjectFilePath").val(),
                    name: $("#fileName").val()
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
                    jqGrid.initWidth(jQuery, '#buffalo-list-grid-table', "#buffalo-list-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#buffalo-list-grid-table").setGridHeight($("#buffalo-list-jd-table-parent").height() - 70);
                }
            });
        }

    }
    $("#buffalo-task-list").data("init", buffaloTaskList);


    function initSelect() {
        $("#buffalo_task_type").select2({
            placeholder: '请选择产品类型',
            allowClear: true,
        });
        $("#buffalo_appgroup").empty();
        var options = "<option value=''></option>";
        var retObj = undefined;
        commonAjaxEvents.commonPostAjax("/scriptcenter/buffalo/getAppByErp.ajax", {}, $("#buffalo_appgroup"), function (node, data) {
            if (data.code == 0 && data.obj) {
                retObj = data.obj;
                for (var index = 0; index < data.obj.length; index++) {
                    var node = data.obj[index];
                    options += "<option value='" + node.appgroupId + "' >" + node.appgroupName + "</option>"
                }
            }
        }, function () {
            $("#buffalo_appgroup").append(options);
            var curProjectSpaceId = top.window.projectSpaceId;
            var defaultVal = curProjectSpaceId ? curProjectSpaceId : (retObj ? retObj[0].appgroupId : 0);
            $("#buffalo_appgroup").val(defaultVal).select2({
                placeholder: '请选择应用',
                allowClear: true,
            });
        })
    }

    $("#buffalo-list-jd-table-parent").on("click", "span.run-script-version", function (event) {
        var version = $(this).attr("data-version");
        openScriptContent($("#gitProjectId").val(),$("#gitProjectFilePath").val(),version);
    })
    $("#buffalo-list-jd-table-parent").on("click", "span.buffalo-task-detail", function (event) {
        var url = $(this).attr("data-link");
        window.open(url)
    })
})



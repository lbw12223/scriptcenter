// save :保存文件  saveAs：另存为  upFile：上传文件  move：移动文件   dir：创建目录
$(function () {
    var spin = undefined;
    var gitProjectId = undefined;
    var gitProjectFilePath = undefined;
    var version = undefined;
    var buffaloAddJobUrl = undefined;
    var buffaloLookJobUrl = undefined;
    var buffaloScriptWorkFlowUrl = undefined;
    var applicationId = undefined;
    var relaveScriptStatus = 0;
    var scriptId = undefined;
    var preAppGroupId = undefined;
    var scriptType = undefined;
    var uplineBuffaloTaskList = {};
    //提交上线
    $("#uplineOk").click(function (event) {
        var valid = $('#upline-form').valid();
        if (valid) {
            var option = $("option:selected", $("#uplineAppSelect"));
            if (option.length == 1) {
                $("#upLineModal").modal("hide");
                spin = wait();
                applicationId = option.attr("data-id");
                commonAjaxEvents.commonSpinPostAjax("/scriptcenter/buffalo/upLineScript.ajax", {
                    gitProjectId: gitProjectId,
                    gitProjectFilePath: gitProjectFilePath,
                    applicationId: applicationId,
                    version: version,
                    description: $("#upLineDescription").val(),
                    relaveScriptStatus: relaveScriptStatus,
                    scriptId: scriptId

                }, null, function (node, data) {
                    $("#upLineModal").modal("show");
                    $(".after-upline", $("#upLineModal")).show();
                    $(".before-upline", $("#upLineModal")).hide();
                    $("#afterUpLineButtons").show();
                    $("#beforeUpLineButtons").hide();
                    var activeWindow=datadevInit.getActiveWindow(getKey(gitProjectId,gitProjectFilePath));
                    if(activeWindow && activeWindow.win && activeWindow.win.removeVersionChange){
                        activeWindow.win.removeVersionChange();
                    }
                    if (data.obj.buffaloScriptWorkFlowUrl) {
                        buffaloScriptWorkFlowUrl = data.obj.buffaloScriptWorkFlowUrl;
                        buffaloAddJobUrl = data.obj.buffaloAddJobUrl;
                        $("#checkApprovalInfo").show();
                        $("#checkTestJobInfo").hide();
                        $(".after-upline-tips", $("#upLineModal")).hide();
                        $(".after-online", $("#upLineModal")).show();
                    } else {
                        $("#checkApprovalInfo").hide();
                        $("#checkTestJobInfo").show();
                        $(".after-upline-tips", $("#upLineModal")).hide();
                        if(data.obj.isFirst == "true"){
                            $("#afterUplineNotFirstTips").hide();
                        }else {
                            $("#afterUplineNotFirstTips").show();
                        }
                        $(".after-test", $("#upLineModal")).show();
                        buffaloLookJobUrl = data.obj.buffaloLookJobUrl + "?scriptName=" + (data.obj.scriptName || "") + "&appGroupId=" + applicationId ;
                        buffaloAddJobUrl = data.obj.buffaloAddJobUrl;
                    }
                }, spin);
            }
        }
    })
    $("#checkApprovalInfo").click(function () {
        if (buffaloScriptWorkFlowUrl) {
            window.open(buffaloScriptWorkFlowUrl);
        } else {
            $.errorMsg("请求出错");
        }
    })
    $("#checkTestJobInfo").click(function () {
        if (buffaloLookJobUrl) {
            window.open(buffaloLookJobUrl);
        } else {
            $.errorMsg("请求出错");
        }
    })
    $("#createBuffolaJob").click(function () {
        if (buffaloAddJobUrl && gitProjectFilePath && gitProjectId) {
            var path = encodeURIComponent(encodeURIComponent(gitProjectFilePath));
            window.open(buffaloAddJobUrl + "?dataDevProjectId=" + gitProjectId + "&dataDevProjectFilePath=" + path + "&appGroupId=" + applicationId + "&actionType=" + (scriptType * 1 == 1 ? "datasql":"pyscript"));
        } else {
            $.errorMsg("请求出错");
        }
    })

    $('#upline-form').validate({
        rules: {
            description: {
                required: true,
                maxlength: 255
            },
            uplineAppSelect: {
                required: true,
            }
        },
        messages: {
            description: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            uplineAppSelect: {
                required: "请选择项目空间！",
            }
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            error.appendTo(element.parent());
        }
    });
    $("#upLineModal").modal({
        backdrop: 'static',
        show: false,
        keyboard: false
    });
    $("#upLineDuplicationModal").modal({
        backdrop: 'static',
        show: false,
        keyboard: false
    });

    function initTaskListJqgrid(appId) {
        if (uplineBuffaloTaskList.isInit) {
            if (appId) {
                jQuery("#upline-buffalo-list-grid-table").jqGrid('setGridParam',{
                    page:1,
                    datatype: 'json'
                }).trigger("reloadGrid");
            } else {
                jQuery("#upline-buffalo-list-grid-table").clearGridData();
            }
        } else {
            var dataType = "json";
            if (!appId) {
                dataType = "local";
            }
            uplineBuffaloTaskList.isInit = true;
            var _colModel = [
                {
                    name: 'taskName',
                    label: "任务名称",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        if(record.taskVersion==1){
                            return "<span data-link='"+_bdpDomain+"/buffalo4/task/detail.html?taskId="+ record.taskId + "' class='buffalo-task-detail' style='text-decoration: underline;cursor: pointer'>" + cellvalue + "</span>";
                        }else {
                            return "<span data-link='"+_bdpDomain+"/buffalo/task/detail.html?id="+record.taskId+"&businessType=001&taskName="+ cellvalue + "' class=' buffalo-task-detail' style='text-decoration: underline;cursor: pointer'>" + cellvalue + "</span>";
                        }
                    },
                    width:253
                },
                {
                    name: 'approveStatusStr',
                    label: "任务审批状态",
                    sortable: false,
                    width:94
                },
                {
                    name: 'managersName',
                    label: "任务负责人",
                    sortable: false,
                    width:120
                }
            ];
            var pager_selector = "#upline-buffalo-list-grid-pager";
            jQuery("#upline-buffalo-list-grid-table").jqGrid({
                datatype: dataType,
                url: '/scriptcenter/buffalo/getJqGridBuffaloTasks.ajax',
                mtype: 'POST',
                postData: {
                    name: $("#upLineScriptName").val(),
                    appGroupId: $("#uplineAppSelect").val(),
                },
                colModel: _colModel,
                viewrecords: true,
                rowList: [5, 10, 20, 50, 100],
                rowNum: 5,
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
                    jqGrid.initWidth(jQuery, '#upline-buffalo-list-grid-table', "#upline-buffalo-list-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#upline-buffalo-list-grid-table").setGridHeight($("#upline-buffalo-list-jd-table-parent").height() - 70);
                }
            });
        }
    }

    function initJobNum(callback) {
        jQuery("#upline-buffalo-list-grid-table").jqGrid("setGridParam", {
            postData: {
                name: $("#upLineScriptName").val(),
                appGroupId: $("#uplineAppSelect").val()
            }
        })
        var option = $("option:selected", $("#uplineAppSelect"));
        var appId = option.val();
        if (appId) {
            commonAjaxEvents.commonPostAjax("/scriptcenter/buffalo/getJobNum.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath,
                applicationId: appId,
                scriptName: $("#upLineScriptName").val()
            }, null, function (node, data) {
                callback && callback();
                if (data.obj.buffaloInfo) {
                    var buffaloInfo = data.obj.buffaloInfo;
                    scriptId = buffaloInfo.buffaloScriptId;
                    if (!buffaloInfo.gitProjectId && !buffaloInfo.gitProjectFilePath) {
                        initTaskListJqgrid(appId);
                        $("#upLineDuplicationModal").modal("show");
                    } else if (!(buffaloInfo.gitProjectId == gitProjectId && buffaloInfo.gitProjectFilePath == gitProjectFilePath)) {
                        relaveScriptStatus = 0;
                        $("#uplineAppSelect").val("").trigger("change");
                        $.errorMsg("项目空间【" + data.obj.appGroupName + "】下有同名脚本，请修改项目空间或脚本名称")
                    } else {
                        //已经同步过，并且是正确的脚本
                        initTaskListJqgrid(appId);
                    }
                } else {
                    //从未同步过
                    scriptId = undefined;
                    initTaskListJqgrid(appId);
                }
            }, function () {
                $("#uplineOk").removeClass("disabled");
                $("#uplineAppSelect").removeClass("disabled");
            })
        } else {
            callback && callback();
            //为空时 如果没有初始化jqgrid  先初始化
            initTaskListJqgrid(undefined);
        }
    }

    $("#relateScript").click(function () {
        relaveScriptStatus = 1;
        $("#upLineDuplicationModal").modal("hide");
    })
    $("#upLineDuplicationModal").on("show.bs.modal", function () {
        relaveScriptStatus = 0;
    })
    $("#upLineDuplicationModal").on("hide.bs.modal", function () {
        if (relaveScriptStatus == 0) {
            $("#uplineAppSelect").val("").trigger("change")
        }
    })
    $("#uplineAppSelect").on("change",function () {
        initJobNum();
    })
    $("#upline-buffalo-list-jd-table-parent").on("click", "span.buffalo-task-detail", function (event) {
        var url = $(this).attr("data-link");
        window.open(url)
    })

    $.fn.upLine = function (id, path, name, vers, type ) {
        gitProjectId = id;
        gitProjectFilePath = path;
        version = vers;
        relaveScriptStatus = 0;
        scriptType = type;

        $("#upLineModal .bdp-help-block").remove();
        $(".after-upline", $("#upLineModal")).hide();
        $(".before-upline", $("#upLineModal")).show();
        $("#afterUpLineButtons").hide();
        $("#beforeUpLineButtons").show();
        $("#upLineScriptName").val(name);
        $("#upLineOnlineJobNum").val(0);
        $("#upLineTestJobNum").val(0);
        $("#upLineDescription").val("");


        commonAjaxEvents.commonPostAjax("/scriptcenter/buffalo/getAppByErp.ajax", {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath
        }, null, function (node, data) {
            var apps = data.obj;
            var options = "<option value=''></option>";
            if (apps && apps.length) {
                for (var index = 0; index < apps.length; index++) {
                    var selected = apps[index].defaultStatus == 1 ? "selected='selected'" : "";
                    options += "<option data-id='" + apps[index].appgroupId + "' value='" + apps[index].appgroupId + "'" + selected + ">" + apps[index].appgroupName + "</option>";
                }
            }
            $("#uplineAppSelect").empty().append(options);
            $("#uplineAppSelect").select2({
                placeholder: "请选择"
            }).on("select2-opening", function () {
                $("#uplineOk").addClass("disabled");
                $("#uplineAppSelect").addClass("disabled");
                preAppGroupId = $('#uplineAppSelect').val()
            }).on("select2-close", function () {
                var nowAppGroupId = $('#uplineAppSelect').val();
                if (preAppGroupId == nowAppGroupId) {
                    $("#uplineOk").removeClass("disabled");
                    $("#uplineAppSelect").removeClass("disabled");
                }
            })
            initJobNum(function () {
                $("#upLineModal").modal("show");
            });


        })
    }
})

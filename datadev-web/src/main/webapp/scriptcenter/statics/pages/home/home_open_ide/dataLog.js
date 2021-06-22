var _colModel = [];
$(function () {
    var initInfoStatus = false;
    var initLogStatus = false;
    var initDataStatus = false;
    var editor = initAce();
    var getLogUrl = "/scriptcenter/script/runTimeLog.ajax";
    var internal = undefined;
    var currentLogNum = 1;
    window.onbeforeunload = null;
    $("#dataLogContainer").JdDataDevTab({
        afterClickCallBack: function (event, obj, args, li) {
            if (li.attr("target") == "runDetailDiv") {
                initInfo();
            } else if (li.attr("target") == "logDiv") {
                initLog();
            } else if (li.attr("target") == "dataDiv") {
                initResultData();
            }
        }
    });
    var activeIndex = $("#dataLog").val() || 0
    $("#dataLogContainer").JdDataDevTab("active", activeIndex);


    function initAce() {
        //初始化对象
        var editor = ace.edit("logCode");
        //设置风格和语言（更多风格和语言，请到github上相应目录查看）
        // var theme = "tomorrow_night"
        // editor.setTheme("ace/theme/" + theme);
        var color = HOME_COOKIE.getColorCookie()

        var theme = "chrome";
        editor.setTheme("ace/theme/" + theme);
        editor.session.setMode("ace/mode/text");
        //字体大小
        editor.setFontSize(12);
        //设置只读（true时只读，用于展示代码）
        editor.setReadOnly(true);

        //自动换行,设置为off关闭
        editor.setOption("wrap", "free");

        //启用提示菜单
        // ace.require("ace/ext/language_tools");
        editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: true,
            enableLiveAutocompletion: true
        });
        editor.setValue("");
        editor.focus()
        return editor;

    }

    function initLog() {
        if (!initLogStatus) {
            initLogStatus = true;
            getLog();
        }
    }

    function getLog() {
        $.ajax({
            url: getLogUrl,
            data: {
                runDetailId: $("#runDetailId").val(),
                currentLogIndex: currentLogNum
            },
            type: "post",
            dataType: 'json',
            success: function (data) {
                if (data && data.obj && data.code == 0) {
                    var logs = data.obj.logs;
                    if (logs.length == 0) {
                    } else {
                        var string = logs.join("\n") + "\n";
                        currentLogNum += logs.length;
                        editor.session.insert({row: currentLogNum, column: 0}, string);
                    }
                    if (!data.obj.isLastLog) {
                        setTimeout(getLog, 1000);
                    }
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                window.clearInterval(internal);
                if (parent.$) {
                    //parent.$.errorMsg("出现异常,请联系管理员!");
                } else {
                    //$.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    }

    function initInfo() {
        if (!initInfoStatus) {
            initInfoStatus = true;
            commonAjaxEvents.commonPostAjax("/scriptcenter/home/basicInfo.ajax", {
                runDetailId: $("#runDetailId").val()
            }, null, function (node, data) {
                if (data) {
                    var runDetailObj = data;
                    $("#script-detail-project").text(runDetailObj.gitProjectPath || "--")
                    $("#script-detail-file-path").text(runDetailObj.gitProjectFilePath || "--")
                    $("#script-run-id").text(runDetailObj.id || "--")
                    $("#script-run-operator").text(runDetailObj.operator || "--")
                    $("#script-run-startTime").text(runDetailObj.startTime || "--")
                    $("#script-run-stopTime").text(runDetailObj.endTime || "--")
                    $("#script-run-totalTime").text(runDetailObj.timePeriod || "--")
                    $("#script-run-status").text(runDetailObj.statusStr || "--")
                    $("#script-run-cluster").text(runDetailObj.clusterCode || "--")
                    $("#script-run-market").text(runDetailObj.marketLinuxUser || "--")
                    $("#script-run-account").text(runDetailObj.accountCode || "--")
                    $("#script-detail-queue").text(runDetailObj.queueCode || "--")
                    if ($("#scriptType").val() == 4) {
                        $("#script-start-shell-path").text(runDetailObj.startShellPath || "--")
                    }
                    if ($("#scriptType").val() == 1) {
                        $("#script-engine").text(runDetailObj.engineType || "--")
                    }
                    var argsShow = "";
                    try {
                        var args = runDetailObj.args;
                        var argsObj = JSON.parse(args);
                        var index = 1;
                        while (argsObj[index] != undefined) {
                            argsShow += "参数" + index + " : " + (argsObj[index] || "") + "\n";
                            index++;
                        }
                    } catch (e) {
                    }
                    $("#script-detail-args").val(argsShow || "--")
                }
            })
        }

    }
})





'use strict';
var LOG_URI = "/scriptcenter/script/runTimeLog.ajax";
var DATA_URI = "/scriptcenter/home/home_open_ide_runData.html?runDetailId=";
var START_MOVE_LOG_OR_DATASPLITHANDER = false;
var START_MOVE_LOG_OR_DATASPLITHANDER_PAGE_Y = 0;

var code = $("#code");
var logOrDataSplit = $(".logOrDataSplit");
var logOrDataParent = $(".logOrDataParent");

var startCodeBottom = 0;
var startLogOrDataSplitBottom = 0;
var startLogOrDataParentHeight = 0;
var startAceContentHeight = 0;
var logStayStatus = false;
var removeStatus = false;
var logMaxNum = 5000;

var windowParams = {
    isHasResult: false
}

var hasOpendResult = "";

function hideClearLog() {
    $(".clearLog").hide();
}

function clickActiveDataLi() {
    var activeTab = $("#logOrData").JdDataDevTab("getCurrentActiveTab");
    if (activeTab && activeTab.activeLi) {
        $(activeTab.activeLi).click();
    }
}

function getParentRunStatusMap() {
    return parent.runStatusMap;
}

function modifyRunStatus(runDetailId, key) {
    var map = getParentRunStatusMap();
    if (map && map.get(runDetailId) == 0) {
        //notify run success
        parent.resetAllTopButton && parent.resetAllTopButton(parent.datadevInit.getActiveWindow(key));
        switchStartAndStop(1);
        initMode("unRun-mode");
        map.put(runDetailId, 1);
        resetVersionChange();
    }
}

function appendLastLog(runDetailId) {
    commonAjaxEvents.commonPostAjax("/scriptcenter/script/getLastLog.ajax", {
        runDetailId: runDetailId,
    }, null, function (_node, data) {
        var logContainer = $("#run-log-content");
        if (data.obj) {
            var oriRunDetailId = data.obj.runDetailId;
            var nowRunDetailId = $("#scriptRunDetailId").val();
            if (oriRunDetailId != nowRunDetailId) {
                return;
            }
            var log = data.obj.log;
            log = htmlUtil.htmlEncode(log).replace(/ /g, "&nbsp;");
            var p = $("<p style='word-wrap:break-word ;'>");
            p.html(log);
            logContainer.append(p);
            if (!logStayStatus) {
                logContainer.scrollTop(logContainer[0].scrollHeight);
            }
        }
    })
}

var htmlUtil = {
    encodeNode: undefined,
    htmlEncode: function (html) {
        //1.首先动态创建一个容器标签元素，如DIV
        if (!this.encodeNode) {
            this.encodeNode = document.createElement("div");
        }
        //2.然后将要转换的字符串设置为这个元素的innerText(ie支持)或者textContent(火狐，google支持)
        (this.encodeNode.textContent != undefined) ? (this.encodeNode.textContent = html) : (this.encodeNode.innerText = html);
        //3.最后返回这个元素的innerHTML，即得到经过HTML编码转换的字符串了
        var output = this.encodeNode.innerHTML;
        return output;
    }
}

function showClearLog(event) {
    if (event.which == 3) {
        var parentNode = $("#logAndRunResult");
        var left = event.pageX - parentNode.position().left * 1 + 10;
        var top = event.pageY - parentNode.position().top * 1 + 10;
        hiddenMoreFunction();
        $(".clearLog").css("left", left + "px").css("top", top + "px").css("display", "block");
    }
}

function addLog(log) {
    var logContainer = $("#run-log-content");
    if (!removeStatus) {
        var nowLogNum = $("p", logContainer).length;
        removeStatus = nowLogNum >= logMaxNum ? true : false;
    }
    var p = $("<p style='word-wrap:break-word ;'>");
    log = htmlUtil.htmlEncode(log).replace(/ /g, "&nbsp;").replace(/\n/g, "<br/>").replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    p.html(log);
    logContainer.append(p);
    if (removeStatus) {
        $("p:first", logContainer).remove();
    }
    if (!logStayStatus) {
        logContainer.scrollTop(logContainer[0].scrollHeight);
    }
}

/**
 * 返回当前的日志，放在array里面
 */
function getLogs() {
    let logContainer = $("#run-log-content");
    let logPs = $("p", logContainer);
    let result = new Array();
    for (let index = 0; index < logPs.length; index++) {
        let log = $(logPs[index]).text();
        result.push(log);
    }
    return result;
}

function selectAllLog() {
    var logPs = $("#run-log-content p");
    logPs.addClass("selected");
}

function copyLogs() {
    var logPs = $("#run-log-content p.selected");
    var logs = "";
    if (logPs.length > 0) {
        for (var index = 0; index < logPs.length; index++) {
            logs += $(logPs[index]).text() + "\n";
        }
        const input = document.createElement('textarea');
        input.textContent = logs;
        document.body.appendChild(input);
        input.focus();
        input.setSelectionRange(0, input.value.length);
        if (document.execCommand('copy')) {
            document.execCommand('copy');
        }
        document.body.removeChild(input);
    }
}

function cancelSelectLogs() {
    var logPs = $("#run-log-content p");
    logPs.removeClass("selected");
}

var homeOpenPageEvent = {
    initPage: function () {
        $("#rightDiv").JdDataDevTab({
            changeClickCallBack: function (event, _this, li, tabContent) {
                if (li.hasClass("active")) {
                    $("#rightDiv").JdDataDevTab("unActive", li);
                    return false;
                }
                var target = $(li).attr("target");
                if ($("#" + target).length > 0) {
                    var init = $("#" + target).data("init");
                    if (init && init.init) {
                        init.init();
                    }
                }
                return true;
            }
        });
        $("#logOrData").JdDataDevTab({
            hasFullScreen: true,
            fullScreenClick: function (node) {
                var span = $("span", node);
                if (span.hasClass("ide-icon-zk")) {
                    // 全屏
                    var orignData = {
                        codeBottom: code.css("bottom"),
                        logOrDataParentHeight: logOrDataParent.height(),
                        logOrDataSplitBottom: logOrDataSplit.css("bottom")
                    }
                    node.data("orignData", orignData);
                    code.css("bottom", "100%");
                    logOrDataParent.height("100%");
                    logOrDataSplit.css("bottom", "100%");
                    editor && editor.resize();
                    span.removeClass("ide-icon-zk").addClass("ide-icon-sq");
                    return;
                }
                if (span.hasClass("ide-icon-sq")) {
                    //缩小
                    var orignData = node.data("orignData");
                    code.css("bottom", orignData && orignData.codeBottom ? orignData.codeBottom : "160px");
                    logOrDataParent.height(orignData && orignData.logOrDataParentHeight ? orignData.logOrDataParentHeight : 150);
                    logOrDataSplit.css("bottom", orignData && orignData.logOrDataSplitBottom ? orignData.logOrDataSplitBottom : "160px");
                    editor && editor.resize();
                    span.removeClass("ide-icon-sq").addClass("ide-icon-zk");
                    return;
                }
            },
            afterClickCallBack: function (event, args, obj, li, tabContent) {
                var iframe = $("iframe", tabContent);
                if (iframe.length > 0) {
                    var childWin = $("iframe", tabContent)[0].contentWindow;
                    childWin && childWin.resizeJqGrid && childWin.resizeJqGrid();
                }
            }
        });

        $("#run-log-content").mouseenter(function () {
            logStayStatus = true;
        })
        $("#run-log-content").mouseleave(function () {
            logStayStatus = false;
        })

        $("#run-log-content").mousedown(function (event) {
            parent && parent.hiddenAllRightMeun && parent.hiddenAllRightMeun();
            showClearLog(event);
        })
        $("#clearLogEventLi").click(function () {
            $("#run-log-content").html("");
            hideClearLog();
        })


    },
    initPageSizeMouseMove: function (event) {
        if (START_MOVE_LOG_OR_DATASPLITHANDER) {

            var currentY = event.pageY;
            var changedY = START_MOVE_LOG_OR_DATASPLITHANDER_PAGE_Y - currentY;
            code.css("bottom", (changedY + startCodeBottom) + "px");
            logOrDataParent.height(changedY + startLogOrDataParentHeight);
            logOrDataSplit.css("bottom", (changedY + startLogOrDataSplitBottom) + "px");
            //修改editor 的高度
            editor && editor.resize();
            var height = $("#code").height();
            $("#depView").css({height: height});
        }
    },
    initPageSizeMouseUp: function (event) {
        START_MOVE_LOG_OR_DATASPLITHANDER = false;
    },
    initPageSizeChange: function () {

        $(document).mousemove(function (event) {
            homeOpenPageEvent.initPageSizeMouseMove(event);
        });
        $(document).on("mousedown", ".logOrDataSplit", function (event) {
            START_MOVE_LOG_OR_DATASPLITHANDER = true;
            START_MOVE_LOG_OR_DATASPLITHANDER_PAGE_Y = event.pageY;
            startCodeBottom = code.css("bottom").replace("px", "") * 1;
            startLogOrDataSplitBottom = logOrDataSplit.css("bottom").replace("px", "") * 1;
            startLogOrDataParentHeight = logOrDataParent.height() * 1;
            startAceContentHeight = $(".ace_content").height() * 1;

            event.stopPropagation();
            event.preventDefault();
        })
        $(document).mouseup(function (event) {
            homeOpenPageEvent.initPageSizeMouseUp(event);
        })

        $(document).click(function () {
            //hidden 右键弹出
            parent && parent.hiddenAllRightMeun && parent.hiddenAllRightMeun();
        })
    },
    waitRun: function (activeWindow) {
        var gitProjectId = $("#gitProjectId").val();
        var gitProjectFilePath = $("#gitProjectFilePath").val();
        activeWindow.win.switchStartAndStop(0);
        var start_message = "=============================================================================================";
        var logContainer = $("#run-log-content");
        logContainer.append($("<p>").html(start_message));
        var key = parent.getKey(gitProjectId, gitProjectFilePath);
        parent.disableAllTopButton && parent.disableAllTopButton(key);
        $("#chooseVersionBtn").addClass("bdp-disabled");
    },
    notifyLog: function (key, runDetailId, activeWindow) {
        windowParams.isHasResult = false;
        var logContainer = $("#run-log-content");
        initMode("run-mode");
        homeOpenPageEvent.loadRunTimeLog(1, logContainer, runDetailId, key, 0);
        $("#logOrData").JdDataDevTab("active", 0);
        parent.parentInitTopButton && parent.parentInitTopButton(key, parent.TOP_BUTTON.stop, true);
    },
    openResult: function (scriptRunId) {
        //新创建结果窗口
        windowParams.isHasResult = true;

        if (hasOpendResult.indexOf(scriptRunId) == -1) {
            var filterParam = $("#logOrData").JdDataDevTab("findInfoByParam", "scriptId", scriptRunId);
            if (!filterParam) {
                var li = $("ul.datadev-ul.dynamic-ul > li:last", $("#logOrData"));
                var resultIndex = 1;
                if (li.length > 0 && li.attr("data-id")) {
                    var params = $("#logOrData").JdDataDevTab("getTabInfo", li.attr("data-id"));
                    resultIndex = (params && params.params && params.params.key) || 0;
                    resultIndex++;
                } else {
                    resultIndex = $("#logOrData").JdDataDevTab("getSizeOfTab");
                }
                var params = {
                    url: DATA_URI + scriptRunId,
                    label: "结果" + resultIndex,
                    key: resultIndex,
                    canRemove: true,
                    scriptId: scriptRunId
                };

                $("#logOrData").JdDataDevTab("addTab", params);
                $("#logOrData").JdDataDevTab("activeLastOne");
            }

            hasOpendResult += "," + scriptRunId;
        }

    },
    loadRunTimeLog: function (currentLogIndex, _node, runDetailId, key, hasFinishRequestTimes) {
        runDetailId = runDetailId ? runDetailId : $("#scriptRunDetailId").val();
        if (runDetailId * 1 > 0) {
            commonAjaxEvents.runTimeLogPostAjax(LOG_URI, {
                runDetailId: runDetailId,
                currentLogIndex: currentLogIndex,
                hasFinishRequestTimes: hasFinishRequestTimes
            }, _node, function (_node, data) {

                var logContainer = _node;
                var logSize = 0;
                if (data.obj) {
                    if (data.obj && data.obj.logs && data.obj.logs.length > 0) {
                        var oriRunDetailId = data.obj.runDetailId;
                        var nowRunDetailId = $("#scriptRunDetailId").val();
                        if (oriRunDetailId != nowRunDetailId) {
                            return;
                        }
                        logSize = data.obj.logs.length;
                        for (var index = 0; index < data.obj.logs.length; index++) {
                            var log = (data.obj.logs[index] && data.obj.logs[index].length > 0) ? data.obj.logs[index] : "";
                            addLog(log);
                        }
                    }

                    //is sql && finish
                    if (data.obj.runDetail.type
                        && data.obj.runDetail.type * 1 == 1
                        && data.obj.runDetail.status * 1 == 0) {
                        hasFinishRequestTimes = hasFinishRequestTimes * 1 >= 0 ? ++hasFinishRequestTimes : 0;
                    }

                    if ((data.obj.isFinish + "") === "false" || (data.obj.isLastLog + "")  ==  "false" ) {
                        var map = getParentRunStatusMap();
                        if (!map || map.get(runDetailId) != 2) {
                            window.setTimeout((function () {
                                homeOpenPageEvent.loadRunTimeLog(currentLogIndex + logSize, logContainer, runDetailId, key, hasFinishRequestTimes);
                            }), 1000)
                        } else {
                            appendLastLog(runDetailId);
                        }
                    } else {
                        modifyRunStatus(runDetailId, key);
                    }
                    if ((data.obj.canOpenResult + "") === "true") {
                        homeOpenPageEvent.openResult(runDetailId);
                    }
                    //
                    if (data.obj.isErrorLog + "" == "true") {
                        parent.showAccountErrorByLog && parent.showAccountErrorByLog(key);
                        return;
                    }
                    if ((data.obj.isFinish + "") === "true") {
                        modifyRunStatus(runDetailId, key);
                    }
                } else {
                    window.setTimeout(function () {
                        homeOpenPageEvent.loadRunTimeLog(currentLogIndex, _node, runDetailId, key, hasFinishRequestTimes);
                    }, 2000)
                }

            }, function () {
                // 报错3秒后重试，每个日志最多重试3次
                var status = 0;
                if (parent.runStatusMap) {
                    status = parent.runStatusMap.get(runDetailId) || 0;
                }
                if (status == 0) {
                    window.setTimeout(function () {
                        console.error("日志重新获取：" + runDetailId + "：" + currentLogIndex + "：");
                        homeOpenPageEvent.loadRunTimeLog(currentLogIndex, _node, runDetailId, key, hasFinishRequestTimes);
                    }, 3000)
                }

            })
        }
    }

}

homeOpenPageEvent.initPage();
homeOpenPageEvent.initPageSizeChange();
$(document).contextmenu(function () {
    return false;
})

var firstShiftKeyTime = 0;
var clickShiftKeyTime = 0;
var isMac = (navigator.userAgent.indexOf('Mac') >= 0) ? true : false;

$(document).keyup(function (event) {
    if (event.keyCode == 16) {
        var currentTime = new Date().getTime();
        if (firstShiftKeyTime > 0) {
            clickShiftKeyTime++;
        }
        if (currentTime - firstShiftKeyTime > 1000) {
            firstShiftKeyTime = 0;
            clickShiftKeyTime = 0;
            return;
        }
        if (clickShiftKeyTime % 2 == 0) {
            parent && parent.showSearchScript();
            firstShiftKeyTime = 0;
            clickShiftKeyTime = 0;
        }
    }
})
$(document).keydown(function (event) {
    if (event.keyCode == 16) {
        if (firstShiftKeyTime == 0) {
            firstShiftKeyTime = new Date().getTime();
        } else if ((new Date().getTime() - firstShiftKeyTime) > 1000) {
            firstShiftKeyTime = new Date().getTime();
            clickShiftKeyTime = 0;
        }
    } else {
        firstShiftKeyTime = 0;
        clickShiftKeyTime = 0;
    }
    if ((!isMac && event.ctrlKey || isMac && event.metaKey) && event.keyCode == 65) {
        selectAllLog();
        return false;
    }
    if ((!isMac && event.ctrlKey || isMac && event.metaKey) && event.keyCode == 67) {
        copyLogs();
    }
})
$(document).click(function () {
    cancelSelectLogs();
})

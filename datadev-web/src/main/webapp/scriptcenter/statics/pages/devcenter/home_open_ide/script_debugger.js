
var editor;
// var shellInitStr = "#!/bin/bash\n"
// var python2InitStr = "#!/usr/bin/env python\n# -*- coding: utf-8 -*-\n";
// var python3InitStr = "#!/usr/bin/env python3\n# -*- coding: utf-8 -*-\n";
var getScriptUrl = "/scriptcenter/script/getScript.ajax";
var ztree = undefined;
var packDetailMap = new Map();
var rootPath = "root";
var gitProjectFilePath = $("#gitProjectFilePath").val();
var gitProjectId = $("#gitProjectId").val();
var runMode = false;//false:普通运行  true：debug运行
var initContentStatus = false; //脚本内容初始化
var isInitByChangeVersion = false;//判断脚本内容是否已经被多版本情况初始化过
var isLeftVersionChoice = false;
var skipInit = true;
var needShowArgs = false;
var languageTools;
var tipCallBack = undefined;
var isEditContent = true ;
//DataDevSqlTip
var dataDevSqlTip = new DataDevSqlTip();

var debug = {
    oldLineHightId: undefined,//ace 内部id用来取消掉高亮状态
    lineno: undefined,
    objId: 0,
    treeArray: new Array(),
    oriFilePath: undefined,
    ingFilePath: undefined,//正在debug的文件
    socket: undefined,
    Status: undefined,
    canOperate: undefined,
    detailId: undefined,
    nowBreakPointsStatus: true,
    needAddBreakPoints: new Array(),
    pythonVersion: 0,
    closeCallBack: undefined,
    errorCallBack: undefined,
    intervalId: undefined,
    constant: {
        READY: "Ready",
        START: "Start",
        STEP_NEXT: "StepNext",
        STEP_INTO: "StepInto",
        FINISH: "Finish",
        STOP: "Stop",
        SHOW_PARAM: "ShowParam",
        SHOW_FULL_PARAM: "ShowFullParam",
        ADD_BREAKPOINT: "AddBreakpoint",
        CANCEL_BREAKPOINT: "CancelBreakPoint",
        DISABLE_BREAKPOINT: "DisableBreakPoint",
        ENABLE_BREAKPOINT: "EnableBreakPoint",
        CONTINUE: "Continue",
        STEP_OUT: "StepOut",
        SHOW_CODE_LINE: "ShowCodeLine",
        LOG_MSG: "LogMsg",
        NETTY_READY: "NettyReady",
        CLOSE: "Close",
        RESTART: "Restart",
        EXCEPTION: "Exception",
        DO_NOTHING: "DoNothing",
    },
    showParamKeyPathMap: new Map(),
    paramTree: undefined,
    treeSetting: {
        view: {
            dblClickExpand: false,
            showLine: false,
            selectedMulti: false,
            initPadding: 25,
            initPaddingLeft: 10,
            nameIsHTML: true,
            showTitle: false
        },
        data: {
            simpleData: {
                enable: true,
                idKey: "path",
                pIdKey: "parentPath",
                rootPId: ""
            },
            keep: {
                parent: true,
                leaf: true
            },
        },
        callback: {
            beforeClick: function (treeId, treeNode) {
                if (treeNode && treeNode.isParent) {
                    debug.paramTree.expandNode(treeNode, undefined, undefined, undefined, true);
                }
            },
            beforeExpand: function (treeId, treeNode) {
                if (!treeNode.hasShowParam) {
                    treeNode.hasShowParam = true;
                    var paramKey = "";
                    var parentNodes = treeNode.getPath();
                    if (parentNodes.length > 0 && parentNodes[0].isDefaultNode) {
                        return true;
                    } else {
                        var parentNode = null;
                        for (var index = 0; index < parentNodes.length; index++) {
                            var childNode = parentNodes[index];
                            if (parentNode == null) {
                                paramKey += childNode.openKey;
                            } else if (parentNode.valueType == "list" || parentNode.valueType == "dict" || parentNode.valueType == "tuple") {
                                paramKey = paramKey + "[" + childNode.openKey + "]";
                            } else {
                                paramKey = paramKey + "." + childNode.openKey;
                            }
                            parentNode = childNode;
                        }
                    }
                    var oriWindow = debug.getOriWindow();
                    var paramObj = {
                        key: paramKey,
                        type: "object"
                    };
                    if (treeNode.valueType == "list" || treeNode.valueType == "tuple") {
                        paramObj.type = "list";
                    } else if (treeNode.valueType == "dict") {
                        paramObj.type = "dict";
                    } else if (treeNode.valueType == "set") {
                        paramObj.type = "set";
                    }
                    oriWindow && oriWindow.win && oriWindow.win.debug.showParamKeyPathMap.put(paramKey, treeNode.path);
                    debug.sendCmd(debug.constant.SHOW_PARAM, JSON.stringify(paramObj));
                }
                return true;
            },
        }
    },
    init: function () {
        debug.initEvent();
        if ($("#scriptType").val() == 3) {
            $("#disableBreakPointLi").show();
            $("#debugCommand").show();
        }
    },
    heartBeat: function () {
        debug.intervalId = window.setInterval(function () {
            try {
                debug.sendCmd(debug.constant.DO_NOTHING);
            } catch (e) {
                console.log(e);
            }
        }, 60000)
    },
    clearHeartBeat: function () {
        debug.intervalId && window.clearInterval(debug.intervalId);
    },
    modifyOperateStatus: function (canOperate) {
        debug.canOperate = canOperate;
        if (!canOperate) {
            $("#stepOver").addClass("disable-operate");
            $("#stepInto").addClass("disable-operate");
            $("#stepOut").addClass("disable-operate");
            $("#restartCommand").addClass("disable-operate");
            $("#debugContinue").addClass("disable-operate");
        } else {
            $("#stepOver").removeClass("disable-operate");
            $("#stepInto").removeClass("disable-operate");
            $("#stepOut").removeClass("disable-operate");
            $("#restartCommand").removeClass("disable-operate");
            $("#debugContinue").removeClass("disable-operate");
        }
    },
    startDebug: function (closeCallBack, errorCallBack, detailId, pythonVersion) {
        debug.pythonVersion = pythonVersion;
        $("#scriptRunDetailId").val(detailId);
        debug.paramTree && debug.paramTree.destroy();
        debug.treeArray.splice(0, debug.treeArray.length)
        debug.objId = 0;
        debug.status = debug.constant.READY;
        debug.needAddBreakPoints = new Array();
        debug.initWebSocket(closeCallBack, errorCallBack, detailId);
    },
    getOriWindow: function () {
        return datadevInit.getActiveWindow(getKey($("#gitProjectId").val(), debug.oriFilePath));
    },
    showDebug: function () {
        $.removeMsg();
        if ($("#debugInfo").length == 0) {
            var debugerLi = "<li id='debugInfo' target='run-debug' data-title='debug' data-label='debug'><a style='text-align:center;float: left;width: 44px' href='#'>调试器</a></li>";
            $("#fixedUl").append(debugerLi);
        }
        $("#stepCommandUl").show();
        $("#logOrData").JdDataDevTab("active", 1);
    },
    endDebug: function (rundetailId, callBack) {
        if (!rundetailId || debug.detailId == rundetailId) {
            debug.sendCmd(debug.constant.STOP);
            if (callBack) {
                var oldBack = debug.closeCallBack;
                debug.closeCallBack = function () {
                    oldBack();
                    callBack();
                }
            }
        }
    },
    changeBreakPointStatus: function (breakPointsStatus) {
        debug.nowBreakPointsStatus = breakPointsStatus;
        if (breakPointsStatus) {
            $("#disableBreakPointLi span").removeClass("disable-checked");
        } else {
            $("#disableBreakPointLi span").addClass("disable-checked");
        }
        if (!debug.oriFilePath || debug.oriFilePath && debug.oriFilePath == $("#gitProjectFilePath").val()) {
            var array = new Array();
            var dependencyPaths = debug.getDependencyPaths();
            for (var index = 0; index < dependencyPaths.length; index++) {
                var realWindow = datadevInit.getActiveWindow(getKey($("#gitProjectId").val(), dependencyPaths[index]));
                var debugBreakPoints = debug.getBreakPoints(dependencyPaths[index]);
                array = array.concat(debugBreakPoints);
                var aceBreakPointsArray = new Array();
                for (var aceIndex = 0; aceIndex < debugBreakPoints.length; aceIndex++) {
                    aceBreakPointsArray.push(debugBreakPoints[aceIndex].lineno - 1);
                }
                if (realWindow && realWindow.status && realWindow.win.editor) {
                    realWindow.win.editor.session.setBreakpoints(aceBreakPointsArray, "bdp-index-lb bdp-icon " + (!breakPointsStatus ? "disable-point " : ""));
                }
            }
            if (debug.oriFilePath) {
                debug.sendCmd(breakPointsStatus ? debug.constant.ENABLE_BREAKPOINT : debug.constant.DISABLE_BREAKPOINT)
                if (debug.needAddBreakPoints && debug.needAddBreakPoints.length > 0 && breakPointsStatus) {
                    debug.sendCmd(debug.constant.ADD_BREAKPOINT, JSON.stringify({
                        breaks: debug.needAddBreakPoints,
                        enableBreaks: true
                    }))
                    debug.needAddBreakPoints = new Array();
                }
            }
        }
    },
    close: function () {
        var keys = debug.getDependencyPaths();
        debug.clearHeartBeat();
        debug.broadcast(keys, function (realWindow) {
            var realDebug = realWindow.win.debug;
            var jq = realWindow.jq;
            realWindow.win.editor.session.setErrorpoint();
            jq("#logOrData").JdDataDevTab("active", 0);
            realDebug.paramTree && realDebug.paramTree.destroy();
            realDebug.status = realDebug.constant.STOP;
            realDebug.oriFilePath = undefined;
            resetAllTopButton(realWindow);
            jq("#debugInfo").remove();
            jq("#stepCommandUl").hide();
            realWindow.win.initMode("unRun-mode");
            realWindow.win.setRunMode(false);
            realDebug.highlightLine();
        })
        debug.socket && debug.socket.close();
    },
    broadcast: function (keys, operate) {
        if (keys) {
            for (var index = 0; index < keys.length; index++) {
                var realWindow = datadevInit.getActiveWindow(getKey($("#gitProjectId").val(), keys[index]));
                if (realWindow && realWindow.status && realWindow.win) {
                    try {
                        operate(realWindow, keys[index])
                    } catch (e) {
                        console.warn(e)
                    }
                }
            }
        }
    },
    /**
     * 先从ace获取，如果没获取到就从localStorge获取
     * @param scriptFilePath
     * @returns {any[]}
     */
    getBreakPoints: function (scriptFilePath) {
        var scriptKey = getKey($("#gitProjectId").val(), scriptFilePath);
        var realWindow = datadevInit.getActiveWindow(scriptKey);
        var breakPointsArray = new Array();
        if (realWindow && realWindow.win && realWindow.win.editor) {
            var array = realWindow.win.editor.session.getBreakpoints();
            for (var index = 0; index < array.length; index++) {
                if (array[index]) {
                    breakPointsArray.push({
                        scriptFilePath: scriptFilePath,
                        lineno: (index + 1)
                    })
                }
            }
        }
        if (breakPointsArray.length > 0) {
            return breakPointsArray;
        }
        var storgeKey = getBreakPointsStorgeKey($("#gitProjectId").val(), scriptFilePath);
        var bps = storgeContent(storgeKey);
        try {
            var bpArray = JSON.parse(bps);
            for (var index = 0; index < bpArray.length; index++) {
                breakPointsArray.push({
                    scriptFilePath: scriptFilePath,
                    lineno: bpArray[index] + 1
                })
            }
        } catch (e) {
            console.log(e)
        }
        return breakPointsArray;
    },
    /**
     *
     * @param lineNum 从0计数
     */
    highlightLine: function (scriptFilePath, lineNum, showError) {
        if (debug.oldLineHightId) {
            editor.session.removeMarker(debug.oldLineHightId);
        }
        if (scriptFilePath && lineNum && scriptFilePath == $("#gitProjectFilePath").val()) {
            var obj = undefined;
            if (lineNum || lineNum == 0) {
                editor.clearSelection();
                obj = editor.session.highlightLines(lineNum, lineNum);
                editor.session.setScrollTop(debug.getScrollTop(lineNum));
            }
            debug.oldLineHightId = obj ? obj.id : undefined;
            if (showError) {
                editor.session.setErrorpoint(lineNum, "bzzx-alert  bdp-icon ");
            }
        }
    },
    getScrollTop: function (lineNum) {
        var rowcells = $(".ace_gutter-layer  .ace_gutter-cell");
        if (rowcells.length > 0) {
            var first = rowcells[0].textContent.trim();
            var last = rowcells[rowcells.length - 1].textContent.trim();
            var editorHeight = $("#code").height();
            var everyLineHeight = editorHeight / (last - first + 1);
            var schrollTop = everyLineHeight * lineNum - editorHeight / 2
            return schrollTop;
        }
        return 0;
    },
    recursionTree: function (obj) {
        var array = Object.getOwnPropertyNames(obj);
        for (var index = 0; index < array.length; index++) {
            if (obj.hasOwnProperty(array[index]) && array[index] != "datadev_debug_id") {
                var treeObj = {
                    path: debug.objId,
                    parentPath: obj.datadev_debug_id,
                    isParent: true,
                    openKey: array[index],
                    iconSkin: "icon01"
                };
                var dataObj = {};
                dataObj.key = array[index];
                dataObj.value = obj[array[index]];
                treeObj.name = debug.getKeyValue(dataObj, treeObj);
                debug.treeArray.push(treeObj);
                debug.objId++;
                if (typeof obj[array[index]] === "object") {
                    obj[array[index]].datadev_debug_id = debug.objId - 1;
                    debug.recursionTree(obj[array[index]]);
                }
            }
        }
    },
    analyzeZtree: function (obj) {
        if (debug.treeArray.length == 0) {
            debug.treeArray.push({
                path: debug.objId++,
                parentPath: -1,
                name: "Special Variables",
                isParent: true,
                isDefaultNode: true,
                iconSkin: "icon_sv"
            });
        }
        var specialVariables = debug.treeArray[0];

        var regExp = /^_\w*_$/;
        var treeObj = {
            path: debug.objId,
            parentPath: obj.datadev_debug_id || (regExp.test(obj.key) ? (specialVariables.path) : -1),
            isParent: true,
            openKey: obj.key,
            iconSkin: "icon01"
        };
        debug.objId++;
        treeObj.name = debug.getKeyValue(obj, treeObj);
        debug.treeArray.push(treeObj);
        if (typeof obj.value === "object") {
            obj.value.datadev_debug_id = debug.objId - 1;
            debug.recursionTree(obj.value);
        }
    },
    /**
     * 1。设置不同的key，value 不同的颜色
     * 2。处理 valueType
     */
    getKeyValue: function (dataObj, treeObj) {
        // console.log("key " + key + " value : " + value);
        var classReg = new RegExp("^<(class|type)\\W*(\\w*)\\W*>$");
        var valueReg = new RegExp("^<(.*?) .*?>$");
        var value = dataObj.value;
        var execResultType = classReg.exec(dataObj.type || "");
        var execResultClassValue = classReg.exec(value);
        var execResultGeneralValue = valueReg.exec(value);
        var valueType = undefined;
        if (dataObj.key == "__builtins__") {
            valueType = "module";
        } else if (execResultType && execResultType.length > 2) {
            valueType = execResultType[2];
        } else if (execResultClassValue && execResultClassValue.length > 2) {
            valueType = execResultClassValue[2];
        } else if (execResultGeneralValue && execResultGeneralValue.length > 1) {
            valueType = execResultGeneralValue[1];
        } else if (/^-?\d*\.\d+$/.test(value)) {
            valueType = "float";
        } else if (/^-?\d+$/.test(value)) {
            valueType = "int";
        } else {
            valueType = "string"
        }
        if (valueType.lastIndexOf(".") != -1) {
            valueType = valueType.substring(valueType.lastIndexOf(".") + 1);
        }
        if (valueType == "int" || valueType == "float" || valueType == "str" || valueType == "bool" || valueType == "NoneType") {
            treeObj.isParent = false;
            treeObj.iconSkin = "icon_value";
        }
        treeObj.valueType = valueType;
        value = dataObj.key == "__builtins__" ? "<module '__builtin__' (built-in)>" : String(value);
        var fixValue = value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        var html = "<span class='debugparamkey'>" + dataObj.key + "</span>"; // $("<span>").addClass("debugparamkey");
        html += "<span class='debugparamEq'>=</span>";
        if (valueType) {
            html += "<span class='debugparamvaluetype'>{" + valueType + "}</span>"
        }
        html += "<span class='debugparamvalue'>" + fixValue + "</span>";
        return html;
    },
    initWebSocket: function (closeCallBack, errorCallBack, detailId) {
        debug.detailId = detailId;
        debug.oriFilePath = $("#gitProjectFilePath").val();
        debug.closeCallBack = closeCallBack;
        debug.errorCallBack = errorCallBack;
        var socket = debug.socket = new WebSocket("ws://" + location.host + "/scriptcenter/runDebug?runDetailId=" + detailId + "&pythonVersion=" + debug.pythonVersion);
        socket.onmessage = function (evt) {
            console.log(evt.data);
            var message = evt.data;
            var messageObj = JSON.parse(message);
            if (messageObj.code == 0 && messageObj.runDetailId == debug.detailId) {
                if (messageObj.debugCmd == debug.constant.SHOW_CODE_LINE && debug.status != debug.constant.STOP) {
                    debug.showCodeLine(messageObj);
                } else if (messageObj.debugCmd == debug.constant.SHOW_FULL_PARAM) {
                    debug.showParams(messageObj.data)
                } else if (messageObj.debugCmd == debug.constant.LOG_MSG) {
                    debug.showLog(messageObj.data.logMsg);
                } else if (messageObj.debugCmd == debug.constant.NETTY_READY) {
                    debug.modifyOperateStatus(false);
                    var array = new Array();
                    var dependencyPaths = debug.getDependencyPaths();
                    for (var index = 0; index < dependencyPaths.length; index++) {
                        array = array.concat(debug.getBreakPoints(dependencyPaths[index]));
                    }
                    var obj = {};
                    obj.enableBreaks = debug.nowBreakPointsStatus;
                    if (debug.nowBreakPointsStatus) {
                        obj.breaks = array;
                    } else {
                        debug.needAddBreakPoints = array;
                        obj.breaks = new Array();
                    }
                    debug.sendCmd(debug.constant.START, JSON.stringify(obj));
                    debug.heartBeat();
                } else if (messageObj.debugCmd == debug.constant.FINISH) {
                    debug.endDebug();
                } else if (messageObj.debugCmd == debug.constant.CLOSE) {
                    debug.close();
                } else if (messageObj.debugCmd == debug.constant.EXCEPTION) {
                    debug.showCodeLine(messageObj, true);
                } else if (messageObj.debugCmd == debug.constant.SHOW_PARAM) {
                    debug.showParam(messageObj.data);
                }
            }
        };
        socket.onerror = function () {
            debug.errorCallBack && debug.errorCallBack();
            debug.highlightLine();
        }
        socket.onclose = function () {
            debug.closeCallBack && debug.closeCallBack();
            // debug.highlightLine();
        };
        return socket;
    },
    showLog: function (log) {
        if (debug.oriFilePath == $("#gitProjectFilePath").val()) {
            var keys = debug.getDependencyPaths();
            debug.broadcast(keys, function (realWindow) {
                realWindow.win.addLog && realWindow.win.addLog(log);
            })
        }
    },
    getDependencyPaths: function () {
        var key = getKey($("#gitProjectId").val(), debug.oriFilePath);
        var dependencys = debugDepndendedMap.get(key);
        if (dependencys && dependencys.length > 0) {
            return dependencys;
        } else {
            var defaultDependencys = new Array();
            defaultDependencys.push(debug.oriFilePath);
            return defaultDependencys;
        }
    },
    showCodeLine: function (messageObj, showError) {
        if (debug.status == debug.constant.READY) {
            debug.status = debug.constant.START;
            debug.showDebug();
            return;
        }
        messageObj.data = messageObj.data || {};
        messageObj.data.scriptFilePath = messageObj.data.scriptFilePath || debug.ingFilePath;
        messageObj.data.lineno = messageObj.data.lineno || debug.lineno;
        debug.ingFilePath = messageObj.data.scriptFilePath;
        debug.lineno = messageObj.data.lineno;
        if (debug.status != debug.constant.EXCEPTION) {
            debug.status = showError ? debug.constant.EXCEPTION : debug.status;
            if (messageObj.data.scriptFilePath == debug.oriFilePath) {
                var highLineNum = debug.lineno - 1;
                openScript($("#gitProjectId").val(), debug.oriFilePath);
                var keys = debug.getDependencyPaths();
                debug.broadcast(keys, function (realWindow) {
                    realWindow.win && realWindow.win.debug.highlightLine(messageObj.data.scriptFilePath, highLineNum, showError);
                })
            } else {
                var dependencykey = getKey($("#gitProjectId").val(), messageObj.data.scriptFilePath);
                var dependencyWindow = datadevInit.getActiveWindow(dependencykey);
                var transferObj = {};
                transferObj[debug.constant.SHOW_CODE_LINE] = debug.lineno;
                if (showError) {
                    transferObj.showError = true;
                }
                transferObj.oriFilePath = debug.oriFilePath;
                transferObj.ingFilePath = messageObj.data.scriptFilePath;
                transferObj.paramsTree = debug.treeArray;
                if (dependencyWindow && dependencyWindow.win && dependencyWindow.win.debug) {
                    openScript($("#gitProjectId").val(), messageObj.data.scriptFilePath);
                    dependencyWindow.win.debug.initDependencyDebug(transferObj, false);
                } else {
                    initFunctionMap.put(getKey(null, messageObj.data.scriptFilePath), function (key, win) {
                        var window = datadevInit.getActiveWindow(getKey($("#gitProjectId").val(), messageObj.data.scriptFilePath));
                        window.win.debug.initDependencyDebug(transferObj, true)
                    })
                    openScript($("#gitProjectId").val(), messageObj.data.scriptFilePath);
                }
            }
        }

    },
    showParams: function (data) {
        var keys = debug.getDependencyPaths();
        var treeArray = debug.treeArray;
        treeArray.splice(0, treeArray.length)
        for (var index = 0; index < data.sortDatas.length; index++) {
            debug.analyzeZtree(data.sortDatas[index]);
        }
        var buildInIndex = -1;
        for (var index = 0; index < treeArray.length; index++) {
            if (treeArray[index].name == "__builtins__") {
                buildInIndex = index;
            }
        }
        if (buildInIndex > 0) {
            var buildObj = treeArray[buildInIndex];
            treeArray.splice(buildInIndex, 1);
            treeArray.unshift(buildObj);
        }
        debug.broadcast(keys, function (realWindow) {
            var realDebug = realWindow.win.debug;
            realDebug && realDebug.initParamTree(treeArray);
            realDebug && realDebug.modifyOperateStatus(true);
        })
    },
    showParam: function (data) {
        var keys = debug.getDependencyPaths();
        var paramKey = data.paramName;
        var parentPath = debug.showParamKeyPathMap.get(paramKey);
        var parenNode = debug.paramTree.getNodeByParam("path", parentPath);
        if (parenNode) {
            var newArray = [];
            for (var index = 0; index < data.sortDatas.length; index++) {
                var obj = data.sortDatas[index];
                var treeObj = {
                    path: debug.objId,
                    parentPath: parentPath,
                    isParent: true,
                    openKey: obj.key,
                    iconSkin: "icon01"
                };
                debug.objId++;
                treeObj.name = debug.getKeyValue(obj, treeObj);
                debug.treeArray.push(treeObj);
                newArray.push(treeObj);
            }
            debug.broadcast(keys, function (realWindow) {
                var realDebug = realWindow.win.debug;
                realDebug.addParam(newArray, parentPath);
            })
        }
    },
    addParam: function (treeArray, parentPath) {
        var parenNode = debug.paramTree.getNodeByParam("path", parentPath);
        if (parenNode) {
            parenNode.isParent = true;
            parenNode.iconSkin = "icon01";
            debug.paramTree.updateNode(parenNode);
            debug.paramTree.removeChildNodes(parenNode);
            debug.paramTree.addNodes(parenNode, treeArray);
        }
    },
    sendCmd: function (cmd, param) {
        if (debug.oriFilePath && debug.status && debug.status != debug.constant.STOP) {
            if (debug.oriFilePath != $("#gitProjectFilePath").val()) {
                var activeWindow = debug.getOriWindow();
                activeWindow.win.debug.sendCmd(cmd, param);
            } else {
                if (cmd == debug.constant.STEP_OUT || cmd == debug.constant.STEP_INTO || cmd == debug.constant.STEP_NEXT || cmd == debug.constant.CONTINUE) {
                    if (debug.canOperate) {
                        var keys = debug.getDependencyPaths();
                        debug.broadcast(keys, function (realWindow) {
                            realWindow.win.debug && realWindow.win.debug.paramsTree && realWindow.win.debug.paramsTree.destroy();
                            realWindow.win.debug && realWindow.win.debug.modifyOperateStatus(false);
                        })

                        //todo
                    } else {
                        return;
                    }
                } else if (cmd == debug.constant.RESTART) {
                    debug.closeCallBack = function () {
                        command2debug();
                    }
                    cmd = debug.constant.STOP;
                }
                cmd = cmd + ":" + (param || "");
                debug.socket && debug.socket.send(cmd);
            }
        }

    },
    initEvent: function () {
        $("#code").on("click", ".ace_gutter-layer  .ace_gutter-cell", function () {
            if ($("#scriptType").val() != 3) {
                return;
            }
            var breakPointLineNum = $(this).text().trim();
            if (breakPointLineNum) {
                var lineNum = breakPointLineNum - 1;
                var hasBreakPoint = false;
                var oldBreakPoints = editor.session.getBreakpoints();
                for (var index = 0; index < oldBreakPoints.length; index++) {
                    var obj = oldBreakPoints[index];
                    if (obj && lineNum == index) {
                        hasBreakPoint = true;
                        break;
                    }
                }
                var breakPoints = new Array();
                breakPoints.push({
                    scriptFilePath: $("#gitProjectFilePath").val(),
                    lineno: breakPointLineNum
                })
                var storgeKey = getBreakPointsStorgeKey($("#gitProjectId").val(), $("#gitProjectFilePath").val());
                var bps = storgeContent(storgeKey);
                var bpArray;
                try {
                    bpArray = JSON.parse(bps);
                } catch (e) {
                    console.log(e)
                }
                if (bpArray == null) {
                    bpArray = new Array();
                }
                if (hasBreakPoint) {
                    var index = bpArray.indexOf(lineNum);
                    if (index != -1) {
                        bpArray.splice(index, 1);
                    }
                    editor.session.clearBreakpoint(lineNum);
                    debug.sendCmd(debug.constant.CANCEL_BREAKPOINT, JSON.stringify({
                        enableBreaks: true,
                        breaks: breakPoints
                    }))
                } else if (debug.canSetBreakPoint(lineNum)) {
                    var index = bpArray.indexOf(breakPointLineNum);
                    if (index == -1) {
                        bpArray.push(lineNum);
                    }
                    var className = "bdp-index-lb bdp-icon";
                    if (!debug.nowBreakPointsStatus) {
                        className += " disable-point ";
                    }
                    editor.session.setBreakpoint(lineNum, className);
                    debug.sendCmd(debug.constant.ADD_BREAKPOINT, JSON.stringify({
                        enableBreaks: debug.nowBreakPointsStatus,
                        breaks: breakPoints
                    }))
                }
                storgeContent(storgeKey, JSON.stringify(bpArray))
            }
        })
        $("#stepOver").click(function () {
            debug.sendCmd(debug.constant.STEP_NEXT);
        })
        $("#stepInto").click(function () {
            debug.sendCmd(debug.constant.STEP_INTO);
        })
        $("#stepOut").click(function () {
            debug.sendCmd(debug.constant.STEP_OUT);
        })
        $("#debugSideBar").on("click", ".sideBar-li", function () {
            var command = $(this).attr('data-command');
            switch (command) {
                case "run":
                    command2run();
                    break;
                case "restart":
                    command2restart();
                    break;
                case "continue":
                    command2continue();
                    break;
                case "debug":
                    command2debug();
                    break;
                case "stop":
                    command2stop($(this));
                    break;
                case "check-breakpoint":

                    break;
                case "disable-breakpoint":
                    command2disabled();
                default:
                    break;
            }

        })
        $(document).on("mouseenter mouseleave mousemove", ".pop-title", function (event) {
            var left = event.pageX + 8, top = event.pageY + 8;
            var ele = event.target;
            var title = $(this).attr("data-title");
            var type = event.type;
            if (type == 'mouseenter') {
                ele.title = '';
                if (title != null) {
                    var showEle = $('<div></div>', {text: title, class: 'showTitleBox'}).css({
                        top: top,
                        left: left,
                    })
                    showEle.appendTo('body');
                }
            }
            else if (type == "mouseleave" || type == 'mouseout') {
                $('.showTitleBox').remove();
            } else if (type == 'mousemove') {
                $('.showTitleBox').css({
                    top: top + 10,
                    left: left
                })
            }
            // return false;
        })

    },
    initParamTree: function (dataArray) {
        debug.paramTree = $.fn.zTree.init($("#variablesTree"), debug.treeSetting, dataArray);
    },
    initBreakPoints: function () {
        if ($("#scriptType").val() != 3) {
            return;
        }
        editor.session.setErrorpoint();
        var disabledProjects = storgeContent(getDisBreakPointStatusStorgeKey());
        var storgeKey = getBreakPointsStorgeKey($("#gitProjectId").val(), $("#gitProjectFilePath").val());
        var bpsValue = storgeContent(storgeKey);
        if (disabledProjects) {
            try {
                disabledProjects = JSON.parse(disabledProjects);
            } catch (e) {
                console.log(e);
            }
        }
        var isDisabled = disabledProjects && disabledProjects.indexOf($("#gitProjectId").val()) != -1;
        if (isDisabled) {
            $("#disableBreakPointLi span").addClass("disable-checked");
            debug.nowBreakPointsStatus = false;
        } else {
            debug.nowBreakPointsStatus = true;
        }
        if (bpsValue) {
            try {
                var bpArray = JSON.parse(bpsValue);
                var className = "bdp-index-lb bdp-icon" + (isDisabled ? " disable-point" : "");
                editor.session.setBreakpoints(bpArray, className);
            } catch (e) {
                console.log(e);
            }
        }
        $("#disableBreakPointLi").show();
    },
    canSetBreakPoint: function (row) {
        var lineContent = editor.session.getLine(row);
        if (!lineContent || lineContent.trim() == "")
            return false;
        var tokens = editor.session.getTokens(row);
        if (tokens.length == 1 && (tokens[0].type == "string" || tokens[0].type == "comment")) {
            return false;
        }
        return true;

    },
    initDependencyDebug(statusObj, newOpen) {
        debug.showDebug();
        debug.status = debug.constant.START;
        setRunMode(true);
        initMode("debug-mode");
        disableAllTopButton && disableAllTopButton(getKey());
        parentInitTopButton && parentInitTopButton(getKey(), TOP_BUTTON.stop, true);
        if (statusObj.oriFilePath) {
            debug.oriFilePath = statusObj.oriFilePath;
        }
        if (statusObj[debug.constant.SHOW_CODE_LINE]) {
            var keys = debug.getDependencyPaths();
            debug.broadcast(keys, function (realWindow) {
                realWindow.win && realWindow.win.debug.highlightLine($("#gitProjectFilePath").val(), statusObj[debug.constant.SHOW_CODE_LINE] - 1, statusObj.showError);
            })
        }
        if (newOpen) {
            var oriWindow = debug.getOriWindow();
            var oriDebug = oriWindow.win.debug;
            if (oriDebug.canOperate != undefined) {
                debug.modifyOperateStatus(oriDebug.canOperate);
                var logs = oriWindow.win.getLogs();
                for (var index = 0; index < logs.length; index++) {
                    var log = logs[index];
                    addLog(log);
                }
            }
            if (oriDebug.treeArray && oriDebug.treeArray.length > 0) {
                debug.initParamTree(oriDebug.treeArray)
            }
        } else {
            if (statusObj.paramsTree) {
                debug.initParamTree(statusObj.paramsTree);
            }
        }
    }
}



/**
 * 默认显示bufflo 版本
 */
function initHasVersionChange(isLeft, value, version, dependencyId) {
    //屏蔽一些显示操作
    isLeftVersionChoice = isLeft;
    shieldByVersionChange(isLeft);
    //当前版本
    $("#currentVersionInfo").text(version);
    if (isLeft) {
        $("#version").val(version);
        $("#currentRelationDependencyId").val(dependencyId);
        $("#mergeVersionContent .merge-version-tips").text("当前脚本为调度版本，保存后将覆盖最新版本，返回").show();
    } else {
        $("#lastVersion").val(version);
        $("#lastVersionInfo").text(version);
        $("#relationDependencyId").val(dependencyId);
        $("#currentRelationDependencyId").val(dependencyId);
        $("#version").val(version);
        $("#mergeVersionContent .merge-version-tips").text("当前脚本为最新版本，返回");
    }
    if (!skipInit && dependencyId) {
        packDetail(dependencyId);
    } else {
        skipInit = false;
    }
    $("#fileMd5").val($.md5(value || ""));
    editor && editor.setValue(value);
    editor && editor.clearSelection()
    editor && editor.focus();
    if (!isInitByChangeVersion) {
        $("#code").css({
            top: 30
        })
        $("#mergeVersionContent").show();
        isInitByChangeVersion = true;
        editor && editor.resize();
    }

}

function removeVersionChange() {
    if (isInitByChangeVersion) {
        $("#code").css({
            top: 0
        })
        $("#mergeVersionContent").hide();
        isInitByChangeVersion = false;
        editor && editor.resize();
        shieldByVersionChange(false);
        $("#ztreeWrapDiv").css({
            "padding-top": "50px"
        })
        $("#update2NewVersion").show();
    }

}

function resetVersionChange() {
    if (isInitByChangeVersion) {
        shieldByVersionChange(isLeftVersionChoice);
        $("#chooseVersionBtn").removeClass("bdp-disabled");
    }
}
function isTemplate() {
    return $("#templateId").val() && $("#templateId").val() > 0
}

//因为多版本，需要屏蔽一些显示及操作，
function shieldByVersionChange(isLeft) {
    if (isLeft) {
        $("#gitVersionStatusTip").hide();
        $("#update2NewVersion").hide();
        parentInitTopButton && parentInitTopButton(getKey(), "git", false);
        parentInitTopButton && parentInitTopButton(getKey(), "upLine", false);
        parentInitTopButton && parentInitTopButton(getKey(), "dependencyButton", false);
        $("#scriptGit").addClass("event-disabled");
        $("#ztreeWrapDiv").css({
            "padding-top": "0px"
        })
    } else {
        $("#gitVersionStatusTip").show();
        $("#update2NewVersion").show();
        parentInitTopButton && parentInitTopButton(getKey(), "git", $("#filePosition").val() != "target");
        parentInitTopButton && parentInitTopButton(getKey(), "upLine", true);
        parentInitTopButton && parentInitTopButton(getKey(), "dependencyButton", $("#isShow").val() != 1 && ($("#scriptType").val() == 2 || $("#scriptType").val() == 3));
        $("#scriptGit").removeClass("event-disabled");
        $("#ztreeWrapDiv").css({
            "padding-top": "50px"
        })
    }
}

//多版本模式下并且选中旧版本  或者sql选中执行
function isDirectRun() {
    return (isInitByChangeVersion && $("#version").val() != $("#lastVersion").val() || $("#scriptType").val() == 1 && editor.session.getTextRange(editor.getSelectionRange()));
}

function getDirectRunValue() {
    if ($("#scriptType").val() == 1) {
        return editor.session.getTextRange(editor.getSelectionRange());
    } else {
        return editor && editor.getValue() || "";
    }
}

/**
 * 是否初始化完成
 * @returns {boolean}
 */
function isFinishContent() {
    return $("#canEdit").val() == "true" && initContentStatus;
}

//运行
function command2run() {
    parentButtonClick && parentButtonClick("run");
}

//保存
function command2save() {
    parentButtonClick && parentButtonClick("save");
}

//继续
function command2continue() {
    debug.sendCmd(debug.constant.CONTINUE)
}

//调试
function command2debug() {
    parentButtonClick && parentButtonClick("debug");
}

//停止
function command2stop(node) {
    if (!node || !node.hasClass("ico-disabled")) {
        stopScript && stopScript(debug.getOriWindow());

    }
}

//重新运行，分调试跟正常运行
function command2restart() {
    if (getRunMode() == 1) {
        //debug restart
        debug.sendCmd(debug.constant.RESTART);
    } else {
        //run restart
        restartScript(getKey(), $("#scriptRunDetailId").val());
    }
    parentButtonClick && parentButtonClick("stop");
}

//静音断点
function command2disabled() {
    var gitProjectId = $("#gitProjectId").val();
    var status = !debug.nowBreakPointsStatus;
    var dbpKey = getDisBreakPointStatusStorgeKey();
    var dbpValue = storgeContent(dbpKey);
    try {
        dbpValue = JSON.parse(dbpValue);
    } catch (e) {
        console.warn(e)
    }
    if (dbpValue == null) {
        dbpValue = new Array();
    }
    if (!status && dbpValue.indexOf(gitProjectId) == -1) {
        dbpValue.push(gitProjectId);
    } else if (status && dbpValue.indexOf(gitProjectId) != -1) {
        dbpValue.splice(dbpValue.indexOf(gitProjectId), 1);
    }
    storgeContent(dbpKey, JSON.stringify(dbpValue));
    var windows = datadevInit.getAllWindow();
    for (var index = 0; index < windows.length; index++) {
        var window = windows[index];
        if (window && window.win && window.win.debug) {
            window.win.debug.changeBreakPointStatus(status);
        }
    }
}

function setRunMode(mode) {
    runMode = mode;
}

function saveBreakPoints() {
    if (editor && editor.session) {
        var array = editor.session.getBreakpoints();
        var newBreakPointsArray = new Array();
        for (var index = 0; index < array.length; index++) {
            if (array[index]) {
                newBreakPointsArray.push(index);
            }
        }
        var storgeKey = getBreakPointsStorgeKey($("#gitProjectId").val(), $("#gitProjectFilePath").val());
        storgeContent(storgeKey, JSON.stringify(newBreakPointsArray));
    }

}

function getRunMode() {
    return runMode;
}

function showInfo() {
    $("#rightDiv").JdDataDevTab("active", 1);
}

function showArgu() {
    var runArgus = getRunArgs();
    var length = Object.getOwnPropertyNames(runArgus).length;
    if (length > 0) {
        $("#rightDiv").JdDataDevTab("active", 0);
    } else {
        needShowArgs = true;
    }
}

function initMode(modeClass) {
    debug.modifyOperateStatus(true);
    $("#debugSideBar .sideBar-li").each(function () {
        if ($(this).hasClass(modeClass)) {
            if (modeClass == "unRun-mode" && $(this).attr("data-command") == "stop") {
                $(this).addClass("ico-disabled");
            } else {
                $(this).removeClass("ico-disabled");
            }
            if (!$(this).attr("data-type") || $(this).attr("data-type") == $("#scriptType").val()) {
                $(this).show();
            }
        } else {
            $(this).hide();
        }
    })
}

/**
 * 改变按钮禁用状态
 */
function changeSideBarStatus(buttonCommand, enabled) {
    if (enabled) {
        $("#debugSideBar .sideBar-li[data-command='" + buttonCommand + "']").removeClass("ico-disabled");
    } else {
        $("#debugSideBar .sideBar-li[data-command='" + buttonCommand + "']").addClass("ico-disabled");
    }
}

//如果支持localstorge，优先使用localstorge,否则使用cookie
function storgeContent(key, value) {
    if (window.localStorage) {
        var storage = window.localStorage;
        if (value) {
            storage.setItem(key, value);
        } else {
            return storage.getItem(key);
        }
    } else {
        if (value) {
            $.cookie(key, JSON.stringify(value), {expires: 3});
        } else {
            return $.cookie(key);
        }
    }
}

function getBreakPointsStorgeKey(gitProjectId, gitProjectFilePath) {
    return ("B_P_" + getKey(gitProjectId, gitProjectFilePath).trim().toUpperCase().replace(/\//g, "_"));
}

function getDisBreakPointStatusStorgeKey() {
    return "B_P_D_S";
}

function changeDep(isShow) {
    if (isShow) {
        $("#cheDepDiv").show();
        $("#runType").val(1);
    } else {
        $("#cheDepDiv").hide();
        $("#depView").hide();
        $("#runType").val(0);
    }
}

function getNodeDiv(node) {
    if (!node.type || node.type == -1) {
        node.type = -3;
    }
    var script = getScriptObj(node.type);
    var iconClass = "bdp-icon " + script.iconClass;
    var version = node.version || "";//去null
    var lastVersion = node.lastVersion || "";//去null
    var treeNodeTd = "<div style='padding-left: " + ((node.tableLevel || 0) * 25) + "px' class='script-file'><input class='checkbox' type='checkbox'><span class='script-icon  " + iconClass + "'></span><span class='script-name'>" + node.name + "</span></div>";
    var zipVersionTd = "<div class='version ' data-version='" + version + "'><span>" + version + "</span></div>";
    var lastVersionTd = "<div class='version lastVersion " + (node.deleted == 1 ? "noVersion" : "") + "' data-version='" + lastVersion + "'><span>" + lastVersion + "</span></div>";
    var lastModifiedTd = "<div class='modified'>" + (node.lastModified || "") + "</div>";
    var lastMenderTd = "<div class='mender'>" + (node.modifier || "") + "</div>";
    return "<div class='nodeDiv'>" + treeNodeTd + zipVersionTd + lastVersionTd + lastMenderTd + lastModifiedTd + "</div>";
}

function packDetail(dependencyId, callBack) {
    packDetailMap.clear();
    $("#packTreeUl").empty();
    $("#treeDetailTableBody").empty();

    commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScriptsByDependencyId.ajax", {
        dependencyId: dependencyId
    }, null, function (node, data) {
        if (data.obj) {
            childNodes(data.obj);
            // for (var i = 0; i < data.obj.length; i++) {
            //     // var tr = getTr(data.obj[i]);
            //     // $("#treeDetailTableBody").append(tr);
            //     // var li = getLi(data.obj[i]);
            //     // $("#packTreeUl").append(li);
            //     // packDetailMap.put(data.obj[i].path, "1")
            // }
            var root = packDetailMap.get(rootPath);
            var chis = root.childrens;
            var lis = "";
            for (var i = 0; i < chis.length; i++) {
                lis += getUlLis(chis[i], true);
            }
            $("#packTreeUl").append(lis);
            callBack && callBack();
            initContentStatus = true;
            initFunction();
        }
    })
}

function childNodes(nodes) {
    packDetailMap.put(rootPath, {
        childrens: []
    })
    for (var i = 0; i < nodes.length; i++) {
        var parent = nodes[i].parentPath;
        var parentNode = packDetailMap.get(rootPath + parent);
        var childrens = parentNode.childrens;
        if (!childrens) {
            parentNode.childrens = [];
            childrens = parentNode.childrens;
        }
        var index = 0;
        for (; index < childrens.length; index++) {
            if (childrens[index].parChl == 1) {
                break;
            }
        }
        childrens.splice(index, 0, nodes[i]);
        nodes[i].parent = parentNode;
        packDetailMap.put((rootPath + nodes[i].path), nodes[i]);
    }
}

function getUlLis(node, isBorder) {
    var childrens = node.childrens;
    var liClass = "open-li ";
    liClass += (isBorder ? "table-border  " : "");
    liClass += (node.parChl == 0 ? "dir-li" : "file-li");
    var li = "<li data-path='" + node.path + "' data-parent='" + node.parentPath + "' class='" + liClass + "'>"
    var nodeDiv = getNodeDiv(node);
    li += nodeDiv;
    li += "<ul>";
    for (var i = 0; childrens && i < childrens.length; i++) {
        li += getUlLis(childrens[i]);
    }
    li += "</ul></li>"
    return li;
}

function updatePackDetail(gitProjectId, gitProjectFilePath) {
    var nowProjectId = $("#gitProjectId").val();
    if (nowProjectId == gitProjectId && $("#currentRelationDependencyId").val() && $("#currentRelationDependencyId").val() > 0 && packDetailMap.containsKey(rootPath + gitProjectFilePath)) {
        packDetail($("#currentRelationDependencyId").val());
    }
}






function refreshFileInfoStatus(fileMd5, version, gitStatus) {
    if (fileMd5) {
        $("#fileMd5").val(fileMd5);
    }
    if (version) {
        $("#version").val(version);
    }
    if (gitStatus) {
        $("#gitStatus").val(gitStatus);
        if (gitStatus == "MOD" || gitStatus == "ADD") {
            $("#gitVersionStatusTip span").text("与git版本不一致");
        } else if (gitStatus == "DEL") {
            $("#gitVersionStatusTip span").text("git脚本已删除");
        } else {
            $("#gitVersionStatusTip span").text("");
        }
    }
}

function hiddenMoreFunction() {
     hiddenAllRightMeun && hiddenAllRightMeun();
}

function showMorefunction(event) {
    if (event.which == 3) {
        // thisNode.position().left * 1;
        var parentNode = $("#code");
        var left = event.pageX - parentNode.position().left * 1 + 10;
        var top = event.pageY - parentNode.position().top * 1 + 10;

        var menuHeight = $(".moreFunction").height();
        var menuWidth = $(".moreFunction").width();
        if (getIframeOffset && getWindowSize) {
            var offset = getIframeOffset();
            var size = getWindowSize();
            if (offset && size) {
                var windowHeight = size.height;
                var windowWidth = size.width;
                if (menuHeight + top + offset.top + 10 > windowHeight) {
                    top = windowHeight - offset.top - 10 - menuHeight;
                    top = top < 0 ? 0 : top;
                }
                if (menuWidth + left + offset.left + 10 > windowWidth) {
                    left = windowWidth - offset.left - 10 - menuWidth;
                    left = left < 0 ? 0 : left;
                }
            }
        }
        var isShow = $("#isShow").val();
        if (isShow == 1) {
            $("li[envent-type='rename']", $(".moreFunction")).hide();
        } else {
            $("li[envent-type='rename']", $(".moreFunction")).show();
        }
        hiddenMoreFunction();
        $(".moreFunction").css("left", left + "px").css("top", top + "px").css("display", "block");
    }
}

function getKey(gitProjectId, path) {
    path = path || $("#gitProjectFilePath").val();
    gitProjectId = gitProjectId || $("#gitProjectId").val();
    gitProjectId = gitProjectId.trim();
    path = path.trim();
    if (path.startsWith("/")) {
        path = gitProjectId + path;
    } else {
        path = gitProjectId + "/" + path.trim();
    }
    return path;
}

function copyValue(value) {
    var oInput = document.createElement('input');
    oInput.value = value;
    document.body.appendChild(oInput);
    oInput.select(); // 选择对象
    document.execCommand("Copy"); // 执行浏览器复制命令
    oInput.className = 'oInput';
    oInput.style.display = 'none';
    $.successMsg("复制成功");
}

/*整个重新刷新顶部按钮*/
function parentInitTopButtons(key, scriptMapParams) {
    scriptMap.put(key, scriptMapParams);
    if (key && datadevInit.isActive(key)) {
        datadevInit.changeTopButtons(key);
    }
}
function initLocation() {
    if ($("#isShow").val() == 1) {
        $(".moreFunction li[envent-type='git']").addClass("event-disabled");
        $(".moreFunction li[envent-type='scriptUrl']").addClass("event-disabled");
        $(".moreFunction li[envent-type='position']").addClass("event-disabled");
        $(".moreFunction li[envent-type='copyfilename']").addClass("event-disabled");
        $(".moreFunction li[envent-type='copyfilepath']").addClass("event-disabled");
        $(".moreFunction li[envent-type='rename']").addClass("event-disabled");
        parentInitTopButton && parentInitTopButton(getKey(), "dependencyButton", false);
        parentInitTopButton && parentInitTopButton(getKey(), "upLine", false);

    } else {
        $(".moreFunction li[envent-type='git']").removeClass("event-disabled");
        $(".moreFunction li[envent-type='scriptUrl']").removeClass("event-disabled");
        $(".moreFunction li[envent-type='position']").removeClass("event-disabled");
        $(".moreFunction li[envent-type='copyfilename']").removeClass("event-disabled");
        $(".moreFunction li[envent-type='copyfilepath']").removeClass("event-disabled");
        $(".moreFunction li[envent-type='rename']").removeClass("event-disabled");
        parentInitTopButton && parentInitTopButton(getKey(), "dependencyButton", !isTemplate() && ($("#scriptType").val() == 2 || $("#scriptType").val() == 3));
        parentInitTopButton && parentInitTopButton(getKey(), "upLine", !isTemplate() && ($("#scriptType").val() == 1 || $("#scriptType").val() == 2 || $("#scriptType").val() == 3 || $("#scriptType").val() == 4));

    }
    var templateId = $("#templateId").val();
    var scriptType = $("#scriptType").val();
    if (isTemplate() || !(scriptType == 1 || scriptType == 2 || scriptType == 3)) {
        $(".moreFunction li[envent-type='addtemplate']").hide();
    }
    if ($("#runType").val() == 1) {
        $("#cheDepDiv").show()
    } else {
        $("#cheDepDiv").hide()
    }
    if ($("#isShow").val() == 1 || $("#filePosition").val() == "target" || !initContentStatus) {
        parentInitTopButton && parentInitTopButton(getKey(), "git", false);
    } else {
        parentInitTopButton && parentInitTopButton(getKey(), "git", !isTemplate());
    }
}

/**
 * init:true 表示是脚本刚打开时调用
 * @param init
 */
function initScriptContent(init) {
    var status = false;
    var version = $("#version").val();
    var gitProjectId = $("#gitProjectId").val();
    var gitProjectFilePath = $("#gitProjectFilePath").val();
    if ($("#canEdit").val() && $("#canEdit").val() == "true") {
        commonAjaxEvents.commonPostAjax(getScriptUrl, {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            version: version
        }, $("#code"), function (node, data) {
            if (data) {
                status = true;
                initContentStatus = true;
                var res = data.obj.res;
                var md5 = !data.obj.md5 ? $.md5(res) : data.obj.md5;
                var gitStatus = data.obj.gitStatus;
                refreshFileInfoStatus && refreshFileInfoStatus(gitProjectId, gitProjectFilePath, md5, null, gitStatus);
                editor.setValue(res);
                editor.focus();
                editor.clearSelection()
                $("#code").data("editor", editor);
                debug.initBreakPoints();
                initTopButton();
                initFunction();
            }
        }, function () {
            if (init && !status) {
                initTopButton();
            }
        })
    }
}


function switchStartAndStop(value) {
    if (value == 0) {
        $(".moreFunction li[envent-type='stop']").show();
        $(".moreFunction li[envent-type='run']").hide();
        $(".moreFunction li[envent-type='debug']").hide();
    } else {
        $(".moreFunction li[envent-type='run']").show();
        $(".moreFunction li[envent-type='debug']").show();
        $(".moreFunction li[envent-type='stop']").hide();
    }
}

function initIframeKeyMap() {
    var isMac = (navigator.userAgent.indexOf('Mac') >= 0) ? true : false;
    if (isMac) {
        $(".moreFunction li[envent-type='save'] span.keyMap").text("⌘S")
        $(".moreFunction li[envent-type='run'] span.keyMap").text("⌃↩")
        $(".moreFunction li[envent-type='debug'] span.keyMap").text("⌃⇧↩")
        $("#stepOut").attr("data-title", "Step Out，跳出子函数（⇧F8）")
        $("#stepUntil").attr("data-title", "运行到光标处（⌥F9）")
    }

    $(document).keydown(function (event) {
        if (event.ctrlKey && event.shiftKey && event.keyCode == 13) {
            command2debug();
        } else if (event.ctrlKey && event.keyCode == 83) {
            saveScript && saveScript();
            return false;
        } else if (event.metaKey && event.keyCode == 83) {
            saveScript && saveScript();
            return false;
        } else if (event.ctrlKey && event.keyCode == 13) {
            runOrStopScript && runOrStopScript();
            return false;
        } else if (event.keyCode == 113 && $("#isShow").val() == 0) {
            renameScript();
            return false;
        } else if (isMac && event.ctrlKey && event.altKey && event.keyCode == 76) {
            format && format();
            return false;
        } else if (!isMac && event.ctrlKey && event.shiftKey && event.keyCode == 70) {
            format && format();
            return false;
        } else if (event.shiftKey && event.keyCode == 119) {
            $("#stepOut").click();
        } else if (event.keyCode == 118) {
            $("#stepInto").click();
        } else if (event.keyCode == 119) {
            $("#stepOver").click();
        }
    })

}

function renameScript() {
    var gitProjectId = $("#gitProjectId").val();
    var gitProjectFilePath = $("#gitProjectFilePath").val();
    var gitProjectDirPath = $("#gitProjectDirPath").val();
    var name = $("#fileName").val();
    var type = $("#scriptType").val();
    parentRenameScript && parentRenameScript(gitProjectId, gitProjectFilePath, gitProjectDirPath, name, type);
}

function initCallBack() {
    var gitProjectId = $("#gitProjectId").val();
    var gitProjectFilePath = $("#gitProjectFilePath").val();
    var key = getKey(gitProjectId, gitProjectFilePath);
    var callBack = afterInitCallBackMap && afterInitCallBackMap.get(key);
    afterInitCallBackMap && afterInitCallBackMap.put(key, undefined)
    callBack && callBack();
}

function initTopButton() {
    var key = getKey();
    var scriptType = $("#scriptType").val();
    var canRun = ($("#canRun").val() && $("#canRun").val() == "true") ? true : false
    var hasDependencyButton = canRun
    if (canRun) {
        hasDependencyButton = $("#isShow").val() != 1 && !isTemplate() && ($("#scriptType").val() == 2 || $("#scriptType").val() == 3);
    }
    var canDebug = $("#scriptType").val() == 3;//
    //sql upline
    var scriptMapParams = {
        key: key,
        addScriptTab: true,
        save: true,
        saveAs: true && !isTemplate(),
        format: canRun,
        run: canRun,
        dependencyButton: hasDependencyButton,
        stop: false,
        upLine: $("#isShow").val() != 1 && !isTemplate() && (scriptType == 1 || scriptType == 2 || scriptType == 3 || scriptType == 4),
        debug: canDebug,
        git: $("#isShow").val() != 1 && $("#filePosition").val() == "general" && !isTemplate()
    }
    parentInitTopButtons && parentInitTopButtons(key, scriptMapParams);
}

/**
 * 执行初始化方法
 */
function initFunction() {
    // var fun = initFunctionMap.get(getKey());
    // fun && fun(getKey(), window);
    // initFunctionMap.remove(getKey());
}


function sqlTipCaculateTable(){
    if( $("#scriptType").val() * 1 == 1 && false){
        dataDevSqlTip.setMarketId($("#configMarketId").val() * 1);
        dataDevSqlTip.caculateTable(true,true);
    }
}

$(function () {
    initIframeKeyMap();
    initLocation();
    initEvent();
    initEdit();
    debug.init();


    /**
     * 如果有hasChangeVersion，那么不可编辑，不可保存
     */
    function initInfo() {
        var key = getKey();
        if (parent && scriptInitStatusMap && scriptInitStatusMap.get(key)) {
            scriptInitStatusMap.get(key)();
            scriptInitStatusMap.remove(key);
        }
        if ($("#hasChangVersion").val() * 1 == 1) {
            editor && editor.setReadOnly(true);
        }
    }

    function deleteLine(from, length) {
        var array = editor.session.getBreakpoints();
        var newBreakPointsArray = new Array();
        for (var index = 0; index < array.length; index++) {
            if (array[index]) {
                if (index < from) {
                    newBreakPointsArray.push(index);
                } else {
                    if (debug.canSetBreakPoint(index - length)) {
                        newBreakPointsArray.push(index - length);
                    }
                }
            }
        }
        editor.session.setBreakpoints(newBreakPointsArray, "bdp-index-lb bdp-icon");
    }

    function addLine(from, length) {
        var array = editor.session.getBreakpoints();
        var newBreakPointsArray = new Array();
        for (var index = 0; index < array.length; index++) {
            if (array[index]) {
                if (index < from) {
                    newBreakPointsArray.push(index);
                } else {
                    if (debug.canSetBreakPoint(index + length)) {
                        newBreakPointsArray.push(index + length);
                    }
                }
            }
        }
        editor.session.setBreakpoints(newBreakPointsArray, "bdp-index-lb bdp-icon");
    }


    function initAce() {



        //初始化对象
        editor = ace.edit("code");
        //设置风格和语言（更多风格和语言，请到github上相应目录查看）
        editor.setTheme("ace/theme/chrome");

        var scriptType = $("#scriptType").val();
        var scriptObj = getScriptObj(scriptType);
        var language = scriptObj.aceMode;

        editor.session.setMode("ace/mode/" + language);
        //字体大小
        editor.setFontSize(14);
        //设置只读（true时只读，用于展示代码）
        editor.setReadOnly(false);

        //自动换行,设置为off关闭
        editor.setOption("wrap", "free");
        editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: false,
            enableLiveAutocompletion: true
        });
        //启用提示菜单
        languageTools = ace.require("ace/ext/language_tools");

        if(scriptType * 1 === 1 && false){
            //sql 类型
            dataDevSqlTip.setEditor(editor);
            languageTools.addCompleter({
                getCompletions: function (editor, session, pos, prefix, callback) {
                    var tipResult = dataDevSqlTip.getTipResult();
                    if(tipResult.tipList && tipResult.tipList.length > 0){
                        callback(null, tipResult.tipList);
                    }
                }
            });

        }



        editor.getSession().on('change', function (e) {
            var start = e.start;
            var end = e.end;
            var action = e.action;

            if (action == "remove") {
                if (start.row < end.row) {
                    deleteLine(start.row + 1, end.row - start.row);
                }
            } else if (action == "insert") {
                if (start.row < end.row) {
                    addLine(start.row + 1, end.row - start.row);
                } else if (start.row == end.row && !debug.canSetBreakPoint(start.row) && editor.session.getBreakpoints()[start.row]) {
                    editor.session.clearBreakpoint(start.row);
                }
            }



            //当前光标在脚本中的位置
            var cursor = (editor.selection.getCursor());
            isEditContent = true ;

        });
        initScriptContent(true);
        editor.commands.addCommand({
            name: 'myCommand4',
            bindKey: {win: 'Ctrl-Shift-F', mac: 'Ctrl-Alt-L'},
            exec: function (editor) {
                format && format();
            },
            readOnly: true // 如果不需要使用只读模式，这里设置false
        });
        editor.commands.addCommand({
            name: 'rename',
            bindKey: {win: 'F2', mac: 'F2'},
            exec: function (editor) {
                if ($("#isShow").val() == 0) {
                    renameScript();
                }
            },
            readOnly: true // 如果不需要使用只读模式，这里设置false
        });


        editor.session.selection.on('changeCursor', function (e) {
            var cursor = (editor.selection.getCursor());
            $("#rownumber").html(cursor.row + 1);
            $("#colnumber").html(cursor.column + 1);



            if (scriptType * 1 == 1 && isEditContent  === true && false) { //sql 类型
                var cusorIndex = 0;
                for (var index = 0; index < cursor.row; index++) {
                    cusorIndex += editor.getSession().getLine(index).length + 1;
                }
                cusorIndex += cursor.column ;
                dataDevSqlTip.setSqlAndCusorPosition(editor.getValue(), cusorIndex)
                dataDevSqlTip.sqlTip();
            }

            isEditContent = false ;
           // editor.execCommand("startAutocomplete");


        });
        editor.on("paste", function (obj) {
            if (obj.text.endWith("\n")) {
                obj.text = obj.text.substring(0, obj.text.length - 1);
            }
            console.log(obj)
        })
    }

    function initUnEditTopButton() {
        var key = getKey();
        var scriptType = $("#scriptType").val();
        var canRun = ($("#canRun").val() && $("#canRun").val() == "true") ? true : false;
        var hasDependencyButton = canRun;
        if (canRun) {
            hasDependencyButton = $("#isShow").val() != 1 && (scriptType == 2 || scriptType == 3) && !isTemplate();
        }
        var scriptMapParams = {
            key: key,
            addScriptTab: true,
            save: false,
            saveAs: false,
            edit: false,
            format: false,
            run: canRun,
            dependencyButton: hasDependencyButton,
            stop: false,
            upLine: (scriptType == 4) && !isTemplate(),
            debug: false,
            git: $("#isShow").val() != 1 && $("#filePosition").val() == "general" && !isTemplate()
        }
        parentInitTopButtons && parentInitTopButtons(key, scriptMapParams);
        refreshFileInfoStatus(null, null, $("#gitStatus").val());
    }


    function initConfigAndArgs() {
        changeConfig && changeConfig($("#configId").val(), getKey(), true)
    }


    function initEvent() {
        $(".moreFunction li").click(function () {
            if ($(this).hasClass("event-disabled")) {
                return false;
            }
            var enevntType = $(this).attr("envent-type");
            if (enevntType === "run") {
                command2run();
            }
            if (enevntType === "debug") {
                command2debug();
            }
            if (enevntType === "stop") {
                command2stop();
            }
            if (enevntType === "save") {
                command2save();
            }
            if (enevntType === "git") {
                return false;
            }
            if (enevntType === "scriptUrl") {
                shareScript && shareScript($("#gitProjectId").val(), $("#gitProjectFilePath").val())
            }
            if (enevntType === "position") {
                locationScript && locationScript($("#gitProjectId").val(), $("#gitProjectFilePath").val());
            }
            if (enevntType === "copyfilename") {
                copyValue($("#fileName").val());
            }
            if (enevntType === "copyfilepath") {
                copyValue($("#gitProjectFilePath").val());
            }
            if (enevntType === "addtemplate") {
                saveTemplate && saveTemplate(datadevInit.getActiveWindow(getKey()));
            }
            if (enevntType === "down") {
                downloadScript && downloadScript($("#gitProjectFilePath").val(), $("#gitProjectId").val());
            }
            if (enevntType === "rename") {
                renameScript();
            }
            hiddenMoreFunction();
        })
        $("#rightClickGitMenu").on("click", "li", function () {

            $("#rightClickGitMenu").hide();
            var liClass = $(this).attr("class");
            if (liClass == "pull") {
                pullFile && pullFile($("#gitProjectId").val(), $("#gitProjectFilePath").val())
            } else if (liClass == "push") {
                pushFile && pushFile($("#gitProjectId").val(), $("#gitProjectFilePath").val())
            } else if (liClass == "showGitHistory") {
                showGitHistory && showGitHistory($("#gitProjectId").val(), $("#gitProjectFilePath").val(), false);
            }
        })
        //右键目录git
        $("#scriptGit").mouseenter(function () {
            if ($(this).hasClass("event-disabled")) {
                return false;
            } else {
                var newScriptMenu = $("#rightClickGitMenu");
                newScriptMenu.attr("data-type", "dir");
                var top = $(this).offset().top;
                var left = $(this).offset().left;
                left = left + $(".moreFunction").width();
                var menuWidth = $("#rightClickGitMenu").width();
                if (getIframeOffset && getWindowSize) {
                    var offset = getIframeOffset();
                    var size = getWindowSize();
                    if (offset && size) {
                        var windowWidth = size.width;
                        if (menuWidth + left + offset.left + 10 > windowWidth) {
                            left = left - $(".moreFunction").width() - menuWidth;
                            left = left < 0 ? 0 : left;
                        }
                    }
                }
                newScriptMenu.css({top: top, left: left}).show();
            }
        })
        $("#scriptGit").mouseleave(function () {
            var newMenu = $("#rightClickGitMenu");
            newMenu.hide();
        })
        $("#rightClickGitMenu").mouseenter(function () {
            $("#rightClickGitMenu").show();
        })
        $("#rightClickGitMenu").mouseleave(function () {
            $("#rightClickGitMenu").hide();
        })
        $("#code").mousedown(function (event) {
            showMorefunction(event);
            return false;
        })
        $("#code").click(function (event) {
            hiddenMoreFunction();
        })

        $("#cheDepDiv").click(function () {
            choDep && choDep($('#gitProjectId').val(), $("#gitProjectFilePath").val());
        })
        $("#packTreeUl").on("click", "input.checkbox", function (event) {
            var activeLi = $(this).parent().parent().parent();
            var parentPath = activeLi.attr("data-parent");
            var path = activeLi.attr("data-path");
            var isChecked = $(this).is(":checked");
            var nodeObj = packDetailMap.get(rootPath + path);
            nodeObj.tbChecked = isChecked;
            var chilLisArray = [];
            if (isChecked) {
                //向上处理选中
                var isHalfCheck = false;
                while (parentPath) {
                    var parentLi = $("li[data-path='" + parentPath + "']", $("#packTreeUl"));
                    var parentNode = packDetailMap.get(rootPath + parentPath);
                    if (!isHalfCheck) {
                        var chiNs = parentNode.childrens;
                        for (var i = 0; i < chiNs.length; i++) {
                            if (!chiNs[i].tbChecked) {
                                isHalfCheck = true;
                                break;
                            }
                        }
                    }
                    var nodeDIv = parentLi.children(".nodeDiv");
                    if (!isHalfCheck) {
                        nodeDIv.addClass("selectNode");
                        nodeDIv.find("input.checkbox").removeClass("part_check");
                        nodeDIv.find("input.checkbox").prop("checked", true);
                        parentNode.tbChecked = true;
                    } else {
                        nodeDIv.find("input.checkbox").addClass("part_check");
                    }
                    parentPath = parentLi.attr("data-parent");
                }
                //向下处理选中
                chilLisArray.push(activeLi);
                while (chilLisArray.length > 0) {
                    var li = chilLisArray.shift();
                    var nodeDIv = li.children(".nodeDiv");
                    nodeDIv.addClass("selectNode");
                    nodeDIv.find("input.checkbox").prop("checked", true);
                    path = li.attr("data-path");
                    var node = packDetailMap.get(rootPath + path);
                    node.tbChecked = true;
                    var chiLis = $("li[data-parent='" + path + "']", $("#packTreeUl"));
                    if (chiLis.length > 0) {
                        for (var i = 0; i < chiLis.length; i++) {
                            var li = $(chiLis[i]);
                            chilLisArray.push(li);
                        }
                    }
                }

            } else {
                $("#checkAll").prop("checked", false);

                //向上处理取消
                while (parentPath) {
                    var parentNode = packDetailMap.get(rootPath + parentPath);
                    parentNode.tbChecked = false;
                    var parentLi = $("li[data-path='" + parentPath + "']", $("#packTreeUl"));
                    var nodeDIv = parentLi.children(".nodeDiv");
                    var checkeds = $("li[data-parent='" + parentPath + "'] > .nodeDiv input.checkbox:checked", $("#packTreeUl"));
                    var halfChecks = $("li[data-parent='" + parentPath + "'] > .nodeDiv input.checkbox.part_check", $("#packTreeUl"));
                    var thisCheckBox = nodeDIv.find("input.checkbox");
                    nodeDIv.removeClass("selectNode");
                    thisCheckBox.prop("checked", false);
                    if (checkeds.length > 0 || halfChecks.length > 0) {
                        thisCheckBox.addClass("part_check");
                    }
                    parentPath = parentLi.attr("data-parent");
                }
                //向下取消
                chilLisArray.push(activeLi);
                while (chilLisArray.length > 0) {
                    var li = chilLisArray.shift();
                    var nodeDIv = li.children(".nodeDiv");
                    nodeDIv.removeClass("selectNode");
                    nodeDIv.find("input.checkbox").prop("checked", false);
                    path = li.attr("data-path");
                    var chilNode = packDetailMap.get(rootPath + path);
                    chilNode.tbChecked = false;
                    var chiLis = $("li[data-parent='" + path + "']", $("#packTreeUl"));
                    if (chiLis.length > 0) {
                        for (var i = 0; i < chiLis.length; i++) {
                            var li = $(chiLis[i]);
                            chilLisArray.push(li);
                        }
                    }
                }
            }
            event.stopPropagation();
            // return false
            // alert($(this).parents("li").attr("data-path"));
        })
        $("#update2NewVersion").click(function () {
            var checkBoxs = $("#packTreeUl input:checkbox:checked");
            if (checkBoxs.length == 0) {
                top.$.errorMsg("请勾选至少一个脚本进行更新");
                return;
            }
            var array = [];
            var gitProjectFilePath = $("#gitProjectFilePath").val();
            var gitProjectId = $("#gitProjectId").val();
            for (var i = 0; i < checkBoxs.length; i++) {
                var checkBox = $(checkBoxs[i]);
                var li = checkBox.closest("li");
                var path = li.attr("data-path");
                if (li.hasClass("file-li")) {
                    var version = li.children(".nodeDiv").find(".lastVersion").attr("data-version");
                    array.push({
                        gitProjectId: gitProjectId,
                        path: path,
                        version: version,
                        parChl: 1
                    })
                }
            }
            var data = JSON.stringify(array);
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/updateNewVersion.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath,
                data: data
            }, $("#update2NewVersion"), function (node, data) {
                if (data.obj) {
                    var relationDependencyId = data.obj.relationDependencyId;
                    var version = data.obj.version;
                    if (version) {
                        $("#currentRelationDependencyId").val(relationDependencyId)
                        $("#relationDependencyId").val(relationDependencyId)
                        packDetail($("#currentRelationDependencyId").val());
                        $("#version").val(version);
                        $("#lastVersion").val(version);
                        $("#currentVersionInfo").text(version);
                        $("#lastVersionInfo").text(version);
                        removeVersionChange();
                    }
                }
                top.$.successMsg("更新成功");
            }, null, null, null, null, function () {
                top.$.loadingMsg("更新打包中...")
            })
        })
        $("#packTreeUl").on("click", "li.file-li span.script-name", function () {
            var li = $(this).closest("li");
            var gitProjectId = $("#gitProjectId").val();
            var gitProjectFilePath = li.attr("data-path");
            openScript && openScript(gitProjectId, gitProjectFilePath)
        })
        $("#packTreeUl").on("click", ".version span", function () {
            var versionDiv = $(this).closest(".version");
            if ($(versionDiv).hasClass("noVersion")) {
                return;
            }
            var version = $(versionDiv).attr("data-version");
            var li = $(versionDiv).closest("li");
            var gitProjectFilePath = li.attr("data-path");
            var gitProjectId = $('#gitProjectId').val();
            openScriptContent(gitProjectId, gitProjectFilePath, version)
            return false
        })
        $("#packTreeUl").on("click", "li.dir-li > .nodeDiv .script-file", function () {
            var li = $(this).closest("li.dir-li");
            var openClass = getScriptObj(-3).iconClass;
            var closeClass = getScriptObj(-1).iconClass;
            var iconSpan = $(li).children(".nodeDiv").find(".script-icon");
            if (li.hasClass("close-li")) {
                li.removeClass("close-li").addClass("open-li");
                iconSpan.removeClass(closeClass).addClass(openClass)
            } else if (li.hasClass("open-li")) {
                li.addClass("close-li").removeClass("open-li");
                iconSpan.removeClass(openClass).addClass(closeClass)
            }
            return false;
        })
        $("#checkAll").click(function () {
            $("#packTreeUl input.checkBox").removeClass("part_check");
            if ($(this).is(":checked")) {
                $("#packTreeUl input.checkBox").prop("checked", true);
                $("#packTreeUl .nodeDiv").addClass("selectNode");
            } else {
                $("#packTreeUl input.checkBox").prop("checked", false);
                $("#packTreeUl .nodeDiv").removeClass("selectNode");
            }
        })
        $("#chooseVersionBtn").click(function () {
            datadevInit && datadevInit.mergeChoose($("#version").val() != $("#lastVersion").val(),undefined);
        })
    }

    function initEdit() {
        /**
         * 可编辑肯定可以运行
         */
        if ($("#currentRelationDependencyId").val() && $("#currentRelationDependencyId").val() > 0) {
            $("#packZipDetail").show();
            packDetail($("#currentRelationDependencyId").val());
            initUnEditTopButton();
        } else if ($("#canEdit").val() && $("#canEdit").val() == "true") {
            $("#code").empty();
            initAce();
        } else {
            $("#code").html("（该脚本类型不支持预览，编辑！）").css("text-align", "center").css("font-size", "16px").css("padding-top", "11%");
            initContentStatus = true;
            initUnEditTopButton();
            initFunction();
        }
        //放在 initAce之后
        initConfigAndArgs();
        initInfo();
    }

})

function changeLastMender(data) {
    $("#lastmender").html(data.obj.lastmender);
    $("#lastmodified").html(data.obj.lastmodified);
    $("#currentVersionInfo").html(data.obj.currentVersion);
    $("#lastVersionInfo").html(data.obj.lastVersion);
    $("#version").val(data.obj.currentVersion);
}

function clearRunDirectVersion(data) {
    $("#hasChangVersion").val("0");
    $("#cancelForceEditSpan").css("display", "none");
}
function parentButtonClick(buttonId) {
    $("#" + buttonId).click();
}

$(".changeVersion").click(function () {

    if ($("#hasChangVersion").val() * 1 == 0) {
        var node = $(this);
        var version = node.html();
        $("#version").val(version);
        initScriptContent();
    }

})

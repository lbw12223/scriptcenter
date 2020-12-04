var START_MOVE_SPLITHANDER = false;
var START_MOVE_SPLITHANDER_PAGE_X = 0;
var E_RESIZE_STATUS = false;
var S_RESIZE_STATUS = false;
var leftMenuWidth = 0;
var windowWidth = 0;
var $homdeIndexLeftMenu = undefined;
var $homdeIndexRightContent = undefined;
var windowHeight = 0;
var addScriptUrl = "/scriptcenter/script/addScript.ajax";
var stopUrl = "/scriptcenter/script/stop.ajax";
var getConfigUrl = "/scriptcenter/config/getConfigByErp.ajax";
var saveContentUrl = "/scriptcenter/script/saveContent.ajax";
var getScriptBaseUrl = "/scriptcenter/scriptFile/getInfo.ajax";
var getTemplateHtml = "/scriptcenter/scriptTemplate/templates.html";
var configObj = [];
var runStatusMap = new Map();//key:运行id value:0表示没完成 1表示完成 2停止
var lazyLocation = new Array();
var initFunctionMap = new Map();//打开脚本需要初始化的操作 ，key 脚本key  value 需要的操作function
var debugDependentMap = new Map();//key:脚本key   value: 依赖key的脚本列表
var debugDepndendedMap = new Map();//key:脚本key   value: key依赖的脚本列表
var saveStatusMap = new Map();//key script-key  value 0 可以保存  1表示正在保存
var mouseMoveChangeEvents = new Array();
var cg = "false";
/*
 存放脚本的信息
 * scriptId
 * canSave;
 * canUpLine:
 * canRun
 * canStop
 */
var TOP_BUTTON = {
    addScriptTab: "addScriptTab",
    save: "save",
    saveAs: "saveAs",
    format: "format",
    run: "run",
    dependencyButton: "dependencyButton",
    stop: "stop",
    upLine: "upLine",
    debug: "debug",
    git: "git"
}
var scriptMap = new Map();
var scriptMergeContentMap = new Map();
var scriptInitStatusMap = new Map();//1打开详情标签
/**
 * 隐藏所有的邮件
 */
function hiddenAllRightMeun() {

    $("#codeEditContainer").JdDataDevTab("hiddenTabPullDown");
    $("#pullPushMenuContent").hide();
    $("#newScriptDiv").hide();
    $("#gitMenuDiv").hide();
    $("#rightClickScriptMenu").hide();
    $("#rightClickTargetDirMenu").hide();
    $("#rightClickMenu").hide();
    $("#mouseScriptDiv").hide();
    $("#gitMenuDiv").hide();
    $("#queueCodeDropDiv").hide();
    $('.showTitleBox').remove();
    $(".datadevSelectedText").removeClass("datadevSelectedText");
    var activeWindow = datadevInit.getActiveWindow();
    if (activeWindow && activeWindow.win) {
        activeWindow.win.hideClearLog && activeWindow.win.hideClearLog();
        activeWindow.win.hideMoreFunction && activeWindow.win.hideMoreFunction();
    }

}

function canPush(gitStatus) {
    return (gitStatus == "ADD" || gitStatus == "MOD" || gitStatus == "DEL");
}

function showModifyIcon(key, isShow) {
    $("#codeEditContainer").JdDataDevTab("showModifyIcon", key, isShow);
}

//重置url用于导入脚本成功后，改变url
function resetUrl() {
    history && history.pushState(null, null, "/scriptcenter/index.html")
}

function popUrl() {
    history && history.back(-1);

}

function parentButtonClick(buttonId) {
    $("#" + buttonId).click();
}

function showAccountErrorByLog(key) {
    var activeWindow = datadevInit.getActiveWindow();
    var activeKey = getKey(activeWindow.gitProjectId, activeWindow.gitProjectFilePath);
    if (key && key == activeKey) {
        showAccountError();
    }
}

function removeScriptTabByKey(key) {
    $("#codeEditContainer").JdDataDevTab("removeScript", key);
}

function showAccountError() {
    $("#queueCode").addClass("runError");
    var top = $("#queueCode").offset().top + $("#queueCode").height() + 2;
    var left = $("#queueCode").offset().left;
    $("#runErrorDiv").css({top: top, left: left}).show();
    window.setTimeout(function () {
        $("#runErrorDiv").hide();
        $("#queueCode").removeClass("runError");
    }, 2000)
}


/*整个重新刷新顶部按钮*/
function parentInitTopButtons(key, scriptMapParams) {
    scriptMap.put(key, scriptMapParams);
    if (key && datadevInit.isActive(key)) {
        datadevInit.changeTopButtons(key);
    }
}

/*只刷新某个按钮*/
function parentInitTopButton(key, properties, value) {
    var scriptMapParams = scriptMap.get(key);
    if (scriptMapParams) {
        scriptMapParams[properties] = value;
    } else {
        var scriptMapParams = {
            key: key,
            configId: undefined,
            addScriptTab: true,
            save: true,
            saveAs: true,
            format: false,
            run: false,
            debug: false,
            dependencyButton: false,
            stop: false,
            upLine: false,
            git: false
        }
        scriptMap.put(key, scriptMapParams);
    }
    // datadevInit.changeTopButtons(key)
    if (key && datadevInit.isActive(key)) {
        datadevInit.changeTopButton(properties, value == true ? "available" : "unavailable", key);
    }
}

function disableAllTopButton(scriptKey) {
    var scriptMapParams = scriptMap.get(scriptKey);
    if (scriptMapParams) {
        parentInitTopButton(scriptKey, TOP_BUTTON.stop, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.save, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.saveAs, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.format, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.run, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.debug, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.dependencyButton, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.upLine, false);
        parentInitTopButton(scriptKey, TOP_BUTTON.git, false);
    }
}

/*upline sql upline*/
function resetAllTopButton(scriptWindow) {
    if (scriptWindow && scriptWindow.win && scriptWindow.jq) {
        var scriptKey = scriptWindow.key;
        var scriptType = scriptWindow.type;
        var scriptObj = getScriptObj(scriptType);
        var canEdit = scriptObj.canEdit;
        var canRun = scriptObj.canRun;
        var isTmp = scriptWindow.jq("#isShow").val() == 1;
        var isTmplate = scriptWindow.win.isTemplate && scriptWindow.win.isTemplate();
        var isTarget = scriptWindow.jq("#filePosition").val() == "target";
        var scriptMapParams = scriptMap.get(scriptKey);
        if (scriptMapParams) {
            parentInitTopButton(scriptKey, TOP_BUTTON.stop, false);
            parentInitTopButton(scriptKey, TOP_BUTTON.save, canEdit);
            parentInitTopButton(scriptKey, TOP_BUTTON.saveAs, canEdit);
            parentInitTopButton(scriptKey, TOP_BUTTON.format, canEdit);
            parentInitTopButton(scriptKey, TOP_BUTTON.run, canRun);
            parentInitTopButton(scriptKey, TOP_BUTTON.debug, scriptType == 3);
            parentInitTopButton(scriptKey, TOP_BUTTON.dependencyButton, !isTmp && !isTmplate && (scriptType == 2 || scriptType == 3));
            parentInitTopButton(scriptKey, TOP_BUTTON.upLine, !isTmp && !isTmplate && (scriptType == 1 || scriptType == 2 || scriptType == 3 || scriptType == 4));
            parentInitTopButton(scriptKey, TOP_BUTTON.git, !isTmp && !isTmplate && !isTarget);
        }
        if (scriptWindow && scriptWindow.win) {
            scriptWindow.win.resetVersionChange && scriptWindow.win.resetVersionChange();
        }
    }

}

function applyBuffaloMarket() {
    commonAjaxEvents.commonPostAjax("/scriptcenter/buffalo/getAllMarket.ajax", {}, null, function (node, data) {
        var obj = data.obj;
        var options = "<option value=''>请选择</option>";
        if (obj && obj.length > 0) {
            for (var index = 0; index < obj.length; index++) {
                options += "<option value='" + obj[index].marketId + "' data-cluster='" + obj[index].clusterCode + "' data-linuxuser='" + obj[index].linuxUser + "' data-ugdap='" + obj[index].isUgdap + "'>" + obj[index].marketName + "</option>";
            }
        }
        /*background: #2A2F33;*/
        var html = "<div style='margin-top: 30px'><span  style='color: #DC9116;'>提示：请选择需要申请的账号和队列所在集市，系统将根据集市是否支持UGDAP引导到对应的申请流程页面。</span></div>";
        html += "<div style='margin-top: 30px;overflow: hidden;width: 100%;height: 35px;'><div style='float: left;width: 80px;position: relative;top: 5px'><i class='bdp-note'>*</i>申请集市：</div><select style='float: left;width: 350px' id='applyAllMarket'>" + options + "</select></div>";
        html += "<div id='marketSelectErrorDiv' style='position:absolute;padding-left: 80px;color: red;display: none;'>请选择需要开通权限的集市</div>";
        $.bdpMsg({
            title: "集市账号和队列权限申请",
            mainContent: html,
            width: "500px",
            afterCloseCallBack: function () {
                $("#select2-drop-mask").click();
            },
            buttons: [
                {
                    text: "确定",
                    event: function () {
                        var marketId = $("#applyAllMarket").find("option:selected").val();
                        if (marketId) {
                            var isUdgap = $("#applyAllMarket").find("option:selected").attr("data-ugdap");
                            var marketName = $("#applyAllMarket").find("option:selected").text();
                            var clusterCode = $("#applyAllMarket").find("option:selected").attr("data-cluster");
                            var linuxUser = $("#applyAllMarket").find("option:selected").attr("data-linuxuser");

                            if (isUdgap == 0) {
                                var data = "{select_market:'" + marketName + "(" + linuxUser + ")" + clusterCode + "#@8@#" + clusterCode + "#" + linuxUser + "'}";
                                data = encodeURIComponent(data);
                                $.removeMsg();
                                window.open($("#ugdapWorkFlowUrl").val() + "&data=" + data);
                            } else {
                                var data = "{select_market:'" + marketName + "(" + linuxUser + ")" + ":" + marketId + "'}";
                                data = encodeURIComponent(data);
                                $.removeMsg();
                                window.open($("#norWorkFlowUrl").val() + "&data=" + data);
                            }
                        } else {
                            $("#marketSelectErrorDiv").show();
                        }
                    },
                    btnClass: 'bdp-btn-primary'
                },
                {
                    text: "取消",
                    event: function () {
                        $("#select2-drop-mask").click();
                        $.removeMsg();
                    }
                }
            ]
        }, function () {
            $("#applyAllMarket").select2({
                placeholder: "请选择",
            })
            $("#applyAllMarket").on("change", function () {
                if ($("#applyAllMarket").val()) {
                    $("#marketSelectErrorDiv").hide();
                } else {
                    $("#marketSelectErrorDiv").show();
                }
            })
        })
    });

}

function copyContent(value) {
    var input = document.createElement('textarea');
    input.textContent = value;
    document.body.appendChild(input);
    input.focus();
    input.setSelectionRange(0, input.value.length);
    if (document.execCommand('copy')) {
        document.execCommand('copy');
    }
    document.body.removeChild(input);
}

function askNewGit() {
    $.confirmMsg({
        content: "当前无项目，是否选择创建项目?",
        buttons: [
            {
                text: "确定",
                event: function () {
                    $.removeMsg();
                    var projectArt = $.dialog.open("/scriptcenter/project/newGitProject.html", {
                        title: "新项目",
                        lock: true,
                        width: "600px",
                        height: "400px",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    });
                    $.dialog.data("projectArt", projectArt);
                }
            },
            {
                text: "取消"
            }
        ]
    })
}

function getRightPosition() {
    return $homdeIndexRightContent.position();
}

function wait() {
    $("#spins").show();
    var opts = {
        lines: 13, // 花瓣数目
        length: 20, // 花瓣长度
        width: 10, // 花瓣宽度
        radius: 30, // 花瓣距中心半径
        corners: 1, // 花瓣圆滑度 (0-1)
        rotate: 0, // 花瓣旋转角度
        direction: 1, // 花瓣旋转方向 1: 顺时针, -1: 逆时针
        color: '#5882FA', // 花瓣颜色
        speed: 1, // 花瓣旋转速度
        trail: 60, // 花瓣旋转时的拖影(百分比)
        shadow: false, // 花瓣是否显示阴影
        hwaccel: false, //spinner 是否启用硬件加速及高速旋转
        className: 'spinner', // spinner css 样式名称
        zIndex: 2e9, // spinner的z轴 (默认是2000000000)
        top: "auto", // spinner 相对父容器Top定位 单位 px
        left: "auto"// spinner 相对父容器Left定位 单位 px
    };
    var spinner = new Spinner(opts);
    spinner.spin($("#spins")[0]);
    return spinner;
}

function cancelWait(spinner) {
    $("#spins").hide();
    spinner.spin();
}

function getLeftPosition() {
    return $homdeIndexLeftMenu.position();
}

function resetSelect(configId) {
    configId = configId || -1;
    var lis = "<li class='defaultDropLi queueCodeDropLi' data-index='-1'><span class='name'>不使用集市资源</span></li>";
    var selectStatus = false;
    for (var index = 0; index < configObj.length; index++) {
        var config = configObj[index];
        if (configId > 0 && config.id == configId) {
            $("#queueName").attr("data-index", config.id)
            $("#queueName").text(config.name);
            selectStatus = true;
        }
        lis += "<li class='queueCodeDropLi' data-index='" + config.id + "'><span class='name'>" + config.name + "</span></li>";
    }
    if (!selectStatus) {
        setDefaultConfig($(lis), true);
    } else {
        $("#queueCodeDropDiv li.queueCodeDropLi[data-index='" + configId + "']").addClass("active").siblings("li.queueCodeDropLi").removeClass("active");
    }
    $("li.queueCodeDropLi").remove();
    $("#queueCodeDropDiv ul.queueCodeDropUl").prepend(lis);
}

function setDefaultConfig(lis, isInit) {
    $("#queueCodeDropDiv .defaultDropLi").addClass("active").siblings("li.queueCodeDropLi").removeClass("active")
    $("#queueName").attr("data-index", "-1")
    $("#queueName").text(isInit && lis && lis.length == 1 ? "配置账号队列" : "不使用集市资源");
}

/**
 * 通过configId 获取config
 *
 * @param configId
 */
function getConfigById(configId) {
    for (var index = 0; index < configObj.length; index++) {
        if (configObj[index].id == configId) {
            return configObj[index];
        }
    }
    return undefined;
}

/**
 * 修改选中的集市资源
 * @param configId
 * @param key
 * @param isInit
 */
function changeConfig(configId, key, isInit) {
    configId = configId || -1;
    var configDetail = getConfigById(configId);
    datadevInit.getActiveWindow(key).jq && datadevInit.getActiveWindow(key).jq("#configId").val(configId);
    if (configDetail) {
        datadevInit.getActiveWindow(key).jq && datadevInit.getActiveWindow(key).jq("#configMarketId").val(configDetail.marketId);
        datadevInit.getActiveWindow(key).win.sqlTipCaculateTable();
    }


    var lis = $("li.queueCodeDropLi", $("#queueCodeDropDiv"));
    var findStatus = false;
    if (configId && configId > 0) {
        $(lis).each(function (index, element) {
            if ($(this).attr("data-index") == configId) {
                var dataIndex = $(this).attr("data-index");
                var name = $(this).find("span.name").text();
                $("#queueName").attr("data-index", dataIndex).text(name);
                $(this).addClass("active").siblings("li.queueCodeDropLi").removeClass("active");
                findStatus = true;
                return false;
            }
        })
    }
    if (!findStatus) {
        setDefaultConfig(lis, isInit);
    }
}

//2018-12-12 添加args参数
/**
 *
 * @param script 脚本对象  主要是类型
 * @param treeNode 页面目录node
 * @param gitProjectId 项目id
 * @param content 脚本内容
 * @param isTemplate true表示创建的脚本是一个模板 ，false或者默认表名是一个普通脚本
 * @param templateId 模板id表明由模板创建一个脚本，content失效
 */
function addScript(script, treeNode, gitProjectId, content, isTemplate, templateId) {
    var type = script.scriptType;
    var pythonType = script.pythonType;
    var dirPath = (treeNode && treeNode.isParent && treeNode.path) ? treeNode.path : "";
    templateId = templateId || 0;
    commonAjaxEvents.commonPostAjax(addScriptUrl, {
        type: type,
        gitProjectId: gitProjectId,
        isShow: 1,
        gitProjectDirPath: dirPath,
        content: content,
        templateId: templateId,
        isTemplate: !!isTemplate,
        pythonType: pythonType || ""
    }, $("#tree"), function (node, data) {
        if (data && data.obj) {
            var zTreeNode = data.obj;
            while (zTreeNode.children && zTreeNode.children.length > 0) {
                zTreeNode = zTreeNode.children[0];
            }
            initFunctionMap && initFunctionMap.put(getKey(gitProjectId, zTreeNode.path), function (key, scriptWin) {
                scriptWin && scriptWin.showArgu && scriptWin.showArgu();
            });
            openScript(zTreeNode.gitProjectId, zTreeNode.path, getTemScriptName(type, isTemplate), pythonType, true, dirPath);
            // openScript(gitProjectId, zTreeNode.path, "未命名1", pythonType, true, dirPath);
        }
    });
}

//获取临时文件名字
function getTemScriptName(type, isTemplate) {

    var tmpPattern = /^(模板\-)?未命名([0-9]+)(\.\w+)?$/;
    var list = $("#codeEditContainer").JdDataDevTab("getAllTab");
    var max = 0;
    if (list && list.length > 0) {
        for (var index = 0; index < list.length; index++) {
            var label = $(list[index]).attr("data-label");
            max = (label && label.match(tmpPattern) && label.match(tmpPattern)[2]) ? Math.max(max, label.match(tmpPattern)[2]) : max;
        }
    }
    var script = getScriptObj(type);
    var scriptName = "";
    if (isTemplate) {
        scriptName = "模板-" + "未命名" + (max + 1);
    } else {
        scriptName = "未命名" + (max + 1) + ((script && script.suffix) ? ("." + script.suffix) : "");
    }
    return scriptName;
}

function getKey(gitProjectId, path) {
    gitProjectId = (gitProjectId + "").trim();
    path = path && path.trim() || "";
    if (path.startsWith("/")) {
        path = gitProjectId + path;
    } else {
        path = gitProjectId + "/" + path.trim();
    }
    return path;
}


/**
 * @param nowGitProjectId  新建的时候projectId
 * @param isTemporary  是否是临时文件
 * @param path 脚本全路径
 * @param name 脚本名字带后缀
 * @param pythonType python版本 1：python2   2：python3
 * @param dirPath 父目录
 * @param version 版本号
 */
function openScript(nowGitProjectId, path, name, pythonType, isTemporary, dirPath, version) {
    if (!path) {
        $.bdpUtil.alertDialog($("#tree"), "脚本path为空，不能打开脚本");
    }
    if (!name) {
        var index = path.lastIndexOf("/");
        name = index != -1 ? path.substring(index + 1) : path;
    }
    var params = getParam(nowGitProjectId, path, name, pythonType, isTemporary);
    HOME_COOKIE.addTabCookie(nowGitProjectId, params.key, params, isTemporary, dirPath);
    if (version) {
        modifyUrl(params, "version", version);
    }
    if (!datadevInit.getActiveWindow(params.key) || !datadevInit.getActiveWindow(params.key).status) {
        var scriptMapParams = {
            key: params.key,
            configId: undefined,
            addScriptTab: true,
            save: false,
            saveAs: false,
            format: false,
            run: false,
            stop: false,
            upLine: false,
            dependencyButton: false,
            debug: false,
            git: false
        }
        scriptMap.put(params.key, scriptMapParams);
    }
    var targetLiId = $("#codeEditContainer").JdDataDevTab("addTab", params);
    if (targetLiId) {
        $("#" + targetLiId).trigger("click");
    }

}


function getUrl(gitProjectId, path, pythonType) {
    var encodepath = encodeURIComponent(encodeURIComponent(path.trim()));
    var url = "/scriptcenter/home/home_open_ide.html?gitProjectFilePath=" + encodepath + "&gitProjectId=" + gitProjectId;
    if (pythonType) {
        url += "&pythonType=" + pythonType;
    }
    return url;
}

/**
 *  判断目录方式运行，子文件是否有未保存情况，有则返回文件path，没有返回null
 *  有未保存是提示找到的第一个未保存的文件保存
 */
function verifyNoSave(gitProjectId, gitProjectFilePath) {
    // var parNode = getTheRunTypeNode(gitProjectFilePath);
    //
    // if (parNode != null) {
    //     var parPath = parNode.path;
    //     var keyPrefix = getKey(gitProjectId, parPath);
    //     keyPrefix = keyPrefix.endWith("/") ? keyPrefix : keyPrefix + "/";
    //     var windows = datadevInit.getAllWindow();
    //     for (var i = 0; i < windows.length; i++) {
    //         var window = windows[i];
    //         if (window.key.startsWith(keyPrefix) && window.verifyModify && window.verifyModify()) {
    //             return window.gitProjectFilePath;
    //         }
    //     }
    // }
    return null;
}

function getTheRunTypeNode(gitProjectFilePath) {
    if (zTree) {
        var ztreeNode = zTree.getNodeByParam("path", gitProjectFilePath);
        if (ztreeNode) {
            var nodes = ztreeNode.getPath();
            if (nodes && nodes.length > 0) {
                for (var i = nodes.length - 2; i >= 0; i--) {
                    if (nodes[i].runType == 1) {
                        return nodes[i];
                    }
                }
            }
        }
    }
    return null;
}

function getParam(gitProjectId, path, name, pythonType, isTmp) {
    var url = getUrl(gitProjectId, path, pythonType);
    var key = getKey(gitProjectId, path);
    var params = {
        url: url,
        label: name || "bdp_script",
        canRemove: true,
        path: path,
        title: isTmp ? name : path,
        gitProjectId: gitProjectId,
        key: key,
    }
    return params;
}

function modifyUrl(params, paramkey, paramValue) {
    var url = params.url;
    url += "&" + paramkey + "=" + paramValue;
    params.url = url;
}

function initWindow() {
    windowWidth = $(window).width();
    windowHeight = $(window).height();
    /*  $homdeIndexLeftMenu.css("width", "20%");
     $homdeIndexRightContent.css("width", "80%");*/
}

function showPushInfo(pushNum, submitCallBack, isDir) {
    if (pushNum == 0) {
        $.successMsg("与git版本一致，不需要push。");
        return;
    }
    var html =
        "<form id='pushForm' style='padding: 20px 20px 0'>" +
        "     <div class='' style=' width: 100%;height: 30px;color: #DC9116'>" +
        "           <div style='width: 80px;float: left;text-align: right;margin-right: 20px;" + (isDir ? "" : "display:none;") + "'>提示：</div>" +
        "           <div style='float: left;width: 50%; " + (isDir ? "" : "display:none;") + "'>本次提交影响&nbsp;&nbsp;<span style=' '>" + (pushNum || 0) + "</span>&nbsp;&nbsp;个文件</div>" +
        "     </div>" +
        "     <div class='bdp-form-group dialog-group'>" +
        "          <label class='bdp-control-label account-label' style='width: 80px;margin-right: 20px;'><i class='bdp-note'>*</i> 提交信息：</label>" +
        "          <div class='' style='position: relative'>" +
        "               <textarea name='gitCommitMessage' class='bdp-form-control' id='gitCommitMessage' style='color:#A3A9B2;resize: none;width: 400px;height: 200px;padding: 10px' placeholder='请输入提交信息，该信息将同步至git中'></textarea>" +
        "           </div>" +
        "     </div>" +
        "</form>";
    $.bdpMsg({
        title: "Push 提交",
        mainContent: html,
        width: 600,
        buttons: [
            {
                text: "提交",
                event: function () {
                    var valid = $('#pushForm').valid();
                    if (valid) {
                        submitCallBack && submitCallBack();
                    }
                    // $.removeMsg();
                },
                btnClass: 'bdp-btn-primary'
            },
            {
                text: "取消",
                event: function () {
                    $.removeMsg();
                }
            }
        ]
    }, function () {
        $('#pushForm').validate({
            rules: {
                gitCommitMessage: {
                    required: true,
                    maxlength: 1000,

                }
            },
            messages: {
                gitCommitMessage: {
                    required: "必填字段！",
                    maxlength: $.validator.format("最多{0}个字符"),
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

    })
}

function mouseMoveChangeSize(event) {
    if (START_MOVE_SPLITHANDER) {
        var endPageX = event.pageX;
        var changed = (endPageX - START_MOVE_SPLITHANDER_PAGE_X) * 1;
        var currentLeftMenuWidth = (changed + leftMenuWidth);
        currentLeftMenuWidth = currentLeftMenuWidth < 170 ? 170 : currentLeftMenuWidth;
        event.currentLeftMenuWidth = currentLeftMenuWidth;
        $homdeIndexLeftMenu.css("width", (currentLeftMenuWidth) + "px");
        $homdeIndexRightContent.css("width", (windowWidth - (currentLeftMenuWidth)) + "px");
    }
    if (mouseMoveChangeEvents && mouseMoveChangeEvents.length > 0) {
        for (var index = 0; index < mouseMoveChangeEvents.length; index++) {
            var eventFun = mouseMoveChangeEvents[index];
            try {
                if (typeof eventFun == "function") {
                    eventFun(event);
                }
            } catch (e) {
                console.error(e)
            }
        }
    }
}

function addMouseMoveChangeEvent(event) {
    mouseMoveChangeEvents.push(event)
}

function saveTemplate(activeWindow, templateId, callBack) {
    if (activeWindow) {
        activeWindow && activeWindow.win && activeWindow.win.saveArgs()
        datadevInit.directSave(function (data) {
            var scriptType = activeWindow.jq("#scriptType").val();
            var pythonType = activeWindow.jq("#pythonType").val();
            if (scriptType == 3 && !pythonType) {
                var firstLine = activeWindow.editor.session.getLine(0) || "";
                pythonType = firstLine.toLocaleLowerCase().indexOf("python3") != -1 ? 2 : 1;
            }
            var scriptObj = getScriptObj(scriptType, pythonType);
            templateId = templateId || activeWindow.jq("#templateId").val();
            if (templateId && templateId > 0) {
                commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/getShareInfos.ajax", {
                    templateId: templateId
                }, $("#save"), function (node, data) {
                    var infos = data.obj;
                    var erps = infos.erps || "";
                    var gitShares = !!infos.gitShares;
                    var name = infos.name || "";
                    var desc = infos.desc || "";
                    showSaveTemplate(activeWindow, templateId, scriptObj, erps, gitShares, name, desc, callBack);
                })
            } else {
                var name = activeWindow.name || "";
                var index = name.lastIndexOf(".");
                if (index != -1) {
                    name = name.substring(0, index);
                }
                showSaveTemplate(activeWindow, templateId, scriptObj, "", false, name, activeWindow.desc || "", callBack);
            }
        }, null, activeWindow.key)
    } else if (templateId && templateId > 0) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/getShareInfos.ajax", {
            templateId: templateId
        }, $("#save"), function (node, data) {
            var infos = data.obj;
            var erps = infos.erps || "";
            var gitShares = !!infos.gitShares;
            var name = infos.name || "";
            var desc = infos.desc || "";
            var scriptType = infos.scriptType;
            var pythonType = infos.pythonType;
            var scriptObj = getScriptObj(scriptType, pythonType);
            showSaveTemplate(activeWindow, templateId, scriptObj, erps, gitShares, name, desc, callBack);
        })
    }
}

function showSaveTemplate(activeWindow, templateId, scriptObj, erps, gitShares, name, desc, callBack) {
    var typeName = scriptObj.typeName || scriptObj.name;
    var type = scriptObj.scriptType;
    var pythonType = scriptObj.pythonType;
    var html =
        "<form class='datadev-form-inline' id='templateSaveContent'>" +
        "    <h4 class='datadev-control-title'>基本信息</h4>" +
        "    <div class='datadev-form-group'>" +
        "        <label class='template-label datadev-control-label'><i class='bdp-note'>*</i>模板名称：</label>" +
        "        <div class='template-content template-name-content datadev-form-content'>" +
        "            <input class='datadev-form-control' value='" + name + "' name='name' placeholder='请输入模板名称，最长30个字符' autocomplete='off'/>" +
        "        </div>" +
        "    </div>" +
        "    <div class='datadev-form-group'>" +
        "        <label class='template-label datadev-control-label'><i class='bdp-note'>*</i>模板类型：</label>" +
        "        <div class='template-content template-type-content datadev-form-content'>" +
        "            <input class='datadev-form-control' value='" + typeName + "' placeholder='' autocomplete='off' disabled/>" +
        "        </div>" +
        "    </div>" +
        "    <div class='datadev-form-group'>" +
        "        <label class='template-label datadev-control-label'><i class='bdp-note'>*</i>模板描述：</label>" +
        "        <div class='template-content template-desc-content datadev-form-content'>" +
        "            <textarea class='datadev-form-control' name='desc' placeholder='请输入模板描述，最长255个字符' autocomplete='off'>" + desc + "</textarea>" +
        "        </div>" +
        "    </div>" +
        "    <h4 class='datadev-control-title'>分享设置</h4>" +
        "    <div class='datadev-form-group'>" +
        "        <label class='template-label datadev-control-label'>指定人（erp）：</label>" +
        "        <div class='template-content template-erp-content datadev-form-content'>" +
        "            <input id='templateShareErps' class='datadev-form-control bdp-form-control template-select' value='" + (erps || "") + "' name='shareErps' placeholder='' autocomplete='off'/>" +
        "        </div>" +
        "    </div>" +
        "    <div class='datadev-form-group'>" +
        "        <label class='template-label datadev-control-label'>分享到组或项目：</label>" +
        "        <div class='template-content template-git-content datadev-form-content'>" +
        // "            <input class='datadev-form-control bdp-form-control template-select' value='" + (gits || "") + "' id='templateShareGits' name='shareGits' placeholder='' autocomplete='off'/>" +
        // "            <input class='datadev-form-control bdp-form-control template-select' type='checkbox' id='templateShareGits' "+(gitShares?"checked='checked'":"")+" name='shareGits' placeholder='' autocomplete='off' style='min-height: 14px !important;position: relative;top: 5px;border: 1px solid #44494F;'/>" +
        "            <input class='template-select-radio' type='radio' id='templateShareGits' " + (gitShares ? "checked='checked'" : "") + " name='shareGits' placeholder='' autocomplete='off' style=''/><span class='template-select-title'>是</span>" +
        "            <input class='template-select-radio' type='radio' " + (gitShares ? "" : "checked='checked'") + " name='shareGits' placeholder='' autocomplete='off' style=''/><span class='template-select-title'>否</span>" +
        "        </div>" +
        "    </div>" +
        "</form>";
    $.bdpMsg({
        title: "保存模板",
        mainContent: html,
        width: "600px",
        buttons: [
            {
                text: "保存",
                id: "saveTemplateButton",
                event: function () {
                    var valid = $('#templateSaveContent').valid();
                    if (valid) {
                        var name = $("#templateSaveContent input[name='name']").val().trim();
                        var desc = $("#templateSaveContent textarea[name='desc']").val();
                        var shareErps = $("#templateSaveContent input[name='shareErps']").val();
                        // var shareGits = $("#templateSaveContent input[name='shareGits']").val();
                        var shareGits = $("#templateShareGits").is(":checked");
                        commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/saveTemplate.ajax", {
                            id: templateId,
                            name: name,
                            desc: desc,
                            shareErps: shareErps,
                            shareGits: shareGits,
                            gitProjectId: activeWindow && activeWindow.gitProjectId || "",
                            gitProjectFilePath: activeWindow && activeWindow.gitProjectFilePath || "",
                            scriptType: type || "",
                            pythonType: pythonType || "",
                        }, $("#saveTemplateButton"), function (node, data) {
                            callBack && callBack(data.obj);
                            if (!templateId || templateId < 0) {
                                var obj = data.obj;
                                openScript(obj.gitProjectId, obj.gitProjectFilePath, "模板-" + obj.name, null, true);
                            } else if (activeWindow && activeWindow.win) {
                                var params = getParam(activeWindow.gitProjectId, activeWindow.gitProjectFilePath, "模板-" + name, null, true);
                                HOME_COOKIE.changeName(activeWindow.key, activeWindow.key, params);
                                activeWindow.win.initInfo && activeWindow.win.initInfo();
                                $("#codeEditContainer").JdDataDevTab("changeTabInfos", activeWindow.key, activeWindow.key, params);
                            }
                            $.successMsg("保存成功");
                        })
                    }
                },
                btnClass: 'bdp-btn-primary'
            },
            {
                text: "取消",
                event: function () {
                    $.removeMsg();
                }
            }
        ]
    }, function () {
        datadev_user_common._int($("#templateShareErps"), true);
        // datadev_git_common._int($("#templateShareGits"), true);
        $('#templateSaveContent').validate({
            rules: {
                desc: {
                    maxlength: 255,
                    required: true
                },
                name: {
                    required: true,
                    maxlength: 30,
                },
            },
            messages: {
                name: {
                    required: "必填字段！",
                    maxlength: $.validator.format("最多{0}个字符"),
                },
                desc: {
                    required: "必填字段！",
                    maxlength: $.validator.format("最多{0}个字符"),
                }
            },
            errorElement: 'label',
            errorClass: 'bdp-help-block',
            focusInvalid: false,
            highlight: function (e) {
                $(e).closest('.datadev-form-group').find(".datadev-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
            },
            success: function (e) {
                $(e).closest('.datadev-form-group').find(".datadev-form-control").removeClass('bdp-wrong');
                $(e).remove();
            },
            errorPlacement: function (error, element) {
                error.attr("style", "margin-left:110px !important");
                error.appendTo(element.parents(".datadev-form-group"));

            }
        });
    })
}

//isCLose true说明要关闭并保存
function saveScript(isClose) {

    var node = $("#save");
    if ($(".ico-disabled", node).length > 0) {
        return;
    }
    var activeWindow = datadevInit.getActiveWindow();
    if (activeWindow && activeWindow.type && activeWindow.editor) {
        var type = activeWindow.type;
        var scriptObj = getScriptObj(type);
        var limitSize = scriptObj.limitSize || 0;
        var contentSize = limitSize > 0 ? sizeofStr(activeWindow.editor.getValue()) : 0;
        if (limitSize > 0 && contentSize > limitSize) {
            $.errorMsg("此类型文件最大只支持2M大小");
            return;
        }
    }

    var key = activeWindow.key;
    var isTemplate = activeWindow.win && activeWindow.win.isTemplate && activeWindow.win.isTemplate();
    if (isTemplate) {
        saveTemplate(activeWindow)
    } else {
        var saveStatus = saveStatusMap.get(key);
        if (saveStatus != 1) {
            saveStatusMap.put(key, 1);
            var closeallbackFun = getCloseCallBack((isClose ? key : undefined));
            var callbackFun = function (data) {
                saveStatusMap.put(key, 0);
                closeallbackFun(data);
            }
            var isShow = activeWindow.jq("#isShow").val();
            if (isShow == 1) {
                $("#saveModal").FileMode("save", {
                    key: activeWindow.key,
                    gitProjectId: activeWindow.gitProjectId,
                    gitProjectDirPath: activeWindow.gitProjectDirPath || "",
                    gitProjectFilePath: activeWindow.gitProjectFilePath || "",
                    type: activeWindow.type,
                    activeWindow: activeWindow
                }, callbackFun);
            } else {
                datadevInit.directSave(callbackFun, null, activeWindow.key);
            }

        }
    }

}

function restartScript(key, detailId) {
    var currentWindow = datadevInit.getActiveWindow(key);
    var jq = currentWindow.jq;
    jq("#restartCommand").addClass("ico-disabled");
    commonAjaxEvents.commonAllPostAjax(stopUrl, {
        runDetailId: detailId,
    }, $("#stop"), function (node, data) {
        jq("#restartCommand").removeClass("ico-disabled");
        if (data && data.code == 0) {
            runScript(currentWindow, false);
        }
    }, function () {
        jq("#restartCommand").removeClass("ico-disabled");
        resetAllTopButton(currentWindow);
        currentWindow.win.switchStartAndStop(1);
        runStatusMap.put(detailId, 2);
        currentWindow.win.initMode("unRun-mode");
    })
}

function getCloseCallBack(key) {
    return key ? function (data) {
        $("#codeEditContainer").JdDataDevTab("removeScript", key);
        data ? $.successMsg(data.message) : "";
        return true;
    } : function (data) {
        data ? $.successMsg(data.message) : "";
        return true;
    };
}

function templateModal(node, treeNode) {
    if (!hasProjectStatus) {
        askNewGit();
        return;
    }
    $(".rightMenu").hide();
    $(".scriptDiv").hide();
    var scriptType = node.closest(".newScriptTypeSign").attr("data-index");
    if (scriptType == 7) {
        var html =
            "<div class='txtScriptDiv' id='txtNewScriptDiv'>" +
            "       <div class='txtChooseContainer'>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='7'>" +
            "                   <span class='bdp-icon ide-txt'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>TXT</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='8'>" +
            "                   <span class='bdp-icon ide-xml'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>XML</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='18'>" +
            "                   <span class='bdp-icon ide-yml'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>YAML</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='20'>" +
            "                   <span class='bdp-icon ide-json'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>JSON</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='17'>" +
            "                   <span class='bdp-icon ide-ini'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>INI</span>" +
            "               </div>" +
            "           </div>" +
            "       </div>" +
            "       <div class='txtChooseContainer'>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='10'>" +
            "                   <span class='bdp-icon ide-csv'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>CSV</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='15'>" +
            "                   <span class='bdp-icon ide-prop'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>PROPERTIES</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='19'>" +
            "                   <span class='bdp-icon ide-cfg'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>CFG</span>" +
            "               </div>" +
            "           </div>" +
            "           <div class='txtChooseContent'>" +
            "               <div class='round-wrap' data-type='16'>" +
            "                   <span class='bdp-icon ide-conf'></span>" +
            "               </div>" +
            "               <div class='txtName'>" +
            "                   <span>CONF</span>" +
            "               </div>" +
            "           </div>" +
            "       </div>" +
            "</div>";
        //文本类型
        $.bdpMsg({
            title: "选择文本文件类型",
            mainContent: html,
            width: "666px",
            buttons: []
        }, function () {
            var selected = false;
            $("#txtNewScriptDiv").on("click", ".round-wrap", function () {
                if (!selected) {
                    selected = true;
                    var dataType = $(this).attr("data-type");
                    if (dataType) {
                        var script = getScriptObj(dataType);
                        addScript(script, treeNode, getSelectedProjectId(), script.default);
                    }
                    $.removeMsg();
                }
            })
        })
    } else {
        var pythonType = node.closest(".newScriptTypeSign").attr("data-python");

        var url = getTemplateHtml + "?scriptType=" + scriptType;
        if (pythonType) {
            url += "&pythonType=" + pythonType;
        }
        var templateArt = $.dialog.open(url, {
            title: "选择脚本模板",
            lock: true,
            width: "908px",
            height: "524px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("templateArt", templateArt);
        $.dialog.data("addScript", addScript);
        $.dialog.data("addScriptDirNode", getSelectedNode());

        $.dialog.data("selectedProjectId", getSelectedProjectId());
        $.dialog.data("openScript", openScript);
        $.dialog.data("removeScriptTabByKey", removeScriptTabByKey);
        $.dialog.data("getKey", getKey);
        $.dialog.data("saveTemplate", saveTemplate);
    }

}

function saveAsScript() {
    var node = $("#saveAs");
    if ($(".ico-disabled", node).length > 0) {
        return;
    }
    var activeWindow = datadevInit.getActiveWindow();
    var gitProjectId = activeWindow.gitProjectId;
    var gitProjectFilePath = activeWindow.jq("#gitProjectFilePath").val();
    var isShow = activeWindow.jq("#isShow").val();
    // if (isShow==1 ) {
    //     var info = {
    //         gitProjectId: gitProjectId,
    //         gitProjectDirPath: gitProjectDirPath || "",
    //         gitProjectFilePath:gitProjectFilePath,
    //         type: activeWindow.type,
    //         activeWindow: activeWindow
    //     };
    //     $("#saveModal").FileMode("saveAs", info);
    // } else {
    commonAjaxEvents.commonPostAjax(getScriptBaseUrl, {
        gitProjectFilePath: gitProjectFilePath,
        gitProjectId: gitProjectId
    }, $("#saveAs"), function (node, data) {
        if (data && data.obj) {
            var file = data.obj;
            file.activeWindow = activeWindow;
            if (isShow == 1) {
                file.name = "";
            }
            $("#saveModal").FileMode("saveAs", file);
        }
    })
}

function format() {
    var node = $("#format");
    if ($(".ico-disabled", node).length > 0) {
        return;
    }
    var activeWindow = datadevInit.getActiveWindow()
    activeWindow.editor.setReadOnly(true);
    disableAllTopButton(activeWindow.key);
    commonAjaxEvents.commonPostAjax("/scriptcenter/script/format.ajax", {
        content: activeWindow.editor.getValue(),
        type: activeWindow.jq("#scriptType").val() || "",
        id: activeWindow.jq("#scriptFileId").val() || "",
        name: activeWindow.jq("#fileName").val() || ""
    }, $("#format"), function (node, data) {
        if (data && data.obj) {
            activeWindow.editor.setValue(data.obj.content)
        }
    }, function () {
        activeWindow.editor.setReadOnly(false);
        resetAllTopButton(activeWindow);
    });
}

function stopScript(currentWindow) {

    if (currentWindow.win.getRunMode() == 1) {
        //debug
        currentWindow.win.debug.endDebug();
    } else {
        //run
        var runDetailId = currentWindow.jq("#scriptRunDetailId").val();
        commonAjaxEvents.commonAllPostAjax(stopUrl, {
            runDetailId: runDetailId,
        }, $("#stop"), function (node, data) {
            if (data && data.code == 0) {
                $.successMsg("停止成功", 1000);
            }
        }, null, function () {
            resetAllTopButton(currentWindow);
            currentWindow.win.switchStartAndStop(1);
            runStatusMap.put(runDetailId, 2);
            currentWindow.win.initMode("unRun-mode");
            currentWindow.win.resetVersionChange();
        })
    }
}

/**
 *
 * @param runType 0 单文件运行  1依赖运行
 * @param isDebug false空值：正常运行 true：debug
 * isMoreVersion 默认false是正常运行 true多版本运行
 */
function runScript(activeWindow, isDebug) {
    var runType = activeWindow.runType();
    var directRunStatus = activeWindow.isDirectRun();
    //查看是否是允许运行
    if (isDebug) {
        var node = $("#debug");
        if ($(".ico-disabled", node).length > 0) {
            return;
        }
    } else {
        var node = $("#run");
        if ($(".ico-disabled", node).length > 0) {
            return;
        }
    }
    activeWindow.win.setRunMode(isDebug || false);
    var configId = $("#queueName").attr("data-index");
    //sql脚本必须选择账号队列
    var type = activeWindow.type;
    if (type == 1 && (!configId || configId == -1)) {
        //sql脚本必须选择账号队列配置
        showAccountError();
        return;
    } else {
        $("#queueCode").removeClass("runError");
    }


    var str = "";
    var version = activeWindow.jq("#version").val();
    if (activeWindow.editor) {
        str = activeWindow.editor.getValue();
    }
    var gitProjectId = activeWindow.gitProjectId;
    var gitProjectFilePath = activeWindow.gitProjectFilePath;
    var key = getKey(gitProjectId, gitProjectFilePath);
    if (!activeWindow.win.validateArgs()) {
        return;
    }
    //验证目录方式运行都已经保存
    if (runType == 1) {
        var path = verifyNoSave(gitProjectId, gitProjectFilePath);
        if (path) {
            $.errorMsg("请先保存文件：" + path);
            return;
        }
    }
    //zip类型验证启动项
    if (type == 4) {
        if (!activeWindow.win.valiateStarShellPath()) {
            var startShellHtml = "<div class='bdp-form-group dialog-group'  style='height: 32px;line-height: 32px;margin-top: 20px'>" +
                "                        <label class='bdp-control-label ' style='width: 80px'><i class='bdp-note'>*</i>启动项：</label>" +
                "                        <div class=' ' style='position: relative'>" +
                "                            <input id='addStartShell' class='bdp-form-control' style='width: 300px;margin-left: 10px'   name='startShell'" +
                "                                   placeholder='请输入启动项' autocomplete='off'>" +
                "                        </div>" +
                "                    </div>";
            var descriptionHtml = "<div class='bdp-form-group dialog-group'  style=''>" +
                "                        <label class='bdp-control-label ' style='width: 80px'>脚本描述：</label>" +
                "                        <div class='' style='position: relative'>" +
                "                            <textarea id='addDescription' placeholder='请输入描述' class='bdp-form-control ' style='width: 300px;margin-left: 10px;height: 100px'  name='description'></textarea>" +
                "                        </div>" +
                "                    </div>";
            $.bdpMsg({
                title: "请设置启动项",
                width: '450px',
                mainContent: "<form id='startShellAddForm'>" + startShellHtml + descriptionHtml + "</form>",
                buttons: [
                    {
                        text: "确定",
                        event: function () {
                            var valid = $("#startShellAddForm").valid();
                            if (valid) {
                                commonAjaxEvents.commonPostAjax("/scriptcenter/scriptFile/saveArgs.ajax", {
                                    gitProjectId: gitProjectId,
                                    gitProjectFilePath: gitProjectFilePath,
                                    startShellPath: $("#addStartShell").val(),
                                    description: $("#addDescription").val()
                                }, null, function (node, data) {
                                    activeWindow && activeWindow.win && activeWindow.win.initInfo(gitProjectId, gitProjectFilePath);
                                    datadevInit.run(runType, activeWindow, activeWindow.version, JSON.stringify(activeWindow.win.getRunArgs()), configId, $("#addStartShell").val(), false);
                                    parent.$.removeMsg();
                                })
                            }
                        },
                        btnClass: 'bdp-btn-primary'
                    },
                    {
                        text: "取消",
                        event: function () {
                            parent.$.removeMsg();
                        },
                        btnClass: 'bdp-btn-default'
                    }
                ]
            }, function () {
                $('#startShellAddForm').validate({
                    rules: {
                        startShell: {
                            required: true,
                            maxlength: 1000
                        }
                    },
                    messages: {
                        startShell: {
                            required: "必填字段！",
                            maxlength: $.validator.format("最多{0}个字符"),
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
                        // error.appendTo(element.parents(".bdp-form-group"));
                        // error.css({"margin-left":"90px !important"});
                        error.attr("style", "margin-left:90px !important");
                        error.appendTo(element.parent());
                    }
                })
            });
            return;
        } else {
            datadevInit.run(runType, activeWindow, activeWindow.version, JSON.stringify(activeWindow.win.getRunArgs()), configId, activeWindow.win.getStartShellPath(), false);
        }
    } else {
        var ajaxData = {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            content: str,
            version: version
        };
        var content = activeWindow.getDirectRunValue();
        //多版本运行或者调试的时候不需要保存
        if (directRunStatus) {
            if (isDebug) {
                datadevInit.debug(runType, activeWindow, activeWindow.version, JSON.stringify(activeWindow.win.getRunArgs()), configId, activeWindow.win.getStartShellPath(), true, content)
            } else {
                datadevInit.run(runType, activeWindow, activeWindow.version, JSON.stringify(activeWindow.win.getRunArgs()), configId, activeWindow.win.getStartShellPath(), true, content);
            }
        } else {
            datadevInit.directSave(function (data) {
                if (isDebug) {
                    datadevInit.debug(runType, activeWindow, data.obj.serverVersion, JSON.stringify(activeWindow.win.getRunArgs()), configId, activeWindow.win.getStartShellPath(), false);
                } else {
                    datadevInit.run(runType, activeWindow, data.obj.serverVersion, JSON.stringify(activeWindow.win.getRunArgs()), configId, activeWindow.win.getStartShellPath(), false);
                }
            }, ajaxData, key, true);
        }
    }
}

/**
 *
 * @param gitProjectId
 * @param gitProjectFilePath
 */
function getPushStatus(gitProjectId, gitProjectFilePath) {
    var gitStatus = "";
    if (gitProjectId == getSelectedProjectId()) {
        var treeNode = zTree.getNodeByParam("path", gitProjectFilePath);
        if (treeNode) {
            gitStatus = treeNode.gitStatus;
        }
    }
    if (gitStatus == "") {
        var key = getKey(gitProjectId, gitProjectFilePath);
        var scriptWindow = datadevInit.getActiveWindow(key);
        if (scriptWindow && scriptWindow.jq && scriptWindow.jq("#gitStatus").val()) {
            gitStatus = scriptWindow.jq("#gitStatus").val();
        }
    }
    if (gitStatus == "") {
        gitStatus = SCRIPT_GIT_STSTUS.NON;
    }
    return gitStatus;
}

var datadevInit = {
    /*页面改变大小控制*/
    initChangeSize: function () {
        $(window).resize(function (event) {
            initWindow();
        })
        $homdeIndexLeftMenu = $("#homdeIndexLeftMenu");
        $homdeIndexRightContent = $("#homdeIndexRightContent");
        initWindow();
        $(document).mousemove(function (event) {
            mouseMoveChangeSize(event);
        });
        $(document).on("mousedown", "#homeIndexSplitHandler", function (event) {
            START_MOVE_SPLITHANDER = true;
            START_MOVE_SPLITHANDER_PAGE_X = event.pageX;
            leftMenuWidth = $homdeIndexLeftMenu.width() * 1;
            event.stopPropagation();
            event.preventDefault();
        })
        $(document).mouseup(function (event) {
            if (event.target.nodeName == "INPUT" || event.target.nodeName == "TEXTAREA") {
                return true;
            }
            START_MOVE_SPLITHANDER = false;
            // event.stopPropagation();
            // event.preventDefault();
        })
    },
    isActive: function (key) {//key表示的脚本是否是激活脚本
        var activeWindow = $("#codeEditContainer").JdDataDevTab("getCurrentActiveTab");
        var activeKey = activeWindow && activeWindow.activeLi.attr("data-id");
        return activeKey == key;
    },
    getActiveWindow: function (key) {
        var activeWindow = undefined;
        if (key) {
            activeWindow = $("#codeEditContainer").JdDataDevTab("getActiveTabByDataId", key);
        } else {
            activeWindow = $("#codeEditContainer").JdDataDevTab("getCurrentActiveTab");
        }
        if (activeWindow && activeWindow.activeDiv && activeWindow.initStatus) {
            activeWindow.key = activeWindow.activeLi.attr("data-id");
            var iframe = $("iframe", activeWindow.activeDiv);
            if (iframe.length > 0) {
                activeWindow.status = true;
                activeWindow.win = $("iframe", activeWindow.activeDiv)[0].contentWindow;
                activeWindow.jq = activeWindow.win.jQuery;
                activeWindow.target = activeWindow.activeLi.attr("target");
                activeWindow.name = $("span.labelName", activeWindow.activeLi).text();
                activeWindow.desc = activeWindow.jq && activeWindow.jq("#script-detail-description").val();
                activeWindow.code = activeWindow.jq && activeWindow.jq("#code");
                activeWindow.editor = activeWindow.code && activeWindow.code.data("editor");
                activeWindow.gitProjectId = activeWindow.jq && activeWindow.jq("#gitProjectId").val();
                activeWindow.gitProjectFilePath = activeWindow.jq && activeWindow.jq("#gitProjectFilePath").val();
                activeWindow.gitProjectDirPath = activeWindow.jq && activeWindow.jq("#gitProjectDirPath").val();
                activeWindow.version = activeWindow.jq && activeWindow.jq("#version").val();
                activeWindow.type = activeWindow.jq && activeWindow.jq("#scriptType").val();
                activeWindow.isShow = activeWindow.jq && activeWindow.jq("#isShow").val() * 1;
                activeWindow.md5 = activeWindow.jq && activeWindow.jq("#fileMd5").val();
                activeWindow.getDirectRunValue = function () {
                    return activeWindow.win && activeWindow.win.getDirectRunValue();
                };
                activeWindow.verifyModify = activeWindow.win && activeWindow.win.verifyModify;
                activeWindow.isDirectRunVersion = activeWindow.jq && activeWindow.jq("#hasChangVersion").val() * 1 == 1 ? 1 : 0;
                activeWindow.runType = function () {
                    return activeWindow.jq("#runType").val()
                };
                activeWindow.isDirectRun = function () {
                    return activeWindow.win.isDirectRun();
                }
            }
        } else {
            activeWindow ? activeWindow.status = false : "";
        }
        return activeWindow;
    },
    getAllWindow: function () {
        var allTabs = $("#codeEditContainer").JdDataDevTab("getAllTab");
        var windows = [];
        for (var i = 0; allTabs != null, i < allTabs.length; i++) {
            var tab = allTabs[i];
            var key = tab.attr("data-id");
            if (key) {
                var window = datadevInit.getActiveWindow(key);
                windows.push(window);
            }
        }
        return windows;
    },
    /**
     *
     * @param callBack 成功保存的回调函数
     * @param ajaxData 保存提交的数据
     */
    directSave: function (callBack, ajaxData, oriKey, noUpdate) {
        var window = datadevInit.getActiveWindow(oriKey);
        var content = window && window.editor && window.editor.getValue() || "";
        var gitProjectId = window && window.gitProjectId || ajaxData.gitProjectId;
        var gitProjectFilePath = window && window.gitProjectFilePath || ajaxData.gitProjectFilePath;
        var postData = ajaxData || {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            content: content,
            version: window.version,
        };
        disableAllTopButton(oriKey);
        commonAjaxEvents.commonAllPostAjax(saveContentUrl, JSON.stringify(postData), $("#save"), function (node, data) {
            var key = getKey(gitProjectId, gitProjectFilePath);
            resetAllTopButton(window);
            if (data.obj.isDirectCover == true) {
                refreshFileInfoStatus(gitProjectId, gitProjectFilePath, data.obj.fileMd5, data.obj.serverVersion, data.obj.gitStatus);
                if (ajaxData && !noUpdate) {
                    window && window.editor && window.editor.setValue(ajaxData.content);
                    window && window.editor && window.editor.clearSelection();
                }
                window && window.win && window.win.verifyModify();//重新计算md5
                window && window.win && window.win.removeVersionChange();//去掉多版本状态
                window && window.win && window.win.changeLastMender(data);
                window && window.win && window.win.saveBreakPoints();
                //更新依赖显示
                var windows = datadevInit.getAllWindow();
                if (windows && windows.length > 0) {
                    for (var i = 0; i < windows.length; i++) {
                        windows[i].win && windows[i].win.updatePackDetail && windows[i].win.updatePackDetail(gitProjectId, gitProjectFilePath);
                    }
                }
                callBack && callBack(data);
                return;
            } else {
                //暂存内容
                scriptMergeContentMap.put(key, {
                    local: postData.content,
                    remote: data.obj.serverContent,
                    localVersion: data.obj.serverVersion
                });
                $.dialog.data("leftTips", "数据开发平台       当前提交人：" + data.obj.nowErp);
                $.dialog.data("rightTips", "数据开发平台       上次提交人：" + data.obj.lastErp + "     上次提交时间：" + data.obj.lastModified);
                diff(gitProjectId, gitProjectFilePath, function (key, data) {
                    datadevInit.directSave(callBack, data, key);
                }, key);
                return;
            }
        }, function () {
            resetAllTopButton(window);
            saveStatusMap.put(oriKey, 0);
        }, null, true)
    },
    restoreSideBar: function (acticeWindow, mode) {
        mode = mode || "unRun-mode";
        acticeWindow.win && acticeWindow.win.initMode(mode);
    },
    /**
     * 关闭SQL TOOl 自动审核
     * @param sqlToolId
     * @param params
     */
    runSqlCloseSqlTool: function (sqlToolId, params) {
        var sqlCloseSqlToolContent = "<div style='margin-top: 10px;'>您正在关闭自动审核SQL规范功能！</div>" +
            "<div style='margin-top: 10px;'>1、产品不再自动审核<span style='color: red;'>当天</span>提交执行的SQL。</div>" +
            "<div style='margin-top: 10px;'>2、后续产品将自动审核所有SQL是否符合规范。</div>" +
            "<div style='margin-top: 10px;'><a href='http://sqltools.jd.com/' target='_blank'>点击获取更多SQL规范审核信息。</div>";
        ;
        $.bdpMsg({
            title: "关闭审核SQL提示",
            mainContent: sqlCloseSqlToolContent,
            buttons: [
                {
                    text: "查看建议",
                    event: function () {
                        $.removeMsg();
                        window.open("http://sqltools.jd.com/bdpcheck?sqlInfoId=" + sqlToolId)
                    },
                    btnClass: 'bdp-btn-primary'

                },
                {
                    text: "关闭审核",
                    event: function () {
                        $.cookie("doSqlToolCheck", "false", {expires: 1});
                        datadevInit.runWithParams(params);
                        $.removeMsg();
                    }
                }
            ]
        })

    },
    runSqlSqlTool: function (data, params) {

        var validateObj = data.obj;
        var problems = validateObj.problems;
        var sqlValidateContent = "<div  style='margin-top: 10px;word-break: break-all'>您提交的SQL，存在以下规范性问题：</div>";
        for (var index = 0; index < problems.length; index++) {
            sqlValidateContent += "<div style='margin-top: 10px;'>" + (index + 1) + "、" + problems[index].description + "</div>"
        }


        sqlValidateContent += "<div style='color: red;margin-top: 10px;'>注：规范的SQL将保证代码质量，确保执行效率，请您根据建议优化您的SQL。</div>"

        $.bdpMsg({
            title: "优化SQL提示",
            mainContent: sqlValidateContent,
            buttons: [
                {
                    text: "查看建议",
                    event: function () {
                        $.removeMsg();
                        window.open("http://sqltools.jd.com/bdpcheck?sqlInfoId=" + validateObj["sqlInfoId"])
                    },
                    btnClass: 'bdp-btn-primary'

                },
                {
                    text: "直接提交",
                    event: function () {
                        params.doSqlToolCheck = "false";
                        datadevInit.runWithParams(params)
                        parent.$.removeMsg();
                    }
                },
                {
                    text: "关闭审核",
                    event: function () {
                        params.doSqlToolCheck = "false"
                        datadevInit.runSqlCloseSqlTool(validateObj["sqlInfoId"], params);
                    },

                }
            ]
        })

    },

    /**
     * 通过 obj 方式运行 fanparams
     * @param params
     */
    runWithParams: function (params) {
        datadevInit.run(params["runType"],
            params["activeWindow"],
            params["version"],
            params["args"],
            params["scriptConfigId"],
            params["startShellPath"],
            params["runByContent"],
            params["content"],
            params["doSqlToolCheck"]
        );

    },
    run: function (runType, activeWindow, version, args, scriptConfigId, startShellPath, runByContent, content, doSqlToolCheck) {
        doSqlToolCheck = doSqlToolCheck == undefined ? $.cookie("doSqlToolCheck") : doSqlToolCheck;
        var gitProjectId = activeWindow.gitProjectId;
        var gitProjectFilePath = activeWindow.gitProjectFilePath;
        var key = getKey(gitProjectId, gitProjectFilePath);
        var ajaxData = {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            version: version,
            args: args,
            scriptConfigId: scriptConfigId,
            startShellPath: startShellPath,
            runType: runType,
            detailToken: $("#detailToken").val(),
            runByContent: runByContent || false,
            content: content || "",
            doSqlToolCheck: doSqlToolCheck,

        }
        activeWindow.win.homeOpenPageEvent.waitRun(activeWindow);
        commonAjaxEvents.commonAllPostAjax("/scriptcenter/script/run.ajax", ajaxData, null, function (node, data) {
            if (data.code + "" === "0") {

                if (data.obj.responseCode * 1 == 102) {
                    //sql 验证失败
                    var params = {
                        runType: runType,
                        activeWindow: activeWindow,
                        version: version,
                        args: args,
                        scriptConfigId: scriptConfigId,
                        startShellPath: startShellPath,
                        runByContent: runByContent,
                        content: content
                    }
                    datadevInit.runSqlSqlTool(data, params);
                    resetAllTopButton(activeWindow);
                    activeWindow.win.switchStartAndStop(1)
                    return;
                }
                if (data.obj.responseCode == 101) {
                    resetAllTopButton(activeWindow);
                    activeWindow.win.switchStartAndStop(1);
                    $.bdpMsg({
                        title: "依赖关系验证提示",
                        mainContent: "<div style='margin-top: 20px'><p>当前依赖关系存在异常，无法正常运行，请重新设置依赖关系。</p></div>",
                        width: "350px",
                        buttons: [
                            {
                                text: "修改依赖关系",
                                event: function () {
                                    choDep(gitProjectId, gitProjectFilePath);
                                    $.removeMsg();
                                },
                                btnClass: 'bdp-btn-primary'
                            },
                            {
                                text: "取消",
                                event: function () {
                                    $.removeMsg();
                                }
                            }
                        ]
                    })
                } else {
                    activeWindow.jq("#scriptRunDetailId").val(data.obj.id);
                    runStatusMap.put(data.obj.id, "0");
                    activeWindow.win.homeOpenPageEvent.notifyLog(key, data.obj.id, activeWindow);
                }
            } else {
                resetAllTopButton(activeWindow);
                activeWindow.win.switchStartAndStop(1);
                $.errorMsg(data.message || data._msg);
            }
        }, function () {
            resetAllTopButton(activeWindow);
            activeWindow.win.switchStartAndStop(1);
        })
    },
    debug: function (runType, activeWindow, version, args, scriptConfigId, startShellPath, runByContent, content) {
        var gitProjectId = activeWindow.gitProjectId;
        var gitProjectFilePath = activeWindow.gitProjectFilePath;
        var key = getKey(gitProjectId, gitProjectFilePath);
        var firstLine = activeWindow.editor.session.getLine(0) || "";
        var pythonVersion = firstLine.toLocaleLowerCase().indexOf("python3") != -1 ? 1 : 0;
        var ajaxData = {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            version: version,
            args: args,
            scriptConfigId: scriptConfigId,
            startShellPath: startShellPath,
            runType: runType,
            debugFlag: 1,
            detailToken: $("#detailToken").val(),
            runByContent: runByContent || false,
            content: content || ""
        }
        var backFun = function () {
            resetAllTopButton(activeWindow);
            activeWindow.win.switchStartAndStop(1);
            activeWindow.win.resetVersionChange();
        }
        activeWindow.win.homeOpenPageEvent.waitRun(activeWindow);
        commonAjaxEvents.commonAllPostAjax("/scriptcenter/script/run.ajax", ajaxData, null, function (node, data) {
            if (data.code + "" === "0") {
                try {
                    parent.parentInitTopButton && parent.parentInitTopButton(key, parent.TOP_BUTTON.stop, true);
                    activeWindow.win.initMode("debug-mode");
                    var detailId = data.obj.id;
                    $.loadingMsg("调试环境正在创建中");
                    var dependencys = data.obj.devDependencyDetails;
                    if (dependencys && dependencys.length > 0) {
                        var oldBackFun = backFun;
                        backFun = function () {
                            oldBackFun();
                            debugDepndendedMap.remove(key);
                            for (var index = 0; index < dependencys.length; index++) {
                                var dependentKey = getKey(gitProjectId, dependencys[index]);
                                var array = debugDependentMap.get(dependentKey);
                                if (array && array.indexOf(gitProjectFilePath) != -1) {
                                    array.splice(array.indexOf(gitProjectFilePath), 1);
                                }
                            }
                        }
                        debugDepndendedMap.put(key, dependencys);
                        for (var index = 0; index < dependencys.length; index++) {
                            var dependentKey = getKey(gitProjectId, dependencys[index]);
                            if (!debugDependentMap.get(dependentKey)) {
                                debugDependentMap.put(dependentKey, new Array());
                            }
                            var array = debugDependentMap.get(dependentKey);
                            if (array.indexOf(gitProjectFilePath) == -1) {
                                array.push(gitProjectFilePath);
                            }
                        }
                    }
                    activeWindow.win.debug.startDebug(backFun, backFun, detailId, pythonVersion)
                } catch (e) {
                    backFun();
                }
            } else {
                backFun();
                $.errorMsg(data.message || data._msg);
            }
        }, function () {
            backFun();
        })
    },
    uplineSaveScript: function (scriptWindow) {
        if (scriptWindow.type == 4) {
            datadevInit.uplinePushNumScript(scriptWindow);
        } else {
            datadevInit.directSave(function (data) {
                scriptWindow.version = data.obj.serverVersion;
                var canPush = datadevInit.uplineCheckContent(scriptWindow);
                if (canPush) {
                    datadevInit.uplinePushNumScript(scriptWindow);
                }
            }, null, scriptWindow.key);
        }
    },
    //上线的时候对于SQL文件的内容检查
    uplineCheckContent: function (scriptWindow) {
        if (scriptWindow.type * 1 == 1) {
            //sql 需要检查SQL语句是否合法
            return datadevInit.uplineCheckSqlContent(scriptWindow);
        }
        return true;
    },
    uplineCheckSqlContent: function (scriptWindow) {
        var sql = scriptWindow.editor.getValue();
        var dataDevSql = new DataDevSql(sql);
        var checkResult = dataDevSql.checkCanUpLine();
        if (!checkResult.isValidate) {
            var content = "当前SQL脚本存在异常情况，请参照下文逐项核实并修改：";
            content += "<p>1、存在语法问题，请在开发平台IDE运行验证；</p>";
            content += "<p>2、表前未写库名，应按照【库名.表名】，如app.app_w08_log_bdpstatistics_uv；</p>";
            content += "<p>3、SQL中必须具有ALTER、CREATE、DROP、INSERT、LOAD、TRUNCATE修改性操作。</p>"

            $.bdpMsg(
                {
                    title: "提示",
                    width: "400px",
                    mainContent: "<p style='margin-top: 30px;margin-bottom: 30px;'>" + content + "</p>",
                    buttons: [
                        {
                            text: "关闭",
                            event: function () {
                                $.removeMsg();
                            },
                            btnClass: 'bdp-btn-primary'
                        }
                    ]
                }
            )
        }
        return checkResult.isValidate;
    },
    uplinePushNumScript: function (scriptWindow) {
        var gitProjectId = scriptWindow.gitProjectId;
        var gitProjectFilePath = scriptWindow.gitProjectFilePath;
        var name = scriptWindow.name;
        var version = scriptWindow.version;
        var scriptType = scriptWindow.type;
        if (gitProjectFilePath.startsWith("BDP.TARGET/")) {
            $("#upLineModal").upLine(gitProjectId, gitProjectFilePath, name, version, scriptType);
        } else {
            $.loadingMsg("正在Push...");
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getPushNum.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath
            }, null, function (node, data) {
                var gitMerge = data.obj.gitMerge;
                var pushNum = data.obj.pushNum;
                var mergeList = data.obj.mergeList;
                if (gitMerge == true) {
                    var key = getKey(gitProjectId, gitProjectFilePath);
                    var script = mergeList[0];
                    scriptMergeContentMap.put(key, {
                        localVersion: script.version,
                        remoteVersion: script.newGitVersion
                    });
                    $.dialog.data("leftTips", "数据开发平台  当前操作人：" + data.obj.erp);
                    $.dialog.data("rightTips", "git   commitId：" + script.newGitVersion);
                    diff(gitProjectId, gitProjectFilePath, function (key, ajaxData) {
                        datadevInit.directSave(function (data) {
                            if (data.obj) {
                                datadevInit.uplineShowInfo(gitProjectId, gitProjectFilePath, name, version, 1, scriptType);
                            }
                        }, ajaxData, getKey(gitProjectId, gitProjectFilePath))
                    });
                } else {
                    datadevInit.uplineShowInfo(gitProjectId, gitProjectFilePath, name, version, pushNum, scriptType);
                }

            })

        }
    },
    uplineShowInfo: function (gitProjectId, gitProjectFilePath, name, version, pushNum, scriptType) {
        if (pushNum == 0) {
            $("#upLineModal").upLine(gitProjectId, gitProjectFilePath, name, version, scriptType);
        } else {
            showPushInfo(pushNum, function () {
                var gitCommitMessageNode = $("#gitCommitMessage");
                var gitCommitMessage = gitCommitMessageNode.length > 0 && gitCommitMessageNode.val() || "";
                $.loadingMsg("正在Push...");
                datadevInit.mergeAndPushScript(gitProjectId, gitProjectFilePath, gitCommitMessage, function (data) {
                    $("#upLineModal").upLine(data.gitProjectId, data.gitProjectFilePath, data.name, data.version, scriptType);
                });
            }, false);
        }
    },
    mergeAndPushScript: function (gitProjectId, gitProjectFilePath, commitMessage, callBack) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/pushFile.ajax", {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            commitMessage: commitMessage,
        }, null, function (node, data) {
            if (data.obj.isMerge == true) {
                var key = getKey(gitProjectId, gitProjectFilePath);
                scriptMergeContentMap.put(key, {localVersion: data.obj.version, remoteVersion: data.obj.mergeVersion});
                $.dialog.data("leftTips", "数据开发平台  当前操作人：" + data.obj.erp);
                $.dialog.data("rightTips", "git   commitId：" + data.obj.mergeVersion);
                diff(gitProjectId, gitProjectFilePath, function (key, ajaxData) {
                    datadevInit.directSave(function (data) {
                        if (data.obj) {
                            datadevInit.mergeAndPushScript(gitProjectId, gitProjectFilePath, commitMessage);
                        }
                    }, ajaxData, getKey(gitProjectId, gitProjectFilePath))
                });
            } else {
                changeFileGitStatus(gitProjectId, gitProjectFilePath, SCRIPT_GIT_STSTUS.NON);
                callBack && callBack(data.obj);
            }
        }, undefined, undefined, undefined, undefined, function () {
            $.loadingMsg("正在Push...");
        })
    },
    /*页面按钮事件*/
    initButtonEvent: function () {
        $(document).contextmenu(function (event) {
            // console.log(event);
            // console.log(arguments);
            if (!$(event.target).parents(".bdp-product").length > 0) {
                return false;
            }
        })

        $("#changeColor").click(function () {
            HOME_COOKIE.changeColorCookie();
            window.location.reload();

        })
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
                    showSearchScript();
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
        })
        $(document).click(function (event) {
            hiddenAllRightMeun();
        })
        $("#leftContentDiv").JdDataDevTab({
            afterClickCallBack: function (event, obj, args, li, tabContent) {
                var target = li.attr("target");
                if (target == "table-query") {
                    initTableSize && initTableSize();
                }
            }
        });
        $("#leftContentDiv").JdDataDevTab("active", 0);
        var _this = this;
        $("#codeEditContainer").JdDataDevTab({
            hasFullScreen: true,
            fullScreenClick: function (node) {
                var span = $("span", node);
                if (span.hasClass("ide-icon-zk")) {
                    // 全屏
                    var width = $("#homdeIndexLeftMenu").css("width");
                    node.data("originWidth", width);
                    $("#homdeIndexLeftMenu").css("width", "0px");
                    $("#homeIndexSplitHandler").css("display", "none");
                    span.removeClass("ide-icon-zk").addClass("ide-icon-sq");
                    return;
                }
                if (span.hasClass("ide-icon-sq")) {
                    //缩小
                    var originWidth = node.data("originWidth") ? node.data("originWidth") : "20%";
                    $("#homdeIndexLeftMenu").css("width", originWidth);
                    $("#homeIndexSplitHandler").css("display", "block");
                    span.removeClass("ide-icon-sq").addClass("ide-icon-zk");
                    return;
                }
            },
            afterClickCallBack: function (event) {
                var currentWindow = _this.getActiveWindow();
                //设置active的li
                if (currentWindow && currentWindow.key) {

                    datadevInit.changeTopButtons(currentWindow.key);
                    if (currentWindow.jq && currentWindow.jq("#configId").val()) {
                        changeConfig(currentWindow.jq("#configId").val());
                    }
                } else {
                    //说明选中的wellcome页面
                    datadevInit.defaultButtons();
                }

                var currentGitProjectId = currentWindow.gitProjectId;
                var currentGitProjectFilePath = currentWindow.gitProjectFilePath;
                var key = currentWindow.key;
                if (!currentGitProjectFilePath && !currentGitProjectId && key) {
                    var splitIndex = key.indexOf("/");
                    if (splitIndex != -1) {
                        currentGitProjectId = key.substring(0, splitIndex);
                        currentGitProjectFilePath = key.substring(splitIndex + 1);
                    }
                }
                if (zTree) {
                    locationScript(currentGitProjectId, currentGitProjectFilePath)
                } else if (currentGitProjectId && currentGitProjectFilePath) {
                    lazyLocation.push({
                        gitProjectId: currentGitProjectId,
                        gitProjectFilePath: currentGitProjectFilePath
                    });
                }
                HOME_COOKIE.setActiveLiCookie(currentWindow.activeLi.attr("data-id"));
                currentWindow && currentWindow.win && currentWindow.win.clickActiveDataLi && currentWindow.win.clickActiveDataLi();
                return true;
            },
            afterRemoveTabCallBackEvent: function (key) {
                $('.showTitleBox').remove();
                if (key) {
                    HOME_COOKIE.removeTabCookie(key)
                }
            },
            beforeClickClose: function (node) {
                var key = node.attr("data-id");
                try {
                    //正在运行脚本不能关闭
                    var scriptMapParams = scriptMap.get(key);

                    if (scriptMapParams && scriptMapParams.stop) {


                        $.bdpMsg({
                            title: "提示",
                            mainContent: " <div style='font-size: 14px;margin-top: 30px;word-break: break-all'>有正在运行的脚本，不能关闭当前标签</div><div style='font-size: 12px;margin-top: 30px;'>请确认是否需要关闭当前Tab页并终止执行？" + "</div>",
                            buttons: [
                                {
                                    text: "强制关闭",
                                    event: function () {

                                        $("#stop").click();

                                        setTimeout(function () {$("#codeEditContainer").JdDataDevTab("removeScript", key);}, 2000)

                                    },
                                    btnClass: 'bdp-btn-primary'
                                },
                                {
                                    text: "取消",
                                    event: function () {
                                        $.removeMsg();
                                    }
                                }
                            ]
                        })


                        return;
                    }
                    var callback = getCloseCallBack(key);
                    var activeWindow = datadevInit.getActiveWindow(key);
                    if (!activeWindow || !activeWindow.jq || activeWindow.jq("#openScript").length == 0) {
                        callback && callback();
                        return;
                    }
                    var type = activeWindow.jq("#scriptType").val();
                    if (type == 4 || type == 5) {
                        callback && callback();
                        return;
                    }
                    //计算两次的MD5的值，如果没改变那么表示没有编辑，直接关闭
                    var oldMd5 = activeWindow.md5;
                    var newMd5 = $.md5(activeWindow && activeWindow.editor && activeWindow.editor.getValue() || "");

                    if ($.trim(oldMd5).length < 1 && activeWindow.isShow != 1) {
                        saveScript(key);
                        return;
                    }
                    if (activeWindow.win.initContentStatus && oldMd5 != newMd5) {
                        var append = "";
                        if (activeWindow.name) {
                            append += "[" + activeWindow.name + "]";
                        }
                        $.bdpMsg({
                            title: "提示",
                            mainContent: " <div style='font-size: 14px;margin-top: 30px;color: #E4ECFA;opacity: 0.8;word-break: break-all'>是否将更改保存到" + append + "？</div><div style='font-size: 12px;margin-top: 30px;color: #E4ECFA;opacity: 0.8;'>文件尚未保存修改内容，如果不保存，您的修改将会丢失！</div>",
                            buttons: [
                                {
                                    text: "保存",
                                    event: function () {
                                        node.click();
                                        $.removeMsg();
                                        saveScript(key);
                                    },
                                    btnClass: 'bdp-btn-primary'
                                },
                                {
                                    text: "不保存",
                                    event: function () {
                                        $("#codeEditContainer").JdDataDevTab("removeScript", key);
                                        $.removeMsg();
                                    }
                                },
                                {
                                    text: "取消",
                                    event: function () {
                                        $.removeMsg();
                                    }
                                }
                            ]
                        })
                        return;
                    }

                    //最后关闭
                    $("#codeEditContainer").JdDataDevTab("removeScript", key);
                } catch (e) {
                    //最后关闭
                    $("#codeEditContainer").JdDataDevTab("removeScript", key);
                }

            }
        });
        $("#codeEditContainer").JdDataDevTab("active", 0);

        //脚本管理子页面也会调用这个方法
        $("#addScriptTab").click(function (event) {
            $(".bdp-new-help-modal-backdrop").click();
            if (!hasProjectStatus) {
                askNewGit();
                return false;
            }
            var node = $(this);
            if ($(".ico-disabled", node).length > 0) {
                return;
            }
            var offset = $(this).offset();
            var top = offset.top;
            var left = offset.left;
            top += $(this).height();
            left += $(this).width();
            hiddenAllRightMeun();
            $("#newScriptDiv").css({top: top + 10, left: left - 65}).show();
            event.stopPropagation();
            event.preventDefault();
        })


        $("#newScriptDiv").mouseleave(function () {
            $(this).hide();
        })
        //之前的新建脚本
        // $("#newScriptDiv").on("click", "li.scriptLi", function () {
        //
        //     var dataIndex = $(this).attr("data-index");
        //     var script = scriptTypeArr[dataIndex];
        //     var applicationId = getSelectedProjectId();
        //     addScript(script, null, applicationId);
        // })
        // 加入模板之后的新建脚本
        $("#newScriptDiv").on("click", "li span.blankScript", function () {

            var dataIndex = $(this).parent().attr("data-index");
            var pythonType = $(this).parent().attr("data-python");
            var script = getScriptObj(dataIndex, pythonType);
            var applicationId = getSelectedProjectId();
            $("#newScriptDiv").hide();
            addScript(script, getSelectedNode(), applicationId, script.default);
        })
        $("#newScriptDiv").on("click", "li span.templateScript", function () {
            var selectedNode = getSelectedNode();
            var treeNode = selectedNode ? (selectedNode.isParent ? selectedNode : selectedNode.getParentNode()) : null;
            templateModal($(this), treeNode);
        });
        $("#format").click(function () {
            format();
        })
        $("#newScriptDiv").on("click", "li.scriptLi", function (event) {
            event.stopPropagation();
            event.preventDefault();
        })
        $("#stop").click(function () {
            var node = $("#stop");
            if ($(".ico-disabled", node).length > 0) {
                return;
            }
            stopScript(datadevInit.getActiveWindow());
        })

        $("#dependencyButton").click(function (event) {
            if ($(".ico-disabled", $(this)).length > 0) {
                return;
            }
            var activeWindow = datadevInit.getActiveWindow();
            if (activeWindow.gitProjectId && activeWindow.gitProjectFilePath) {
                if (!activeWindow.jq("#relationDependencyId").val() || activeWindow.jq("#relationDependencyId").val() <= 0) {
                    choDep(activeWindow.gitProjectId, activeWindow.gitProjectFilePath)
                }
            }
        })
        $('#upLine').click(function () {
            $(".bdp-new-help-modal-backdrop").click();
            if ($(".ico-disabled", $("#upLine")).length > 0) {
                return;
            }
            var window = datadevInit.getActiveWindow();
            if (window.isShow == 1) {
                $.confirmMsg({content: "临时文件保存后再上线!"});
                return;
            }
            if (window.type != 5) {
               /* var name = window.name;
                var pattern = /^[0-9a-zA-z\-_]+\.(\w+)$/;
                if (pattern.test(name) && window.name.length < 256) {
                    datadevInit.uplineSaveScript(window);
                } else {
                    $.errorMsg("上线脚本名字只能是字母、数字、下划线、中线，且长度小于256，请先修改名称再上线！");
                }*/
                datadevInit.uplineSaveScript(window);
            } else {
                // $.errorMsg("只支持py、zip、sh、sql脚本上线！");
                $.errorMsg("只支持sql、py、zip、sh脚本上线！");
            }
        })
        $("#help").click(function () {
            window.open("http://bdp.jd.com/helpCenter/front/showDocumentList.html?appId=31");
        })

        $("#run").click(function () {
            $(".bdp-new-help-modal-backdrop").click();
            var activeWindow = datadevInit.getActiveWindow();
            runScript(activeWindow);
        })
        $("#debug").click(function () {
            $(".bdp-new-help-modal-backdrop").click();
            var activeWindow = datadevInit.getActiveWindow();
            runScript(activeWindow, true);
        })


        $("#removeTab").click(function () {
            $("#leftContentDiv").JdDataDevTab("removeTab", 0);
        })
        $("#save").click(function () {
            $(".bdp-new-help-modal-backdrop").click();

            saveScript();
        })
        $("#saveAs").click(saveAsScript);
        //git
        $("#git").click(function () {
            var node = $("#git");
            if ($(".ico-disabled", node).length > 0) {
                return;
            }
            var newScriptMenu = $("#pullPushMenuContent");
            var top = $(this).position().top;
            var left = $(this).position().left - 10;
            top = top + $("#git").height() + 8;
            newScriptMenu.css({top: top, left: left}).show();
            return false;
        })
        //右键新建脚本模板
        $("#pullPushMenuContent").mouseenter(function () {
            var newScriptMenu = $("#pullPushMenuContent");
            newScriptMenu.show();
        })
        $("#pullPushMenuContent").mouseleave(function () {
            var newScriptMenu = $("#pullPushMenuContent");
            newScriptMenu.hide();
        })
        $("#pullPushMenuContent").on("click", "li", function () {
            var activeWindow = datadevInit.getActiveWindow();
            if ($(this).hasClass("pull-li")) {
                pullFile(activeWindow.gitProjectId, activeWindow.gitProjectFilePath);
            } else if ($(this).hasClass("push-li")) {
                pushFile(activeWindow.gitProjectId, activeWindow.gitProjectFilePath);
            } else if ($(this).hasClass("commit-li")) {
                showGitHistory(activeWindow.gitProjectId, activeWindow.gitProjectFilePath, false);
            }
        })
    },
    initSelect: function () {
        commonAjaxEvents.commonPostAjax(getConfigUrl, {}, $("#queueCode"), function (node, data) {
            var lis = "<li class='defaultDropLi queueCodeDropLi active' data-index='-1'><span class='name'>不使用集市资源</span></li>";
            if (data && data.obj && data.obj.length > 0) {
                for (var index = 0; index < data.obj.length; index++) {
                    var cinfig = data.obj[index];
                    if (cinfig.engineType == null) {
                        cinfig.engineType = "";
                    }
                    configObj.push(cinfig);
                    lis += "<li class='queueCodeDropLi' data-index='" + cinfig.id + "'><span class='name'>" + cinfig.name + "</span></li>";
                }
                $("#queueName").text("不使用集市资源");
            } else {
                $("#queueName").text("配置账号队列");
            }
            $("#queueCodeDropDiv ul.queueCodeDropUl").prepend(lis);
        })
        $("#queueCodeDropDiv").appendTo("body");

        $("#runErrorDiv").appendTo("body");
        $("#newScriptDiv").appendTo("body");
        $("#runAddtionContent").appendTo("body");
        $("#spins").appendTo("body");
        $("#codeModal").modal({
            backdrop: 'static',
            show: false,
            keyboard: false
        });
        $("#queueCode").click(function (event) {
            $(".bdp-new-help-modal-backdrop").click();
            var top = $(this).offset().top;
            var left = $(this).offset().left;
            var height = $(this).height();
            top = top + height;
            hiddenAllRightMeun();
            $("#queueCodeDropDiv").css({top: top, left: left}).show();
            event.stopPropagation();
            event.preventDefault();
        })
        $("#queueCodeDropDiv").on("click", "li.queueCodeDropLi", function () {
            var dataIndex = $(this).attr("data-index");
            changeConfig(dataIndex, datadevInit.getActiveWindow().key)
            hiddenAllRightMeun();
        })
        $("#queueCodeLi").click(function () {
            $("#codeModal").modal("show");
        })
    },
    defaultButtons: function () {
        this.changeTopButton(TOP_BUTTON.addScriptTab, "available");
        this.changeTopButton(TOP_BUTTON.save, "unavailable");
        this.changeTopButton(TOP_BUTTON.saveAs, "unavailable");
        this.changeTopButton(TOP_BUTTON.format, "unavailable");
        this.changeTopButton(TOP_BUTTON.run, "unavailable");
        this.changeTopButton(TOP_BUTTON.dependencyButton, "unavailable");
        this.changeTopButton(TOP_BUTTON.stop, "unavailable");
        this.changeTopButton(TOP_BUTTON.upLine, "unavailable");
        this.changeTopButton(TOP_BUTTON.debug, "unavailable");
        this.changeTopButton(TOP_BUTTON.git, "unavailable");
    },
    changeTopButtons: function (key) {
        //根据状态去决定应该用什么颜色
        var scriptMapParams = scriptMap.get(key);
        if (scriptMapParams) {
            for (var button in TOP_BUTTON) {
                this.changeTopButton(TOP_BUTTON[button], scriptMapParams[button] == true ? "available" : "unavailable", key);
            }
        }
    },
    changeTopButton: function (buttonId, classs, key) {
        //available: 运行特有的 绿色
        //unavailable:停止特有的 红色
        var button = $("#" + buttonId);
        var icon = $(".bdp-icon", button);
        if (key) {
            var window = datadevInit.getActiveWindow(key);
            window && window.status && window.win.changeSideBarStatus && window.win.changeSideBarStatus(buttonId, classs === "available");
        }
        if (classs === "available") {
            icon.removeClass("ico-disabled");
        } else {
            icon.addClass("ico-disabled");
        }
    },
    initTitle: function () {
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
            } else if (type == "mouseleave" || type == 'mouseout') {
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
    doImportScript: function () {
        var scriptId = $("#scriptId").val() * 1;
        var scriptName = encodeURIComponent(encodeURIComponent($("#scriptName").val()));
        var scriptVersion = $("#scriptVersion").val();
        var jsdAppgroupId = $("#jsdAppgroupId").val();
        var gitProjectId = $("#gitProjectId").val();
        var uri = "/scriptcenter/project/tranferScript.html?scriptId=" + scriptId + "&scriptName=" + scriptName + "&scriptVersion=" + scriptVersion + "&jsdAppgroupId=" + jsdAppgroupId + "&projectId=" + gitProjectId;
        var tranferArt = $.dialog.open(uri, {
            title: "将脚本管理中脚本导入开发平台",
            lock: true,
            width: "600px",
            height: "500px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("tranferArt", tranferArt);
        $.dialog.data("addNodeByImport", addNodeByImport);
        $.dialog.data("scriptMergeContentMap", scriptMergeContentMap);
        $.dialog.data("diff", diff);
        $.dialog.data("getKey", getKey);
        $.dialog.data("getActiveWindow", datadevInit.getActiveWindow);
        $.dialog.data("successMsg", function () {
            $.successMsg("同步成功")
        });
        $.dialog.data("locationScript", locationScript);
        $.dialog.data("openScript", openScript);
        $.dialog.data("resetUrl", resetUrl);
    },

    initSearch: function () {

        $("#backgroundSearch").click(function (event) {
            if (event.target.id && event.target.id === "backgroundSearch") {
                $(this).hide();
            }
        })
        $('#searchName').autocompleter({
            highlightMatches: true,
            source: "/scriptcenter/script/searchArrays.ajax",
            template: '{{ label }} <span>({{ gitProjectFilePath }})</span>',
            empty: false,
            cache: false,
            focusOpen: true,
            selectFirst: true,
            changeWhenSelect: false,
            combine: function (params) {
                var applicationId = getSelectedProjectId();
                return {
                    gitProjectId: applicationId,
                    fileTypes: datadevInit.getSearchSelectTypes(),
                    limit: 10
                }
            },
            callback: function (value, index, selected) {
                if (selected) {
                    $("#backgroundSearch").hide();
                    openScript(selected.gitProjectId, selected.gitProjectFilePath, selected.label);
                }
            }
        });
    },
    getSearchSelectTypes: function () {
        var fileTyps = $("input:checked", $("#filTypes"));
        var str = "";
        for (var index = 0; index < fileTyps.length; index++) {
            var dataCode = $(fileTyps[index]).attr("data-code");
            if (dataCode * 1 == -1) {
                continue;
            }
            str += "," + dataCode;
        }
        return str.length > 1 ? str.substr(1) : "";
    },
    initSearchAllSelectType: function () {
        var checked = $("input:checked", $(".typePullDown"));
        var appendSpan = "";
        var filTypes = $("#filTypes");

        if (checked.length == $("input", $(".typePullDown")).length || checked.length == 0) {
            var temp = $("<span/>");
            temp.addClass("fileType").attr("data-type", "all").html("all").attr("data-code", "-1");
            filTypes.html(temp);
            return;
        }
        filTypes.html("")
        for (var index = 0; index < checked.length; index++) {
            var node = $(checked[index]);
            var temp = $("<span/>");
            var dataType = node.attr("data-type");
            var dataCode = node.attr("data-code");
            temp.addClass("fileType").attr("data-type", dataType).attr("data-code", dataCode).html(dataType);
            filTypes.append(temp);
        }
    },
    initSearchType: function () {

        $(".typePullDown").mouseleave(function () {
            $(this).css("display", "none");
        })

        $("#scriptTypeSelectSpan").click(function () {
            // $(".typePullDown").toggle();
        })
        $("input", $(".typePullDown")).click(function () {
            // var isChecked = ($(this).is(":checked"));
            //获取所有选中的值
            // datadevInit.initSearchAllSelectType();
        })
    },
    lackProject: function (gitProjectId, gitPath) {
        $.newBdpMsg({
            title: "提示",
            mainContent: "<div style='padding: 20px;line-height: 20px;'>当前脚本所属git项目已移除bdp_ide账户或项目被删除。需重新导入或将bdp_ide账号加入原git项目。</div>",
            width: "400px",
            buttons: [
                {
                    text: "重新导入",
                    event: function () {
                        $.removeNewMsg();
                        datadevInit.doImportScript();
                    },
                    btnClass: 'bdp-btn-primary'
                },
                {
                    text: "跳转至git",
                    event: function () {
                        popUrl();
                        var gitBasePath = "http://git.jd.com/";
                        gitBasePath += (gitPath ? gitPath : "");
                        window.open(gitBasePath);
                    },
                    btnClass: 'bdp-btn-primary'
                },
                {
                    text: "取消",
                    event: function () {
                        $.removeNewMsg();
                    }
                }
            ]
        })
    },
    lackScript: function () {
        $.newBdpMsg({
            title: "提示",
            mainContent: "<div style='padding: 10px'>当前脚本不存在，请重新导入脚本!</div>",
            width: "300px",
            buttons: [
                {
                    text: "重新导入",
                    event: function () {
                        $.removeNewMsg();
                        datadevInit.doImportScript();
                    },
                    btnClass: 'bdp-btn-primary'
                },
                {
                    text: "取消",
                    event: function () {
                        $.removeNewMsg();
                    }
                }
            ]
        })
    },
    //chooseLeft :undefined 不作处理  true左边按钮变为 "继续编辑"  false：右边按钮变为 "继续编辑"
    mergeChoose: function (chooseLeft, modified) {
        var currentVersion = $("#buffaloCurrentVersion").val();
        var lastVersion = $("#datadevLastVersion").val();
        var gitProjectId = $("#gitProjectId").val();
        var gitProjectFilePath = $("#gitProjectFilePath").val();
        var scriptType = $("#mergeScriptType").val()
        var encodeGitProjectFilePath = encodeURIComponent(encodeURIComponent(gitProjectFilePath));
        $.dialog.data("topTips", "说明：检测到调度中的版本与开发平台不一致，请选择所需要编辑的版本。");
        $.dialog.data("leftTips", "调度版本（" + currentVersion + "）");
        $.dialog.data("rightTips", "开发平台版本（" + lastVersion + "）");
        //choice 0:调度版本  1开发平台版本
        $.dialog.data("callBack", function (isLeft, value, version, dependencyId) {
            var key = getKey(gitProjectId, gitProjectFilePath);
            var activeWindow = datadevInit.getActiveWindow(key);
            if (activeWindow && activeWindow.win && activeWindow.win.initHasVersionChange) {
                activeWindow.win.initHasVersionChange(isLeft, value, version, dependencyId);
            } else {
                initFunctionMap.put(key, function (actKey, win) {
                    win && win.initHasVersionChange(isLeft, value, version, dependencyId);
                })
                openScript(gitProjectId, gitProjectFilePath, $("#scriptName").val(), null, null, null, isLeft ? currentVersion : null);
            }
        });
        var dialog = $.dialog.open("/scriptcenter/script/aceDiffChoice.html?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + encodeGitProjectFilePath
            + "&currentVersion=" + currentVersion + "&lastVersion=" + lastVersion + "&scriptType=" + scriptType + (chooseLeft == undefined ? "&choice=0" : chooseLeft ? "&choice=-1" : "&choice=1") + (modified ? "&modifiedStatus=1" : "&modifiedStatus=0"), {
            title: "脚本选择",
            lock: true,
            width: "70%",
            height: "70%",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("dialog", dialog);
    },
    noAuth: function () {
        var ownerErps = $("#ownerErps").val();
        var erps = ownerErps.split(",");
        var ownerHtml = "";
        for (var index = 0; index < erps.length; index++) {
            if (erps[index]) {
                ownerHtml += "<span style='    margin: 3px 5px;'>" + erps[index] + "</span>";
            }
        }
        $.newBdpMsg({
            title: "提示",
            mainContent: "<div style='padding: 10px 0px 10px 5px'>项目无权限，请联系项目负责人!</div><div style='display: flex;flex-wrap: wrap'>" + ownerHtml + "</div>",
            width: "300px",
            buttons: [
                {
                    text: "取消",
                    event: function () {
                        $.removeNewMsg();
                    }
                }
            ]
        })
        popUrl();
    },
    initOpenScript: function () {
        var gitProjectId = $("#gitProjectId").val() * 1;
        var gitProjectFilePath = $("#gitProjectFilePath").val().trim();
        var scriptName = $("#scriptName").val();
        var openScriptType = $("#openScriptType").val();
        var targetGitPath = $("#targetGitPath").val();
        var liCookie = HOME_COOKIE.getLiCookie();
        var array = HOME_COOKIE.coverToCookieArray(liCookie);
        $("#codeEditContainer").JdDataDevTab("initTabs", array);
        var active = HOME_COOKIE.getActiveLiCookie();
        switch (parseInt(openScriptType)) {
            case 0:
                //正常导入主页，打开cookie记录打开的脚本
                $("#codeEditContainer").JdDataDevTab("activeLiByDataId", active);
                break;
            case 1:
                //只传调度脚本id，没有传开发平台项目与脚本路径 -> 调度导入
                datadevInit.doImportScript();
                break;
            case 2:
                //项目存在，但是开发平台脚本被删除 -> 重建脚本
                datadevInit.lackScript();
                break;
            case 3:
                //项目存在，但是当前用户无权限 ->重新添加权限 目前会直接抛错
                // $.errorMsg("项目无权限，请联系项目负责人!");
                datadevInit.noAuth();
                break;
            case 4:
                //bdp_ide虚拟账号已经拉不到该项目 ->重新添加权限或者重建项目重建脚本
                datadevInit.lackProject(gitProjectId, targetGitPath);
                break;
            case 5:
                //项目与脚本都存在，并且有项目权限 -> 正常打开
                openScript(gitProjectId, gitProjectFilePath, scriptName);
                break;
            case 6:
                datadevInit.mergeChoose();
                break;
            default:
                break;
        }
    },
    initPostMessage: function () {
        window.addEventListener("message", function (e) {
            console.log(e)
        });
    },
    initPackZip: function () {
        $("#packModal").modal({
            backdrop: 'static',
            show: false,
            keyboard: false
        });
    }

}

function runOrStopScript() {
    if ($(".ico-disabled", $("#run")).length == 0) {
        $("#run").click();
    }
    // else if($(".ico-disabled", $("#stop")).length==0){
    //     $("#stop").click();
    // }
}

function copySelected() {
    var seletedSpans = $(".datadevSelectedText");
    if (seletedSpans.length == 0) {
        //不拦截事件
        return true;
    } else {
        //拦截事件
        var str = "";
        seletedSpans.each(function (index, element) {
            str += $(this).text() + "\n";
        })
        if (str.length > 0) {
            str = str.substring(0, str.length - 2);
        }
        copyContent(str)
    }
}

function initKeyMap() {
    cg = $("#cg").val();
    var isMac = (navigator.userAgent.indexOf('Mac') >= 0) ? true : false;
    if (isMac) {
        $("#save").attr("data-title", "⌘S");
        $("#format").attr("data-title", "⌃⌥L");
        $("#scriptManageTool span.search").attr("data-title", "⇧⇧");
        $("#run").attr("data-title", "⌃↩");
    } else {
        $("#save").attr("data-title", "Ctrl+S");
        $("#format").attr("data-title", "Ctrl+Shift+F");
        $("#scriptManageTool span.search").attr("data-title", "Shift+Shift");
        $("#run").attr("data-title", "Ctrl+Enter");
    }
    $(document).keydown(function (event) {
        if (!isMac && event.ctrlKey && event.keyCode == 83) {
            //ctrl S
            saveScript();
            return false;
        } else if (isMac && event.metaKey && event.keyCode == 83) {
            //mac command S
            saveScript();
            return false;
        } else if (event.ctrlKey && event.keyCode == 13) {
            //ctrl enter
            runOrStopScript();
            return false;
        } else if (!isMac && event.ctrlKey && event.keyCode == 67 || isMac && event.metaKey && event.keyCode == 67) {
            //ctrl c command c
            return copySelected();
        } else if (event.keyCode == 113) {
            var selectedLi = $("#tree li.selectedNode");
            if (selectedLi && selectedLi.length == 1) {
                var tId = selectedLi.attr("id");
                var node = zTree.getNodeByTId(tId);
                if (!node.isParent) {
                    renameScript(node.gitProjectId, node.path);
                }
            }
            return false;
        } else if (isMac && event.ctrlKey && event.altKey && event.keyCode == 76) {
            $("#format").click();
            return false;
        } else if (!isMac && event.ctrlKey && event.shiftKey && event.keyCode == 70) {
            $("#format").click();
            return false;
        } else if (event.shiftKey && event.keyCode == 119) {
            var activeWIndow = datadevInit.getActiveWindow();
            if (activeWIndow && activeWIndow.jq) {
                activeWIndow.jq("#stepOut").click();
            }
        } else if (event.keyCode == 118) {
            var activeWIndow = datadevInit.getActiveWindow();
            if (activeWIndow && activeWIndow.jq) {
                activeWIndow.jq("#stepInto").click();
            }
        } else if (event.keyCode == 119) {
            var activeWIndow = datadevInit.getActiveWindow();
            if (activeWIndow && activeWIndow.jq) {
                activeWIndow.jq("#stepOver").click();
            }
        }
    })
}

var firstShiftKeyTime = 0;
var clickShiftKeyTime = 0;

$(document).ready(function () {
    initKeyMap();
    resetUrl();
    datadevInit.initChangeSize();
    datadevInit.initButtonEvent();
    datadevInit.initSelect();
    datadevInit.defaultButtons();
    datadevInit.initTitle();
    datadevInit.initOpenScript();
    datadevInit.initSearch();
    datadevInit.initSearchType();
    datadevInit.initPostMessage();
    datadevInit.initPackZip();
})



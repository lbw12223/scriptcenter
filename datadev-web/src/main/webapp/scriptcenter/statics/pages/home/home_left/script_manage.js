var zTree;
var hasProjectStatus = false;
var locationScriptObj = {};
var isRefresh = false;
var SCRIPT_GIT_STSTUS = {
    ADD: "ADD",
    MOD: "MOD",
    NON: "NON",
    DEL: "DEL"
}

function getSelectedProjectPath() {
    return $("#appSelect").select2("data") && $("#appSelect").select2("data").appgroupName || "";
}

function getIframeOffset() {
    var activeWindow = datadevInit.getActiveWindow();
    if (activeWindow &&  activeWindow.activeDiv) {
        return $(activeWindow.activeDiv).offset();
    }
    return null;
}

function getWindowSize() {
    var windowHeight = window.innerHeight || ((document.body) && (document.body.clientHeight));
    var windowWidth = window.innerWidth || ((document.body) && (document.body.clientWidth));
    return {
        width: windowWidth,
        height: windowHeight
    }
}

function refreshFileInfoStatus(gitProjectId, gitProjectFilePath, fileMd5, version, gitStatus) {
    var scriptWindow = datadevInit.getActiveWindow(getKey(gitProjectId, gitProjectFilePath));
    scriptWindow && scriptWindow.win && scriptWindow.win.refreshFileInfoStatus(fileMd5, version, gitStatus);
    if (gitStatus && scriptWindow && scriptWindow.activeLi) {
        var li = scriptWindow.activeLi;
        $(".labelName", li).attr("git-status", gitStatus);
    }
    updateNode({
        gitProjectId: gitProjectId,
        path: gitProjectFilePath,
        gitStatus: gitStatus
    })
}

function getSelectedNode() {
    var treeNode = null;
    var selectedLi = $("#tree li.selectedNode");
    if (selectedLi && selectedLi.length == 1) {
        var tId = selectedLi.attr("id");
        treeNode = zTree.getNodeByTId(tId);
    }
    return treeNode;
}

/**
 * status 只能取NON：无变化  ADD：增加状态  MOD：修改状态 DEL：git删除状态
 * @param gitProjectId
 * @param gitProjectFilePath
 * @param Status
 */
function changeFileGitStatus(gitProjectId, gitProjectFilePath, Status) {
    refreshFileInfoStatus(gitProjectId, gitProjectFilePath, null, null, Status);
}

/**
 *
 * @param gitProjectId
 * @param gitProjectFilePath
 * @param Status
 */
function changeDirGitStatus(gitProjectId, gitProjectDirPath, Status) {
    if (gitProjectId == getSelectedProjectId()) {
        var nodes = [];
        var thisNode = zTree.getNodeByParam("path", gitProjectDirPath);
        if (!gitProjectDirPath) {
            var parentNodes = zTree.getNodes();
            nodes = zTree.transformToArray(parentNodes);
        } else if (thisNode) {
            var children = thisNode.children;
            if (children && children.length > 0) {
                nodes = zTree.transformToArray(children);
            }
        }
        if (nodes && nodes.length > 0) {
            for (var updateIndex = 0; updateIndex < nodes.length; updateIndex++) {
                var ztreeNode = nodes[updateIndex];
                if (!ztreeNode.isParent) {
                    changeFileGitStatus(gitProjectId, ztreeNode.path, SCRIPT_GIT_STSTUS.NON);
                }
            }
        }
    } else {
        var windows = datadevInit.getAllWindow();
        if (windows && windows.length > 0) {
            for (var index = 0; index < windows.length; index++) {
                var scriptWindow = windows[index];
                if (scriptWindow && scriptWindow.win && scriptWindow.gitProjectId == gitProjectId && (!gitProjectDirPath || scriptWindow.gitProjectFilePath && scriptWindow.gitProjectFilePath.startsWith(gitProjectDirPath + "/"))) {
                    changeFileGitStatus(scriptWindow.gitProjectId, scriptWindow.gitProjectFilePath, SCRIPT_GIT_STSTUS.NON);
                }
            }
        }
    }
}

function getZtreeNode(zNode) {
    var script = getScriptObj(zNode.type);
    return {
        parentPath: zNode.parentPath,
        path: zNode.path,
        name: "<span class='" + (zNode.parChl == 0 ? SCRIPT_GIT_STSTUS.NON : zNode.gitStatus) + "'>" + zNode.name + "</span>",
        iconSkin: script.icon,
        isParent: zNode.parChl == 0 ? true : false,
        type: zNode.type,
        runType: zNode.runType,
        gitProjectId: zNode.gitProjectId,
        open: zNode.openStatus == 1,
        targetDir: zNode.targetDir,
        gitStatus: zNode.gitStatus,
        oriName: zNode.name
    }
}

function addNodeByImport(nodeArr, gitProjectId, callback) {
    var nowGitProjectId = $("#appSelect").val();
    if (nodeArr && nodeArr.length > 0 && gitProjectId && gitProjectId == nowGitProjectId) {
        for (var i = 0; i < nodeArr.length; i++) {
            var zNode = nodeArr[i];
            while (zNode != null) {
                var parNode = zTree.getNodeByParam("path", zNode.parentPath);
                var nowNode = zTree.getNodeByParam("path", zNode.path);
                if (!nowNode) {
                    if (!zNode.parentPath || (parNode && parNode.zAsync)) {
                        var addNewNode = getZtreeNode(zNode);
                        var newNodes = [addNewNode];
                        var index = 0;
                        if (zNode.parChl == 1) {
                            var nodes = zTree.getNodesByFilter(function (node) {
                                return node.parentPath == zNode.parentPath && node.isParent == true;
                            });
                            index = nodes && nodes.length || 0;
                        }
                        zTree.addNodes(parNode, index, newNodes);
                    } else {
                        zTree.expandNode(parNode);
                    }
                    break;
                } else {
                    if (zNode.children && zNode.children.length > 0) {
                        zNode = zNode.children[0];
                    } else {
                        break;
                    }
                }
            }
        }

    }
    $.successMsg("同步成功", 2000, callback)

}

function getSelectedProjectId() {
    return $("#appSelect").val();
}


function canLocation() {
    return locationScriptObj && locationScriptObj.gitProjectId && locationScriptObj.gitProjectFilePath;
}

function setScriptLocation(gitProjectId, gitProjectFilePath) {
    locationScriptObj.gitProjectId = gitProjectId;
    locationScriptObj.gitProjectFilePath = gitProjectFilePath;
}

function resetLocation() {
    locationScriptObj = {};
}

function loadScript(gitProjectId, gitProjectFilePath) {
    if (getSelectedProjectId() == gitProjectId) {
        var tmpPath = gitProjectFilePath;
        var selectNode = zTree.getNodeByParam("path", tmpPath);
        if (selectNode == null) {
            while (selectNode == null && tmpPath != "") {
                var index = tmpPath.lastIndexOf("/");
                tmpPath = tmpPath.substring(0, index);
                selectNode = zTree.getNodeByParam("path", tmpPath);
            }
            if (selectNode != null || tmpPath == "") {
                zTree.removeChildNodes(selectNode);
                commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScripsByDirId.ajax", {
                    path: tmpPath,
                    gitProjectId: gitProjectId,
                    selectFilePath: gitProjectFilePath,
                    isSelectRoot: tmpPath != "",
                    targetRange: 1
                }, null, function (node, data) {
                    if (data.obj) {
                        for (var index = 0; index < data.obj.length; index++) {
                            var scriptObj = getZtreeNode(data.obj[index]);
                            var parentPath = scriptObj.parentPath;
                            var parNode = zTree.getNodeByParam("path", parentPath);
                            if (parNode) {
                                parNode.zAsync = true;
                                zTree.addNodes(parNode, scriptObj, true);
                            } else if (parentPath == "") {
                                zTree.addNodes(null, scriptObj, true);
                            }
                        }
                    }
                })
            }
        }
    }
}

function locationScript(gitProjectId, gitProjectFilePath, scriptId) {
    if (!(gitProjectId && gitProjectFilePath)) {
        return;
    }
    commonAjaxEvents.commonPostAjax("/scriptcenter/script/checkFileExist.ajax", {
        gitProjectId: gitProjectId,
        gitProjectFilePath: gitProjectFilePath
    }, null, function (node, data) {
        //脚本文件存在才定位
        if (data.obj.result == 1) {
            if(data.obj.isTemplate){
                //模板文件不定位
                return;
            } else if (data.obj.isShow == 1) {
                //isShow = 1说明临时文件只转换列表
                var nowGitProjectId = $("#appSelect").val();
                if (nowGitProjectId != gitProjectId) {
                    $("#appSelect").val(gitProjectId).trigger("change", true);
                }
            } else {
                setScriptLocation(gitProjectId, gitProjectFilePath);
                if (getSelectedProjectId() == gitProjectId) {
                    var tmpPath = gitProjectFilePath;
                    var selectNode = zTree.getNodeByParam("path", tmpPath);
                    if (selectNode != null) {
                        locationScriptPositon(selectNode, true)
                    } else {
                        while (selectNode == null && tmpPath != "") {
                            var index = tmpPath.lastIndexOf("/");
                            tmpPath = tmpPath.substring(0, index);
                            selectNode = zTree.getNodeByParam("path", tmpPath);
                        }
                        if (selectNode != null || tmpPath == "") {
                            zTree.removeChildNodes(selectNode);
                            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScripsByDirId.ajax", {
                                path: tmpPath,
                                gitProjectId: gitProjectId,
                                selectFilePath: gitProjectFilePath,
                                isSelectRoot: tmpPath != "",
                                targetRange: 1
                            }, null, function (node, data) {
                                if (data.obj) {
                                    for (var index = 0; index < data.obj.length; index++) {
                                        var scriptObj = getZtreeNode(data.obj[index]);
                                        var parentPath = scriptObj.parentPath;
                                        var parNode = zTree.getNodeByParam("path", parentPath);
                                        if (parNode) {
                                            parNode.zAsync = true;
                                            zTree.addNodes(parNode, scriptObj, true);
                                        } else if (parentPath == "") {
                                            zTree.addNodes(null, scriptObj, true);
                                        }
                                    }
                                    var selectNode = zTree.getNodeByParam("path", gitProjectFilePath);
                                    locationScriptPositon(selectNode)
                                }
                            })
                        }
                        return;
                    }
                } else {
                    $("#appSelect").val(gitProjectId).trigger("change", true);
                }
            }

        } else {
            $.errorMsg("该脚本已被删除");
        }
    })
}

function locationScriptPositon(selectNode) {
    if (selectNode) {
        var parentNode = selectNode.getParentNode();
        if (parentNode && !parentNode.open) {
            zTree.expandNode(parentNode, true, false, true, true);
        } else {
            var selectNodeDom = $("#" + selectNode.tId + " .nodeDiv");
            selectNodeDom.click();
            if (selectNodeDom.length > 0) {
                var nodeTop = selectNodeDom.offset().top;
                var divTop = $("#scriptManageTab .script-content").offset().top;
                var content = $("#tree").closest('.script-content');
                var contentHeigth = $(content).height();
                $(content).scrollTop($(content).scrollTop() + nodeTop - divTop - 0.5 * contentHeigth);
            }
            resetLocation();
        }
    }
}

function getSettingAsync(gitProjectId) {
    var async = {
        enable: true,
        url: "/scriptcenter/script/getScripsByDirId.ajax",
        autoParam: ["path"],
        otherParam: ["gitProjectId", gitProjectId, "targetRange", 1],
        dataFilter: function (treeId, parentNode, responseData) {
            if (responseData && responseData.obj && responseData.obj.length > 0) {
                var array = responseData.obj;
                var resultArray = new Array();
                for (var i = 0; i < array.length; i++) {
                    var znode = getZtreeNode(array[i]);
                    resultArray.push(znode);
                }
                return resultArray;
            } else {
                return [];
            }
        }
    };
    return async;
}

function showSearchScript() {
    var $backgroundSearch = $("#backgroundSearch");
    if ($backgroundSearch.css("display") === "none") {
        $(".autocompleter-list").html("");
        $("#searchName").val("");
        window.setTimeout(function () {
            $("#searchName").focus();
        }, 300);
        $backgroundSearch.show();
    }
}

function downloadScript(path, gitProjectId) {
    commonAjaxEvents.commonPostAjax("/scriptcenter/script/checkFileExist.ajax", {
        gitProjectId: gitProjectId,
        gitProjectFilePath: path
    }, null, function (node, data) {
        if (data.obj.result == 1) {
            window.onbeforeunload = null;
            var gitProjectFilePath = encodeURIComponent(encodeURIComponent(path));
            var form_id = "script_form_downLoad";
            if ($("#" + form_id).length > 0) {
                $("#" + form_id).remove();
            }
            var temp = document.createElement("form");
            temp.action = "/scriptcenter/api/downloadScriptNoAuth.ajax" + "?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath;
            temp.method = "post";
            temp.style.display = "none";
            temp.id = form_id;
            document.body.appendChild(temp);
            temp.submit();
            $("#" + form_id).remove();

        } else {
            $.errorMsg("该脚本已被删除");
        }
    })
}

function parentRenameScript(gitProjectId, gitProjectFilePath, gitProjectDirPath, name, type) {
    var info = {
        gitProjectId: gitProjectId,
        gitProjectFilePath: gitProjectFilePath,
        gitProjectDirPath: gitProjectDirPath,
        name: name,
        type: type
    };
    $("#saveModal").FileMode("scriptRename", info);
}

function renameScript(gitProjectId, gitProjectFilePath) {
    var node = zTree.getNodeByParam("path", gitProjectFilePath);
    if (node && !node.isParent) {
        var info = {
            gitProjectId: node.gitProjectId,
            gitProjectFilePath: node.path,
            gitProjectDirPath: node.parentPath,
            name: node.oriName,
            type: node.type
        };
        $("#saveModal").FileMode("scriptRename", info);
    }
}

function updateNode(newNode) {
    if (getSelectedProjectId() == newNode.gitProjectId && newNode.path) {
        var node = zTree.getNodeByParam("path", newNode.path);
        if (node != null) {
            if (newNode.gitStatus) {
                newNode.name = "<span class='" + newNode.gitStatus + "'>" + node.oriName + "</span>";
            }
            $.extend(node, newNode);
            zTree.updateNode(node);
        }
        if (newNode.runType != undefined) {
            var activeWindow = datadevInit.getActiveWindow(getKey(newNode.gitProjectId, newNode.path));
            activeWindow && activeWindow.win && activeWindow.win.changeDep(newNode.runType == 1);
        }
    }
}


function choDep(gitProjectId, gitProjectFilePath) {
    gitProjectFilePath = encodeURIComponent(encodeURIComponent(gitProjectFilePath));
    var url = "/scriptcenter/script/dependency.html?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath;
    var dependencyArt = $.dialog.open(url, {
        title: "设置依赖关系",
        lock: true,
        width: "55%",
        height: "70%",
        opacity: 0.5,
        esc: false,
        close: function () {
        }
    });
    $.dialog.data("dependencyArt", dependencyArt);
    $.dialog.data("updateNode", updateNode);
    $.dialog.data("pack2Zip", pack2Zip);
}

function pack2Zip(gitProjectId, gitProjectFilePath) {
    gitProjectFilePath = encodeURIComponent(encodeURIComponent(gitProjectFilePath));
    var url = "/scriptcenter/script/packZip.html?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath;
    var packZipArt = $.dialog.open(url, {
        title: "打包zip文件",
        lock: true,
        width: "700",
        height: "520",
        opacity: 0.5,
        esc: false,
        close: function () {
        }
    });
    $.dialog.data("packZipArt", packZipArt);
    $.dialog.data("loadScript", loadScript);
    $.dialog.data("updateNode", updateNode);
}

$(function () {
        var getAppUrl = "/scriptcenter/script/getAppByErp.ajax";
        var getDirsUrl = "/scriptcenter/script/getScripsByDirId.ajax";
        var deleteDirUrl = "/scriptcenter/script/deleteDir.ajax";
        var deleteScriptUrl = "/scriptcenter/script/remove.ajax";
        var getScriptBaseUrl = "/scriptcenter/scriptFile/getInfo.ajax";
        var pullDirUrl = "/scriptcenter/script/pullDir.ajax";
        var pullFileUrl = "/scriptcenter/script/pullFile.ajax";
        var pushDirUrl = "/scriptcenter/script/pushDir.ajax";
        var preSelectedId = undefined;
        var rightClickNode;//右键菜单的节点
        var oldnodes = [];
        var setting = {};
        initSelect();
        initEvent();
        initMenu();

        function addSelect(appgroupId, msg) {
            if (!hasProjectStatus) {
                $("#gitManage").hide();
                $("#scriptManageTab").show();
                $("#appSelect").val(appgroupId).trigger("change");
            }
            $.successMsg(msg);
        }

        $.dialog.data("addSelect", addSelect);

        // // 打开脚本模板页面
        // function templateModal() {
        //     $(".rightMenu").hide();
        //     $(".scriptDiv").hide();
        //     var scriptType = $(this).parent().attr("data-index");
        //     var pythonType = $(this).parent().attr("data-python");
        //     var url = getTemplateHtml + "?scriptType=" + scriptType;
        //     if (pythonType) {
        //         url += "&pythonType=" + pythonType;
        //     }
        //     var templateArt = $.dialog.open(url, {
        //         title: "选择脚本模板",
        //         lock: true,
        //         width: "942px",
        //         height: "564px",
        //         opacity: 0.5,
        //         esc: false,
        //         close: function () {
        //         }
        //     });
        //     $.dialog.data("templateArt", templateArt);
        //     $.dialog.data("addScript", addScript);
        //     $.dialog.data("addScriptDirNode", rightClickNode);
        //     $.dialog.data("selectedProjectId", getSelectedProjectId());
        //     $.dialog.data("scriptTypeArr", scriptTypeArr);
        // }


        function initSelect() {
            $("#appSelect").select2({
                multiple: false,
                cache: true,
                // allowClear: true,
                placeholder: "请选择",
                separator: ",",
                ajax: {
                    url: getAppUrl,
                    dataType: 'json',
                    type: 'post',
                    cache: true,
                    data: function (term) {
                        return {
                            keyword: term.trim()
                        };
                    },
                    results: function (data) {
                        return {
                            results: data.obj
                        };
                    }
                },
                initSelection: function (element, callback) {
                    $.ajax({
                        url: getAppUrl,
                        data: {},
                        type: "post",
                        success: function (result) {
                            if (result.obj && result.obj.length > 0) {
                                // if (false) {
                                hasProjectStatus = true;
                                var gitProjectId = $("#gitProjectId").val() * 1;
                                var cookieProjectId = (element.val() && element.val() > 0) ? element.val() : (gitProjectId ? gitProjectId : HOME_COOKIE.getActiveGitCookie($("#loginErp").val()));
                                var initApp = result.obj[0];
                                if (cookieProjectId) {
                                    for (var index = 0; index < result.obj.length; index++) {
                                        if (cookieProjectId == result.obj[index].appgroupId) {
                                            initApp = result.obj[index];
                                            break;
                                        }
                                    }
                                }
                                $("#appSelect").val(initApp.appgroupId);
                                callback(initApp);
                                initZtree();
                            } else {
                                $("#scriptManageTab").hide();
                                $("#gitManage").show();
                                hasProjectStatus = false;
                            }
                        },
                        dataType: 'json'
                    });
                },
                id: function (item) {
                    return item.appgroupId
                },
                formatResult: function (item) {
                    return item.appgroupName;
                },
                formatSelection: function (item) {
                    return item.appgroupName;
                },
                dropdownCssClass: "appSelectDrop",
                formatSearching: function () {
                    return "加载中..."
                },
                formatNoMatches: function (term, data) {
                    return "没有匹配结果."
                },
                escapeMarkup: function (m) {
                    return m;
                }
            })
            $("#scriptManageTab").on("mouseenter mouseleave mousemove", ".appSelect", function (event) {
                var left = event.pageX + 8, top = event.pageY + 8;
                var ele = event.target;
                var title = getSelectedProjectPath();
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
                return false;
            })
            $(".appSelectDrop").on("mouseenter mouseleave mousemove", ".select2-result", function (event) {
                var left = event.pageX + 8, top = event.pageY + 8;
                var ele = event.target;
                var title = $(this).find(".select2-result-label").text();
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
                return false;
            })
        }

        function initZtree(isKeepOpen, openDir) {

            var selectedData = $("#appSelect").select2("data");
            if (selectedData) {
                var gitProjectId = selectedData.appgroupId;
                setting = {
                    view: {
                        dblClickExpand: false,
                        showLine: false,
                        selectedMulti: false,
                        initPadding: 25,
                        initPaddingLeft: 10,
                        nameIsHTML: true,
                        showTitle: false
                    },
                    async: getSettingAsync(gitProjectId),
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
                            zTree = $.fn.zTree.getZTreeObj("tree");
                            if (treeNode.isParent) {
                                zTree.expandNode(treeNode);
                            }
                            var id = treeNode.tId;
                            if (preSelectedId) {
                                $("#" + preSelectedId).removeClass("selectedNode")
                            }
                            $("#" + id).addClass("selectedNode");
                            preSelectedId = id;
                            return true;
                        },
                        onRightClick: function (event, treeId, treeNode) {
                            rightClickNode = treeNode;
                            if (!treeNode) {
                                $("#rightClickMenu li.remove").hide();
                                showRightDirMenu(event, $("#rightClickMenu"));
                                rightClickNode = undefined;
                                $("#rightClickGitMenu").attr("data-type", "dir");
                            } else if (treeNode.isParent) {
                                var parents = treeNode.getPath();
                                var menu = undefined;
                                if (treeNode.targetDir || (parents && parents[0].targetDir)) {
                                    menu = $("#rightClickTargetDirMenu")
                                    if (treeNode.targetDir) {
                                        //target目录
                                        $("#deleteDirLi").hide();
                                    } else {
                                        //target子目录
                                        $("#deleteDirLi").show();
                                    }
                                } else {
                                    $("#rightClickMenu li.remove").show();
                                    menu = $("#rightClickMenu")
                                }
                                $("#rightClickGitMenu").attr("data-type", "dir");
                                showRightDirMenu(event, menu);
                            } else {
                                var nodes = treeNode.getPath();
                                var isTargetFile = false;
                                if (nodes != null && nodes.length > 0 && nodes[0].targetDir) {
                                    isTargetFile = true;
                                }
                                $("#rightClickGitMenu").attr("data-type", "file");
                                showRightScriptMenu(event, isTargetFile);
                            }
                            return false;
                        },
                        onDblClick: function (event, treeId, treeNode) {
                            if (treeNode && !treeNode.isParent && treeNode.path) {
                                openScript(gitProjectId, treeNode.path, treeNode.oriName);
                            }
                            return false;
                        },
                        onExpand: function (event, treeId, treeNode) {
                            if (canLocation()) {
                                var selectNode = zTree.getNodeByParam("path", locationScriptObj.gitProjectFilePath, treeNode);
                                if (selectNode) {
                                    var selectNodeDom = $("#" + selectNode.tId + " .nodeDiv");
                                    selectNodeDom.click();
                                    if (selectNodeDom.length > 0) {
                                        var nodeTop = selectNodeDom.offset().top;
                                        var divTop = $("#scriptManageTab .script-content").offset().top;
                                        var treeHeigth = $("#tree").height();
                                        $("#tree").scrollTop($("#tree").scrollTop() + nodeTop - divTop - 0.5 * treeHeigth);
                                    }
                                    resetLocation();
                                }
                            }
                        },
                    },
                };
                var openDirs = "";
                if (isKeepOpen) {
                    var needOpenNodes = zTree.getNodesByParam("open", true);
                    for (var index = 0; index < needOpenNodes.length; index++) {
                        openDirs += needOpenNodes[index].path + ",";
                    }
                }
                if (gitProjectId) {
                    $("#appSelect").attr("disabled", "disabled")
                    if (zTree) {
                        zTree.destroy();
                    }
                    if (!isRefresh) {
                        isRefresh = true;
                        commonAjaxEvents.commonPostAjax(getDirsUrl, {
                            gitProjectId: gitProjectId,
                            selectFilePath: (locationScriptObj && locationScriptObj.gitProjectFilePath) || "",
                            openDirs: openDirs,
                            targetRange: 1
                        }, $("#appSelect"), function (node, data) {
                            if (data && data.obj) {
                                var zNodeArr = data.obj;
                                var zNodes = [];
                                for (var index = 0; index < zNodeArr.length; index++) {
                                    var zNode = zNodeArr[index];
                                    var insertNode = getZtreeNode(zNode);
                                    zNodes.push(insertNode);
                                }
                                var t = $("#tree");
                                $.fn.zTree.init(t, setting, zNodes);
                                zTree = $.fn.zTree.getZTreeObj("tree");
                                oldnodes = [];
                                if (locationScriptObj && locationScriptObj.gitProjectId && locationScriptObj.gitProjectFilePath) {
                                    var locationNode = zTree.getNodeByParam("path", locationScriptObj.gitProjectFilePath);
                                    locationScriptPositon(locationNode);
                                }
                            }
                        }, function () {
                            $("#appSelect").removeAttr("disabled");
                            isRefresh = false;
                            if (lazyLocation.length > 0) {
                                var obj = lazyLocation.pop();
                                if (obj.gitProjectId && obj.gitProjectFilePath) {
                                    locationScript(obj.gitProjectId, obj.gitProjectFilePath)
                                }
                            }
                        });
                    }
                }
            }
        }

        window.initZtree = initZtree;
        window.pullFile = pullFile;
        window.pushFile = pushFile;
        window.shareScript = shareScript;
        window.showGitHistory = showGitHistory;
        function initMenu() {
            $("#rightMenuDiv").appendTo($("body"))
        }

        function initEvent() {
            //左侧触发创建git
            $("#createLeftGitBtn").click(function () {
                var projectArt = $.dialog.open("/scriptcenter/project/newGitProject.html", {
                    title: "新项目",
                    lock: true,
                    width: "600",
                    height: "400",
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                });
                $.dialog.data("projectArt", projectArt);
            });

            $("#getLeftAvailGit").click(function () {
                var useProjectArt = $.dialog.open("/scriptcenter/project/useGitProject.html", {
                    title: "使用现有git项目",
                    lock: true,
                    width: "600",
                    height: "300",
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                });
                $.dialog.data("useProjectArt", useProjectArt);
            });

            $("#createLeftCodingBtn").click(function () {
                var projectArt = $.dialog.open("/scriptcenter/project/newCodingProject.html", {
                    title: "新项目",
                    lock: true,
                    width: "600",
                    height: "400",
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                });
                $.dialog.data("projectArt", projectArt);
            });

            $("#getLeftAvailCoding").click(function () {
                var useProjectArt = $.dialog.open("/scriptcenter/project/useCodingProject.html", {
                    title: "使用现有coding项目",
                    lock: true,
                    width: "600",
                    height: "300",
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                });
                $.dialog.data("useProjectArt", useProjectArt);
            });

            $("#gitMenuBtn").click(function (event) {
                if ($("#gitMenuDiv").is(':visible')) {
                    $("#gitMenuDiv").hide();
                } else {
                    var offSet = $("#gitMenuBtn").offset();
                    var menuTop = offSet.top + $("#gitMenuBtn").height() + 2;
                    var menuLeft = offSet.left - $("#gitMenuDiv").width() + 16;
                    hiddenAllRightMeun();
                    var id = $("#appSelect").val();
                    if(id > 900000000){
                        $("#gitOrCoding").html("跳转至coding");
                        // $("#gitOrCodingSelect").html("Coding")
                    }else{
                        $("#gitOrCoding").html("跳转至git");
                        // $("#gitOrCodingSelect").html("Git")
                    }
                    $("#gitMenuDiv").css({top: menuTop, left: menuLeft}).show();
                    event.stopPropagation();
                    event.preventDefault();
                }

            })
            // 右键脚本git
            $("#rightClickGitDiv").mouseenter(function () {
                var newScriptMenu = $("#rightClickGitMenu");
                newScriptMenu.attr("data-type", "file");
                var top = $(this).offset().top;
                var left = $(this).offset().left;
                left = left + $("#rightClickScriptMenu").width();

                newScriptMenu.css({top: top, left: left}).show();
            })
            $("#rightClickGitDiv").mouseleave(function () {
                var newMenu = $("#rightClickGitMenu");
                newMenu.hide();
            })
            $("#rightClickGitMenu").mouseenter(function () {
                var newScriptMenu = $("#rightClickGitMenu");
                newScriptMenu.show();
            })
            $("#rightClickGitMenu").mouseleave(function () {
                var newScriptMenu = $("#rightClickGitMenu");
                newScriptMenu.hide();
            })
            //右键目录git
            $("#rightClickDirGitDiv").mouseenter(function () {
                var newScriptMenu = $("#rightClickGitMenu");
                var top = $(this).offset().top;
                var left = $(this).offset().left;
                left = left + $("#rightClickDirGitDiv").width();
                var menuHeight = newScriptMenu.height();
                var windowHeight = window.innerHeight || ((document.body) && (document.body.clientHeight));
                if(top + menuHeight +10 > windowHeight){
                    top = windowHeight - 10 - menuHeight;
                    top = top<0?0:top;
                }
                newScriptMenu.css({top: top, left: left}).show();
            })
            $("#rightClickDirGitDiv").mouseleave(function () {
                var newMenu = $("#rightClickGitMenu");
                newMenu.hide();
            })
            //项目选项中的git
            $("#gitMenuScriptGit").mouseenter(function () {
                var newScriptMenu = $("#rightClickGitMenu");
                newScriptMenu.attr("data-type", "all");
                var top = $(this).offset().top;
                var left = $(this).offset().left;
                left = left + $("#gitMenuDiv").width();

                newScriptMenu.css({top: top, left: left}).show();
            })
            $("#gitMenuScriptGit").mouseleave(function () {
                var newMenu = $("#rightClickGitMenu");
                newMenu.hide();
            })
            //右键目录
            $("#newScriptLi").mouseenter(function () {
                var newScriptMenu = $("#mouseScriptDiv");
                var top = $(this).offset().top;
                var left = $(this).offset().left;
                left = left + $("#rightClickMenu").width();
                var menuHeight = newScriptMenu.height();
                var windowHeight = window.innerHeight || ((document.body) && (document.body.clientHeight));
                if(top + menuHeight +10 > windowHeight){
                    top = windowHeight - 10 - menuHeight;
                    top = top<0?0:top;
                }

                newScriptMenu.css({top: top, left: left}).show();
            })
            $("#newScriptLi").mouseleave(function () {
                var newScriptMenu = $("#mouseScriptDiv");
                newScriptMenu.hide();
            })
            //右键新建脚本模板
            $("#mouseScriptDiv").mouseenter(function () {
                var newScriptMenu = $("#mouseScriptDiv");
                newScriptMenu.show();
            })
            $("#mouseScriptDiv").mouseleave(function () {
                var newScriptMenu = $("#mouseScriptDiv");
                newScriptMenu.hide();
            })
            // 加入模板之后的新建脚本
            $("#mouseScriptDiv").on("click", "li span.blankScript", function () {

                var dataIndex = $(this).parent().attr("data-index");
                var pythonType = $(this).parent().attr("data-python");
                var script = getScriptObj(dataIndex, pythonType);
                var applicationId = getSelectedProjectId();
                $("#mouseScriptDiv").hide();
                addScript(script, rightClickNode, applicationId, script.default);
            })
            $("#mouseScriptDiv").on("click", "li span.templateScript", function () {
                templateModal($(this), rightClickNode);
            });
            $("#mouseScriptDiv").on("click", "li.scriptLi", function (event) {
                event.stopPropagation();
                event.preventDefault();
            })
            $("#gitMenuDiv").on("click", "li", function () {
                if ($(this).hasClass("gitProjectDetail")) {
                    var gitProjectId = getSelectedProjectId();
                    $.dialog.open("/scriptcenter/project/projectDetail.html?gitProjectId=" + gitProjectId, {
                        title: "项目详情",
                        lock: true,
                        width: "600",
                        height: "500",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    });
                } else if ($(this).hasClass("uploadFileHistory")) {
                    var gitProjectId = getSelectedProjectId();
                    $.dialog.open("/scriptcenter/project/uploadFileHistory.html?gitProjectId=" + gitProjectId, {
                        title: "文件上传历史",
                        lock: true,
                        width: "600",
                        height: "500",
                        capacity: 0.5,
                        esc: false,
                        close: function () {

                        }
                    });

                } else if ($(this).hasClass("gitProjectOperateDetail")) {

                } else if ($(this).hasClass("toGit")) {
                    var id = $("#appSelect").val();
                    if(id > 900000000){
                        var url = "https://coding.jd.com/";
                    }else{
                        var url = "http://git.jd.com/";
                    }
                    var path = getSelectedProjectPath();

                    url += (path ? (path + "/tree/bdp_ide_branch") : "");
                    window.open(url);
                }

            })
            //新建脚本 end

            $("#transferScript").click(function () {
                var projectId = getSelectedProjectId();
                var gitProjectDirPath = rightClickNode ? (rightClickNode.isParent ? rightClickNode.path : rightClickNode.parentPath) : "";
                gitProjectDirPath = encodeURIComponent(encodeURIComponent(gitProjectDirPath));
                var tranferArt = $.dialog.open("/scriptcenter/project/tranferScript.html?projectId=" + projectId + "&gitProjectDirPath=" + gitProjectDirPath, {
                    title: "迁移脚本",
                    lock: true,
                    width: "600",
                    height: "400",
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                });
                $.dialog.data("tranferArt", tranferArt);
                $.dialog.data("refreshFileInfoStatus", refreshFileInfoStatus);
            })
            //重命名目录
            // $("#renameDir").click(function () {
            //     renameDir();
            // })

            $("#appSelect").on("change", function (event, noZtree) {
                $('.showTitleBox').remove();
                HOME_COOKIE.setActiveGitCookie($("#loginErp").val(), getSelectedProjectId());
                if (!noZtree) {
                    initZtree();
                }
            })
            $("#appSelect").on("select2-opening", function (event) {
                hiddenAllRightMeun();
            })
            //  右键目录菜单
            $("#rightClickMenu").on("click", "li", function () {
                var liClass = $(this).attr("class");
                switch (liClass) {
                    case "new-dir-li":
                        newDir(rightClickNode);
                        break;
                    case "upload-script":
                        upLoadFile(rightClickNode)
                        break;
                    case "remove":
                        deleteDir(rightClickNode);
                        break;
                    default:
                        break;
                }
            })
            //右键target及其子目录菜单
            $("#rightClickTargetDirMenu").on("click", "li", function () {
                var liClass = $(this).attr("class");
                switch (liClass) {
                    case "new-dir-li":
                        newDir(rightClickNode);
                        break;
                    case "remove":
                        deleteDir(rightClickNode);
                        break;
                    default:
                        break;
                }
            })
            //    下面是右键脚本的菜单事件
            $("#rightClickScriptMenu").on("click", "li", function (event) {
                var liClass = $(this).attr("class").split(" ")[0];
                switch (liClass) {
                    case "script_open":
                        openScript(rightClickNode.gitProjectId, rightClickNode.path);
                        break;
                    case "script_remove":
                        removeScript();
                        break;
                    case "script_move":
                        moveScript();
                        break;
                    case "script_copy":
                        copy();
                        break;
                    case "script_rename":
                        renameScript(rightClickNode.gitProjectId, rightClickNode.path);
                        break;
                    case "script_download":
                        downloadScript(rightClickNode.path, rightClickNode.gitProjectId);
                        break;
                    case "script_info":
                        showScriptInfo();
                        break;
                    case "script_url":
                        shareScript(rightClickNode.gitProjectId, rightClickNode.path)
                        break;
                    default:
                        break;
                }
            })
            $("#rightClickGitMenu").on("click", "li", function () {
                $("#rightClickGitMenu").hide();
                var type = $("#rightClickGitMenu").attr("data-type");
                var liClass = $(this).attr("class");
                switch (liClass) {
                    case "pull":
                        if (type == "file" && rightClickNode && !rightClickNode.isParent) {
                            pullFile(rightClickNode.gitProjectId, rightClickNode.path);
                        } else if (type == "dir") {
                            pullDir()
                        } else if (type == "all") {
                            pullDir(getSelectedProjectId(), "");
                        }
                        break;
                    case "push":
                        if (type == "file" && rightClickNode && !rightClickNode.isParent) {
                            pushFile(rightClickNode.gitProjectId, rightClickNode.path);
                        } else if (type == "dir") {
                            pushDir();
                        } else if (type == "all") {
                            pushDir(getSelectedProjectId(), "");
                        }
                        break;
                    case "showGitHistory":
                        if (type == "file" && rightClickNode && !rightClickNode.isParent) {
                            showGitHistory(rightClickNode.gitProjectId, rightClickNode.path,false);
                        } else if (type == "dir") {
                            var gitProjectId = undefined ;
                            var gitProjectDirPath = undefined;
                            gitProjectId = gitProjectId || (rightClickNode ? rightClickNode.gitProjectId : getSelectedProjectId());
                            gitProjectDirPath = gitProjectDirPath != undefined ? gitProjectDirPath : (rightClickNode ? (rightClickNode.isParent ? rightClickNode.path : rightClickNode.parentPath) : "");
                            showGitHistory(gitProjectId,gitProjectDirPath,true);
                        } else if (type == "all") {
                            showGitHistory(getSelectedProjectId(), "",true);
                        }
                        break;
                    default:
                        break;
                }
            })
            //git mulu

            $("#scriptManageTool").on("click", "span.bdp-icon", function () {
                if ($(this).hasClass("search")) {
                    showSearchScript();
                } else if ($(this).hasClass("addDir")) {
                    var selectedLi = $("#tree li.selectedNode");
                    if (selectedLi && selectedLi.length == 1) {
                        var tId = selectedLi.attr("id");
                        rightClickNode = zTree.getNodeByTId(tId);
                    } else {
                        rightClickNode = null;
                    }
                    newDir(rightClickNode);
                } else if ($(this).hasClass("refresh")) {
                    initZtree(true);
                } else if ($(this).hasClass("upLoad")) {
                    var selectedLi = $("#tree li.selectedNode");
                    if (selectedLi && selectedLi.length == 1) {
                        var tId = selectedLi.attr("id");
                        rightClickNode = zTree.getNodeByTId(tId);
                    } else {
                        rightClickNode = null;
                    }
                    upLoadFile(rightClickNode);
                } else if ($(this).hasClass("newGit")) {
                    $(".bdp-new-help-modal-backdrop").click();
                    var tipsArt = $.dialog.open("/scriptcenter/project/newProjectTips.html", {
                        title: "选择项目类型",
                        lock: true,
                        width: "600",
                        height: "150",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    });
                    $.dialog.data("projectTipsArts", tipsArt);
                }
            })
        }


        function shareScript(gitProjectId, gitProjectFilePath) {
            var html = "<div style='line-height: 25px;padding: 20px 20px 10px'>复制脚本URL到剪贴板，可让其他项目成员快速定位到该脚本。</div>";
            $.bdpMsg({
                title: "提示",
                mainContent: html,
                width: "350px",
                buttons: [
                    {
                        text: "复制脚本URL",
                        event: function () {
                            gitProjectFilePath = encodeURIComponent(encodeURIComponent(gitProjectFilePath));
                            var url = _bdpDomain + "/scriptcenter/index.html?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath;
                            copyContent(url);
                            $.successMsg("脚本URL已复制到剪贴板")
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
            });
        }

        function showRightDirMenu(event, menu) {
            var windowHeight = window.innerHeight || ((document.body) && (document.body.clientHeight));
            var menuTop = event.clientY + 5;
            var menuLeft = event.clientX + 5;
            var menuHeight = $(menu).height();
            if (menuHeight + menuTop + 10 > windowHeight) {
                menuTop = windowHeight - 10 - menuHeight
                menuTop = menuTop < 0 ? 0 : menuTop;
            }
            hiddenAllRightMeun();
            $(menu).css({top: menuTop, left: menuLeft}).show();
        }

        function newDir(rightClickNode) {
            var gitProjectId = rightClickNode ? rightClickNode.gitProjectId : getSelectedProjectId();
            var gitProjectDirPath = rightClickNode ? (rightClickNode.isParent ? rightClickNode.path : rightClickNode.parentPath) : "";
            $("#saveModal").FileMode("dir", {
                gitProjectId: gitProjectId,
                gitProjectDirPath: gitProjectDirPath,
                targetRange: 1
            });
        }

        function showRightScriptMenu(event, isTargetFile) {
            if (isTargetFile) {
                $("li.script_git", $("#rightClickScriptMenu")).hide();
                $("li.script_copy", $("#rightClickScriptMenu")).hide();
            } else {
                $("li.script_git", $("#rightClickScriptMenu")).show();
                $("li.script_copy", $("#rightClickScriptMenu")).show();
            }
            var windowHeight = window.innerHeight || ((document.body) && (document.body.clientHeight));
            var menuTop = event.clientY + 5;
            var menuLeft = event.clientX + 5;
            var menuHeight = $("#rightClickScriptMenu").height();
            if (menuHeight + menuTop + 10 > windowHeight) {
                menuTop = windowHeight - 10 - menuHeight
                menuTop = menuTop < 0 ? 0 : menuTop;
            }
            hiddenAllRightMeun();
            $("#rightClickScriptMenu").css({top: menuTop, left: menuLeft}).show();
        }

        function removeScript() {
            var gitProjectFilePath = rightClickNode.path;
            var gitProjectId = getSelectedProjectId();
            commonAjaxEvents.commonPostAjax("/scriptcenter/buffalo/checkUpline.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath
            }, null, function (node, data) {
                var content = "";
                if (data.obj == 0) {
                    content = "是否确定删除脚本？";
                } else {
                    content = "是否确定删除脚本（任务调度中的脚本将同步删除）？";
                }
                $.confirmMsg({
                    content: content,
                    buttons: [
                        {
                            text: "确定",
                            event: function () {
                                $.removeMsg();
                                removeScriptDirect(gitProjectId, gitProjectFilePath);
                            }
                        },
                        {
                            text: "取消"
                        }
                    ]
                })
            })
        }

        function removeScriptDirect(gitProjectId, gitProjectFilePath) {
            var key = getKey(gitProjectId, gitProjectFilePath);
            commonAjaxEvents.commonPostAjax(deleteScriptUrl, {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath,
            }, null, function (node, data) {
                if (data && data.code == 0) {
                    var node = zTree.getNodeByParam("path", gitProjectFilePath);
                    if (node) {
                        zTree.removeNode(node);
                    }
                    $("#codeEditContainer").JdDataDevTab("removeScript", key);
                    $.successMsg("删除脚本成功");
                } else {
                    $.errorMsg("删除出现异常");
                }
            })

        }

        function moveScript() {
            if (rightClickNode && !rightClickNode.isParent) {
                var parents = rightClickNode.getPath();
                var isTargetFile = parents != null && parents.length > 0 && parents[0].targetDir;
                var gitProjectFilePath = rightClickNode.path;
                var gitProjectId = rightClickNode.gitProjectId;
                commonAjaxEvents.commonPostAjax(getScriptBaseUrl, {
                    gitProjectId: gitProjectId,
                    gitProjectFilePath: gitProjectFilePath
                }, null, function (node, data) {
                    if (data && data.obj) {
                        var file = data.obj;
                        file.targetRange = isTargetFile ? 2 : 0;
                        $("#saveModal").FileMode("move", file);
                    }
                })
            }

        }

        function copy() {
            var gitProjectFilePath = rightClickNode.path;
            var gitProjectId = rightClickNode.gitProjectId;
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/copy.ajax", {
                gitProjectFilePath: gitProjectFilePath,
                gitProjectId: gitProjectId
            }, null, function (node, data) {
                if (data && data.obj) {
                    var zNode = data.obj;
                    while (zNode.children && zNode.children.length > 0) {ƒ
                        zNode = zNode.children[0];
                    }
                    var parNode = zTree.getNodeByParam("path", zNode.parentPath);
                    if (!zNode.parentPath || (parNode && parNode.zAsync)) {
                        var newNodes = [getZtreeNode(zNode)];
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.parentPath == zNode.parentPath && node.isParent == true;
                        });
                        var index = nodes && nodes.length || 0;
                        zTree.addNodes(parNode, index, newNodes);
                    }
                }
            })
        }


        function pullFile(gitProjectId, gitProjectFilePath) {
            var key = getKey(gitProjectId, gitProjectFilePath);
            var scriptWindow = datadevInit.getActiveWindow(key);

            if (scriptWindow && scriptWindow.win && scriptWindow.win.isFinishContent()) {
                datadevInit.directSave(function () {
                    pullFileDirect(gitProjectId, gitProjectFilePath)
                }, null, key);
            } else {
                pullFileDirect(gitProjectId, gitProjectFilePath);
            }
        }

        function pullFileDirect(gitProjectId, gitProjectFilePath) {
            commonAjaxEvents.commonPostAjax(pullFileUrl, {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath
            }, null, function (node, data) {
                if (data.obj && data.obj.isMerge) {
                    var key = getKey(gitProjectId, gitProjectFilePath);
                    scriptMergeContentMap.put(key, {localVersion: data.obj.version, remoteVersion: data.obj.newGitVersion});
                    $.dialog.data("leftTips", "数据开发平台  当前操作人：" + data.obj.nowPullErp);
                    $.dialog.data("rightTips", "git   commitId：" + data.obj.newGitVersion);
                    diff(gitProjectId, gitProjectFilePath, function (key, ajaxData) {
                        datadevInit.directSave(function () {
                            $.successMsg(data.message);
                        }, ajaxData, key);
                    });
                } else {
                    $.successMsg("pull成功");
                }
            }, undefined, undefined, undefined, undefined, function () {
                $.loadingMsg("正在Pull...")
            });
        }

        function pushFile(gitProjectId, gitProjectFilePath) {
            $.loadingMsg("正在Push...");
            var key = getKey(gitProjectId, gitProjectFilePath);
            var scriptWindow = datadevInit.getActiveWindow(key);
            if (scriptWindow && scriptWindow.win && scriptWindow.win.isFinishContent()) {
                datadevInit.directSave(function (data) {
                    pushFileDirect(gitProjectId, gitProjectFilePath);
                }, null, key);
            } else {
                pushFileDirect(gitProjectId, gitProjectFilePath);
            }
        }

        /**
         * 查看 文件的提交历史
         * @param gitProjectId
         * @param gitProjectFilePath
         */
        function showGitHistory(gitProjectId, gitProjectFilePath,isDir) {
            console.log("gitProjectId :  " + gitProjectId);
            console.log("gitProjectFilePath :  " + gitProjectFilePath);
            console.log("isDir :  " + isDir);


            var url = "/scriptcenter/script/gitHis.html?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath + "&isDir="+ isDir;
            $.dialog.open(url, {
                title: "提交历史",
                lock: true,
                width: "900px",
                height: "500px",
                opacity: 0.5,
                resize:false,
                esc: false,
                close: function () {
                }
            });
        }

        /**
         * gitStatus 先从左边treeNode获取，如果因为不是当前项目不存在treeNode，则从脚本gitStatus属性里面回去
         * @param gitProjectId
         * @param gitProjectFilePath
         * @param scriptWindow
         */
        function pushFileDirect(gitProjectId, gitProjectFilePath) {
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getPushNum.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath
            }, null, function (node, data) {
                var gitMerge = data.obj.gitMerge;
                var pushNum = data.obj.pushNum;
                var mergeList = data.obj.mergeList;
                if (gitMerge == true && mergeList.length > 0) {
                    var key = getKey(gitProjectId, gitProjectFilePath);
                    var script = mergeList[0];
                    scriptMergeContentMap.put(key, {localVersion: script.version, remoteVersion: script.newGitVersion});
                    $.dialog.data("leftTips", "数据开发平台  当前操作人：" + data.obj.erp);
                    $.dialog.data("rightTips", "git   commitId：" + script.newGitVersion);
                    diff(gitProjectId, gitProjectFilePath, function (key, data) {
                        datadevInit.directSave(function (data) {
                            showPushInfo(pushNum, function () {
                                var gitCommitMessageNode = $("#gitCommitMessage");
                                var gitCommitMessage = gitCommitMessageNode.length > 0 && gitCommitMessageNode.val() || "";
                                datadevInit.mergeAndPushScript(gitProjectId, gitProjectFilePath, gitCommitMessage, function (data) {
                                    $.successMsg("Push成功");
                                })
                            }, false);
                        }, data, key);
                    }, getKey(gitProjectId, gitProjectFilePath));
                } else {
                    showPushInfo(pushNum, function () {
                        var gitCommitMessageNode = $("#gitCommitMessage");
                        var gitCommitMessage = gitCommitMessageNode.length > 0 && gitCommitMessageNode.val() || "";
                        datadevInit.mergeAndPushScript(gitProjectId, gitProjectFilePath, gitCommitMessage, function (data) {
                            $.successMsg("Push成功");
                        })
                    }, false);
                }
            })
        }

        function showScriptInfo() {
            var path = rightClickNode.path;
            var gitProjectId = getSelectedProjectId();
            var key = getKey(gitProjectId, path);
            var tableInfo = $("#codeEditContainer").JdDataDevTab("getTabInfo", key);
            if (tableInfo && tableInfo.content_div) {
                openScript(gitProjectId, path, rightClickNode.oriName);
                var active = datadevInit.getActiveWindow();
                active.win.showInfo && active.win.showInfo();
            } else {
                scriptInitStatusMap.put(key, function () {
                    var realWindow = datadevInit.getActiveWindow(key);
                    realWindow.win && realWindow.win.showInfo();
                });
                openScript(gitProjectId, path, rightClickNode.oriName);
            }
        }


        function upLoadFile(rightClickNode) {
            var info = {};
            info.gitProjectDirPath = rightClickNode ? (rightClickNode.isParent ? rightClickNode.path : rightClickNode.parentPath) : "";
            info.gitProjectId = rightClickNode ? rightClickNode.gitProjectId : getSelectedProjectId();
            // $("#saveModal").FileMode("upFile", info);

            var mutilFileWindow = $.dialog.open("/scriptcenter/script/saveMutilFile.html?gitProjectId=" + info.gitProjectId + "&gitProjectDirPath=" + info.gitProjectDirPath, {
                title: "上传脚本",
                lock: true,
                width: "800",
                height: "400",
                opacity: 0.5,
                esc: false,
                cancel: false
            });
            $.dialog.data("mutilFileWindow", mutilFileWindow);

        }

        function deleteDir(rightClickNode) {
            var gitProjectId = rightClickNode.gitProjectId;
            var gitProjectDirPath = rightClickNode.path;
            commonAjaxEvents.commonPostAjax(deleteDirUrl, {
                gitProjectId: gitProjectId,
                gitProjectDirPath: gitProjectDirPath
            }, $(this), function (node, data) {
                var node = zTree.getNodeByParam("path", gitProjectDirPath);
                zTree.removeNode(node)
                $.successMsg(data.message)
            })
        }

        function pullDir(gitProjectId, gitProjectDirPath) {
            gitProjectId = gitProjectId || (rightClickNode ? rightClickNode.gitProjectId : getSelectedProjectId());
            gitProjectDirPath = gitProjectDirPath != undefined ? gitProjectDirPath : (rightClickNode ? (rightClickNode.isParent ? rightClickNode.path : rightClickNode.parentPath) : "");
            commonAjaxEvents.commonPostAjax(pullDirUrl, {
                gitProjectId: gitProjectId,
                gitProjectDirPath: gitProjectDirPath
            }, null, function (node, data) {
                console.log(data)
                var addNodes = data.obj.b;
                addNodesFromServer(addNodes);
                if (data.obj.a && data.obj.a.length > 0) {
                    $.dialog.data("mergeList", data.obj.a);
                    var dialogId = $.dialog.open("/scriptcenter/script/mergeList.html", {
                        title: "合并脚本",
                        lock: true,
                        width: "600",
                        height: "500",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    });
                    $.dialog.data("dialogObj", dialogId);
                } else {
                    $.successMsg("Pull成功");
                }
            }, undefined, undefined, undefined, undefined, function () {
                $.loadingMsg("正在Pull...")
            })
        }

        function pushDir(gitProjectId, gitProjectDirPath) {
            gitProjectId = gitProjectId || (rightClickNode ? rightClickNode.gitProjectId : getSelectedProjectId());
            gitProjectDirPath = gitProjectDirPath != undefined ? gitProjectDirPath : (rightClickNode ? (rightClickNode.isParent ? rightClickNode.path : rightClickNode.parentPath) : "");
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getPushNum.ajax", {
                gitProjectId: gitProjectId,
                gitProjectDirPath: gitProjectDirPath
            }, null, function (node, data) {
                var gitMerge = data.obj.gitMerge;
                var pushNum = data.obj.pushNum;
                var mergeList = data.obj.mergeList;
                if (gitMerge == true) {
                    $.dialog.data("mergeList", mergeList);
                    var dialogId = $.dialog.open("/scriptcenter/script/mergeList.html", {
                        title: "合并脚本",
                        lock: true,
                        width: "600",
                        height: "500",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    });
                    $.dialog.data("dialogObj", dialogId);
                    $.dialog.data("afterMergeAll", function () {
                        commonAjaxEvents.commonPostAjax("/scriptcenter/script/getPushNum.ajax", {
                            gitProjectId: gitProjectId,
                            gitProjectDirPath: gitProjectDirPath
                        }, null, function (node, data) {
                            var pushNum = data.obj.pushNum;
                            showPushInfo(pushNum, function () {
                                var gitCommitMessageNode = $("#gitCommitMessage");
                                var gitCommitMessage = gitCommitMessageNode.length > 0 && gitCommitMessageNode.val() || "";
                                mergeAndPushDir(gitProjectId, gitProjectDirPath, gitCommitMessage);
                            }, true);
                        })
                    });
                } else {
                    showPushInfo(pushNum, function () {
                        var gitCommitMessageNode = $("#gitCommitMessage");
                        var gitCommitMessage = gitCommitMessageNode.length > 0 && gitCommitMessageNode.val() || "";
                        mergeAndPushDir(gitProjectId, gitProjectDirPath, gitCommitMessage);
                    }, true);
                }
            })

        }

        function mergeAndPushDir(gitProjectId, gitProjectDirPath, gitCommitMessage) {
            commonAjaxEvents.commonPostAjax(pushDirUrl, {
                gitProjectId: gitProjectId,
                gitProjectDirPath: gitProjectDirPath,
                commitMessage: gitCommitMessage
            }, null, function (node, data) {
                if (data.obj.isMerge == true) {
                    var mergeList = data.obj.mergeList;
                    $.dialog.data("mergeList", mergeList);
                    var dialogId = $.dialog.open("/scriptcenter/script/mergeList.html", {
                        title: "合并脚本",
                        lock: true,
                        width: "600",
                        height: "500",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    });
                    $.dialog.data("dialogObj", dialogId);
                    $.dialog.data("afterMergeAll", function () {
                        mergeAndPushDir(gitProjectId, gitProjectDirPath, gitCommitMessage);
                    });
                } else {
                    $.successMsg("push成功")
                    changeDirGitStatus(gitProjectId, gitProjectDirPath);
                }
            }, undefined, undefined, undefined, undefined, function () {
                $.loadingMsg("正在Push...")
            })
        }

        function addNodesFromServer(addNodes) {
            var addMap = new Map();
            addNodes.forEach(function (addNode) {
                while (addNode != null) {
                    var zNode = zTree.getNodeByParam("path", addNode.path);
                    if (zNode == null) {
                        if (!addMap.containsKey(addNode.path)) {
                            addMap.put(addNode.path, addNode);
                        }
                        break;
                    } else {
                        addNode = addNode.children ? addNode.children[0] : null;
                    }
                }
            })
            var values = addMap.values();
            var expandNode = new Map();
            values.forEach(function (value) {
                var parNode = zTree.getNodeByParam("path", value.parentPath);
                if (!value.parentPath || (parNode && parNode.zAsync)) {
                    zTree.addNodes(parNode, value.parChl == 0 ? 0 : -1, [getZtreeNode(value)])
                } else if (parNode && !parNode.zAsync && !expandNode.containsKey(parNode.path)) {
                    zTree.expandNode(parNode, true);
                    expandNode.put(parNode.path, parNode);
                }
            })
        }
    }
)

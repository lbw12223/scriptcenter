// save :保存文件  saveAs：另存为  upFile：上传文件  move：移动文件   dir：创建目录
var uploadRequest = undefined;

(function ($) {

    var rootPath = "root";
    var preSelectedDirId = undefined;
    var suffixAttr = {"sql": "1", "sh": "2", "py": "3", "zip": "4", "": 5};
    var getChiDirUrl = "/scriptcenter/script/getScripsByDirId.ajax";
    var SAVE_MODE = "save", SAVE_AS_MODE = "saveAs", MOVE_MODE = "move", DIR_MODE = "dir",
        SCRIPT_RENAME_MODE = "scriptRename", DIR_RENAME_MODE = "dirRename";
    var key = undefined;
    var jq = undefined;
    var info = undefined;
    var callBack = undefined;
    var gitProjectId = undefined;
    var scriptPath = undefined;
    var targetRange = 0;
    var currentWindow = undefined ;
    // var uploadRequest=undefined;
    var setting = {
        view: {
            dblClickExpand: false,
            showLine: false,
            selectedMulti: false,
            initPadding: 25,
            initPaddingLeft: 10
        },
        async: {
            enable: true,
            url: getChiDirUrl,
            autoParam: ["path"],
            otherParam: ["gitProjectId", gitProjectId, "range", 1, "targetRange", targetRange],
            dataFilter: function (treeId, parentNode, responseData) {
                if (responseData && responseData.obj && responseData.obj.length > 0) {
                    var array = responseData.obj;
                    for (var i = 0; i < responseData.obj.length; i++) {
                        if (array[i].parChl == 0) {
                            array[i].isParent = true;
                            var script = undefined;
                            if (array[i].runType * 1 == 1) {
                                script = getScriptObj(-2);
                            } else {
                                script = getScriptObj(-1);
                            }

                            array[i].iconSkin = script.icon;
                            array[i].rootPath = rootPath + "/" + array[i].path;
                            array[i].rootParentPath = rootPath + "/" + array[i].parentPath;
                        }
                    }
                    return array;
                } else {
                    return [];
                }
            }
        },
        data: {
            simpleData: {
                enable: true,
                idKey: "rootPath",
                pIdKey: "rootParentPath",
                rootPId: ""
            }
        },
        callback: {
            beforeClick: function (treeId, treeNode) {
                var zTree = $.fn.zTree.getZTreeObj("fileTree");
                if (treeNode.isParent) {
                    zTree.expandNode(treeNode);
                }
                var selectedclass = "selectedNodeDir";
                var tId = treeNode.tId;
                if (preSelectedDirId) {
                    $("#" + preSelectedDirId).removeClass(selectedclass)
                }
                $("#" + tId).addClass(selectedclass);
                preSelectedDirId = tId;
                $("#savePath").val(treeNode.rootPath).trigger("keyup");
                $("input.dirId", $("#savePathDiv")).val(treeNode.path);
                return true;
            }
        }
    };
    var modeAttr = {
        save: {
            title: "保存文件",
            url: "/scriptcenter/script/save.ajax",
            saveType: 0,
            fileNameDiv: {
                title: "文件名称：",
                display: "show",
                disabled: "",
                readOnly: "readOnly",
                placeholder: "请输入名称,支持字母,数字,下划线,中划线"
            },
            upLoadFileDiv: {display: "hide"},
            fileDesDiv: {display: "show", disabled: ""},
            startShellDiv: {display: "hide"},
            saveFileSelectDiv: {display: "show", disabled: "disabled"},
            savePathDiv: {display: "show"}

        },
        saveAs: {
            title: "文件另存为",
            url: "/scriptcenter/script/saveAs.ajax",
            saveType: 1,
            fileNameDiv: {title: "文件名称：", display: "show", disabled: "", placeholder: "请输入名称,支持字母,数字,下划线,中划线"},
            startShellDiv: {display: "hide"},
            upLoadFileDiv: {display: "hide"},
            fileDesDiv: {display: "show", disabled: ""},
            saveFileSelectDiv: {display: "show", disabled: "disabled"},
            savePathDiv: {display: "show"}
        },
        move: {
            title: "移动脚本",
            url: "/scriptcenter/script/move.ajax",
            saveType: 0,
            fileNameDiv: {title: "文件名称：", display: "show", disabled: "disabled", placeholder: "请输入名称,支持字母,数字,下划线,中划线"},
            upLoadFileDiv: {display: "hide"},
            fileDesDiv: {display: "show", disabled: "disabled"},
            startShellDiv: {display: "hide"},
            saveFileSelectDiv: {display: "show", disabled: "disabled"},
            savePathDiv: {display: "show"},
        },
        dir: {
            url: "/scriptcenter/script/addDir.ajax",
            title: "创建目录",
            fileNameDiv: {title: "目录名称：", display: "show", disabled: "", placeholder: "请输入名称,支持中文,字母,数字,下划线,中划线,正斜杠"},
            upLoadFileDiv: {display: "hide"},
            startShellDiv: {display: "hide"},
            fileDesDiv: {display: "hide", disabled: ""},
            saveFileSelectDiv: {display: "hide"},
            savePathDiv: {display: "show"}
        },
        scriptRename: {
            title: "修改文件名",
            url: "/scriptcenter/script/scriptRename.ajax",
            saveType: 0,
            fileNameDiv: {title: "文件名称：", display: "show", disabled: "", placeholder: "请输入名称,支持字母,数字,下划线,中划线"},
            upLoadFileDiv: {display: "hide"},
            fileDesDiv: {display: "hide", disabled: ""},
            startShellDiv: {display: "hide"},
            saveFileSelectDiv: {display: "hide"},
            savePathDiv: {display: "hide"}
        },
        dirRename: {
            title: "修改目录名",
            url: "/scriptcenter/script/dirRename.ajax",
            saveType: 0,
            fileNameDiv: {title: "目录名称：", display: "show", disabled: "", placeholder: "请输入名称,支持中文,字母,数字,下划线,中划线,正斜杠"},
            upLoadFileDiv: {display: "hide"},
            fileDesDiv: {display: "hide", disabled: ""},
            startShellDiv: {display: "hide"},
            saveFileSelectDiv: {display: "hide", disabled: "disabled"},
            savePathDiv: {display: "hide"}
        }
    };
    var modeCode = undefined;
    var mode = undefined;
    var parentPath = undefined;
    var treeSource = 0;//treeNode来源 0取页面左边的 1还是重新请求
    var suffix = "";
    var type = undefined;
    var dirZtree = undefined;

    function refreshStatus(newKey, version, gitProjectId, gitProjectPath, md5, parentPath, name) {
        var activeWindow = window.datadevInit.getActiveWindow(newKey);
        activeWindow && activeWindow.win && activeWindow.win.initInfo && activeWindow.win.initInfo(gitProjectId, gitProjectPath, version, 0, md5, parentPath, name);
        activeWindow && activeWindow.win && activeWindow.win.saveBreakPoints && activeWindow.win.saveBreakPoints();
        activeWindow && activeWindow.win && activeWindow.win.verifyModify();//重新计算md5

    }

    function updateNode(zNode) {
        var node = zTree.getNodeByParam("path", scriptPath);
        var name = zNode.name;
        var newPath = zNode.path;
        var parentPath = zNode.parentPath;
        var script = getScriptObj(zNode.type);
        if (node) {
            node.oriName = name;
            node.name = "<span class='" + (node.isParent ? "NON" : node.gitStatus) + "'>" + name + "</span>";
            node.iconSkin = script.icon;
            node.path = newPath;
            node.parentPath = parentPath;
            zTree.updateNode(node);
        }
    }

    function getAjaxData() {
        var name = $("#fileName").val();
        if (name) {
            //名称输入了原后缀名
            if (!suffix || !name.endsWith(suffix)) {
                name += suffix;
            }
        }
        var newDir = $("input.dirId", $("#savePathDiv")).val();
        var ajaxData = {};
        switch (modeCode) {
            case SAVE_MODE:
            case SAVE_AS_MODE:
                var el = jq("#code").data("editor");
                ajaxData = {
                    gitProjectId: gitProjectId,
                    gitProjectDirPath: newDir,
                    gitProjectFilePath: scriptPath,
                    name: name,
                    description: $(".file-text", $("#saveModal")).val() || "",
                    content: el && el.getValue() ? el.getValue() : "",
                    type: type
                };
                break;
            case MOVE_MODE:
                ajaxData = {
                    gitProjectId: gitProjectId,
                    gitProjectFilePath: scriptPath,
                    gitProjectDirPath: newDir,
                    name: name,
                    description: $(".file-text", $("#saveModal")).val() || "",
                };
                break;
            case DIR_MODE:
                ajaxData = {
                    gitParentProjectDirPath: newDir,
                    name: $("#fileName").val(),
                    gitProjectId: gitProjectId
                };
                break;
            case SCRIPT_RENAME_MODE:
                ajaxData = {
                    gitProjectId: gitProjectId,
                    gitProjectFilePath: scriptPath,
                    name: name,
                };
                break;
            case DIR_RENAME_MODE:
                ajaxData = {
                    id: info.id,
                    name: $("#fileName").val(),
                };
                break;
        }
        return ajaxData;
    }

    function initDirTree() {

        if (dirZtree) {
            dirZtree.destroy();
        }
        var t = $("#fileTree");
        var pathAttr = [];
        var znodesDir = [];

        if (mode.savePathDiv.display == "show") {
            commonAjaxEvents.commonGetAjax(getChiDirUrl, {
                gitProjectId: gitProjectId,
                selectDirPath: parentPath || "",
                range: 1,
                targetRange: targetRange
            }, function (data) {
                if (data && data.obj) {
                    var zNodeArr = data.obj;
                    znodesDir = getNodes(zNodeArr, parentPath);
                    // var node = zTree.getNodeByParam("path", parentPath);
                    // if (node) {
                    //     var nodes = node.getPath();
                    //     for (var index = 0; index < nodes.length; index++) {
                    //         var path = nodes[index].path;
                    //         pathAttr.push(path);
                    //     }
                    // }
                    createDirTree(t, setting, znodesDir, parentPath);
                }
            });
        }
    }

    function getSelectedProjectId() {
        //return $("#appSelect").val();
        return info.gitProjectId;
    }

    function initParams() {

        treeSource = 0;
        var gitProjectIdNow = getSelectedProjectId();
        //不同模式处理
        gitProjectId = info.gitProjectId;
        parentPath = info.gitProjectDirPath;
        scriptPath = info.gitProjectFilePath;
        targetRange = info.targetRange || 0;
        var activeWindow = info.activeWindow;
        jq = activeWindow && activeWindow.jq;
        if (gitProjectId != gitProjectIdNow) {
            treeSource = 1;
        }
        type = info.type;
        setting.async.otherParam = ["gitProjectId", gitProjectId, "range", 1, "targetRange", targetRange];
        if (info && info.key) {
            key = info.key;
        } else {
            key = getKey(gitProjectId, scriptPath || "");
        }
    }

    function addDirFile(zNode, scriptPath) {
        if (scriptPath) {
            var old = zTree.getNodeByParam("path", scriptPath);
            zTree.removeNode(old);
        }
        zNode = addRev(zNode);
        while (zNode.children && zNode.children.length > 0) {
            zNode = zNode.children[0]
        }
        return zNode;
    }

    function addRev(zNode) {
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
                    } else {
                        var nodes = zTree.getNodesByFilter(function (node) {
                            return node.parentPath == zNode.parentPath && node.isParent == true && node.oriName < zNode.name;
                        });
                        index = nodes && nodes.length || 0;
                    }
                    var addTreeNodes = zTree.addNodes(parNode, index, newNodes);
                    //增加的目录不需要异步
                    if (modeCode == DIR_MODE && !zNode.children) {
                        zTree.expandNode(addTreeNodes[0]);
                    }
                } else {
                    zTree.expandNode(parNode);
                }
                return zNode;
            } else {
                if (zNode.children && zNode.children.length > 0) {
                    zNode = zNode.children[0];
                } else {
                    return zNode;
                }
            }
        }
    }

    function getPath(parentPath) {
        var pathAttr = [];
        while (parentPath.lastIndexOf("/") != -1) {
            var index = parentPath.lastIndexOf("/");
            parentPath = parentPath.substring(0, index);
            pathAttr.push(parentPath);
        }
        return pathAttr;
    }

    function getNodes(zNodes, parentPath) {
        var znodesDir = [];
        var parentArray = getPath(parentPath);
        for (var index = 0; index < zNodes.length; index++) {
            var zNode = zNodes[index];
            znodesDir.push({
                path: zNode.path,
                parentPath: zNode.parentPath,
                rootPath: rootPath + "/" + zNode.path,
                rootParentPath: zNode.parentPath ? (rootPath + "/" + zNode.parentPath) : rootPath,
                name: zNode.name || "bdp_default_dir",
                iconSkin: getScriptObj(-1).icon,
                isParent: true,
                zAsync: zNode.zAsync,
                open: (parentArray && $.inArray(zNode.path, parentArray) != -1)
            });
        }
        if (targetRange != 2) {
            znodesDir.push({
                path: "",
                parentPath: "",
                rootPath: rootPath,
                rootParentPath: "",
                name: "根目录",
                iconSkin: getScriptObj(-1).icon,
                isParent: true,
                open: true
            });
        }
        return znodesDir;
    }

    function refreshScriptInifo(loginErp, gitProjectId, oldPath, newPath , name) {

        var oldKey = getKey(gitProjectId,oldPath) ;

        var params = {
            url: "/scriptcenter/devcenter/script_edit.html?gitProjectFilePath=" + newPath + "&gitProjectId=" + gitProjectId,
            icon: '',
            title: name,
            key: getKey(gitProjectId,newPath),
            type: 'iframe',
            closeConfirm: false
        }


        //huliang
        currentWindow.QIAN_KUN.utils.updateTabByKey(oldKey , params  )

        debugger
        TabCacheClass.updateCacheByKey(oldKey,params)

    }

    function initFileInfo(mode, type) {
        //初始化需要显示的条目
        $("#saveModal h4").text(mode.title);
        for (var objId in mode) {
            if (mode[objId].display) {
                mode[objId].display == "hide" ? $("#" + objId).hide() : $("#" + objId).show();
            }
        }
        if (mode.fileNameDiv.display == "show") {
            var label = mode.fileNameDiv.title;
            if (mode.fileNameDiv.disabled == "disabled") {
                $("#fileNameDiv input").addClass("disabled");
            } else {
                label = "<i class='bdp-note'>*</i>" + label;
                $("#fileNameDiv input").removeClass("disabled");
            }
            if (mode.fileNameDiv.placeholder) {
                $("#fileNameDiv input").attr("placeholder", mode.fileNameDiv.placeholder)
            }
            $("#fileNameDiv label").empty().append(label);
        }
        if (mode.saveFileSelectDiv.display == "show") {
            if (mode.saveFileSelectDiv.disabled == "disabled") {
                $("#save-file-select").addClass("disabled");
                $("#saveFileSelectDiv label .bdp-note").hide();
            } else {
                $("#save-file-select").removeClass("disabled");
                $("#saveFileSelectDiv label .bdp-note").show();
            }
        }
        if (mode.fileDesDiv.display == "show") {
            if (mode.fileDesDiv.disabled == "disabled") {
                $("#fileDesDiv textarea").addClass("disabled");
                $("#fileDesDiv label .bdp-note").hide();
            } else {
                $("#fileDesDiv textarea").removeClass("disabled");
                $("#fileDesDiv label .bdp-note").show();
            }
        }
        //初始化内容 并重置提示
        $("#upLoadFile").val("");
        $("#savePath").val("");
        $("#startShellPath").val("");
        $("#upLoadFileDiv .bdp-file-content").text("请选择文件！");

        type = type || "";
        if (type) {
            if (type == 5) {
                var dotIndex = (info.name || "").lastIndexOf(".");
                suffix = dotIndex != -1 ? (info.name || "").substring(dotIndex) : "";
            } else {
                var script = getScriptObj(type);
                suffix = "." + script.suffix;
            }
        }
        if (modeCode == MOVE_MODE || modeCode == SAVE_AS_MODE || modeCode == SCRIPT_RENAME_MODE) {
            var name = info.name || "";
            var index = name.lastIndexOf(suffix);
            if (index != -1) {
                name = name.substring(0, index);
            }
            $("#fileName").val(name);
            $(".file-text").val(info && info.description ? info.description : "");
        } else {
            $("#fileName").val("");
            $(".file-text").val("");
        }
        //文件类型select初始化
        if (type == 5) {
            $("#unRecognizeType").text("不可识别类型(" + suffix + ")")
        }
        $("#save-file-select").val(type).select2();
        //去掉上次的错误提示
        $(".bdp-help-block", $("#saveModal")).remove();
    }

    function initSelect() {
        var options = "<option value=''></option>";
        var scriptObjs = {};
        for (var index = 0; index < scriptTypeArr.length; index++) {
            var scriptObj = scriptTypeArr[index];
            if (scriptObjs[scriptObj.scriptType]) {
                continue
            }
            if (scriptObj.scriptType == 5) {
                options += "<option id='unRecognizeType' value='" + scriptObj.scriptType + "'>不可识别类型(.)</option>";
            } else {
                options += "<option value='" + scriptObj.scriptType + "'>" + (scriptObj.name + "(" + "." + scriptObj.suffix + ")") + "</option>";
            }
            scriptObjs[scriptObj.scriptType] = true;
        }
        $("#save-file-select").html(options);
    }

    function createDirTree(t, setting, znodesDir, parentPath) {
        dirZtree = $.fn.zTree.init(t, setting, znodesDir);
        var node = dirZtree.getNodeByParam("path", parentPath);
        var tId = node && node.tId;
        if (tId) {
            $("#" + tId + " > .nodeDiv").click();
        }
    }

    jQuery.validator.addMethod("validFileRename", function (value, element) {
        var pattern = /^[0-9a-zA-Z\-_]+$/;
        if (modeCode == SCRIPT_RENAME_MODE) {
            return pattern.test(value);
        } else {
            return true;
        }
    }, "脚本重命名只支持字母,数字,下划线,中划线");
    jQuery.validator.addMethod("validFileName", function (value, element) {

        var pattern = /^[0-9a-zA-Z\-_/]+$/;
        if (modeCode == MOVE_MODE || modeCode == SAVE_AS_MODE || modeCode == SAVE_MODE) {
            return pattern.test(value);
        } else {
            return true;
        }
    }, "脚本命名只支持中文,字母,数字,下划线,中划线,正斜杠");
    jQuery.validator.addMethod("validDirName", function (value, element) {
        var pattern = /^[\u4e00-\u9fa50-9a-zA-Z\-_/]+$/;
        if (modeCode == DIR_MODE) {
            return pattern.test(value);
        } else {
            return true;
        }
    }, "目录命名只支持中文,字母,数字,下划线,中划线,正斜杠");
    jQuery.validator.addMethod("validStartShellPathValue", function (value, element) {
        if (value && value.trim()) {
            var pattern = /^[0-9a-zA-Z\-_./]+$/
            return pattern.test(value);
        } else {
            return true;
        }
    }, "只支持字母,数字,下划线,中划线,正斜杠,点");
    jQuery.validator.addMethod("validFileType", function (value, element) {
        var suffixPattern = /\.(\w+)$/;
        var grops = suffixPattern.exec(value);
        if (grops && grops[1]) {
            value = grops[1];
        } else {
            return false;
        }
        return suffixAttr[value];
    }, "只支持sh、sql、py、zip后缀名文件");
    jQuery.validator.addMethod("validStartShellPath", function (value, element) {
        var fileName = $("#upLoadFile").val();
        if (fileName) {
            var suffixPattern = /\.(\w+)$/;
            var suffix = undefined;
            var grops = suffixPattern.exec(fileName);
            if (grops && grops[1]) {
                suffix = grops[1];
            } else {
                return true;
            }
            if (suffix && suffix.toLowerCase() == "zip" && !value) {
                return false;
            }
        }
        return true;
    }, "zip文件必须填写启动项");
    jQuery.validator.addMethod("validDirPath", function (value, element) {
        if (value.startWith("/") || value.endWith("/") || value.indexOf("//") != -1) {
            return false;
        }
        if (value.indexOf(" ") != -1) {
            return false;
        }
        return true;
    }, "目录错误! eg. dirs/pythonUtil/util");
    jQuery.validator.addMethod("validDescription", function (value, element) {
        if (modeCode == MOVE_MODE) {
            return true;
        } else {
            return value && value.trim().length > 0;
        }
    }, "目录错误! eg. dirs/pythonUtil/util");


    $("#saveModal").modal({
        backdrop: 'static',
        show: false,
        keyboard: false
    });


    $.fn.FileMode = function (mode, args, callbackfun) {
        modeCode = mode;
        info = args;
        callBack = callbackfun;
        $(this).modal("show");
    }
    $(function () {


        modeCode = ($.dialog.data("modeCode"));
        info = ($.dialog.data("info"));
        currentWindow = ($.dialog.data("currentWindow"));
        zTree = ($.dialog.data("zTree"));
        callBack = ($.dialog.data("callbackfun"));
        var loginErp = ($.dialog.data("loginErp"));

        initSelect();

        $("input[name=finame]").focus();
        mode = modeAttr[modeCode];
        if (!mode) {
            return false;
        }
        type = info && info.type;
        if (!type && modeCode != DIR_MODE && modeCode != DIR_RENAME_MODE) {
            return false;
        }
        initFileInfo(mode, type);
        initParams();
        initDirTree();

        // $("#saveModal").on("hide.bs.modal", function () {
        //     alert("1111")
        // })
        // $("#saveModal").on("hidden.bs.modal", function () {
        //     uploadRequest && uploadRequest.abort();
        //     uploadRequest = null;
        //     saveStatusMap.put(key, 0);
        // })

        $("#save-cancel").click(function () {
            var currentArt = $.dialog.data("currentArt");
            currentArt && currentArt.close();
            clearData();
        })
        $("#save-ok").click(function (event) {

            var valid = $('#file-form').valid();
            if (valid) {
                var ajaxData = getAjaxData();
                if (!ajaxData) {
                    return false;
                }
                var isJson = false;
                if (modeCode == SAVE_MODE || modeCode == SAVE_AS_MODE) {
                    ajaxData = JSON.stringify(ajaxData);
                    isJson = true;
                }
                commonAjaxEvents.commonPostAjax(mode.url, ajaxData, $("#save-ok"), function (node, data) {
                    if (data && data.code == 0 && data.obj) {
                        var zNode = data.obj;
                        switch (modeCode) {
                            case MOVE_MODE:
                            case SAVE_AS_MODE:
                            case SAVE_MODE:
                                if (treeSource === 0) {
                                    if (modeCode == MOVE_MODE) {
                                        //移动脚本的时候先删除原来的脚本
                                        var old = zTree.getNodeByParam("path", scriptPath);
                                        zTree.removeNode(old);
                                    }
                                }
                                while (zNode.children && zNode.children.length > 0) {
                                    zNode = zNode.children[0];
                                }
                                new FrameBus().emit("leftManager:refresh");
                                break;
                            case DIR_MODE:
                                if (treeSource === 0) {
                                    zNode = addDirFile(zNode, scriptPath);
                                }
                                break;
                            case SCRIPT_RENAME_MODE:
                                if (zNode.status == "1") {
                                    top.$.errorMsg(zNode.msg);
                                } else {
                                    updateNode(zNode);

                                    refreshScriptInifo(loginErp, zNode.gitProjectId, scriptPath, zNode.path , zNode.name);
                                }
                                break;
                        }
                        callBack && callBack(data);

                        clearData();
                        $("#save-cancel").click();

                        if (modeCode == MOVE_MODE) {
                            top.$.successMsg("移动成功");
                        } else if (zNode.status != "1") {
                            top.$.successMsg(data.message || "操作成功");
                        }
                    }
                }, null, null, isJson)

            }
        })
        $('#file-form').validate({
            rules: {
                description: {
                    validDescription: true,
                    maxlength: 255
                },
                dirPath: {
                    required: true
                },
                startShellPath: {
                    validStartShellPath: true,
                    // validStartShellPathValue: true,
                    maxlength: 1000
                },
                name: {
                    required: true,
                    maxlength: 255,
                    validFileName: true,
                    validFileRename: true,
                    validDirName: true,
                    validDirPath: true
                },
                file: {
                    required: true,
                    maxlength: 255,
                    /*         validFileType: true,*/
                    validFileName: true
                }
            },
            messages: {
                name: {
                    required: "必填字段！",
                    maxlength: $.validator.format("最多{0}个字符"),
                    validFileName: "脚本命名只支持字母,数字,下划线,中划线",
                    validFileRename: "脚本重命名只支持字母,数字,下划线,中划线",
                    validDirName: "目录命名只支持中文,字母,数字,下划线,中划线,正斜杠",
                    validDirPath: "目录不能以/开i头结尾，并且不支持连续//"
                },
                description: {
                    validDescription: "必填字段！",
                    maxlength: $.validator.format("最多{0}个字符"),
                },
                dirPath: {
                    required: "必需选择目录！"
                },
                file: {
                    required: "必须上传文件！",
                    maxlength: $.validator.format("最多{0}个字符"),
                    validFileType: "只支持sh、sql、py、zip后缀名文件",
                    validFileName: "脚本命名只支持中文,字母,数字,下划线,中划线,正斜杠"
                },
                startShellPath: {
                    validStartShellPath: "zip文件必须填写启动项",
                    // validStartShellPathValue: "只支持字母,数字,下划线,中划线,正斜杠,点",
                    maxlength: $.validator.format("最多{0}个字符"),
                },
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
                // if (element.attr("name") == "dirPath" ) {
                //     error.appendTo(element.parent().parent());
                // }else if(element.attr("name") == "file"){
                //     error.appendTo(element.parent().parent().parent());
                // }else {
                //     error.appendTo(element.parent());
                // }
                error.appendTo(element.parents(".dialog-group"));

            }
        });

    })

})(jQuery)

function clearData() {

    ($.dialog.removeData("modeCode"));
    ($.dialog.removeData("info"));
    ($.dialog.removeData("zTree"));
    ($.dialog.removeData("callbackfun"));
}

function getKey(gitProjectId, path) {
    path = path || $("#gitProjectFilePath").val();
    gitProjectId = gitProjectId || $("#gitProjectId").val();
    gitProjectId = $.trim(gitProjectId)
    path = $.trim(path);
    if (path.startsWith("/")) {
        path = gitProjectId + path;
    } else {
        path = gitProjectId + "/" + path.trim();
    }
    return path;
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

function getUrl(gitProjectId, path, pythonType) {
    var encodepath = encodeURIComponent(encodeURIComponent(path.trim()));
    var url = "/scriptcenter/home/home_open_ide.html?gitProjectFilePath=" + encodepath + "&gitProjectId=" + gitProjectId;
    if (pythonType) {
        url += "&pythonType=" + pythonType;
    }
    return url;
}

function getZtreeNode(zNode) {
    var SCRIPT_GIT_STSTUS = {
        ADD: "ADD",
        MOD: "MOD",
        NON: "NON",
        DEL: "DEL"
    }
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

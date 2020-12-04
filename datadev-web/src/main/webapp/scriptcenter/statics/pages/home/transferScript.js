$(function () {
    var getChiDirUrl = "/scriptcenter/script/getScripsByDirId.ajax";
    var rootPath = "root";
    var preSelectedDirId = undefined;
    var path = "";
    var tranferArt = $.dialog.data("tranferArt");
    var scriptMergeContentMap = $.dialog.data("scriptMergeContentMap");
    var diff = $.dialog.data("diff");
    var getKey = $.dialog.data("getKey");
    var getActiveWindow = $.dialog.data("getActiveWindow");
    var successMsg = $.dialog.data("successMsg");
    var openScript = $.dialog.data("openScript");
    var resetUrl = $.dialog.data("resetUrl");
    var refreshFileInfoStatus = $.dialog.data("refreshFileInfoStatus");

    var dirZtree;
    initSelect();
    $("#tranferOkBtn").click(function () {
        var valid = $('#transferForm').valid();
        if (valid) {
            var gitProjectId = $("#projectSelect").val();
            // var dirPath = $("#projectDirPath").val();
            var dirPath = $("#projectDirPath").attr("real-path");
            var appGroupId = $("#applicationSelect").length > 0 ? $("#applicationSelect").val() : $("#jsdAppgroupId").val();
            var postData = {
                gitProjectId: gitProjectId,
                dirPath: dirPath,
                jsdAppgroupId: appGroupId,
                // syncMember: syncMember,
                // scriptVersion: $("#scriptVersion").val(),
                scriptName: $("#scriptName").val(),
                scriptId: $("#scriptId").val(),
                sync: 1
            };
            if (gitProjectId && appGroupId) {
                commonAjaxEvents.commonPostAjax("/scriptcenter/script/checkFileExist.ajax", {
                    gitProjectId: gitProjectId,
                    gitProjectDirPath: dirPath || "",
                    name: $("#scriptName").val()
                }, $("#tranferOkBtn"), function (node, data) {
                    if (data.obj.result == 1) {
                        $("#transferScriptArt").hide();
                        $("#duplicationNameDiv").show();
                        path = data.obj.path;
                        tranferArt.title("同名提示");

                    } else {
                        commonAjaxEvents.commonPostAjax("/scriptcenter/project/syncScriptToDataDev.ajax", postData, $("#tranferOkBtn"), function (node, data) {
                            if (data.obj) {
                                successMsg();
                                var info = data.obj;
                                var ztreeList = info && info.ztreeNodeList;
                                if (ztreeList) {
                                    var node = ztreeList[0];
                                    while (node.children && node.children.length > 0) {
                                        node = node.children[0];
                                    }
                                    var gitProjectId = node.gitProjectId;
                                    var gitProjectFilePath = node.path;
                                    var name = node.name;
                                    // locationScript(gitProjectId, gitProjectFilePath);
                                    openScript(gitProjectId, gitProjectFilePath, name);
                                }
                                resetUrl && resetUrl();
                                tranferArt.close()
                                // var addNodeByImport = $.dialog.data("addNodeByImport");
                                // addNodeByImport && addNodeByImport(info && info.ztreeNodeList, info && info.gitProjectId, closeArt);
                            }
                        })
                    }
                })
            }
        }
    })
    $("#lastStep").click(function () {
        tranferArt.title("将脚本管理中脚本导入开发平台");
        $("#duplicationNameDiv").hide();
        $("#transferScriptArt").show();
    })

    function updateScriptAndRelate(gitProjectId, gitProjectFilePath, scriptId, scriptName, content) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/syncScriptByMerge.ajax", {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            scriptId: scriptId,
            scriptName: scriptName,
            content: content
        }, null, function (node, data) {
            successMsg();
            var key = getKey(gitProjectId, path);
            var scriptWindow = getActiveWindow(key);
            if (scriptWindow && scriptWindow.status) {
                //此脚本已被打开
                if (content) {
                    scriptWindow && scriptWindow.editor && scriptWindow.editor.setValue(content);
                }
                refreshFileInfoStatus && refreshFileInfoStatus(gitProjectId,gitProjectFilePath,data.obj.fileMd5,data.obj.version,data.obj.gitStatus);
            }
            openScript(gitProjectId, gitProjectFilePath, scriptName);
            resetUrl && resetUrl();
            tranferArt.close();
        })
    }

    $("#updateScript").click(function () {
        var scriptName = $("#scriptName").val();
        var gitProjectId = $("#projectSelect").val();
        var scriptId = $("#scriptId").val();
        var canEdit = scriptName && (scriptName.endWith(".sql") || scriptName.endWith(".py") || scriptName.endWith(".sh"))
        if (!canEdit) {
            updateScriptAndRelate(gitProjectId, path, scriptId, scriptName, null);
        } else if (scriptMergeContentMap && diff && getKey) {
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getImportMergeContent.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: path,
                scriptId: scriptId
            }, $("#updateScript"), function (node, data) {
                var key = getKey(gitProjectId, path);
                var contents = {
                    local: data.obj.hbaseContent || "",
                    remote: data.obj.buffaloContent || "",
                };
                scriptMergeContentMap.put(key, contents)
                if ($.md5(contents.local) == $.md5(contents.remote)) {
                    updateScriptAndRelate(gitProjectId, path, scriptId, scriptName, contents.local);
                } else {
                    $.dialog.data("leftTips", "数据开发平台   所在路径：" + ($("#projectDirPath").attr("real-path")||"根目录"));
                    $.dialog.data("rightTips", "任务调度脚本管理   项目空间：" + $("#jsdAppgroupName").text());
                    diff($("#projectSelect").val(), path, function (key, data) {
                        updateScriptAndRelate(gitProjectId, path, scriptId, scriptName, data.content);
                    });
                }
            })
        }
    })
    $("#closeTranferBtn").click(closeArt)

    function closeArt() {
        var tranferArt = $.dialog.data("tranferArt");
        tranferArt && tranferArt.close()
    }

    jQuery.validator.addMethod("validDirPath", function (value, element) {
        var pattern = /^[\u4e00-\u9fa50-9a-zA-Z\-_/]+$/;
        return pattern.test(value);
    }, "目录命名只支持中文,字母,数字,下划线,中划线,正斜杠");

    $('#transferForm').validate({
        rules: {
            projectDirPath: {
                required: true,
                maxlength: 1000,
                validDirPath: true
            },
            applicationSelect: {
                required: true,
                maxlength: 1000
            },
            projectSelect: {
                required: true,
                maxlength: 1000
            },

        },
        messages: {
            projectDirPath: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validDirPath: "目录命名只支持中文,字母,数字,下划线,中划线,正斜杠"
            },
            applicationSelect: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            projectSelect: {
                required: "（当前地址无效，请重新输入）",
                maxlength: $.validator.format("最多{0}个字符"),
            },
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.bdp-control-content').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.bdp-control-content').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            // error.appendTo(element.parents(".bdp-form-group"));
            if (element.attr("id") == "projectDirPath") {
                error.css({top: "234px"}).appendTo(element.parent());
            } else {
                error.appendTo(element.parent());
            }

        }
    });

    function initSelect() {
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/getAppByErp.ajax", {}, null, function (node, data) {
            var apps = data.obj;
            var options = "<option value=''>请选择</option>";
            for (var index = 0; index < apps.length; index++) {
                options += "<option data-id='" + apps[index].appgroupId + "' value='" + apps[index].appgroupId + "'>" + apps[index].appgroupName + "</option>";
            }
            var gitProjectId = $("#projectId").val();
            $("#projectSelect").empty().append(options);
            $("#projectSelect").select2().val(gitProjectId).trigger("change");
        })
    }

    $('#transferForm').on("click", "#createGitProject", function () {
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
    })
    $("#projectSelect").on("change", function () {
        var projectId = $("#projectSelect").val();
        if (projectId) {
            commonAjaxEvents.commonPostAjax(getChiDirUrl, {
                gitProjectId: projectId,
                range:1,
            }, null, function (node, data) {
                if (data && data.obj) {
                    var zNodeArr = data.obj;
                    var znodesDir = getNodes(zNodeArr);
                    $("#projectDirPath").val("");
                    $("#projectDirPath").attr("real-path", "");
                    dirZtree = $.fn.zTree.init($("#fileTree"), getSetting(projectId), znodesDir);
                }
            });
        }
    })

    function getNodes(zNodes) {
        var znodesDir = [];
        for (var index = 0; index < zNodes.length; index++) {
            var zNode = zNodes[index];
            znodesDir.push({
                path: zNode.path,
                parentPath: zNode.parentPath,
                rootPath: rootPath + "/" + zNode.path,
                rootParentPath: zNode.parentPath ? (rootPath + "/" + zNode.parentPath) : rootPath,
                name: zNode.name || "bdp_default_dir",
                iconSkin: "icon01",
                isParent: true
            });
        }
        znodesDir.push({
            path: "",
            parentPath: "",
            rootPath: rootPath,
            rootParentPath: "",
            name: "根目录",
            iconSkin: "icon01",
            isParent: true,
            open: true
        });
        return znodesDir;
    }
    function getSettingAsync(gitProjectId) {
        var async = {
            enable: true,
            url: "/scriptcenter/script/getScripsByDirId.ajax",
            autoParam: ["path"],
            otherParam: ["gitProjectId", gitProjectId,"range",1],
            dataFilter: function (treeId, parentNode, responseData) {
                if (responseData && responseData.obj && responseData.obj.length > 0) {
                    var array = responseData.obj;
                    var resultArray = new Array();
                    for (var i = 0; i < array.length; i++) {
                        var zNode = array[i];
                        resultArray.push({
                            path: zNode.path,
                            parentPath: zNode.parentPath,
                            rootPath: rootPath + "/" + zNode.path,
                            rootParentPath: zNode.parentPath ? (rootPath + "/" + zNode.parentPath) : rootPath,
                            name: zNode.name || "bdp_default_dir",
                            iconSkin: "icon01",
                            isParent: true
                        });
                    }
                    return resultArray;
                } else {
                    return [];
                }
            }
        };
        return async;
    }
    function getSetting(gitProjectId) {
        var setting = {
            view: {
                dblClickExpand: false,
                showLine: false,
                selectedMulti: false
            },
            async: getSettingAsync(gitProjectId),
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
                    $("#projectDirPath").val(treeNode.rootPath).trigger("keyup");
                    $("#projectDirPath").attr("real-path", treeNode.path);
                    return true;
                }
            }
        };
        return setting;
    }

    $("#projectDirPath").bind("input propertychange", function () {
        // var path = $("#projectDirPath").val();
        // var node=dirZtree.getNodeByParam("path", path);
        // if(node){
        //     dirZtree.expandNode(node, true, false, true, true);
        //     var selectNodeDom = $("#" + node.tId );
        //     var selectedclass = "selectedNodeDir";
        //     if (preSelectedDirId) {
        //         $("#" + preSelectedDirId).removeClass(selectedclass)
        //     }
        //     selectNodeDom.addClass(selectedclass);
        // }
    });

    // function getTransferProcess(appGroupId) {
    //     commonAjaxEvents.commonPostAjax("/scriptcenter/project/syncScriptToDataDevProcess.ajax", {
    //         appGroupId: appGroupId
    //     }, null, function (node, data) {
    //         var info = data.obj;
    //         doProcess(info);
    //     })
    // }

    // function doProcess(info) {
    //     if (info == "") {
    //         if (interId) {
    //             window.clearInterval(interId);
    //         }
    //         datadev_process.process($("#processTransfer"), 100);
    //         // $.successMsg("同步完成", 2000, closeArt);
    //     } else {
    //         var currentIndex = info.successCount + info.failCount;
    //         var total = info.total;
    //         $("#currentFileName").text(info.currentFile)
    //         $("#successNum").text(info.successCount)
    //         $("#failureNum").text(info.failCount)
    //         $("#totalNum").text(total)
    //         datadev_process.process($("#processTransfer"), (currentIndex / total * 100).toFixed(2));
    //         if (currentIndex == total) {
    //             if (interId) {
    //                 window.clearInterval(interId);
    //             }
    //             datadev_process.process($("#processTransfer"), 100);
    //             // $.successMsg("同步完成", 2000, closeArt);
    //         }
    //     }
    // }


})

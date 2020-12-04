// save :保存文件  saveAs：另存为  upFile：上传文件  move：移动文件   dir：创建目录
var uploadRequest = undefined;

function closeWindowBefore() {
    if ($("#process").data("mutiFile")) {
        var runData = $("#process").MutiFile("runResult");
        console.log(runData);
        if (runData.NEED_UP > 0 || runData.UP_ING > 0) {
            parent.$.bdpMsg({
                title: "提示",
                width: "350px",
                mainContent: "<p style='margin-top: 30px;margin-bottom: 30px;'>关闭页面，将终止脚本上传操作，请确定是否关闭？ </p>",
                buttons: [
                    {
                        text: "继续关闭",
                        event: function () {
                            parent.$.removeMsg();
                            if (runData.UP_FINISH_SUCCESS > 0) {
                                parent.initZtree && parent.initZtree(true);
                            }
                            $.dialog.data("mutilFileWindow").close();

                        },
                        btnClass: 'bdp-btn-default'
                    },
                    {
                        text: "取消",
                        event: function () {
                            if (runData.UP_FINISH_SUCCESS > 0) {
                                parent.initZtree && parent.initZtree(true);
                            }
                            parent.$.removeMsg();
                        },
                        btnClass: 'bdp-btn-primary'
                    }
                ]
            })
            return false;
        }

        if (runData.UP_CANCEL > 0 || runData.UP_FINISH_FAILED > 0) {
            parent.$.bdpMsg({
                title: "提示",
                width: "350px",
                mainContent: "<p style='margin-top: 30px;margin-bottom: 30px;'>本次上传有未处理脚本，请确定是否关闭？ </p>",
                buttons: [
                    {
                        text: "继续关闭",
                        event: function () {
                            parent.$.removeMsg();
                            if (runData.UP_FINISH_SUCCESS > 0) {
                                parent.initZtree && parent.initZtree(true);
                            }
                            $.dialog.data("mutilFileWindow").close();
                        },
                        btnClass: 'bdp-btn-default'
                    },
                    {
                        text: "取消",
                        event: function () {
                            if (runData.UP_FINISH_SUCCESS > 0) {
                                parent.initZtree && parent.initZtree(true);
                            }
                            parent.$.removeMsg();
                        },
                        btnClass: 'bdp-btn-primary'
                    }
                ]
            })
            return false;
        }
        if (runData && runData.UP_FINISH_SUCCESS > 0) {
            parent.initZtree && parent.initZtree(true);
        }
    }
    return true;
}


jQuery(function () {
    var dirZtree = undefined;
    var parentPath = $("#gitProjectDirPath").val();
    var rootPath = "root";
    var gitProjectId = $("#gitProjectId").val();
    var preSelectedDirId = undefined;
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
            url: "/scriptcenter/script/getScripsByDirId.ajax",
            autoParam: ["path"],
            otherParam: ["gitProjectId", gitProjectId, "range", 1],
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

                var appendRootPath = $.trim(treeNode.rootPath);
                appendRootPath = appendRootPath.replace("root", "");
                if ($.trim(appendRootPath).length > 0) {
                    $("#selectDirPath").html($("#gitProjectName").val() + appendRootPath)
                } else {
                    $("#selectDirPath").html($("#gitProjectName").val())
                }

                $("input.dirId", $("#savePathDiv")).val(treeNode.path);
                return true;
            }
        }
    };

    function createDirTree(t, setting, znodesDir, parentPath) {
        dirZtree = $.fn.zTree.init(t, setting, znodesDir);
        var node = dirZtree.getNodeByParam("path", parentPath);
        var tId = node && node.tId;
        if (tId) {
            $("#" + tId + " > .nodeDiv").click();
        }
    }

    function getNodes(zNodes, pathAttr) {
        var znodesDir = [];
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
                open: (pathAttr && $.inArray(zNode.path, pathAttr)) != -1 ? true : false
            });
        }
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
        return znodesDir;
    }

    var initPage = {
        initpage: function () {
            initPage.initZtree();
            initPage.initEvent();
            initPage.initValidate();
        },
        initValidate: function () {
            $('#file-form').validate({
                rules: {
                    description: {
                        required: true,
                        maxlength: 255
                    },
                    dirPath: {
                        required: true
                    }
                },
                messages: {
                    description: {
                        required: "必填字段！",
                        maxlength: $.validator.format("最多{0}个字符"),
                    },
                    dirPath: {
                        required: "必需选择目录！"
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
                    error.appendTo(element.parents(".dialog-group"));
                }
            });
        },
        initEvent: function () {
            $("#selectDir").click(function () {
                var valid = $('#file-form').valid();
                if (valid) {
                    $("#fileFolder").click();
                }
            })
            $("#cancel").click(function () {
                if (closeWindowBefore()) {
                    $.dialog.data("mutilFileWindow").close();
                }
            })

            $("#selectFiles").click(function () {
                var valid = $('#file-form').valid();
                if (valid) {
                    $("#fileFiles").click();
                }
            })
            $("#fileFolder").change(function () {
                var dirFiles = $("#fileFolder")[0].files;
                initPage.beforeUpLoad(dirFiles);
            });
            $("#fileFiles").change(function () {
                var selectFiles = $("#fileFiles")[0].files;
                initPage.beforeUpLoad(selectFiles);
            })
        },
        beforeUpLoad: function (selectFiles) {

            if (selectFiles.length < 1) {
                $.errorMsg("请先选择文件！");
                return;
            }
            var ajaxData = {
                gitProjectId: gitProjectId,
                description: $("#description").val(),
                gitProjectDirPath: $("#savePath").val()
            }
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/addScriptUpload.ajax", ajaxData, undefined, function (node, data) {
                initPage.initProcess(selectFiles, data.obj)
            })


        },
        initProcess: function (selectFiles, scriptUpload) {

            $("#upFileSelect").css("display", "none");
            $("#fileUpProcess").css("display", "block");

            $("#selectDir").css("display", "none");
            $("#selectFiles").css("display", "none");


            $("#process").MutiFile(
                {
                    files: selectFiles,
                    url: '/scriptcenter/script/saveMutilFile.ajax',
                    sizeLimit: 300, /*单个文件大小限制M*/
                    threadLimit: 10, /*同时能够上传的文件个数*/
                    showLimit: 100, /*列表最多显示的文件个数*/
                    fileTypeFilter: "all", /*过滤文件类型*/
                    userParams: {
                        gitProjectId: scriptUpload.gitProjectId,
                        gitProjectDirPath: scriptUpload.gitProjectDirPath,
                        scriptUpLoadId: scriptUpload.id
                    },
                    fileItemUpFinishCallBack: function (obj) {
                        var proess = obj.successItems.length + "/" + obj.countTotal;
                        var span = "<span style='font-weight: bold;'>正在上传 (" + proess + ")</span>";
                        $.dialog.data("mutilFileWindow").title(span);

                    }
                }
            )
        },
        initZtree: function () {
            if (dirZtree) {
                dirZtree.destroy();
            }
            var t = $("#fileTree");
            var znodesDir = [];

            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScripsByDirId.ajax", {
                gitProjectId: gitProjectId,
                selectDirPath: parentPath || "",
                range: 1
            }, null, function (node, data) {
                if (data && data.obj) {
                    var zNodeArr = data.obj;
                    znodesDir = getNodes(zNodeArr);
                    createDirTree(t, setting, znodesDir, parentPath);
                }
            });

        }

    }
    initPage.initpage();
})
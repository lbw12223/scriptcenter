(function ($) {
    var preSelectedDirId = undefined;
    var zTree = undefined;
    var packZipArt = $.dialog.data("packZipArt");
    var loadScript = $.dialog.data("loadScript");
    var dependencyArt = $.dialog.data("dependencyArt");
    var packZipData = $.dialog.data("packZipData");
    var updateNode = $.dialog.data("updateNode");

    var initPage = {
        initEvent: function () {
            $("#closeDia").click(function () {
                packZipArt && packZipArt.close();
            })
            $("#pack2Save").click(function () {
                var valid = $("#packZipForm").valid();
                if (valid) {
                    var path = initPage.getPath();
                    commonAjaxEvents.commonPostAjax("/scriptcenter/script/checkFileExist.ajax", {
                        gitProjectId: $("#gitProjectId").val(),
                        gitProjectFilePath: path
                    }, null, function (node, data) {
                        if (data.obj.result == 1) {
                            $.bdpMsg({
                                title: "提示",
                                mainContent: "<div style='padding: 20px 10px;'>当前zip文件已经存在，请确定是否覆盖？</div>",
                                width: "300px",
                                buttons: [
                                    {
                                        text: "确定",
                                        event: function () {
                                            $.removeMsg();
                                            initPage.packZip();
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
                            initPage.packZip();
                        }
                    })
                }
            })
        },
        initTargetZtree: function () {
            var gitProjectId = $("#gitProjectId").val();
            var setting = {
                view: {
                    dblClickExpand: false,
                    showLine: false,
                    selectedMulti: false,
                    initPadding: 25,
                    initPaddingLeft: 10
                },
                data: {
                    simpleData: {
                        enable: true,
                        idKey: "path",
                        pIdKey: "parentPath",
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
                        $("#savePath").val(treeNode.path)
                        return true;
                    }
                }
            };
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScripsByTarget.ajax", {
                gitProjectId: gitProjectId,
            }, null, function (node, data) {
                if (data && data.obj) {
                    var zNodeArr = data.obj;
                    var zNodes = [];
                    for (var index = 0; index < zNodeArr.length; index++) {
                        var zNode = zNodeArr[index];
                        if (!zNode.type) {
                            zNode.type = -1;
                        }
                        var script = undefined;
                        script = getScriptObj(zNode.type);
                        zNodes.push({
                            parentPath: zNode.parentPath,
                            path: zNode.path,
                            name: zNode.name || "bdp_default",
                            iconSkin: script.icon,
                            isParent: zNode.parChl == 0 ? true : false,
                            type: zNode.type,
                            runType: zNode.runType,
                            gitProjectId: zNode.gitProjectId,
                            open: zNode.openStatus == 1
                        });
                    }
                    var t = $("#fileTree");
                    zTree = $.fn.zTree.init(t, setting, zNodes);
                    var node = zTree.getNodeByParam("parentPath", "");
                    if (node) {
                        $(".nodeDiv", $("#" + node.tId)).click();
                    }
                }
            });
        },
        initValidate: function () {
            $('#packZipForm').validate({
                rules: {
                    name: {
                        required: true,
                        maxlength: 255,
                        validFileName: true,
                    },
                    comment: {
                        required: true,
                        maxlength: 255,
                    }
                },
                messages: {
                    name: {
                        required: "必填字段！",
                        maxlength: $.validator.format("最多{0}个字符"),
                        validFileName: "脚本命名只支持字母,数字,下划线,中划线",
                    },
                    comment: {
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
                    error.appendTo(element.parents(".dialog-group"));

                }
            });
            jQuery.validator.addMethod("validFileName", function (value, element) {
                var pattern = /^[0-9a-zA-Z\-_/]+$/;
                return pattern.test(value);
            }, "脚本命名只支持中文,字母,数字,下划线,中划线,正斜杠");
        },
        packZip: function () {
            var gitProjectId = $("#gitProjectId").val();
            var gitProjectFilePath = $("#gitProjectFilePath").val();
            var packPath = initPage.getPath();
            var comment = $("#comment").val();
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/packZip.ajax", {
                gitProjectId: gitProjectId,
                gitProjectFilePath: gitProjectFilePath,
                packPath: packPath,
                data: packZipData,
                comment:comment
            }, null, function (node, data) {
                if (data.obj) {
                    updateNode && updateNode({
                        gitProjectId: gitProjectId,
                        path: gitProjectFilePath,
                        runType: 1
                    })
                    $.successMsg("打包成功！", 2000, function () {
                        loadScript(data.obj.gitProjectId, data.obj.path)
                        dependencyArt.close();
                        packZipArt.close();
                    })
                }
            }, null, null, null, null, function () {
                $.loadingMsg("正在打包zip脚本，请稍后！")
            })
        },
        getPath: function () {
            var name = $("#fileName").val();
            name = name.endWith(".zip") ? name : (name + ".zip");
            var path = $("#savePath").val();
            path = (path.endWith("/") ? path : (path + "/")) + name;
            return path
        }
    }
    $(function () {
        initPage.initEvent();
        initPage.initTargetZtree();
        initPage.initValidate();
    })
})(jQuery)

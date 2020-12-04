// save :保存文件  saveAs：另存为  upFile：上传文件  move：移动文件   dir：创建目录
var uploadRequest = undefined;

var rootPath = "root";
var preSelectedDirId = undefined;
var getChiDirUrl = "/scriptcenter/script/getScripsByDirId.ajax";

var key = undefined;
var jq = undefined;
var info = undefined;
var callBack = undefined;
var gitProjectId = $("#gitProjectId").val();
var scriptType = $("#scriptType").val();
var targetRange = 0;
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
var parentPath = '';
var dirZtree = undefined;


var ztreeOpt = {
    initZtree: function () {
        if (dirZtree) {
            dirZtree.destroy();
        }
        var t = $("#fileTree");
        var znodesDir = [];
        commonAjaxEvents.commonPostAjax(getChiDirUrl, {
            gitProjectId: gitProjectId,
            selectDirPath: parentPath || "",
            range: 1,
            targetRange: targetRange
        }, null, function (node, data) {
            if (data && data.obj) {
                var zNodeArr = data.obj;
                znodesDir = ztreeOpt.getNodes(zNodeArr, parentPath);
                ztreeOpt.createDirTree(t, setting, znodesDir, parentPath);
            }
        });
    },
    getNodes: function (zNodes, parentPath) {
        var znodesDir = [];
        var parentArray = ztreeOpt.getPath(parentPath);
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
    },
    getPath: function (parentPath) {
        var pathAttr = [];
        while (parentPath.lastIndexOf("/") != -1) {
            var index = parentPath.lastIndexOf("/");
            parentPath = parentPath.substring(0, index);
            pathAttr.push(parentPath);
        }
        return pathAttr;
    },
    createDirTree: function (t, setting, znodesDir, parentPath) {
        dirZtree = $.fn.zTree.init(t, setting, znodesDir);
        var node = dirZtree.getNodeByParam("path", parentPath);
        var tId = node && node.tId;
        if (tId) {
            $("#" + tId + " > .nodeDiv").click();
        }
    }
}
var homeOpenIdeSave = {

    initPage: function () {
        ztreeOpt.initZtree();
        homeOpenIdeSave.initSelect();
        homeOpenIdeSave.initValidate();
        homeOpenIdeSave.initButtonEvent();
    },
    ajaxSaveContent: function () {
        var valid = $('#file-form').valid();
        if (valid) {
            var name = $("#name").val();
            var newDir = $("input.dirId", $("#savePathDiv")).val();
            var description = $("#description").val() || "";

            var ajaxData = {
                gitProjectId: gitProjectId,
                gitProjectDirPath: newDir,
                gitProjectFilePath: scriptPath,
                name: name,
                description: description,
                content: jq("#code").data("editor").getValue() || "",
                type: scriptType
            };
            ajaxData = JSON.stringify(ajaxData);
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/save.ajax", ajaxData, $("#save"), function (node, data) {
                if (data && data.code == 0 && data.obj) {

                }
            }, null, null, true)

        }

    },
    initButtonEvent: function () {

        $("#save").click(function () {
            homeOpenIdeSave.ajaxSaveContent();
        })

        $("#cancel").click(function () {

        })
    },
    initValidate: function () {



        $.validator.addMethod("fileNahhme", function (value, element) {
            var pattern = /^[\u4e00-\u9fa50-9a-zA-Z\-_]+$/;
            return pattern.test(value);
        }, "脚本名称只支持中文,字母,数字,下划线,中划线");


        $('#file-form').validate({
            rules: {
                filename: {
                    required: true,
                    maxlength: 255
                }
                // },
                // description1: {
                //     required: true,
                //     maxlength: 255
                // }
            },
            messages: {
                filename: {
                    required: "必填字段！",
                    maxlength: $.validator.format("最多{0}个字符"),
                    name: "脚本命名只支持字母,数字,下划线,中划线",
                },
                description1: {
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

    },
    initSelect: function () {
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
        $("#save-file-select").val(scriptType).select2();
    },

    initZtree: function () {

    }
}


homeOpenIdeSave.initPage();

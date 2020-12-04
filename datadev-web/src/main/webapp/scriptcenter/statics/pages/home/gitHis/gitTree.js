

jQuery(function () {

    var dirZtree = undefined;
    var commit = $("#commitId").val();
    var rootPath = "root";
    var gitProjectId = $("#gitProjectId").val();
    var preSelectedDirId = undefined;
    var isFirstClick = true ;
    var setting = {
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
                idKey: "rootPath",
                pIdKey: "rootParentPath",
                rootPId: ""
            }
        },

        callback: {
            beforeClick: function (treeId, treeNode) {
                if(isFirstClick){
                    isFirstClick = false ;
                    return ;
                }
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
            },
            onDblClick:function (event,treeId, treeNode) {
                console.log(arguments);
                console.log(treeNode);
                if(treeNode.id * 1 > 0){
                    $.dialog.open("/scriptcenter/script/gitHisDiff.html?id="+treeNode.id , {
                        title: "Git版本详情对比",
                        lock: true,
                        width: "70%",
                        height: "80%",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    })
                    return ;
                }
                return ;
            }
        }

    };

    function getNodes(zNodes, pathAttr) {
        var znodesDir = [];
        for (var index = 0; index < zNodes.length; index++) {
            var zNode = zNodes[index];
            console.log(zNode);
            var spanName = "<span class='"+zNode.gitStatus+"'>"+zNode.name +"</span>";
            znodesDir.push({
                path: zNode.path,
                parentPath: zNode.parentPath,
                rootPath: rootPath + "/" + zNode.path,
                rootParentPath: zNode.parentPath ? (rootPath + "/" + zNode.parentPath) : rootPath,
                name: spanName || "bdp_default_dir",
                iconSkin: getScriptObj(zNode.type).icon,
                isParent: zNode.type * 1 == -1 ? true : false,
                zAsync: zNode.zAsync,
                open: true,
                id:zNode.id
            });
        }
        znodesDir.push({
            path: "",
            parentPath: "",
            rootPath: rootPath,
            rootParentPath: "",
            name: $("#gitProjectName").val(),
            iconSkin: getScriptObj(-1).icon,
            isParent: true,
            open: true,
            id:0
        });
        return znodesDir;
    }


    var pageInit = {
        initPage : function () {
            pageInit.initZtree();
        },
        initZtree :function () {
            if (dirZtree) {
                dirZtree.destroy();
            }
            var t = $("#fileTree");
            var znodesDir = [];
            ///scriptcenter/script/getScripsByDirId.ajax
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/gitHisCommitTree.ajax", {
                gitProjectId: gitProjectId,
                commit: commit || "",
                range: 1,
                gitProjectFilePath:$("#gitProjectFilePath").val()
            }, null, function (node, data) {
                if (data && data.obj) {
                    var zNodeArr = data.obj;
                    znodesDir = getNodes(zNodeArr);
                    pageInit.createDirTree(t, setting, znodesDir, "");
                }
            });
        },
        createDirTree:function (t, setting, znodesDir, parentPath) {
            dirZtree = $.fn.zTree.init(t, setting, znodesDir);
            var node = dirZtree.getNodeByParam("path", parentPath);
            var tId = node && node.tId;
            if (tId) {
                $("#" + tId + " > .nodeDiv").click();
            }
        }
        

    }

    pageInit.initPage();

})

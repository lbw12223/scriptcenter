$(function () {
    var leftZtree = undefined;
    var rightZtree = undefined;
    var leftPreSelectedId = undefined
    var rightPreSelectedId = undefined
    var readyToDo = new window.Map();//key:parentPath  value ;[] 同一父目录下的待处理文件或子目录
    var gitProjectId = undefined;
    var dependencyArt = $.dialog.data("dependencyArt");
    var updateNode = $.dialog.data("updateNode");
    var pack2Zip = $.dialog.data("pack2Zip");
    var needShowPaths = new Map();//默认存在的脚本及目录


    function initLeftZtree(gitProjectId, gitProjectFilePath) {
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
                otherParam: ["gitProjectId", gitProjectId],
                dataFilter: function (treeId, parentNode, responseData) {
                    if (responseData && responseData.obj && responseData.obj.length > 0) {
                        var array = responseData.obj;
                        for (var i = 0; i < array.length; i++) {
                            var script = undefined;
                            script = getScriptObj(array[i].type || -1);
                            array[i].isParent = array[i].parChl == 0 ? true : false;
                            array[i].iconSkin = script.icon;
                            array[i].checked = parentNode.checked;
                        }
                        if (parentNode.checked) {
                            var newArray = [];
                            for (var i = 0; i < array.length; i++) {
                                newArray.push({
                                    parentPath: array[i].parentPath,
                                    path: array[i].path,
                                    name: array[i].name,
                                    iconSkin: array[i].iconSkin,
                                    isParent: array[i].isParent,
                                    gitProjectId: array[i].gitProjectId,
                                    fullDir: array[i].isParent
                                })
                            }
                            var parentNode = rightZtree.getNodeByParam("path", parentNode.path);
                            if (parentNode) {
                                parentNode.fullDir = false;
                                rightZtree.addNodes(parentNode, newArray);
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
                    idKey: "path",
                    pIdKey: "parentPath",
                    rootPId: ""
                },
                keep: {
                    parent: true,
                    leaf: true
                },
            },
            check: {
                enable: true,
                autoCheckTrigger: true,
                chkStyle: "checkbox",
                chkboxType: {"Y": "ps", "N": "ps"},
            },
            callback: {
                beforeClick: function (treeId, treeNode) {
                    if (treeNode.isParent) {
                        leftZtree.expandNode(treeNode);
                    }
                    var id = treeNode.tId;
                    if (leftPreSelectedId) {
                        $("#" + leftPreSelectedId).removeClass("selectedNode")
                    }
                    $("#" + id).addClass("selectedNode");
                    leftPreSelectedId = id;
                    return true;
                },
                onCheck: function (event, treeId, treeNode) {
                    var leftPath = treeNode.path;
                    var rightPath = leftPath;
                    if (treeNode.checked) {
                        var rightParentPath = treeNode.parentPath;
                        var rightParentNode = rightZtree.getNodeByParam("path", rightParentPath);
                        var newNode = {
                            parentPath: treeNode.parentPath,
                            path: treeNode.path,
                            name: treeNode.name,
                            iconSkin: treeNode.iconSkin,
                            isParent: treeNode.isParent,
                            gitProjectId: treeNode.gitProjectId,
                            fullDir: treeNode.isParent && !treeNode.zAsync
                        };
                        if (rightParentNode || rightParentPath == "") {
                            var toDoArray = new Array();
                            var parentPath = newNode.path;
                            var parentNode = rightParentNode;
                            var thisNode = rightZtree.getNodeByParam("path", parentPath);
                            if (!thisNode) {
                                var index = 0;
                                if (!newNode.isParent) {
                                    var nodes = rightZtree.getNodesByFilter(function (node) {
                                        return node.parentPath == newNode.parentPath && node.isParent == true;
                                    });
                                    index = nodes && nodes.length || 0;
                                }
                                parentNode = rightZtree.addNodes(rightParentNode, index, [newNode])[0];
                            } else {
                                parentNode = thisNode;
                            }


                            if (readyToDo.containsKey(newNode.path)) {
                                toDoArray = toDoArray.concat(readyToDo.get(newNode.path))
                                readyToDo.remove(newNode.path);
                                while (toDoArray.length > 0) {
                                    var node = toDoArray.shift();
                                    if (node.parentPath != parentPath) {
                                        parentPath = node.parentPath;
                                        parentNode = rightZtree.getNodeByParam("path", parentPath);
                                    }
                                    var thisNode = rightZtree.getNodeByParam("path", node.path);
                                    if (!thisNode) {
                                        var index = 0;
                                        if (!node.isParent) {
                                            var nodes = rightZtree.getNodesByFilter(function (filterNode) {
                                                return filterNode.parentPath == node.parentPath && filterNode.isParent == true;
                                            });
                                            index = nodes && nodes.length || 0;
                                        }
                                        rightZtree.addNodes(parentNode, index, [node]);
                                    }
                                    if (readyToDo.containsKey(node.path)) {
                                        toDoArray = toDoArray.concat(readyToDo.get(node.path))
                                        readyToDo.remove(node.path)
                                    }
                                }
                            }

                        } else {
                            if (!readyToDo.containsKey(newNode.parentPath)) {
                                readyToDo.put(newNode.parentPath, new Array())
                            }
                            var array = readyToDo.get(newNode.parentPath);
                            array.push(newNode);
                        }

                    } else {
                        var rightNode = rightZtree.getNodeByParam("path", rightPath);
                        if (rightNode && !needShowPaths.containsKey(rightPath)) {
                            rightZtree.removeNode(rightNode)
                        }
                    }
                }
            },
        }
        if (leftZtree) {
            leftZtree.destroy();
        }
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScripsByDependency.ajax", {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath
        }, null, function (node, data) {
            if (data && data.obj) {
                var zNodeArr = data.obj;
                var zNodes = [];
                var initRightNodes = [];
                for (var index = 0; index < zNodeArr.length; index++) {
                    var zNode = zNodeArr[index];
                    if (!zNode.type) {
                        zNode.type = -1;
                    }
                    var script = undefined;
                    script = getScriptObj(zNode.type);
                    if (zNode.deleted != 1) {
                        zNodes.push({
                            parentPath: zNode.parentPath,
                            path: zNode.path,
                            name: zNode.name || "bdp_default",
                            iconSkin: script.icon,
                            isParent: zNode.parChl == 0,
                            type: zNode.type,
                            gitProjectId: zNode.gitProjectId,
                            checked: zNode.selected == 1,
                            chkDisabled: zNode.chkDisabled && zNode.parChl == 1,
                            open: zNode.selected == 1
                        });
                    }
                    if (zNode.selected == 1) {
                        initRightNodes.push({
                            parentPath: zNode.parentPath,
                            path: zNode.path,
                            name: zNode.name || "bdp_default",
                            iconSkin: script.icon,
                            isParent: zNode.parChl == 0,
                            type: zNode.type,
                            gitProjectId: zNode.gitProjectId,
                            open: true,
                            noSurvive: zNode.deleted == 1
                        })
                    }
                    if (zNode.chkDisabled) {
                        needShowPaths.put(zNode.path, "1")
                    }
                }
                var t = $("#leftPackZtree");
                leftZtree = $.fn.zTree.init(t, setting, zNodes);
                initRightZtree(initRightNodes, gitProjectFilePath);
            }
        });
    }

    function initRightZtree(initRightNodes, selectedPath) {
        var setting = {
            view: {
                dblClickExpand: false,
                showLine: false,
                selectedMulti: false,
                initPadding: 25,
                initPaddingLeft: 10,
                addDiyDom: function (treeId, treeNode) {
                    if (treeNode.noSurvive) {
                        var a = $("#" + treeNode.tId + "_a");
                        var id = treeNode.tId + "_remove_button";
                        var button = "<span class='button remove bdp-icon bzzx-close' id='" + id + "' data-path='" + treeNode.path + "' data-id='" + treeNode.tId + "' title=''></span>";
                        a.append($(button));
                        $("#" + id).bind("click", function () {
                            var treeId = $(this).attr("data-id");
                            var node = rightZtree.getNodeByTId(treeId);
                            rightZtree.removeNode(node);
                            var path = $(this).attr("data-path");
                            var leftNode = leftZtree.getNodeByParam("path", path);
                            if (leftNode) {
                                leftZtree.checkNode(leftNode, false, true, true);
                            }
                        })
                        var nodeDiv = a.closest(".nodeDiv");
                        nodeDiv.addClass("no-survive-node");
                    }

                }
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
                }
            },
            callback: {
                beforeClick: function (treeId, treeNode) {
                    if (treeNode) {
                        if (treeNode.isParent) {
                            var realPath = treeNode.path;
                            var leftParentNode = leftZtree.getNodeByParam("path", realPath);
                            if (!leftParentNode.zAsync) {
                                leftZtree.expandNode(leftParentNode);
                            }
                            rightZtree.expandNode(treeNode);
                        }
                        var id = treeNode.tId;
                        if (rightPreSelectedId) {
                            $("#" + rightPreSelectedId).removeClass("selectedNode")
                        }
                        $("#" + id).addClass("selectedNode");
                        rightPreSelectedId = id;
                    }
                    return true;
                }
            },
        }
        if (rightZtree) {
            rightZtree.destroy();
        }
        var t = $("#rightPackZtree");
        rightZtree = $.fn.zTree.init(t, setting, initRightNodes);
    }

    //返回json字符串
    function getPacketObj() {
        var result = new Array();
        var nodes = rightZtree.getNodes();

        nodes = rightZtree.transformToArray(nodes);
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            result.push({
                path: node.path,
                parChl: node.isParent ? 0 : 1,
                fullDir: node.fullDir ? 1 : 0
            })
        }
        return result;
    }

    function saveDepend() {
        var result = getPacketObj();
        if (!result || result.length < 1) {
            $.errorMsg("必须选择至少一个依赖文件");
        }
        var gitProjectId = $("#gitProjectId").val();
        var gitProjectFilePath = $("#gitProjectFilePath").val();
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/depSave.ajax", {
            data: JSON.stringify(result),
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath
        }, null, function (node, data) {
            updateNode && updateNode({
                gitProjectId: gitProjectId,
                path: gitProjectFilePath,
                runType: data.obj == true ? 1 : 0
            })
            $.successMsg(data.message, 2000, function () {
                dependencyArt.close();
            });
        }, undefined, undefined, undefined, undefined, function () {
            $.loadingMsg("保存依赖...")
        })
    }

    function packDepend() {
        var result = getPacketObj();
        if (!result || result.length < 1) {
            $.errorMsg("必须选择至少一个依赖文件");
        }
        var gitProjectId = $("#gitProjectId").val();
        var gitProjectFilePath = $("#gitProjectFilePath").val();
        $.dialog.data("packZipData",JSON.stringify(result));
        pack2Zip && pack2Zip(gitProjectId, gitProjectFilePath);
    }

    $("#pack2Save").click(function () {
        saveDepend();
    })
    $("#pack2zip").click(function () {
        packDepend()
    })
    $("#closeDia").click(function () {
        dependencyArt.close();
    })

    function initPack(projectId, gitProjectFilePath) {
        gitProjectId = projectId;
        initLeftZtree(gitProjectId, gitProjectFilePath);
    }

    initPack($("#gitProjectId").val(), $("#gitProjectFilePath").val());
})
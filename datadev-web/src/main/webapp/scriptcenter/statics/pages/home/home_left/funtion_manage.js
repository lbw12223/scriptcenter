$(function () {
    var funSetting = {
        view: {
            dblClickExpand: false,
            showLine: false,
            selectedMulti: false
        },
        async: {
            enable: false,
            url: "/scriptcenter/funtion/getFunsByDirIdsssssssssssssssssssssssssssss.ajax",
            autoParam: ["id=dirId"],
            dataFilter: function (treeId, parentNode, responseData) {
                if (responseData && responseData.obj && responseData.obj.length > 0) {
                    var array = responseData.obj;
                    for (var i = 0; i < responseData.obj.length; i++) {
                        array[i].isParent = array[i].parChl == 0 ? true : false;
                        array[i].iconSkin = array[i].parChl == 0 ? "icon01" : icon;
                        array[i].id = array[i].parChl == 0 ? array[i].id : ("f_" + array[i].id);
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
                idKey: "id",
                pIdKey: "pId",
                rootPId: ""
            },
            keep: {
                parent: true,
                leaf: true
            },
        },
        callback: {
            beforeClick: function (treeId, treeNode) {
                if (treeNode.isParent) {
                    funZtree.expandNode(treeNode);
                }
                var id = treeNode.tId;
                if (preFunSelectedId) {
                    $("#" + preFunSelectedId).removeClass("selectedNode")
                }
                $("#" + id).addClass("selectedNode");
                preFunSelectedId = id;
                return true;
            },
            onDblClick: function (event, treeId, treeNode) {
                if (!treeNode.isParent) {
                    var id = treeNode.id;
                    id = id.split("_")[1];
                    // functionShowModal
                    commonAjaxEvents.commonPostAjax("/scriptcenter/funtion/getFunById.ajax", {id: id}, null, function (node, data) {
                        if (data.obj) {
                            var currentArt = $.dialog.open("/scriptcenter/devcenter/function_manage_detail.html", {
                                title: "函数",
                                lock: true,
                                width: "538px",
                                height: "500px",
                                opacity: 0.5,
                                esc: true,
                                resize: false,
                                close: function () {
                                }
                            });
                            $.dialog.data("currentArt", currentArt);
                            $.dialog.data("info", data.obj);
                        }
                    });
                }
                return false;
            }
        }
    };
    var preFunSelectedId;
    var icon = "icon07";
    var funZtree = undefined;
    initFunZtree();
    $("#copyFunc").click(function () {
        var textarea = document.getElementById("expText");
        var text = textarea.value;
        const input = document.createElement('input');
        input.setAttribute('readonly', 'readonly');
        input.setAttribute('value', text);
        document.body.appendChild(input);
        input.focus();
        input.setSelectionRange(0, input.value.length);
        if (document.execCommand('copy')) {
            document.execCommand('copy');
        }
        document.body.removeChild(input);
        $.successMsg("已复制至剪贴板！");
    })
    function initFunZtree() {
       $("#searchFunction").keyup(function (event) {
           if(event.keyCode == 13){
               searchFunctions();
           }
        });
       $("#searchFunctionFlag").click(function () {
           searchFunctions();
       })
        searchFunctions();
     /*   commonAjaxEvents.commonPostAjax("/scriptcenter/funtion/getFunsByDirId.ajax", {}, null, function (node, data) {
            alert("init....")
            if (data && data.obj) {
                var zNodeArr = data.obj;
                var zNodes = [];
                var initId = undefined;
                for (var index = 0; index < zNodeArr.length; index++) {
                    var zNode = zNodeArr[index];
                    if (!initId && zNode.parChl == 0) {
                        initId = zNode.id;
                    }
                    zNodes.push({
                        id: zNode.parChl == 0 ? zNode.id : ("f_" + zNode.id),
                        pId: zNode.pId,
                        name: zNode.name,
                        iconSkin: zNode.parChl == 0 ? "icon01" : icon,
                        isParent: zNode.parChl == 0 ? true : false,
                    });
                }
                var cc = $("#funcT");
                $.fn.zTree.init(cc, funSetting, zNodes);
                funZtree = $.fn.zTree.getZTreeObj("funcT");
                if (initId) {
                    var node = funZtree.getNodeByParam("id", initId);
                    funZtree.expandNode(node);
                }
            }
        });*/
    }

    function searchFunctions() {
        commonAjaxEvents.commonPostAjax("/scriptcenter/funtion/searchFunctions.ajax", {
            keyword: $.trim($("#searchFunction").val())
        }, null, function (node, data) {
            if (data && data.obj) {
                var zNodeArr = data.obj;
                var zNodes = [];
                var initId = 10000000;
                for (var index = 0; index < zNodeArr.length; index++) {
                    var zNode = zNodeArr[index];
                    if (zNode.id < initId) {
                        initId = zNode.id;
                    }
                    zNodes.push({
                        id: zNode.parChl == 0 ? zNode.id : ("f_" + zNode.id),
                        pId: zNode.pId,
                        name: zNode.name,
                        iconSkin: zNode.parChl == 0 ? "icon01" : icon,
                        isParent: zNode.parChl == 0 ? true : false,
                    });
                }
                var cc = $("#funcT");
                $.fn.zTree.init(cc, funSetting, zNodes);
                funZtree = $.fn.zTree.getZTreeObj("funcT");
                if (initId) {
                    var node = funZtree.getNodeByParam("id", initId);
                    funZtree.expandNode(node);
                }
            }
        });
    }


})
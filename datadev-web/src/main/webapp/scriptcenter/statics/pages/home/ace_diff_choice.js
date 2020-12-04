$(function () {
    var scriptArr = {
        "1": "sql",
        "2": "sh",
        "3": "python",
        "6": "java",
        "8": "xml",
        "9": "html",
        "12": "groovy",
        "13": "javascript",
        "14": "css"
    };
    var topTips = $.dialog.data("topTips");
    var leftTips = $.dialog.data("leftTips");
    var rightTips = $.dialog.data("rightTips");
    var callBack = $.dialog.data("callBack");
    var dialog = $.dialog.data("dialog");
    var leftackDetailMap = new Map();
    var rightPackDetailMap = new Map();
    var rootPath = "root";

    $("#topTips span").text(topTips);
    $("#leftTips span").text(leftTips);
    $("#rightTips span").text(rightTips);
    initChoice();
    var differ = initEditor();
    initEvent();

    function initEditor() {
        if ($("#canEdit").val() == "true") {
            $(".pack-zip-detail").remove();
            $(".zip-tips").remove();
            var differ = new AceDiff({
                mode: "ace/mode/sql",
                theme: "ace/theme/tomorrow_night",
                showConnectors: true,
                showDiffs: true,
                left: {
                    copyLinkEnabled: false,
                    editable:false
                },
                right: {
                    copyLinkEnabled: false,
                    editable:false
                }
            });
            $("#topTips span").text(topTips);
            $("#leftTips span").text(leftTips);
            $("#rightTips span").text(rightTips);
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/getMergeContent.ajax", {
                gitProjectId: $("#gitProjectId").val(),
                gitProjectFilePath: $("#gitProjectFilePath").val(),
                version: $("#lastVersion").val(),
                oldVersion: $("#currentVersion").val()
            }, $(".choose-btn"), function (node, data) {
                differ.getEditors().left.setValue(data.obj.oldContent || "")
                differ.getEditors().right.setValue(data.obj.hbaseContent || "")
                differ.getEditors().left.focus();
                differ.getEditors().right.focus();
                differ.getEditors().left.clearSelection();
                differ.getEditors().right.clearSelection();
            })
            return differ;
        } else {
            if($("#currentRelationDependencyId").val()){
                $("#leftPackZipDetail").show();
                $("#leftZipTips").hide();
                packDetail($("#currentRelationDependencyId").val(),$("#leftPackZipDetail"),leftackDetailMap);
            } else {
                $("#leftZipTips").html("（该脚本类型不支持预览，编辑！）").css({
                    "font-size": "16px",
                    "text-align": "center",
                    "margin-top": "15%"
                }).show();
                $("#leftPackZipDetail").hide();
            }
            if($("#lastRelationDependencyId").val()){
                $("#rightPackZipDetail").show();
                $("#rightZipTips").hide();
                packDetail($("#lastRelationDependencyId").val(),$("#rightPackZipDetail"),rightPackDetailMap);
            } else {
                $("#rightZipTips").html("（该脚本类型不支持预览，编辑！）").css({
                    "font-size": "16px",
                    "text-align": "center",
                    "margin-top": "15%"
                }).show();
                $("#rightPackZipDetail").hide();
            }
            return null;
        }
    }

    function initEvent() {
        $("#flex-container").on("click", ".choose-btn", function () {
            var choice = $(this).attr("data-choice");
            var version = choice == "left" ? $("#currentVersion").val() : $("#lastVersion").val();
            var dependencyId = choice == "left" ? $("#currentRelationDependencyId").val() : $("#lastRelationDependencyId").val();
            var value = choice == "left" ? differ && differ.getEditors().left.getValue()|| "": differ && differ.getEditors().right.getValue()|| "";
            var isLeft = choice == "left";
            var modifiedStatus = $("#modifiedStatus").val();
            var lastChoice = $("#choice").val() == -1?"left":"right";
            if(modifiedStatus != 1){
                //没有修改的时候直接覆盖
                //未修改
                //isLeft true:选择左边   value：选择的脚本内容   version：选择的版本号
                callBack && callBack(isLeft, value, version,dependencyId);
                dialog && dialog.close();
            }else if( lastChoice !=choice){
                //与之前选择不一样，提示覆盖
                var html = getOverwriteHtml();
                $.bdpMsg({
                    title: "提示",
                    mainContent: html,
                    width: 400,
                    buttons: [
                        {
                            text: "确定",
                            event: function () {
                                $.removeMsg();
                                //isLeft true:选择左边   value：选择的脚本内容   version：选择的版本号
                                callBack && callBack(isLeft, value, version,dependencyId);
                                dialog && dialog.close();
                            },
                        },
                        {
                            text: "取消",
                            event: function () {
                                $.removeMsg();
                            },
                            btnClass: 'bdp-btn-primary',
                            datadevFocus:true
                        }
                    ]
                })
            }else {
                //有修改，与之前选择一样，选择继续编辑，不覆盖内容
                dialog && dialog.close();
            }


        })
    }
    function initChoice() {
        if($("#choice").val() == -1){
            $(".choose-btn[data-choice='left']").text("继续编辑");
            $(".choose-btn[data-choice='right']").text("编辑此版本");
        }else if($("#choice").val() == 1){
            $(".choose-btn[data-choice='left']").text("编辑此版本");
            $(".choose-btn[data-choice='right']").text("继续编辑");
        }else {
            $(".choose-btn").text("编辑此版本");
        }
    }
    function getOverwriteHtml() {
        var choice = $("#choice").val();
        var tipsStr = undefined;
         if(choice == -1){
            tipsStr = "调度版本中已修改内容将覆盖，请确定是否继续？"
        }else {
            tipsStr = "开发平台版本中已修改内容将覆盖，请确定是否继续"
        }
        var html = "<div style='padding: 10px;margin-top: 20px;text-align: center'><span>"+tipsStr+"</span></div>";
        return html;
    }
    function packDetail(dependencyId,jqNode,packDetailMap) {
        packDetailMap.clear();
        $(".pack-tree-ul",jqNode).empty();
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScriptsByDependencyId.ajax", {
            dependencyId: dependencyId
        }, null, function (node, data) {
            if (data.obj) {
                childNodes(data.obj,jqNode,packDetailMap);
                var root = packDetailMap.get(rootPath);
                var chis = root.childrens;
                var lis = "";
                for (var i = 0; i < chis.length; i++) {
                    lis += getUlLis(chis[i], true);
                }
                $(".pack-tree-ul",jqNode).append(lis);
            }
        })
    }

    function childNodes(nodes,jqNode,packDetailMap) {
        packDetailMap.put(rootPath, {
            childrens: []
        })
        for (var i = 0; i < nodes.length; i++) {
            var parent = nodes[i].parentPath;
            var parentNode = packDetailMap.get(rootPath + parent);
            var childrens = parentNode.childrens;
            if (!childrens) {
                parentNode.childrens = [];
                childrens = parentNode.childrens;
            }
            var index = 0;
            for (; index < childrens.length; index++) {
                if (childrens[index].parChl == 1) {
                    break;
                }
            }
            childrens.splice(index, 0, nodes[i]);
            nodes[i].parent = parentNode;
            packDetailMap.put((rootPath + nodes[i].path), nodes[i]);
        }
    }

    function getUlLis(node, isBorder) {
        var childrens = node.childrens;
        var liClass = "open-li ";
        liClass += (isBorder ? "table-border  " : "");
        liClass += (node.parChl == 0 ? "dir-li" : "file-li");
        var li = "<li data-path='" + node.path + "' data-parent='" + node.parentPath + "' class='" + liClass + "'>"
        var nodeDiv = getNodeDiv(node);
        li += nodeDiv;
        li += "<ul>";
        for (var i = 0; childrens && i < childrens.length; i++) {
            li += getUlLis(childrens[i]);
        }
        li += "</ul></li>"
        return li;
    }

    function getNodeDiv(node) {
        if (!node.type || node.type == -1) {
            node.type = -3;
        }
        var script = getScriptObj(node.type);
        var iconClass = "bdp-icon " + script.iconClass;
        var version = node.version || "";//去null
        var lastVersion = node.lastVersion || "";//去null
        var treeNodeTd = "<div style='padding-left: " + ((node.tableLevel || 0) * 25) + "px' class='script-file'><span class='script-icon  " + iconClass + "'></span><span class='script-name'>" + node.name + "</span></div>";
        var zipVersionTd = "<div class='version ' data-version='" + version + "'><span>" + version + "</span></div>";
        var lastVersionTd = "<div class='version lastVersion " + (node.deleted == 1 ? "noVersion" : "") + "' data-version='" + lastVersion + "'><span>" + lastVersion + "</span></div>";
        var lastModifiedTd = "<div class='modified'>" + (node.lastModified || "") + "</div>";
        var lastMenderTd = "<div class='mender'>" + (node.modifier || "") + "</div>";
        return "<div class='nodeDiv'>" + treeNodeTd + zipVersionTd + lastVersionTd + lastMenderTd + lastModifiedTd + "</div>";
    }

});

var editor;
$(function () {
    var openScriptContent = $.dialog.data("openScriptContent");
    var packDetailMap = new Map();
    var rootPath = "root";
    var initScriptContent = {
        initDom: function () {
            if ($("#canEdit").val() * 1 == 1) {
                var scriptArr = {"1": "sql", "2": "sh", "3": "python"};
                //初始化对象

                editor = ace.edit("code");
                //设置风格和语言（更多风格和语言，请到github上相应目录查看）
               

                if(HOME_COOKIE.getColorCookie() === "white"){
                    editor.setTheme("ace/theme/chrome");
                }

                var scriptType = $("#scriptType").val();
                var language = scriptArr[scriptType];

                editor.session.setMode("ace/mode/" + language);
                //字体大小
                editor.setFontSize(14);
                //设置只读（true时只读，用于展示代码）
                editor.setReadOnly(true);
                //自动换行,设置为off关闭
                editor.setOption("wrap", "free");
                //启用提示菜单
                ace.require("ace/ext/language_tools");
                editor.setOptions({
                    enableBasicAutocompletion: true,
                    enableSnippets: false,
                    enableLiveAutocompletion: true
                });
                editor.focus();
                editor.clearSelection()
            }

            $(".downLoadData").click(function () {
                downloadScript();
            })
            $("#downLoadData").click(function () {
                downloadScript();
            })
            $("#downLoadDirData").click(function () {
                downloadScript();
            })
            $("#copyContentent").click(function () {
                var text = editor.getValue();
                var input = document.createElement('textarea');
                input.textContent = text;
                input.setAttribute('readonly', 'readonly');
                document.body.appendChild(input);
                input.focus();
                input.setSelectionRange(0, input.value.length);
                if (document.execCommand('copy')) {
                    document.execCommand('copy');
                }
                document.body.removeChild(input);
                $.successMsg("已复制至剪贴板！");
            })
            $("#treeDetailTableBody").on("click", ".version", function () {
                var li = $(this).parents("tr");
                var gitProjectId = li.attr("data-project");
                var gitProjectFilePath = li.attr("data-path");
                var version = $(this).attr("data-version");
                openScriptContent && openScriptContent(gitProjectId, gitProjectFilePath, version)
            })
        },
        packDetail: function () {
            if ($("#relationDependencyId").val() && $("#relationDependencyId").val() > 0) {
                packDetailMap.clear();
                $("#packTreeUl").empty();
                var dependencyId = $("#relationDependencyId").val();
                commonAjaxEvents.commonPostAjax("/scriptcenter/script/getScriptsByDependencyId.ajax", {
                    dependencyId: dependencyId
                }, null, function (node, data) {
                    if (data.obj) {
                        initScriptContent.childNodes(data.obj);
                        var root = packDetailMap.get(rootPath);
                        var chis = root.childrens;
                        var lis = "";
                        for (var i = 0; i < chis.length; i++) {
                            lis += initScriptContent.getUlLis(chis[i], true);
                        }
                        $("#packTreeUl").append(lis);
                    }
                })
            }
        },
        childNodes: function (nodes) {
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
                childrens.push(nodes[i]);
                nodes[i].parent = parentNode;
                packDetailMap.put((rootPath + nodes[i].path), nodes[i]);
            }
        },
        getUlLis: function (node, isBorder) {
            var childrens = node.childrens;
            var liClass = "open-li ";
            liClass += (isBorder ? "table-border  " : "");
            liClass += (node.parChl == 0 ? "dir-li" : "file-li");
            var li = "<li data-path='" + node.path + "' data-parent='" + node.parentPath + "' class='" + liClass + "'>"
            var nodeDiv = initScriptContent.getNodeDiv(node);
            li += nodeDiv;
            li += "<ul>";
            for (var i = 0; childrens && i < childrens.length; i++) {
                li += initScriptContent.getUlLis(childrens[i]);
            }
            li += "</ul></li>"
            return li;
        },
        getNodeDiv: function (node) {
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
        },
        initEvent: function () {
            // $("#packTreeUl").on("click", "li.file-li span.script-name", function () {
            //     var li = $(this).closest("li");
            //     var gitProjectId = $("#gitProjectId").val();
            //     var gitProjectFilePath = li.attr("data-path");
            //     parent.openScript && parent.openScript(gitProjectId, gitProjectFilePath)
            // })


            $("#packTreeUl").on("click", ".version span", function () {
                var versionDiv = $(this).closest(".version");
                if ($(versionDiv).hasClass("noVersion")) {
                    return;
                }
                var version = $(versionDiv).attr("data-version");
                var li = $(versionDiv).closest("li");
                var gitProjectFilePath = li.attr("data-path");
                var gitProjectId = $('#gitProjectId').val();
                openScriptContent(gitProjectId, gitProjectFilePath, version)
                return false
            })
        }

    }
    initScriptContent.initDom();
    initScriptContent.packDetail();
    initScriptContent.initEvent();
})

function downloadScript() {
    var path = $("#gitProjectFilePath").val();
    var gitProjectId = $("#gitProjectId").val();
    var version = $("#version").val();
    window.onbeforeunload = null;
    var gitProjectFilePath = encodeURIComponent(encodeURIComponent(path));
    var form_id = "script_form_downLoad";
    if ($("#" + form_id).length > 0) {
        $("#" + form_id).remove();
    }
    var temp = document.createElement("form");
    temp.action = "/scriptcenter/api/downloadScriptNoAuth.ajax" + "?gitProjectId=" + gitProjectId + "&gitProjectFilePath=" + gitProjectFilePath + "&version=" + version;
    temp.method = "post";
    temp.style.display = "none";
    temp.id = form_id;
    document.body.appendChild(temp);
    temp.submit();
    $("#" + form_id).remove();
    window.setTimeout(function () {
        window.onbeforeunload = function () {
            return "确定离开页面吗？";
        }
    }, 1000)
}

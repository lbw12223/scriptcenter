$(function () {

    var templateArt = $.dialog.data("templateArt");
    var addScript = $.dialog.data("addScript");//直接就可以调用啦
    var addScriptDirNode = $.dialog.data("addScriptDirNode");//直接就可以调用啦
    var selectedProjectId = $.dialog.data("selectedProjectId");//直接就可以调用啦
    var openScript = $.dialog.data("openScript");//直接就可以调用啦
    var removeScriptTabByKey = $.dialog.data("removeScriptTabByKey");
    var getKey = $.dialog.data("getKey");
    var saveTemplate = $.dialog.data("saveTemplate");
    var scriptContentMap = {};
    var ADD_TEMPLATE_CLASS = "add-template";
    var searchTemplateUrl = "/scriptcenter/scriptTemplate/getTemplates.ajax";
    $(document).ready(function () {
        init_template(true);
        initEvent();
    })
    var htmlUtil = {
        encodeNode: undefined,
        htmlEncode: function (html) {
            //1.首先动态创建一个容器标签元素，如DIV
            if (!this.encodeNode) {
                this.encodeNode = document.createElement("div");
            }
            //2.然后将要转换的字符串设置为这个元素的innerText(ie支持)或者textContent(火狐，google支持)
            (this.encodeNode.textContent != undefined) ? (this.encodeNode.textContent = html) : (this.encodeNode.innerText = html);
            //3.最后返回这个元素的innerHTML，即得到经过HTML编码转换的字符串了
            var output = this.encodeNode.innerHTML;
            return output;
        }
    }

    //如果支持localstorge，优先使用localstorge,否则使用cookie
    function storgeContent(key, value) {
        if (window.localStorage) {
            var storage = window.localStorage;
            if (value) {
                storage.setItem(key, value);
            } else {
                return storage.getItem(key);
            }
        } else {
            if (value) {
                $.cookie(key, JSON.stringify(value), {expires: 3});
            } else {
                return $.cookie(key);
            }
        }
    }

    function getShareTemplateKey() {
        return "DATADEV_SHARE_TEMPLATE";
    }

    function initEvent() {
        $(document).on("mouseenter mouseleave mousemove", ".pop-title", function (event) {
            var left = event.pageX + 8, top = event.pageY + 8;
            var ele = event.target;
            var title = $(this).attr("data-title");
            var type = event.type;
            if (type == 'mouseenter') {
                ele.title = '';
                if (title != null) {
                    var showEle = $('<div></div>', {text: title, class: 'showTitleBox'}).css({
                        top: top,
                        left: left,
                    })
                    showEle.appendTo('body');
                }
            } else if (type == "mouseleave" || type == 'mouseout') {
                $('.showTitleBox').remove();
            } else if (type == 'mousemove') {
                $('.showTitleBox').css({
                    top: top + 10,
                    left: left
                })
            }
            return false;
        })
    }

    function isNewTemplate(time) {
        if (time) {
            var now = new Date().getTime();
            var diff = now - time;
            var day = diff / 1000 / 3600 / 24;
            return day && day < 7;
        }
        return false;
    }

    window.init_template=init_template;

    function init_template(isStorge) {
        //请求，然后将结果展示到template-div里
        var postData = {
            scriptType: $("#scriptType").val(),
            key: $("#templateSearchKey").val()
        };
        if ($("#pythonType").val()) {
            postData.pythonType = $("#pythonType").val();
        }
        var sharedTemplateArray = [];
        var sharedTemplateIdArray = [];
        commonAjaxEvents.commonPostAjax(searchTemplateUrl, postData, $("#templateSearch"), function (node, data) {
            $(".template-div").empty();
            var obj = data.obj;
            var html = "";
            if (obj) {
                scriptContentMap = {};
                for (var index = 0; index < obj.length; index++) {
                    var icon = "";
                    if (obj[index].scriptType == "1") {
                        icon = "ide-left-sql";
                    } else if (obj[index].scriptType == "2") {
                        icon = "ide-left-shell";
                    } else {
                        icon = "ide-left-python";
                    }
                    if (obj[index].templateFrom == 1) {
                        sharedTemplateArray.push({
                            templateId: obj[index].id,
                            templateName: htmlUtil.htmlEncode(obj[index].name)
                        });
                        sharedTemplateIdArray.push(obj[index].id);
                    }
                    var isNew = isNewTemplate(obj[index].created);
                    var isTop = obj[index].showOrder && obj[index].showOrder * 1 > 0;
                    var tipsClass = (obj[index].templateType == 0 ? "template-system" : isTop || isNew ? "template-top" : "template-common");
                    var tipsContent = (obj[index].templateType == 0 ? "系统模板" : obj[index].showOrder && obj[index].showOrder > 0 ? "置顶" : "");
                    var newTipsContent = isNew ? "<span class='bdp-icon ide-new newTemplateTips'></span>" : "";
                    var toolHtml = obj[index].templateType == 0 ? "" :
                        ('       <div class="templateType-tool-content ' + (obj[index].templateFrom == 0 ? "from-own-template" : "from-share-template") + '">' +
                            '           <span class="bdp-icon ide-edit editTemplateBtn pop-title" data-title="编辑"></span> ' +
                            '           <span class="bdp-icon ide-delete deleteTemplateBtn pop-title" data-title="删除"></span> ' +
                            '           <span class="bdp-icon ide-share shareTemplateBtn pop-title" data-title="分享"></span> ' +
                            '           <span class="bdp-icon ' + (isTop ? "ide-qxzd" : "ide-zd") + ' topTemplateBtn pop-title" data-title="' + ((isTop ? "取消置顶" : "置顶")) + '"></span> ' +
                            '       </div>');
                    html +=
                        '<div class="template-container" data-word-order="' + obj[index].wordOrder + '" data-id="' + obj[index].id + '" data-top="' + (isTop ? 1 : 0) + '" data-template-type = "' + obj[index].templateType + '" data-scriptType="' + obj[index].scriptType + '" data-pythonType="' + (obj[index].pythonType || 0) + '">' +
                        '   <div class="template-tips-wrapper"> ' +
                        '       <div class="templateType-tips ' + tipsClass + '">' + (tipsContent ? getTopTips(tipsContent) : "") + newTipsContent + '</div>' +
                        '   </div> ' +
                        '   <div class="template-title">' +
                        '       <div class="bdp-icon ' + icon + '"></div>' +
                        '       <div class="template-name">' +
                        '           <span class="pop-title" data-title="' + htmlUtil.htmlEncode(obj[index].name) + '">' + htmlUtil.htmlEncode(obj[index].name) + '</span>' +
                        '       </div>' +
                        '   </div> ' +
                        '   <div class="template-border"></div>' +
                        '   <div class="template-desc"> ' +
                        '       <span>' + htmlUtil.htmlEncode(obj[index].desc == undefined ? "" : obj[index].desc) + '</span> ' +
                        '   </div> ' +
                        '   <div class="template-creator"> ' +
                        '       <span>' + (obj[index].creator ? ("创建人：" + obj[index].creator) : "") + '</span> ' +
                        '   </div> ' + toolHtml +
                        '</div>';
                    scriptContentMap[obj[index].id] = obj[index];
                }
                html +=
                    '<div class="template-container ' + ADD_TEMPLATE_CLASS + '">' +
                    '<span class="bdp-icon ide-tjmb"></span>' +
                    '</div>';
                $(".template-div").append(html);
                if (isStorge) {
                    try {
                        var deleteTips = "";
                        var arrayStr = storgeContent(getShareTemplateKey());
                        if (arrayStr.length > 0) {
                            var oldShareTemplatesArray = JSON.parse(arrayStr);
                            for (var index = 0; index < oldShareTemplatesArray.length; index++) {
                                var oldTemplate = oldShareTemplatesArray[index];
                                if ($.inArray(oldTemplate.templateId, sharedTemplateIdArray)) {
                                    continue;
                                }
                                deleteTips += oldTemplate.templateName + ",";
                            }
                            if (deleteTips.length > 0) {
                                deleteTips = deleteTips.substring(0, deleteTips.length - 1);
                                deleteTips = "分享模板:" + deleteTips + "已被删除！";
                                $.successMsg(deleteTips)
                            }
                        }
                    } catch (e) {
                        console.log(e)
                    }
                    storgeContent(getShareTemplateKey(), JSON.stringify(sharedTemplateIdArray));
                }
            }
        })
    }

    $(".template-div").on("click", ".template-container", function () {
        if ($(this).hasClass(ADD_TEMPLATE_CLASS)) {
            toAddScript(this);
        } else {
            var selectedContainer = $("#template-body").find(".selected");
            if (selectedContainer) {
                selectedContainer.removeClass("selected");
            }
            $(this).addClass("selected");
        }
    })
    $(".template-div").on("dblclick", ".template-container", function () {
        toAddScript(this);
    })
    $("#closeDia").click(function () {
        templateArt.close();
    })
    $('#templateSearchKey').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            //回车执行查询
            $("#templateSearch").click();
        }
    });
    $("#templateSearch").click(function () {
        init_template();
    });
    $("#template-ok").click(function () {
        //获取选中的
        var selectedContainer = $("#template-body").find(".selected");
        if (selectedContainer && selectedContainer.length > 0) {
            // $(selectedContainer).attr("data-id");
            toAddScript(selectedContainer);
        }
    });
    $("#templateWrapper").on("click", ".templateType-tool-content span", function () {
        var toolNode = $(this);
        var containerNode = $(toolNode).closest(".template-container");
        var templateId = containerNode.attr("data-id");
        if (toolNode.hasClass("editTemplateBtn")) {
            toEditTemplate(templateId);
        } else if (toolNode.hasClass("deleteTemplateBtn")) {
            toDeleteTemplate(templateId);
        } else if (toolNode.hasClass("shareTemplateBtn")) {
            console.log("ready to enter toShareTemplate...");
            toShareTemplate(templateId);
        } else if (toolNode.hasClass("topTemplateBtn")) {
            //目前置顶 表示取掉置顶  目前非置顶 表示要置顶
            toTopTemplate(templateId, toolNode.hasClass("ide-zd"));
        }
    })

    function getTopTips(tipsContent) {
        return '<span class="tips-span">' + tipsContent + '</span>';
    }

    function toEditTemplate(templateId) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/getShareInfos.ajax", {
            templateId: templateId
        }, null, function (node, data) {
            var obj = data.obj;
            if (obj.gitProjectId && obj.gitProjectFilePath) {
                openScript && openScript(obj.gitProjectId, obj.gitProjectFilePath, "模板-" + obj.name, null, true);
            }
            templateArt.close();
        })
    }

    function toDeleteTemplate(templateId) {
        var html = "<div style='padding-top: 10px'><span>是否确定要删除模板？</span></div>";
        $.bdpMsg({
            title: "删除模板提示",
            mainContent: html,
            width: "300px",
            buttons: [
                {
                    text: "确认",
                    event: function () {
                        commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/deleteTemplate.ajax", {
                            templateId: templateId
                        }, null, function (node, data) {
                            $(".template-container[data-id='" + templateId + "']").remove();
                            if (data.obj.gitProjectId && data.obj.gitProjectFilePath && getKey && removeScriptTabByKey) {
                                removeScriptTabByKey(getKey(data.obj.gitProjectId, data.obj.gitProjectFilePath));
                            }
                            $.successMsg(data.message);
                            $.removeMsg();
                        })
                    },
                },
                {
                    text: "取消",
                    event: function () {
                        $.removeMsg();
                    },
                    btnClass: 'bdp-btn-primary'
                }
            ]
        })
    }

    function toShareTemplate(templateId) {
        console.log("enter toShareTemplate...");

        var url = "/scriptcenter/devcenter/shareTemplate.html?templateId=" + templateId ;
        var shareTemplateArt = $.dialog.open(url, {
            title: "分享模板",
            lock: true,
            width: "600px",
            height: "524px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("shareTemplateArt", shareTemplateArt);

    }

    function toTopTemplate(templateId, toTop) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/topTemplate.ajax", {
            templateId: templateId,
            toTop: toTop
        }, null, function (node, data) {

            var systemContainers = $(".template-container[data-template-type='0']");
            var customContainers = $(".template-container[data-template-type='1']");
            var targerContainer = $(".template-container[data-id='" + templateId + "']");
            if (toTop) {
                var topTips = getTopTips("置顶");
                $(".templateType-tips", targerContainer).removeClass("template-common").addClass("template-top").prepend(topTips);
                $(".topTemplateBtn", targerContainer).removeClass("ide-zd").addClass("ide-qxzd").attr("data-title", "取消置顶");
                targerContainer.attr("data-top", 1);
                var systemSize = systemContainers.length;
                if (systemSize == 0) {
                    targerContainer.prependTo($(".template-div"));
                } else {
                    targerContainer.insertAfter($(".template-container[data-template-type='0']:last"));
                }
            } else {
                targerContainer.attr("data-top", 0);
                $(".tips-span", targerContainer).remove();
                $(".topTemplateBtn", targerContainer).removeClass("ide-qxzd").addClass("ide-zd").attr("data-title", "置顶");
                var customTemplateSize = customContainers.length;
                if (customTemplateSize == 0) {
                    targerContainer.appendTo($(".template-div"));
                } else {
                    var targetOrder = targerContainer.attr("data-word-order");
                    var index = 0;
                    for (; index < customTemplateSize; index++) {
                        var customContainer = $(customContainers[index]);
                        var templateOrder = customContainer.attr("data-word-order");
                        if (targetOrder * 1 < templateOrder * 1 && customContainer.attr("data-top") == 0) {
                            break;
                        }
                    }
                    if (index == 0) {
                        targerContainer.insertBefore($(customContainers[0]));
                    } else {
                        index--;
                        targerContainer.insertAfter($(customContainers[index]));
                    }
                }
            }
        })

    }

    function toAddScript(selectedContainer) {
        if ($(selectedContainer).hasClass(ADD_TEMPLATE_CLASS)) {
            //创建模板
            var scriptType = $("#scriptType").val();
            var pythonType = $("#pythonType").val();
            var script = getScriptObj(scriptType, pythonType);
            addScript(script, null, selectedProjectId, script.default || "", true, null);
            templateArt.close();
        } else {
            var id = $(selectedContainer).attr("data-id");
            var dataIndex = $(selectedContainer).attr("data-scriptType");
            var pythonType = $(selectedContainer).attr("data-pythonType");
            var script = getScriptObj(dataIndex, pythonType);
            addScript(script, addScriptDirNode, selectedProjectId, "", false, id);
            templateArt.close();
        }
    }

});

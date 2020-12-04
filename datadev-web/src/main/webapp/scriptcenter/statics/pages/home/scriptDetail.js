var getInfoUrl = "/scriptcenter/scriptFile/getInfo.ajax";
var updateDescriptionUrl = "/scriptcenter/scriptFile/saveDescription.ajax";

function initInfo(gitProjectId, gitProjectFilePath, version, isShow, md5,parentPath,fileName) {

    if (gitProjectId) {
        $("#gitProjectId").val(gitProjectId);
    }
    if (gitProjectFilePath) {
        $("#gitProjectFilePath").val(gitProjectFilePath).trigger("change")
    }
    if (version) {
        $('#version').val(version)
    }
    if(fileName){
        $("#fileName").val(fileName)
    }
    if(parentPath){
        $("#gitProjectDirPath").val(parentPath)
    }

    if (isShow == 0) {
        $('#isShow').val(isShow);
        initLocation();

    }
    if (md5) {
        $("#fileMd5").val(md5)
    }
    commonAjaxEvents.commonPostAjax(getInfoUrl, {
        gitProjectId: $("#gitProjectId").val(),
        gitProjectFilePath: $("#gitProjectFilePath").val()
    }, null, function (node, data) {
        if (data && data.obj) {
            // $("#script-detail-app").val(data.obj.applicationName)
            setDetailAttr(data.obj);
            var startShellPath = data.obj.startShellPath;
            showArgs(undefined, startShellPath);
            if ($("#online-history").data("init") && $("#online-history").data("init").isInit) {
                $("#online-history").data("init").init();
            }
            if ($("#operate-history").data("init") && $("#operate-history").data("init").isInit) {
                $("#operate-history").data("init").init();
            }
            if ($("#run-history").data("init") && $("#run-history").data("init").isInit) {
                $("#run-history").data("init").init();
            }
        }
    })
}
function setDetailAttr(obj) {
    $("#script-detail-creator").html(obj.creatorName);
    $("#script-detail-created").html(obj.createdStr);
    $("#script-detail-project").html(obj.applicationName);
    $("#isShow").val(obj.isShow);
    if(isTemplate()){
        $("#script-detail-file-path").text(obj.templateName);
    } else if ($("#isShow").val() == 1) {
        var key = getKey();
        var activeWindow = parent && parent.datadevInit && parent.datadevInit.getActiveWindow(key);
        if (activeWindow && activeWindow.activeLi && activeWindow.activeLi.attr("data-title")) {
            $("#script-detail-file-path").text(activeWindow.activeLi.attr("data-title"))
        } else {
            $("#script-detail-file-path").text(obj.gitProjectFilePath);
        }
    } else {
        $("#script-detail-file-path").text(obj.gitProjectFilePath);
    }
    if(isTemplate()){
        $("#script-detail-description").val(obj.templateDesc)
    }else {
        $("#script-detail-description").val(obj.description);
    }
    $("#gitProjectDirPath").val(obj.gitProjectDirPath);
}

$(function () {
    $("#script-detail-description").blur(function () {
        commonAjaxEvents.commonPostAjax(
            updateDescriptionUrl,
            {
                gitProjectId: $("#gitProjectId").val(),
                gitProjectFilePath: $("#gitProjectFilePath").val(),
                description: $("#script-detail-description").val()
            }
            , null, function (node, data) {
                // $.successMsg("修改成功!");
            });
    })
    if(isTemplate()){
        $("#script_detail_project_name").hide();
        $("#rightDiv li[target='script-detail'] a").text("模板详情")
        $(".script-detail-form h4").text("模板详情")
        $(".script-detail-form .script-detail-name").text("模板名称：")
    }
})
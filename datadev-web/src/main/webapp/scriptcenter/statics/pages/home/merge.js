var differ;
$(function () {

    var win = artDialog.open.origin;
    var gitProjectId = $("#gitProjectId").val();
    var gitProjectFilePath = $("#gitProjectFilePath").val();
    var key = win.getKey(gitProjectId, gitProjectFilePath);
    var color = HOME_COOKIE.getColorCookie()

    var theme= color === "white" ? "chrome" : "tomorrow_night";
    differ = new AceDiff({
        mode: "ace/mode/sql",
        theme: "ace/theme/" + theme,
        showConnectors: true
    });
    var leftTips=$.dialog.data("leftTips") ||"本地代码";
    var rightTips=$.dialog.data("rightTips")||"远程代码";
    $("#leftTips").text(leftTips)
    $("#rightTips").text(rightTips)
    var contentObj = win.scriptMergeContentMap.get(key);
    if (contentObj.remoteVersion) {
        var data = {
            gitVersion: contentObj.remoteVersion,
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath
        };
        if (contentObj.localVersion) {
            data.version = contentObj.localVersion;
        }
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/getMergeContent.ajax", data, null, function (node, data) {
            differ.getEditors().left.setValue(data.obj.hbaseContent || "")
            differ.getEditors().right.setValue(data.obj.gitContent || "")
            differ.getEditors().left.focus();
            differ.getEditors().right.focus();
            differ.getEditors().left.clearSelection();
            differ.getEditors().right.clearSelection();
        })
    } else {
        differ.getEditors().left.setValue(contentObj.local || "")
        differ.getEditors().right.setValue(contentObj.remote || "")
        differ.getEditors().left.focus();
        differ.getEditors().right.focus();
        differ.getEditors().left.clearSelection();
        differ.getEditors().right.clearSelection();
    }

    function save(content) {
        var data = {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            content: content || "",
            version: contentObj.localVersion
        };
        if(contentObj.remoteVersion){
            data.gitVersion=contentObj.remoteVersion;
        }
        win.scriptMergeContentMap.remove(key)
        win.dialogCallBack(key, data);
    }

    $("#leftButton").click(function () {
        save(differ.getEditors().left.getValue());
    })
    $("#rightButton").click(function () {
        save(differ.getEditors().right.getValue());
    })
});

function clearContent() {
    $(".acediff-editor").text("")
}

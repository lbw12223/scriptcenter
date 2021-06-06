var differ;
$(function () {

    var win = artDialog.open.origin;
    var gitProjectId = $("#gitProjectId").val();
    var scriptId = $("#scriptFileId").val();
    var scriptName = $("#fileName").val();
    var gitProjectFilePath = $("#gitProjectFilePath").val();
    var key = win.getKey(gitProjectId, gitProjectFilePath);
    var color = HOME_COOKIE.getColorCookie();
    var compareInfo = undefined;

    var theme= color === "white" ? "chrome" : "tomorrow_night";
    console.log("theme===>", theme);
    theme = "chrome";
    differ = new AceDiff({
        mode: "ace/mode/sql",
        theme: "ace/theme/" + theme,
        showConnectors: true,
        left: {
            editable: false,
            theme: "ace/theme/" + theme
        },
        right: {
            editable: false,
            theme: "ace/theme/" + theme
        }
    });
    var leftTips=$.dialog.data("leftTips") ||"本地代码";
    var rightTips=$.dialog.data("rightTips")||"线上代码";
    $("#leftTips").text(leftTips)
    $("#rightTips").text(rightTips)

    // var data = {
    //     projectSpaceId: gitProjectId,
    //     scriptId: scriptId,
    //     scriptName: scriptName
    // };

    //dataDevScriptFile
    /**
     * projectSpaceId:top.projectSpaceId
     * scriptId:$("#scriptId").val();
     * @type {{scriptId: number, projectSpaceId: number, scriptName: string}}
     */
    var data = {
        projectSpaceId: 10109,
        scriptId: 82286,
        scriptName: "python3_demo.py"
    };
    console.log("diff/scriptCompare data:", data);
    commonAjaxEvents.commonPostAjax("/scriptcenter/diff/scriptCompare.ajax", data, null, function (node, data) {
        compareInfo = data.obj.scriptPair;
        differ.getEditors().left.setValue(data.obj.scriptPair.currentInfo.content || "");
        differ.getEditors().right.setValue(data.obj.scriptPair.remoteInfo.content || "");
        differ.getEditors().left.focus();
        differ.getEditors().right.focus();
        differ.getEditors().left.clearSelection();
        differ.getEditors().right.clearSelection();
    });


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

    // 取消按钮，关闭dialog
    $("#cancelButton").click(function () {
        $.dialog.data("uplineDiff").close();
    });

    $("#nextButton").click(function(){

        // window.location.href = "/scriptcenter/devcenter/uplineArtCheck.html?templateId="
        // $.dialog.data("uplineDiff").size(880,624)


        /**
         * Long projectSpaceId ,
         Long scriptId
         * @type {string}
         */
        //这个界面只是显示一下两边的差异 ,
        //跳转下一个界面，显示任务的
        var projectSpaceId =  (top.projectSpaceId == undefined) ? 0 : top.projectSpaceId * 1;

        var url = "/scriptcenter/devcenter/uplineArtCheck.html?projectSpaceId=" + projectSpaceId+ "&scriptId=" + $("#scriptId").val() ;
        var uplineCheck = $.dialog.open(url, {
            title: "脚本上线卡点",
            lock: true,
            width: "880px",
            height: "624px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        var commitMsg = $("#gitCommitMessage").val();
        console.log("abc=====> ",commitMsg);
        var commitInfo = {
            gitProjectId: gitProjectId,
            gitProjectFilePath: gitProjectFilePath,
            commitMessage: commitMsg
        };
        $.dialog.data("uplineCheck", uplineCheck);
        $.dialog.data("commitInfo", commitInfo);
        $.dialog.data("compareInfo", commitMsg);
        //关闭“脚本对比”弹窗
    })
});

function clearContent() {
    $(".acediff-editor").text("")
}


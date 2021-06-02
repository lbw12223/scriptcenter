$(function () {
    $("#selectGIT").click(function(){
        $(".bdp-new-help-modal-backdrop").click();
        var tipsArt = $.dialog.open("/scriptcenter/project/newGitTips.html", {
            title: "使用提示",
            lock: true,
            width: "600px",
            height: "400px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("tipsArt", tipsArt);
    })

    $("#selectCODING").click(function(){
        $(".bdp-new-help-modal-backdrop").click();
        var tipsArt = $.dialog.open("/scriptcenter/project/newCodingTips.html", {
            title: "使用提示",
            lock: true,
            width: "600px",
            height: "400px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("tipsArt", tipsArt);
    })
    $("#selectLocal").click(function(){
        $(".bdp-new-help-modal-backdrop").click();
        var tipsArt = $.dialog.open("/scriptcenter/project/newLocalProject.html", {
            title: "创建本地项目",
            lock: true,
            width: "600px",
            height: "400px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("tipsArt", tipsArt);
    })

})

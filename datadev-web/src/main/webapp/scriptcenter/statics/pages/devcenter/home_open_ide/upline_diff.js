$("#leftButton").click(function(){
    //这个界面只是显示一下两边的差异 ,
    //跳转下一个界面，显示任务的

    var url = "/scriptcenter/devcenter/uplineArtCheck.html?templateId=" ;
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
    $.dialog.data("uplineCheck", uplineCheck);


})

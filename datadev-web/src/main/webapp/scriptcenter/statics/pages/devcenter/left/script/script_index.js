var lazyLocation = new Array();
function hiddenLeftRightMeun(isFromBroadCast) {
    $("#pullPushMenuContent").hide();
    $("#newScriptDiv").hide();
    $("#gitMenuDiv").hide();
    $("#rightClickScriptMenu").hide();
    $("#rightClickTargetDirMenu").hide();
    $("#rightClickMenu").hide();
    $("#mouseScriptDiv").hide();
    $("#gitMenuDiv").hide();
    $("#queueCodeDropDiv").hide();
    $('.showTitleBox').remove();
    $(".datadevSelectedText").removeClass("datadevSelectedText");
    $("#newScriptDiv").hide()

    //清除右侧右键菜单
    if(isFromBroadCast == undefined){
        QIAN_KUN.utils.broadcast("clear-right-right-meun", "clearRight")
    }

}


var frameBus = new FrameBus();
frameBus.on("clear-left-right-meun", function(data) {
    hiddenLeftRightMeun(true);
})

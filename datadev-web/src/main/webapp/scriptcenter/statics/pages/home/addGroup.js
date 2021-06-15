
$(function () {

    datadev_group_common.initGroup($("#addGroupInput"), false);
    $("#addGroupBtn").click(function () {
        debugger
        var gitGroupName = {};
        var gitProjectId = $("#gitProjectId").val();
        var gitGroupId = $("#addGroupInput").val();
        var allGroups = $("#addGroupInput").data("groupNames");
        var isSync = $("#sync").prop("checked") === true ? 1 : 0 ;
        for (var i = 0; i < allGroups.dataList.length; i++) {
            if (allGroups.dataList[i].gitGroupId == gitGroupId) {
                gitGroupName = allGroups.dataList[i].name;
                break;
            }
        }
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/addSharedWithGroups.ajax",{
            gitProjectId:gitProjectId,
            gitGroupId:gitGroupId,
            gitGroupName: gitGroupName,
            isSyncProjectSpace:isSync
        },$("#addGroupBtn"),function (node, data) {
            $.successMsg(data.obj);
            art.dialog.close();
        })
    })
    $("#closeGroupBtn").click(function () {
        art.dialog.close();
    })

    $("#addGroupInput").on("change", function () {
        var groupId = $("#addGroupInput").val();
        var res = "";
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/listAllGroupMembers.ajax",{
            gitGroupId:groupId
        },null, function (node, data) {
            /*var length = 50;
            for (var i = 0; i < data.dataList.length; i++) {
                var temp = data.dataList[i].gitMemberUserName;
                res += temp;
                for (var j = 0; j < (length - temp.length); j++) {
                    res += " ";
                }
                res += data.dataList[i].accessLevelRight;
                res += "\n";
            }*/
            for (var i = 0; i < data.dataList.length; i++) {
                var temp = data.dataList[i].gitMemberUserName;
                res += temp;
                res += "\n";
            }
            $("#sharedGroupMembers").val(res.trim());
        })
    })
})

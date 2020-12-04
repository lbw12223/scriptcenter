
$(function () {

    datadev_user_common._int($("#addUserInput"), true);
    $("#addUserBtn").click(function () {
        var gitProjectId = $("#gitProjectId").val();
        var erps = $('#addUserInput').val();
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/addProjectMember.ajax",{
            gitProjectId:gitProjectId,
            erps:erps
        },$("#addUserBtn"),function (node, data) {
            art.dialog.close();
        })
    })
    $("#closeUserBtn").click(function () {
        art.dialog.close();
    })
})

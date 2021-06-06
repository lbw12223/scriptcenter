jQuery(function () {


    var page = {
        init: function () {
            $('#gridTable').GM({
                gridManagerName: 'gridTable',
                supportAjaxPage: true,
                ajaxData: '/scriptcenter/diff/scriptTaskList.ajax',
                ajaxType: 'POST',
                supportDrag: false,
                supportCheckbox: false,
                supportAutoOrder: false,
                height: "100%",
                width: "100%",
                query: {
                    projectSpaceId: top.projectSpaceId,
                    scriptName: $("#scriptFileName").val()
                },
                supportMenu: false,
                columnData: [
                    {
                        key: 'creatorName',
                        text: '创建人',
                    },
                    {
                        key: 'taskName',
                        text: '任务名称',
                    },
                    {
                        key: 'priorityDesc',
                        text: '任务等级',
                    }
                ]
            });
        },
        initEvent: function () {
            $("#submit").click(function () {
                //1.提交内容到Git或者Coding ,(  直接覆盖)
                //2.调用调度接口push到生产
                //3.提交发布中心

                var msgRef = top.Msg ? top.Msg : window.$;

                msgRef.loadingMsg("正在提交发布！");


                var commitMsg = $("#commitMsg").val();
                var scriptFileId = $("#scriptFileId").val();
                var _projectSpaceId = top.projectSpaceId == undefined ? 0 : top.projectSpaceId;
                commonAjaxEvents.commonPostAjax("/scriptcenter/diff/submit.ajax", {
                    projectSpaceId: _projectSpaceId,
                    commitMsg: commitMsg,
                    scriptFileId: scriptFileId
                }, null, function (node, data) {
                    msgRef.successMsg("发布成功！")
                    window.artDialog.close();
                })
            });
            $("#cancelButton").click(function (){
                window.artDialog.close();
            });
        }

    }

    page.init();
    page.initEvent();

});

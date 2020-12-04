jQuery(function () {


    var initPage = {

        initDom: function () {
            $("#scriptType").select2();
            $("#closeTemplateBtn").click(function () {
              //  $.dialog.data("addTemplateWindow").close();
                $.dialog.close()
            })
            $("#addTemplateBtn").click(function () {
                var scriptType = $("#scriptType").val();

                var pythonType = 0;
                if (scriptType * 1 == 3) {
                    pythonType = 1;
                }
                if (scriptType * 1 == 4) {
                    pythonType = 2;
                    scriptType = 3;
                }
                var datas = {
                    name: $("#templateName").val(),
                    content:$.dialog.opener.editor.getValue(),
                    scriptType:scriptType,
                    args:JSON.stringify($.dialog.opener.getRunArgs()),
                    pythonType:pythonType,
                    desc:$("#desc").val()
                }
                commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/addTemplates.ajax",datas,null,function () {
                    $.successMsg("添加成功！");
                    $.dialog.close()

                })
                console.log(datas);
            })
        }
    }

    initPage.initDom();
})
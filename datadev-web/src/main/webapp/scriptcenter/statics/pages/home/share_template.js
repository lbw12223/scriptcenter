$(function () {

    datadev_user_common._int($("#templateShareErps"), true);
    // datadev_git_common._int($("#templateShareGits"), true);
    $('#templateSaveContent').validate({
        rules: {
            desc: {
                maxlength: 255,
                required: true
            },
            name: {
                required: true,
                maxlength: 30,
            },
        },
        messages: {
            name: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            desc: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            }
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.datadev-form-group').find(".datadev-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.datadev-form-group').find(".datadev-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            error.attr("style", "margin-left:110px !important");
            error.appendTo(element.parents(".datadev-form-group"));

        }
    });



    $("#template-ok").click(function (){
        var valid = $('#templateSaveContent').valid();
        var templateId = $("#templateId").val();
        if (valid) {
            var name = $("#templateSaveContent input[name='name']").val().trim();
            var desc = $("#templateSaveContent textarea[name='desc']").val();
            var shareErps = $("#templateSaveContent input[name='shareErps']").val();
            // var shareGits = $("#templateSaveContent input[name='shareGits']").val();
            var shareGits = $("#templateShareGits").is(":checked");
            commonAjaxEvents.commonPostAjax("/scriptcenter/scriptTemplate/shareTemplate.ajax", {
                id: templateId,
                name: name,
                desc: desc,
                shareErps: shareErps,
                shareGits: shareGits,
            }, $("#saveTemplateButton"), function (node, data) {
                $.successMsg("保存成功");
                var shareTemplateArt = $.dialog.data("shareTemplateArt");
                shareTemplateArt.close();

            })
        }
    })
    $("#closeDia").click(function (){
        var shareTemplateArt = $.dialog.data("shareTemplateArt");
        shareTemplateArt.close();

    })


});

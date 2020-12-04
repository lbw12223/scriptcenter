$(function () {
    datadev_user_common._int($("#codingGroupMember"),true);
    $("#addCodingGroupBtn").click(function () {
        var valid = $('#codingGroupForm').valid();
        if(valid){
            var groupName = $("#codingGroupPathInput").val();
            var description = $("#codingGroupDescText").val();
            var erps = $("#codingGroupMember").val();
            if(groupName && description ){
                commonAjaxEvents.commonPostAjax("/scriptcenter/project/createGroup.ajax",{
                    groupName:groupName,
                    description:description,
                    erps:erps,
                    gitOrCodingCode:2
                },$("#addCodingGroupBtn"),function (node, data) {
                    var initSelect=$.dialog.data("initSelect");
                    initSelect && initSelect(data.obj);
                    $.successMsg("创建组成功",null,function () {
                        var projectGroupArt = $.dialog.data("projectGroupArt");
                        projectGroupArt && projectGroupArt.close();
                    });
                },undefined,undefined,undefined,undefined,function () {
                    $.loadingMsg("正在创建组...")
                })
            }
        }
    })
    $("#closeGroupBtn").click(function () {
        var projectGroupArt = $.dialog.data("projectGroupArt");
        projectGroupArt && projectGroupArt.close();
    })
    jQuery.validator.addMethod("validProjectGroupName", function (value, element) {
        var pattern = /^[0-9a-zA-z\-_.]+$/
        // if (element.name == "file") {
        //     var namePattern = /([^<>/\\\|:""\*\?]+)\.\w+$/
        //     var grops = namePattern.exec(value);
        //     if (grops && grops[1]) {
        //         value = grops[1]
        //     } else {
        //         return false;
        //     }
        // }
        return pattern.test(value)&& !value.startsWith("-");
    }, "只能是数字、字母、下划线、中线，并且不能以中线开头");
    $('#codingGroupForm').validate({
        rules: {
            codingGroupDesc: {
                required: true,
                maxlength: 1000
            },
            codingGroupPath: {
                required: true,
                maxlength: 255,
                validProjectGroupName:true
            }
        },
        messages: {
            codingGroupDesc: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            codingGroupPath: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validProjectGroupName:"只能是数字、字母、下划线、中线，并且不能以中线开头"
            }
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            // error.appendTo(element.parents(".bdp-form-group"));
            error.appendTo(element.parent());

        }
    });
})
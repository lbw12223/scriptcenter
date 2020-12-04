$(function () {
    //提示modal
    $("#createTopGitBtn").click(function () {
        $("#gitTipsModal").modal("hide")
        $("#createGitProModal").modal('show')
        
    })

    //项目modal
    $("#createGitProModal").on("show.bs.modal",function () {
        // commonAjaxEvents.commonSpinPostAjax("",{},null,function (node, data) {
        //     var obj =data.obj;
        //
        // })
        $("#gitGroupSelect").select2();
        $("#gitProjectNameInput").val("");
        $("#gitProjectDescText").val("");
        $("#gitProjectVisRadio").prop("checked", "checked");
    })
    jQuery.validator.addMethod("validGitProName", function (value, element) {
        var pattern = /^[0-9a-zA-z\-_]+$/
        return pattern.test(value);
    }, "只支持字母,数字,下划线,中划线");
    $('#gitProForm').validate({
        rules: {
            gitGroup: {
                required: true
            },
            gitName: {
                required: true,
                maxlength:1000,
                validGitProName:true
            },
            gitDesc: {
                required: true,
                maxlength:4000
            },
            gitVisibility: {
                required: true,
            }
        },
        messages: {
            gitName: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validGitProName: "只支持字母、数字、下划线、中划线"
            },
            gitDesc: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            gitGroup: {
                required: "必填字段！"
            },
            gitVisibility: {
                required: "必填字段！"
            }
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.bdp-control-content').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.bdp-control-content').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            error.appendTo(element.parent());
        }
    });

    $("#addGitProBtn").click(function () {
        var valid = $('#gitProForm').valid();
    })

    $("#createGitGroupBtn").click(function () {
        $("#createGitGroupModal").modal("show");
    })


    //项目组modal
    $("#createGitGroupModal").on("show.bs.modal",function () {
        $("#gitGroupPathInput").val("");
        $("#gitGroupNameInput").val("");
        $("#gitGroupDescText").val("");
        $("#gitGroupVisRadio").prop("checked", "checked");
    })
    $('#gitGroupForm').validate({
        rules: {
            gitGroupPath: {
                required: true
            },
            gitGroupName: {
                required: true,
                maxlength:1000,
                validGitProName:true
            },
            gitGroupDesc: {
                required: true,
                maxlength:4000
            },
            gitGroupVisibility: {
                required: true,
            }
        },
        messages: {
            gitGroupName: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validGitProName: "只支持字母、数字、下划线、中划线"
            },
            gitGroupDesc: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            gitGroupPath: {
                required: "必填字段！"
            },
            gitGroupVisibility: {
                required: "必填字段！"
            }
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.bdp-control-content').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.bdp-control-content').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            error.appendTo(element.parent());
        }
    });

    $("#addGitGroupBtn").click(function () {
        var valid = $('#gitGroupForm').valid();
    })

})
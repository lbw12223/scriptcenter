$(function () {
    $.dialog.data("initSelect",initSelect);
    initSelect();
    //提示modal
    $("#createGitGroupBtn").click(function () {
        var projectGroupArt=$.dialog.open("/scriptcenter/project/newGitProjectGroup.html", {
            title: "新建组",
            lock: true,
            width: "600px",
            height: "400px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("projectGroupArt",projectGroupArt);
    })
    $("#addGitProBtn").click(function () {
        var valid=$('#gitProForm').valid();
        if(valid){
            var projectName = $("#gitProjectNameInput").val();
            var description = $('#gitProjectDescText').val();
            commonAjaxEvents.commonPostAjax("/scriptcenter/project/createLocalProject.ajax",{
                projectName:projectName,
                description:description
            },$("#addGitProBtn"),function (node, data) {
                if(data.obj){
                   var addSelect = $.dialog.data("addSelect");
                   addSelect && addSelect(data.obj.gitProjectId,"创建项目成功。",data.obj.gitProjectPath);
                    var tipsArt=$.dialog.data("tipsArt");
                    tipsArt&&tipsArt.close();
                    var projectArt=$.dialog.data("projectArt");
                    projectArt&&projectArt.close();

                    $("#closeProjectBtn").click();
                }
            },undefined,undefined,undefined,undefined,function () {
                $.loadingMsg("正在创建项目...")
            })
        }


    })
    $("#closeProjectBtn").click(function () {
        var tipsArt=$.dialog.data("tipsArt");
        tipsArt&&tipsArt.close();
        var projectArt=$.dialog.data("projectArt");
        projectArt&&projectArt.close();
        var projectTipsArts=$.dialog.data("projectTipsArts");
        projectTipsArts&&projectTipsArts.close();

    })
    function initSelect(groupId){
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/listErpGroups.ajax",{
            gitOrCodingCode:1
        },null,function (node, data) {
            $("#gitGroupSelect").empty();
            var options="";
            if(data.obj){
                for(var index=0;index<data.obj.length;index++){
                    if(data.obj[index]){
                        options+="<option value='"+data.obj[index].gitGroupId+"' "+((groupId&&groupId==data.obj[index].gitGroupId)?"selected":"")+" >"+data.obj[index].name+"</option>";
                    }
                }
            }
            $("#gitGroupSelect").empty().append(options).select2({
                placeholder:'请选择'
            });
        })
    }
    jQuery.validator.addMethod("validProjectName", function (value, element) {
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
        return pattern.test(value);
    }, "只能是数字、字母、下划线、中线");
    $('#gitProForm').validate({
        rules: {
            gitDesc: {
                required: true,
                maxlength: 1000
            },
            gitGroup: {
                required: true,
                maxlength: 255
            },
            gitName: {
                required: true,
                maxlength: 255,
                validProjectName:true
            }
        },
        messages: {
            gitDesc: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            gitGroup: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            gitName: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validProjectName:"只能是数字、字母、下划线、中线"
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

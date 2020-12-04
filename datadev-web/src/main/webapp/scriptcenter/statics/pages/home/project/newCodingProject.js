$(function () {
    $.dialog.data("initSelect",initSelect);
    initSelect();
    //提示modal
    $("#createCodingGroupBtn").click(function () {
        var projectGroupArt=$.dialog.open("/scriptcenter/project/newCodingProjectGroup.html", {
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
    $("#addCodingProBtn").click(function () {
        var valid=$('#codingProForm').valid();
        if(valid){
            var projectName = $("#codingProjectNameInput").val();
            var description = $('#codingProjectDescText').val();
            var groupId = $("#codingGroupSelect").val();
            var groupName = $("#codingGroupSelect").select2('data').text;
            commonAjaxEvents.commonPostAjax("/scriptcenter/project/createProject.ajax",{
                projectName:projectName,
                description:description,
                groupId:groupId,
                gitOrCodingCode:2,
                groupName:groupName

            },$("#addCodingProBtn"),function (node, data) {
                if(data.obj){
                   var addSelect = $.dialog.data("addSelect");
                   addSelect && addSelect(data.obj.codingProjectId,"创建项目成功。",data.obj.codingProjectPath);
                    var tipsArt=$.dialog.data("tipsArt");
                    tipsArt&&tipsArt.close();
                    var projectArt=$.dialog.data("projectArt");
                    projectArt&&projectArt.close();
                    var projectTipsArts=$.dialog.data("projectTipsArts");
                    projectTipsArts&&projectTipsArts.close();

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
            gitOrCodingCode:2
        },null,function (node, data) {
            $("#codingGroupSelect").empty();
            var options="";
            if(data.obj){
                for(var index=0;index<data.obj.length;index++){
                    if(data.obj[index]){
                        options+="<option value='"+data.obj[index].gitGroupId+"' "+((groupId&&groupId==data.obj[index].gitGroupId)?"selected":"")+" >"+data.obj[index].name+"</option>";
                    }
                }
            }
            $("#codingGroupSelect").empty().append(options).select2({
                placeholder:'请选择'
            });
        })
    }
    jQuery.validator.addMethod("validProjectName", function (value, element) {
        var pattern = /^[0-9a-zA-z]+$/
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
    }, "只能是数字、字母");
    $('#codingProForm').validate({
        rules: {
            codingDesc: {
                required: true,
                maxlength: 1000
            },
            codingGroup: {
                required: true,
                maxlength: 255
            },
            codingName: {
                required: true,
                maxlength: 255,
                validProjectName:true
            }
        },
        messages: {
            codingDesc: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            codingGroup: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
            },
            codingName: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validProjectName:"只能是数字、字母"
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

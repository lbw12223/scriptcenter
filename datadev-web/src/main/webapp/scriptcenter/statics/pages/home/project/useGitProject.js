$(function () {
    function useProjectArt(msg,adress,isSuceess){
        $("#gitDressId").val(adress);
        if(isSuceess){
            $.successMsg(msg);
        }else {
            $.errorMsg(msg);
        }
    }

    $("#cloneGitProBtn").click(function (e) {
        errorDealFun("",false);
        var valid  = $("#useGitProForm").valid();
        if(valid){
            // e.preventDefault();
            var useGitDress = $.trim($("#gitDressId").val());
            if(!useGitDress.toLocaleString().startsWith("http://")){
                useGitDress = "http://"+useGitDress;
            }
            if(useGitDress){
                commonAjaxEvents.commonSucAndBeforePostAjax("/scriptcenter/project/getGitProjectByPath.ajax", {
                    projectGitPath:useGitDress,
                    gitOrCodingCode:1
                },function (data) {
                    //根据获取单个项目的成功或失败做对应的反应
                    $(".messageClose").click();
                    var alertMessage = data.message ;
                    if(alertMessage.indexOf("需要登录系统") > -1){
                        var logginUrl = _bdpDomain +"/login.html";
                        alertMessage += " <a style='color:#2001e9;cursor: pointer;' href='"+logginUrl+"' target='_blank' >重新登录</a>";
                    }
                    if (data.code * 1 === 0) {
                        if(data.obj){
                            errorDealFun("",false);
                            var addSelect = $.dialog.data("addSelect");
                            addSelect && addSelect(data.obj.gitProjectId,alertMessage,data.obj.gitProjectPath);
                            $("#closeProjectBtn").click();
                        }else{
                            $.errorMsg2("添加项目失败！");
                            errorDealFun("项目不存在!",true);
                        }

                    } else {
                        if(!$("#cloneGitProBtn").hasClass("bdp-disabled")){
                            $("#cloneGitProBtn").addClass("bdp-disabled");
                        }
                        $.errorMsg2("添加项目失败！");
                        errorDealFun(alertMessage,true);

                    }


                },undefined,function () {
                    $.loadingMsg("正在添加项目...")
                })
            }
            // else {
            //     if(!$("#cloneGitProBtn").hasClass("bdp-disabled")){
            //         $("#cloneGitProBtn").addClass("bdp-disabled");
            //     }
            //     $.errorMsg("项目地址不能为空。");
            // }
        }

    });
    jQuery.validator.addMethod("validGitDress", function (value, element) {
        var d = value.length - ".git".length;
        return (d>=0 && value.lastIndexOf(".git")==d);
    }, "当前地址无效，请校验git地址的正确性。");
    jQuery.validator.addMethod("validGitHttp", function (value, element) {
        if(value && value.trim().startsWith("git@git.jd.com:")){
            return false;
        }
        return true;
    }, "只支持http链接");
    $("#useGitProForm").validate({

        rules:{
            gitDress:{
                required:true,
                validGitDress:true,
                validGitHttp:true
            }
        },
        messages: {
            gitDress: {
                required: "git地址不能为空！",
                validGitDress: "请校验地址正确性！",
                validGitHttp:"不支持ssh链接，请输入http链接"
            }

        },
        errorElement: 'label',
            errorClass: 'bdp-help-block',
            focusInvalid: false,
            highlight: function (e) {
                errorDealFun("",false);
                $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            errorDealFun("",false);
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            // error.appendTo(element.parents(".bdp-form-group"));
            // errorDealFun("",false);
            error.appendTo(element.parent());

        }


    });
    $("#closeProjectBtn").click(function () {
        var tipsArt=$.dialog.data("tipsArt");
        tipsArt&&tipsArt.close();
        var useProjectArt=$.dialog.data("useProjectArt");
        useProjectArt&&useProjectArt.close();
        var projectTipsArts=$.dialog.data("projectTipsArts");
        projectTipsArts&&projectTipsArts.close();
    });

    function errorDealFun(message,isFail){
        if(isFail){
            if(!$("#gitDressId").hasClass("bdp-wrong")){
                $("#gitDressId").addClass("bdp-wrong");
            }
            $("#errorSelfId").html(message);
            $("#errorSelfId").removeAttr("style");
            if($("#errorSelfId").hasClass("hidden")){

                $("#errorSelfId").removeClass("hidden");
            }

        }else {
            //如果不是处理错误消息则把错误消息的样式清除
            if($("#gitDressId").hasClass("bdp-wrong")){
                $("#gitDressId").removeClass("bdp-wrong");
            }
            $("#errorSelfId").html("");
            if(!$("#errorSelfId").hasClass("hidden")){

                $("#errorSelfId").addClass("hidden");
            }
        }
    }

    $("#testGitBtnId").click(function (e) {
        // e.preventDefault();
        errorDealFun("",false);
        var valid  = $("#useGitProForm").valid();
        if(valid){
            var useGitDress = $.trim($("#gitDressId").val());
            if(!useGitDress.toLocaleString().startsWith("http://")){
                useGitDress = "http://"+useGitDress;
            }
            if(useGitDress){
                commonAjaxEvents.commonControlSuccessPostAjax("/scriptcenter/project/testGitConnect.ajax", {
                    projectGitPath:useGitDress,
                    gitOrCodingCode:1
                },function (data) {
                    // $(".messageClose").click();
                    if(data.success){
                        var code = data.obj;
                        if(code && code ==200){
                            $.successMsg("测试成功");
                            if($("#cloneGitProBtn").hasClass("bdp-disabled")){
                                $("#cloneGitProBtn").removeClass("bdp-disabled");
                            }
                        }else if(code && code != 200){
                             $.errorMsg2("测试失败！");
                            errorDealFun(data.message,true);
                            if(!$("#cloneGitProBtn").hasClass("bdp-disabled")){
                                $("#cloneGitProBtn").addClass("bdp-disabled");
                            }
                        }else{
                            $.errorMsg2("测试失败！");
                            errorDealFun(data.message,true);
                            if(!$("#cloneGitProBtn").hasClass("bdp-disabled")){
                                $("#cloneGitProBtn").addClass("bdp-disabled");
                            }
                        }
                    }else {
                        $.errorMsg2("测试失败！");
                        errorDealFun(data.message,true);
                        if(!$("#cloneGitProBtn").hasClass("bdp-disabled")){
                            $("#cloneGitProBtn").addClass("bdp-disabled");
                        }
                    }
                },undefined)
            }
        }

        // else {
        //     window.open("http://git.jd.com",'_blank');
        //
        // }

    });

    $("#toGitId").click(function () {
        var useGitDress = $.trim($("#gitDressId").val());
        if (useGitDress){
            if(!useGitDress.toLocaleString().startsWith("http://")){
                useGitDress = "http://"+useGitDress;
            }
            var d = useGitDress.length - ".git".length;
            if (d>=0 && useGitDress.lastIndexOf(".git")==d){
                window.open(useGitDress,'_blank');
            }

        }
        window.open("http://git.jd.com",'_blank');
    });

    $("#toGitId").mouseover(function () {
        if(!$("#imgBlack").hasClass("bdp-hidden")){
            $("#imgBlack").removeClass("bdp-hidden");
        }
        if($("#imgHour").hasClass("bdp-hidden")){
            $("#imgHour").addClass("bdp-hidden");
        }
    });
    $("#toGitId").mouseout(function () {
        if($("#imgBlack").hasClass("bdp-hidden")){
            $("#imgBlack").addClass("bdp-hidden");
        }
        if(!$("#imgHour").hasClass("bdp-hidden")){
            $("#imgHour").removeClass("bdp-hidden");
        }
    });


})
jQuery(function () {


    var page = {
        init: function () {
            $('#gridTable').GM({
                gridManagerName: 'gridTable',
                supportAjaxPage: false,
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
                        key:"taskId",
                        text:"任务ID"
                    },
                    {
                        key:"taskName",
                        text:"任务名称",
                        width: "180px"
                    },
                    {
                        key:"taskType",
                        text:"任务类型",
                        template:function (taskType , row){

                            if (taskType == "wf") {
                                return '工作流任务';
                            } else if (taskType == "single") {
                                return '标准任务';
                            }
                            return taskType;
                        }
                    },
                    {
                        key:"actionType",
                        text:"任务子类型",
                        template:function (actionType, row) {
                            if (actionType === "custom") {
                                return "普通任务";
                            } else if (actionType === "pyscript") {
                                return "数据计算（py/sh/zip）";
                            } else if (actionType === 'chain' || actionType === 'zipper') {
                                return "数据拉链任务";
                            } else if (actionType === 'plumber_input') {
                                return "数据入库任务";
                            } else if (actionType === 'plumber_output') {
                                return "数据出库任务";
                            } else if (actionType === 'plumber') {
                                return "数据出入库任务";
                            } else if (actionType === 'hivesync' || actionType === 'hive2hive') {
                                return "数据同步(集市to集市)";
                            } else if (actionType === 'market_sync' || actionType === 'market2') {
                                return "数据同步(JDW到Jmart)";
                            } else if (actionType === 'moonfall') {
                                return "登月算法任务";
                            } else if (actionType === 'rtchain4' || actionType === 'rtchain') {
                                return "实时拉链任务";
                            } else if (actionType === 'rtcheck') {
                                return "拉链前置数据校验";
                            } else if (actionType === 'mlp') {
                                return "机器学习任务";
                            } else {
                                return actionType
                            }
                        }
                    },
                    {
                        key:"priorityDesc",
                        text:"任务等级"
                    },
                    {
                        key: 'disabled',
                        text: '是否禁用',
                        template:function (disabled , row){
                            return disabled * 1 == 1 ? "是" : "否";
                        }
                    },
                    {
                        key: 'managersName',
                        text: '负责人',
                        template:function (managersName, row) {
                            return managersName + "(" + row.managers + ")";
                        }

                    }
                ]
            });
        },
        initEvent: function () {

            $('#submitForm').validate({
                rules: {
                    commitMsg: {
                        required: true,
                        maxlength: 1000
                    }

                },
                messages: {
                    commitMsg: {
                        required: "必填字段！",
                        maxlength: $.validator.format("最多{0}个字符"),
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
                    error.appendTo(element.parent());
                }
            });

            $("#submit").click(function () {
                //1.提交内容到Git或者Coding ,(  直接覆盖)
                //2.调用调度接口push到生产
                //3.提交发布中心

                var valid = $("#submitForm").valid();

                if(!valid){
                    return
                }
                $.loadingMsg("正在提交发布！");


                var commitMsg = $("#commitMsg").val();
                var scriptFileId = $("#scriptFileId").val();
                var _projectSpaceId = top.projectSpaceId == undefined ? 0 : top.projectSpaceId;
                commonAjaxEvents.commonPostAjax("/scriptcenter/diff/submit.ajax", {
                    projectSpaceId: _projectSpaceId,
                    commitMsg: commitMsg,
                    scriptFileId: scriptFileId
                }, null, function (node, data) {
                    window.artDialog.close();
                    $.dialog.data("uplineDiff").close();
                    top.$.bdpMsg({
                        title: "提示",
                        mainContent: "<p style='margin: 20px;'>脚本已成功提交至发布中心。点此查看<a href='/atom/workflow/v2/apply/detail.html?requestId="+data.obj.wfId+"' target='_blank'/>审批详情</p>",
                        width: "400px"
                    });
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

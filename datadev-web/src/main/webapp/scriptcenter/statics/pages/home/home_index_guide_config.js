var steps = [
    {
        stepIndex: 1,
        doms: "#step1",
        masterDoms:"#step1",
        guide_title: "",
        description: [
            "• 京东大数据架构下的一站式在线数据开发平台，支持多种常用脚本类型",
            "• 基于git强大的版本管理功能，强化多人协作开发能力",
            "• 无缝对接任务调度buffalo，切实优化用户脚本开发路径"
        ],
        position: "windowcenter",
        beforeShowEvent:function (guide) {
            $("#scriptManageTab").css("display","block");
            $("#gitManage").css("display","none");
        },
        afterShowEvent:function (guide) {
        }
    },
    {
        stepIndex: 2,
        doms: "#script-manage,#step_script_manage_tab",
        masterDoms:"#script-manage",
        guide_title: "使用git项目",
        guide_subtitle: "",
        description: ["平台已经与git.jd.com打通，使用时，可通过开发平台创建git项目，也可在现有git项目中增加bdp_ide虚拟账号。"],
        position: {
            left:"315px",
            top:"150px"
        },
        beforeShowEvent:function (guide) {
            $("#step_script_manage_tab").attr("style","border-right: none !important;z-index: 1000000000000 !important;left: 1px;");
            $("#scriptManageTab").css("display","none");
            $("#gitManage").css("display","block");
        }
    },
    {
        stepIndex: 3,
        doms: "#script-manage,#step_script_manage_tab",
        masterDoms:"#script-manage",
        guide_title: "脚本管理",
        guide_subtitle: "",
        description: ["支持在项目下新建目录和脚本，可通过pull、push等操作，与git.jd.com进行交互，实现脚本的在线管理。"],
        position: {
            left:"315px",
            top:"150px"
        },
        beforeShowEvent:function (guide) {
            $("#addScriptTabDiv").removeAttr("style");
            $("#newScriptDiv").attr("style","display: none;");

            $("#step_script_manage_tab").attr("style","border-right: none !important;z-index: 1000000000000 !important;left: 1px;");
            $("#scriptManageTab").css("display","block");
            $("#gitManage").css("display","none");

            //
        }
    },
    {
        stepIndex: 4,
        doms: "#addScriptTabDiv,#newScriptDiv",
        masterDoms:"#newScriptDiv",
        guide_title: "脚本开发",
        guide_subtitle: "",
        description: ["平台支持Python 2.x、Python 3.x、Shell、Hive SQL等类型脚本，结合参数的使用，满足多种数据开发需求。"],
        position: "left",
        beforeShowEvent:function () {
            $("#addScriptTabDiv").attr("style","top:2px;z-index: 1000000000000 !important;border-bottom:none !important;background:black;display:block;");
            $("#newScriptDiv").attr("style","position: absolute !important;display: block;top: 45px;left: 290px;padding-top: 8px;background: black;display:block;")

            $("#queueCode").removeAttr("style");
            $("#queueCodeDropDiv").removeAttr("style");
        },
        afterShowEvent:function (guide) {

        },
        position: {
            left:"600px",
            top:"42px"
        },
    },
    {
        stepIndex: 5,
        doms: "#queueCodeDropDiv,#queueCode",
        masterDoms:"#queueCodeDropDiv",
        guide_title: "配置账号队列",
        guide_subtitle: "",
        description: ["点击<span style='color:rgb(50,134,233);'>【运行】</span>前，需要先选择账号/队列信息，包括Hive SQL脚本和含有SQL的Python、Shell文件。","如果下拉选项中没有您需要的账号/队列、可点击<span style='color: rgb(50,134,233)'>【配置账号队列】</span>进行配置"],
        position: {
            left:"810px",
            top:"50px"
        },
        beforeShowEvent:function () {
            $("#addScriptTabDiv").removeAttr("style");
            $("#newScriptDiv").attr("style","display: none;");
            $("#upLineModal").removeAttr("style");
            $("#codeModal").removeAttr("style");


            $(".guide-title").removeClass("lastStepTitle");
            $("#guide-title-content").removeClass("lastStepTitle-title-content");
            $("#queueCode").attr("style","top: 1px;border-bottom: none !important;z-index: 1000000000000000000000 !important;background: #252A2F;");
            $("#queueCodeDropDiv").attr("style","display: block;position: absolute !important;left: 578px;top: 40px;");
        }
    },
    {
        stepIndex: 6,
        doms: "#upLineButtonDiv,#upLineModal",
        masterDoms:"#upLineModelContentHeader",
        guide_title: "上线至任务调度buffalo",
        guide_subtitle: "",
        description: ["平台无缝对接任务调度buffalo，点击按钮即可将脚本直接上线至任务调度buffalo的脚本中心，并可快速创建数据计算任务。"],
        position: {
            left: "220px",
            top: "69px"
        },
        beforeShowEvent:function () {

            $(".guide-title").addClass("lastStepTitle");
            $("#guide-title-content").addClass("lastStepTitle-title-content");

            $("#queueCode").removeAttr("style");
            $("#queueCodeDropDiv").removeAttr("style");

            $("#upLineButtonDiv").attr("style","top: 2px;z-index: 10000000000 !important;background: black;border-bottom: none !important;")
            $("#upLineModal").attr("style","overflow: hidden;display: block;position: absolute !important;width: 726px;height: 609px;top: 45px;left: 603px;");
            $("#upLineModalInner").attr("style","width:710px;margin:0px;margin-left: 7px;margin-top: 6px;background: black;")
        }
    }
];
// $("#codeModal").modal("show");
var configParams = {
    kown:function (guide) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/finishGuide.ajax",{},null,function () {
            window.location.reload();
        })
    },
    eachStepEvent:function (guide) {
        commonAjaxEvents.commonPostAjax("/scriptcenter/guideStep.ajax",{step:guide.stepIndex},null,function () {
        })
    },
    steps:steps
}
window.onbeforeunload = null;
$(function () {
    var pageInit = {
        init: function () {
            $("#codeEditContainer").JdDataDevTab({});
            $("#codeEditContainer").JdDataDevTab("active", 1);
            $("#leftContentDiv").JdDataDevTab({});
            $("#logOrData").JdDataDevTab({});
            $("#logOrData").JdDataDevTab("active", 0);
            $("#appSelect").html("<option value='1'>bdp/data_dev</option>").select2()
            initAce();
            initGuidZtree();
            initAccount();
            initUpline();
            initJqgrid();
            var firstStepInput = $("#firstStepInput").val() * 1;
            $.guide(configParams, firstStepInput ? firstStepInput : 1);
        }
    }

    function initJqgrid() {
        var _colModel = [
            {
                name: 'taskName',
                label: "任务名称",
                sortable: false,
                formatter: function (cellvalue, options, record) {
                    if (record.taskVersion == 1) {
                        return "<span data-link='" + _bdpDomain + "/buffalo4/task/detail.html?taskId=" + record.taskId + "' class='buffalo-task-detail' style='text-decoration: underline;cursor: pointer'>" + cellvalue + "</span>";
                    } else {
                        return "<span data-link='" + _bdpDomain + "/buffalo/task/detail.html?id=" + record.taskId + "&businessType=001&taskName=" + cellvalue + "' class=' buffalo-task-detail' style='text-decoration: underline;cursor: pointer'>" + cellvalue + "</span>";
                    }
                },
                width: 253
            },
            {
                name: 'approveStatusStr',
                label: "任务审批状态",
                sortable: false,
                width: 94
            },
            {
                name: 'managersName',
                label: "任务负责人",
                sortable: false,
                width: 120
            }
        ];
        var pager_selector = "#upline-buffalo-list-grid-pager";
        jQuery("#upline-buffalo-list-grid-table").jqGrid({
            datatype: "local",
            mtype: 'POST',
            postData: {
                name: $("#ScriptName").val(),
                appGroupId: $("#uplineAppSelect").val(),
            },
            colModel: _colModel,
            viewrecords: true,
            rowList: [5, 10, 20, 50, 100],
            rowNum: 5,
            pager: pager_selector,
            altRows: true,
            width: '100%',
            autowidth: true,
            autoencode: true,
            height: "100%",
            shrinkToFit: true,
            rownumbers: true,
            scrollOffset: 6,
            loadComplete: function (data) {
                jqGrid.initWidth(jQuery, '#upline-buffalo-list-grid-table', "#upline-buffalo-list-jd-table-parent");
                jqGrid.reset(jQuery);
                $("#upline-buffalo-list-grid-table").setGridHeight($("#upline-buffalo-list-jd-table-parent").height() - 70);
            }
        });
        var rows = [
            {'taskId': '12', 'taskName': 'xxxxx', 'approveStatusStr': '线上任务', 'managersName': '姓名（erp）'},
            {'taskId': '12', 'taskName': 'xxxxx', 'approveStatusStr': '测试任务', 'managersName': '姓名（erp）'},
            {'taskId': '12', 'taskName': 'xxxxx', 'approveStatusStr': '线上任务', 'managersName': '姓名（erp）'},
        ];
        var localData = {page: 1, total: 3, records: "3", rows: rows};
        localData.rows = rows;
        var reader = {
            root: function (obj) {
                return localData.rows;
            },
            page: function (obj) {
                return localData.page;
            },
            total: function (obj) {
                return localData.total;
            },
            records: function (obj) {
                return localData.records;
            }, repeatitems: false
        };
        $("#upline-buffalo-list-grid-table").setGridParam({data: localData.rows, reader: reader}).trigger('reloadGrid');
    }

    function initAce() {
        //初始化对象
        editor = ace.edit("code");
        //设置风格和语言（更多风格和语言，请到github上相应目录查看）
        // var theme = "tomorrow_night"

        editor.session.setMode("ace/mode/python");
        //字体大小
        editor.setFontSize(14);
        //设置只读（true时只读，用于展示代码）
        editor.setReadOnly(false);

        //自动换行,设置为off关闭
        editor.setOption("wrap", "free");
        //启用提示菜单
        ace.require("ace/ext/language_tools");
        editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: false,
            enableLiveAutocompletion: true
        });
    }

    function initGuidZtree() {
        var zNodes = [];
        zNodes.push({
            parentPath: "",
            path: "1",
            name: "目录1",
            iconSkin: "icon01",
            isParent: true,
            open: true
        });
        zNodes.push({
            parentPath: "",
            path: "2",
            name: "目录2",
            iconSkin: "icon01",
            isParent: true,
            open: true
        });
        zNodes.push({
            parentPath: "1",
            path: "python",
            name: "python脚本",
            iconSkin: "icon_python",
            isParent: false,
        });
        zNodes.push({
            parentPath: "1",
            path: "zip",
            name: "zip脚本",
            iconSkin: "icon_zip",
        });
        zNodes.push({
            parentPath: "2",
            path: "sql",
            name: "sql脚本",
            iconSkin: "icon_sql",
        });
        zNodes.push({
            parentPath: "2",
            path: "shell",
            name: "shell脚本",
            iconSkin: "icon_shell",
        });
        var setting = {
            view: {
                dblClickExpand: false,
                showLine: false,
                selectedMulti: false
            },
            data: {
                simpleData: {
                    enable: true,
                    idKey: "path",
                    pIdKey: "parentPath",
                    rootPId: ""
                },
                keep: {
                    parent: true,
                    leaf: true
                },
            },
        };
        var t = $("#tree");
        $.fn.zTree.init(t, setting, zNodes);
        zTree = $.fn.zTree.getZTreeObj("tree");
    }

    function initAccount() {
        $("#marketSelect").select2();
        $("#accountCodeSelect").select2();
        $("#queueCodeSelect").select2();
    }

    function initUpline() {
        $("#uplineAppSelect").select2();
    }

    pageInit.init();
})

function showClLinkClick() {
    window.setTimeout(function () {
        $("#guide-ikown").click();
    }, 200);
    window.open("http://bdp.jd.com/helpCenter/front/showDocumentList.html?appId=31");
}
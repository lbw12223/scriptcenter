$(function () {
    var initUserStatus = false;
    var projectDetailText = $("#projectDetail").text();
    var projectDetail = JSON.parse(projectDetailText);
    var gitProjectId = $("#gitProjectId").val();

    var isCodingOrGit = $("#isCodingOrGit").val();
    if(isCodingOrGit * 1 == 1){
        $("#projectName").text(projectDetail.projectPath);
        $("#projectId").text(projectDetail.gitProjectId);
        $("#HTTP").text(projectDetail.webUrl);
        $("#HTTP").attr("href",projectDetail.webUrl);
        $("#SSH").text(projectDetail.sshUrl);
        $("#creatTime").text(projectDetail.createDate);
        $("#description").text(projectDetail.description);

    }

    $("#scriptDetailDiv").JdDataDevTab({});
    $("#scriptDetailDiv").JdDataDevTab({
        afterClickCallBack: function (event, obj, args, li) {
            if (li.attr("target") == "user-detail-content") {
                initUser();
            }
        }
    });

    $("#add-user").on("click", function () {
        $.dialog.open("/scriptcenter/project/addUser.html?gitProjectId=" + $("#gitProjectId").val(),{
            title: "添加成员",
            lock: true,
            width: "400px",
            height: "300px",
            opacity: 0.5,
            esc: false,
            close: function () {
                jQuery("#member-grid-table").trigger("reloadGrid");
            }
        });
    })

    $("#delete-user").on("click", function () {
        $.confirmMsg({
            content: "是否删除该成员",
            buttons: [
                {
                    text: "确定",
                    event: function () {
                        $.removeMsg();
                        removeMember();
                    }
                },
                {
                    text: "取消"
                }
            ]
        })
    });

    function removeMember() {
        var gr = $("#member-grid-table").getGridParam('selrow');
        var gitMemberId = $("#member-grid-table").getCell(gr, "gitMemberId");
        var id =  $("#member-grid-table").getCell(gr, "id");
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/deleteProjectMember.ajax",{
            gitProjectId: gitProjectId,
            gitMemberId: gitMemberId,
            id:id
        }, $("#delete-user"), function (node, data) {
            jQuery("#member-grid-table").trigger("reloadGrid");
        });
    }

    $("#add-group").on("click", function () {
        $.dialog.open("/scriptcenter/project/addGroup.html?gitProjectId=" + $("#gitProjectId").val(),{
            title: "添加組",
            lock: true,
            width: "400px",
            height: "300px",
            opacity: 0.5,
            esc: false,
            close: function () {
                jQuery("#group-grid-table").trigger("reloadGrid");
            }
        });
    })

    $("#delete-group").on("click", function () {
        $.confirmMsg({
            content: "是否删除该共享组",
            buttons: [
                {
                    text: "确定",
                    event: function () {
                        $.removeMsg();
                        removeGroup();
                    }
                },
                {
                    text: "取消"
                }
            ]
        })
    });

    function removeGroup() {
        var gr = $("#group-grid-table").getGridParam('selrow');
        var jdGroupId = $("#group-grid-table").getCell(gr, "jdGroupId");
        commonAjaxEvents.commonPostAjax("/scriptcenter/project/deleteSharedWithGroups.ajax",{
            gitProjectId: gitProjectId,
            jdGroupId: jdGroupId
        }, $("#delete-group"), function (node, data) {
            jQuery("#group-grid-table").trigger("reloadGrid");
        });
    }

    function initUser() {
        if (!initUserStatus) {
            initUserStatus = true;
            var _colModel = [
                {
                    name:'jdGroupId',
                    hidden:true,
                },
                {
                    name: 'groupName',
                    label: "名称",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        return "<a href='javascript:openGroupDetail(" + record.jdGroupId + ")' target='_blank'>" + cellvalue + "</a>"
                    },
                },
                {
                    name: 'groupAccessLevelRight',
                    label: "角色",
                    sortable: false,
                },

            ];

            jQuery("#group-grid-table").jqGrid({
                datatype: "json",
                url:'/scriptcenter/project/sharedWithGroups.ajax',
                mType:'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                },
                colModel: _colModel,
                viewrecords: true,
                altRows: true,
                width: '100%',
                autowidth: true,
                autoencode: true,
                height: "100%",
                shrinkToFit: true,
                scrollOffset: 6,
                forceFit:true,
                multiselect:true,
                multiboxonly: true,
                loadComplete: function (data) {
                    jqGrid.initWidth(jQuery, '#group-grid-table', "#group-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#group-grid-table").setGridHeight($("#group-jd-table-parent").height()-41);
                },
                onSelectRow: function (rowid, status) {
                    initGroupButtons();//格式化功能按钮
                },
                beforeSelectRow: function () {
                    $("#group-grid-table").jqGrid('resetSelection');
                    return(true);
                }
            });

            var _colModel2 = [
                {
                    name: 'gitMemberUserName',
                    label: "名称",
                    sortable: false,
                },
                {
                    name: 'accessLevelRight',
                    label: "角色",
                    sortable: false,
                },
                {
                    name: 'gitMemberId',
                    hidden: true,
                },
                {
                    name: 'id',
                    hidden: true,
                }
            ];

            var pager_selector = "#member-grid-pager";
            jQuery("#member-grid-table").jqGrid({
                datatype: "json",
                url:'/scriptcenter/project/projectMemberInfo.ajax',
                mType:'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                },
                colModel: _colModel2,
                viewrecords: true,
                rowList: [3, 5, 10, 20, 50, 100],
                pager:pager_selector,
                altRows: true,
                width: '100%',
                autowidth: true,
                autoencode: true,
                height: "100%",
                shrinkToFit: true,
                scrollOffset: 6,
                forceFit:true,
                multiselect:true,
                multiboxonly: true,
                loadComplete: function (data) {
                    jqGrid.initWidth(jQuery, '#member-grid-table', "#member-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#member-grid-table").setGridHeight($("#member-jd-table-parent").height()-185);
                },
                onSelectRow: function (rowid, status) {//单击行触发事件
                    initButtons();//格式化功能按钮
                },
                beforeSelectRow: function () {
                    $("#member-grid-table").jqGrid('resetSelection');
                    return(true);
                }
            });
            $(window).resize(function () {
                $("#group-grid-table").setGridWidth($("#group-jd-table-parent").width());
                $("#group-grid-table").setGridHeight($("#group-jd-table-parent").height()-41);
                $("#member-grid-table").setGridWidth($("#member-jd-table-parent").width());
                $("#member-grid-table").setGridHeight($("#member-jd-table-parent").height()-185);
            });
        } else {
            $("#group-grid-table").setGridWidth($("#group-jd-table-parent").width());
            $("#group-grid-table").setGridHeight($("#group-jd-table-parent").height()-41);
            $("#member-grid-table").setGridWidth($("#member-jd-table-parent").width());
            $("#member-grid-table").setGridHeight($("#member-jd-table-parent").height()-185);
        }
    }
})

function openGroupDetail(groupId) {
    $.dialog.open("/scriptcenter/project/groupMember.html?groupId="+ groupId,{
        title: "组成员",
        lock: true,
        width: "600px",
        height: "400px",
        opacity: 0.5,
        esc: false,
        close: function () {
        }
    });
}

//格式化按钮
function initButtons() {
    var hasAuthority = $("#hasAuthority").val();
    if (hasAuthority === 'true') {
        var datas = $("#member-grid-table").jqGrid('getGridParam', 'selarrrow');
        var length = datas.length;
        if (length >= 1) {
            $("#delete-user").removeClass("disabled");
        } else {
            $("#delete-user").addClass("disabled");
        }
    } else {
        $("#add-user").addClass("disabled");
        $("#delete-user").addClass("disabled");
    }
}

//格式化按钮
function initGroupButtons() {
    var hasAuthority = $("#hasAuthority").val();
    if (hasAuthority === 'true') {
        var datas = $("#group-grid-table").jqGrid('getGridParam', 'selarrrow');
        var length = datas.length;
        if (length >= 1) {
            $("#delete-group").removeClass("disabled");
        } else {
            $("#delete-group").addClass("disabled");
        }
    } else {
        $("#add-group").addClass("disabled");
        $("#delete-group").addClass("disabled");
    }
}

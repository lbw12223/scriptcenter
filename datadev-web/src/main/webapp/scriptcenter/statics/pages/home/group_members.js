$(function () {
    var member = {};
    member.init = function initData() {
        var _colModel = [
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
        ];

        var pager_selector = "#groupMember-grid-pager";
        var groupMembersContent = JSON.parse($("#groupMembers").text());
        jQuery("#groupMembers-grid-table").jqGrid({
            datatype:"local",
            data: groupMembersContent,
            colModel:_colModel,
            viewrecords:true,
            rowList:[5,10,20],
            pager:pager_selector,
            altRows:true,
            width:'100%',
            autowidth:true,
            autoencode:true,
            height:'100%',
            shrinkToFit: true,
            rownumbers:true,
            scrollOffset:6,
            forceFit:true,
            loadComplete:function (data) {
                jqGrid.initWidth(jQuery, "#groupMembers-grid-table", "#groupMember-jd-table-parent");
                jqGrid.reset(jQuery);
                $("#groupMembers-grid-table").setGridHeight($("#groupMember-jd-table-parent").height()-70);
            }

        });
        $(window).resize(function () {
            $("#groupMembers-grid-table").setGridWidth($("#groupMember-jd-table-parent").width());
            $("#groupMembers-grid-table").setGridHeight($("#groupMember-jd-table-parent").height()-70);
        });
    }

    $("#groupMember-detail-content").data("init", member);
    $("#groupMember-detail-content").data("init").init();

})


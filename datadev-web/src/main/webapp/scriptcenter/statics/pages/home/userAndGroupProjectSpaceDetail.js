$(function () {


    page = {
        init: function () {
            page.initEvent();
            page.initProjectSpace();

        },
        initEvent: function () {

        },
        initProjectSpace: function () {

            //初始化表格


            var _colModel = [
                {
                    name: 'jdGroupId',
                    hidden: true,
                },
                {
                    name: 'projectName',
                    label: "项目空间名称",
                    sortable: false,
                    width: "310px",
                    formatter: function (cellvalue, options, record) {
                        return "<a href='javascript:openProjectGroupDetail(" + record.jdGroupId + ")' target='_self'>" + cellvalue + "</a>"
                    }
                },
                {
                    name: 'opertor',
                    label: "操作",
                    sortable: false,
                    width: "200px",
                    formatter: function (cellvalue, options, record) {
                    }
                },

            ];

            jQuery("#projectSpace-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/project/sharedWithGroups.ajax',
                mType: 'POST',
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
                forceFit: true,
                multiselect: true,
                multiboxonly: true,
                loadComplete: function (data) {
                    jqGrid.initWidth(jQuery, '#group-grid-table', "#group-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#group-grid-table").setGridHeight($("#group-jd-table-parent").height() - 41);
                },
                onSelectRow: function (rowid, status) {
                    initGroupButtons();//格式化功能按钮
                },
                beforeSelectRow: function () {
                    $("#group-grid-table").jqGrid('resetSelection');
                    return (true);
                }
            });


        }
    }

    page.init();
})




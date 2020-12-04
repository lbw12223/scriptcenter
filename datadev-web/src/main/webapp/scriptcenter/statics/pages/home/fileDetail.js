$(function () {
    var fileDetail = {};
    fileDetail.init = function initData() {
        var _colModel = [
            {
                name: 'gitProjectFilePath',
                label: "文件路径",
                width: 80,
                sortable: false,
            },
            {
                name: 'sizeShow',
                label: "文件大小",
                width: 20,
                sortable: false,
            },
        ];

        var pager_selector = "#fileDetail-grid-pager";
        jQuery("#fileDetail-grid-table").jqGrid({
            datatype: "json",
            url:'/scriptcenter/project/fileDetail.ajax',
            mType:'POST',
            postData: {
                scriptUpLoadId: $.trim($("#scriptUpLoadId").val()),
            },
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
                jqGrid.initWidth(jQuery, "#fileDetail-grid-table", "#fileDetail-jd-table-parent");
                jqGrid.reset(jQuery);
                $("#fileDetail-grid-table").setGridHeight($("#fileDetail-jd-table-parent").height()-70);
            }

        });
        $(window).resize(function () {
            $("#fileDetail-grid-table").setGridWidth($("#fileDetail-jd-table-parent").width());
            $("#fileDetail-grid-table").setGridHeight($("#fileDetail-jd-table-parent").height()-70);
        });
    }

    $("#fileDetail-content").data("init", fileDetail);
    $("#fileDetail-content").data("init").init();

})


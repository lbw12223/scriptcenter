
var timeoutId=undefined;
$(window).resize(function () {
    $("#grid-table").setGridWidth($(window).width());
    $("#grid-table").setGridHeight($(window).height() - 75);
});
$(document).ready(function () {
    var _colModel = [];
    var widthSum=0;
    try {
        var json = JSON.parse($("#cols").html());
        for (var i in json) {
            var width=140;
            widthSum+=width;
            var temp = {
                name: i + '',
                label: json[i],
                sortable: false,
                width:width
            }
            _colModel.push(temp);
        }
    } catch (e) {
    }
    var shrinkToFit = widthSum>$("#grid-table").width()?false:true;
    var pager_selector = "#grid-pager";
    jQuery("#grid-table").jqGrid({
        datatype: "json",
        url: '/scriptcenter/script/runData.ajax',
        mtype: 'POST',
        postData: {
            runDetailId: $("#runDetailid").val()
        },
        colModel: _colModel,
        viewrecords: true,
        rowList: [5, 10, 20, 50, 100],
        pager: pager_selector,
        altRows: true,
        width: '100%',
        autowidth: true,
        autoencode:true,
        height: "100%",
//            autowidth: false,
        shrinkToFit: shrinkToFit,
        rownumbers: true,
        scrollOffset: 6,
        loadComplete: function (data) {
            jqGrid.initWidth(jQuery, '.jd-table', ".jd-table-parent");
            jqGrid.reset(jQuery);
            // console.log(_colModel);
            // console.log(data);
            $("#grid-table").setGridHeight($(window).height() - 75);
        }
    });

});
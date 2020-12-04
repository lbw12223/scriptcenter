$(function () {

    var getTitleUrl = "/scriptcenter/script/title.ajax";
    var downLoadDataUrl = "/scriptcenter/script/downLoad.ajax";

    function initData() {
        var _colModel = [];
        var widthSum = 0;
        commonAjaxEvents.commonPostAjax(getTitleUrl, {runDetailId: $("#runDetailId").val()}, $("#dataGridCommonCtrlDiv"), function (node, data) {
                if (data && data.obj) {
                    var index = 0;
                    while (data.obj[index]) {
                        var width = (data.obj[index].length * 8 + 10) > 50 ? (data.obj[index].length * 8 + 10) : 50;
                        widthSum += width;
                        var temp = {
                            name: index + '',
                            label: data.obj[index],
                            sortable: false,
                            width: width
                        }
                        _colModel.push(temp);
                        index++;
                    }
                    var shrinkToFit = widthSum > $("#grid-table").width() ? false : true;
                    var pager_selector = "#grid-pager";
                    jQuery("#grid-table").jqGrid({
                        datatype: "json",
                        url: '/scriptcenter/script/runData.ajax',
                        mtype: 'POST',
                        postData: {
                            runDetailId: $("#runDetailId").val()
                        },
                        colModel: _colModel,
                        viewrecords: true,
                        rowList: [5, 10, 20, 50, 100],
                        pager: pager_selector,
                        altRows: true,
                        width: '100%',
                        autowidth: true,
                        autoencode: true,
                        height: "100%",
                        shrinkToFit: shrinkToFit,
                        rownumbers: true,
                        scrollOffset: 6,
                        loadComplete: function (data) {
                            jqGrid.initWidth(jQuery, '.jd-table', ".jd-table-parent");
                            jqGrid.reset(jQuery);
                            console.log(_colModel);
                            console.log(data);
                            $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
                        }
                    });
                }
            }
        )


    }
    $(window).resize(function () {
        $("#grid-table").setGridWidth($("#home-data-jd-table-parent").width());
        $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
    });

    $("#downLoadData").click(function () {
        var temp = document.createElement("form");
        temp.action = downLoadDataUrl + "?runDetailId=" + $("#runDetailId").val();
        temp.method = "post";
        temp.style.display = "none";
        temp.id = "temp_form";
        document.body.appendChild(temp);
        temp.submit();
        $("#temp_form").remove();
    })

    initData();

})
$(function () {
    var allTableJqgrid = false;
    var info = $.dialog.data("info");

    init();

    $("#columnsSearchButton").click(function () {
        $("#allGridColumn").jqGrid("setGridParam", {
            postData: {
                searchWord: $.trim($("#columnsSearchInput").val())
            }
        }).trigger("reloadGrid")
    });
    $("#columnsSearchInput").keydown(function (event) {
        // console.log(event.keyCode)
        var e = event || window.event || arguments.callee.caller.arguments[0];
        if (e && e.keyCode === 13) {
            $("#columnsSearchButton").click();
        }
    });

    function init() {
        showAllTableColumns(info.marketId, info.dbName, info.tbName);
    }

    function showAllTableColumns(marketId, dbName, tbName) {
        $("#columnsSearchInput").val("");
        if (allTableJqgrid) {
            $("#allGridColumn").jqGrid("setGridParam", {
                postData: {
                    marketId: marketId,
                    dbName: dbName,
                    tbName: tbName,
                    searchWord: ""
                },
                page: 1
            }).trigger("reloadGrid")
        } else {
            allTableJqgrid = true;
            var _colModel = [
                {
                    name: 'columnName',
                    label: "列名",
                    sortable: false
                },
                {
                    name: 'columnType',
                    label: "类型",
                    sortable: false
                },
                {
                    name: 'comment',
                    label: "描述",
                    sortable: false
                }
            ];
            var data = {};
            if (marketId && dbName && tbName) {
                data.marketId = marketId;
                data.dbName = dbName;
                data.tbName = tbName;
            }
            $("#allGridColumn").jqGrid({
                datatype: "json",
                url: '/scriptcenter/config/getAllColumns.ajax',
                mtype: 'POST',
                postData: data,
                colModel: _colModel,
                viewrecords: true,
                rowList: [5, 10, 20, 50, 100],
                rowNum: 10,
                altRows: true,
                pager: "#jdGridPager",
                width: "100%",
                autowidth: true,
                autoencode: true,
                // height: "70%",
                shrinkToFit: true,
                rownumbers: true,
                scrollOffset: 6,
                loadComplete: function (data) {
                    resetGridColumnSize();
                }
            });
        }
    }

    function resetGridColumnSize() {
        $("#allGridColumn").setGridHeight($("#allGridColumnParent").height() - 70);
        $("#allGridColumn").setGridWidth($("#allGridColumnParent").width());
        jqGrid.reset(jQuery);
    }
});


$(function () {
    var dialogObj=$.dialog.data("dialogObj");
    var afterMergeAll=$.dialog.data("afterMergeAll");

    initColumn();
    $(window).resize(function () {
        $("#merge-grid-table").setGridHeight($("#merge-jd-table-parent").height() - 40);
        $("#merge-grid-table").setGridWidth($("#merge-jd-table-parent").width());
    });
    $("#merge").click(function () {
        var rowId = jQuery("#merge-grid-table").jqGrid("getGridParam", "selrow");
        var rowData = $("#merge-grid-table").jqGrid('getRowData', rowId);
        if(rowData){
            merge(rowId,rowData);
            console.log(rowId);
            console.log(rowData);
        }
    })
    $("#acceptYours").click(function () {
        accept(true);
    })
    $("#acceptTheirs").click(function () {
        accept(false);
    })

    /**
     *
     * @param flag true保存为本地  false使用git版本
     */
    function accept(flag) {
        var rowId = jQuery("#merge-grid-table").jqGrid("getGridParam", "selrow");
        var rowData = $("#merge-grid-table").jqGrid('getRowData', rowId);
        if( JSON.stringify(rowData)=='{}'){
            return;
        }
        var win = artDialog.open.origin;
        var key=win.getKey(rowData.gitProjectId,rowData.gitProjectFilePath);
        var data = {
            version:rowData.version,
            gitVersion: rowData.newGitVersion,
            gitProjectId: rowData.gitProjectId,
            gitProjectFilePath: rowData.gitProjectFilePath
        };
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/getMergeContent.ajax", data, null, function (node, callBackdata) {
            var content=flag?(callBackdata.obj.hbaseContent || ""):(callBackdata.obj.gitContent||"");
            data.content=content;
            win.datadevInit.directSave(function () {
                $("#merge-grid-table").jqGrid('delRowData', rowId);
                checkClose();
            }, data,key);
        })
    }
    function checkClose() {
        var datas=$("#merge-grid-table").jqGrid("getRowData");
        if(!datas || datas.length==0){
            afterMergeAll && afterMergeAll();
            dialogObj.close();
        }
    }
    function merge(rowId,rowData) {
        var win = artDialog.open.origin;
        var key=win.getKey(rowData.gitProjectId,rowData.gitProjectFilePath);
        win.scriptMergeContentMap.put(key, {localVersion: rowData.version, remoteVersion: rowData.newGitVersion});
        win.diff( rowData.gitProjectId, rowData.gitProjectFilePath, function (key, data) {
            win.datadevInit.directSave(function (data) {
                $("#merge-grid-table").jqGrid('delRowData', rowId);
                checkClose();
            }, data,key);
        });
    }
    function initColumn() {
        var _colModel = [
            {
                name: 'gitProjectFilePath',
                label: "脚本路径",
                sortable: false,
            }, {
                name: 'typeStr',
                label: "脚本类型",
                sortable: false,
            }, {
                name: "deleted",
                label: "Yours",
                sortable: false,
                formatter: function (cellvalue, options, record) {
                    var deleted = record.deleted;
                    return deleted == 1 ? "deleted" : "modified";
                }
            }, {
                name: "deleted",
                label: "Theirs",
                sortable: false,
                formatter: function (cellvalue, options, record) {
                    return "modified";
                }
            }, {
                name: "version",
                label: "version",
                hidden: true
            }, {
                name: "newGitVersion",
                label: "newGitVersion",
                hidden: true
            },{
                name: "gitProjectId",
                label: "gitProjectId",
                hidden: true
            }
        ];
        var grid = jQuery("#merge-grid-table").jqGrid({
            datatype: "local",
            colModel: _colModel,
            viewrecords: true,
            rowList: [5, 10, 20, 50, 100],
            rowNum: 10000,
            altRows: true,
            width: '100%',
            autowidth: true,
            autoencode: true,
            height: "100%",
            shrinkToFit: true,
            rownumbers: true,
            scrollOffset: 6,
            loadComplete: function (data) {
                $("#merge-grid-table").setGridHeight($("#merge-jd-table-parent").height() - 40);
                $("#merge-grid-table").setGridWidth($("#merge-jd-table-parent").width());
            },
            ondblClickRow:function (rowId, iRow, iCol, e) {
                var rowData = $("#merge-grid-table").jqGrid('getRowData', rowId);
                if(rowData){
                    merge(rowId,rowData);
                    console.log(rowId);
                    console.log(rowData);
                }
            }
        });
        var dataList = $.dialog.data("mergeList");
        var results = {
            page: "1",
            total: "1",
            records: dataList.length,
            rows: dataList
        };
        var reader = {
            root: function (obj) {
                return results.rows;
            },
            page: function (obj) {
                return results.page;
            },
            total: function (obj) {
                return results.total;
            },
            records: function (obj) {
                return results.records;
            },
        }
        grid.setGridParam({data: results.rows, localReader: reader}).trigger('reloadGrid');
    }

})
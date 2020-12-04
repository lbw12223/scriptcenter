$(function () {
    var dataPreviewRunDetailId = undefined;
    var dataPreviewSpinner = undefined;
    var info = $.dialog.data("info");

    showDataPreview(info.clusterCode, info.marketCode, info.dbName, info.tbName, info.marketName);

    function showDataPreview(clusterCode, linuxUser, dbName, tbName, marketName) {
        hiddenSpinner(dataPreviewSpinner, $("#dataPreviewSpinner"));
        dataPreviewSpinner = showSpinner($("#dataPreviewSpinner"));
        commonAjaxEvents.commonAllPostAjax("/scriptcenter/config/dataPreview.ajax", {
            clusterCode: clusterCode,
            linuxUser: linuxUser,
            dbName: dbName,
            tbName: tbName
        }, null, function (node, data) {
            var runDetailId = data.obj;
            getSqlData(runDetailId, marketName, dbName, tbName, dataPreviewSpinner);
        }, function () {
            hiddenSpinner(dataPreviewSpinner, $("#dataPreviewSpinner"));
        })
    }

    function getSqlData(runDetailId, marketName, dbName, tbName, spinner) {
        var dataPreviewContent = $("#dataPreviewContent");
        // dataPreviewContent.find(".show-info-tips .mr-name").text(marketName);
        // dataPreviewContent.find(".show-info-tips .db-name").text(dbName);
        // dataPreviewContent.find(".show-info-tips .tb-name").text(tbName);
        dataPreviewContent.show();
        $("#allPreviewTable").jqGrid('GridUnload');
        dataPreviewRunDetailId = runDetailId;
        if (runDetailId) {
            $("#allPreviewTableParent").show();
            $("#noDataTips").hide();
            validResult(runDetailId, spinner);
        } else {
            $("#allPreviewTableParent").hide();
            $("#noDataTips").show();
        }
    }

    function validResult(runDetailId, spinner) {
        if (runDetailId * 1 > 0 && runDetailId == dataPreviewRunDetailId) {
            commonAjaxEvents.commonAllPostAjax("/scriptcenter/config/validResult.ajax", {
                runDetailId: runDetailId,
            }, null, function (_node, data) {
                var error = data.obj.error + "";
                var result = data.obj.result + "";
                var dataCount = data.obj.dataCount;
                if (error == "true" || dataCount != undefined && dataCount == 0) {
                    hiddenSpinner(dataPreviewSpinner, $("#dataPreviewSpinner"))
                    $("#allPreviewTableParent").hide();
                    $("#noDataTips").show();
                } else if (result === "false") {
                    window.setTimeout((function () {
                        validResult(runDetailId, spinner);
                    }), 1000)
                } else {
                    if (dataPreviewRunDetailId == runDetailId) {
                        dataPreviewRunDetailId = undefined;
                        hiddenSpinner(dataPreviewSpinner, $("#dataPreviewSpinner"))
                        showData(runDetailId);
                    }
                }
            })
        }
    }

    function showData(runDetailId) {
        var widthSum = 0;
        commonAjaxEvents.commonPostAjax("/scriptcenter/script/title.ajax", {runDetailId: runDetailId}, null, function (node, data) {
            if (data && data.obj) {
                var _colModel = [];
                var index = 0;
                while (data.obj[index]) {
                    var width = (data.obj[index].length * 8 + 10) > 50 ? (data.obj[index].length * 8 + 10) : 50;
                    widthSum += width;
                    var temp = {
                        name: index + '',
                        label: data.obj[index],
                        sortable: false,
                        width: width,
                        formatter: function (cellvalue, options, record) {
                            try {
                                if (typeof cellvalue == "object") {
                                    cellvalue = JSON.stringify(cellvalue).replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
                                }
                                cellvalue = cellvalue.replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
                            } catch (e) {
                            }
                            var title = cellvalue.replace(new RegExp("\"", "gm"), "&quot;").replace(new RegExp("'", "gm"), "&apos;");
                            return '<div class="pop-title" data-title="' + title + '">' + cellvalue + '</div>';
                        },
                    }
                    _colModel.push(temp);
                    index++;
                }
                shrinkToFit = widthSum > $("#allPreviewTable").width() ? false : true;
                var pager_selector = "#jdPreviewGridPager";
                jQuery("#allPreviewTable").jqGrid({
                    datatype: "json",
                    url: '/scriptcenter/script/runData.ajax',
                    mtype: 'POST',
                    postData: {
                        runDetailId: runDetailId
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
                    title: false,
                    loadComplete: function (data) {
                        jqGrid.initWidth(jQuery, '#allPreviewTable', "#allPreviewTableParent");
                        jqGrid.reset(jQuery);
                        $("#allPreviewTable").setGridHeight($("#allPreviewTableParent").height() - 75);
                    }
                });
            }
        })
    }

    function hiddenSpinner(spinner, element) {
        $(element).hide();
        if (spinner) {
            spinner.spin();
        }
    }

    //展示循环等待图标
    function showSpinner(element) {
        $(element).show();
        var opts = {
            lines: 13, // 花瓣数目
            length: 10, // 花瓣长度
            width: 5, // 花瓣宽度
            radius: 15, // 花瓣距中心半径
            corners: 1, // 花瓣圆滑度 (0-1)
            rotate: 0, // 花瓣旋转角度
            direction: 1, // 花瓣旋转方向 1: 顺时针, -1: 逆时针
            color: '#5882FA', // 花瓣颜色
            speed: 1, // 花瓣旋转速度
            trail: 60, // 花瓣旋转时的拖影(百分比)
            shadow: false, // 花瓣是否显示阴影
            hwaccel: false, //spinner 是否启用硬件加速及高速旋转
            className: 'spinner', // spinner css 样式名称
            zIndex: 2e9, // spinner的z轴 (默认是2000000000)
            top: "auto", // spinner 相对父容器Top定位 单位 px
            left: "auto"// spinner 相对父容器Left定位 单位 px
        };
        var spinner = new Spinner(opts);
        spinner.spin($(element)[0]);
        return spinner;
    }
});


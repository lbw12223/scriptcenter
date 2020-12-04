var _colModel = [];
var initDataStatus = false;
var getTitleUrl = "/scriptcenter/script/title.ajax";
var downLoadDataUrl = "/scriptcenter/script/downLoad.ajax";
var chartDataUrl = "/scriptcenter/script/chartData.ajax"
var chartInstance = undefined;
var shrinkToFit = false;

/*
 初始化resultData
 * */
function initResultData() {
    var selectedChartLi = $(".chart-result-ul > li.selected");
    initChart.initSelectChart(selectedChartLi);
}
function resizeJqGrid() {
    $("#grid-table").setGridWidth($("#home-data-jd-table-parent").width(),shrinkToFit);
    $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
}


var initChart = {
    initChart: function () {
        /*
         chartInstance = new Highcharts.Chart('chartShowDiv', default_chart_options);
         chartInstance.reflow();*/
        $("#downLoadData").click(function () {
            window.onbeforeunload = null;
            var temp = document.createElement("form");
            temp.action = downLoadDataUrl + "?runDetailId=" + $("#runDetailId").val();
            temp.method = "post";
            temp.style.display = "none";
            temp.id = "temp_form";
            document.body.appendChild(temp);
            temp.submit();
            $("#temp_form").remove();
        })
    },
    showChart: function (chartOptions) {
        initChart.loadColumn();
        var options = $.extend({}, default_chart_options, chartOptions);
        console.log(options);
        chartInstance = new Highcharts.Chart('chartShowDiv', options);
        chartInstance.reflow();
    },
    getDefaultChartOption: function (chartTypeId, defaultTitleText, chartType, userOptions) {

        /**
         *
         *    .data("categoryLabel", $("#categoryLabelContainer").html())
         .data("xLabel", $("#xLabelContainer").html())
         .data("yLabel", $("#yLabelContainer").html())
         .data("zLabel", $("#zLabelContainer").html());
         */
        $("#categoryLabelContainer").html($("#" + chartTypeId).data("categoryLabel") ? $("#" + chartTypeId).data("categoryLabel") : "");
        $("#zLabelContainer").html($("#" + chartTypeId).data("zLabel") ? $("#" + chartTypeId).data("zLabel") : "");
        $("#xLabelContainer").html($("#" + chartTypeId).data("xLabel") ? $("#" + chartTypeId).data("xLabel") : "");
        $("#yLabelContainer").html($("#" + chartTypeId).data("yLabel") ? $("#" + chartTypeId).data("yLabel") : "");
        /**
         * 首先看 data 里面是否有值
         * 没有使用default
         */
        initChart.loadColumn();
        var option = $("#" + chartTypeId).data("chartData");
        if (!option) {
            option = {
                title: {
                    text: defaultTitleText,
                },
                chart: {
                    type: chartType
                }
            }
            if (chartType === "pie") {
                option = $.extend({}, default_pie_chart_options, option);
            } else if (chartType === "bubble") {
                option = $.extend({}, default_bubble_chart_options, option);
            } else {
                option = $.extend({}, default_chart_options, option);
            }
        }
        if (userOptions) {
            option = $.extend({}, option, userOptions);
        }
        if (chartType === "bubble") {
            $(".bubble").css("display", "block");
            $(".unBubble").css("display", "none");
        } else {
            $(".bubble").css("display", "none");
            $(".unBubble").css("display", "block");
        }
        console.log(option);
        $("#" + chartTypeId).data("chartData", option);
        return option;
    },
    table_chart: function () {
        if (!initDataStatus) {
            initDataStatus = true;
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
                            width: width,
                            formatter: function (cellvalue, options, record) {
                                try {
                                    if (typeof  cellvalue == "object") {
                                        return JSON.stringify(cellvalue).replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
                                    }
                                    return cellvalue.replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
                                } catch (e) {
                                }
                                return cellvalue||""
                            },
                        }
                        _colModel.push(temp);
                        index++;
                    }
                    initChart.fillColumn();
                    shrinkToFit = widthSum > $("#grid-table").width() ? false : true;
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
            })
        } else {
            $("#grid-table").setGridWidth($("#home-data-jd-table-parent").width());
            $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
        }
    },
    zt_chart: function (option) {
        chartInstance = new Highcharts.Chart('chartShowDiv', initChart.getDefaultChartOption("zt_chart", "条形图示例", "column", option));
        chartInstance.reflow();
    },
    xt_chart: function (option) {
        chartInstance = new Highcharts.Chart('chartShowDiv', initChart.getDefaultChartOption("xt_chart", "折线图示例", "line", option));
        chartInstance.reflow();
    },
    bt_chart: function (option) {
        chartInstance = new Highcharts.Chart('chartShowDiv', initChart.getDefaultChartOption("bt_chart", "饼状图示例", "pie", option));
        chartInstance.reflow();
    },
    mjdjt_chart: function (option) {
        chartInstance = new Highcharts.Chart('chartShowDiv', initChart.getDefaultChartOption("mjdjt_chart", "面积图示例", "area", option));
        chartInstance.reflow();
    },
    qpt_chart: function (option) {
        if (option && option.xAxis && option.xAxis.categories) {
            option.xAxis.categories = [];
        }
        chartInstance = new Highcharts.Chart('chartShowDiv', initChart.getDefaultChartOption("qpt_chart", "气泡图示例", "bubble", option));
        chartInstance.reflow();
    },
    initDom: function () {
        $("#dataLogContainer").JdDataDevTab({
            afterClickCallBack: function (event, obj, args, li) {
                if (li.attr("target") == "runDetailDiv") {
                    initInfo();
                } else if (li.attr("target") == "logDiv") {
                    initLog();
                } else if (li.attr("target") == "dataDiv") {
                    initData();
                }
            }
        });
        var activeIndex = $("#dataLog").val() || 0
        $("#dataLogContainer").JdDataDevTab("active", activeIndex);
    },
    initSelectChart: function (selectedChartLi) {
        var target = selectedChartLi.attr("target");
        $(".dataContent").css("display", "none");
        $("#" + target).css("display", "block");
        $(".chart-result-ul > li").removeClass("selected");
        selectedChartLi.addClass("selected");
        var id = selectedChartLi.attr("id");
        initChart[id]();
    },
    hightLightWord: function (value, search) {
        if ($.trim(search).length > 0) {
            var indexOf = value.indexOf(search);
            if (indexOf != -1) {
                var subStr = value.substring(indexOf + search.length);
                return value.substring(0, indexOf) + "<span class='hightLightWord'>" + search + "</span>" + initChart.hightLightWord(subStr, search);
            } else {
                return value
            }
        }
        return value;
    },
    initEvent: function () {
        $("#searchColumnName").keyup(function (event) {
            var searchValue = $.trim($("#searchColumnName").val());
            var lis = $("#chart-result-column-ul > li");
            for (var index = 0; index < lis.length; index++) {
                var liNode = $(lis[index]);
                var liHtml = liNode.attr("value");
                var indexOf = liHtml.indexOf(searchValue);
                //改变成高亮
                var html = initChart.hightLightWord(liHtml, searchValue);
                if (indexOf != -1) {
                    liNode.css("display", "block").html(html);
                } else {
                    liNode.css("display", "none").html(html);
                }
            }
        })

        /**
         * 左侧不同类型的图标
         */
        $(".chart-result-ul > li").click(function () {
            var thisNode = $(this);
            initChart.initSelectChart(thisNode);
        })
        /**
         * 小x 按钮 事件
         */
        $(".chart-config-item-container").on("click", ".chart-config-item-close", function (event, args) {
            var node = $(this).parent();
            node.remove();
            $(".moreColumnSelectDiv").css("display", "none");
            initChart.setChartTypeXyzColumnData();
        })
        /**
         * 选择某个column
         */
        $(".moreColumnSelectDiv").on("click", "li", function () {
            var thisNode = $(this);
            var sort = thisNode.attr("sort");
            var html = thisNode.html();

            $("span[opencolunmselect='true'] > a").html(html).attr("sort", sort).attr("title", html);
            $(".moreColumnSelectDiv").hide();
            $("span[opencolunmselect='true']").removeAttr("opencolunmselect");

            initChart.setChartTypeXyzColumnData();

        });
        $(".chart-config-item-container").on("click", ".chart-config-item-content", function () {
            var xMoreColumnMoreDiv = $("#xMoreColumnMoreDiv");
            var content = $("a", this).attr("orgin");
            var thisParent = $(this).parent();
            var location = thisParent.position();

            var xyzColumn = thisParent.parent().parent().attr("id");
            var str = "";
            if (xyzColumn === "categoryLabel") {
                str += "<li sort='0'>" + content + "</li>";
                str += "<li sort='1'>" + content + "(升序)</li>";
                str += "<li sort='2'>" + content + "(降序)</li>";
            } else if (xyzColumn === "xLabel") {
                str += "<li sort='0'>" + content + "</li>";
                str += "<li sort='1'>" + content + "(升序)</li>";
                str += "<li sort='2'>" + content + "(降序)</li>";
            } else if (xyzColumn === "yLabel") {
                str += "<li sort='0'>" + content + "</li>";
                str += "<li sort='3'>" + content + "(求和)</li>";
                str += "<li sort='4'>" + content + "(最大值)</li>";
                str += "<li sort='5'>" + content + "(中位数)</li>";
                str += "<li sort='6'>" + content + "(平均值)</li>";
                str += "<li sort='7'>" + content + "(最小值)</li>";
                str += "<li sort='8'>" + content + "(计数)</li>";
                str += "<li sort='9'>" + content + "(计数不重复)</li>";
            } else if (xyzColumn === "zLabel") {
                str += "<li sort='0'>" + content + "</li>";
            }
            $(".moreColumnSelect", xMoreColumnMoreDiv).html(str);
            var chartTypeId = $(".chart-result-ul > li.selected").attr("id");
            /**
             * qpt_chart 气泡图除了 分类轴有
             */
            var isShow = false;
            if (chartTypeId === "qpt_chart") {
                if (xyzColumn === "categoryLabel") {
                    isShow = true;
                }
            } else if (chartTypeId === "bt_chart") {
                if (xyzColumn === "xLabel") {
                    isShow = false;
                }
            } else {
                isShow = true;
            }
            if (isShow) {
                $(this).attr("opencolunmselect", "true");
                xMoreColumnMoreDiv.css("display", "block").css("left", (location.left + 6) + "px").css("top", (location.top + thisParent.height() + 6) + "px");
            }
        })
        $(".moreColumnSelectDiv").mouseleave(function () {
            var thisNode = $(this);
            thisNode.css("display", "none");
            $("span[opencolunmselect='true']").removeAttr("opencolunmselect");
        })

        $("#chart-result-column-ul > li").draggable({
            distance: 20,
            helper: function () {
                var clone = $('<div class="moveLiClass">' + $(this).html() + '</div>');
                return clone;
            },
            containment: "window",

        });
        $(".chart-config-item-container").droppable({
            accept: "#chart-result-column-ul > li",
            hoverClass: "chart-config-item-container-active",
            drop: function (event, ui) {
                var thisParent = $(this).parent();
                var id = thisParent.attr("id");
                var chartTypeId = $(".chart-result-ul > li.selected").attr("id");
                /* 气泡图 所有都只允许一个*/
                if (chartTypeId === "qpt_chart") {
                    $(".chart-config-item", $(this)).remove();
                }
                /*x轴 只允许一个*/
                if (id == "xLabel" && $(".chart-config-item", $(this)).length > 0) {
                    $(".chart-config-item", $(this)).remove();
                }
                /**
                 * 饼图x周只允许一个.
                 */
                if (chartTypeId === "bt_chart") {
                    $(".chart-config-item", $(this)).remove();
                }
                var cloneNode = $("#clone-chart-config-item").clone();
                $("a", cloneNode).html(ui.draggable.text())
                    .attr("title", ui.draggable.attr("value"))
                    .attr("name", ui.draggable.attr("name"))
                    .attr("sort", "0")
                    .attr("orgin", ui.draggable.text());
                cloneNode.css("display", "block");
                cloneNode.appendTo($(this));


                initChart.setChartTypeXyzColumnData();

            }
        });
        $(".chart-config-item-container").sortable({
            opacity: 0.8,
            axis: "y",
            distance: 10,
            revert: true,
            stop: function () {
                initChart.setChartTypeXyzColumnData();
            }
        });


        $(window).resize(function () {
            $("#grid-table").setGridWidth($("#home-data-jd-table-parent").width());
            $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
        });

        $("#showButton").click(function () {
            var _chartType = $(".chart-result-ul > li.selected").attr("id");
            var data = {
                categoryLabel: getXYZLabelValues("categoryLabel"),
                xLabel: getXYZLabelValues("xLabel"),
                yLabel: getXYZLabelValues("yLabel"),
                zLabel: getXYZLabelValues("zLabel"),
                runDetailId: $("#runDetailId").val(),
                chartType: _chartType
            }
            var checkPass = true;
            //check
            if (data.xLabel.length < 1 || data.yLabel.length < 1) {
                checkPass = false;
            }
            if (_chartType === "qpt_chart") {
                if (data.xLabel.length < 1 || data.yLabel.length < 1 || data.categoryLabel.length < 1 || data.zLabel.length < 1) {
                    checkPass = false;
                }
            }

            if (!checkPass) {
                if (parent.$) {
                    parent.$.errorMsg("请先配置分类和数值！");
                } else {
                    $.errorMsg("请先配置分类和数值！");
                }
                return;
            }

            var dataString = JSON.stringify(data);
            commonAjaxEvents.commonPostAjax(chartDataUrl, {data: dataString}, $("#dataGridCommonCtrlDiv"), function (node, data) {
                for (var index = 0; index < data.data.series.length; index++) {
                    data.data.series[index].animation = {};
                    data.data.series[index].animation.duration = 1000;
                }
                var options = {
                    title: {
                        text: null,
                    },
                    xAxis: {
                        categories: data.data.categories
                    },
                    series: data.data.series
                }
                var selectedMethod = $(".chart-result-ul > li.selected").attr("id");
                initChart[selectedMethod](options);
            })

        })
    },
    loadColumn: function () {
        if (!_colModel && _colModel.length < 1) {

            commonAjaxEvents.commonPostAjax(getTitleUrl, {runDetailId: $("#runDetailId").val()}, $("#dataGridCommonCtrlDiv"), function (node, data) {
                if (data && data.obj) {
                    var index = 0;
                    while (data.obj[index]) {
                        var width = (data.obj[index].length * 8 + 10) > 50 ? (data.obj[index].length * 8 + 10) : 50;
                        var temp = {
                            name: index + '',
                            label: data.obj[index],
                            sortable: false,
                            width: width,
                            formatter: function (cellvalue, options, record) {
                                try {
                                    if (typeof  cellvalue == "object") {
                                        return JSON.stringify(cellvalue).replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
                                    }
                                    return cellvalue.replace(new RegExp("<", "gm"), "&lt;").replace(new RegExp(">", "gm"), "&gt;");
                                } catch (e) {
                                }
                                return cellvalue
                            },
                        }
                        _colModel.push(temp);
                        index++;
                    }
                    initChart.fillColumn();
                }
            })
        }
    },
    fillColumn: function () {
        if (_colModel && _colModel.length > 0) {
            var ul = $("#chart-result-column-ul");
            ul.html("");
            for (var index = 0; index < _colModel.length; index++) {
                var temp = $("<li></li>").html(_colModel[index].label).attr("name", _colModel[index].name).attr("value", _colModel[index].label);
                ul.append(temp);
            }
            $("#chart-result-column-ul > li").draggable({
                distance: 20,
                helper: function () {
                    var clone = $('<div class="moveLiClass">' + $(this).html() + '</div>');
                    return clone;
                },
                containment: "window"

            });

        }
    },
    setChartTypeXyzColumnData: function () {
        var chartTypeId = $(".chart-result-ul > li.selected").attr("id");
        $("#" + chartTypeId)
            .data("categoryLabel", $("#categoryLabelContainer").html())
            .data("xLabel", $("#xLabelContainer").html())
            .data("yLabel", $("#yLabelContainer").html())
            .data("zLabel", $("#zLabelContainer").html());

    }
}
function getXYZLabelValues(lableId) {
    var lables = $("#" + lableId);
    var contents = $(".chart-config-item-content", lables);
    var array = [];
    if (contents.length > 0) {
        for (var index = 0; index < contents.length; index++) {
            /*
             * {
             *     columnIndex:
             *     name:
             * }
             * */
            var innerA = $("a", $(contents[index]));
            var temp = {
                name: innerA.html(),
                columnIndex: innerA.attr("name"),
                sort: innerA.attr("sort")
            }
            array.push(temp);
        }
    }
    return array;
}
/*init chart*/
$(function () {
    /*   initChart.initDom();*/
    initChart.initEvent();
    initChart.initChart();
})

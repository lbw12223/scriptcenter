var _colModel = [];
var initInfoStatus = false;
var initLogStatus = false;
var initDataStatus = false;
var editor = initAce();
var getLogUrl = "/scriptcenter/script/runTimeLog.ajax";
var getTitleUrl = "/scriptcenter/script/title.ajax";
var downLoadDataUrl = "/scriptcenter/script/downLoad.ajax";
var chartDataUrl = "/scriptcenter/script/chartData.ajax"
var internal = undefined;
var currentLogNum = 1;
var chartInstance = undefined;
window.onbeforeunload = null;

function initAce() {
    //初始化对象
    var editor = ace.edit("logCode");
    //设置风格和语言（更多风格和语言，请到github上相应目录查看）
    // var theme = "tomorrow_night"
    // editor.setTheme("ace/theme/" + theme);
    editor.session.setMode("ace/mode/text");
    //字体大小
    editor.setFontSize(12);
    //设置只读（true时只读，用于展示代码）
    editor.setReadOnly(true);

    //自动换行,设置为off关闭
    editor.setOption("wrap", "free");

    //启用提示菜单
    // ace.require("ace/ext/language_tools");
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true
    });
    editor.setValue("");
    editor.focus()
    return editor;
}
function initInfo() {
    if (!initInfoStatus) {
        initInfoStatus = true;
        commonAjaxEvents.commonPostAjax("/scriptcenter/home/basicInfo.ajax", {
            runDetailId: $("#runDetailId").val()
        }, null, function (node, data) {
            if (data) {
                var runDetailObj = data;
                $("#script-detail-project").text(runDetailObj.gitProjectPath || "--")
                $("#script-detail-file-path").text(runDetailObj.gitProjectFilePath || "--")
                $("#script-run-id").text(runDetailObj.id || "--")
                $("#script-run-operator").text(runDetailObj.operator || "--")
                $("#script-run-startTime").text(runDetailObj.startTime || "--")
                $("#script-run-stopTime").text(runDetailObj.endTime || "--")
                $("#script-run-totalTime").text(runDetailObj.timePeriod || "--")
                $("#script-run-status").text(runDetailObj.statusStr || "--")
                $("#script-run-cluster").text(runDetailObj.clusterCode || "--")
                $("#script-run-market").text(runDetailObj.marketLinuxUser || "--")
                $("#script-run-account").text(runDetailObj.accountCode || "--")
                $("#script-detail-queue").text(runDetailObj.queueCode || "--")
                var argsShow = "";
                try {
                    var args = runDetailObj.args;
                    var argsObj = JSON.parse(args);
                    var index = 1;
                    while (argsObj[index] != undefined) {
                        argsShow += "参数" + index + " : " + (argsObj[index] || "") + "\n";
                        index++;
                    }
                } catch (e) {
                }
                $("#script-detail-args").val(argsShow || "--")
            }
        })
    }

}
function initLog() {
    if (!initLogStatus) {
        initLogStatus = true;
        getLog();
    }
}
function getLog() {
    $.ajax({
        url: getLogUrl,
        data: {
            runDetailId: $("#runDetailId").val(),
            currentLogIndex: currentLogNum
        },
        type: "post",
        dataType: 'json',
        success: function (data) {
            if (data && data.obj && data.code == 0) {
                var logs = data.obj.logs;
                if (logs.length == 0) {
                } else {
                    var string = logs.join("\n") + "\n";
                    currentLogNum += logs.length;
                    editor.session.insert({row: currentLogNum, column: 0}, string);
                }
                if (!data.obj.isLastLog) {
                    setTimeout(getLog, 1000);
                }
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            window.clearInterval(internal);
            if (parent.$) {
               // parent.$.errorMsg("出现异常,请联系管理员!");
            } else {
               // $.errorMsg("出现异常,请联系管理员!");
            }
        }
    });
}
function initData() {
    var selectedChartLi = $(".chart-result-ul > li.selected");
    initChart.initSelectChart(selectedChartLi);

}


var initChart = {

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
                            width: width
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
                containment: "window",
                scroll: false
            });
        }
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
                            width: width
                        }
                        _colModel.push(temp);
                        index++;
                    }
                    initChart.fillColumn();
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
            })
        } else {
            $("#grid-table").setGridWidth($("#home-data-jd-table-parent").width());
            $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
        }
    },
    zt_chart: function () {
        initChart.loadColumn();
        chartInstance.update({
            title: {
                text: null
            },
            chart: {
                type: "bar"
            }
        })
        chartInstance.reflow();
    },
    xt_chart: function () {
        initChart.loadColumn();
        chartInstance.update({
            title: {
                text: null
            },
            chart: {
                type: "line"
            }
        })
        chartInstance.reflow();
    },
    bt_chart: function () {
        initChart.loadColumn();
        chartInstance.reflow();

    },
    mjdjt_chart: function () {
        initChart.loadColumn();
        chartInstance.reflow();

    },
    qpt_chart: function () {
        initChart.loadColumn();
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
    initChart: function () {
        var options = {
            subTitle: {
                text: null,
            },
            xAxis: {
                title: {
                    text: null
                }
            },
            yAxis: {
                title: {
                    text: null
                }
            },
            series: [/*{
             name: '安装，实施人员',
             data: [[1,23233],[3,1000],[6,2322],[5,6767],[2,8000],[1,2222]]
             },
             {
             name: '工人，实施人员',
             data: [[1,2323],[3,100],[6,322],[5,667],[2,400],[1,222]]
             }*/
                {
                    name: '工人',
                    data: [24916, 24064, 29742, 29851, 32490, 30282, 38121, 40434]
                }, {
                    name: '销售',
                    data: [11744, 17722, 16005, 19771, 20185, 24377, 32147, 39387]
                }, {
                    name: '项目开发',
                    data: [null, null, 7988, 12169, 15112, 22452, 34400, 34227]
                }, {
                    name: '其他',
                    data: [12908, 5948, 8105, 11248, 8989, 11816, 18274, 18111]
                }]
        }
        chartInstance = new Highcharts.Chart('chartShowDiv', options);
        chartInstance.reflow();
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
            window.setTimeout(function () {
                window.onbeforeunload = function () {
                    return "确定离开页面吗？";
                }
            }, 1000)
        })
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

        $(".chart-result-ul > li").click(function () {
            var thisNode = $(this);
            initChart.initSelectChart(thisNode);
        })

        $(".chart-config-item-container").on("click", ".chart-config-item-close", function (event, args) {
            var node = $(this).parent();
            node.remove();
            $(".moreColumnSelectDiv").css("display", "none");
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
        });
        $(".chart-config-item-container").on("click", ".chart-config-item-content", function () {
            var xMoreColumnMoreDiv = $("#xMoreColumnMoreDiv");
            var content = $("a", this).attr("orgin");
            var thisParent = $(this).parent();
            var location = thisParent.offset();

            var xyzColumn = thisParent.parent().parent().attr("id");
            if (xyzColumn === "xLabel") {
                var str = "";
                str += "<li sort='0'>" + content + "</li>";
                str += "<li sort='1'>" + content + "(升序)</li>";
                str += "<li sort='2'>" + content + "(降序)</li>";
            } else if (xyzColumn === "yLabel") {
                var str = "";
                str += "<li sort='0'>" + content + "</li>";
                str += "<li sort='3'>" + content + "(求和)</li>";
                str += "<li sort='4'>" + content + "(最大值)</li>";
                str += "<li sort='5'>" + content + "(中位数)</li>";
                str += "<li sort='6'>" + content + "(平均值)</li>";
                str += "<li sort='7'>" + content + "(最小值)</li>";
                str += "<li sort='8'>" + content + "(计数)</li>";
                str += "<li sort='9'>" + content + "(计数不重复)</li>";
            }
            $(".moreColumnSelect", xMoreColumnMoreDiv).html(str);
            $(this).attr("opencolunmselect", "true");
            xMoreColumnMoreDiv.css("display", "block").css("left", location.left + "px").css("top", (location.top - 13) + "px");
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
            scroll: false
        });
        $(".chart-config-item-container").droppable({
            accept: "#chart-result-column-ul > li",
            hoverClass: "chart-config-item-container-active",
            drop: function (event, ui) {
                var thisParent = $(this).parent();
                var id = thisParent.attr("id");
                /*x轴 只允许一个*/
                if (id == "xLabel" && $(".chart-config-item", $(this)).length > 0) {
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
            }
        });
        $(".chart-config-item-container").sortable({opacity: 0.8, axis: "y", distance: 5});
        $(".chart-config-item-container").disableSelection();

        $(window).resize(function () {
            $("#grid-table").setGridWidth($("#home-data-jd-table-parent").width());
            $("#grid-table").setGridHeight($("#home-data-jd-table-parent").height() - 75);
        });

        $("#showButton").click(function () {


            var data = {
                xLabel: getXYZLabelValues("xLabel"),
                yLabel: getXYZLabelValues("yLabel"),
                zLabel: getXYZLabelValues("zLabel"),
                runDetailId: $("#runDetailId").val()
            }

            //check
            if ($.trim(data.xLabel).length < 1 || $.trim(data.yLabel).length < 1) {
                $.errorMsg("请先配置维度！");
                return;
            }

            var dataString = JSON.stringify(data);
            console.log(dataString);
            //  var x =
            commonAjaxEvents.commonPostAjax(chartDataUrl, {data: dataString}, $("#dataGridCommonCtrlDiv"), function (node, data) {
                console.log(data);
                console.log(data.data.series);
                if (data && data.data) {
                    //x轴
                    chartInstance.xAxis[0].setCategories(data.data.categories, false);
                    while (chartInstance.series.length > 0) {
                        chartInstance.series[0].remove(false);
                    }
                    for (var index = 0; index < data.data.series.length; index++) {
                        chartInstance.addSeries(data.data.series[index]);
                    }
                    chartInstance.redraw();
                    /* chartInstance.update({
                     xAxis:{
                     categories:data.data.categories
                     },
                     series:data.data.series
                     })*/
                }
            })

        })
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
    initChart.initDom();
    initChart.initEvent();
    initChart.initChart();
})

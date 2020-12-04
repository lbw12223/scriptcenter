//https://blog.csdn.net/zwx19921215/article/details/34442593
Highcharts.wrap(Highcharts.Chart.prototype, 'getSVG', function (proceed) {
    return proceed.call(this)
        .replace(
            /(fill|stroke)="rgba\(([ 0-9]+,[ 0-9]+,[ 0-9]+),([ 0-9\.]+)\)"/g,
            '$1="rgb($2)" $1-opacity="$3"'
        );
});
//导出chart的方法
function exportChart(type) {
    var filename="datadev_chart";
    var svg = $('#chartShowDiv').highcharts().getSVG();
    var form_id = "highchart_form_downLoad";
    if ($("#" + form_id).length > 0) {
        $("#" + form_id).remove();
    }
    //表单的形式去提交
    var temp = document.createElement("form");
    temp.action = "/scriptcenter/highchart/downLoad.ajax" + "?filename=" + filename;
    temp.method = "post";
    temp.style.display = "none";
    temp.id = form_id;
    //svg
    var svgInput = document.createElement("input");
    svgInput.name="svg";
    svgInput.value=svg;
    temp.appendChild(svgInput);
    //type
    var typeInput = document.createElement("input");
    typeInput.name="type";
    typeInput.value=type;
    temp.appendChild(typeInput);
    document.body.appendChild(temp);
    temp.submit();
    $("#" + form_id).remove();
}

if(HOME_COOKIE.getColorCookie() == "black"){
    Highcharts.setOptions({
        exporting: {},
        lang: {
            contextButtonTitle: "图表导出菜单",
            decimalPoint: ".",
            downloadJPEG: "下载JPEG图片",
            downloadPDF: "下载PDF文件",
            downloadPNG: "下载PNG文件",
            downloadSVG: "下载SVG文件",
            drillUpText: "返回 {series.name}",
            loading: "加载中",
            months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            noData: "没有数据",
            numericSymbols: ["千", "兆", "G", "T", "P", "E"],
            printChart: "打印图表",
            resetZoom: "恢复缩放",
            resetZoomTitle: "恢复图表",
            shortMonths: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
            thousandsSep: ",",
            weekdays: ["星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天"]
        },
        colors: "#4A99E3 #34CCAA #FFED4E #FB971A #EB3472 #764EBE #5262BC #5EB761 #D2DF4D #FBC642 #F5564A #86665A".split(" "),
        labels: {
            style: {
                color: '#757B86'
            }
        },
        chart: {
            borderColor: "#1D2327",
            backgroundColor: "#272B2F"
        },
        title: {
            style: {
                fontSize: "16",
                color: "#757B86",
                opacity: 0.9
            },
            align: "center"
        },
        subtitle: {
            style: {
                fontSize: "12",
                color: "#757B86",
                opacity: 0.8
            }
        },
        exporting: {
            buttons: {
                contextButton: {
                    symbol: 'menu',
                    text: '下载',
                    symbolFill: '#808080',
                    symbolStroke: '#808080',
                    // 自定义导出菜单项目及顺序
                    menuItems: [
                        // {
                        //     text: "下载PDF图片",
                        //     onclick: function() {
                        //         exportChart('application/pdf');
                        //     }
                        // },
                        {
                            text: "下载JPEG图片",
                            onclick: function() {
                                exportChart('image/jpeg');
                            }
                        },
                        {
                            text:"下载SVG图片",
                            onclick: function() {
                                exportChart('image/svg+xml');
                            }
                        },
                        {
                            text:"下载PNG图片",
                            onclick: function() {
                                exportChart( 'image/png');
                            }
                        }
                    ]
                }
            }
        },
        navigation:{
            //字体颜色：#A3A9B2 背景颜色：#363A40 hover蓝：#337ab7 #428bca 图表颜色：#252A2F
            menuStyle:{ "border": "0px solid #363A40", "background": "#363A40", "padding": "0px 0 #363A40" ,"box-shadow":"2px 2px 4px black"},
            menuItemStyle: {
                "padding": "0.5em 1em",
                "color": "#A3A9B2",
                "background": "#363A40"
            }
            ,
            menuItemHoverStyle:{
                "background": "#3286E9",
                "color": "#D7E3F7"
            }
            ,
            buttonOptions: {
                // symbolStroke: '#428bca',
                // theme: {
                //     states: {
                //         normal:{
                //             fill: '#337ab7'
                //         },
                //         hover: {
                //             fill: '#ffffff'
                //         },
                //         select: {
                //             fill: '#ffffff'
                //         }
                //     }
                // }
            }
        },
        xAxis: {
            gridLineColor: "#1D2327",
            title: {
                style: {
                    color: '#757B86'
                }
            },
            labels: {
                style: {
                    color: "#757B86"
                }
            },
            tickmarkPlacement: "on",
            tickColor: "#757B86",
            lineColor: "#757B86"
        },
        yAxis: {
            gridLineColor: "#1D2327",
            title: {
                style: {
                    color: '#757B86'
                }
            },
            labels: {
                style: {
                    color: "#757B86"
                }
            }
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            bar: {
                borderWidth: 0,
                borderRadius: 2
            },
            column: {
                borderWidth: 0,
                borderRadius: 2
            },
            area: {
                series: {
                    fillOpacity: 0.8
                }
            },
            pie: {
                borderWidth: null,
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    style: {
                        color: "#757B86",
                        fontWeight: "normal",
                        textOutline: "0px 0px #757B86"
                    }
                },
                showInLegend: true
            }
        },
        legend: {
            itemStyle: {
                color: '#757B86',
                opacity: 1
            },
            itemHoverStyle: {
                color: '#757B86',
                opacity: 0.8
            }
        }
    })
}else{
    Highcharts.setOptions({
        exporting: {
            formAttributes: {
                "acceptCharset": "UTF-8"
            }
        },
        lang: {
            contextButtonTitle: "图表导出菜单",
            decimalPoint: ".",
            downloadJPEG: "下载JPEG图片",
            downloadPDF: "下载PDF文件",
            downloadPNG: "下载PNG文件",
            downloadSVG: "下载SVG文件",
            drillUpText: "返回 {series.name}",
            loading: "加载中",
            months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            noData: "没有数据",
            numericSymbols: ["千", "兆", "G", "T", "P", "E"],
            printChart: "打印图表",
            resetZoom: "恢复缩放",
            resetZoomTitle: "恢复图表",
            shortMonths: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
            thousandsSep: ",",
            weekdays: ["星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天"]
        },
        colors: "#4A99E3 #34CCAA #FFED4E #FB971A #EB3472 #764EBE #5262BC #5EB761 #D2DF4D #FBC642 #F5564A #86665A".split(" "),
        labels: {
            style: {
                /*    color: '#757B86'*/
            }
        },
        chart: {
            /*   borderColor: "#1D2327",
               backgroundColor: "#FFFFFF"*/
        },
        title: {
            style: {
                fontSize: "16",
                opacity: 0.9
            },
            align: "center"
        },
        subtitle: {
            style: {
                fontSize: "12",
                opacity: 0.8
            }
        },
        exporting: {
            formAttributes: {
                "acceptCharset": "UTF-8"
            },
            buttons: {
                contextButton: {
                    symbol: 'menu',
                    text: '下载',
                    // 自定义导出菜单项目及顺序
                    menuItems: [
                        // {
                        //     text: "下载PDF图片",
                        //     onclick: function() {
                        //         exportChart('application/pdf');
                        //     }
                        // },
                        {
                            text: "下载JPEG图片",
                            onclick: function () {
                                exportChart('image/jpeg');
                            }
                        },
                        {
                            text: "下载SVG图片",
                            onclick: function () {
                                exportChart('image/svg+xml');
                            }
                        },
                        {
                            text: "下载PNG图片",
                            onclick: function () {
                                exportChart('image/png');
                            }
                        }
                    ]
                }
            }
        },
        navigation: {
            //字体颜色：#A3A9B2 背景颜色：#363A40 hover蓝：#337ab7 #428bca 图表颜色：#252A2F
            /*    menuStyle: {
                    "border": "0px solid #363A40",
                    "background": "#363A40",
                    "padding": "0px 0 #363A40",
                    "box-shadow": "2px 2px 4px black"
                },
                menuItemStyle: {
                    "padding": "0.5em 1em",
                    "color": "#A3A9B2",
                    "background": "#363A40"
                }
                ,
                menuItemHoverStyle: {
                    "background": "#3286E9",
                    "color": "#D7E3F7"
                }
                ,
                buttonOptions: {
                    // symbolStroke: '#428bca',
                    // theme: {
                    //     states: {
                    //         normal:{
                    //             fill: '#337ab7'
                    //         },
                    //         hover: {
                    //             fill: '#ffffff'
                    //         },
                    //         select: {
                    //             fill: '#ffffff'
                    //         }
                    //     }
                    // }
                }*/
        },
        xAxis: {
            /*    gridLineColor: "#1D2327",
                title: {
                    style: {
                        color: '#757B86'
                    }
                },
                labels: {
                    style: {
                        color: "#757B86"
                    }
                },
                tickmarkPlacement: "on",
                tickColor: "#757B86",
                lineColor: "#757B86"*/
        },
        yAxis: {
            /*     gridLineColor: "#1D2327",
                 title: {
                     style: {
                         color: '#757B86'
                     }
                 },
                 labels: {
                     style: {
                         color: "#757B86"
                     }
                 }*/
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            bar: {
                borderWidth: 0,
                borderRadius: 2
            },
            column: {
                borderWidth: 0,
                borderRadius: 2
            },
            area: {
                series: {
                    fillOpacity: 0.8
                }
            },
            pie: {
                /* borderWidth: null,
                 allowPointSelect: true,
                 cursor: 'pointer',
                 dataLabels: {
                     enabled: true,
                     style: {
                         color: "#757B86",
                         fontWeight: "normal",
                         textOutline: "0px 0px #757B86"
                     }
                 },
                 showInLegend: true*/
            }
        },
        legend: {
            /*    itemStyle: {
                    color: '#757B86',
                    opacity: 1
                },
                itemHoverStyle: {
                    color: '#757B86',
                    opacity: 0.8
                }*/
        }
    })
}



var default_chart_options = {
    subTitle: {
        text: null,
    },
    xAxis: {
        title: {
            text: null
        },
    },
    yAxis: {
        title: {
            text: null
        }
    },
    plotOptions: {
        series: {
            fillOpacity: 0.3
        }
    },
    series: [
        {
            name: '工人',
            data: [24916, 24064, 29742, 29851, 32490, 30282, 38121, 40434],
            animation: {
                duration: 1000
            }
        }, {
            name: '销售',
            data: [11744, 17722, 16005, 19771, 20185, 24377, 32147, 39387],
            animation: {
                duration: 1000
            }
        }, {
            name: '项目开发',
            data: [null, null, 7988, 12169, 15112, 22452, 34400, 34227],
            animation: {
                duration: 1000
            }
        }]
}
var default_pie_chart_options = {
    tooltip: {
        pointFormat: '占比: <b>{point.percentage:.4f}%</b> <br /> 数值:{point.value} '
    },
    series: [{
        name: 'Brands',
        colorByPoint: true,
        data: [{
            name: 'Chrome',
            value: '1000',
            y: 61.41,
        }, {
            name: 'Internet Explorer',
            value: "1000",
            y: 11.84
        }, {
            name: 'Firefox',
            value: "1000",
            y: 10.85
        }, {
            name: 'Edge',
            value: "1000",
            y: 4.67
        }, {
            name: 'Safari',
            value: "1000",
            y: 4.18
        }, {
            name: 'Sogou Explorer',
            value: "1000",
            y: 1.64
        }, {
            name: 'Opera',
            value: "1000",
            y: 1.6
        }, {
            name: 'QQ',
            value: "1000",
            y: 1.2
        }, {
            name: 'Other',
            value: "1000",
            y: 2.61
        }]
    }]
}
var default_bubble_chart_options = {
    subTitle: {
        text: null,
    },
    xAxis: {
        title: {
            text: null
        },
    },
    yAxis: {
        title: {
            text: null
        }
    },
    series: [{
        name: '数据列 1',
        // 每个气泡包含三个值，x，y，z；其中 x，y用于定位，z 用于计算气泡大小
        data: [[97, 36, 79], [94, 74, 60], [68, 76, 58], [64, 87, 56], [68, 27, 73], [74, 99, 42], [7, 93, 87], [51, 69, 40], [38, 23, 33], [57, 86, 31]]
    }, {
        name: '数据列 2',
        data: [[25, 10, 87], [2, 75, 59], [11, 54, 8], [86, 55, 93], [5, 3, 58], [90, 63, 44], [91, 33, 17], [97, 3, 56], [15, 67, 48], [54, 25, 81]]
    }, {
        name: '数据列 3',
        data: [[47, 47, 21], [20, 12, 4], [6, 76, 91], [38, 30, 60], [57, 98, 64], [61, 17, 80], [83, 60, 13], [67, 78, 75], [64, 12, 10], [30, 77, 82]]
    }]
}

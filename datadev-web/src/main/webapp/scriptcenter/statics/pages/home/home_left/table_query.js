function initTableSize() {
    $("#table-query-grid-table").setGridHeight($("#table-query-jd-table-parent").height() - 40);
    $("#table-query-grid-table").setGridWidth($("#table-query-jd-table-parent").width());
}


$(function () {

    var jqGridFlag = false;
    var allTableFlag = false;
    var allTableJqgrid = false;
    var columnGridDefaultWidth = 325;
    var allTableColSpiner = undefined;
    var E_RESIZE_STATUS = false;
    var S_RESIZE_STATUS = false;

    initMarket();
    initMyTableEvent();
    initAllTableEvent();
    initAllMarket();

    function initMarket() {
        commonAjaxEvents.commonPostAjax("/scriptcenter/config/getMarketByErp.ajax", {}, $("#marketSelect"), function (node, data) {
            $("#queryMarketSelect").empty();
            var options = "<option value=''></option>";
            if (data && data.obj) {
                for (var index = 0; index < data.obj.length; index++) {
                    var market = data.obj[index];
                    options += "<option data-ugdap='" + market.isUgdap + "' data-cluster='" + market.clusterCode + "' data-id='" + market.marketId + "' data-market-user='" + market.marketUser + "' value='" + market.marketId + "'>" + market.marketName + "</option>";
                }
            }
            $("#queryMarketSelect").append(options);
            $("#queryMarketSelect").select2({
                placeholder: '请选择集市'
            });
            changeAccount();
        });
    }

    function changeAccount(marketId) {
        if (marketId) {
            commonAjaxEvents.commonPostAjax("/scriptcenter/config/getAccountByErp.ajax", {marketId: marketId}, $("#accountCodeSelect"), function (node, data) {
                $("#queryAccountSelect").empty();
                var options = "<option value=''></option>";
                if (data && data.obj) {
                    for (var index = 0; index < data.obj.length; index++) {
                        var account = data.obj[index];
                        options += "<option  data-id='" + account.id + "'  value='" + account.code + "'>" + account.name + "</option>";
                    }
                }
                $("#queryAccountSelect").append(options);
                $("#queryAccountSelect").select2({
                    placeholder: '请选择生产账号'
                });
                changeDb();
            });
        } else {
            var options = "<option value=''></option>";
            $("#queryAccountSelect").empty().append(options);
            $("#queryAccountSelect").select2({
                placeholder: '请选择生产账号'
            });
            changeDb();
        }
    }


    function changeDb(marketId, accountCode) {
        if (marketId) {
            var data = {marketId: marketId};
            if (accountCode) {
                data.productionAccount = accountCode;
            }
            commonAjaxEvents.commonPostAjax("/scriptcenter/config/getDbByErp.ajax", data, $("#queryDbSelect"), function (node, data) {
                $("#queryDbSelect").empty();
                var options = "<option value=''></option>";
                if (data && data.obj) {
                    for (var index = 0; index < data.obj.length; index++) {
                        var db = data.obj[index];
                        options += "<option  data-id='" + db.dbid + "'  value='" + db.dbName + "'>" + db.dbName + "</option>";
                    }
                }
                $("#queryDbSelect").append(options);
                $("#queryDbSelect").select2({
                    placeholder: '请选择库'
                });
                changeTb();
            });
        } else {
            var options = "<option value=''></option>";
            $("#queryDbSelect").empty().append(options);
            $("#queryDbSelect").select2({
                placeholder: '请选择库'
            });
            changeTb();
        }
    }

    function changeTb(marketId, dbName, accountId) {

        $("#queryTableSelect").val("").select2({
            multiple: false,
            cache: true,
            // allowClear: true,
            placeholder: "请选择表",
            separator: ",",
            ajax: {
                url: "/scriptcenter/config/getTableByErp.ajax",
                dataType: 'json',
                method: 'post',
                cache: true,
                data: function (term) {
                    var data = {tbName: term};
                    if (dbName) {
                        data.dbName = dbName;
                    }
                    if (marketId) {
                        data.marketId = marketId;
                    }
                    if (accountId) {
                        data.productionAccountId = accountId;
                    }
                    return data;
                },
                results: function (data) {
                    return {
                        results: data.obj ? data.obj : []
                    };
                }
            },
            id: function (item) {
                return item.tbName
            },
            formatResult: function (item) {
                return item.tbName;
            },
            formatSelection: function (item) {
                return item.tbName;
            },
            dropdownCssClass: "bigdrop",
            formatSearching: function () {
                return "加载中..."
            },
            formatNoMatches: function (term, data) {
                return "没有匹配结果."
            },
            escapeMarkup: function (m) {
                return m;
            }
        });
        initColumn();
    }

    function initColumn(marketId, accountCode, dbName, tbName) {
        if (!jqGridFlag) {
            var _colModel = [
                {
                    name: 'columnName',
                    label: "列名",
                    sortable: false,
                },
                {
                    name: 'columnType',
                    label: "类型",
                    sortable: false,
                },
                {
                    name: 'comment',
                    label: "描述",
                    sortable: false,
                },
            ];
            var data = {};
            if (marketId && dbName && tbName) {
                data.marketId = marketId;
                data.dbName = dbName;
                data.tbName = tbName;
            }
            if (accountCode) {
                data.accountCode = accountCode;
            }
            jQuery("#table-query-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/config/getColumns.ajax',
                mtype: 'POST',
                postData: data,
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
                    $("#table-query-grid-table").setGridHeight($("#table-query-jd-table-parent").height() - 40);
                    $("#table-query-grid-table").setGridWidth($("#table-query-jd-table-parent").width());
                }
            });
            jqGridFlag = true;
        } else {

            if (marketId && dbName && tbName) {
                var data = {};
                data.marketId = marketId;
                data.dbName = dbName;
                data.tbName = tbName;
                if (accountCode) {
                    data.accountCode = accountCode;
                }
                jQuery("#table-query-grid-table").jqGrid("setGridParam", {
                    postData: data
                }).trigger("reloadGrid");
            } else {
                jQuery("#table-query-grid-table").jqGrid("clearGridData");
            }
        }

    }

    function initCol() {
        if ($("#queryTableSelect").val()) {
            jQuery("#table-query-grid-table").trigger("reloadGrid");
        }
    }

    function changeAllDb(marketId) {
        if (marketId) {
            commonAjaxEvents.commonPostAjax("/scriptcenter/config/getAllDbs.ajax", {marketId: marketId}, null, function (node, data) {
                var obj = data.obj;
                var options = "<option value=''>请选择所在库</option>";
                if (obj && obj.length > 0) {
                    for (var index = 0; index < obj.length; index++) {
                        options += "<option value='" + obj[index].dbName + "'>" + obj[index].dbName + "</option>";
                    }
                }
                $("#allTableDbSelect").empty();
                $("#allTableDbSelect").append(options);
                $("#allTableDbSelect").select2({
                    placeholder: "请选择所在库",
                });
            });
        } else {
            var options = "<option value=''>请选择所在库</option><option value='bdm'>bdm</option><option value='fdm'>fdm</option>" +
                "<option value='gdm'>gdm</option><option value='adm'>adm</option><option value='app'>app</option><option value='dim'>dim</option>" +
                "<option value='odm'>odm</option><option value='udm'>udm</option><option value='dev'>dev</option><option value='test'>test</option>";
            $("#allTableDbSelect").empty();
            $("#allTableDbSelect").append(options);
            $("#allTableDbSelect").select2({
                placeholder: "请选择所在库",
            });
        }

    }

    function initAllMarket() {
        $("#dataPreviewContent").appendTo($("#homdeIndexRightContent"));

        if (!allTableFlag) {
            allTableFlag = true;
            changeAllDb();
            commonAjaxEvents.commonPostAjax("/scriptcenter/buffalo/getAllMarket.ajax", {}, null, function (node, data) {
                var obj = data.obj;
                var options = "";//"<option value=''>请选择所在集市</option>";
                if (obj && obj.length > 0) {
                    for (var index = 0; index < obj.length; index++) {
                        options += "<option value='" + obj[index].marketId + "' data-cluster='" + obj[index].clusterCode + "' data-linuxuser='" + obj[index].linuxUser + "' data-ugdap='" + obj[index].isUgdap + "'>" + obj[index].marketName + "</option>";
                    }
                }
                $("#allTableMarketSelect").empty();
                $("#allTableMarketSelect").append(options);
                $("#allTableMarketSelect").select2({
                    placeholder: "请选择所在集市",
                });
            });
            $('#allTablePager').pagination({
                dataSource: [],
                callback: function (data, pagination) {
                    // console.log(data);
                    // console.log(pagination);
                }
            })
            $("#draggableColumnsContent").draggable({
                distance: 20,
                // helper: function () {
                //     var clone = $('<div class="moveLiClass">' + $(this).html() + '</div>');
                //     return clone;
                // },
                // containment: "#homeIndexMainContainer",
                scroll: false,
                iframeFix: true,
                helper: "clone",
                zIndex: 10000,
                revert: false,
                cursor: "move",
                cancel: ".draggable-content,#columnsSearchInput,.resize-content",
                appendTo: "#homdeIndexLeftMenu",
                start: function (event, ui) {   //开始的时候，执行的方法 ，比如添加css或是别的。
                    if (!$("#draggableColumnsContent").hasClass("init_move")) {
                        $("#draggableColumnsContent").addClass("init_move");
                        $(".draggable-columns-content").width($("#draggableColumnsContent").width())
                    }
                    $(".draggable-columns-content").height($("#draggableColumnsContent").height())

                    $("#draggableColumnsContent").hide();
                    $(".header-table-name").removeClass("pop-title");
                    $("#draggableColumnsContent").insertBefore($("#homdeIndexLeftMenu"));
                    $('.showTitleBox').remove();
                },
                stop: function (event, ui) {
                    var top = ui.position.top;
                    var left = ui.position.left;
                    $("#draggableColumnsContent").css({
                        top: top,
                        left: left,
                        // width:columnGridDefaultWidth
                    }).show();
                    $(".resize-content").show()
                    resetGridColumnSize();
                    $('.showTitleBox').remove();
                },
                drag: function (event, ui) {
                    var top = ui.position.top;
                    var left = ui.position.left;
                    var width = $("#draggableColumnsContent").width();
                    var height = $("#draggableColumnsContent").height();
                    var right = left + width + 5;
                    var bottom = top + height + 50;
                    var documentWidth = $(document).width();
                    var documentHeight = $(document).height();
                    if (top <= 5) {
                        ui.position.top = 5;
                    }
                    if (left <= 5) {
                        ui.position.left = 5;
                    }
                    if (right >= documentWidth) {
                        ui.position.left = documentWidth - width - 5;
                    }
                    if (bottom >= documentHeight) {
                        ui.position.top = documentHeight - height - 50;
                    }
                }
            });
        }
    }

    function showTableDetail(list) {
        $("#allTableContainer").empty();
        var html = "";
        for (var index = 0; index < list.length; index++) {
            html += getTemplate(list[index]);
        }
        $("#allTableContainer").append(html);
    }

    function getTemplate(data) {
        var item =
            "<div class='table-detail-group'>" +
            "   <div class='table-item-name pop-title' data-title='" + data.tbName + "'>" + data.tbName + "</div>" +
            "   <div class='table-item-comment pop-title' data-title='" + data.memo + "'>" + data.memo + "</div>" +
            "   <div class='table-item-category'>" +
            "       <div class='market-name pop-title' data-cluster='" + data.clusterCode + "' data-code='" + data.linuxUser + "' data-id='" + data.marketId + "' data-title='" + data.marketName + "'>集市：<span>" + data.marketName + "</span></div>" +
            "       <div class='db-name pop-title' data-title='" + data.dbName + "'>库：<span>" + data.dbName + "</span></div>" +
            "   </div>" +
            "   <div class='table-item-operate'>" +
            "       <div class='table-preview'>数据预览</div>" +
            "       <div class='table-structure'>表结构</div>" +
            "   </div>" +
            "</div>";
        return item;
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
                    sortable: false,
                },
                {
                    name: 'columnType',
                    label: "类型",
                    sortable: false,
                },
                {
                    name: 'comment',
                    label: "描述",
                    sortable: false,
                },
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
                width: '100%',
                autowidth: true,
                autoencode: true,
                height: "100%",
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

    function searchAllTable(marketId, dbName, searchWord) {
        allTableColSpiner = showSpinner($("#searchAllTabOrColSpinner"));
        var tips = '共搜索到<span id="tableSearchNum">0</span>个结果';
        if ($("#tableSearchNum").length == 0) {
            $(".all-table-num-content").html(tips);
        }
        $("#tableSearchNum").text(0);
        $('#allTablePager').pagination({
            dataSource: "/scriptcenter/config/getAllTables.ajax",
            totalNumberLocator: function (response) {
                $("#tableSearchNum").text(response.obj.total || 0);
                return response.obj.total;
            },
            pageSize: 10,
            totalNumber: 1,
            pageRange: 1,
            locator: "obj.list",
            ajax: {
                type: "POST",
                data: {
                    marketId: marketId,
                    dbName: dbName,
                    searchWord: $.trim(searchWord || ""),
                }
            },
            callback: function (data, pagination) {
                showTableDetail(data);
                hiddenSpinner(allTableColSpiner, $("#searchAllTabOrColSpinner"));
                console.log(data);
                console.log(pagination);
            }
        })
    }

    function initMyTableEvent() {
        $("#tableQueryApply").click(function () {
            window.open(_bdpDomain+"/planning/project/detail/"+top.window.projectSpaceId);
        })
        $("#toThink").click(function () {


            var marketOptions = $("option:selected", $("#queryMarketSelect"));

            var clusterCode = marketOptions.attr("data-cluster");
            var marketCode = marketOptions.attr("data-market-user");
            var dbName = $("option:selected", "#queryDbSelect").val()
            var tbName = $("#queryTableSelect").val();

            if ($.trim(clusterCode).length < 1 || $.trim(marketCode).length < 1 || $.trim(dbName).length < 1 || $.trim(tbName).length < 1) {
                return ;
            }
            var href = "http://bdp.jd.com/dataassets/table/detail.html?cluster=" + clusterCode + "&linuxUser=" + marketCode + "&dbName=" + dbName + "&tbName=" + tbName;
            window.open(href);

        })
        $("#queryMarketSelect").on("change", function () {
            var option = $("option:selected", $("#queryMarketSelect"));
            if (option.length > 0 && option.attr("value") != "") {
                var marketId = option.attr("data-id");
                var isUgdap = option.attr("data-ugdap");
                if (isUgdap == "true") {
                    $("#queryAccountSelect").val("");
                    $("#queryAccountSelect").prop("disabled", false);
                    changeAccount(marketId);
                } else {
                    $("#queryAccountSelect").html("<option value=''></option>").select2({
                        placeholder: '已选择默认生产账号'
                    });
                    $("#queryAccountSelect").prop("disabled", true);
                    changeDb(marketId)
                }
            } else {
                changeAccount();
            }
        })


        $("#queryAccountSelect").on("change", function () {
            var accountOption = $("option:selected", $("#queryAccountSelect"));
            var marketOption = $("option:selected", $("#queryMarketSelect"));
            if (marketOption.length > 0 && accountOption.length > 0) {
                var accountCode = accountOption.val();
                var marketId = marketOption.attr("data-id");
                changeDb(marketId, accountCode);
            } else {
                changeDb();
            }
        })

        $("#queryDbSelect").on("change", function () {
            var accountOption = $("option:selected", $("#queryAccountSelect"));
            var marketOption = $("option:selected", $("#queryMarketSelect"));
            var dbOption = $("option:selected", $("#queryDbSelect"));
            if (dbOption.length > 0 && marketOption.length > 0) {
                var accountId = (accountOption.length > 0 && accountOption.attr("data-id") != "") ? accountOption.attr("data-id") : null;
                var marketId = marketOption.attr("data-id");
                var dbName = dbOption.attr("value");
                changeTb(marketId, dbName, accountId);
            } else {
                changeTb();
            }
        })
        $("#queryTableSelect").on("change", function () {
            var marketOption = $("option:selected", $("#queryMarketSelect"));
            var accountOption = $("option:selected", $("#queryAccountSelect"));

            var dbOption = $("option:selected", $("#queryDbSelect"));
            var tbOption = $("#queryTableSelect");

            if (marketOption.length > 0 && dbOption.length > 0 && tbOption.length > 0) {
                var marketId = marketOption.attr("data-id");
                var dbName = dbOption.attr("value");
                var tbName = tbOption.val();
                var martCode = accountOption.val();
                initColumn(marketId, martCode, dbName, tbName);
            } else {
                initColumn();
            }
        })

        $("#tableQueryOuterContent").on("click", ".table-query-mode", function () {
            var node = $(this);
            node.addClass("active");
            node.siblings(".table-query-mode").removeClass("active");
            var targetId = node.attr("target");
            var target = $("#" + targetId);
            target.siblings(".table-query-content").hide();
            target.show();
        })
        // $("#homdeIndexLeftMenu")
        EleResize.on($("#homdeIndexLeftMenu")[0], initTableSize);
        $("#createSqlIcon").click(function () {
            var currentArt = $.dialog.open("/scriptcenter/devcenter/create_sql.html", {
                title: "一键生成SQL",
                lock: true,
                width: "630px",
                height: "80%",
                opacity: 0.5,
                esc: true,
                resize: false,
                close: function () {
                }
            });
            $.dialog.data("currentArt", currentArt);
            // $("#createSqlModal").modal("show")
        })
        $("#searchColContent input").bind("input propertychange", function () {
            jQuery("#table-query-grid-table").jqGrid("setGridParam", {
                postData: {
                    searchWord: $.trim($("#searchColContent input").val())
                }
            })
        });
        $("#searchColContent input").keydown(function (e) {
            if (e.keyCode == 108 || e.keyCode == 13) {
                initCol();
            }
        })
        $("#searchColContent span.ide-search").click(function (e) {
            initCol();
        })
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

    function hiddenSpinner(spinner, element) {
        $(element).hide();
        if (spinner) {
            spinner.spin();
        }
    }

    function initAllTableEvent() {
        $("#allTableMarketSelect").on("change", function () {
            var option = $("option:selected", $("#allTableMarketSelect"));
            if (option.length > 0 && option.attr("value") != "") {
                var marketId = option.val();
                changeAllDb(marketId);
            } else {
                changeAllDb();
            }
        })
        $("#allTableQueryBtn").click(function () {
            searchAllTable($("#allTableMarketSelect").val(), $("#allTableDbSelect").val(), $("#tableSearchInput").val(), 1, 10)
        })
        $("#tableSearchInput").keydown(function (event) {
            var e = event || window.event || arguments.callee.caller.arguments[0];
            if (e && e.keyCode == 13) {
                $("#allTableQueryBtn").click();
            }
        })
        $("#allTableContainer").on("click", ".table-structure", function () {
            var group = $(this).closest(".table-detail-group");
            var tbName = group.find(".table-item-name").text();
            var marketId = group.find(".market-name").attr("data-id");
            var dbName = group.find(".db-name").attr("data-title");
            var info = {
                group: group,
                tbName: tbName,
                marketId: marketId,
                dbName: dbName
            };
            // $("#draggableColumnsContent").attr("data-market-id", marketId);
            // $("#draggableColumnsContent").attr("data-tb-name", tbName);
            // $("#draggableColumnsContent").attr("data-db-name", dbName);
            // $("#draggableColumnsContent .header-table-name span").text(tbName);
            // $("#draggableColumnsContent .header-table-name").attr("data-title", tbName);
            //$("#draggableColumnsContent").show();
            var currentArt = $.dialog.open("/scriptcenter/devcenter/table_query_detail.html", {
                title: tbName,
                lock: true,
                width: "610px",
                height: "420px",
                opacity: 0.5,
                esc: true,
                resize: false,
                close: function () {
                }
            });
            $.dialog.data("currentArt", currentArt);
            $.dialog.data("info", info);
            // showAllTableColumns(marketId, dbName, tbName);
        })
        $("#columnsSearchButton").click(function () {
            $("#allGridColumn").jqGrid("setGridParam", {
                postData: {
                    searchWord: $.trim($("#columnsSearchInput").val())
                }
            }).trigger("reloadGrid")
        })
        $("#allTableContainer").on("click", ".table-preview", function () {
            // var dataPreviewContent = $("#dataPreviewContent");
            var group = $(this).closest(".table-detail-group");
            var tbName = group.find(".table-item-name").text();
            var marketId = group.find(".market-name").attr("data-id");
            var dbName = group.find(".db-name").attr("data-title");
            var clusterCode = group.find(".market-name").attr("data-cluster");
            var marketCode = group.find(".market-name").attr("data-code");
            var marketName = group.find(".market-name").attr("data-title");
            // var curDataId = marketId + ":" + dbName + ":" + tbName;
            var info = {
                clusterCode: clusterCode,
                marketCode: marketCode,
                dbName: dbName,
                tbName: tbName,
                marketName: marketName
            };
            var currentArt = $.dialog.open("/scriptcenter/devcenter/table_query_data_preview.html", {
                title: "数据预览——"+marketName+"-"+dbName,
                lock: true,
                width: "800px",
                height: "300px",
                opacity: 0.5,
                esc: true,
                resize: false,
                close: function () {
                }
            });
            $.dialog.data("currentArt", currentArt);
            $.dialog.data("info", info);

            // var dataId = dataPreviewContent.attr("data-id");
            // var marketName = group.find(".market-name").attr("data-title");
            // if (curDataId && curDataId != dataId) {
            //     showDataPreview(clusterCode, marketCode, dbName, tbName, marketName);
            // }
        })
        $("#draggableColumnsContent").on("click", ".header-close", function () {
            $("#draggableColumnsContent").hide();
            $("#draggableNameHeader").addClass("pop-title");
            $("#draggableColumnsContent").removeClass("init_move")
            $("#draggableColumnsContent").appendTo($("#allTableDetailContent")).css({
                left: 0,
                top: "auto",
                width: "100%",
                height: "75%"
            });
        })
        $("#draggableNameHeader span").dblclick(function () {
            $(this).addClass("datadevSelectedText");
        })
        // $("#dataPreviewContent").on("click", ".close-preview", function () {
        //     hideDataPreview();
        // })

        // addMouseMoveChangeEvent(function (event) {
        //     if (event.currentLeftMenuWidth) {
        //         resetGridColumnSize();
        //     }
        // })
        // addMouseMoveChangeEvent(function (event) {
        //     if (S_RESIZE_STATUS) {
        //         var endPageY = event.pageY || event.clientY;
        //         var offset = $("#draggableColumnsContent").offset();
        //         var top = offset.top;
        //         var height = endPageY - top;
        //         if (height > 0) {
        //             $("#draggableColumnsContent").height(height);
        //         }
        //         resetGridColumnSize();
        //     } else if (E_RESIZE_STATUS) {
        //         var endPageX = event.pageX || event.clientX;
        //         var offset = $("#draggableColumnsContent").offset();
        //         var left = offset.left;
        //         var width = endPageX - left;
        //         if (width > 0) {
        //             $("#draggableColumnsContent").width(width);
        //         }
        //         resetGridColumnSize();
        //     }
        // })
        $(document).on("mousedown", ".resize-content", function (event) {
            if ($(this).hasClass("resize-s-div")) {
                S_RESIZE_STATUS = true;
                E_RESIZE_STATUS = false;
            } else if ($(this).hasClass("resize-e-div")) {
                S_RESIZE_STATUS = false;
                E_RESIZE_STATUS = true;
            }
            $(".draggable-content").addClass("no-select")
        })
        $(document).mouseup(function (event) {
            S_RESIZE_STATUS = false;
            E_RESIZE_STATUS = false;
            $(".draggable-content").removeClass("no-select")
        })
        $("#columnsSearchInput").keydown(function (event) {
            // console.log(event.keyCode)
            var e = event || window.event || arguments.callee.caller.arguments[0];
            if (e && e.keyCode == 13) {
                $("#columnsSearchButton").click();
            }
        })
    }

})

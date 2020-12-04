$(function () {
    var onlineHistory = {};
    $("#gitProjectFilePath").on("change", function () {
        jQuery("#online-grid-table").jqGrid("setGridParam", {
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                gitProjectFilePath: $.trim($("#gitProjectFilePath").val())
            }
        })
    })
    onlineHistory.isInit = false;
    onlineHistory.init = function initData() {
        if(this.isInit){
            jQuery("#online-grid-table").jqGrid('setGridParam',{
                page:1
            }).trigger("reloadGrid");
        }else {
            this.isInit = true;
            var _colModel = [
                {
                    name: 'version',
                    label: "版本号",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        var version = getVersionFromScriptUrl(record.releaseInfoDev.scriptUrl);
                        var id = record.id;
                        var name = "";
                        var str = '<span class="run-item run-script-version" data-id="'+id+'" data-name="'+name+'" data-version="' + version + '"  >' + version + '</span>';
                        return str;
                    }
                },
                {
                    name:'projectName',
                    label:'项目空间',
                    sortable:false,
                    formatter: function (cellvalue, options, record) {
                        return record.projectName+"("+record.releaseInfoDev.projectId+")";
                    }
                },
                // {
                //     name:'runTypeStr',
                //     label:'上线方式',
                //     sortable:false
                // },
                {
                    name: 'releaseErpName',
                    label: "发布人",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        return record.releaseErpName+"("+record.releaseErp+")";
                    }
                },
                {
                    name: 'modified',
                    label: "审批生效时间",
                    sortable: false,
                },
                {
                    name: 'releaseStatusName',
                    label: "状态",
                    sortable: false,
                },{
                    name: 'desc',
                    label: "备注",
                    sortable: false,
                    formatter: function (cellvalue, options, record) {
                        return cellvalue||"--";
                    }
                }
            ];
            var pager_selector = "#online-grid-pager";
            jQuery("#online-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/scriptFile/getOnlineHistory.ajax',
                mtype: 'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                    gitProjectFilePath: $.trim($("#gitProjectFilePath").val())
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
                shrinkToFit: true,
                rownumbers: true,
                scrollOffset: 6,
                loadComplete: function (data) {
                    jqGrid.initWidth(jQuery, '#online-grid-table', "#online-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#online-grid-table").setGridHeight($("#online-jd-table-parent").height()-70);
                }
            });
        }

    }
    $("#online-history").data("init", onlineHistory);

    $("#online-grid-table").on("click", "span.run-script-version", function (event) {
        var version = $(this).attr("data-version");
        openScriptContent($("#gitProjectId").val(),$("#gitProjectFilePath").val(),version);
    });

    function getVersionFromScriptUrl(scriptUrl) {
        if (scriptUrl.indexOf("?") >= 0) {
            var params = scriptUrl.substring(scriptUrl.indexOf("?") + 1).split("&");
            for (var i = 0; i < params.length; i++) {
                if (params[i].startWith("version=")) {
                    return params[i].substring(params[i].indexOf("=") + 1)
                }
            }
        }
        return "";
    }

})


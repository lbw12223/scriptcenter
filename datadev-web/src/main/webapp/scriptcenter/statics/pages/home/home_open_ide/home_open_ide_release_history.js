$(function () {
    var onlineHistory = {};
    $("#gitProjectFilePath").on("change", function () {
        jQuery("#release-grid-table").jqGrid("setGridParam", {
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                gitProjectFilePath: $.trim($("#gitProjectFilePath").val())
            }
        })
    });
    $("#release-his-query").click(function () {
        jQuery("#release-grid-table").jqGrid('setGridParam', {
            page: 1,
            postData: {
                projectId: $("#projectSpaceMo option:checked").val(),
                scriptId: $.trim($("#scriptFileId").val())
            }
        }).trigger("reloadGrid");
    });
    onlineHistory.isInit = false;
    onlineHistory.init = function initData() {
        initSelect();
        if(this.isInit){
            jQuery("#release-grid-table").jqGrid('setGridParam',{
                page:1,
                postData: {
                    projectId: $("#projectSpaceMo option:checked").val(),
                    scriptId: $.trim($("#scriptFileId").val())
                }
            }).trigger("reloadGrid");
        }else {
            this.isInit = true;
            var _colModel = [
                {
                    name: 'version',
                    label: "版本",
                    sortable: false,
                    // formatter: function (cellvalue, options, record) {
                    //     var version = getVersionFromScriptUrl(record.releaseInfoDev.scriptUrl);
                    //     var id = record.id;
                    //     var name = "";
                    //     var str = '<span class="run-item run-script-version" data-id="'+id+'" data-name="'+name+'" data-version="' + version + '"  >' + version + '</span>';
                    //     return str;
                    // }
                },
                {
                    name:'projectName',
                    label:'项目空间',
                    sortable:false,
                    // formatter: function (cellvalue, options, record) {
                    //     return record.projectName+"("+record.projectId+")";
                    // }
                },
                {
                    name: 'releaseErp',
                    label: "发布人",
                    sortable: false,
                    // formatter: function (cellvalue, options, record) {
                    //     return record.releaseErpName+"("+record.releaseErp+")";
                    // }
                },
                {
                    name: 'releaseTime',
                    label: "提交时间",
                    sortable: false
                    // formatter: function (cellvalue, options, record) {
                    //     return record.releaseErpName+"("+record.releaseErp+")";
                    // }
                },
                {
                    name: 'releaseOperatorTypeDesc',
                    label: "变更类型",
                    sortable: false
                },
                {
                    name: 'releaseDesc',
                    label: "变更描述",
                    sortable: false
                },
                {
                    name: 'statusDesc',
                    label: "状态",
                    sortable: false
                }
            ];
            var pager_selector = "#release-grid-pager";
            jQuery("#release-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/diff/releaseRecord.ajax',
                mtype: 'POST',
                postData: {
                    projectId: $("#projectSpaceMo option:checked").val(),
                    scriptId: $.trim($("#scriptFileId").val())
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
                    jqGrid.initWidth(jQuery, '#release-grid-table', "#release-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#release-grid-table").setGridHeight($("#release-jd-table-parent").height()-70);
                }
            });
        }

    }
    $("#online-history").data("init", onlineHistory);

    $("#release-grid-table").on("click", "span.run-script-version", function (event) {
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

    function initSelect() {
        $("#projectSpaceMo").empty();
        var url = "/scriptcenter/test/getUserProjectSpace.ajax";
        commonAjaxEvents.commonPostAjax(url, {}, $("#projectSpaceMo"), function (node, data) {
            if (data && data.code === 0 && data.obj) {
                var options = "<option value=''></option>";
                for (var index = 0; index < data.obj.length; index++) {
                    var p = data.obj[index];
                    options += "<option value='" + p.id + "' >" + p.name+ "</option>"
                }
                $("#projectSpaceMo").append(options);

                var defaultVal = data.obj[0] ? data.obj[0].id : 0;
                $("#projectSpaceMo").val(defaultVal).select2();
            }
        })
    }

})


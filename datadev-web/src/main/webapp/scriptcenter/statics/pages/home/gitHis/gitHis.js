jQuery(function () {

    var pageInit = {
        initPage: function () {
            var _colModel = [
                {
                    name: 'created',
                    label: "提交时间",
                    sortable: false,
                    width: 50
                },
                {
                    name: 'creator',
                    label: "提交人",
                    sortable: false,
                    width: 40,
                    /*formatter: function (cellvalue, options, record) {
                        var id = record.id;
                        var name = "";
                        var str = '<span class="run-item run-script-version" data-id="'+id+'" data-name="'+name+'" data-version="' + record.version + '"  >' + record.version + '</span>';
                        return str;
                    }*/
                },
                {
                    name: 'comment',
                    label: "提交信息",
                    sortable: false,
                    width: 100,
                    formatter: function (cellvalue, options, record) {
                        var id = record.id;
                        var commitId = record.commitId;

                        var message = record.comment.length < 1 ? "N/A" : record.comment;

                        var str = '<span class="a-item script-commit" data-id="' + id + '" data-commitId="' + commitId + '" >' + message + '</span>';
                        return str;
                    }
                },
                {
                    name: 'commitId',
                    label: "提交ID",
                    sortable: false,
                    width: 50,
                    formatter: function (cellvalue, options, record) {
                        var commitId = record.commitId;
                        return commitId.substr(0, 8);
                    }

                }

            ];
            var pager_selector = "#run-grid-pager";
            jQuery("#run-grid-table").jqGrid({
                datatype: "json",
                url: '/scriptcenter/script/gitHis.ajax',
                mtype: 'POST',
                postData: {
                    gitProjectId: $.trim($("#gitProjectId").val()),
                    gitProjectFilePath: $.trim($("#gitProjectFilePath").val()),
                    dir: $.trim($("#isDir").val()),
                    keyWord: $.trim($("#keyWord").val()),
                    creator: $("#selectUploadErp").val()
                },
                colModel: _colModel,
                viewrecords: true,
                rowList: [5, 10, 20, 50, 100],
                rowNum: 10,
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
                    jqGrid.initWidth(jQuery, '#run-grid-table', "#run-jd-table-parent");
                    jqGrid.reset(jQuery);
                    $("#run-grid-table").setGridHeight($("#run-jd-table-parent").height() - 70);
                }
            });
        },
        initSelectUser: function () {
            var gitProjectId = $("#gitProjectId").val();
            var editorUrl = "/scriptcenter/project/gitMembers.ajax?gitProjectId=" + gitProjectId;

            function initSelect() {
                $("#selectUploadErp").empty();
                commonAjaxEvents.commonPostAjax(editorUrl, {}, $("#selectUploadErp"), function (node, data) {
                    if (data.code == 0 && data.obj) {
                        var options = "<option value=''></option>";
                        for (var index = 0; index < data.obj.length; index++) {
                            var node = data.obj[index];
                            options += "<option value='" + node.gitMemberUserName + "' >" + node.systemUserName + "</option>"
                        }
                        $("#selectUploadErp").append(options);
                        $("#selectUploadErp").select2(
                            {placeholder: '请选择提交人', allowClear: true}
                        );
                    }
                })
            }

            initSelect();


            $("#history-query").click(function () {

                jQuery("#run-grid-table").jqGrid('setGridParam', {
                    postData: {
                        gitProjectId: $.trim($("#gitProjectId").val()),
                        gitProjectFilePath: $.trim($("#gitProjectFilePath").val()),
                        dir: $.trim($("#isDir").val()),
                        keyWord: $.trim($("#keyWord").val()),
                        creator: $("#selectUploadErp").val(),
                    },
                    page: 1
                }).trigger("reloadGrid");
            })

            $("#run-jd-table-parent").on("click", "span.script-commit", function (event) {
                var id = $(this).attr("data-id");
                var commitId = $(this).attr("data-commitId");

                pageInit.openCommitDetail(id, commitId);

            })

        },
        openCommitDetail: function (id, commitId) {

            var isDir = $("#isDir").val();
            if(isDir === "true"){
                $.dialog.open("/scriptcenter/script/gitHisCommitTree.html?id=" + id + "&gitProjectFilePath=" + $("#gitProjectFilePath").val(), {
                    title: "提交详情",
                    lock: true,
                    width: "610px",
                    height: "548px",
                    resize: false,
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                })

            }else{
                $.dialog.open("/scriptcenter/script/gitHisDiff.html?id=" + id, {
                    title: "Git版本详情对比",
                    lock: true,
                    width: "70%",
                    height: "80%",
                    opacity: 0.5,
                    esc: false,
                    close: function () {
                    }
                })
            }
            if(1 > 0){
                return ;
            }
            commonAjaxEvents.commonPostAjax("/scriptcenter/script/countGitHisCommit.ajax?commitId=" + commitId, {}, null, function (node, data) {
                if (data.obj * 1 == 1) {
                    $.dialog.open("/scriptcenter/script/gitHisDiff.html?id=" + id, {
                        title: "Git版本详情对比",
                        lock: true,
                        width: "70%",
                        height: "80%",
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    })
                    return;
                }
                if (data.obj * 1 > 1) {
                    $.dialog.open("/scriptcenter/script/gitHisCommitTree.html?id=" + id, {
                        title: "提交详情",
                        lock: true,
                        width: "610px",
                        height: "548px",
                        resize: false,
                        opacity: 0.5,
                        esc: false,
                        close: function () {
                        }
                    })
                    return;
                }
            })


        }
    }

    pageInit.initPage();
    pageInit.initSelectUser();

})

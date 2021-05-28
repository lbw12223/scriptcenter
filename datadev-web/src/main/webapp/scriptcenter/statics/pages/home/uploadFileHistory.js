$(function() {
    var uploadHis = {};
    uploadHis.init = function initData() {
        initSelect();
        var _colModel = [
            {
                name: 'id',
                hidden:true,
            },
            {
                name: 'creator',
                label: "上传人",
                sortable: false,
            },
            {
                name: 'created',
                label: "上传时间",
                sortable: false,
            },
            {
                name: 'description',
                label:'描述',
                sortable: false,
            },
            {
                name: 'fileCount',
                label: "文件个数",
                sortable: false,
                formatter: function (cellvalue, options, record) {
                    return "<a href='javascript:openFileDetail(" + record.id + ")'>" + cellvalue + "</a>"
                },
            },
        ];
        var pager_selector = "#uploadFile-grid-pager";
        jQuery("#uploadFile-grid-table").jqGrid({
            datatype: "json",
            url: '/scriptcenter/project/uploadHis.ajax',
            mtype: 'POST',
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                creator: $("#selectUploadErp").val(),
                startTime: $("#uploadFileStartTime").val(),
                endTime: $("#uploadFileEndTime").val(),
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
            forceFit:true,
            loadComplete: function (data) {
                jqGrid.initWidth(jQuery, "#uploadFile-grid-table", "#uploadFile-jd-table-parent");
                jqGrid.reset(jQuery);
                $("#uploadFile-grid-table").setGridHeight($("#uploadFile-jd-table-parent").height()-195);
            }
        });
        $(window).resize(function () {
            $("#uploadFile-grid-table").setGridWidth($("#uploadFile-jd-table-parent").width());
            $("#uploadFile-grid-table").setGridHeight($("#uploadFile-jd-table-parent").height()-195);
        });
    }

    /*datadev_user_common.selectUpload($("#selectUploadErp"), false);*/
    $('#uploadFileStartTime').datetimepicker({
        autoclose: true,
        language: "zh-CN",
        minView: 0,
        minuteStep: 5,
        format: 'yyyy-mm-dd HH:ii:00',
        todayBtn: true,
        clearBtn: true
    });
    $('#uploadFileEndTime').datetimepicker({
        autoclose: true,
        language: "zh-CN",
        minView: 0,
        minuteStep: 5,
        format: 'yyyy-mm-dd HH:ii:00',
        todayBtn: true,
        clearBtn: true
    });
    $("#uploadFileStartTime,#uploadFileEndTime,#selectUploadErp").on("change", function () {
        if (!timeRangeValid($("#uploadFileStartTime").val(), $("#uploadFileEndTime").val())) {
            $("#timeRangeChecker").attr("style", "display: block; color: red !important; float: left; padding-left: 85px;");
            return
        }
        $("#timeRangeChecker").attr("style", "display: none");
        jQuery("#uploadFile-grid-table").jqGrid("setGridParam", {
            postData: {
                gitProjectId: $.trim($("#gitProjectId").val()),
                startTime: $("#uploadFileStartTime").val(),
                endTime: $("#uploadFileEndTime").val(),
                creator: $("#selectUploadErp").val()
            }
        })
    });
    $("#uploadFileSelect").click(function () {
        if (!timeRangeValid($("#uploadFileStartTime").val(), $("#uploadFileEndTime").val())) {
            $.errorMsg("结束时间不能大于开始时间");
            return
        }
        jQuery("#uploadFile-grid-table").trigger("reloadGrid");
    });

    $("#uploadFileHisDiv").data("init", uploadHis);
    $("#uploadFileHisDiv").data("init").init();

})

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
                {placeholder: '请选择上传人'}
            );
        }
    })
}


function openFileDetail(id) {
    $.dialog.open("/scriptcenter/project/fileDetail.html?scriptUpLoadId="+ id,{
        title: "上传文件详情",
        lock: true,
        width: "900px",
        height: "600px",
        opacity: 0.5,
        esc: false,
        close: function () {
        }
    });
}

function timeRangeValid(start, end) {
    return !start || !end || start <= end;
}
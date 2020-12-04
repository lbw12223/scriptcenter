jQuery(function () {

    var initPage = {
        init: function () {
            $("#upFile").click(function () {
                var dirFiles = $("#fileFolder")[0].files;
                $("#oneTemp").MutiFile(
                    {
                        files: dirFiles,
                        url: '/scriptcenter/test/saveFile.ajax',
                        threadLimit: 1,
                        sizeLimit: 1, /*单个文件大小限制M*/
                        showLimit: 105, /*列表最多显示的文件个数*/
                        fileTypeFilter: "all", /*过滤文件类型*/
                        userParams: {
                            gitProjectId: 1000
                        },
                        fileItemUpFinishCallBack: function (node) {
                        }
                    }
                )
            })
            $("#run").click(function () {
                $("#oneTemp").MutiFile("run");
            })
        }
    }
    initPage.init();
})
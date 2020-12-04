$(function () {
    var currentArt = $.dialog.data("currentArt");
    var addScript = $.dialog.data("addScript");//直接就可以调用啦
    var treeNode = $.dialog.data("treeNode");
    var selectedProjectId = $.dialog.data("selectedProjectId");//直接就可以调用啦

    var selected = false;
    $("#txtNewScriptDiv").on("click", ".round-wrap", function () {
        if (!selected) {
            selected = true;
            console.log("this:"+$(this));
            var dataType = $(this).attr("data-type");
            console.log("dataType:" + dataType);
            if (dataType) {
                var script = getScriptObj(dataType);
                addScript(script, treeNode, selectedProjectId, script.default);
            }
            currentArt.close();
        }
    })
});
$(function () {
    $(".well-come-create-file-ul .blankScript").click(function () {
        var scriptType = $(this).parent().attr("data-index");
        var pythonType = $(this).parent().attr("data-python");
        addByWelcome(scriptType,pythonType);
    })
    $(".well-come-create-file-ul").on("click", "li span.templateScript", function () {
        var selectedNode = getSelectedNode();
        var treeNode = selectedNode? (selectedNode.isParent?selectedNode:selectedNode.getParentNode()):null;
        templateModal($(this),treeNode);
    });
    function addByWelcome(scriptType,pythonType) {
        var script = getScriptObj(scriptType,pythonType);
        if(!hasProjectStatus){
            askNewGit();
        }else {
            var applicationId = getSelectedProjectId();
            addScript(script, null, applicationId,script.default);
        }
    }
})
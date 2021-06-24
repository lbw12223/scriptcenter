var QIAN_KUN = undefined;

window["bdp-qiankun"] = {
    mount: function (msg) {
        console.log("call .....llll")
        QIAN_KUN = msg;
    }
}

function openScript(nowGitProjectId, path, name) {
    if (!path) {
        top.$.errorMsg("脚本path为空，不能打开脚本");
    }
    if (!name) {
        var index = path.lastIndexOf("/");
        name = index != -1 ? path.substring(index + 1) : path;
    }


    var params = {
        url: "/scriptcenter/devcenter/script_edit.html?gitProjectFilePath=" + path + "&gitProjectId=" + nowGitProjectId,
        icon: '',
        title: name,
        key: getKey(nowGitProjectId, path),
        type: 'iframe',
        closeConfirm: false
    }

    QIAN_KUN && QIAN_KUN.utils.addTab(params)
}

function updateQianKunTabByThis(gitProjectId, oldPath, newPath ) {

    var params = {
        url: "/scriptcenter/devcenter/script_edit.html?gitProjectFilePath=" + newPath + "&gitProjectId=" + gitProjectId,
        icon: '',
        title: "sssssssss",
        key: getKey(gitProjectId, newPath),
        type: 'iframe',
        closeConfirm: false
    }
    QIAN_KUN.utils.updateTab(params);


}

function updateQianKunTab(dataObj) {

    var obj = dataObj.obj;

    while (obj.children && obj.children.length > 0) {
        obj = obj.children[0];
    }
    var path = obj.path;
    var gitProjectId = obj.gitProjectId;
    var name = obj.name;

    var params = {
        url: "/scriptcenter/devcenter/script_edit.html?gitProjectFilePath=" + path + "&gitProjectId=" + gitProjectId,
        icon: '',
        title: name,
        key: getKey(gitProjectId, path),
        type: 'iframe',
        closeConfirm: false
    }


    QIAN_KUN.utils.updateTab(params)
}





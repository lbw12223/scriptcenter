//这里要跟后端一致
//pythonType: 1:python2  2:python3

var scriptTypeArr = [

    {
        scriptType: "1",
        name: "SQL",
        icon: "icon_sql",
        canEdit: true,
        suffix: "sql",
        iconClass: "ide-left-sql",
        default: "",
        aceMode:"sql",
        canRun:true
    },
    {
        scriptType: "2",
        name: "Shell",
        icon: "icon_shell",
        canEdit: true,
        suffix: "sh",
        iconClass: "ide-left-shell",
        default: "#!/bin/bash\n",
        aceMode:"sh",
        canRun:true
    },
    {
        scriptType: "3",
        name: "Python",
        typeName:"Python 2.x",
        pythonType: "1",
        canEdit: true,
        icon: "icon_python",
        suffix: "py",
        iconClass: "ide-left-python",
        default: "#!/usr/bin/env python\n# -*- coding: utf-8 -*-\n",
        aceMode:"python",
        canRun:true
    },
    {
        scriptType: "3",
        name: "Python",
        pythonType: "2",
        canEdit: true,
        typeName:"Python 3.x",
        icon: "icon_python",
        suffix: "py",
        iconClass: "ide-left-python",
        default: "#!/usr/bin/env python3\n# -*- coding: utf-8 -*-\n",
        aceMode:"python",
        canRun:true
    },
    {scriptType: "4", name: "Zip", icon: "icon_zip", canEdit: false, suffix: "zip", iconClass: "ide-left-zip",canRun:true},
    {scriptType: "5", name: "UnOp", icon: "icon_unkown", canEdit: false, suffix: "", iconClass: "ide-left-hs",canRun:false},
    {scriptType: "6", name: "Java", icon: "icon_java", canEdit: true, suffix: "java", iconClass: "ide-left-hs",aceMode:"java",canRun:false},
    {
        scriptType: "7",
        name: "Txt",
        icon: "icon_txt",
        canEdit: true,
        suffix: "txt",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"text",
        canRun:false
    },
    {
        scriptType: "8",
        name: "Xml",
        icon: "icon_xml",
        canEdit: true,
        suffix: "xml",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"xml",
        canRun:false
    },
    {scriptType: "9", name: "Html", icon: "icon_html", canEdit: true, suffix: "html", iconClass: "ide-left-hs",aceMode:"html"},
    {
        scriptType: "10",
        name: "Csv",
        icon: "icon_csv",
        canEdit: true,
        suffix: "csv",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"text",
        canRun:false
    },
    {scriptType: "11", name: "Jar", icon: "icon_jar", canEdit: false, suffix: "jar", iconClass: "ide-left-hs",canRun:false},
    {scriptType: "12", name: "Groovy", icon: "icon_unkown", canEdit: true, suffix: "groovy", iconClass: "ide-left-hs",aceMode:"groovy",canRun:false},
    {scriptType: "13", name: "Javscript", icon: "icon_unkown", canEdit: true, suffix: "js", iconClass: "ide-left-hs",aceMode:"javascript",canRun:false},
    {scriptType: "14", name: "Css", icon: "icon_unkown", canEdit: true, suffix: "css", iconClass: "ide-left-hs",aceMode:"css",canRun:false},
    {
        scriptType: "15",
        name: "Prop",
        icon: "icon_prop",
        canEdit: true,
        suffix: "properties",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"properties",
        canRun:false
    },
    {
        scriptType: "16",
        name: "Conf",
        icon: "icon_conf",
        canEdit: true,
        suffix: "conf",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"text",
        canRun:false
    },
    {
        scriptType: "17",
        name: "Ini",
        icon: "icon_ini",
        canEdit: true,
        suffix: "ini",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"ini",
        canRun:false
    },
    {
        scriptType: "18",
        name: "Yaml",
        icon: "icon_yaml",
        canEdit: true,
        suffix: "yaml",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"yaml",
        canRun:false
    },
    {
        scriptType: "19",
        name: "Cfg",
        icon: "icon_cfg",
        canEdit: true,
        suffix: "cfg",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"text",
        canRun:false
    },
    {
        scriptType: "20",
        name: "Json",
        icon: "icon_json",
        canEdit: true,
        suffix: "json",
        iconClass: "ide-left-hs",
        limitSize: 1024 * 1024 * 2,
        aceMode:"json",
        canRun:false
    },
    {scriptType: "-3", name: "目录打开", icon: "icon01", canEdit: false, suffix: "", iconClass: "ide-wjjdk"},
    {scriptType: "-1", name: "目录关闭", icon: "icon01", canEdit: false, suffix: "", iconClass: "ide-wjj"},
    {scriptType: "-2", name: "目录方式运行", icon: "icon00", canEdit: false, suffix: "", iconClass: "ide-left-python"}
];

function getScriptObj(scriptType, pythonType) {
    var defaultScript = undefined;
    for (var index = 0; index < scriptTypeArr.length; index++) {
        var script = scriptTypeArr[index];
        if (script.scriptType == scriptType && (!pythonType || pythonType == 0 || script.pythonType == pythonType)) {
            return script;
        }
        if (script.scriptType == -1) {
            defaultScript = script;
        }
    }
    return defaultScript;
}
function getScriptByIndex(index) {
    return scriptTypeArr[index];
}

function canEdit(scriptType) {
    var script = getScriptObj(scriptType);
    return script && script.canEdit || false;
}

var sizeofStr = function (str) {
    var total = 0,
        charCode,
        i,
        len;
    for (i = 0, len = str.length; i < len; i++) {
        charCode = str.charCodeAt(i);
        if (charCode <= 0x007f) {
            total += 1;
        } else if (charCode <= 0x07ff) {
            total += 2;
        } else if (charCode <= 0xffff) {
            total += 3;
        } else {
            total += 4;
        }
    }
    return total;
}

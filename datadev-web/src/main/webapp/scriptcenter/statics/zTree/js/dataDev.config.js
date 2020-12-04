//这里要跟后端一致scriptType: 1 sql 2 shell  3 python
//pythonType: 1:python2  2:python3
var scriptTypeArr = [
    {scriptType: "3", name: "Python 2.x", pythonType: "1", icon: "icon_python", suffix: "py",iconClass:"ide-left-python"},
    {scriptType: "3", name: "Python 3.x", pythonType: "2", icon: "icon_python", suffix: "py",iconClass:"ide-left-python"},
    {scriptType: "1", name: "HIVE SQL", icon: "icon_sql", suffix: "sql",iconClass:"ide-left-sql"},
    {scriptType: "2", name: "Shell", icon: "icon_shell", suffix: "sh",iconClass:"ide-left-shell"},
    {scriptType: "4", name: "Zip", icon: "icon_zip", suffix: "zip",iconClass:"ide-left-zip"},
    {scriptType: "5", name: "UnOp", icon: "icon_unkown", suffix: "",iconClass:"ide-left-hs"},
    {scriptType: "6", name: "Java", icon: "icon_java", suffix: "java",iconClass:"ide-left-hs"},
    {scriptType: "7", name: "Txt", icon: "icon_txt", suffix: "txt",iconClass:"ide-left-hs"},
    {scriptType: "8", name: "Xml", icon: "icon_xml", suffix: "xml",iconClass:"ide-left-hs"},
    {scriptType: "9", name: "Html", icon: "icon_html", suffix: "html",iconClass:"ide-left-hs"},
    {scriptType: "10", name: "Csv", icon: "icon_csv", suffix: "csv",iconClass:"ide-left-hs"},
    {scriptType: "11", name: "Jar", icon: "icon_unkown", suffix: "jar",iconClass:"ide-left-hs"},
    {scriptType: "12", name: "Groovy", icon: "icon_unkown", suffix: "groovy",iconClass:"ide-left-hs"},
    {scriptType: "13", name: "Javscript", icon: "icon_unkown", suffix: "js",iconClass:"ide-left-hs"},
    {scriptType: "14", name: "Css", icon: "icon_unkown", suffix: "css",iconClass:"ide-left-hs"},
    {scriptType: "15", name: "Prop", icon: "icon_prop", suffix: "properties",iconClass:"ide-left-hs"},
    {scriptType: "-3", name: "目录打开", icon: "icon01", suffix: "",iconClass:"ide-wjjdk"},
    {scriptType: "-1", name: "目录关闭", icon: "icon01", suffix: "",iconClass:"ide-wjj"},
    {scriptType: "-2", name: "目录方式运行", icon: "icon00", suffix: "",iconClass:"ide-left-python"}

];
/**
 * 获取节点样式
 * @param scriptType
 * @param pythonType
 * @returns {*}
 */
function getScriptObj(scriptType, pythonType) {
    var defaultScript = undefined;
    for (var index = 0; index < scriptTypeArr.length; index++) {
        var script = scriptTypeArr[index];
        if (script.scriptType == scriptType && (!pythonType || scriptType.pythonType == pythonType)) {
            return script;
        }
        if (script.scriptType == -1) {
            defaultScript = script;
        }
    }
    return defaultScript;
}
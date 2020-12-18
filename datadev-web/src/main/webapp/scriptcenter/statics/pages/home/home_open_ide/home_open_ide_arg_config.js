var updateArgsUrl = "/scriptcenter/scriptFile/saveArgs.ajax";
var argValueMap = new Map();
var scriptType = $("#scriptType").val()
var argTable = $("#argTable");
var argTbody = $("#argTbody");
var mergeTr = '<tr class="mergeTr"> <td colspan="3" class="merge">无参数 </td> </tr>';
$("#addArgs").click(function () {
    addOneArg();
})

function addOneArg() {
    $(".mergeTr", argTbody).remove();
    var countTr = $("tr", argTbody).length;
    var html = "<tr data-index='" + (countTr + 1) + "'><td>" + (countTr + 1) + "</td>";
    if (scriptType * 1 === 1) {
        html += "<td>" + '<span class="argName">参数名称</span>' + "</td>";
        html += "<td>" + '<input class="bdp-form-control argValue" value=""  placeholder="参数值" autocomplete="off" style="width:300px">' + "</td>";
        html += "</tr>";
    } else {
        html += "<td>" + '<input class="bdp-form-control argValue" value=""  placeholder="参数值" autocomplete="off" style="width:300px">' + "</td>";
        html += "<td><div class='argTools '>" + $("#addOpt").html() + "</div></td></tr>";
    }
    argTbody.append(html);
}

/**
 * 获取匹配到的参数列表
 * @param content
 */
function getMatchArgs(content) {
    var result = new Map();
    var arrays = [];
    var reg = /\${(.*?)}/igm;
    content = content.replace(/--.*/gi, "--");
    var matchResult = content.match(reg);
    if (matchResult != null && matchResult.length > 0) {
        for (var index = 0; index < matchResult.length; index++) {
            var key = $.trim(matchResult[index].substr(2, matchResult[index].length - 3));
            if (key.length > 0 && !result.containsKey(key)) {
                result.put(key, "");
                arrays.push(key);
            }
        }
    }
    return arrays;
}

/**
 * sql 类型的参数获取params - value
 */
function sqlGetParamValues() {
    var argTbody = $("#argTbody");
    var trs = $("tr", argTbody);
    var params = new Map();
    trs.each(function () {
        var node = $(this);
        var argName = $(".argName", node).html();
        var argValue = $(".argValue", node).val();
        params.put(argName, argValue)
    })
    return params;
}

function notifyArgContentChange(editValue) {
    if (scriptType * 1 === 1) {
        //sql
        //获取已经设置好的参数，重新设置或者添加
        var args = getMatchArgs(editValue);
        // var argsValueMap = sqlGetParamValues(editValue);

        if (args.length > 0) {
            initSqlParamsTbody(args);
        } else {
            var mergeTrTemp = $(".mergeTr", argTbody);
            if (mergeTrTemp.length <= 0) {
                argTbody.html(mergeTr);
            }
        }
    }
}

/**
 * 初始化 sql参数
 * @param args
 */
function initSqlParamsTbody(args) {
    $(".mergeTr", argTbody).remove();
    argTbody.html("");
    for (var index = 0; index < args.length; index++) {
        var trs = "<tr>";
        var argValue = argValueMap.get(args[index]);
        argValue = argValue ? argValue : "";
        trs += "<td>" + (index + 1) + "</td>";
        trs += "<td><span class='argName'>" + args[index] + "</span></td>";
        trs += "<td><input class='bdp-form-control argValue'  placeholder='参数值' autocomplete='off' style='width:300px'></td>";
        trs += "</tr>";
        var tr = $(trs);
        $(".argValue", tr).val(argValue);
        argTbody.append(tr)
    }

}

argTable.on("click", ".iconRemoveArg", function (event) {
    var _this = $(this);
    var tr = _this.parent().parent().parent();
    tr.remove();
    var countTr = $("tr", argTbody).length;
    if (countTr < 1) {
        argTbody.append(mergeTr);
        return;
    }
    var index = 1;
    var trs = $("tr", argTbody);
    trs.each(function () {
        var _this = $(this);
        var td = $("td:first", _this);
        td.html(index++)
    })
})
argTable.on("keyup", ".argValue", function (event) {
    var _this = $(this);
    if (scriptType * 1 === 1) {
        var tr = _this.parent().parent();

        var argName = $(".argName", tr).html();
        var argValue = $(".argValue", tr).val();
        argValueMap.put(argName, argValue);
    }


})

function valiateStarShellPath() {
    var starShellPath = getStartShellPath();
    if (starShellPath.length < 1) {
        return false;
    }
    return true;
}

function getStartShellPath() {
    if ($("#startShellPath").length < 1) {
        return "";
    }
    var starShellPath = $.trim($("#startShellPath").val());
    return starShellPath;
}

function validateArgs() {
    /*    var trs = $("tr", argTbody);
     var isValidate = true;
     trs.each(function (index) {
     var _this = $(this);
     if (!_this.hasClass("mergeTr")) {
     var input = $("input", _this);
     if ($.trim(input.val()).length < 1) {
     isValidate = isValidate && false;
     parent.$.confirmMsg({content: "第" + (index + 1) + "行参数配置为空!"});
     return false;
     }
     }
     })
     return isValidate;*/
    return true;
}

function getCgroupArgs() {
    var cgroupArgs = {};
    var cgroupMemoryLimit = 10;
    var cgroupCpuLimit = 5;
    var cgroupLimit = 1;

    if ($("#cgroupMemoryLimit").length > 0) {
        cgroupMemoryLimit = $("#cgroupMemoryLimit").val();
        cgroupCpuLimit = $("#cgroupCpuLimit").val();
        cgroupLimit = 1;
    }
    cgroupArgs.cgroupMemoryLimit = cgroupMemoryLimit;
    cgroupArgs.cgroupCpuLimit = cgroupCpuLimit;
    cgroupArgs.cgroupLimit = cgroupLimit;
    return cgroupArgs;
}

function getRunArgs() {
    var trs = $("tr:not(.mergeTr)", argTbody);
    var runArgs = {};
    var indexArgs = 0;
    trs.each(function () {
        var _this = $(this);
        var input = $(".argValue", _this);
        var value = $.trim(input.val());
        if (scriptType * 1 == 1) {
            var argName = $(".argName", _this).html();
            runArgs[argName] = value;
        } else {
            runArgs[++indexArgs] = value;
        }
    })
    return runArgs;
}

function showArgs(args, startShellPath, cgroupArgs) {
    if (args) {
        try {
            var object = JSON.parse(args);
            console.log(object);
            var argName = [];
            for (var i in object) {
                var str = object[i];
                argName.push(i);
                console.log("value:" + str);
                $("#addArgs").click();
                $("#argTbody tr:eq(" + (i - 1) + ")").find("input.argValue").val(str);
                argValueMap.put(i, str);
                i++;
            }
            if (scriptType * 1 == 1) {
                //sql
                initSqlParamsTbody(argName);
            }
        } catch (e) {
            console.error("error:", e);
        }
    }
    if (startShellPath) {
        $("#startShellPath").val(startShellPath);
    }

    if(parent.cg === "true"){
        $("#cgroupConfigDiv").css("display","block");
    }
    //sql
    var cgroupMemoryLimit = 10;
    var cgroupCpuLimit = 5;
    if (cgroupArgs != null) {
        var cgroupObject = JSON.parse(cgroupArgs);
        cgroupMemoryLimit = cgroupObject.cgroupMemoryLimit;
        cgroupCpuLimit = cgroupObject.cgroupCpuLimit;
    }

    $("#cgroupMemoryLimit").val(cgroupMemoryLimit);
    $("#cgroupCpuLimit").val(cgroupCpuLimit);


}

function initArgs() {
    commonAjaxEvents.commonPostAjax(getInfoUrl, {
        gitProjectId: $("#gitProjectId").val(),
        gitProjectFilePath: $("#gitProjectFilePath").val()
    }, null, function (node, data) {
        if (data && data.obj) {
            // $("#script-detail-app").val(data.obj.applicationName)

            setDetailAttr(data.obj);
            var args = data.obj.args;

            var startShellPath = data.obj.startShellPath;
            var cgroupArgs = data.obj.cgroupArgs
            showArgs(args, startShellPath, cgroupArgs);
            if (needShowArgs) {
                $("#rightDiv").JdDataDevTab("active", 0);
            }
            needShowArgs = false;
        }
    })
}

function saveArgs(callback) {
    if (validateArgs()) {
        commonAjaxEvents.commonPostAjax(
            updateArgsUrl,
            {
                gitProjectId: $("#gitProjectId").val(),
                gitProjectFilePath: $("#gitProjectFilePath").val(),
                args: JSON.stringify(getRunArgs()),
                startShellPath: $("#startShellPath").length > 0 ? $("#startShellPath").val() : "",
                description: "",
                cgroupArgs: JSON.stringify(getCgroupArgs())
            }
            , null, function (node, data) {
                callback && callback();
            });
    }
}

$(function () {
    $("#updateScriptArgs").click(function () {
        saveArgs(function () {
            $.successMsg("修改成功!");
        })
    })
    initArgs();
})

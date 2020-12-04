var editor;
$(function () {
    // var editor;
    var getLogUrl = "/scriptcenter/script/runTimeLog.ajax";
    var internal = undefined;
    var currentLogNum = 1;
    initAce();
    getLog();

    function initAce() {
        //初始化对象
        editor = ace.edit("logCode");
        //设置风格和语言（更多风格和语言，请到github上相应目录查看）
        // var theme = "tomorrow_night"
        // editor.setTheme("ace/theme/" + theme);
        editor.session.setMode("ace/mode/text");
        //字体大小
        editor.setFontSize(12);
        //设置只读（true时只读，用于展示代码）
        editor.setReadOnly(true);

        //自动换行,设置为off关闭
        editor.setOption("wrap", "free");

        //启用提示菜单
        // ace.require("ace/ext/language_tools");
        editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: true,
            enableLiveAutocompletion: true
        });
        editor.setValue("");
        editor.focus()

    }

    function getLog() {
        $.ajax({
            url: getLogUrl,
            data:  {
                runDetailId: $("#runDetailId").val(),
                currentLogIndex: currentLogNum
            },
            type: "post",
            dataType: 'json',
            beforeSend: function () {
            },
            success: function (data) {
                if(data && data.obj && data.code==0){
                    var logs=data.obj.logs;
                    if(logs.length==0){
                    }else {
                        var string=logs.join("\n")+"\n";
                        currentLogNum+=logs.length;
                        editor.session.insert({row:currentLogNum,column:0},string);
                    }
                    if(!data.obj.isLastLog){
                        setTimeout(getLog,1000);
                    }
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                window.clearInterval(internal);
                if (parent.$) {
                    //parent.$.errorMsg("出现异常,请联系管理员!");
                }else{
                    //$.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    }
})

var differ;

function initDiff(){

    var theme = HOME_COOKIE.getColorCookie() === "white" ? "chrome" : "tomorrow_night" ;
    differ = new AceDiff({
        mode: "ace/mode/sql",
        theme: "ace/theme/" + theme,
        showConnectors: true,
        showDiffs: true,
        left: {
            copyLinkEnabled: false,
            editable:false
        },
        right: {
            copyLinkEnabled: false,
            editable:false
        }


    });
}


function initAce() {
    //初始化对象
    editor = ace.edit("code");

    if(HOME_COOKIE.getColorCookie() === "white"){
        editor.setTheme("ace/theme/chrome");
    }else{
        editor.setTheme("ace/theme/tomorrow_night");
    }
    editor.session.setMode("ace/mode/sql");
    //字体大小
    editor.setFontSize(12);
    //设置只读（true时只读，用于展示代码）
    editor.setReadOnly(true);

    //自动换行,设置为off关闭
    editor.setOption("wrap", "free");

    //启用提示菜单
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: false,
        enableLiveAutocompletion: true
    });
}

jQuery(function () {

    var pageInit = {
        initPage : function () {

            if($("#canEdit").val() === "true"){
                if($("#showSideCount").val() * 1 == 1){
                    initAce();
                }else{
                    initDiff();
                }
            }

            $(".downloadA").click(function () {
                var node = $(this);
                var form_id = "script_form_downLoad";
                if ($("#" + form_id).length > 0) {
                    $("#" + form_id).remove();
                }
                var commitId = node.attr("data-commitid");
                var dataId = node.attr("data-id");
                var temp = document.createElement("form");
                temp.action = "/scriptcenter/script/downGitScript.ajax" + "?gitHisDetailId=" + dataId + "&commitId="+ commitId;
                temp.method = "post";
                temp.style.display = "none";
                temp.id = form_id;
                document.body.appendChild(temp);
                temp.submit();
                $("#" + form_id).remove();
            })
        },
        getScriptFileContent:function () {

        }
    }


    pageInit.initPage();

})

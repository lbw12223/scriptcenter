$(function () {
    function copyContent(value) {
        var input = document.createElement('textarea');
        input.textContent = value;
        document.body.appendChild(input);
        input.focus();
        input.setSelectionRange(0, input.value.length);
        if (document.execCommand('copy')) {
            document.execCommand('copy');
        }
        document.body.removeChild(input);
    }

    function getSql(type) {
        var rows = $("#table-query-grid-table").jqGrid('getRowData');
        var tableName = $("#queryTableSelect").val();
        var dbName = $("#queryDbSelect").val();
        var sql="";
        if (tableName && rows && rows.length > 0) {
            switch (type) {
                case 1:
                    //select语句
                    sql+="SELECT";
                    for(var index=0;index<rows.length;index++){
                        sql+="\n            "+rows[index].columnName+",";
                    }
                    sql=sql.substring(0,sql.length-1);
                    sql+=" \nFROM "+dbName+"."+tableName;
                    break;
                case 2:
                    //create语句
                    sql+="CREATE TABLE IF NOT EXISTS "+dbName+"."+tableName+"\n(";
                    for(var index=0;index<rows.length;index++){
                        sql+="\n    "+rows[index].columnName+"    "+rows[index].columnType.toUpperCase()+"    COMMENT \'"+rows[index].comment+"\',";
                    }
                    sql=sql.substring(0,sql.length-1);
                    sql+="\n);"
                    break;
                default:
                    break;
            }
        }
        return sql;
    }
    $("#createSqlModal").on("show.bs.modal",function () {
        var selectSql=getSql(1);
        $("#selectSqlSentence").val(selectSql);
        var createSql=getSql(2);
        $("#createSqlSentence").val(createSql);
    })
    $("#copySqlBtn").click(function () {
        var activeLi = $("#createSqlUl li.active");
        var target = $(activeLi).attr("target")
        var textarea = document.getElementById(target);
        var text = textarea.value;
        copyContent(text);
        $.successMsg("已复制至剪贴板！");
    })
    $("#createSqlUl").on("click", "li", function () {
        $("li", $("#createSqlUl")).removeClass("active");
        $(this).addClass("active");
        var target = $(this).attr("target");
        $("#" + target).siblings("textarea").hide();
        $("#" + target).show();
    })

})
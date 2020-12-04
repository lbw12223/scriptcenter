Array.prototype.pushUndefinedStr = function (arg) {
    if (!arg) {
        return;
    }
    if ($.trim(arg).length < 1) {
        return;
    }
    this.push($.trim(arg));
}
/**
 * 上线到调度任务错误原因
 *
 * @type {{SUCCESS: number, SET_ERROR: number, DB_TBALE_ERRORR: number, ONLY_SELECT_ERROR: number}}
 */
var SQL_VALIDATE_TYPE = {
    SUCCESS: 0,
    SET_ERROR: 1,
    DB_TBALE_ERRORR: 2,
    ONLY_SELECT_ERROR: 3
}


function DataDevSql(sql) {
    this.sql = sql;
    this.readIndex = -1;
    this.readChar = '';
    this.keywords = [];

    /**
     * 过滤 '' "" 中的数据
     * 过滤 注释 -- 后面的数据
     */
    this.fileterStringAndComment = function () {
        var replace_single = /'.*'/g;   //单引号
        var replace_double = /".*"/g;   //双引号
        var replace_comment = /--.*/g;  // 注释
        var replace_line = /\n/g;
        this.sql = this.sql.toLowerCase();
        this.sql = this.sql.replace(replace_single, "").replace(replace_double, "").replace(replace_comment, " ").replace(replace_line, " ");

    }
    this.getNextChar = function () {
        if (this.readIndex < this.sql.length) {
            this.readIndex++;
            this.readChar = this.sql.charAt(this.readIndex);
            return true;
        }
    }
    /**
     * 当前字符是否是 SQL的分割语句
     */
    this.isSqlKeyWordSplite = function (char) {
        return char == ' '
            || char == '('
            || char == ')'
            || char == ','
            || char == '<'
            || char == '>'
            || char == '='
            || char == '!'
            || char == '*'
            || char == '-'
            || char == '+'
            || char == '/'
            || char == '%';
    }
    /**
     * 是否是运算符 <> , != , = , + , / ,*
     * @param char
     */
    this.isSqlKeyOperator = function (char) {

    }
    /**
     * a-z | A-Z | _ \ 0-9
     * @param char
     */
    this.isSqlChar = function (char) {
        return /^[\w|\.]+$/g.test(char);
    }
    /**
     * 过滤出关键字
     */
    this.scan = function () {
        this.fileterStringAndComment();
        console.log(this.sql)
        var chars = [];
        while (this.getNextChar()) {
            if (this.isSqlChar(this.readChar)) {
                chars.push(this.readChar);
                continue;
            }
            var isSplit = this.isSqlKeyWordSplite(this.readChar);
            if (isSplit) {
                var keyword = this.convertChars2Word(chars);
                this.keywords.pushUndefinedStr(keyword)
                this.keywords.pushUndefinedStr(this.readChar);
                chars = [];
            }
        }
        //剩余的字符转化成keyword
        var keyword = this.convertChars2Word(chars);
        this.keywords.pushUndefinedStr(keyword);
        console.log(this.keywords);
    }
    this.convertChars2Word = function (chars) {
        var str = "";
        for (var index = 0; index < chars.length; index++) {
            str += chars[index];
        }
        str = $.trim(str);
        if ($.trim(str).length < 1) {
            return undefined;
        }
        return str;
    }
    /**
     *
     * 1.检查当前SQL是否能够上线
     * 2.set sss.sss = sdfs ; 只能放在文件的开头。
     * 3.纯SQL语句不能上线(select) ，只能上线包含 INSERT, DROP, TRUNCATE, LOAD, CREATE, ALTER;
     * 4.每条语句中的表名，必须包含有app.table
     *
     */
    this.checkCanUpLine = function () {
        this.scan();
        var valiateResult = {
            isValidate: true,
            isValidateType: SQL_VALIDATE_TYPE.SUCCESS
        }
        var lastSetIndex = 0;  //最后一个Set的index
        var isOnlySelect = false;


        var mutiSql = this.sql.split(";");
        for (var index = 0; index < mutiSql.length; index++) {
            var tempIsOnlySelect = true;
            if (mutiSql[index].trim().length < 1) {
                continue;
            }
            var tempSqlKeyWord = mutiSql[index].split(" ");
            if (tempSqlKeyWord.length <= 1) {
                continue;
            }
            for (var tempIndex = 0; tempIndex < tempSqlKeyWord.length; tempIndex++) {
                var keyword = tempSqlKeyWord[tempIndex];
                if (keyword === "insert"
                    || keyword === "drop"
                    || keyword === "truncate"
                    || keyword === "load"
                    || keyword === "create"
                    || keyword === "alter"
                    || keyword === "delete"
                ) {
                    tempIsOnlySelect = false;
                }
            }

            isOnlySelect = tempIsOnlySelect || isOnlySelect;

        }
        for (var index = 0; index < this.keywords.length; index++) {
            var keyword = this.keywords[index];

            //set 中间是否位于顶部
            if (keyword === "set") {
                lastSetIndex = index;
                var validateSet = this.isValidateSetHasSqlKeyWord(this.keywords.slice(0, index));
                if (!validateSet) {
                    valiateResult.isValidate = false;
                    valiateResult.isValidateType = SQL_VALIDATE_TYPE.SET_ERROR;
                    return valiateResult;
                }
            }


            /*  //判断是否包含非select SQL的关键词
              if (keyword === "insert"
                  || keyword === "drop"
                  || keyword === "truncate"
                  || keyword === "load"
                  || keyword === "create"
                  || keyword === "alter"
                  || keyword === "delete"
              ) {
                  isOnlySelect = false;
              }*/
            /**
             *  1。判断表名必须是db.tableName，出现表名的地方均是这样。
             *  2。from 后面的
             *  3。exists 后面
             *  4. join 后面
             *
             */
            if (keyword === "from" || keyword === "join") {
                var nextKeyWord = (index + 1) < this.keywords.length ? this.keywords[index + 1] : undefined;
                if (nextKeyWord) {
                    if (nextKeyWord === "(") {
                        continue;
                    }
                    // from app.tableName as A, app.TestName as T
                    if (this.isSqlChar(nextKeyWord)) {
                        //db.tableName

                        var tableNames = this.findFromJoinTableNames(index + 1);
                        for (var tableIndex = 0; tableIndex < tableNames.length; tableIndex++) {
                            if (tableNames[tableIndex].indexOf(".") <= 0) {
                                valiateResult.isValidate = false;
                                valiateResult.isValidateType = SQL_VALIDATE_TYPE.DB_TBALE_ERRORR;
                                return valiateResult;
                            }
                        }
                    }
                }
            }
            if (this.keyWordIsTableName(index)) {
                if (this.isSqlChar(keyword)) {
                    //db.tableName
                    if (keyword.indexOf(".") == -1) {
                        valiateResult.isValidate = false;
                        valiateResult.isValidateType = SQL_VALIDATE_TYPE.DB_TBALE_ERRORR;
                        return valiateResult;
                    }
                }
            }
        }
        if (isOnlySelect) {
            valiateResult.isValidate = false;
            valiateResult.isValidateType = SQL_VALIDATE_TYPE.ONLY_SELECT_ERROR;
        }
        return valiateResult;
    }


    this.findFromJoinTableNames = function (index) {
        var start = index;
        var tableNames = [];
        var FROM_TABLE_END_FLAG = "(where)(group)(order)(join)(on)(union)(having)(limit)(select)())(()";
        var endIndex = this.keywords.length;
        for (; index < this.keywords.length; index++) {
            if (FROM_TABLE_END_FLAG.indexOf(this.keywords[index]) != -1) {
                endIndex = index;
                break;
            }
        }
        var afterFromJoinArrays = this.keywords.slice(start, endIndex);
        //from app.tableName as A, app.TestName as T
        var afterFromJoinString = afterFromJoinArrays != null && afterFromJoinArrays.length > 0 ? afterFromJoinArrays.join(" ") : "";
        if (afterFromJoinString.length > 0) {
            var arrays = afterFromJoinString.split(",");
            for (var index = 0; index < arrays.length; index++) {
                tableNames.push(arrays[index].trim().split(" ")[0]);
            }
        }

        return tableNames;

    }

    //判断当前的keyword 是否是tableName
    /**
     * insert into tableName
     * insert into table tableName
     * insert overwrite tableName
     * create table tableName
     * create external table tableName
     *
     * drop table tableName
     * drop table if exists tableName
     * truncate table tableName
     * alter table tableName
     *
     * 前面已经判断了 from join 这里值判断上面的情况，确定当前是否是表名
     * @param index
     */
    this.keyWordIsTableName = function (index) {
        var start = index - 2;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "insert into") {
                if (this.keywords[index] && this.keywords[index] === "table") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "insert overwrite") {
                if (this.keywords[index] && this.keywords[index] === "table") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "rename to") {
                return true;
            }
            if (beforeStringJoins === "create table") {
                if (this.keywords[index] && this.keywords[index] === "if") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "drop table") {
                if (this.keywords[index] && this.keywords[index] === "if") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "truncate table") {
                return true;
            }
            if (beforeStringJoins === "alter table") {
                return true;
            }
            if (beforeStringJoins === "delete table") {
                return true;
            }
            if (beforeStringJoins === "desc formatted") {
                return true;
            }

        }
        start = index - 3;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "create external table") {
                if (this.keywords[index] && this.keywords[index] === "if") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "create temporary table") {
                return true;
            }
            if (beforeStringJoins === "insert into table") {
                return true;
            }
            if (beforeStringJoins === "insert overwrite table") {
                return true;
            }
        }
        start = index - 4;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "drop table if exists") {
                return true;
            }
            if (beforeStringJoins.startWith("create table") && beforeStringJoins.endWith("like")) {
                return true;
            }
        }
        start = index - 5;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "create table if not exists") {
                return true;
            }
        }
        start = index - 6;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "create external table if not exists") {
                return true;
            }
            if (beforeStringJoins.startWith("create table") && beforeStringJoins.endWith("like")) {
                return true;
            }

        }
        return false;
    }
    this.findKeyWordInArea = function (start, end) {
        var arrays = []
        for (; start < end; start++) {
            arrays.push(this.keywords[start]);
        }
        if (arrays.length > 0) {
            return arrays.join(" ");
        }
        return ""
    }

    /**
     * set 之前是否有SQL的关键词
     * @param keywords
     */
    this.isSqlKeyWord = function (word) {
        return word === "select"
            || word === "from"
            || word === "where"
            || word === "group"
            || word === "by"
            || word === "insert"
            || word === "into"
            || word === "value"
            || word === "having"
            || word === "order"
            || word === "table"
            || word === "create"
            || word === "drop"
            || word === "update"
            || word === "if"
            || word === "delete"

    }
    /**
     * 检查Set 之前的keyword里面有没有sql 的关键词
     * 1。如果有关键词，那么set语句就不是放到顶部
     * @param keywords
     */
    this.isValidateSetHasSqlKeyWord = function (keywords) {
        for (var index = 0; index < keywords.length; index++) {
            var isSqlKeyWord = this.isSqlKeyWord(keywords[index]);
            if (isSqlKeyWord) {
                return false;
            }
        }
        return true;
    }
}

var dataDevSql = new DataDevSql("\n" +
    "set hive.exec.parallel=true;\n" +
    "set hive.exec.parallel.thread.number=8;\n" +
    "create table dev.dev_lms_tdc_whitelist stored as orc as\n" +
    "select\n" +
    "    c.delv_center_num\n" +
    "    ,b.item_sku_id\n" +
    "    ,count(distinct b.parent_sale_ord_id) as order_cnt\n" +
    "from\n" +
    "    (\n" +
    "    select\n" +
    "        *\n" +
    "    from\n" +
    "        gdm_m04_ord_det_sum   --订单明细宽表\n" +
    "    where\n" +
    "        dt >= '2018-10-01'\n" +
    "        and sale_ord_dt between '2018-10-01' and '2018-12-05'\n" +
    "        and item_first_cate_name not in ('测试分类')\n" +
    "        and phy_pop_flag = 0                   --实物POP订单\n" +
    "        and sale_ord_valid_flag = 1            --有效标志\n" +
    "        and sale_ord_type_cd in ('0', '29', '6', '17')             --- 一般订单\n" +
    "        and split_status_cd in ('2', '3')\n" +
    "        and getDataTypeBySkuId(cast(item_sku_id as bigint)) in (10) \n" +
    "    ) b\n" +
    "join \n" +
    "    (\n" +
    "    select * from dim.dim_ipc_ioa_store_da where dt between '2018-10-01' and '2018-12-05'\n" +
    "    ) a\n" +
    "on\n" +
    "    a.delv_center_num = b.delv_center_num\n" +
    "    and a.store_id = b.store_id\n" +
    "    and a.dt = b.sale_ord_dt\n" +
    "join\n" +
    "    (\n" +
    "    select * from app.app_ioa_dim_county_delv_center_mapping where dt between '2018-10-01' and '2018-12-05' and delv_center_num in (775, 826)\n" +
    "    ) c\n" +
    "on\n" +
    "    b.rev_addr_county_id = c.county_id \n" +
    "    and b.sale_ord_dt = c.dt\n" +
    "group by \n" +
    "    c.delv_center_num\n" +
    "    ,b.item_sku_id;\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "\n" +
    "select min(sale_ord_dt),max(sale_ord_dt) from app.app_ipc_ioa_tdc_replenish_black_sku_info");


//console.log(dataDevSql.checkCanUpLine()) ;
//dataDevSql.scan();
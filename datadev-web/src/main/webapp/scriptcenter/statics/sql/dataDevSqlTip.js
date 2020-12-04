Array.prototype.pushUndefinedStr = function (arg) {
    if (!arg) {
        return;
    }
    if ($.trim(arg).length < 1) {
        return;
    }
    this.push($.trim(arg));
}
Array.prototype.pushArray = function (arg) {
    if (!arg) {
        return;
    }
    for(var index = 0 ; index < arg.length ; index++ ){
        this.push(arg[index]);
    }
}
/**
 * 不存在的才添加
 * @param arg
 */
Array.prototype.pushUnExsits = function (arg) {
    if (!arg) {
        return false;
    }
    if ($.trim(arg).length < 1) {
        return;
    }
    arg = $.trim(arg);
    for (var index = 0; index < this.length; index++) {
        if (this[index] === arg) {
            return false;
        }
    }
    this.push($.trim(arg));
    return true;
}


SQL_TIP_TYPE = {
    NO_TIP: 0, /*字符串 或者 注释内容*/
    COLUMN: 1,
    TABLE: 2,
    KEYWORD: 3
}

KEY_WORD_TYPE = {
    NO_START: "0",
    COLUMN_START: "1",
    TABLE_START: "2",
    ZHUSHI_START: "3",
    STRING_START: "4",
    DEFAULT_DB_START: "5"
}
//from table 后面结束的标志
FROM_TABLE_END_FLAG = "(where)(group)(order)(left)(right)(inner)(join)(on)(union)(having)(limit)(select)())(()"
SQL_KEY_WORD = "select,where,and,or,group,order,left,right,inner,join,on,union,having,limit,sum,avg,min,as";
/**
 * [
 *   {
 *      table:tableName,
 *      column:[name,age,user],
 *      alias:xxx,
 *      db:gdm,
 *      marketId:,
 *      marketName:,
 *      matchKey:逗号分割,
 *   }
 * ]
 * @type {Array}
 */
CACHE_tables = [];
CACHE_dbs = new Map();
var queryTableUrl = "/scriptcenter/config/getSqlTipSearchTableName.ajax"
var queryTableColumnUrl = "/scriptcenter/config/getSqlTipSearchTableColumns.ajax"


/**
 * 1。传入参数 当前SQL，当前光标所在位置
 * 2。返回可能的字段名称，表名
 *
 * @param sql
 * @param cursorPosition
 * @constructor
 */
function DataDevSqlTip() {
    this.fixSql = "";
    this.originSql = "";
    this.readIndex = -1;
    this.readChar = '';
    this.searchValue = "";
    this.CACHE_NEED_QUERY_Tables = [];
    /**
     * {keyword:"",type: columnstart | tablestart | , startIndex :  endIndex }
     * @type {Array}
     */
    this.keywords = [];
    this.brackets = []; //括号
    //当前光标的位置
    this.cursorPosition = -1;
    this.tableQueryLength = 4;
    this.callBack = undefined;
    this.editor = undefined;
    this.marketId = 0;
    /*   过滤表的时候至少输入多少个字符才开始查询 */
    /**
     * '' 引号里面的区域
     * -- 注释后面的区域直到\n（换行符）
     * @type {Array}
     */
    this.noTipArea = [];
    this.tableRangeStart = 0;
    /**
     * tipType: SQL_TIP_TYPE.NO_TIP,
     * tipList: [
     *    {meta: "abcdefg", caption: "sonic", value: "sonic", score:1}
     * ]
     * @type {{}}
     */
    this.tipResult = {
        tipType: SQL_TIP_TYPE.NO_TIP,
        tipList: []
    };


    /**
     * 过滤 '' "" 中的数据
     * 过滤 注释 -- 后面的数据
     */
    this.fileterStringAndComment = function () {
        var replace_single = /'.*'/g;   //单引号
        var replace_double = /".*"/g;   //双引号
        var replace_comment = /--.*/g;  // 注释
        var replace_line = /\n/g;
        this.fixSql = this.fixSql.toLowerCase();
        this.fixSql = this.fixSql.replace(replace_single, "").replace(replace_double, "").replace(replace_comment, " ").replace(replace_line, " ");
    }
    this.setSqlAndCusorPosition = function (sql, cursorPosition) {
        this.originSql = sql;
        this.fixSql = sql;
        this.cursorPosition = cursorPosition;
        this.keywords = [];
        this.readIndex = -1;
        this.noTipArea = [];
    }
    this.setNotifyCall = function (call) {
        this.callBack = call;
    }
    this.setEditor = function (editor) {
        this.editor = editor;
    }
    this.setMarketId = function (marketId) {
        this.marketId = marketId;
    }
    this.hasNextChar = function () {
        if (this.readIndex < this.originSql.length) {
            this.readIndex++;
            this.readChar = this.originSql.charAt(this.readIndex);
            return true;
        }
        return false;
    }
    /**
     * 获取 index 处的char
     * @param index
     * @returns {*}
     */
    this.getChar = function (index) {
        if (index < this.originSql.length) {
            return this.originSql.charAt(index);
        }
        return undefined;
    }

    /**
     * a-z | A-Z | _ | 0-9 | .
     * sql允许的一个单词
     * @param char
     */
    this.isOneWord = function (char) {
        return /^[\w|\.]+$/g.test(char);
    }
    /**
     * 发现下一个 char
     * @param index
     * @param char
     * @returns {*}
     */
    this.findNextCharIndex = function (index, char) {
        for (; index < this.originSql.length; index++) {
            if (this.originSql.charAt(index) == char) {
                return index;
            }
        }
        return this.originSql.length;
    }

    /**
     *
     * 转换chars to 一个单词
     * @param chars
     * @returns {*}
     */
    this.convertChars2Word = function (chars) {
        var str = "";
        for (var index = 0; index < chars.length; index++) {
            str += chars[index];
        }
        str = $.trim(str);
        if ($.trim(str).length < 1) {
            return "";
        }
        return str;
    }
    this.convertKeyWord2String = function (arrays) {
        var string = "";
        for (var index = 0; index < arrays.length; index++) {
            string += " " + arrays[index];
        }
        return string.trim().replace(";", "");
    }

    this.convertKeyWordString = function (arrays) {
        var string = "";
        for (var index = 0; index < arrays.length; index++) {
            string += " " + arrays[index].keyword;
        }
        return string.trim().replace(";", "");
    }

    this.addKeyWord = function (word, startIndex, endIndex, keyWordType) {

        if (word && word.trim().length > 0) {
            word = word.trim().toLowerCase();

            var insertKeyWord = {
                keyword: word,
                type: keyWordType ? keyWordType : KEY_WORD_TYPE.NO_START,
                startIndex: startIndex + 1,
                endIndex: endIndex ? endIndex : -1,
                keywordIndex: this.keywords.length
            }
            switch (word) {
                case "select" :
                    insertKeyWord.type = KEY_WORD_TYPE.COLUMN_START;
                    break;
                case "from" :
                    insertKeyWord.type = KEY_WORD_TYPE.TABLE_START;
                    break;
                case "table" :
                    insertKeyWord.type = KEY_WORD_TYPE.TABLE_START;
                    break;
                case "into" :
                    insertKeyWord.type = KEY_WORD_TYPE.TABLE_START;
                    break;
                case "where" :
                    insertKeyWord.type = KEY_WORD_TYPE.COLUMN_START;
                    break;
                case "on" :
                    /*join on ... */
                    insertKeyWord.type = KEY_WORD_TYPE.COLUMN_START;
                    break;
                case "by" :
                    /*order by | group by提示字段*/
                    insertKeyWord.type = KEY_WORD_TYPE.COLUMN_START;
                    break;
                case "having" :
                    insertKeyWord.type = KEY_WORD_TYPE.COLUMN_START;
                    break;
                case "join" :
                    insertKeyWord.type = KEY_WORD_TYPE.TABLE_START;
                    break;
                case "use" :
                    insertKeyWord.type = KEY_WORD_TYPE.DEFAULT_DB_START;
                    break;
                case "as" :
                    insertKeyWord.type = KEY_WORD_TYPE.STRING_START;
                    break;
                default :
                    break;
            }
            this.keywords.push(insertKeyWord);
        }
    }
    /**
     * 1。找出Keyword。
     * 2。找出提示 字段 ， 表范围
     *
     */
    this.findKeyWord = function () {
        //解析出来 单个词语
        var chars = [];
        while (this.hasNextChar()) {
            if (this.isOneWord(this.readChar)) {
                chars.push(this.readChar);
            } else {
                var word = this.convertChars2Word(chars);
                this.addKeyWord(word, this.readIndex - 1);

                var isAdded = false;
                // 处理注释 -- 后面才是注释
                if (this.readChar == "-") {
                    if (this.getChar(this.readIndex + 1) === "-") {
                        var endZhushiIndex = this.findNextCharIndex(this.readIndex + 1, "\n");
                        this.noTipArea.push({
                            startIndex: this.readIndex,
                            endIndex: endZhushiIndex
                        })
                        isAdded = true;
                        var zhushiString = this.originSql.substr(this.readIndex, endZhushiIndex - this.readIndex + 1);
                        this.addKeyWord(zhushiString, this.readIndex + 1, endZhushiIndex, KEY_WORD_TYPE.ZHUSHI_START);
                        this.readIndex = endZhushiIndex;
                        continue;
                    }
                }
                if (this.readChar == "'") {
                    var endStringIndex = this.findNextCharIndex(this.readIndex + 1, "'");
                    this.noTipArea.push({
                        startIndex: this.readIndex,
                        endIndex: endStringIndex
                    })
                    var stringIndex = this.originSql.substr(this.readIndex, endStringIndex - this.readIndex + 1);
                    this.addKeyWord(stringIndex, this.readIndex, endStringIndex, KEY_WORD_TYPE.STRING_START);
                    this.readIndex = endStringIndex;
                    continue

                }
                this.addKeyWord(this.convertChars2Word(this.readChar), this.readIndex);

                chars = [];
            }
        }
    }
    /**
     * {keyword:"",type: columnstart | tablestart | , startIndex :  endIndex }
     * 2.计算出来 字段区域可以用到到表集合（包含别名）
     * 3.计算出来的表先存入needQuery ,然后在和 CAHCE_table 做对比
     * 4.
     */
    this.caculateTable = function () {

        this.CACHE_NEED_QUERY_Tables = [];
        this.tableRangeStart = 0;
        var defaultDb = "";
        for (var index = 0; index < this.keywords.length; index++) {
            var keyword = this.keywords[index];
            //use gdm...
            if (keyword.keyword === "use") {
                defaultDb = this.keywords[index + 1].keyword;
            }
            //sql结束的标志  rangeEnd , rangeStart
            if (keyword.keyword === ";") {
                this.setTableRange(keyword);
                this.tableRangeStart = keyword.startIndex;
            }
            if (keyword.type == KEY_WORD_TYPE.TABLE_START) {
                var nextKeyWord = (index + 1 < this.keywords.length) ? this.keywords[index + 1] : undefined;
                if (nextKeyWord) {
                    if (nextKeyWord.keyword != "(") {
                        index = this.findTableName(index + 1, defaultDb);
                    }
                }
            }

        }
        this.mergeCacheTable();
        //查询库表中的字段
        for (var index = 0; index < CACHE_tables.length; index++) {
            var queryTable = CACHE_tables[index];
            if (queryTable) {

                if (queryTable.status == "needquery"
                    && queryTable.marketId * 1 > 0
                    && queryTable.table.length > 0
                    && queryTable.db.length > 0) {
                    this.queryTableColumns(index, queryTable);
                }
            }
        }


        this.caculateTipArray();
        this.caculateSubSql();
    }
    /**
     * 计算出子查询(select ) as t , 或者 (select ) t
     */
    this.caculateSubSql = function () {

        this.brackets = [];
        var subSqls = [];
        for (var index = 0; index < this.keywords.length; index++) {
            var keyWord = this.keywords[index];

            if (keyWord.keyword == "(") {
                this.brackets.push({
                    startIndex: index,
                    endIndex: -1
                })
            }
            if (keyWord.keyword == ")") {
                var bracket = this.lastNoRightBracket();
                if (bracket) {
                    bracket.endIndex = index;
                }
            }
        }
        //得到是子查询的括号只匹配 (select from) as T , (select from) t
        for (var index = 0; index < this.brackets.length; index++) {
            var string = "";
            var tempBracket = this.brackets[index];
            var barcketStart = tempBracket.startIndex;
            var barcketEnd = tempBracket.endIndex;
            if (barcketEnd == -1) {
                continue;
            }
            //barcketStart -> (select
            if (this.keywords[barcketStart + 1].keyword != "select") {
                continue;
            }
            //barcketEnd (select ) as t  , (select) t
            var isSubSql = false;
            var endNextKeyword = barcketEnd + 1 < this.keywords.length ? this.keywords[barcketEnd + 1] : undefined;
            if (endNextKeyword) {
                if (endNextKeyword.keyword === "as") {
                    if (!this.isSqlKeyWord(barcketEnd + 2)) {
                        isSubSql = true;
                        tempBracket.endIndex = tempBracket.endIndex + 2;
                        //continue ;
                    }
                } else {
                    if (!this.isSqlKeyWord(barcketEnd + 1)) {
                        isSubSql = true;
                        tempBracket.endIndex = tempBracket.endIndex + 1;
                        //continue ;
                    }
                }
            }

            if (!isSubSql) {
                continue;
            }
            // 子查询
            subSqls.push(tempBracket);


            this.covertSubSqlToTable(subSqls);
        }

    }
    //将子查询转换为table
    /**
     * 需要解析出表，字段
     * @param subSqls
     */
    //select * from tables,select age,table.*  from
    this.covertSubSqlToTable = function (subSqls) {

        for (var index = 0; index < subSqls.length; index++) {
            var subSql = subSqls[index];
            this.getSubSqlColumns(subSql);
        }


    }
    this.getSubSqlColumns = function (subSql) {
        var subSqlStart = subSql.startIndex;
        var subSqlEnd = subSql.endIndex;
        var startColumn = false;
        var endColumn = false;
        var columns = [];
        var tables = [];
        var inCacheTable = [];      //cache table 里面的数据
        var alias = this.keywords[subSqlEnd].keyword;
        //找出Columns,Table
        for (; subSqlStart < subSqlEnd; subSqlStart++) {
            var keyword = this.keywords[subSqlStart];
            if (keyword.keyword === "select") {
                startColumn = true;
            }
            if (keyword.type == KEY_WORD_TYPE.TABLE_START) {
                endColumn = true;
            }
            if (startColumn && !endColumn) {
                columns.push(keyword);
            }
            if (keyword.type == KEY_WORD_TYPE.TABLE_START) {
                tables.push(this.keywords[subSqlStart + 1])
            }
        }

        //根据表名，得到所有的column
        var totalColumns = [];

        for (var tableIndex = 0; tableIndex < tables.length; tableIndex++) {
            var keyword = tables[tableIndex];
            var dbTableArrays = keyword.keyword.split(".");
            var db = dbTableArrays.length > 1 ? dbTableArrays[0] : "";
            var tableName = dbTableArrays.length > 1 ? dbTableArrays[1] : dbTableArrays[0];
            for (var index = 0; index < CACHE_tables.length; index++) {
                var cacheTable = CACHE_tables[index];
                if (tableName === cacheTable.table) {
                    if (db.trim().length > 0) {
                        if (db.trim() === cacheTable.db) {
                            inCacheTable.push(cacheTable);
                            if (cacheTable.sub) {
                                totalColumns.pushArray(cacheTable.sub)
                            }
                        }
                    } else {
                        inCacheTable.push(cacheTable);
                        if (cacheTable.sub) {
                            totalColumns.pushArray(cacheTable.sub)
                        }
                    }
                }
            }
        }


        //得到自查询表 字段信息 select age , sum(oo) as t , leert(34,343), t.*,* from
        /**
         * 1.先按照逗号分割, 在按照 as 分割
         * 2.columnName: "list0", columnType: "string0"
         */
        var columnStr = this.convertKeyWordString(columns.slice(1));
        columnStr = this.replaceBrackets(columnStr);
        var tempColumnArrays = columnStr.split(",");
        var subs = [];
        for (var index = 0; index < tempColumnArrays.length; index++) {
            var tempColumn = tempColumnArrays[index].trim();
            if (tempColumn.length < 1) {
                continue;
            }
            var hasContainAs = tempColumn.indexOf("as") != -1;
            var hasContainDoc = tempColumn.indexOf(".") != -1;
            var hasContainSpace = tempColumn.indexOf(" ") != -1;
            var columnName = "";
            var columnType = "";
            if (tempColumn === "*") {
                if (totalColumns.length > 0) {
                    subs.pushArray(totalColumns);
                }
                continue;
            }
            //包含有 as
            if (hasContainAs) {
                columnName = tempColumn.split("as")[1].trim();
                if (columnName.length < 1) {
                    continue;
                }
                columnType = this.getColumnType(tempColumn.split("as")[0], totalColumns);
                subs.push({
                    columnName: columnName,
                    columnType: columnType
                })
                continue;
            }
            if(hasContainSpace){
                columnName = tempColumn.split(/\s+/)[1].trim();
                if (columnName.length < 1) {
                    continue;
                }
                columnType = this.getColumnType(tempColumn.split(/\s+/)[0], totalColumns);
                subs.push({
                    columnName: columnName,
                    columnType: columnType
                })
                continue;
            }
            //包含有 .
            if (hasContainDoc) {
                //t.* , t.userAge
                if (tempColumn.split(".")[1] === "*") {
                    //add all tableColumn
                    var tempTableName = tempColumn.split(".")[0];
                    for (var tableIndex = 0; tableIndex < inCacheTable.length; tableIndex++) {
                        if (tempTableName === inCacheTable[tableIndex].table
                            || tempTableName === inCacheTable[tableIndex].alias) {
                            subs.pushArray(inCacheTable[tableIndex].sub);
                            break;
                        }
                    }
                } else {
                    //单个字段
                    columnName = tempColumn.split(".")[1].trim();
                    if (columnName.length < 1) {
                        continue;
                    }
                    columnType = this.getColumnType(columnName, totalColumns);
                    subs.push({
                        columnName: columnName,
                        columnType: columnType
                    })
                }
                continue;

            }
            //普通的字段
            subs.push({
                columnName: tempColumn,
                columnType: this.getColumnType(tempColumn, totalColumns)
            })


        }

        //构造出subSql Table
        var cacheTableTemp = {
            alias: alias,
            db: inCacheTable.length > 0 ? inCacheTable[0].db : "",
            marketId: inCacheTable.length > 0 ? inCacheTable[0].marketId : "",
            marketName: inCacheTable.length > 0 ? inCacheTable[0].marketName : "",
            matchKey: "",
            rangeEnd: this.maxRangeEnd(inCacheTable),
            rangeStart: this.minRangeStart(inCacheTable),
            status: "hascolumns",
            sub: subs,
            table: ""
        }
        CACHE_tables.push(cacheTableTemp);


    }
    this.getColumnType = function (columnName, totalColumns) {
        for (var index = 0; index < totalColumns.length; index++) {
            if (columnName.indexOf(totalColumns[index].columnName) != -1) {
                return totalColumns[index].columnType;
            }
        }
        return "";
    }
    this.replaceBrackets = function (str) {
        while(str.indexOf("(") > 0){
            str = str.replace(/\(.*?\)/,"");
        }
        return str ;
    }
    this.minRangeStart = function (inCacheTable) {
        var min = inCacheTable.length > 0 ? inCacheTable[0].rangeStart : 0;
        if (min == 0) {
            return min;
        }
        for (var index = 0; index < inCacheTable.length; index++) {
            if (inCacheTable[index].rangeStart < min) {
                min = inCacheTable[index].rangeStart;
            }
        }
        return min;
    }
    this.maxRangeEnd = function (inCacheTable) {
        var max = inCacheTable.length > 0 ? inCacheTable[0].rangeEnd : 0;
        for (var index = 0; index < inCacheTable.length; index++) {
            if (inCacheTable[index].rangeEnd > max) {
                max = inCacheTable[index].rangeEnd;
            }
        }
        return max;
    }
    this.isSqlKeyWord = function (kewwordIndex) {
        if (kewwordIndex >= this.keywords.length) {
            return true;
        }
        var keyword = this.keywords[kewwordIndex]
        return SQL_KEY_WORD.indexOf(keyword) > -1;
    }

    this.lastNoRightBracket = function () {
        for (var index = this.brackets.length - 1; index >= 0; index--) {
            if (this.brackets[index].endIndex == -1) {
                return this.brackets[index];
            }
        }
        return undefined;
    }
    /**
     * 设置 table range
     */
    this.setTableRange = function (keyword) {
        for (var index = 0; index < this.CACHE_NEED_QUERY_Tables.length; index++) {
            var tableTemp = this.CACHE_NEED_QUERY_Tables[index];
            if (!tableTemp.rangeEnd || tableTemp.rangeEnd == 0) {
                tableTemp.rangeEnd = keyword.startIndex;
                tableTemp.rangeStart = this.tableRangeStart;
            }
        }

    }
    /**
     * 1。合并Cache Table
     * 2。使用this.CACHE_NEED_QUERY_Tables 为准，然后删除Cache table 中，已经没有的表
     * 3。使用this.CACHE_NEED_QUERY_Tables 为准，添加Cache table 中
     */
    this.mergeCacheTable = function () {

        for (var index = 0; index < this.CACHE_NEED_QUERY_Tables.length; index++) {
            var table = this.CACHE_NEED_QUERY_Tables[index];
            var getCacheTable = this.getCacheTable(table);
            if (getCacheTable) {
                table.sub = getCacheTable.sub;
                table.status = getCacheTable.status;
                table.sub = getCacheTable.sub;
                table.marketName = getCacheTable.marketName;
                //table.rangeStart = getCacheTable.rangeStart ;
                //table.rangeEnd = getCacheTable.rangeEnd ;
            }
        }
        CACHE_tables = this.CACHE_NEED_QUERY_Tables;

    }
    this.getCacheTable = function (table) {
        for (var cacheIndex = 0; cacheIndex < CACHE_tables.length; cacheIndex++) {
            var tempTable = CACHE_tables[cacheIndex];
            if (table.db === tempTable.db
                && table.marketId === tempTable.marketId
                && table.table === tempTable.table
            ) {
                return tempTable;
            }
        }
        return undefined;
    }
    /**
     /**
     * [
     *   {
     *      table:tableName,
     *      column:[name,age,user],
     *      alias:xxx,
     *      db:gdm,
     *      marketId:,
     *      marketName:,
     *      matchKey:逗号分割,
     *   }
     * ]
     * @type {Array}
     */

    this.caculateTipArray = function () {
        CACHE_dbs.clear();
        for (var index = 0; index < CACHE_tables.length; index++) {
            var table = CACHE_tables[index];
            if (table) {
                if (table.table.length > 0 && table.db.length > 0) {
                    // matchKey
                    var matchKey = "";
                    matchKey += table.table + ",";
                    matchKey += table.db + "." + table.table + ",";
                    if (table.alias.length > 0) {
                        matchKey += table.alias;
                    }
                    table.matchKey = matchKey;
                    this.addCacheDb(table.db, table);
                }
            }
        }
    }
    this.addCacheDb = function (dbName, table) {
        var subTables = CACHE_dbs.get(dbName);
        if (!subTables) {
            subTables = [];
        }
        var canAdd = true;
        for (var tableIndex = 0; tableIndex < subTables.length; tableIndex++) {
            if (subTables[tableIndex].table === table.table) {
                canAdd = false;
                break;
            }
        }
        if (canAdd) {
            subTables.push(table);
        }
        CACHE_dbs.put(dbName, subTables);
    }

    this.queryTableColumns = function (tableIndex, queryTable) {
        queryTable.status = "nocolumns";

        var requestParams = {
            tableName: queryTable.table,
            marketId: queryTable.marketId,
            dbName: queryTable.db
        }
        $.ajax({
            type: 'POST',
            url: queryTableColumnUrl,
            data: requestParams,
            success: function (data) {
                if (data.code * 1 == 0 && data.obj) {
                    var columns = [];
                    //{columnName:,columnType}
                    for (var colIndex = 0; colIndex < data.obj.columns.length; colIndex++) {
                        var tempColumn = {
                            columnName: data.obj.columns[colIndex].columnName,
                            columnType: data.obj.columns[colIndex].columnType
                        }
                        columns.push(tempColumn);
                    }
                    queryTable.marketName = data.obj.market ? data.obj.market.marketName : "";
                    queryTable.sub = columns;
                    queryTable.status = "hascolumns";
                } else {
                    //CACHE_tables[tableIndex] = undefined;
                    queryTable.status = "nocolumns";
                }
            },
            dataType: "json"
        });


    }
    /**
     * 查找from 后面 出现 where | group by | order by | join | on | union | having | limit | )
     * @param index
     */
    this.findTableName = function (index, defaultDb) {
        var findIndex = index;
        var arrays = [];
        for (; index < this.keywords.length; index++) {
            var keyword = this.keywords[index];
            if (keyword.type === KEY_WORD_TYPE.STRING_START || keyword.type == KEY_WORD_TYPE.ZHUSHI_START) {
                continue;
            }
            var keywordString = keyword.keyword;
            if (FROM_TABLE_END_FLAG.indexOf("(" + keywordString + ")") != -1) {
                findIndex = index - 1;
                break;
            }
            arrays.push(keywordString);

        }
        var tableNames = this.convertKeyWord2String(arrays);

        this.parseTableName(tableNames, defaultDb, findIndex);

        return findIndex;
    }

    /**
     *
     * {
     *      table:tableName,
     *      column:[name,age,user],
     *      alias:xxx,
     *      db:gdm
     * }
     *
     * 1.gdm_m04_ord_det_sum v , user as t
     * 2.dim.dim_ipc_ioa_store_da t
     * 3.dim.dim_ipc_ioa_store_da
     */
    this.parseTableName = function (tableNames, defaultDb, findIndex) {

        tableNames = tableNames.trim();

        var tables = tableNames.split(",");
        for (var index = 0; index < tables.length; index++) {

            var table = {
                table: "",
                sub: [],
                alias: "",
                db: "",
                status: "needquery", /*needquery,querying,hascolumns,nocolumns*/
                marketId: this.marketId,
                marketName: "",
                matchKey: "",
                rangeStart: 0,
                rangeEnd: 0

            }
            //解析别名
            var tableName = tables[index].trim();
            if (tableName.indexOf(" as ") != -1) {
                table.table = tableName.split(" as ")[0];
                table.alias = tableName.split(" as ")[1];
            } else if (tableName.indexOf(" ") != -1) {
                table.table = tableName.split(" ")[0].trim();
                table.alias = tableName.split(" ")[1].trim();
            } else {
                table.table = tableName;
            }
            //解析库名称
            if (table.table.indexOf(".") != -1) {
                table.db = table.table.split(".")[0];
                table.table = table.table.split(".")[1];
            } else {
                table.db = defaultDb;
            }
            this.CACHE_NEED_QUERY_Tables.push(table);
        }
    }

    /**
     * 判断当前的currentIndex 是否在noTipArea
     * @returns {boolean}
     */
    this.cursorPositionInNoTipArea = function () {

        for (var index = 0; index < this.noTipArea.length; index++) {
            var noTipArea = this.noTipArea[index];
            if (noTipArea.startIndex < this.cursorPosition && this.cursorPosition < noTipArea.endIndex) {
                return true;
            }
        }

        return false;
    }
    /**
     * 向前找一个单词
     * @param index
     * @returns {*}
     */
    this.findBeforeOneWord = function (index) {
        var chars = [];
        while (index > 0) {
            var char = this.getChar(index);
            if (char == "." || this.isOneWord(char)) {
                index--;
                chars.unshift(char)
            } else {
                break;
            }
        }
        return this.convertChars2Word(chars);
    }
    /**
     * 找到当前currentIndex 所在的 keyword
     * @returns {undefined}
     */
    this.findCurrentIndexKeyWord = function () {
        var currentKeyWord = undefined;
        for (var index = 0; index < this.keywords.length; index++) {
            var tempKeyWord = this.keywords[index];
            var begin = tempKeyWord.startIndex - tempKeyWord.keyword.length;
            var end = tempKeyWord.startIndex;

            if (tempKeyWord) {
                if (begin <= this.cursorPosition && end >= this.cursorPosition) {
                    currentKeyWord = tempKeyWord;
                    break;
                }
            }
        }
        return currentKeyWord;
    }
    /**
     * 当前的光标是否在编辑表名
     * 当前光标的keyword的前一个是 table 提示符
     *
     * from fdm.test
     * join fdm.test
     *
     * from fdm.test as test , fdm.sfss
     *
     *
     */
    this.isEditTable = function () {
        //;
        if (this.cursorPosition > this.fixSql.length) {
            return false;
        }

        var currentKeyWord = this.findCurrentIndexKeyWord();
        if (!currentKeyWord) {
            return false;
        }
        var isInTableName = this.keyWordIsTableName(currentKeyWord.keywordIndex);
        return isInTableName;

    }
    //判断当前的keyword 是否是tableName
    /**
     * insert into tableName
     * insert overwrite tableName
     * create table tableName
     * create external table tableName
     *
     * drop table tableName
     * drop table if exists tableName
     * truncate table tableName
     * alter table tableName
     *
     *
     * from zhanglei as td  ,listsssa
     *
     * 前面已经判断了 from join 这里值判断上面的情况，确定当前是否是表名
     * @param index
     */
    this.keyWordIsTableName = function (index) {

        var start = index - 1;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "from" || beforeStringJoins === "join") {
                return true
            }
        }
        start = index - 2;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "insert into") {
                if (this.keywords[index] && this.keywords[index].keyword === "table") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "insert overwrite") {
                return true;
            }
            if (beforeStringJoins === "create table") {
                if (this.keywords[index] && this.keywords[index].keyword === "if") {
                    return false;
                }
                return true;
            }
            if (beforeStringJoins === "drop table") {
                if (this.keywords[index] && this.keywords[index].keyword === "if") {
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
            if (beforeStringJoins === "desc formatted") {
                return true;
            }
        }
        start = index - 3;
        if (start >= 0) {
            var beforeStringJoins = this.findKeyWordInArea(start, index);
            if (beforeStringJoins === "create external table") {
                return true;
            }
            if (beforeStringJoins === "create temporary table") {
                return true;
            }
            if (beforeStringJoins === "insert into table") {
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
        var lopIndex = index - 1;

        if (index >= 0 && lopIndex > 0) {
            var hasTableStartFlag = false;
            var tableStartIndex = 0;
            for (; lopIndex >= 0; lopIndex--) {
                var keyWord = this.keywords[lopIndex];
                if (keyWord.keyword == "join" || keyWord.keyword == "from") {
                    hasTableStartFlag = true;
                    tableStartIndex = lopIndex;
                    break;
                }
            }

            //from zhanglei as td  ,listsssa
            if (hasTableStartFlag && this.keywords[index - 1].keyword === "," && this.rangeKeyWordNoColumnType(tableStartIndex, index - 1)) {
                return true;
            }
        }


        return false;
    }
    this.findKeyWordInArea = function (start, end) {
        var arrays = []
        for (; start < end; start++) {
            arrays.push(this.keywords[start].keyword);
        }
        if (arrays.length > 0) {
            return arrays.join(" ");
        }
        return ""
    }
    /**
     * 两个keyWord 之间没有 column 提示
     */
    this.rangeKeyWordNoColumnType = function (startIndex, endIndex) {

        for (; startIndex < endIndex; startIndex++) {
            if (this.keywords[startIndex] && this.keywords[startIndex] && this.keywords[startIndex].type == KEY_WORD_TYPE.COLUMN_START) {
                return false;
            }
        }
        return true;
    }
    /**
     * 1。传入有可能是app,app.testTable.,testTable,columnName
     * 2。首先处理db,表，字段的情况
     * @param searchValue
     */
    this.getLikeSearch = function (searchValue) {
        var tips = [];
        var splitSeachValue = searchValue.split(".");


        if (splitSeachValue.length == 1) {
            //查询字段，db,表 eg : app , testTable , testColumn

            this.getLikeDb(tips, splitSeachValue[0]);
            this.getLikeTable(tips, splitSeachValue[0]);
            this.getLikeColumn(tips, splitSeachValue[0]);


        }
        if (splitSeachValue.length == 2) {
            //查询表，或者字段 app.testTable , app. , testTable. , testTable.c , db
            this.getLikeDb(tips, splitSeachValue[0], splitSeachValue[1]);
            this.getLikeTable(tips, splitSeachValue[0], splitSeachValue[1]);
        }
        return tips;
    }
    this.getLikeColumn = function (tipValues, searchColumn) {
        for (var index = 0; index < CACHE_tables.length; index++) {
            var table = CACHE_tables[index];
            if (table) {

                if (table.rangeEnd > 0) {
                    if (this.cursorPosition < table.rangeStart) {
                        continue;
                    }
                    if (this.cursorPosition > table.rangeEnd) {
                        continue;
                    }
                }

                for (var columnIndex = 0; columnIndex < table.sub.length; columnIndex++) {
                    var columnName = table.sub[columnIndex].columnName;
                    if(!columnName){
                        continue ;
                    }
                    var columnType = table.sub[columnIndex].columnType;
                    if (columnName.indexOf(searchColumn) != -1) {
                        var columnType = columnType.trim().length > 0 ? "[" + columnType + "]" : "";
                        var temp = {
                            meta: table.db + "." + (table.table ? table.table : "tmp_sub"),
                            value: columnName,
                            caption: columnName,
                            score: 100,
                            sqlType: columnType
                        }
                        tipValues.push(temp);
                    }
                }

            }
        }

    }
    //table
    //testTable,testTable. testTable.testColumn
    //
    this.getLikeTable = function (tipValues, tableName, sub) {

        for (var index = 0; index < CACHE_tables.length; index++) {
            var table = CACHE_tables[index];

            if (table.rangeEnd > 0) {
                if (this.cursorPosition < table.rangeStart) {
                    continue;
                }
                if (this.cursorPosition > table.rangeEnd) {
                    continue;
                }
            }

            if (table) {
                if (sub == undefined) {
                    if (table.table.startWith(tableName) || table.alias.startWith(tableName)) {
                        var temp = {
                            meta: "[TB]" + table.marketName + "." + table.db,
                            value: table.table,
                            caption: table.table,
                            score: 100
                        }
                        tipValues.push(temp);
                    }
                    continue;
                }
                if (table.table == tableName || (table.alias.length > 0 && table.alias == tableName)) {
                    var columns = table.sub;
                    for (var columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                        var columnName = columns[columnIndex].columnName;
                        var columnType = columns[columnIndex].columnType;
                        if (!columnName) {
                            continue;
                        }
                        var canAdd = sub == ""; // 添加所有的字段

                        if (columnName.indexOf(sub) != -1) {
                            canAdd = true;
                        }
                        var columnType = columnType.trim().length > 0 ? "[" + columnType + "]" : "";
                        if (canAdd) {
                            var temp = {
                                meta: table.db + "." +  (table.table ? table.table : "tmp_sub"),
                                value: columnName,
                                caption: columnName,
                                score: 100,
                                sqlType: columnType
                            }
                            tipValues.push(temp);
                        }
                    }

                }

            }
        }
    }
    //app.  -> "app" "" , ap - > "ap" , undefind , app.a -> "app" , "a"
    //sub == undefind 提示匹配库名
    //sub == "" 提示当前所有的表名称
    //sub == "a" 提示匹配当前库下 sub开头的表
    this.getLikeDb = function (tipValues, db, sub) {
        var keySets = CACHE_dbs.keySet();
        for (var index = 0; index < keySets.length; index++) {
            var key = keySets[index];
            var tables = CACHE_dbs.get(key);
            if (sub == undefined) {
                if (key.indexOf(db) != -1) {
                    var temp = {
                        meta: "[DB]" + key,
                        value: key,
                        caption: key,
                        score: 100
                    }
                    tipValues.push(temp);
                }
                continue;
            }
            if (key == db) {

                for (var tableIndex = 0; tableIndex < tables.length; tableIndex++) {
                    var table = tables[tableIndex];
                    var canAdd = sub == ""; // 添加所有的表

                    if (table.table.indexOf(sub) != -1) {
                        canAdd = true;
                    }

                    if (canAdd) {
                        var temp = {
                            meta: "[TB]" + table.marketName + "." + table.db + "." +  (table.table ? table.table : "tmp_sub"),
                            value: table.table,
                            caption: table.table,
                            score: 100
                        }
                        tipValues.push(temp);
                    }
                }
            }


        }
    }

    this.sqlTip = function () {
        try {
            // console.log(this.originSql);
            this.searchValue = "";
            this.originSql = this.originSql.toLocaleLowerCase();
            this.findKeyWord();
            var tipResult = {
                tipType: SQL_TIP_TYPE.NO_TIP,
                tipList: []
            }

            if (this.cursorPositionInNoTipArea()) {
                this.tipResult = tipResult;
                return this.tipResult;
            } else {
                var beforeChar = this.getChar(this.cursorPosition - 1);
                if (this.isOneWord(beforeChar)) {
                    this.searchValue = this.findBeforeOneWord(this.cursorPosition - 1);
                }
                var tipKeyWord = this.cusorPositionTipKeyWord();
                var editTable = this.isEditTable();
                if (!editTable) {
                    //只有编辑完table的时候才处理，table的查询
                    //前一次的提示是table
                    /*   if (this.tipResult.tipType == SQL_TIP_TYPE.TABLE) {
                           this.caculateTable();
                       } else {
                           if (tipKeyWord && tipKeyWord.type == KEY_WORD_TYPE.TABLE_START) {
                               this.caculateTable();
                           }
                       }*/
                    this.caculateTable();
                }


                /**
                 * 提示table
                 * fdm.zhangrui_test_01
                 * fdm.zhangrui_test_01 as t , zhangrui_test
                 * zhangrui_test_01
                 */
                if (editTable) {
                    this.tipResult.tipType = SQL_TIP_TYPE.TABLE;
                    var defaultDbKeyWord = this.findDefaultDbKeyWord(tipKeyWord.keywordIndex);
                    this.doTipTableName(this.searchValue, defaultDbKeyWord);
                } else {
                    if (this.searchValue.length > 0) {
                        this.tipResult.tipType = SQL_TIP_TYPE.COLUMN;

                        this.tipResult.tipList = this.getLikeSearch(this.searchValue);
                        if (this.tipResult.tipList.length > 0) {
                            this.editor.execCommand("startAutocomplete");
                        }
                    }
                }
            }
        } catch (e) {
            console.log(e);
        }

        return this.tipResult;
    }
    this.getTipResult = function () {
        return this.tipResult;
    }
    /* tableName 的提醒 */
    this.doTipTableName = function (searchOneWord, defaultDbNameKeyWord) {

        var tableName = "";
        var db = defaultDbNameKeyWord && defaultDbNameKeyWord.keyword ? defaultDbNameKeyWord.keyword.trim() : "";
        if (searchOneWord.indexOf(".") > -1) {
            db = searchOneWord.split(".")[0];
            tableName = searchOneWord.split(".")[1];
        } else {
            tableName = searchOneWord;
        }
        //去后台数据库查询数据 && 输入字符的长度大于某个值 && 连续输入的时候应该停止去查询

        db = db.trim();
        tableName = tableName.trim();
        if (db.length > 0 && tableName.length >= this.tableQueryLength && this.marketId > 0) {
            /**
             * [
             *   {
             *       db:,
             *       queryName:,
             *       clusterId,
             *       marketId,
             *       queryTime:,
             *       queryResult:
             *   }
             * ]
             * @type {Array}
             */
            var queryTableParams = {
                dbName: db,
                tableName: tableName,
                clusterId: 0,
                marketId: this.marketId
            }

            var params = JSON.stringify(queryTableParams);
            console.log(params);
            //this.triggerSearchTableName(params);
            setTimeout("dataDevSqlTip.triggerSearchTableName(" + params + ")", 200);
        }

    }
    /**
     * 1.一定时间过后，如果searchValue没有改变那么执行查询
     * 2.触发查询表名 数据
     * @param queryTableParams
     */
    this.triggerSearchTableName = function (queryTableParams) {
        var _this = this;
        var searchValue = this.searchValue.indexOf(".") != -1 ? this.searchValue.split(".")[1] : this.searchValue;

        if (searchValue === queryTableParams.tableName) {


            $.ajax({
                type: 'POST',
                url: queryTableUrl,
                data: queryTableParams,
                success: function (data) {
                    if (data.code * 1 == 0) {
                        var tables = data.obj; //
                        var tempTipList = [];
                        //[{meta: "meta", caption: "sonic", value: "sonic", score:1}]
                        for (var index = 0; index < tables.length; index++) {
                            var table = tables[index];
                            var tipTemp = {
                                meta: "[TB]" + table.marketName + "." + table.dbName,
                                caption: table.tbName,
                                value: table.tbName,
                                score: 100
                            }
                            tempTipList.push(tipTemp);
                        }

                        _this.tipResult.tipList = tempTipList;
                        //_this.callBack && _this.callBack(null, tempTipList);
                        _this.editor.execCommand("startAutocomplete");

                    }
                },
                dataType: "json"
            });


        }
    }

    /**
     * 找到最近的一个 keyword(提示字段，或者表名)
     * @returns {undefined}
     */
    this.cusorPositionTipKeyWord = function () {
        var beforeKeyWord = undefined;
        for (var index = this.keywords.length - 1; index >= 0; index--) {
            var keyword = this.keywords[index];
            if (this.cursorPosition > keyword.startIndex
                && (keyword.type == KEY_WORD_TYPE.COLUMN_START || keyword.type == KEY_WORD_TYPE.TABLE_START || keyword.type == KEY_WORD_TYPE.STRING_START)) {
                beforeKeyWord = keyword;
                break;
            }
        }
        return beforeKeyWord;
    }

    /**
     * 1。找到某个index之前的
     * @param index
     * @returns {*}
     */
    this.findDefaultDbKeyWord = function (index) {
        for (; index >= 0; index--) {
            if (this.keywords[index].type == KEY_WORD_TYPE.DEFAULT_DB_START) {
                return this.keywords[index + 1];
            }
        }
        return undefined;
    }
}

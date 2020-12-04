define("ace/mode/sql_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;
    var sqlFuncHighlightStr = require("./text_highlight_rules").sqlFuncHighlightStr;

    var SqlHighlightRules = function () {

        // var keywords = (
        //     "select|insert|update|delete|from|where|and|or|group|by|order|limit|offset|having|as|case|" +
        //     "when|else|end|type|left|right|join|on|outer|desc|asc|union|create|table|primary|key|if|" +
        //     "foreign|not|references|default|null|inner|cross|natural|database|drop|grant"
        // );
        var keywords = ("add|admin|after|all|alter|analyze|and|archive|array|as|asc|authorization|before|between|" +
        "bigint|binary|boolean|both|bucket|buckets|by|cascade|case|cast|change|char|cluster|clustered|clusterstatus|" +
        "collection|column|columns|comment|compact|compactions|compute|concatenate|conf|continue|create|cross|cube|" +
        "current|current_date|current_timestamp|cursor|data|database|databases|date|datetime|day|dbproperties|decimal|" +
        "deferred|defined|delete|delimited|dependency|desc|describe|directories|directory|disable|distinct|distribute|" +
        "double|drop|elem_type|else|enable|end|escaped|exchange|exclusive|exists|explain|export|extended|external|false|" +
        "fetch|fields|file|fileformat|first|float|following|for|format|formatted|from|full|function|functions|grant|group|" +
        "grouping|having|hold_ddltime|hour|idxproperties|if|ignore|import|in|index|indexes|inner|inpath|inputdriver|inputformat|" +
        "insert|int|intersect|interval|into|is|items|jar|join|keys|key_type|lateral|left|less|like|limit|lines|load|local|location|" +
        "lock|locks|logical|long|macro|map|mapjoin|materialized|minus|minute|month|more|msck|none|noscan|not|no_drop|null|of|offline|" +
        "on|option|or|order|out|outer|outputdriver|outputformat|over|overwrite|owner|partialscan|partition|partitioned|partitions|percent|" +
        "plus|preceding|preserve|pretty|principals|procedure|protection|purge|range|read|readonly|reads|rebuild|recordreader|recordwriter|" +
        "reduce|regexp|reload|rename|repair|replace|restrict|revoke|rewrite|right|rlike|role|roles|rollup|row|rows|schema|schemas|second|select|" +
        "semi|serde|serdeproperties|server|set|sets|shared|show|show_database|skewed|smallint|sort|sorted|ssl|statistics|stored|streamtable|string|" +
        "struct|table|tables|tablesample|tblproperties|temporary|terminated|then|timestamp|tinyint|to|touch|transactions|transform|trigger|true|" +
        "truncate|unarchive|unbounded|undo|union|uniontype|uniquejoin|unlock|unset|unsigned|update|uri|use|user|using|utc|utctimestamp|values|" +
        "value_type|varchar|view|when|where|while|window|with|year")

        var builtinConstants = (
            "true|false"
        );

        var builtinFunctions = (
            "avg|count|first|last|max|min|sum|ucase|lcase|mid|len|round|rank|now|format|" +
            "coalesce|ifnull|isnull|nvl|" + ((sqlFuncHighlightStr != undefined && sqlFuncHighlightStr.length > 0) ? sqlFuncHighlightStr : "")
        );

        var dataTypes = (
            "int|numeric|decimal|date|varchar|char|bigint|float|double|bit|binary|text|set|timestamp|" +
            "money|real|number|integer"
        );

        var keywordMapper = this.createKeywordMapper({
            "support.function": builtinFunctions,
            "keyword": keywords,
            "constant.language": builtinConstants,
            "storage.type": dataTypes
        }, "identifier", true);

        this.$rules = {
            "start": [{
                token: "comment",
                regex: "--.*$"
            }, {
                token: "comment",
                start: "/\\*",
                end: "\\*/"
            }, {
                token: "string",           // " string
                regex: '".*?"'
            }, {
                token: "string",           // ' string
                regex: "'.*?'"
            }, {
                token: "string",           // ` string (apache drill)
                regex: "`.*?`"
            }, {
                token: "constant.numeric", // float
                regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
            }, {
                token: keywordMapper,
                regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
            }, {
                token: "keyword.operator",
                regex: "\\+|\\-|\\/|\\/\\/|%|<@>|@>|<@|&|\\^|~|<|>|<=|=>|==|!=|<>|="
            }, {
                token: "paren.lparen",
                regex: "[\\(]"
            }, {
                token: "paren.rparen",
                regex: "[\\)]"
            }, {
                token: "text",
                regex: "\\s+"
            }]
        };
        this.normalizeRules();
    };

    oop.inherits(SqlHighlightRules, TextHighlightRules);

    exports.SqlHighlightRules = SqlHighlightRules;
});

define("ace/mode/sql", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/sql_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var SqlHighlightRules = require("./sql_highlight_rules").SqlHighlightRules;

    var Mode = function () {
        this.HighlightRules = SqlHighlightRules;
        this.$behaviour = this.$defaultBehaviour;
    };
    oop.inherits(Mode, TextMode);

    (function () {

        this.lineCommentStart = "--";

        this.$id = "ace/mode/sql";
    }).call(Mode.prototype);

    exports.Mode = Mode;

});

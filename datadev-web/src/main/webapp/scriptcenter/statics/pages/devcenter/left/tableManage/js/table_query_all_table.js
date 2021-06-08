/*
 * 表管理左侧查询条件
 * created by hl
 */
/**
 * 脚本提交的逻辑操作
 * @type {ScriptCommit}
 * @dep 依赖项： jquery
 * @dep 依赖项： artDialog
 * @dep 依赖项: wait方法 scriptcenter\statics\pages\home\home_index.js
 */
var TableQueryForm = /** @class */ (function () {
    function TableQueryForm(cfg) {
        this.config = cfg;
        this.state = {};
        this.isInstance();
    }

    TableQueryForm.prototype.run = function () {
        this.bindEvent();
    }

    // 校验是否实例
    TableQueryForm.prototype.isInstance = function () {
        if (!(this instanceof TableQueryForm)) {
            throw TypeError("Class constructor An cannot be invoked without 'new'");
        }
    };
    // 绑定事件
    TableQueryForm.prototype.bindEvent = function () {
        this._bindChangeAdvanceSearchVisible();
    };

    // 切换高级搜索部分展示状态
    TableQueryForm.prototype._bindChangeAdvanceSearchVisible = function () {
        $('#AllTablesContent').on('click', '#allTableSearchBaseContentButton', function () {
            $('#tableManageSearchAdvanced').toggleClass('none')
            $('#allTableContainer').toggleClass('all-table-container--top')
        })
    }
    return TableQueryForm;
}())

$(function () {
    var tableQueryForm = new TableQueryForm();
    tableQueryForm.run();
})






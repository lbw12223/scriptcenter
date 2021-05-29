/*
 * 表管理左侧查询条件
 * created by hl
 */

$(function () {

    initPage()

    function initPage() {
        bindBaseEvent()
    }

    // 绑定事件
    function bindBaseEvent() {
        _bindChangeAdvanceSearchVisible();
    }

    // 切换高级搜索部分展示状态
    function _bindChangeAdvanceSearchVisible() {
        $('#AllTablesContent').on('click', '#allTableSearchBaseContentButton', function () {
            $('#tableManageSearchAdvanced').toggleClass('none')
        })
    }
})

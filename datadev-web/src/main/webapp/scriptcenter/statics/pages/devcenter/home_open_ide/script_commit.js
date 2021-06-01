/**
 * 脚本提交的逻辑操作
 * @type {ScriptCommit}
 */
var ScriptCommit = /** @class */ (function () {
    function ScriptCommit(cfg) {
        this.config = cfg;
        this.isInstance();
    }

    // 校验是否实例
    ScriptCommit.prototype.isInstance = function () {
        if (!(this instanceof ScriptCommit)) {
            throw TypeError("Class constructor An cannot be invoked without 'new'");
        }
    };
    // 绑定事件
    ScriptCommit.prototype.bindEvent = function () {
    };
    // 更新dom
    ScriptCommit.prototype.updateDom = function () {
    };
    // 是否变更且未保存
    ScriptCommit.prototype.isChangeAndNotSave = function () {

    };
    // 未修改的弹窗提示
    ScriptCommit.prototype.tipNotSave = function () {

    };
    // 调取版本比对接口
    ScriptCommit.prototype.getCompareDataApi = function () {
    };
    // 渲染版本比对的dom
    ScriptCommit.prototype.renderCompareDom = function () {
    };
    // 下一步按钮事件
    ScriptCommit.prototype.bindNextStepEvent = function () {
    };
    // 上一步按钮事件
    ScriptCommit.prototype.bindPrevStepEvent = function () {
    };
    // 获取关联任务接口
    ScriptCommit.prototype.getRelationApi = function () {
    }


    return ScriptCommit;
}());

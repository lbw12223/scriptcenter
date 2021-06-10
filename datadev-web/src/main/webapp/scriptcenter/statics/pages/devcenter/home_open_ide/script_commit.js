/**
 * 脚本提交的逻辑操作
 * @type {ScriptCommit}
 * @dep 依赖项： jquery
 * @dep 依赖项： artDialog
 * @dep 依赖项: wait方法 scriptcenter\statics\pages\home\home_index.js
 */
var ScriptCommit = /** @class */ (function () {
    function ScriptCommit(cfg) {
        this.config = cfg;
        this.state = {
            // 没有保存dialog
            notSaveDialog: null,
            // 步骤的dialog
            stepDialog: null,
            // 校验结果弹窗
            validDialog: null,
            spin: null,
            step: 0
        };
        this.dom = {}
        this.isInstance();
    }

    ScriptCommit.prototype.run = function () {
        this.bindEvent();
    }
    // 校验是否实例
    ScriptCommit.prototype.isInstance = function () {
        if (!(this instanceof ScriptCommit)) {
            throw TypeError("Class constructor An cannot be invoked without 'new'");
        }
    };
    // 绑定事件
    ScriptCommit.prototype.bindEvent = function () {
        var _this = this;
        console.log($('#contentWrap'))
        console.log($('#pushOnline'))
        $('#contentWrap').on('click', '#pushOnline', function () {
            if (_this.isChangeAndNotSave(editor)) {
                _this.tipNotSave();
            } else {
                _this.showDiff();
            }
        })
    };
    // 更新dom
    ScriptCommit.prototype.updateDom = function () {
    };
    /**
     * 是否变更且未保存
     * @param editor 编辑器的实例
     * @return {boolean}
     */
    ScriptCommit.prototype.isChangeAndNotSave = function (editor) {
        if (!scriptHelper) {
            return false;
        }
        return scriptHelper.validIsChangeCode(editor)
    };
    // 未修改的弹窗提示
    ScriptCommit.prototype.tipNotSave = function () {
        this.state.notSaveDialog = $.fn.dialog({
            title: '提示',
            content: '脚本必须先保存才能提交,请先保存脚本',
            lock: true,
            width: "600",
            height: "400",
            opacity: 0.5,
            esc: false,
            cancelValue: '关闭',
            cancel: function () {
            }
        });
        this.state.notSaveDialog.show();
    };
    // 第一步展示对比
    ScriptCommit.prototype.showDiff = function () {
        var _this = this;
        var domWrapStr = '<div class="bdp-script-commit-step-dialog" id="bdpScriptCommitStepDialog"></div>';
        this.state.stepDialog = $.dialog({
            title: '版本比对',
            content: $(domWrapStr).get(0),
            lock: true,
            width: "600",
            height: "400",
            opacity: 0.5,
            esc: false,
            button: [
                {
                    id: 'bdpScriptCommitCancelButton',
                    value: '取消',
                    name: '取消'
                },
                {
                    id: 'bdpScriptCommitStepButton',
                    value: '下一步',
                    name: "下一步",
                    callback: function () {
                        _this.bindNextStepEvent();
                        return false;
                    }
                }
            ]
        })
        ;
        this.state.stepDialog.show();
    };
    // 调取版本比对接口
    ScriptCommit.prototype.getCompareDataApi = function (scriptId, scriptName) {
        var _this = this;
        this.state.spin = wait && wait();
        commonAjaxEvents.commonSpinPostAjax("/scriptcenter/diff/scriptCompare.ajax",
            {
                scriptId: scriptId,
                scriptName: scriptName
            }, null,
            function (node, data) {
                console.log(data.obj);
                _this.renderCompareDom();
            }, this.state.spin);
    };
    // 渲染版本比对的dom
    ScriptCommit.prototype.renderCompareDom = function () {

    };
    // 下一步按钮事件
    ScriptCommit.prototype.bindNextStepEvent = function () {
        this.state.step++;
    };
    // 上一步按钮事件
    ScriptCommit.prototype.bindPrevStepEvent = function () {
    };
    // 渲染关联任务dom
    ScriptCommit.prototype.renderRelationDom = function () {
    };
    // 第二步,提交到发布中心dialog渲染(改变)
    ScriptCommit.prototype.renderRelationDialog = function () {
        if (!this.state.stepDialog) {
            throw new Error("没有打开弹窗")
        }
        this.state.stepDialog.title('提交至发布中心');
        this.state.stepDialog.content('提交至发布中心');
        // TODO 写一个js生成的title组件
    };
    // 获取关联任务接口
    ScriptCommit.prototype.getRelationApi = function (scriptId, scriptName) {
        var _this = this;
        this.state.spin = wait && wait();
        commonAjaxEvents.commonSpinPostAjax("/scriptcenter/diff/scriptTaskList.ajax",
            {
                scriptId: scriptId,
                scriptName: scriptName
            }, null,
            function (node, data) {
                console.log(data.obj);
                _this.renderRelationDom();
            }, this.state.spin);
    };

    // 渲染提交至发布中心的表单渲染
    ScriptCommit.prototype.renderCommitForm = function () {

    };
    // 获取校验结果
    ScriptCommit.prototype.getValidResultApi = function (scriptId, scriptName) {
        var _this = this;
        this.state.spin = wait && wait();
        commonAjaxEvents.commonSpinPostAjax("/scriptcenter/diff/check.ajax",
            {
                scriptId: scriptId,
                scriptName: scriptName
            }, null,
            function (node, data) {
                console.log(data.obj);
                _this.state.stepDialog.remove();
                _this.reset();
                _this.renderValidResultDom(data.obj);
            }, this.state.spin);
    };
    // 校验结果展示弹窗
    ScriptCommit.prototype.renderValidResultDom = function (data) {
        // 判断是否成功失败
        this.state.notSaveDialog = $.fn.dialog({
            content: '提交失败',
            lock: true,
            width: "600",
            height: "400",
            opacity: 0.5,
            esc: false,
            cancelValue: '关闭',
            cancel: function () {
            }
        });
        this.state.notSaveDialog.show();

    };
    // 调提交接口
    ScriptCommit.prototype.saveCommitInfoApi = function () {
    };
    // 重置
    ScriptCommit.prototype.reset = function () {
        this.state.step = 0;
        this.state.notSaveDialog = null;
        this.state.stepDialog = null;
        this.state.validDialog = null;
        this.state.spin = null;
    }
    return ScriptCommit;
}())

$(function () {
    var scriptCommit = new ScriptCommit();
    console.log(scriptCommit)
    scriptCommit.run();
})


// 此处编写用于script的一些辅助方法
var scriptHelper = {
    // 校验代码是否变更
    validIsChangeCode: function (editor, oMd5,) {
        if (!editor) {
            return false;
        }
        var oldMd5 = oMd5 ? oMd5 : $("#fileMd5").val();
        var newMd5 = $.md5(editor.getValue());
        return oldMd5 !== newMd5
    }
}

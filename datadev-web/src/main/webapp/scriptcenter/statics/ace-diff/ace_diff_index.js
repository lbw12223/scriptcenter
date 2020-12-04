var diffDialog=undefined;
var callBackFun=undefined;

/**
 *
 * @param serverVersion
 * @param gitProjectId
 * @param gitProjectFilePath
 * @param callBack 点击提交后的回调
 */
function diff( serverVersion,gitProjectId,gitProjectFilePath,callBack) {
    var encodeGitProjectFilePath=encodeURIComponent(encodeURIComponent(gitProjectFilePath));
    callBackFun=callBack;
    diffDialog=$.dialog.open("/scriptcenter/script/aceDiff.html?serverVersion="+serverVersion+"&gitProjectFilePath="+encodeGitProjectFilePath+"&gitProjectId="+gitProjectId,{title:"合并脚本",lock:true,width:"90%",height:"90%",opacity:0.5,esc:false,close:function () {
    }});
}
function dialogCallBack(key, data) {
    diffDialog && diffDialog.close()
    callBackFun(key,data);
}

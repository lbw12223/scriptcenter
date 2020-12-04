var diffDialog=undefined;
var callBackFun=undefined;

/**
 *
 * @param serverVersion
 * @param gitProjectId
 * @param gitProjectFilePath
 * @param callBack 点击提交后的回调
 */
function diff( gitProjectId,gitProjectFilePath,callBack,key) {
    var encodeGitProjectFilePath=encodeURIComponent(encodeURIComponent(gitProjectFilePath));
    var url="/scriptcenter/script/aceDiff.html?gitProjectFilePath="+encodeGitProjectFilePath+"&gitProjectId="+gitProjectId;
    callBackFun=callBack;
    diffDialog=$.dialog.open(url,{title:"合并脚本",lock:true,width:"90%",height:"90%",opacity:0.5,esc:false,close:function () {
            saveStatusMap.put(key,0);
        }});
}
function dialogCallBack(key, data) {
    diffDialog && diffDialog.close()
    callBackFun(key,data);
}

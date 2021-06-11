String.prototype.endWith=function(s){
    if(s==null||s==""||this.length==0||s.length>this.length)
        return false;
    if(this.substring(this.length-s.length)==s)
        return true;
    else
        return false;
    return true;
}

String.prototype.startWith=function(s){
    if(s==null||s==""||this.length==0||s.length>this.length)
        return false;
    if(this.substr(0,s.length)==s)
        return true;
    else
        return false;
    return true;
}


var commonAjaxEvents = {

    commonPostAjax: function (_url, _data, node, succssCallback, complete, parentModal,isJson,spinner,beforeEvent) {
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId <= 0 ? 10347 : top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        //_data['projectSpaceId'] = 10109;
        $.ajax({
            url: _url,
            data: _data,
            type: "post",
            dataType: 'json',
            contentType:isJson?"application/json;charset=utf-8":"application/x-www-form-urlencoded;charset=utf-8",
            beforeSend: function () {
                beforeEvent && beforeEvent();
                node && disableButton(node);
            },
            success: function (data) {
                enabledButton(node);
                $.removeMsg();
                if (data.code || data._code) {
                    var alertMessage = data.message || data.errorMessage ;
                    if(alertMessage.indexOf("需要登录系统") > -1){
                        var logginUrl = _bdpDomain +"/login.html";
                        alertMessage += " <a style='color:#2001e9;cursor: pointer;' href='"+logginUrl+"' target='_blank' >重新登录</a>";
                    }
                    if (data.code * 1 === 0 || data._code * 1 === 0) {
                        succssCallback && succssCallback(node, data)
                    } else {
                        parentModal && parentModal.modal("hide");
                        if (parent.$ && parent.$.errorMsg ) {
                            parent.$.errorMsg(alertMessage);
                        }else{
                            $.errorMsg(alertMessage);
                        }
                    }
                } else {
                    succssCallback && succssCallback(node, data);
                }

            },
            complete:function () {
                complete && complete()
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                enabledButton(node);
                if (parent.$) {
                   // parent.$.errorMsg("出现异常,请联系管理员!");
                }else{
                   // $.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    },
    commonAllPostAjax: function (_url, _data, node, succssCallback,error, complete,isJson) {
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        $.ajax({
            url: _url,
            data: _data,
            type: "post",
            dataType: 'json',
            contentType:isJson?"application/json;charset=utf-8":"application/x-www-form-urlencoded;charset=utf-8",
            beforeSend: function () {
                node && disableButton(node);
            },
            success: function (data) {
                $.removeMsg();
                enabledButton(node);
                if (data.code || data._code) {
                    var alertMessage = data.message || data.errorMessage ;
                    if(alertMessage.indexOf("需要登录系统") > -1){
                        var logginUrl = _bdpDomain +"/login.html";
                        alertMessage += " <a style='color:#2001e9;cursor: pointer;' href='"+logginUrl+"' target='_blank' >重新登录</a>";
                    }
                    if (data.code * 1 === 0 || data._code * 1 === 0) {
                        succssCallback && succssCallback(node, data)
                    } else {
                        if (parent.$ && parent.$.errorMsg) {
                            parent.$.errorMsg(alertMessage);
                        }else{
                            $.errorMsg(alertMessage);
                        }
                        error && error();
                    }
                } else {
                    succssCallback && succssCallback(node, data);
                }

            },
            complete:function () {
                complete && complete()
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                enabledButton(node);
                error && error();
                if (parent.$) {
                    //parent.$.errorMsg("出现异常,请联系管理员!");
                }else{
                    //$.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    },

    runTimeLogPostAjax: function (_url, _data, node, succssCallback,error) {
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        $.ajax({
            url: _url,
            data: _data,
            type: "post",
            dataType: 'json',
            contentType:"application/x-www-form-urlencoded;charset=utf-8",
            beforeSend: function () {
            },
            success: function (data) {
                $.removeMsg();
                succssCallback && succssCallback(node, data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                error && error();
            }
        });
    },


    commonGetAjax:function (_url,_data,succssCallback){
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        var str = "?" ;
        for(params in _data){
            str += (params+"="+_data[params]+"&");
        }

        $.ajax({
            url: _url + str,
            type: "get",
            success: function (data) {
                if (data.code || data._code) {
                    var alertMessage = data.message || data.errorMessage ;
                    if(alertMessage.indexOf("需要登录系统") > -1){
                        var logginUrl = _bdpDomain +"/login.html";
                        alertMessage += " <a style='color:#2001e9;cursor: pointer;' href='"+logginUrl+"' target='_blank' >重新登录</a>";
                    }
                    if (data.code * 1 === 0 || data._code * 1 === 0) {
                        succssCallback && succssCallback(node, data)
                    } else {
                        parentModal && parentModal.modal("hide");
                        if (parent.$ && parent.$.errorMsg) {
                            parent.$.errorMsg(alertMessage);
                        }else{
                            $.errorMsg(alertMessage);
                        }
                    }
                } else {
                    succssCallback && succssCallback( data);
                }
            }
        });


    },

    commonJSONPostAjax: function (_url, _data, node, succssCallback, complete, parentModal) {
        window.commonAjaxEvents.commonPostAjax(_url, _data, node, succssCallback, complete, parentModal,true)
    },
    commonControlSuccessPostAjax: function (_url, _data, succssCallback,isJson) {
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        $.ajax({
            url: _url,
            data: _data,
            type: "post",
            dataType: 'json',
            contentType:isJson?"application/json;charset=utf-8":"application/x-www-form-urlencoded;charset=utf-8",
            beforeSend: function () {
            },
            success: function (data) {
                succssCallback(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (parent.$) {
                    //parent.$.errorMsg("出现异常,请联系管理员!");
                }else{
                    //$.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    },
    commonSpinPostAjax: function (_url, _data, node, succssCallback,spinner) {
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        $.ajax({
            url: _url,
            data: _data,
            type: "post",
            dataType: 'json',
            contentType:"application/x-www-form-urlencoded;charset=utf-8",
            beforeSend: function () {
                node && disableButton(node);
            },
            success: function (data) {
                if(spinner){
                    cancelWait(spinner)
                }
                enabledButton(node);
                if (data.code || data._code) {
                    var alertMessage = data.message || data.errorMessage ;
                    if(alertMessage.indexOf("需要登录系统") > -1){
                        var logginUrl = _bdpDomain +"/login.html";
                        alertMessage += " <a style='cursor: pointer;' href='"+logginUrl+"' target='_blank' >登陆地址</a>";
                    }
                    if (data.code * 1 === 0 || data._code * 1 === 0) {
                        succssCallback && succssCallback(node, data)
                    } else {
                        if (parent.$ && parent.$.errorMsg) {
                            parent.$.errorMsg(alertMessage);
                        }else{
                            $.errorMsg(alertMessage);
                        }
                    }
                } else {
                    succssCallback && succssCallback(node, data);
                }

            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if(spinner){
                    cancelWait(spinner)
                }
                enabledButton(node);
                if (parent.$) {
                    //parent.$.errorMsg("出现异常,请联系管理员!");
                }else{
                   // $.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    },
    commonSucAndBeforePostAjax: function (_url, _data, succssCallback,isJson,beforeEvent) {
        if(top && top.window && top.window.projectSpaceId){
            _data['projectSpaceId'] = top.window.projectSpaceId
        }else{
            _data['projectSpaceId'] = 0 ;
        }
        $.ajax({
            url: _url,
            data: _data,
            type: "post",
            dataType: 'json',
            contentType:isJson?"application/json;charset=utf-8":"application/x-www-form-urlencoded;charset=utf-8",
            beforeSend: function () {
                beforeEvent && beforeEvent();
            },
            success: function (data) {
                succssCallback(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (parent.$) {
                   // parent.$.errorMsg("出现异常,请联系管理员!");
                }else{
                    //$.errorMsg("出现异常,请联系管理员!");
                }
            }
        });
    }
}
function disableButton(button) {
    button && button.addClass("bdp-disabled");
};
function enabledButton(button) {
    button && button.removeClass("bdp-disabled");
};

/**
 * grace 表格
 * fix selectRows
 * @param selectRows
 * @returns {*}
 */
function fixSelectRows(selectRows) {
    if (typeof(selectRows) == "undefined") {
        return [];
    }
    if (typeof(selectRows.length) == "undefined") {
        return [];
    }
    return selectRows;
}


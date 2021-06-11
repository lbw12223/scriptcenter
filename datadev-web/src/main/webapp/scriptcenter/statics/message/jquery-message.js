$.extend({
    htmlTemplet: function (params) {
        if(top.Msg){
            return top.Msg.htmlTemplet(params);
        }
        var type = params.type;
        if (type === "success") {
            var SUCCESS_HTML =
                '<div class="successMessageBack"> ' +
                '<div class="messageContent  success">' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                ' </div>' +
                ' </div>';
            return SUCCESS_HTML;
        } else if (type === "confirm") {
            var BUTTONS_HTML = "";
            if (params.buttons && params.buttons.length > 0) {
                for (var index = 0; index < params.buttons.length; index++) {
                    var button = params.buttons[index];
                    var isAppendCloseFlag = !button.event ? "messageClose" : "";
                    BUTTONS_HTML += "<a class='messageButton " + isAppendCloseFlag + "' href='javascript:void(0)'>" + button.text + "</a>&nbsp;&nbsp;"
                }
            } else {
                BUTTONS_HTML = "<span class='messageClose messageCloseA'>×</span>"
            }
            var CONFIRM_HTML =
                '<div class="messageBack"> ' +
                '<div class="messageContent confirm">' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                '   <span class="messageCloseButton">' + BUTTONS_HTML +
                '   </span>' +
                ' </div>' +
                ' </div>';

            return CONFIRM_HTML;
        } else if (type == "noBackConfirm") {
            var BUTTONS_HTML = "";
            if (params.buttons && params.buttons.length > 0) {
                for (var index = 0; index < params.buttons.length; index++) {
                    var button = params.buttons[index];
                    var isAppendCloseFlag = !button.event ? "messageClose" : "";
                    BUTTONS_HTML += "<a class='messageButton " + isAppendCloseFlag + "' href='javascript:void(0)'>" + button.text + "</a>&nbsp;&nbsp;"
                }
            } else {
                BUTTONS_HTML = "<span class='messageClose messageCloseA'>×</span>"
            }
            var CONFIRM_HTML =
                '<div class="messageContent confirm noBackConfirm">' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                '   <span class="messageCloseButton">' + BUTTONS_HTML +
                '   </span>' +
                ' </div>';

            return CONFIRM_HTML;
        } else if (type === "error") {
            var BUTTONS_HTML = "<span class='messageClose messageCloseA'>×</span>"
            var ERROR_HTML =
                '<div class="messageBack"> ' +
                '<div class="messageContent  error">' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                '   <span class="messageCloseButton">' + BUTTONS_HTML +
                ' </div>' +
                ' </div>';
            return ERROR_HTML;
        } else if (type === "wait") {
            var WAIT_HTML =
                '<div class="messageBack"> ' +
                '<div class="messageContent  wait">' +
                ' </div>' +
                ' </div>';
            return WAIT_HTML;
        } else if (type === "loading") {
            var BUTTONS_HTML = "<span class='messageClose messageCloseA'>×</span>"
            var CONFIRM_HTML =
                '<div class="messageBack"> ' +
                '<div class="messageContent confirm">' +
                '<div class="loader">' +
                '<div class="loading-3">' +
                '<i></i>' +
                '<i></i>' +
                '<i></i>' +
                '<i></i>' +
                '<i></i>' +
                '<i></i>' +
                '<i></i>' +
                '<i></i>' +
                '</div>' +
                '</div>' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                '   <span class="messageCloseButton">' + BUTTONS_HTML +
                '   </span>' +
                ' </div>' +
                ' </div>';

            return CONFIRM_HTML;
        } else if (type === "bdpMsg") {
            /**
             * title
             * mainContent
             * subContent
             * buttons
             * @param params
             */
            var BUTTON_HTML = "";
            if (params.buttons && params.buttons.length > 0) {
                for (var index = 0; index < params.buttons.length; index++) {
                    var button = params.buttons[index];
                    var appendClass = button.btnClass ? button.btnClass : "bdp-btn-default";
                    BUTTON_HTML += '<a id="'+(button.id||"")+'" class="bdp-btn messageButton ' + appendClass + (button.datadevFocus?" datadev-focus":"") +'" style="margin-left: 5px;">' + button.text + '</a>';
                }
            }
            var BDP_MSG_HTML = "";
            var css = "";
            if (params.width) {
                css += "width:" + params.width + ";"
            }
            if (params.height) {
                css += "height:" + params.height + ";"
            }
            BDP_MSG_HTML = ' <div class="messageBack">' +
                ' <div ' + (css ? ('style="' + css + '"') : '') + 'class="messageContent bdp-msg">' +
                ' <div class="bdp-msg-title">' +
                ' <span class="bdp-msg-title-content">' +
                (params.title ? params.title : "提示") +
                '</span>' +
                ' <span class="messageClose messageCloseA bdp-msg-title-close" >×</span>' +
                ' </div>' +
                ' <div class="bdp-msg-main-content" >' +
                (params.mainContent ? params.mainContent : "") +
                ' </div>' +
                ' <div class="bdp-msg-buttons">' + BUTTON_HTML +
                '</div>' +
                ' </div>'
            return BDP_MSG_HTML;
        } else if (type == "error2") {
            // var BUTTONS_HTML = "<span class='messageClose messageCloseA'>X</span>"
            var ERROR_HTML =
                '<div class="successMessageBack"> ' +
                '<div class="messageContent  error">' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                ' </div>' +
                ' </div>';
            return ERROR_HTML;
        } else if (type === "newBdpMsg") {
            /**
             * title
             * mainContent
             * subContent
             * buttons
             * @param params
             */
            var BUTTON_HTML = "";
            if (params.buttons && params.buttons.length > 0) {
                for (var index = 0; index < params.buttons.length; index++) {
                    var button = params.buttons[index];
                    var appendClass = button.btnClass ? button.btnClass : "bdp-btn-default";
                    BUTTON_HTML += '<a class="bdp-btn messageButton ' + appendClass + '" style="margin-left: 5px;">' + button.text + '</a>';
                }
            }
            var BDP_MSG_HTML = "";
            var css = "";
            if (params.width) {
                css += "width:" + params.width + ";"
            }
            if (params.height) {
                css += "height:" + params.height + ";"
            }
            BDP_MSG_HTML = ' <div class="newMessageBack">' +
                ' <div ' + (css ? ('style="' + css + '"') : '') + 'class="messageContent bdp-msg">' +
                ' <div class="bdp-msg-title">' +
                ' <span class="bdp-msg-title-content">' +
                (params.title ? params.title : "提示") +
                '</span>' +
                ' <span class="messageClose messageCloseA bdp-msg-title-close" >×</span>' +
                ' </div>' +
                ' <div class="bdp-msg-main-content" >' +
                (params.mainContent ? params.mainContent : "") +
                ' </div>' +
                ' <div class="bdp-msg-buttons">' + BUTTON_HTML +
                '</div>' +
                ' </div>'
            return BDP_MSG_HTML;
        } else if (type == "error2") {
            // var BUTTONS_HTML = "<span class='messageClose messageCloseA'>X</span>"
            var ERROR_HTML =
                '<div class="successMessageBack"> ' +
                '<div class="messageContent  error">' +
                '   <span class="message">' +
                params.content +
                '   </span>' +
                ' </div>' +
                ' </div>';
            return ERROR_HTML;
        }
        return HTML;
    },
    successMsg: function (_content, delay, callback) {
        if(top.Msg){
            top.Msg.successMsg(_content,delay,callback);
            return ;
        }
        this.removeMsg();
        var params = {
            content: _content,
            type: "success"
        }
        var html = this.htmlTemplet(params);
        window.setTimeout(function () {
            $(".successMessageBack").fadeOut(500);
            callback && callback();
        }, delay || 2000);
        $("body").append(html);
    },
    confirmMsg: function (_params) {
        if(top.Msg){
            top.Msg.confirmMsg(_params);
            return ;
        }
        this.removeMsg();
        var params = {
            content: _params.content,
            type: "confirm",
            buttons: _params.buttons
        }
        var html = this.htmlTemplet(params);
        $("body").append(html);
        //绑定事件
        $(".messageClose").click(function () {
            $(".messageBack").remove();
        });
        $(".messageButton").each(function (index) {
            var node = $(this);
            node.click(function () {
                _params.buttons[index] && _params.buttons[index].event && _params.buttons[index].event();
            })
        })
    },
    confirmNoBackMsg: function (_params) {
        if(top.Msg){
            top.Msg.confirmNoBackMsg(_params);
            return ;
        }
        this.removeMsg();
        var params = {
            content: _params.content,
            type: "noBackConfirm",
            buttons: _params.buttons
        }
        var html = this.htmlTemplet(params);
        $("body").append(html);
        //绑定事件
        $(".messageClose").click(function () {
            $(".noBackConfirm").remove();
        });
        $(".messageButton").each(function (index) {
            var node = $(this);
            node.click(function () {
                _params.buttons[index] && _params.buttons[index].event && _params.buttons[index].event();
            })
        })
    },

    errorMsg: function (_content) {
        if(top.Msg){
            top.Msg.errorMsg(_content);
            return ;
        }
        this.removeMsg();
        var params = {
            content: _content,
            type: "error",
        }
        var html = this.htmlTemplet(params);
        $("body").append(html);
        //绑定事件
        $(".messageClose").click(function () {
            $(".messageBack").remove();
        });
    },
    errorMsg2: function (_content, delay, callback) {
        if(top.Msg){
            top.Msg.errorMsg2(_content, delay, callback);
            return ;
        }
        this.removeMsg();
        var params = {
            content: _content,
            type: "error2",
        }
        var html = this.htmlTemplet(params);
        $("body").append(html);
        window.setTimeout(function () {
            // $(".messageClose").click();
            $(".successMessageBack").fadeOut(500);
            callback && callback();
        }, delay || 2000);
    },
    loadingMsg: function (_content) {
        if(top.Msg){
            top.Msg.loadingMsg(_content);
            return ;
        }

        this.removeMsg();
        var params = {
            content: _content,
            type: "loading",
        }
        var html = this.htmlTemplet(params);
        $("body").append(html);
        //绑定事件
        $(".messageClose").click(function () {
            $(".messageBack").remove();
        });
    },
    /**
     * title
     * mainContent
     * subContent
     * buttons
     * @param params
     */
    bdpMsg: function (params, init) {
        if(top.Msg){
            top.Msg.bdpMsg(params,init);
            return ;
        }
        this.removeMsg();
        params.type = "bdpMsg";
        var html = this.htmlTemplet(params);
        $("body").append(html);
        init && init();
        $(".datadev-focus").focus();
        //绑定事件
        $(".messageClose").click(function () {
            params.afterCloseCallBack && params.afterCloseCallBack();
            $(".messageBack").remove();
        });
        $(".messageButton").each(function (index) {
            var node = $(this);
            node.click(function () {
                params.buttons[index] && params.buttons[index].event && params.buttons[index].event();
            })
        })
    },
    /**
     * title
     * mainContent
     * subContent
     * buttons
     * @param params
     */
    newBdpMsg: function (params, init) {
        if(top.Msg){
            top.Msg.newBdpMsg(params,init);
            return ;
        }

        this.removeMsg();
        params.type = "newBdpMsg";
        var html = this.htmlTemplet(params);
        $("body").append(html);
        init && init();
        //绑定事件
        $(".messageClose").click(function () {
            params.afterCloseCallBack && params.afterCloseCallBack();
            $(".newMessageBack").remove();
        });
        $(".messageButton").each(function (index) {
            var node = $(this);
            node.click(function () {
                params.buttons[index] && params.buttons[index].event && params.buttons[index].event();
            })
        })
    },
    removeMsg: function () {
        if(top.Msg){
            top.Msg.removeMsg();
            return ;
        }
        $(".messageBack").remove();
        $(".successMessageBack").remove();
        $(".noBackConfirm").remove();
    },
    removeNewMsg(){
        if(top.Msg){
            top.Msg.removeNewMsg();
            return ;
        }
        $(".newMessageBack").remove();
    }
});

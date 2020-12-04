/**
 *  $("#personForm").sysRemoteUserSingleSelect({placeholder:"personForm",ajax:{url:"/jdw/ztc/rt/join/userList.ajax"}});
 *  $("#personForm").sysRemoteUserSingleSelect("selectedUserLists")获取选择对象List
 */

(function ($) {
    //默认的属性设置
    var defaultsOptions = function () {
        var defaults = {
            placeholder: "选择负责人",
            minimumInputLength: 0,
            multiple: false,
            cache: true,
            allowClear: true,
            id: function (user) {
                return user;
            },
            ajax: {
                url: "/jdw/ztc/rt/join/userList.ajax",
                dataType: 'json',
                method: 'post',
                data: function (term, page) {
                     return {
                        keyword: term,
                        rows: 10,
                        page: 1
                    };
                },
                results: function (data, page) {
                    return {results: data.dataList};
                }
            },
            initSelection: function (element, callback) {
                var data = element.val().split(",");
                $.ajax("/think/meta/table/listUsersByErps.ajax", {
                    method: 'post',
                    data: {
                        erps: data
                    },
                    dataType: "json"
                }).done(function (json) {
                    callback(json.dataList);
                });
            },
            //下拉框显示的值
            formatResult:function formatResult(user) {
                 return user.name + "(" + user.email + user.deptName+ ")";
             },
            //选择过后，反填写的值
            formatSelection: function formatSelection(user) {
               /* var fixDepart = user.deptName  ;
                if(fixDepart.indexOf("-") > 0){
                    fixDepart =  fixDepart.substr(fixDepart.lastIndexOf("-")+1);
                }*/
                 return user.name + "(" +user.deptName + ")";
            },
            dropdownCssClass: "bigdrop",
            formatSearching: function () {
                return "加载中..."
            },
            formatNoMatches: function (term, data) {
                alert(term + " data : " + data)
                return "没有匹配结果."
            },
            escapeMarkup: function (m) {
                return m;
            }
        }
        return defaults;
    }
    var privateMethod = {
        removeSelectedUser : function (selectedUsers , removeUser) {
            if(selectedUsers && selectedUsers.length > 0){
                for(var index = 0 ; index < selectedUsers.length ; index++ ){
                    if(selectedUsers[index].erp === removeUser.erp){
                        selectedUsers.splice(index,1);
                        return ;
                    }
                }
            }
        },
        addSelectedUser : function (selectedUser , addUser) {
            selectedUser.push(addUser);
        },
        getSelectedUserErp:function (selectedUser) {
            var result = "";
            if( selectedUser  && selectedUser.length > 0 ){
                for(var index = 0 ; index < selectedUser.length ; index++ ){
                     result += ","+selectedUser[index].erp ;
                }
            }
            return result.length > 1 ? result.substr(1) : "";
        }
    }


    var methods = {

        init: function (options) {
            // 在每个元素上执行方法
            return this.each(function () {
                var $this = $(this);
                var settings = $this.data('sysRemoteUserSingleSelect');
                if (typeof(settings) == 'undefined') {
                    settings = $.extend(true,{}, defaultsOptions(), options);
                    $this.data('sysRemoteUserSingleSelect', settings);
                }
                //初始化
                $this.select2(settings).on("change", function (e) {
                    var selectedData =  $this.data("selectedLists");
                    selectedData = selectedData ? selectedData : [] ;
                    if (e && e.removed) {
                        privateMethod.removeSelectedUser(selectedData , e.removed);
                    }
                    if (e && e.added) {
                        privateMethod.addSelectedUser(selectedData,e.added);
                    }
                    $this.val(privateMethod.getSelectedUserErp(selectedData));
                    $this.data("selectedLists",selectedData)
                    $this.valid && $(this).valid();
                }).on("select2-close", function() {
                    $this.valid && $(this).valid();
                });
            });
        },
        destroy: function (options) {
            // 在每个元素中执行代码
            return $(this).each(function () {
                var $this = $(this);
                $this.select2("destroy");
            });
        },
        selectedUserLists: function (options) {
            var result = this.data("selectedLists") ;
            return result ? result : [] ;
        },
        data:function(datas){
            this.each(function () {
                var $this = $(this);
                $this.select2("data",datas);
                return $this ;
            });
        }
    };

    $.fn.sysRemoteUserSingleSelect = function () {
        var method = arguments[0];
        if (methods[method]) {
            method = methods[method];
            arguments = Array.prototype.slice.call(arguments, 1);
        } else if (typeof(method) == 'object' || !method) {
            method = methods.init;
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.pluginName');
            return this;
        }
        return method.apply(this, arguments);
    }
})(jQuery);
/**
 * 用户相关操作js
 * @type {{}}
 */
var datadev_user_common = {};
datadev_user_common.listUserUrl = "/scriptcenter/common/user/list_user.ajax";
datadev_user_common.listUserByErpsUrl = "/scriptcenter/common/user/list_users_by_erps.ajax";
datadev_user_common._int = function(element, multiple){
    var erps = $(element).val();
    $(element).select2({
        // minimumInputLength : 1,
        multiple : multiple,
        cache: true,
        allowClear: true,
        placeholder : "请选择",
        separator : ",",
        ajax : {
            url : datadev_user_common.listUserUrl,
            dataType : 'json',
            method : 'post',
            cache : true,
            data : function(term) {
                return {
                    keyword : encodeURIComponent(encodeURIComponent(term.trim()))
                };
            },
            results : function(data) {
                return {
                    results : data.dataList
                };
            }
        },
        initSelection : function(element, callback) {

            if (erps != "") {
                $.ajax({
                    url :datadev_user_common.listUserByErpsUrl,
                    data : {
                        erps : erps
                    },
                    type : "post",
                    success : function(result) {
                        if (result.dataList) {
                            if(multiple) {
                                callback(result.dataList);
                            } else {
                                callback(result.dataList[0]);
                            }
                        }
                    },
                    dataType : 'json'
                });
            }
        },
        id : function(item) {
            return item.erp
        },
        formatResult : function(item) {
            return item.name + "(" + item.email + ")";
        },
        formatSelection : function(item) {
            return item.name + "(" + item.email + ")";
        },
        dropdownCssClass : "bigdrop",
        formatSearching : function() {
            return "加载中..."
        },
        formatNoMatches : function(term, data) {
            return "没有匹配结果."
        },
        escapeMarkup : function(m) {
            return m;
        }
    })

};

datadev_user_common.selectUpload = function (element, multiple) {
    $(element).select2({
        // minimumInputLength : 1,
        multiple : multiple,
        cache: true,
        allowClear: true,
        placeholder : "请选择上传人",
        ajax : {
            url : datadev_user_common.listUserUrl,
            dataType : 'json',
            method : 'post',
            cache : true,
            data : function(term) {
                return {
                    keyword : encodeURIComponent(encodeURIComponent(term.trim()))
                };
            },
            results : function(data) {
                return {
                    results : data.dataList
                };
            }
        },
        id : function(item) {
            return item.erp;
        },
        formatResult : function(item) {
            return item.erp + "(" + item.name + ")";
        },
        formatSelection : function(item) {
            return item.erp + "(" + item.name + ")";
        },
        dropdownCssClass : "bigdrop",
        formatSearching : function() {
            return "加载中..."
        },
        formatNoMatches : function(term, data) {
            return "没有匹配结果."
        },
        escapeMarkup : function(m) {
            return m;
        }
    })
}

var datadev_group_common = {};
datadev_group_common.listGroupUrl = "/scriptcenter/common/user/list_groups.ajax";
datadev_group_common.initGroup = function (element, multiple) {
    var groups = $(element).val();
    $(element).select2({
        multiple : multiple,
        cache: true,
        allowClear: true,
        placeholder : "请选择",
        ajax : {
            url : datadev_group_common.listGroupUrl,
            dataType : 'json',
            method : 'post',
            cache : true,
            data : function(term) {
                return {
                    keyWord : encodeURIComponent(encodeURIComponent(term.trim()))
                };
            },
            results : function(data) {
                $(element).data("groupNames",data);
                return {
                    results : data.dataList
                };
            }
        },

        id : function(item) {

            return item.gitGroupId;
        },
        formatResult : function(item) {
            return item.name ;
        },
        formatSelection : function(item) {

            return item.name ;
        },
        dropdownCssClass : "bigdrop",
        formatSearching : function() {
            return "加载中..."
        },
        formatNoMatches : function(term, data) {
            return "没有匹配结果."
        },
        escapeMarkup : function(m) {
            return m;
        }

    })

};
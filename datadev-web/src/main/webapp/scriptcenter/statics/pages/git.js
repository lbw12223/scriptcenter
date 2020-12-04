/**
 * 用户相关操作js
 * @type {{}}
 */
var datadev_git_common = {};
datadev_git_common.listGitsUrl = "/scriptcenter/project/list_gits.ajax";
datadev_git_common.listGitsByIds = "/scriptcenter/project/list_gits_by_ids.ajax";
datadev_git_common._int = function(element, multiple){
    var gits = $(element).val();
    $(element).select2({
        // minimumInputLength : 1,
        multiple : multiple,
        cache: true,
        allowClear: true,
        placeholder : "请选择",
        separator : ",",
        ajax : {
            url : datadev_git_common.listGitsUrl,
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

            if (gits != "") {
                $.ajax({
                    url :datadev_git_common.listGitsByIds,
                    data : {
                        gits : gits
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
            return item.value
        },
        formatResult : function(item) {
            return item.path;
        },
        formatSelection : function(item) {
            return item.path;
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
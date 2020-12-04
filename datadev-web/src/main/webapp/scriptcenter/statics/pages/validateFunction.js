/**
 * Created by wangqiaohui on 2017/11/23.
 */
/**
 * 控制输入参数的个数：
 *1. 输入参数是以逗号分隔的字符串。 2. param 是允许的最大对象个数
 * 调用：在相应的规则下加：sizeControl：param
 * param=[true,num]
 */
$.validator.addMethod(
    "sizeControl", //验证方法名称
    function (value, element, param) {//验证规则
        var values = value.split(",");

        // alert(values.length);
        return this.optional(element) || (values.length <= param[1]);
        // if (values.length > param[1]) {
        //     return false;
        // } else {
        //     return true;
        // }
    },
    '最多只能输入{1}个对象'//验证提示信息
);
$.validator.addMethod(
    "numRange", //验证方法名称
    function (value) {//验证规则
        return value * 1 >= 1 && value * 1 <= 2147483647;
    },
    '数字范围为1-2147483647'//验证提示信息
);
/**
 * 校验手机号格式校验
 */
$.validator.addMethod("isMobile", function (value, element) {
    var length = value.length;
    var mobile = /^1[3|4|5|7|8]\d{9}$/;
    return this.optional(element) || (length == 11 && mobile.test(value));
}, "请正确填写您的手机号码");
/**
 * 汉字校验
 */
$.validator.addMethod("chinese", function (value, element) {
    var chinese = /^[\u4E00-\u9FFF]+$/;
    return this.optional(element) || (chinese.test(value));
}, "格式不对");
/**
 * 字符串只能包含中文、英文字母、数字和_ 以及 -
 */
$.validator.addMethod("stringCheck", function (value, element) {
    var strRule = /^[a-zA-Z0-9_\u4e00-\u9fa5-]+$/;
    return this.optional(element) || strRule.test(value);
}, "只能包括中文字、英文字母、数字和_ 以及 -");




// // JavaScript Document
// $(function(){
//     //左侧菜单展开
//     $("body").delegate(".nav-tree dl dt","click",function() {
//         $(this).toggleClass('off');
//         $(this).siblings('dd').toggle();
//     });
//
//     //左侧菜单默认选中
//     var selectMenuNode = $("#"+selected_menu_id);
//     if(selectMenuNode && selectMenuNode.length > 0){
//         $(".leftMeun").removeClass("on");
//         selectMenuNode.addClass("on");
//     }
//
//
//     //右上角用户hover
//    $(".light-blue").hover(function () {
//        $(this).addClass("open");
//         $(this).find(".dropdown-menu:first").show();
//     }, function () {
//        $(this).removeClass("open");
//         $(this).find(".dropdown-menu:first").hide();
//     });
//
//
// });
//新手入门
var bdp_new_start;
$(document).ready(function () {
    try{
        var objNoLeft = $("body>.bdp-no-top-left-menu");
        if(objNoLeft.length>0){
            return;
        }
        //初始话新手入门
        var URL= '/helpCenter/api/newstart/data.ajax';
        bdp_new_start = $.bdp_new_start({current:1,url:URL});
        bdp_new_start.init();
    }catch (e) {
        console.debug(e)
    }
});

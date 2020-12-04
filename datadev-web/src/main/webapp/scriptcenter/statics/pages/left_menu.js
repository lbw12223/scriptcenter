/**
 * Created by wangqiaohui on 2017/11/22.
 */
mark = 0;

   $(document).ready(function () {
       var left_status = $.cookie("left_menu_status");
       if(left_status)
       {
           if(left_status == "0")
           {//说明  左侧菜单来是收缩的

               no_left();
           } else if(left_status == "1")
           {//说明  左侧菜单是展开的
               show_left();
           }
       }

       $("#showId").click(function () {
           if(mark == 0) {
               no_left();
           $.cookie("left_menu_status", 0, {expires: 30});
               mark = mark + 1;
           }else {
               show_left();
           $.cookie("left_menu_status", 1, {expires: 30});
               mark = 0;
           }
       });
   });

   function no_left (){
       $(".nav-tree").addClass("nav-tree-width");
       $(".icon-off").addClass("icon-off-width");
       $(".scroll-wrapper.nav-scoll.scrollbar-inner").addClass(" nav-tree-width");
       $(".nav-scoll.scrollbar-inner.scroll-content").addClass("nav-tree-width");
       $(".nav-scoll.scrollbar-inner.scroll-content").removeClass("scroll-scrolly_visible");
       $(".scroll-element.scroll-x").removeClass("scroll-scrolly_visible");
       $(".scroll-element.scroll-y").removeClass("scroll-scrolly_visible");
       $(".tree-layout").css("display","none");
       $(".tree-height").css("display","none");

       $(".main-container ").addClass("bdp-nav-left-flag");
       $(".main-content ").addClass("margin-left-60");
   }

   function show_left() {
       $(".nav-tree").removeClass("nav-tree-width");
       $(".icon-off").removeClass("icon-off-width");
       $(".scroll-wrapper.nav-scoll.scrollbar-inner").removeClass(" nav-tree-width");
       $(".nav-scoll.scrollbar-inner.scroll-content").removeClass("nav-tree-width");
       $(".nav-scoll.scrollbar-inner.scroll-content").addClass("scroll-scrolly_visible");
       $(".scroll-element.scroll-x").addClass("scroll-scrolly_visible");
       $(".scroll-element.scroll-y").addClass("scroll-scrolly_visible");
       $(".tree-layout").css("display","block");
       $(".tree-height").css("display","block");

       $(".main-container ").removeClass("bdp-nav-left-flag");
       $(".main-content ").removeClass("margin-left-60");
   }

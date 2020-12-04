// JavaScript Document
$(function(){
    //导航下拉框
    //三个模块切换
    //var nanHover = $(".bdp-index .nav-layout");
    //nanHover.hover(function(){
    //    nanHover.removeClass("nav-on");
    //    $(this).addClass("nav-on");
    //    $(".nav-img").hide();
    //    $(".nav-img").eq(nanHover.index(this)).show().c;
    //});
    var nanHover = $(".bdp-index .nav-layout");
    nanHover.hover(function(){
        nanHover.removeClass("nav-on");
        $(this).addClass("nav-on");
        //$(".nav-img").hide();
        //$(".nav-img").eq(nanHover.index(this)).show();
        //$(".nav-img2").eq(0).show();
    });

    //实验室
    var nanHover2 = $(".bdp-index .nav-layout2");
    nanHover2.hover(function(){
        nanHover2.removeClass("nav-on");
        $(this).addClass("nav-on");
        $(".nav-img2").hide();
        $(".nav-img2").eq(nanHover2.index(this)).show();
        // $(".nav-img").eq(0).show();
    });
    //产品下拉
    var product = $('.bdp-head .index-menu dd');
    // productHover = $('.bdp-head .nav-hover'),
    //productLink = $('.bdp-head .product a.name');
    product.hover(function(){
        $(this).children('.nav-hover').show();
        $(this).children('a.name').addClass('on');
        var maxheight = 0;
        $(".nav-hover .nav-layout").each(function(){
            if (!($(this).hasClass("nav-layout2"))){
                var height = $($(this).find(".list")[0]).outerHeight() + 130;
                if(height > maxheight){
                    maxheight = height;
                }
            }

        });
        $(".nav-hover .nav-layout").each(function(){
            if (!($(this).hasClass("nav-layout2"))){
                $(this).css("height", maxheight + "px")
            }

        });
        //实验室边框
        $(".bdp-index .nav-layout2").css("height","287px");

    },function(){
        $(this).children('.nav-hover').hide();
        $(this).children('a.name').removeClass('on');
    });

    //导航个人信息下拉
    var navMsg = $('.bdp-index .bdp-head .right .nav > li');
    navMsg.hover(function(){
        $(this).children('a.dropdown-toggle').addClass('on');
        $(this).children('ul.dropdown-menu').show();
    },function(){
        $(this).children('a.dropdown-toggle').removeClass('on');
        $(this).children('ul.dropdown-menu').hide();
    });
    //我们的产品鼠标滑过显示
    var indexTab = $('.index-goods .nav-tabs > li > a'),
        indexContent = $('.index-goods .tab-content>.tab-pane');
    indexTab.hover(function(){
        indexTab.parent('li').removeClass('active');
        $(this).parent('li').addClass('active');
        indexContent.hide();
        indexContent.eq(indexTab.index(this)).show();
    });


    /**
     * 我的产品tag浮动js
     */
    $(".bdp-index-tab").hover(function() {
        $(".bdp-index-tab").removeClass("bdp-index-tab-active");
        $(this).addClass("bdp-index-tab-active");
        var id = $(this).data("id");
        $(".bdp-tab-pane").removeClass("bdp-index-tab-pane-active");
        $("#"+id).addClass("bdp-index-tab-pane-active");
    });

    //首页轮播 查看详情按钮
    $(".bdp-index-play .carousel-inner .bdp-text .more").hover(function(){
        $(this).find("a").toggleClass("bdp-color-block")
    } );
    //我的产品 查看详情按钮
     $(".bdp-tag-action").hover(function(){
        $(this).find("a").toggleClass("bdp-color-block")
    },function() {
         $(this).find("a").toggleClass("bdp-color-block")
     });
    /**
     * 大数据专家认证
     */
    $(".bdp-index-auth-content-div").hover(function() {
        $(this).toggleClass("bdp-index-auth-active",1000);
    },function(){
        $(this).toggleClass("bdp-index-auth-active",1000);
    })
    /**
     * 实时中心
     */
    $(".bdp-sszx-content").hover(function() {
        $(this).toggleClass("bdp-sszx-content-active",1000);
    },function(){
        $(this).toggleClass("bdp-sszx-content-active",1000);
    })

    //我们的产品悬浮
    var product = $('.bdp-tab-pane li');
    product.hover(function(){
        $(this).children('.bdp-tag-div').addClass("bdp-hidden");
        $(this).children('.bdp-tag-div-info').removeClass("bdp-hidden");
    },function(){

        $(this).children('.bdp-tag-div').removeClass("bdp-hidden");
        $(this).children('.bdp-tag-div-info').addClass("bdp-hidden");
    });

    /**
     * 合作案例
     * 图标切换效果
     */
    $('.bdp-index-banner-tab .bdp-banner-tab-li').click(function () {
        $(this).addClass("active");
        $(this).siblings().removeClass("active");
        var dataIndex = $(this).attr("data-index");
        if(dataIndex == '1') {
            $('.bdp-index-case .bdp-index-case-ul-1').css('display', 'block');
            $('.bdp-index-case .bdp-index-case-ul').css('display', 'none');
        } else if(dataIndex == '2'){
            $('.bdp-index-case .bdp-index-case-ul-1').css('display', 'none');
            $('.bdp-index-case .bdp-index-case-ul').css('display', 'block');
        }
    });


    /**
     * 右侧悬浮区
     */
    //展示二维码
    $('.bdp-index-show-big').hover(function () {
        $($(this).find('.bdp-index-rightbar-big')[0]).toggleClass("bdp-hidden");
    });

    //置顶
    $('.bdp-index-to-top').click(function () {
        $("body").scrollTop(0);
    });

});
$(document).ready(function () {


    //标注平台连接
    $("#homePage").click(function () {
        window.location.reload(true);
    });
    $("body").delegate(".bdp-new-help-button-a-new-start", "click", function () {
        $.removeCookie("bdp_new_start_src", {domain: ".bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_right", {domain: ".bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_show", {domain: ".bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_step", {domain: ".bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_word", {domain: ".bdp.jd.com", path: '/', expires: -1});

        $.removeCookie("bdp_new_start_src", {domain: ".test.bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_right", {domain: ".test.bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_show", {domain: ".test.bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_step", {domain: ".test.bdp.jd.com", path: '/', expires: -1});
        $.removeCookie("bdp_new_start_word", {domain: ".test.bdp.jd.com", path: '/', expires: -1});
    });

    initExperienceDataDev();

});


//初始化头部体验中心信息
function initExperienceDataDev() {
    try {
        var erp=$.cookie("_c_k_u_ex");
        var code = $.cookie("experienceCode");
        if(erp){
            var url = window.location.pathname;
            var html = "<div class='bdp-experience'>" +
                "<span class='name' style='position: relative;top: -8px;color: white'>您当前在体验环境中，如需返回正式环境，请点击" +
                '<a class="return" href="'+_bdpDomain+'/experience/switch.html?experienceCode=2&returnUrl=' + url + '">切换到正式环境</a></span>' +
                "</div>";
            $(".bdp-product").append(html);
        }else if(code *1==2){
            $.successMsg("您已经进入正式环境，开工喽！");
            $.removeCookie("experienceCode", {domain: ".bdp.jd.com", path: '/', expires: -1});
        }
    } catch (e) {
        console.debug(e)
    }
}





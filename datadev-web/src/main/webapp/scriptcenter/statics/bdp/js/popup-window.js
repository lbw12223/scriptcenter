var timer_request_for_announcementList;
var announcementType;
var popuped;
var announcementList;
var current_index;
var href;
var announcementDetailUrl = _bdpDomain+'/urm/announcement/detail.html';
var checkIsPopupWindowURL = _bdpDomain+'/urm/announcement/admin/checkPopupWindiw.ajax';
var getPopupAnnouncementListURL = _bdpDomain+'/urm/announcement/admin/getPopupAnnouncementList.ajax';

function popRightWindow(){
    //设定窗口的位置
    $(".bdp-popup-window").css("position","fixed").css("bottom","30px").css("right","30px").slideToggle("slow");
}

$(document).ready(function () {

    var thisHREF = document.location.href;
    href = thisHREF.split( "//" );
    var temp = href[1].split("/");
    href = href[1].replace(temp[0],"");
    checkIsPopupWindow(href);

    $(".popup-closed img").click(function(){
        $(this).parent().parent().parent().hide("slow");
        popuped = false;
        timer_request_for_announcementList =setInterval("init_announcement_list()", 1000*60*3);
        announcementList =[];
    });
    $(".popup-more").click(function(){
        window.open(announcementDetailUrl+"?id="+announcementList[current_index].id);
    })

});


function checkIsPopupWindow(href){
    $.ajax({
        url: checkIsPopupWindowURL,
        dataType: "json",
        type: "post",
        timeout:3000,
        data: {
            url: href
        },
        success:function(res) {
            if (res.announcementType!= null && res.announcementType!="") {
                announcementType = res.announcementType;
                popuped = false;
            } else{
                announcementType = "";
                popuped = true;
            }
            try{
                if(popuped == false ){
                    init_announcement_list();
                    //启动定时器
                    timer_request_for_announcementList =setInterval("init_announcement_list()", 1000*60*3);
                }
            }catch(err){
                console.error("起定时异常!!")
            }
        },
        error:function(){
            console.error("请求超时等，不启动定时器!");
        }
    })
}

next_clickable=function(){
    if(current_index==0){
        $(".previous-unclickable").replaceWith("<a href ='#' class='previous-clickable' onclick='previous_clickable()'></a>");

    }if(current_index +1 == announcementList.length-1){
        $(".next-clickable").replaceWith("<div class='next-unclickable'></div>");
    }
    current_index= current_index +1;
    var title =announcementList[current_index].title;
    if(title.length>16){
        title =title.substring(0,16)+"..." ;
    }
    $(".popup-window-title span").replaceWith("<span class='span'>" +title+"</span>");
    var content;
    content  = announcementList[current_index].content.replace(/<(?!br).*?>/g,"");
    content  = content.replace(/<(br).*?>/g,"<br/>");
    if(content.length>60){
        content = content.substring(0,60)+"...";
    }
    $(".popup-window-content span").replaceWith("<span class='span'>"+content+"</span>");
    
    //add created time by gaolch start
    //var _created = announcementList[current_index].created;
    // var created = new Date(_created)
    var effectiveDate = announcementList[current_index].effectiveDate;
    $(".popup-window-created span").replaceWith("<span class='span'>"+effectiveDate+"</span>");
    //add created time by gaolch end
};

previous_clickable = function(){
    if(current_index-1 ==0){
        $(".previous-clickable").replaceWith("<div class='previous-unclickable'></div>");
    }
    if(current_index == announcementList.length-1){
        $(".next-unclickable").replaceWith("<a href='#' class='next-clickable' onclick='next_clickable()'></a>");
    }
    current_index = current_index-1;
    var title = announcementList[current_index].title;
    if(title.length>16){
        title =title.substring(0,16)+"..." ;
    }
    $(".popup-window-title span").replaceWith("<span class='span'>" +title+"</span>");
    var content;
    content  = announcementList[current_index].content.replace(/<(?!br).*?>/g,"");
    content  = content.replace(/<(br).*?>/g,"<br/>");
    if(content.length>60){
        content = content.substring(0,60)+"...";
    }
    $(".popup-window-content span").replaceWith("<span class='span'>"+content+"</span>");
    
    //add created time by gaolch start
    //var _created = announcementList[current_index].created;
    // var created = new Date(_created)
    var effectiveDate = announcementList[current_index].effectiveDate;
    $(".popup-window-created span").replaceWith("<span class='span'>"+effectiveDate+"</span>");
    //add created time by gaolch end
};

init_announcement_list = function(){

    $.ajax({
        url: getPopupAnnouncementListURL,
        dataType: "json",
        type: "post",
        data: {
            announcementType: announcementType
        },
        success: function (res){
            if(res !=null && res.length > 0){
                $(".popup-page-check").replaceWith("<div class='popup-page-check'><div class='previous-unclickable'></div><div class='next-clickable' onclick='next_clickable()'></div></div>");
                popuped = true;
                announcementList =res;
                //弹出弹窗及页面数据组装
                //判断当前的 popuped 是否为false-弹窗为关闭状态  true-弹窗为打开状态
                if(popuped==true){
                    //清掉定时器
                    current_index=0;
                    if(res.length <=1){
                        $(".previous-unclickable").remove();
                        $(".next-clickable").remove();
                    }
                    var title=announcementList[current_index].title;
                    if(title.length>16){
                        title =title.substring(0,16)+"..." ;
                    }
                    $(".popup-window-title span").replaceWith("<span class='span'>" +title+"</span>");
                    var content;
                    content  = announcementList[current_index].content.replace(/<(?!br).*?>/g,"");
                    content  = content.replace(/<(br).*?>/g,"<br/>");
                    if(content.length>60){
                        content =content.substring(0,60)+"...";
                    }
                    $(".popup-window-content span").replaceWith("<span class='span'>"+content+"</span>");
                    
                    //add created time by gaolch start
                    //var _created = announcementList[current_index].created;
                   // var created = new Date(_created)
                    var effectiveDate = announcementList[current_index].effectiveDate;
                    $(".popup-window-created span").replaceWith("<span class='span'>"+effectiveDate+"</span>");
                    //add created time by gaolch end
                    popRightWindow();
                    clearInterval(timer_request_for_announcementList);
                }
            }else{
                if(popuped==true){
                    //清掉定时器
                    clearInterval(timer_request_for_announcementList);
                }
            }
        }
    })
};

//格式化日期
/**
Date.prototype.Format = function (fmt) {
  var o = {
    "y+": this.getFullYear(),
    "M+": this.getMonth() + 1,                 //月份
    "d+": this.getDate(),                    //日
    "h+": this.getHours(),                   //小时
    "m+": this.getMinutes(),                 //分
    "s+": this.getSeconds(),                 //秒
    "q+": Math.floor((this.getMonth() + 3) / 3), //季度
    "S+": this.getMilliseconds()             //毫秒
  };
  for (var k in o) {
    if (new RegExp("(" + k + ")").test(fmt)){
      if(k == "y+"){
        fmt = fmt.replace(RegExp.$1, ("" + o[k]).substr(4 - RegExp.$1.length));
      }
      else if(k=="S+"){
        var lens = RegExp.$1.length;
        lens = lens==1?3:lens;
        fmt = fmt.replace(RegExp.$1, ("00" + o[k]).substr(("" + o[k]).length - 1,lens));
      }
      else{
        fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
      }
    }
  }
  return fmt;
}**/




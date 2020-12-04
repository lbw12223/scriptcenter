
if (!("bdpLoad" in window)) {
    window.bdpLoad = {}
}
bdpLoad.load = function(a,msg,color) {
    if(color == null || color == '') {
        color = 'red';
    }
    a("<div class=\"bdp-load-mask\"></div>").css({ display: "block", width: "100%", height: a(window).height() }).appendTo("body");
    a("<div class=\"bdp-load-mask-msg\"></div>").html('<div style="text-align: center;"><i class="icon-spinner icon-spin icon-2x" style="color: '+color+'"></i></div><div style="text-align: center;"><span class="bdp-load-msg">'+msg+'</span></div>').appendTo("body").css({ display: "block", left: (a(document.body).outerWidth(true) - 20) / 2, top: (a(window).height() - 45) * 0.38 });
}
bdpLoad.loadP = function(a,p,msg,color) {
    if(color == null || color == '') {
        color = 'red';
    }
    a("<div class=\"bdp-load-mask\"></div>").css({ display: "block", width: "100%", height: a(p).height() }).appendTo(a(p));
    a("<div class=\"bdp-load-mask-msg\"></div>").html('<div style="text-align: center;"><i class="icon-spinner icon-spin icon-2x" style="color: '+color+'"></i></div><div style="text-align: center;"><span class="bdp-load-msg">'+msg+'</span></div>').appendTo(a(p)).css({ display: "block", left: (a(p).width()) / 2, top: (a(p).height() - 45) * 0.38 });
}

bdpLoad.removeLoad = function(a) {
    a(".bdp-load-mask").remove();
    a(".bdp-load-mask-msg").remove();
};
bdpLoad.removeLoadP = function(a,p) {
    a(p).find(".bdp-load-mask").remove();
    a(p).find(".bdp-load-mask-msg").remove();
}

var FloatFrame = (function() {
    "use strict";
    var elem,
        hideHandler,
        that = {};

    that.init = function(options) {
        elem = $(options.selector);
    };

    that.show = function(text) {
        clearTimeout(hideHandler);

        elem.find("span").html(text);
        elem.delay(200).fadeIn().delay(4000).fadeOut();
    };

    return that;
}());
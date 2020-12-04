var datadev_process = {};
datadev_process.init = function (element, init) {
    var initPer = (typeof init == "number" ? (init <= 100 ? init : 100) : 0);
    var right = initPer > 50 ? "0px" : "-40px";
    initPer += "%";
    var processHtml = '<div class="progress">' +
        '<div class="progress-bar progress-bar-success progress-bar-striped active" style="' + initPer + '">' +
        '<div class="progress-value" style="right:'+right+'">' + initPer + '</div>' +
        '</div>' +
        '</div>'
    $(element).append(processHtml);
}
datadev_process.process = function (element, per) {
    per=parseFloat(per);
    var initPer = (per ? (per <= 100 ? per : 100) : 0) ;
    var right = initPer > 50 ? "0px" : "-40px";
    initPer+="%";
    $(".progress-bar", $(element)).css({"width": initPer});
    $(".progress-value", $(element)).text(initPer).css({right:right});
}
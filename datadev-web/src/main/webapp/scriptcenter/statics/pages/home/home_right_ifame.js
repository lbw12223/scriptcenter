

(function (flag, VariableName) {
    if (typeof (window[VariableName]) != "undefined") return;
    if (flag) return;
    var top_window = window.top;
    var top_doc = top_window.document;
    var NameSpace = ".MouseHelper";
    var MoveEvent = "mousemove" + NameSpace;

    function MouseHelper() {
        var self = this;

        self.mouseX = 0;
        self.mouseY = 0;
        self.browerX = 0;
        self.browerY = 0;

        $(top_doc).unbind(MoveEvent).bind(MoveEvent, function (e) {
            self.mouseX = e.clientX;
            self.mouseY = e.clientY;
            self.browerX = e.screenX - e.clientX;
            self.browerY = e.screenY - e.clientY;
        });
    }


    var info;
    if (typeof (top_window[VariableName]) == "undefined") info = new MouseHelper();
    else info = top_window[VariableName]

    try {
        if (frameElement != null) {
            $(document).unbind(MoveEvent).bind(MoveEvent, function (e) {
                info.mouseX = e.screenX - info.browerX;
                info.mouseY = e.screenY - info.browerY;
            });
        }
    } catch (e) {

    }
    window[VariableName] = info;
}(typeof (jQuery) == "undefined", "MouseInfo"))

$(document).mouseup(function (event) {
    if($(event.target).closest("#data-content").length>0){
        return true;
    }
     var obj ;
    if(window.parent && window.parent.window){
        obj = window.parent ;
    }
    if(window.parent.parent && window.parent.parent.window){
        obj = window.parent.parent ;
    }
    if (obj) {
        obj.window.START_MOVE_SPLITHANDER = false;
    }
    if(window.parent.homeOpenPageEvent){
        window.parent.homeOpenPageEvent.initPageSizeMouseUp(event);
    }

})
$(document).mousemove(function (event) {
    if($(event.target).closest("#data-content").length>0){
        return true;
    }
    var obj ;
    if(window.parent && window.parent.window){
        obj = window.parent ;
    }
    if(window.parent.parent && window.parent.parent.window){
        obj = window.parent.parent ;
    }
    var temp = {};
    if (obj.window.getRightPosition) {
        var position = obj.window.getRightPosition();

        temp.pageX = event.pageX + position.left;
        temp.pageY = MouseInfo.mouseY + position.top;
        obj.window.mouseMoveChangeSize(temp);
    }
    if(window.parent.homeOpenPageEvent){
        window.parent.homeOpenPageEvent.initPageSizeMouseMove(temp);
    }

});
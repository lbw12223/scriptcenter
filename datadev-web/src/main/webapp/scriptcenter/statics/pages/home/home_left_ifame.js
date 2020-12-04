$(document).mouseup(function (event) {
    window.parent.window.START_MOVE_SPLITHANDER = false ;
})
$(document).mousemove(function (event) {
    var position =  window.parent.window.getLeftPosition();
/*    event.pageX = event.pageX + position.left;
    event.pageY = event.pageY + position.top;*/
    window.parent.window.mouseMoveChangeSize(event);
});

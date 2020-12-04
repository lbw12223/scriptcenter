/*给Top调用，添加APP自定义HTML*/
function isUserAppendTopHtml() {
    return $("#parentProjectSpace").html();
}

$(document).on("mousemove", "#projectSpace", function () {
    $("#select-down-container").show();
})
$(document).ready(function () {


    $(document).on("mousemove", "#projectSpace", function () {
        $("#select-down-container").show();
    })

    $(document).on("mouseleave", "#projectSpace", function () {
        $("#select-down-container").hide();
    })

    $(document).on("click", ".select-down-ul > li", function () {

        $("#select-down-container").hide();
        $("#selectBack").hide();
        var node = $(this);
        var dataId = node.attr("data-id") * 1;
        var dataName = node.html();
        $("#projectSpacenName").html(dataName).attr("data-projectSpaceId", dataId);
        $.cookie("P_S_I_D", dataId, {expires: 300, path: "/"});
        window.location.reload();
    })

})

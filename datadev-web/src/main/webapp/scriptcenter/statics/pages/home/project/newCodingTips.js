$(function () {
    //提示modal
    $("#createTopCodingBtn").click(function () {
        var projectArt=$.dialog.open("/scriptcenter/project/newCodingProject.html", {
            title: "新项目",
            lock: true,
            width: "600px",
            height: "400px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("projectArt",projectArt);
    })

    $("#getAvailCoding").click(function () {
        var useProjectArt=$.dialog.open("/scriptcenter/project/useCodingProject.html", {
            title: "使用现有coding项目",
            lock: true,
            width: "600px",
            height: "300px",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("useProjectArt",useProjectArt);

    });


})

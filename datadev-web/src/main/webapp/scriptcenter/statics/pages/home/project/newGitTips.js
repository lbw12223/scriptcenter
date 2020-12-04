$(function () {
    //提示modal
    $("#createTopGitBtn").click(function () {
        var projectArt=$.dialog.open("/scriptcenter/project/newGitProject.html", {
            title: "新项目",
            lock: true,
            width: "600",
            height: "400",
            opacity: 0.5,
            esc: false,
            close: function () {
            }
        });
        $.dialog.data("projectArt",projectArt);
    })

    $("#getAvailGit").click(function () {
        // window.open("http://git.jd.com",'_blank');
        var useProjectArt=$.dialog.open("/scriptcenter/project/useGitProject.html", {
            title: "使用现有git项目",
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

/*
*
*
* 顶部按钮的事件
* save , save as , format, config, run,debug , dependency,stop,git , upline ,document
* */

var HOME_OPEN_IDE_TOP = {


    events: {
        save: function () {
            var params = HOME_OPEN_IDE_TOP.methods.getHomeOpenIdeParams()


            var url = "/scriptcenter/script/home_open_ide_save.html?gitProjectId=" + params.gitProjectId + "&scriptType=" + params.type;
            var saveArt = $.dialog.open(url, {
                title: "保存文件",
                lock: true,
                width: "578px",
                height: "500px",
                opacity: 0.5,
                esc: false,
                close: function () {
                }
            });

        },

        saveAs: function () {
            var params = HOME_OPEN_IDE_TOP.methods.getHomeOpenIdeParams()
            var url = "/scriptcenter/script/home_open_ide_save_as.html?gitProjectId=" + params.gitProjectId;
            var saveArt = $.dialog.open(url, {
                title: "保存文件",
                lock: true,
                width: "578px",
                height: "500px",
                opacity: 0.5,
                esc: false,
                resize: false,
                close: function () {
                }
            });

        },
        format: function () {
        },
        run: function () {
        },
        debug: function () {
        },
        dependency: function () {
        },
        stop: function () {
        },
        git: function () {
        },
        upline: function () {
        },
        document: function () {
        }

    },
    methods: {

        getHomeOpenIdeParams: function () {
            return {
                type: $("#scriptType").val(),
                isShow: $("#isShow").val() * 1,
                code: $("#code"),
                editor: code && code.data("editor"),
                key: "",
                isTemplate: $("#templateId").val() && $("#templateId").val() > 0,
                isShow: $("#isShow").val() * 1,
                gitProjectId: $("#gitProjectId").val(),
                type: $("#scriptType").val()

            }
        }
    },


}
$(function () {
    $("#innerSaveas").click(function () {
        HOME_OPEN_IDE_TOP.events.saveAs();
    })
    $("#innerSave").click(function () {
        HOME_OPEN_IDE_TOP.events.save();
    })
})

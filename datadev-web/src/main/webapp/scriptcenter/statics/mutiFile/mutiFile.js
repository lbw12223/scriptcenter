(function ($) {

    var MutiFile = function MutiFile(element, options) {
        this.$element = element;
        this.defaults = {
            sizeLimit: 1000, /*单个文件大小限制M*/
            threadLimit: 3, /*同时能够上传的文件个数*/
            showLimit: 4, /*列表最多显示的文件个数*/
            fileTypeFilter: "all", /*过滤文件类型*/
        };
        this.$tbody = undefined;
        this.options = $.extend({}, this.defaults, options);
        this.options.fileTypeFilter = this.options.fileTypeFilter.toLocaleLowerCase()
        this.files = this.options.files;
        this.isInRunMethod = false;
        this.countTotal = 0;
        this.successItems = [];
    }
    MutiFile.NEED_UP = 1;
    MutiFile.UP_ING = 2;
    MutiFile.UP_CANCEL = 3;
    MutiFile.UP_FINISH_FAILED = 4;
    MutiFile.UP_FINISH_SUCCESS = 5;

    MutiFile.prototype = {
        initMutiFile: function () {
            if (this.files && this.files.length > 0) {
                $(".fileItem", this.$element).remove();
                this.$tbody = $("tbody", this.$element);

                this.notifyTotalUi();
                var tempFiles = [];
                this.countTotal = this.files.length;
                for (var index = 0; index < this.files.length; index++) {
                    var temp = {};
                    var indeFile = this.files[index];
                    temp.fileData = this.files[index];
                    temp.id = "_upfile_id_" + index;
                    temp.fileStatus = MutiFile.NEED_UP;
                    temp.name = indeFile.name;
                    temp.size = indeFile.size;
                    temp.webkitRelativePath = indeFile.webkitRelativePath;
                    temp.fileType = this.getType(indeFile);
                    temp.icon = this.getIcon(temp);
                    temp.fileIndex = index;
                    tempFiles.push(temp);
                    if (index < this.options.showLimit) {
                        this.notifyItemUi(temp);
                    }
                }
                this.files = tempFiles;

                this.bindOptEvent();
                this.run();
            }
        },
        /**
         * 在操作上绑定事件
         */
        bindOptEvent: function () {
            var _this = this;
            this.$element.on("click", ".restart", function (event, args) {
                //找到对应的File对象，然后设置状态为初始化，失败才有重新上传
                var fileIndex = $(this).parent().attr("data-index") * 1;
                var file = _this.files[fileIndex];
                _this.reStartUp(file);

            })
            /*替换*/
            this.$element.on("click", ".recover", function (event, args) {
                var fileIndex = $(this).parent().attr("data-index") * 1;
                var file = _this.files[fileIndex];
                file.userParams = {
                    fileUpSamePathOpt: 1
                }
                _this.reStartUp(file);
            })
            /*保留新添加一个文件*/
            this.$element.on("click", ".newFile", function (event, args) {
                var fileIndex = $(this).parent().attr("data-index") * 1;
                var file = _this.files[fileIndex];
                file.userParams = {
                    fileUpSamePathOpt: 2
                }
                _this.reStartUp(file);
            })
            this.$element.on("click", ".cancel", function (event, args) {
                var fileIndex = $(this).parent().attr("data-index") * 1;
                var file = _this.files[fileIndex];
                _this.canceUp(file);
            })

        },
        canceUp: function (file) {
            //上传中取消，那么停止上传
            // this.countTotal -= 1;
            if (file.fileStatus == MutiFile.UP_ING) {
                if (file.xhr) {
                    file.xhr.abort();
                }
            }
            file.fileStatus = MutiFile.UP_CANCEL;
            this.notifyItemUi(file);
            /* if (file.ui) {
             file.ui.remove();
             file.ui = undefined;
             }*/

        },
        reStartUp: function (file) {
            file.fileStatus = MutiFile.NEED_UP;
            this.notifyItemUi(file);
            this.run();
        },

        getUiItem: function (file) {
            var uiItem = $("#" + file.id, this.$element);
            if (uiItem.length < 1) {
                uiItem = $("#processUi_______________").clone();
                uiItem.attr("id", file.id).addClass("fileItem").attr("style", "")
            }
            return uiItem;
        },
        getFileSize: function (file) {
            var size = file.size;
            var MB = 1024 * 1024;
            if (size > MB) {
                return (size / MB).toFixed(2) + " MB";
            }
            return (size / 1024).toFixed(2) + " KB";
        },
        getType: function (file) {
            var typeStr = "";
            if (file.name.lastIndexOf(".") != -1) {
                typeStr = file.name.substr(file.name.lastIndexOf("."));
            }
            return typeStr.toLocaleLowerCase();
        },
        getIcon: function (file) {
            if (file.fileType === ".sql") {
                return "bdp-icon ide-left-sql";
            }
            if (file.fileType === ".py") {
                return "bdp-icon ide-left-python";
            }
            if (file.fileType === ".java") {
                return "bdp-icon ide-java";
            }
            if (file.fileType === ".xml") {
                return "bdp-icon ide-xml";
            }
            if (file.fileType === ".html") {
                return "bdp-icon ide-html";
            }
            if (file.fileType === ".htl") {
                return "bdp-icon ide-java";
            }
            if (file.fileType === ".zip") {
                return "bdp-icon ide-left-zip";
            }
            if (file.fileType === ".rar") {
                return "bdp-icon ide-RAR";
            }
            if (file.fileType === ".csv") {
                return "bdp-icon ide-csv";
            }
            if (file.fileType === ".txt") {
                return "bdp-icon ide-txt";
            }
            if (file.fileType === ".properties") {
                return "bdp-icon ide-prop";
            }
            if (file.fileType === ".jar") {
                return "bdp-icon ide-left-jar";
            }
            return "bdp-icon  ide-left-hs";
        },
        notifyTotalUi: function () {
            this.options.fileItemUpFinishCallBack && this.options.fileItemUpFinishCallBack(this);
        },
        notifyItemUi: function (file) {

            if (file.fileStatus === MutiFile.NEED_UP) {
                if (!file.ui) {
                    var uiItem = this.getUiItem(file);
                    file.ui = uiItem;
                    this.$tbody.append(file.ui);
                }
                $(".itemInFinishOrBeforUp", file.ui).removeClass("itemError").html("待上传").css("display", "block").removeClass("itemSuccess");
                $(".itemHeaderFileName", file.ui).html(file.name).attr("title", $.trim(file.webkitRelativePath).length > 0 ? file.webkitRelativePath : file.name);
                $(".itemSize", file.ui).html(this.getFileSize(file));
                $(".itemSpeed", file.ui).html("");
                $(".itemHeaderIcon span", file.ui).addClass(file.icon);
                $(".itemOpt", file.ui).attr("data-index", file.fileIndex);
                $(".itemOpt > span", file.ui).css("display", "none");
                $(".itemOpt > .cancel", file.ui).css("display", "inline-block");
            }
            /**
             * 1.上传失败. 可以有重新上传
             * 2.修改上传的背景颜色
             *
             * 3.如果成功的颜色大于showLimit那么进行Remove
             */
            if (file.fileStatus === MutiFile.UP_FINISH_FAILED) {
                file.pro = 0;
                $(".itemProcessContainer > div", file.ui).addClass("process").css("width", file.pro + "%");
                $(".itemInFinishOrBeforUp", file.ui).addClass("itemError").html(file.resultData && file.resultData.message ? file.resultData.message : "上传失败").css("display", "block").removeClass("itemSuccess");
                $(".itemInProcess", file.ui).css("display", "none");
                $(".itemOpt > span", file.ui).css("display", "none");

                if (file.resultData) {
                    if (file.resultData.code == 2) {
                        //失败 文件重复
                        $(".itemOpt > .recover", file.ui).css("display", "inline-block");
                        $(".itemOpt > .newFile", file.ui).css("display", "inline-block");
                        $(".itemOpt > .cancel", file.ui).css("display", "inline-block");
                    } else if (file.resultData.code == 3) {
                        // $(".itemOpt > .cancel", file.ui).css("display", "inline-block");
                    } else {
                        //其他错误重新上传
                        $(".itemOpt > .restart", file.ui).css("display", "inline-block");
                        //$(".itemOpt > .cancel", file.ui).css("display", "inline-block");
                    }
                } else {
                    $(".itemOpt > .restart", file.ui).css("display", "inline-block");
                }
            }
            if (file.fileStatus === MutiFile.UP_FINISH_SUCCESS) {
                this.successItems.push(file);
                $(".itemInFinishOrBeforUp", file.ui).addClass("itemSuccess").html(file.resultData && file.resultData.message ? file.resultData.message : "上传成功").css("display", "block").removeClass("itemError");
                $(".itemInProcess", file.ui).css("display", "none");

                $(".itemOpt > span", file.ui).css("display", "none");
                if (this.successItems.length > this.options.showLimit) {
                    for (var index = 0; index < this.successItems.length; index++) {
                        var temp = this.successItems[index];
                        if (temp.ui) {
                            temp.ui.remove();
                            temp.ui = undefined;
                            break;
                        }
                    }
                }

            }
            if (file.fileStatus === MutiFile.UP_ING) {
                $(".itemInFinishOrBeforUp", file.ui).addClass("itemSuccess").html("待上传").css("display", "none");
                $(".itemInProcess", file.ui).css("display", "block");
                $(".itemProcessContainer > div", file.ui).addClass("process").css("width", file.pro + "%");
                //计算速度(total / time)
                $(".itemSpeed", file.ui).html((file.pro ? file.pro : 0 ) + "%" + "(" + (file.speed ? file.speed : 0) + ")");

            }
            if (file.fileStatus === MutiFile.UP_CANCEL) {
                $(".itemInFinishOrBeforUp", file.ui).removeClass("itemError").html("已取消").css("display", "block").removeClass("itemSuccess");
                $(".itemOpt", file.ui).attr("data-index", file.fileIndex);
                $(".itemOpt > span", file.ui).css("display", "none");
                $(".itemOpt > .restart", file.ui).css("display", "inline-block");
            }
            this.notifyTotalUi();
        },
        /**
         * 开始上传
         * 1.取出还未上传的文件，然后调用上传的方法.
         * 2.每次获取threadLimit个文件做上传
         */
        run: function () {
            if (this.isInRunMethod) {
                return;
            }
            this.isInRunMethod = true;
            var activeCount = 0;
            for (var index = 0; index < this.files.length; index++) {
                var file = this.files[index];
                if (activeCount >= this.options.threadLimit) {
                    break;
                }
                if (file.fileStatus == MutiFile.NEED_UP) {
                    if (this.check(file)) {
                        this.startUp(file);
                        activeCount++;
                        continue;
                    }
                }
                if (file.fileStatus == MutiFile.UP_ING) {
                    activeCount++;
                    continue;
                }
            }
            this.isInRunMethod = false;
        },

        /**
         * 检查大小，类型是否验证通过
         * @returns {boolean}
         */
        check: function (file) {
            if (file.size > this.options.sizeLimit * 1024 * 1024) {
                file.fileStatus = MutiFile.UP_FINISH_FAILED;
                file.resultData = {
                    message: "文件大小限制为" + this.options.sizeLimit + "M",
                    code: 3
                }
                this.notifyItemUi(file);
                return false;
            }
            if (this.options.fileTypeFilter != "all" && this.options.fileTypeFilter.indexOf(file.fileType) == -1) {
                file.fileStatus = MutiFile.UP_FINISH_FAILED;
                file.resultData = {
                    message: "文件类型不符合",
                    code: 3
                }
                this.notifyItemUi(file);
                return false;
            }
            return true;
        },
        startUp: function (file) {
            var _this = this;
            this.notifyItemUi(file);
            file.fileStatus = MutiFile.UP_ING;

            var fd = new FormData();
            fd.append("file", file.fileData);
            fd.append("fileName", file.name);
            fd.append("relativePath", file.webkitRelativePath || file.name);

            if (this.options.userParams) {
                for (var i in this.options.userParams) {
                    fd.append(i, this.options.userParams[i])
                }
            }
            if (file.userParams) {
                for (var i in file.userParams) {
                    fd.append(i, file.userParams[i])
                }
            }
            file.xhr = $.ajax({
                url: this.options.url,
                type: 'POST',
                cache: false,
                data: fd,
                processData: false,
                contentType: false,
                beforeSend: function (xhr) {
                },
                xhr: function () {
                    var xhr = $.ajaxSettings.xhr();
                    if (xhr.upload) {
                        xhr.upload.onloadstart = function () {
                            file.startUpTime = new Date().getTime();
                            _this.notifyItemUi(file);
                        }
                        xhr.upload.onprogress = function (progress) {
                            if (progress.lengthComputable) {
                                var pro = 0;
                                if (progress.loaded == progress.total) {
                                    pro = 99;
                                } else {
                                    pro = Math.floor((progress.loaded / progress.total).toFixed(2) * 100);
                                }
                                var totalUp = progress.loaded / 1024;
                                var totalTime = (new Date().getTime() - file.startUpTime) / 1000;
                                file.speed = Math.floor(totalUp / totalTime);
                                if (file.speed > 1024) {
                                    file.speed = Math.floor(file.speed / 1024) + "MB/S";
                                } else {
                                    file.speed = Math.floor(file.speed) + "KB/S";
                                }
                                file.pro = pro;
                                _this.notifyItemUi(file);
                            }
                        }
                    }
                    return xhr;
                },
                success: function (data) {
                    file.fileStatus = MutiFile.UP_FINISH_SUCCESS;
                    file.resultData = data;
                    if (data.code * 1 != 0) {
                        //处理失败.
                        file.fileStatus = MutiFile.UP_FINISH_FAILED;
                    }
                },
                error: function () {
                    file.fileStatus = MutiFile.UP_FINISH_FAILED;
                },
                complete: function () {
                    _this.notifyItemUi(file);
                    _this.run();
                }
            })
        },
        runResult: function () {
            var NEED_UP = 0
            var UP_ING = 0;
            var UP_CANCEL = 0;
            var UP_FINISH_FAILED = 0;
            var UP_FINISH_SUCCESS = 0;

            for (var index = 0; index < this.files.length; index++) {
                var tempFile = this.files[index];
                if (tempFile.fileStatus == MutiFile.NEED_UP) {
                    NEED_UP++;
                    continue;
                }
                if (tempFile.fileStatus == MutiFile.UP_ING) {
                    UP_ING++;
                    continue;
                }
                if (tempFile.fileStatus == MutiFile.UP_CANCEL) {
                    UP_CANCEL++;
                    continue;
                }
                if (tempFile.fileStatus == MutiFile.UP_FINISH_FAILED) {
                    UP_FINISH_FAILED++;
                    continue;
                }
                if (tempFile.fileStatus == MutiFile.UP_FINISH_SUCCESS) {
                    UP_FINISH_SUCCESS++;
                    continue;
                }
            }
            var data = {
                NEED_UP: NEED_UP,
                UP_ING: UP_ING,
                UP_CANCEL: UP_CANCEL,
                UP_FINISH_FAILED: UP_FINISH_FAILED,
                UP_FINISH_SUCCESS: UP_FINISH_SUCCESS,
                TOTAL: this.files.length
            }
            return data;
        },
        destory: function () {
            this.$tbody.remove();
        }
    }
    var methods = {
        init: function (options) {
            // 在每个元素上执行方法
            return this.each(function () {
                var mutiFile = new MutiFile($(this), options);
                $(this).data("mutiFile", mutiFile);
                mutiFile.initMutiFile();
            });
        },
        run: function () {
            $(this).data("mutiFile").run();
        },
        runResult: function () {
            return $(this).data("mutiFile").runResult();
        }

    };

    $.fn.MutiFile = function () {
        var method = arguments[0];
        if (methods[method]) {
            method = methods[method];
            arguments = Array.prototype.slice.call(arguments, 1);
        } else if (typeof (method) == 'object' || !method) {
            method = methods.init;
        } else {
            $.error('Method ' + method + ' does not exist on MutiFile');
            return this;
        }
        return method.apply(this, arguments);
    }
})
(jQuery);

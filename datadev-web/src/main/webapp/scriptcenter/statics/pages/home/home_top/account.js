$(function () {
    var tmpConfigArr = [];
    var addIndex = -1;
    var datadev_common = {};
    var maxShowOrder = 0;
    datadev_common.listMarketUrl = "/scriptcenter/config/getMarketByErp.ajax";
    datadev_common.listAccountUrl = "/scriptcenter/config/getAccountByErp.ajax";
    datadev_common.listQueueUrl = "/scriptcenter/config/getQueueByErp.ajax";
    datadev_common.saveUrl = "/scriptcenter/config/save.ajax";
    var accountCodeSelect = $("#accountCodeSelect").select2();
    var queueCodeSelect = $("#queueCodeSelect").select2();
    var marketSelect = $("#marketSelect").select2();
    var engineTypeSelect = $("#engineTypeSelect").select2();
    var formValid = undefined;
    var engineTypeEnum = {"jd-hive": "Hive-引擎", "presto": "Presto-高速引擎", "spark": "Spark-分析引擎", "impala": "Impala-引擎"};//存放的是引擎枚举


    $("#account-ok").click(function () {
        saveConfig(true);
    })
    $("#apply-account").click(function () {
        saveConfig();
    })
    $("#applyMarket").click(function () {
        //打开项目空间详情页
        window.open(_bdpDomain + "/planning/project/detail/" + top.window.projectSpaceId);
        // applyBuffaloMarket();
    })
    //单击选中事件
    $("ul.account-history-ul,ul.account-history-ul2").on("click", "li", function (e) {


        if (e.delegateTarget.className == "account-history-ul2") {
            //快捷配置　
            $(".defaultConfig").css("display", "block")
            $(".personConfig").css("display", "none");



            var target = undefined;
            var dataId = $(this).attr("data-id");
            for (var index = 0; index < configObj.length; index++) {
                if (configObj[index].id  == dataId) {
                    target = configObj[index];
                    break;
                }
            }
            if (target) {
                $("#defaultConfig-name").html(target.name);
                $("#defaultConfig-marketName").html(target.marketName);
                $("#defaultConfig-accountName").html(target.accountName);
                $("#defaultConfig-queueName").html(target.queueName);
                $("#defaultConfig-hasright").html(target.hasRight === true ? "有" : "无");
            }
            $("#applyId").css("display", "none");

            $(".account-history-li").removeClass("active")

            $(this).addClass("active");
            return;
        } else {
            //个人配置
            $(".defaultConfig").css("display", "none")
            $(".personConfig").css("display", "block");
            $("#applyId").css("display", "block");
        }

        formValid.resetForm();
        if ($(this).attr("data-name")) {
            $("#accountName").val($(this).attr("data-name"));
        } else {
            $("#accountName").val("");
        }
        $("#account-form").validate().element($("#accountName"));
        $("li.account-history-li.active").removeClass("active");
        $(this).addClass("active");
        var marketCode = $(this).attr("data-linux-user") ? $(this).attr("data-linux-user") : "";
        $("#marketSelect").val(marketCode).trigger("change", true);

        return false;
    })
    jQuery.validator.addMethod("validAccountName", function (value, element) {
        var pattern = /^[\u4e00-\u9fa50-9a-zA-z\-_]+$/
        return pattern.test(value);
    }, "只支持中文,字母,数字,下划线,中划线");
    jQuery.validator.addMethod("verifyRepeatName", function (value, element) {
        var nameCount = 0;
        $("li.account-history-li").each(function () {
            if ($(this).attr("data-name") == value) {
                nameCount++;
            }
        })
        return nameCount <= 1;
    }, "配置名不能重复");

    formValid = $('#account-form').validate({
        rules: {
            marketLinuxUser: {
                required: true
            },
            accountCode: {
                required: true
            },
            queueCode: {
                required: true
            },
            engineType: {
                required: true
            },
            name: {
                required: true,
                maxlength: 250,
                validAccountName: true,
                verifyRepeatName: true
            }
        },
        messages: {
            name: {
                required: "必填字段！",
                maxlength: $.validator.format("最多{0}个字符"),
                validAccountName: "只支持中文,字母,数字,下划线,中划线",
                verifyRepeatName: "配置名不能重复"
            },
            marketLinuxUser: {
                required: "必填字段！"
            },
            accountCode: {
                required: "必填字段！"
            },
            queueCode: {
                required: "必填字段！"
            },
            engineType: {
                required: "必填字段！"
            }
        },
        errorElement: 'label',
        errorClass: 'bdp-help-block',
        focusInvalid: false,
        highlight: function (e) {
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong').addClass('bdp-wrong');
        },
        success: function (e) {
            $(e).closest('.bdp-form-group').find(".bdp-form-control").removeClass('bdp-wrong');
            $(e).remove();
        },
        errorPlacement: function (error, element) {
            error.appendTo(element.parent());
        }
    });

    function getConfigType(configType){
        var result = [];
        for(var index = 0 ; configObj && index < configObj.length ; index++){
            if(configObj[index].configType * 1 === configType * 1){
                result.push(configObj[index]);
            }
        }
        return result ;
    }

    $('#codeModal').on('show.bs.modal', function () {
        $(".bdp-help-block").remove();



        var ul = $("ul.account-history-ul");
        ul.empty();
        maxShowOrder = 0;
        if (configObj && configObj.length > 0) {
            tmpConfigArr = deepArray(getConfigType(1));
            var lis = "";
            for (var index = 0; index < tmpConfigArr.length; index++) {
                var config = tmpConfigArr[index];
                if (config.showOrder && config.showOrder > maxShowOrder) {
                    maxShowOrder = config.showOrder;
                }
                lis += "<li class='account-history-li' data-id='" + config.id + "' data-market-user='" + config.marketUser + "' data-cluster='" + config.clusterCode + "'  data-name='" + config.name + "'data-linux-user='" + config.marketLinuxUser + "' data-queue='" + config.queueCode + "'data-account='" + config.accountCode + "'data-engine='" + config.engineType + "'>" + config.name + "</li>";
            }
            ul.append(lis);
        }

        var ul2 = $("ul.account-history-ul2");
        ul2.empty();
        var config2Temp = getConfigType(2) ;
        if (config2Temp && config2Temp.length > 0) {
            var lis2 = "";
            for (var index = 0; index < config2Temp.length; index++) {
                var config = config2Temp[index];
                lis2 += "<li class='account-history-li' data-id=" + config.id + ">" + config.name + "</li>";
            }
            ul2.append(lis2);
        } else {
            ul2.parent().prev(".account-part-name").hide()
        }


        addIndex = -1;
        var isEmpty = !configObj || configObj.length == 0;
        changeMarket(function () {
            if (isEmpty) {
               // $("#account-add").click()
            } else {
                activeLi(0);
            }
        });
    })
    //status; 1添加 2删除 3改变 其他值:默认不变
    $("#account-add").click(function () {
        var ul = $("div.account-history ul.account-history-ul");
        var dataId = addIndex--;
        var li = "<li class='account-history-li' data-id='" + dataId + "'>未命名</li>";
        ul.prepend(li);
        var config = {name: "未命名", status: 1, id: dataId, showOrder: ++maxShowOrder};
        tmpConfigArr.unshift(config);
        activeLi(0, true, true);
        return false;
    })
    $("#account-remove").click(function () {
        var acLi = $("li.account-history-li.active");
        var id = $(acLi).attr('data-id');
        $(acLi).remove();
        for (var index = 0; index < tmpConfigArr.length; index++) {
            if (id && tmpConfigArr[index] && tmpConfigArr[index].id == id) {
                if (id > 0) {
                    tmpConfigArr[index].status = 2;
                } else {
                    tmpConfigArr.splice(index, 1);
                }
                break;
            }
        }
        $(".bdp-help-block").remove();
        activeLi(0);
        return false;
    })
    $("#account-copy").click(function () {
        var acli = $("li.account-history-li.active");
        var id = $(acli).attr('data-id');
        var dataId = addIndex--;
        var config = getConfigById(id, true);
        var copyName = config.name + "_copy";
        var copyNamePattern = new RegExp("^" + copyName + "_(\\d)+$");
        var maxNum = 0;
        $("li.account-history-li").each(function () {
            var name = $(this).attr("data-name");
            var results = copyNamePattern.exec(name);
            if (results && results[1]) {
                maxNum = Math.max(maxNum, results[1] * 1);
            }
        })
        config.id = dataId;
        config.name = copyName + "_" + (++maxNum);
        config.showOrder = ++maxShowOrder;
        config.status = 1;
        var ul = $($("div.account-history ul").get(0));
        var li = "<li class='account-history-li' data-market-user='" + config.marketUser + "' data-name='" + config.name + "' data-id='" + dataId + "' data-cluster='" + config.clusterCode + "' data-linux-user='" + config.marketLinuxUser + "' data-queue='" + config.queueCode + "'data-account='" + config.accountCode + "'data-engine='" + config.engineType + "'>" + config.name + "</li>";
        ul.prepend(li);
        tmpConfigArr.unshift(config);
        activeLi(0, true, false);
        return false;
    })
    $("#account-up").click(function () {
        var activeLi = $("li.account-history-li.active");
        var id = $(activeLi).attr('data-id');
        var preLi = activeLi.prev();
        if (preLi.length > 0) {
            var preId = preLi.attr("data-id");
            var config = getConfigById(id);
            var preConfig = getConfigById(preId);
            if (config.showOrder && preConfig.showOrder) {
                var tmpOrder = config.showOrder;
                config.showOrder = preConfig.showOrder;
                preConfig.showOrder = tmpOrder;
                config.status = config.status ? config.status : 3;
                preConfig.status = preConfig.status ? preConfig.status : 3;
            }
            var index = -1;
            var preIndex = -1;
            for (var ind = 0; ind < tmpConfigArr.length; ind++) {
                if (id && tmpConfigArr[ind] && tmpConfigArr[ind].id == id) {
                    index = ind;
                }
                if (id && tmpConfigArr[ind] && tmpConfigArr[ind].id == preId) {
                    preIndex = ind;
                }
            }
            if (index >= 0 && preIndex >= 0) {
                tmpConfigArr.splice(index, 1, preConfig);
                tmpConfigArr.splice(preIndex, 1, config);
                activeLi.insertBefore(preLi);
            }
        }
        return false;
    })
    $("#account-down").click(function () {
        var activeLi = $("li.account-history-li.active");
        var id = $(activeLi).attr('data-id');
        var nextLi = activeLi.next();
        if (nextLi.length > 0) {
            var nextId = nextLi.attr("data-id");
            var config = getConfigById(id);
            var nextConfig = getConfigById(nextId);
            if (config.showOrder || nextConfig.showOrder) {
                var tmpOrder = config.showOrder;
                config.showOrder = nextConfig.showOrder;
                nextConfig.showOrder = tmpOrder;
                config.status = config.status ? config.status : 3;
                nextConfig.status = nextConfig.status ? nextConfig.status : 3;
            }
            var index = -1;
            var nextIndex = -1;
            for (var ind = 0; ind < tmpConfigArr.length; ind++) {
                if (id && tmpConfigArr[ind] && tmpConfigArr[ind].id == id) {
                    index = ind;
                }
                if (id && tmpConfigArr[ind] && tmpConfigArr[ind].id == nextId) {
                    nextIndex = ind;
                }
            }
            if (index >= 0 && nextIndex >= 0) {
                tmpConfigArr.splice(index, 1, nextConfig);
                tmpConfigArr.splice(nextIndex, 1, config);
                nextLi.insertBefore(activeLi);
            }

        }
        return false;
    })

    function activeLi(index, noClick, clearMarket) {
        var lis = $("ul.account-history-ul li");
        if (index >= 0 && lis.length > index) {
            lis.each(function (ind) {
                if (ind == index) {
                    var name = $(this).attr("data-name");
                    $("#accountName").val(name);
                    if (noClick) {
                        $("li.account-history-li.active").removeClass("active");
                        $(this).addClass("active");
                        if (clearMarket) {
                            $("#marketSelect").val("").trigger("change");
                        }
                    } else {
                        $(this).click();
                    }
                }
            })
        } else {
            $("#accountName").val("");
            $("#marketSelect").val("").trigger("change");
        }
    }

    function getConfigById(id, isCopy) {
        var config = {};
        if (isCopy) {
            for (var index = 0; index < tmpConfigArr.length; index++) {
                if (id && tmpConfigArr[index] && (tmpConfigArr[index].id == id || tmpConfigArr[index].oriId == id)) {
                    var oriConfig = tmpConfigArr[index];
                    for (var pro in oriConfig) {
                        config[pro] = oriConfig[pro];
                    }
                    return config;
                }
            }
        } else {
            for (var index = 0; index < tmpConfigArr.length; index++) {
                if (id && tmpConfigArr[index] && tmpConfigArr[index].id == id) {
                    var oriConfig = tmpConfigArr[index];
                    return oriConfig;
                }
            }
        }
        return config;
    }

    $("#accountName").bind("input propertychange", function () {
        var activeLi = $("li.account-history-li.active");
        var id = activeLi.attr("data-id");
        var config = getConfigById(id);
        if (config) {
            config.status = config.status ? config.status : 3;
            config.name = $(this).val();
            activeLi.attr("data-name", config.name);
            if (!config.name || config.name == "") {
                activeLi.text("未命名");
            } else {
                activeLi.text(config.name);
            }
        }
    });

    $("#marketSelect").on("change", function (event, isStayStatus) {

        var option = $("option:selected", $("#marketSelect"));
        var activeLi = $("li.account-history-li.active");
        if (option.length > 0 && option.attr("value") != "") {
            var id = activeLi.attr("data-id");
            var config = getConfigById(id);
            if (config) {
                config.status = config.status ? config.status : 3;
                config.marketLinuxUser = option.attr("data-market-user");
                config.marketUser = option.attr("market-user") != "null" ? option.attr("market-user") : null;
                activeLi.attr("data-cluster", config.clusterCode);
                activeLi.attr("data-linux-user", config.marketLinuxUser);
                activeLi.attr("data-market-user", config.marketUser);
                if (!isStayStatus) {
                    config.accountCode = null;
                    config.accountId = null;
                    config.queueCode = null;
                    config.queueId = null;
                    config.engineType = null;
                    activeLi.attr("data-account", "");
                    activeLi.attr("data-queue", "");
                    activeLi.attr("data-engine", "");
                }

            }
            var marketUser = option.attr("data-market-user");
            changeAccount(marketUser, isStayStatus);
        } else {
            changeAccount();
        }
    })
    $("#accountCodeSelect").on("change", function (event, isStayStatus) {

        var marOption = $("option:selected", $("#marketSelect"));
        var marketUser = marOption.attr("data-market-user");
        var option = $("option:selected", $("#accountCodeSelect"));
        var dataCode = option.attr("value");
        if (marOption.length > 0 && marOption.attr("value") != "" && option.length > 0 && option.attr("value") != "") {
            var activeLi = $("li.account-history-li.active");
            var id = activeLi.attr("data-id");
            var config = getConfigById(id);
            if (config) {
                config.status = config.status ? config.status : 3;
                config.accountCode = option.attr("value");
                config.clusterCode = option.attr("data-cluster");
                config.accountId = option.attr("data-id") != "null" ? option.attr("data-id") : null;
                activeLi.attr("data-account", config.accountCode);
                activeLi.attr("data-cluster", config.clusterCode)
                if (!isStayStatus) {
                    config.queueCode = null;
                    config.queueId = null;
                    config.engineType = null;
                    // config.clusterCode = null;
                    activeLi.attr("data-queue", "");
                    activeLi.attr("data-engine", "");
                }
            }
            $(this).parent().find(".bdp-help-block").remove();
            changeQueue(marketUser, dataCode);
        } else {
            changeQueue();
        }
    })
    //改变queue并将改变记录到config中，将影响传递到引擎中
    $("#queueCodeSelect").on("change", function () {
        var option = $("option:selected", $("#queueCodeSelect"));
        if (option.length > 0 && option.attr("value") != "") {
            var activeLi = $("li.account-history-li.active");
            var id = activeLi.attr("data-id");
            var config = getConfigById(id);
            if (config) {
                config.status = config.status ? config.status : 3;
                config.queueCode = option.attr("value");
                config.queueId = option.attr("data-id") != "null" ? option.attr("data-id") : null;
                activeLi.attr("data-queue", config.queueCode);
            }
            $(this).parent().find(".bdp-help-block").remove();
            changeEngine(option.attr("data-engine") != "null" ? option.attr("data-engine") : null);
        } else {
            changeEngine();
        }
    })

    //改变engine并将改变记录到config中
    $("#engineTypeSelect").on("change", function () {
        var option = $("option:selected", $("#engineTypeSelect"));
        if (option.length > 0 && option.attr("value") != "") {
            var activeLi = $("li.account-history-li.active");
            var id = activeLi.attr("data-id");
            var config = getConfigById(id);
            if (config) {
                config.status = config.status ? config.status : 3;
                config.engineType = option.attr("value");
                // config.queueId = option.attr("data-id") != "null" ? option.attr("data-id") : null;
                activeLi.attr("data-engine", config.engineType);
            }
            $(this).parent().find(".bdp-help-block").remove();
        }
    })

    //重新拉取集市
    function changeMarket(initial) {
        commonAjaxEvents.commonPostAjax(datadev_common.listMarketUrl, {}, $("#marketSelect"), function (node, data) {
            var options = "<option value=''></option>";
            if (data && data.obj) {
                for (var index = 0; index < data.obj.length; index++) {
                    var market = data.obj[index];
                    options += "<option data-ugdap='" + market.isUgdap + "' data-cluster='" + market.clusterCode + "' data-id='" + market.marketUser + "' data-market-user='" + market.marketUser + "' value='" + market.marketUser + "'>" + market.marketName + "</option>";
                }
            }
            $("#marketSelect").empty().append(options);
            marketSelect = $("#marketSelect").select2();
            initial && initial();
        });
    }

    //集市改变重新拉取生产账号
    function changeAccount(marketCode, isStayStatus) {
        if (marketCode) {
            commonAjaxEvents.commonPostAjax(datadev_common.listAccountUrl, {linuxUser: marketCode}, $("#accountCodeSelect"), function (node, data) {
                var options = "<option value=''></option>";
                if (data && data.obj) {
                    for (var index = 0; index < data.obj.length; index++) {
                        var account = data.obj[index];
                        options += "<option data-cluster='" + account.clusterCode + "'  data-id='" + account.id + "'  value='" + account.code + "'>" + account.name + "</option>";
                    }
                }
                $("#accountCodeSelect").empty().append(options);
                accountCodeSelect = $("#accountCodeSelect").select2();
                var activeLi = $("li.account-history-li.active");
                if (activeLi.length == 1) {
                    var account = $(activeLi).attr("data-account");
                    $("#accountCodeSelect").val(account).trigger("change", isStayStatus ? true : false);
                } else {
                    changeQueue();
                }
            });
        } else {
            var options = "<option value=''></option>";
            $("#accountCodeSelect").empty().append(options);
            accountCodeSelect = $("#accountCodeSelect").select2();
            changeQueue();
        }
    }

    //生产账号改变重新拉取队列
    function changeQueue(marketUser, accountCode) {
        if (marketUser) {
            commonAjaxEvents.commonPostAjax(datadev_common.listQueueUrl, {
                productionAccountCode: accountCode || "",
                linuxUser: marketUser
            }, $("#queueCodeSelect"), function (node, data) {
                var options = "<option value=''></option>";
                if (data && data.obj) {
                    for (var index = 0; index < data.obj.length; index++) {
                        var queue = data.obj[index];
                        options += "<option data-id='" + queue.id + "'  value='" + queue.queueCode + "'  data-engine='" + queue.engineTypes + "'>" + queue.queueName + "</option>";
                    }
                }
                $("#queueCodeSelect").empty().append(options);
                queueCodeSelect = $("#queueCodeSelect").select2();
                var activeLi = $("li.account-history-li.active");
                if (activeLi.length == 1) {
                    var queue = $(activeLi).attr("data-queue");
                    $("#queueCodeSelect").val(queue).trigger("change");
                } else {
                    changeEngine();
                }
            });
        } else {
            var options = "<option value=''></option>";
            $("#queueCodeSelect").empty().append(options);
            queueCodeSelect = $("#queueCodeSelect").select2();
            changeEngine();
        }

    }

    //队列改变时重新获取引擎,并默认选中jd-hive
    function changeEngine(engineTypes) {
        if (engineTypes) {
            var options = "<option value=''></option>";
            var splitEngineType = engineTypes.split(",");
            for (var index = 0; index < splitEngineType.length; index++) {
                var engineType = splitEngineType[index];
                options += "<option  value='" + engineType + "'>" + ((engineTypeEnum[engineType] == undefined) ? "不支持引擎" : engineTypeEnum[engineType]) + "</option>";
            }
            $("#engineTypeSelect").empty().append(options);
            engineTypeSelect = $("#engineTypeSelect").select2();
            var activeLi = $("li.account-history-li.active");
            if (activeLi.length == 1 && $(activeLi).attr("data-engine")) {
                var engine = $(activeLi).attr("data-engine");
                $("#engineTypeSelect").val(engine).trigger("change");
            }

            var option = $("option:selected", $("#engineTypeSelect"));
            if (option.length == 1 && option.attr("value") == "") {//默认选中hive
                $("#engineTypeSelect").val("jd-hive").trigger("change");
            }
        } else {
            var options = "<option value=''></option>";
            $("#engineTypeSelect").empty().append(options);
            engineTypeSelect = $("#engineTypeSelect").select2();
        }
    }

    function saveConfig(hideModal) {

        var result = true;
        var lis = $(".account-history-ul > li");
        var duObj = {};
        if (lis.length > 0) {
            result = $('#account-form').valid();
            if (result) {
                lis.each(function (index, element) {
                    element = $(element);
                    result = !duObj[element.attr("data-name")] && Boolean(element.attr("data-cluster")) && Boolean(element.attr("data-name")) && Boolean(element.attr("data-queue")) && Boolean(element.attr("data-account")) && Boolean(element.attr("data-engine"))
                    if (!result) {
                        element.trigger("click");
                        return false;
                    }
                    duObj[element.attr("data-name")] = true;
                });
            }
        }

        if (result) {
            $(".bdp-help-block").remove();
            //添加“项目空间id”
            for (var i = 0; i < tmpConfigArr.length; i++) {
                tmpConfigArr[i].projectSpaceId = top.window.projectSpaceId;
            }
            var configStr = JSON.stringify(tmpConfigArr);
            commonAjaxEvents.commonJSONPostAjax(datadev_common.saveUrl, configStr, $("#account-ok"), function (node, data) {

                
                if (data && data.obj) {
                    configObj = data.obj;
                    //个人配置账号

                    var tmpPP = [] ;
                    for(var index = 0 ; index < configObj.length ; index ++){
                        if(configObj[index].configType * 1 == 2){
                            tmpPP = configObj[index];
                        }
                    }

                    tmpConfigArr = deepArray(tmpPP);
                    var activeLi = $("li.account-history-li.active");
                    var oriId = -1;
                    var configType = activeLi.parent().hasClass("account-history-ul2") ? 2 : 1;
                    if (activeLi.length > 0) {
                        oriId = activeLi.attr("data-id");

                    }
                    resetSelect(oriId , configType);
                }
                if (hideModal) {
                    $('#codeModal').modal("hide");
                }
                return false;
            })
        }
        return false;


    }

    function deepArray(source) {
        var result = [];
        for (var index = 0; index < source.length; index++) {
            result[index] = typeof source[index] === 'object' ? deepCopy(source[index]) : source[index];
        }
        return result;
    }

    function deepCopy(source) {
        var result = {};
        for (var key in source) {
            result[key] = source[key] === null ? null : typeof source[key] === 'object' ? deepCopy(source[key]) : source[key];
        }
        return result;
    }

})

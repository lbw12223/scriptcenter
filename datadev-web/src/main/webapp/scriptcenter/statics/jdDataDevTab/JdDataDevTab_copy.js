(function ($) {
    Array.prototype.remove = function (dx) {
        if (isNaN(dx) || dx > this.length) {
            return false;
        }
        for (var i = 0, n = 0; i < this.length; i++) {
            if (this[i] != this[dx]) {
                this[n++] = this[i]
            }
        }
        this.length -= 1;
    }


    /**
     * 1.可以动态添加或者删除。 动态添加只支持iframe的方式
     * 2.支持可以横，纵
     * @param element
     * @param options
     * @constructor
     */
    var JdDataDevTab = function JdDataDevTab(element, options) {
        this.$element = element;
        this.defaults = {
            hasFullScreen: false
        };
        this.options = $.extend({}, this.defaults, options);
        this.windowsId = 1;
        this.scriptWindow = {};

        this.currentElementWidth = 0;

        /*
         * contentDiv:$node
         * li_id:
         * */
        this.tabInfos = [];
    }
    JdDataDevTab.prototype = {
        addTabInfos: function (content_div, key, params) {
            var temp = {
                content_div: content_div,
                key: key,
                params: params
            }
            this.tabInfos.push(temp);
        },
        getTabInfo: function (key) {
            for (var index = 0; index < this.tabInfos.length; index++) {
                var temp = this.tabInfos[index];
                if (temp.key == key) {
                    return temp;
                }
            }
        },
        changeTabInfos: function (key, newKey, params) {
            for (var index = 0; index < this.tabInfos.length; index++) {
                var temp = this.tabInfos[index];
                if (temp.key == key) {
                    this.tabInfos.splice(index, 1, {
                        content_div: temp.content_div,
                        key: newKey,
                        params: params
                    })
                    break;
                }
            }
            if (this.scriptWindow[key]) {
                this.scriptWindow[newKey] = this.scriptWindow[key];
                this.scriptWindow[key] = undefined;
            }
            var li = $("ul.tab-header > li[data-id='" + key + "']", this.$element)
            if (li.length > 0) {
                var span = $("span.labelName", li);
                span.text(params.label);
                li.attr("data-title", params.title)
                li.attr("data-label", params.label)
                li.attr("data-id", newKey);
            }
            var target = li.attr("target");
            var targetLis = $("ul.moreTagUL li[target='" + target + "']", this.$element);
            $("a", targetLis).text(name);
        },
        removeTabInfos: function (key) {
            for (var index = 0; index < this.tabInfos.length; index++) {
                var temp = this.tabInfos[index];
                if (temp.key == key) {
                    this.tabInfos.splice(index, 1);
                    return;
                }
            }
        },
        hiddenTabPullDown: function () {
            var tabMore = $(".tabPullDown", this.$element);
            if (tabMore && tabMore.length > 0) {
                tabMore.css("display", "none");
            }
        },
        caculateNeedCloseTab: function (currentDatId, eventType) {
            var needCloseTab = [];
            var lis = $(".tab-header > li", this.$element);
            var hasFind = false;
            for (var index = 0; index < lis.length; index++) {
                var liNode = $(lis[index]);
                if ($(".datadevClosFlag", liNode).length > 0) {
                    var tempDataId = liNode.attr("data-id");
                    hasFind = hasFind || (currentDatId == tempDataId);
                    if (eventType === "closeLeft") {
                        if (!hasFind) {
                            needCloseTab.push(liNode);
                        }
                    }
                    if (eventType === "closeRight") {
                        if (hasFind) {
                            if ((currentDatId == tempDataId)) {
                                continue;
                            }
                            needCloseTab.push(liNode);
                        }
                    }
                    if (eventType === "closeOther") {
                        if (!hasFind) {
                            needCloseTab.push(liNode);
                        }
                        if (hasFind && (currentDatId != tempDataId)) {
                            needCloseTab.push(liNode);
                        }
                    }
                    if (eventType === "closeAll") {
                        needCloseTab.push(liNode);
                    }
                    if (eventType === "closeCurrent") {
                        if (hasFind && (currentDatId == tempDataId)) {
                            needCloseTab.push(liNode);
                        }
                    }
                }
            }
            return needCloseTab;
        },
        closeTabPullDown: function (eventType) {
            //下拉的关闭
            var tabPullDown = $(".tabPullDown", this.$element);
            var currentDataId = tabPullDown.attr("data-id");
            //计算可以待close的 tab
            var needCloseTab = this.caculateNeedCloseTab(currentDataId, eventType);
            this.hiddenTabPullDown();
            if (needCloseTab != null && needCloseTab.length > 0) {
                for (var index = 0; index < needCloseTab.length; index++) {
                    var liNode = $(needCloseTab[index]);
                    $(".datadevClosFlag", liNode).click();
                }
            }
        },
        findInfoByParam: function (paramKey, paramValue) {
            if (paramKey) {
                var tabInfos = this.tabInfos;
                for (var index = 0; index < tabInfos.length; index++) {
                    if (tabInfos[index].params[paramKey] == paramValue) {
                        return tabInfos[index];
                    }
                }
            }
            return null;
        },
        initJdDataDevTab: function () {
            var _this = this;
            _this.currentElementWidth = _this.$element.width() - this.getFixtedUlWidth();
            this.$element.on("click", ".tabPullDown li", function (event, args) {
                var thisNode = $(this);
                _this.closeTabPullDown(thisNode.attr("envent-type"));
            })
            //li click 事件
            this.$element.on("click", "ul.datadev-ul li", function (event, args) {
                var li = $(this)
                _this.activeLi(li, args, event);
            })
            this.$element.on("dblclick", "ul.datadev-ul li", function (event) {
                _this.options.fullScreenClick && _this.options.fullScreenClick($(".fullScreen", _this.$element));
                event.stopPropagation();
                event.preventDefault();
            })

            this.$element.on("mousedown", "ul.datadev-ul li", function (event, args) {
                if (event.which == 3) {
                    var thisNode = $(this);
                    var canClose = $(".datadevClosFlag", thisNode).length > 0;
                    if (canClose) {
                        var top = thisNode.height() + 10;
                        var left = thisNode.position().left * 1;
                        var tabPullDown = $(".tabPullDown", _this.$element);
                        if (tabPullDown && tabPullDown.length > 0) {
                            tabPullDown.attr("data-id", thisNode.attr("data-id"));
                            parent && parent.hiddenAllRightMeun && parent.hiddenAllRightMeun();
                            tabPullDown.css("display", "block");
                            tabPullDown.css("left", left + "px");
                            tabPullDown.css("top", top + "px");
                        }
                    }
                }
            })
            //移除事件
            this.$element.on("click", ".datadevClosFlag", function (event) {
                _this.hiddenTabPullDown();
                var node = $(this).parent().parent();
                if (_this.options && _this.options.beforeClickClose) {
                    _this.options.beforeClickClose(node);
                } else {
                    _this.removeLi(node);
                }
                event.stopPropagation();
                event.preventDefault();
            })


            window.setInterval((function () {
                var UlWidth = _this.$element.width();
                var fixtedWidth = _this.getFixtedUlWidth();
                if ((UlWidth - fixtedWidth) != _this.currentElementWidth) {
                    _this.caculateMoreTabLi();
                    _this.currentElementWidth = UlWidth - fixtedWidth;
                }
            }), 100);

            //更多tag 事件处理
            this.$element.on("click", ".moreTitle", function (event) {
                var ul = $(".moreTagUL", _this.$element);
                if (ul.css("display") === "block") {
                    ul.css("display", "none");
                } else {
                    ul.css("display", "block");
                }
            })

            this.$element.on("click", ".moreTagULli", function (event) {
                var li = $(this);
                _this.moreLiClick(li);
                li.parent().css("display", "none");
            })
            this.initFullScreen();
        },
        initFullScreen: function () {
            _this = this;
            if (this.options.hasFullScreen) {
                var fullScreenDiv = $("<div class='fullScreen'/>").html("<span class='bdp-icon ide-icon-zk'></span>");
                var headerParent = this.getDynamicUl().parent();
                headerParent.append(fullScreenDiv);
            }
            this.$element.on("click", ".fullScreen", function (event) {
                _this.options.fullScreenClick && _this.options.fullScreenClick($(this));
                event.stopPropagation();
                event.preventDefault();
            });
        },
        activeLi: function (li, args, event) {
            var target = li.attr("target");
            var show = true;
            var tabContent = $("#" + target, this.$element);
            this.hiddenTabPullDown();
            // tabContent 可能是初始化出来的，需要添加进去
            if (tabContent.length < 1) {
                var key = li.attr("data-id");
                var tabInfo = this.getTabInfo(key);
                var appendDiv =
                    '<div id="' + target + '" class="tab-content undisplay">' +
                    '<iframe data-targetLi="' + li.attr("id") + '" src="' + tabInfo.params.url + '" border="none" width="100%" height="100%" frameborder="0" scrolling="auto"></iframe>' +
                    '</div>';
                $(this.$element).append(appendDiv);
                tabContent = $("#" + target, this.$element);
                tabInfo.content_div = tabContent;

            }

            if (this.options.changeClickCallBack) {
                show = this.options.changeClickCallBack && this.options.changeClickCallBack(event, this, li, tabContent);
            }
            if (show) {
                $(".tab-content", this.$element).css("display", "none");
                $("ul.datadev-ul li.active", this.$element).removeClass("active");
                li.addClass("active");
                tabContent.css("display", "block");
                var dataWidth = tabContent.data("width") * 1;
                if (dataWidth * 1 > 0) {
                    this.$element.css("width", dataWidth + "px");
                    tabContent.css("width", (dataWidth - 33) + "px");
                }
            }

            this.caculateMoreTabLi();
            if (this.options.afterClickCallBack) {
                this.options.afterClickCallBack(event, args, this, li, tabContent);
            }
        },
        unActive: function (li) {
            li.removeClass("active");
            var target = li.attr("target");
            var contentDiv = $("#" + target, this.$element);
            contentDiv.css("display", "none");
            this.$element.css("width", "33px");
        },
        unActiveIndex: function (index) {
            var li = $(".tab-header > li:eq('" + index + "')", this.$element);
            this.unActive(li);
        },
        active: function (index) {
            if (index >= 0 && index < this.getSizeOfTab()) {
                index = index * 1 >= 0 ? index * 1 : 0;
                var li = $(".tab-header > li:eq('" + index + "')", this.$element);
                this.activeLi(li);
            }
        },
        activeLiByDataId: function (dataId) {
            var li = $("li[data-id='" + dataId + "']", this.$element);
            if (li.length > 0) {
                this.activeLi(li);
            }
        },
        addScriptCallBack: function (key, targetId) {
            this.scriptWindow[key] = targetId;
        },
        getSizeOfTab: function () {
            var tabHeaderLiSize = $(".tab-header > li", this.$element).length;
            return tabHeaderLiSize;
        },
        addTab: function (params) {
            if (params && params.key && this.scriptWindow[params.key]) {
                if (this.getTabInfo(params.key) && params.url) {
                    this.getTabInfo(params.key).params.url = params.url;
                }
                return this.scriptWindow[params.key];
            } else {
                var appendFlagHtml = params.canRemove ? '<span class="datadevClosFlag">×</span>' : '';
                var tabHeaderLiSize = this.windowsId++;
                var elementId = this.$element.attr("id");
                var target = elementId + "_iframe_" + tabHeaderLiSize;
                var liId = target + "_li";
                this.scriptWindow[params.key] = liId;
                var li = '<li class="pop-title" data-label="' + params.label + '" data-title="' + (params.title || params.path || params.label) + '" id="' + liId + '" target="' + target + '" data-id="' + params.key + '"><a href="javascript:;"><span class="modify-status"></span><span class="labelName">' + params.label + "</span>" + appendFlagHtml + '</a></li>'
                var appendDiv =
                    '<div id="' + target + '" class="tab-content undisplay">' +
                    '<iframe data-targetLi="' + liId + '" src="' + params.url + '" border="none" width="100%" height="100%" frameborder="0" scrolling="auto"></iframe>' +
                    '</div>';
                var tab_headers = $(".tab-header", this.$element);
                //往动态里面添加
                if (tab_headers.length > 1) {
                    tab_headers = $(".tab-header.dynamic-ul", this.$element);
                }
                tab_headers.append(li);
                $(this.$element).append(appendDiv);

                this.addTabInfos($("#" + target, this.$element), params.key, params);
                return liId;
            }
        },
        /**
         * 只是初始化header中的li
         */
        initTabs: function (tabArrays) {
            if (tabArrays != null && tabArrays.length > 0) {
                for (var index = 0; index < tabArrays.length; index++) {
                    var li = this.getAddTabLi(tabArrays[index].params);
                    $(".tab-header", this.$element).append(li);
                    this.addTabInfos(undefined, tabArrays[index].key, tabArrays[index].params);
                }
            }
        },
        getFixtedUlWidth: function () {
            var fixtedUl = $(".tab-header.datadev-ul.fixed-ul", this.$element);
            return fixtedUl.length == 1 ? (fixtedUl.width() || 0) : 0;
        },
        getFixtedUl: function () {
            var fixtedUl = $(".tab-header.datadev-ul.fixed-ul", this.$element);
            if (fixtedUl.length == 0) {
                fixtedUl = $(".tab-header.datadev-ul", this.$element);
            }
            return fixtedUl;
        },
        getDynamicUl: function () {
            var dynamicUl = $(".tab-header.datadev-ul.dynamic-ul", this.$element);
            if (dynamicUl.length == 0) {
                dynamicUl = $(".tab-header.datadev-ul", this.$element);
            }
            return dynamicUl;
        },
        getAddTabLi: function (params) {
            var appendFlagHtml = params.canRemove ? '<span class="datadevClosFlag">×</span>' : '';

            var tabHeaderLiSize = this.windowsId++;
            var elementId = this.$element.attr("id");
            var target = elementId + "_iframe_" + tabHeaderLiSize;
            var liTarget = target + "_li";
            this.scriptWindow[params.key] = liTarget;
            var li = '<li class="pop-title" data-label="' + params.label + '" data-title="' + (params.title || params.path || params.label) + '" id="' + liTarget + '" target="' + target + '" data-id="' + params.key + '"><a href="javascript:;"><span class="modify-status"></span><span class="labelName"></span>' + appendFlagHtml + '</a></li>';
            var $li = $(li);
            $("span.labelName",$li).text(params.label);
            return $li;
        },
        getAllTab: function () {
            var lis = $("li", $(".tab-header", this.$element));
            var resultList = [];
            if (lis.length > 0) {
                for (var index = 0; index < lis.length; index++) {
                    resultList.push($(lis[index]));
                }
            }
            return resultList;
        },
        getCurrentActiveTab: function () {
            var activeLi = $("li.active", $(".tab-header", this.$element));
            if (activeLi.length > 0) {
                var target = activeLi.attr("target");
                var div = $("#" + target, this.$element);
                var result = {
                    activeLi: activeLi,
                    activeDiv: div,
                    initStatus: div.length>0
                }
                return result;
            }
            return undefined;
        },
        getActiveTabByDataId: function (dataId) {
            var li = $("li[data-id='" + dataId + "']");
            if (li.length > 0) {
                var target = li.attr("target");
                var div = $("#" + target, this.$element);
                var result = {
                    activeLi: li,
                    activeDiv: div,
                    initStatus: div.length>0
                }
                return result;
            }
            return undefined;
        },
        /**
         * 如果新添加的tab > UL ,那么需要显示出 更多标签
         * [caculateMoreTab description]
         * @return {[type]} [description]
         */
        moveMoreTab: function () {
            var liWidths = 0;
            var UlWidth = this.options.hasFullScreen ? this.$element.width() - 60 - this.getFixtedUlWidth() : this.$element.width() - 30 - this.getFixtedUlWidth();
            /**
             * 获取当前选中节点总共的长度 > 总共长度  = inner -margin
             *
             * @type {*}
             */

            var li = $("li.active", $(".tab-header ", this.$element));
            var activeUl = li.parents(".tab-header");
            if (!activeUl.hasClass("fixed-ul")) {
                var preTotal = 0;
                if (li.length > 0) {
                    var beforeLi = li.prevAll();
                    beforeLi.each(function (index) {
                        var li = $(this);
                        preTotal += li.outerWidth();
                    })
                    preTotal += li.outerWidth();
                }
                var dataDevUL = $(".datadev-ul.dynamic-ul", this.$element);
                if (dataDevUL.length == 0) {
                    dataDevUL = $(".datadev-ul", this.$element);
                }
                if (preTotal > UlWidth) {
                    dataDevUL.css("margin-left", -1 * (preTotal - UlWidth ) + "px");
                } else {
                    dataDevUL.css("margin-left", (0) + "px");
                }
            }
        },
        /**
         * 计算下拉里面有 那些li
         * marginLeft + 宽度小于 < 0 || marginLeft + 宽度小于 > ul 都是undisplay
         */
        caculateMoreTabLi: function () {
            this.moveMoreTab();
            var ul = this.getDynamicUl();
            var lis = $("li", ul);
            var total = (ul.css("margin-left") || "0px").replace("px", "") * 1;
            var UlWidth = this.options.hasFullScreen ? this.$element.width() - 60 - this.getFixtedUlWidth() : this.$element.width() - 30 - this.getFixtedUlWidth();

            var unDisplayLi = [];
            lis.each(function () {
                var li = $(this);
                //上一个是否已经超过了
                if (total + 20 > UlWidth) {
                    unDisplayLi.push(li);
                } else {
                    total += li.outerWidth();
                    if (total <= 20) {
                        unDisplayLi.push(li);
                    }
                }

            })
            if (unDisplayLi.length > 0) {
                $(".moreTitle", this.$element).css("display", "block");
                if (this.options.hasFullScreen) {
                    $(".fullScreen", this.$element).css("right", "30px");
                }
            } else {
                $(".moreTitle", this.$element).css("display", "none");
                $(".fullScreen", this.$element).css("right", "0px");
            }
            this.showMoreTab(unDisplayLi);
        },
        showMoreTab: function (unDisplayLi) {
            var lis = "";
            if (unDisplayLi.length > 0) {
                for (var index = 0; index < unDisplayLi.length; index++) {
                    var liNode = $(unDisplayLi[index]);
                    var target = liNode.attr("target");
                    var dataTitle = liNode.attr("data-label");
                    lis += "<li class='moreTagULli' target='" + target + "'><a href='javascript:void(0)'>" + dataTitle + "</a></li>";
                }
                $(".moreTagUL", this.$element).html(lis).css("display", "none");
            } else {
                $(".moreTagUL", this.$element).html("").css("display", "none");
            }
        },
        moreLiClick: function (li) {
            var target = li.attr("target");
            this.activeLi($(".tab-header > li[target='" + target + "']", this.$element));
        },
        removeIndex: function (index) {
            if (index >= 0 && index < this.getSizeOfTab()) {
                index = index * 1 >= 0 ? index * 1 : 0;
                var li = $(".tab-header > li:eq('" + index + "')", this.$element);
                this.removeLi(li);
            }
        },
        /**
         * 移除tab
         * 1.查询当前是否有选中
         * 2.无选中，选中前或者后一个
         * @param li
         */
        removeLi: function (li) {
            var nextLi = li.next();
            var prev = li.prev();
            var target = li.attr("target");
            var tabContent = $("#" + target, this.$element);
            li.remove();
            tabContent.remove();
            var targetLi = li.attr("id");
            if (this.scriptWindow != undefined) {
                for (var id in  this.scriptWindow) {
                    if (this.scriptWindow[id] == targetLi) {
                        this.scriptWindow[id] = undefined;
                    }
                }
            }
            var active = $("li.active", $(".tab-header", this.$element));
            if (active.length < 1) {
                if (nextLi.length > 0) {
                    nextLi.click();
                }
                if (prev.length > 0) {
                    prev.click();
                }
            }
            this.removeTabInfos(li.attr("data-id"));
            this.caculateMoreTabLi();
            this.options.afterRemoveTabCallBackEvent && this.options.afterRemoveTabCallBackEvent(li.attr("data-id"));
            return;
        },
        activeLastOne: function () {
            var size = this.getSizeOfTab();
            if (size > 0) {
                var index = size - 1;
                this.active(index);
            }
        },
        removeScript: function (key) {
            var li = $(".tab-header > li[data-id='" + key + "']", this.$element);
            if (li.length > 0) {
                this.removeLi(li);
            }
        },
        showModifyIcon: function (key, isShow) {
            var li = $(".tab-header > li[data-id='" + key + "']", this.$element);
            if (li.length > 0) {
                if (isShow) {
                    $("span.modify-status", li).text("*");
                    // $("span.modify-status", li).text("*");
                } else {
                    $("span.modify-status", li).text("");
                }
            }
        }
    }
    var methods = {
        init: function (options) {
            // 在每个元素上执行方法
            return this.each(function () {
                var jdDataDevTab = new JdDataDevTab($(this), options);
                $(this).data("jdDataDevTab", jdDataDevTab);
                jdDataDevTab.initJdDataDevTab();
            });
        },
        /**
         * 选中某个
         * @param index
         */
        active: function (index) {
            $(this).data("jdDataDevTab").active(index);
        },

        addTab: function (params) {
            return $(this).data("jdDataDevTab").addTab(params);
        },
        initTabs: function (tabArrays) {
            return $(this).data("jdDataDevTab").initTabs(tabArrays);
        },
        removeTab: function (index) {
            $(this).data("jdDataDevTab").removeIndex(index);
        },
        removeScript: function (key) {
            $(this).data("jdDataDevTab").removeScript(key);
        },
        getAllTab: function () {
            return $(this).data("jdDataDevTab").getAllTab();
        },
        findInfoByParam: function (paramKey, paramValue) {
            return $(this).data("jdDataDevTab").findInfoByParam(paramKey, paramValue);
        },
        unActive: function (li) {
            $(this).data("jdDataDevTab").unActive(li);
        },
        unActiveIndex: function (index) {
            $(this).data("jdDataDevTab").unActiveIndex(index);
        },
        activeLastOne: function () {
            $(this).data("jdDataDevTab").activeLastOne();
        },
        addScriptCallBack: function (key, targetId) {
            $(this).data("jdDataDevTab").addScriptCallBack(key, targetId);
        },
        getSizeOfTab: function () {
            return $(this).data("jdDataDevTab").getSizeOfTab();
        },
        getCurrentActiveTab: function () {
            return $(this).data("jdDataDevTab").getCurrentActiveTab();
        },
        getActiveTabByDataId: function (key) {
            return $(this).data("jdDataDevTab").getActiveTabByDataId(key);
        },
        activeLiByDataId: function (dataId) {
            return $(this).data("jdDataDevTab").activeLiByDataId(dataId);
        },
        getTabInfo: function (key) {
            return $(this).data("jdDataDevTab").getTabInfo(key);
        },
        changeTabInfos: function (key, newKey, params) {
            return $(this).data("jdDataDevTab").changeTabInfos(key, newKey, params);
        },
        hiddenTabPullDown: function () {
            return $(this).data("jdDataDevTab").hiddenTabPullDown();
        },
        showModifyIcon: function (key, isShow) {
            return $(this).data("jdDataDevTab").showModifyIcon(key,isShow);
        }
    };

    $.fn.JdDataDevTab = function () {
        var method = arguments[0];
        if (methods[method]) {
            method = methods[method];
            arguments = Array.prototype.slice.call(arguments, 1);
        } else if (typeof (method) == 'object' || !method) {
            method = methods.init;
        } else {
            $.error('Method ' + method + ' does not exist on JdDataDevTab');
            return this;
        }
        return method.apply(this, arguments);
    }
})(jQuery);

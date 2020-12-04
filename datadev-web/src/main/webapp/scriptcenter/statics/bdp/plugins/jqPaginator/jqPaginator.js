(function ($) {
    'use strict';

    $.jqPaginator = function (el, options) {
        if (!(this instanceof $.jqPaginator)) {
            return new $.jqPaginator(el, options);
        }

        var self = this;

        self.$container = $(el);

        self.$container.data('jqPaginator', self);

        self.init = function () {
            //设置float属性
            self.$container.css("float", "left");
            if (options.first || options.prev || options.next || options.last || options.page) {
                options = $.extend({}, {
                    first: '',
                    prev: '',
                    next: '',
                    last: '',
                    page: ''
                }, options);
            }

            self.options = $.extend({}, $.jqPaginator.defaultOptions, options);

            self.verify();

            self.extendJquery();

            self.render();

            self.fireEvent(this.options.currentPage, 'init');
        };

        self.verify = function () {
            var opts = self.options;
            if (!self.isNumber(opts.totalPages)) {
                throw new Error('[jqPaginator] type error: totalPages');
            }

            if (!self.isNumber(opts.totalCounts)) {
                throw new Error('[jqPaginator] type error: totalCounts');
            }

            if (!self.isNumber(opts.pageSize)) {
                throw new Error('[jqPaginator] type error: pageSize');
            }

            if (!self.isNumber(opts.currentPage)) {
                throw new Error('[jqPaginator] type error: currentPage');
            }

            if (!self.isNumber(opts.visiblePages)) {
                throw new Error('[jqPaginator] type error: visiblePages');
            }

            if (opts.totalPages && opts.totalCounts) {
                throw new Error('[jqPaginator] totalCounts or totalPages is required');
            }

//            if (!opts.totalPages && !opts.totalCounts) {
//                throw new Error('[jqPaginator] totalCounts or totalPages is required');
//            }
//
//            if (!opts.totalPages && opts.totalCounts && !opts.pageSize) {
//                throw new Error('[jqPaginator] pageSize is required');
//            }

            if (opts.totalCounts && opts.pageSize) {
                opts.totalPages = Math.ceil(opts.totalCounts / opts.pageSize);
            }

//            if (opts.currentPage < 1 || opts.currentPage > opts.totalPages) {
//                throw new Error('[jqPaginator] currentPage is incorrect');
//            }

//            if (opts.totalPages < 1) {
//                throw new Error('[jqPaginator] totalPages cannot be less currentPage');
//            }
        };

        self.extendJquery = function () {
            $.fn.jqPaginatorHTML = function (s) {
                return s ? this.before(s).remove() : $('<p>').append(this.eq(0).clone()).html();
            };
        };

        self.render = function () {
            self.renderHtml();
            self.setStatus();
            self.bindEvents();
        };

        self.renderHtml = function () {
            var html = [];

            var pages = self.getPages();
            for (var i = 0, j = pages.length; i < j; i++) {
                html.push(self.buildItem('page', pages[i]));
            }

            self.isEnable('prev') && html.unshift(self.buildItem('prev', self.options.currentPage - 1));
            self.isEnable('first') && html.unshift(self.buildItem('first', 1));
            self.isEnable('statistics') && html.unshift(self.buildItem('statistics'));
            self.isEnable('next') && html.push(self.buildItem('next', self.options.currentPage + 1));
            self.isEnable('last') && html.push(self.buildItem('last', self.options.totalPages));
            //动态设置页数
            self.$container.parent().find('.show-page-total').html(self.options.totalPages);

            if (self.options.wrapper) {
                self.$container.html($(self.options.wrapper).html(html.join('')).jqPaginatorHTML());
            } else {
                self.$container.html(html.join(''));
            }
            if (self.options.goToPages) {
                self.showPage();
            }

            //如果总页数为0隐藏
            if (self.options.totalPages == 0) {
                self.$container.hide();
                self.$container.parent().find(".show-page").hide();
            } else {
                self.$container.show();
                if (self.options.currentPage != self.options.totalPages) {//如果只有一页隐藏
                    self.$container.parent().find(".show-page").show();
                } else if(self.options.currentPage ==1 && self.options.currentPage == self.options.totalPages ){
                    self.$container.parent().find(".show-page").hide();
                    self.$container.hide();
                }
            }
        };

        //显示togoPage
        self.showPage = function () {
            var show_page =
                '<div class="show-page">\
                    <span style="margin-left:10px" class="total">共 <span class="show-page-total">' + self.options.totalPages + '</span> 页 </span><span class="to">到第 <input class="show-page-goto-page" type="text"/> 页</span>\
                    <span class="operation"><button>确定</button></span>\
                </div>';
            if (self.$container.next().length == 0) {
                self.$container.after(show_page);
            }
        };

        self.buildItem = function (type, pageData) {
            var html = self.options[type]
                .replace(/{{page}}/g, pageData)
                .replace(/{{totalPages}}/g, self.options.totalPages)
                .replace(/{{totalCounts}}/g, self.options.totalCounts);

            return $(html).attr({
                'jp-role': type,
                'jp-data': pageData
            }).jqPaginatorHTML();
        };

        self.setStatus = function () {
            var options = self.options;


            if (!self.isEnable('first') || options.currentPage === 1) {
                $('[jp-role=first]', self.$container).addClass(options.disableClass);
            }
            if (!self.isEnable('prev') || options.currentPage === 1) {
                $('[jp-role=prev]', self.$container).addClass(options.disableClass);
            }
            if (!self.isEnable('next') || options.currentPage >= options.totalPages) {
                $('[jp-role=next]', self.$container).addClass(options.disableClass);
            }
            if (!self.isEnable('last') || options.currentPage >= options.totalPages) {
                $('[jp-role=last]', self.$container).addClass(options.disableClass);
            }

            $('[jp-role=page]', self.$container).removeClass(options.activeClass);
            $('[jp-role=page][jp-data=' + options.currentPage + ']', self.$container).addClass(options.activeClass);

            if (!self.options.showFirst) {
                $('[jp-role=first]', self.$container).hide();
            }
            if (!self.options.showLast) {
                $('[jp-role=last]', self.$container).hide();
            }
        };


        self.getPages = function () {
            var pages = [],
                visiblePages = self.options.visiblePages,
                currentPage = self.options.currentPage,
                totalPages = self.options.totalPages;

            if (visiblePages > totalPages) {
                visiblePages = totalPages;
            }

            var half = Math.floor(visiblePages / 2);
            var start = currentPage - half + 1 - visiblePages % 2;
            var end = currentPage + half;

            if (start < 1) {
                start = 1;
                end = visiblePages;
            }
            if (end > totalPages) {
                end = totalPages;
                start = 1 + totalPages - visiblePages;
            }

            var itPage = start;
            while (itPage <= end) {
                pages.push(itPage);
                itPage++;
            }

            return pages;
        };

        self.isNumber = function (value) {
            var type = typeof value;
            return type === 'number' || type === 'undefined';
        };

        self.isEnable = function (type) {
            return self.options[type] && typeof self.options[type] === 'string';
        };

        self.switchPage = function (pageIndex) {
            self.options.currentPage = pageIndex;
            $(".pages .show-page .show-page-goto-page").val(pageIndex);
            self.render();
        };

        self.fireEvent = function (pageIndex, type) {
            return (typeof self.options.onPageChange !== 'function') || (self.options.onPageChange(pageIndex, type) !== false);
        };

        self.callMethod = function (method, options) {
            switch (method) {
                case 'option':
                    self.options = $.extend({}, self.options, options);
                    self.verify();
                    self.render();
                    break;
                case 'destroy':
                    self.$container.empty();
                    self.$container.removeData('jqPaginator');
                    break;
                default :
                    throw new Error('[jqPaginator] method "' + method + '" does not exist');
            }

            return self.$container;
        };

        self.bindEvents = function () {
            var opts = self.options;

            self.$container.off();
            self.$container.on('click', '[jp-role]', function () {
                var $el = $(this);
                if ($el.hasClass(opts.disableClass) || $el.hasClass(opts.activeClass)) {
                    return;
                }

                var pageIndex = +$el.attr('jp-data');
                if (self.fireEvent(pageIndex, 'change')) {
                    self.switchPage(pageIndex);
                }
            });

            self.$container.parent().delegate('.show-page .operation button',"click",function() {
                var goPage = $(".show-page .show-page-goto-page").val();
                //判斷是否是数字类型
                if (!$.isNumeric(goPage)) {
                    $(this).focus();
                } else {
                    goPage = parseInt(goPage);
                    //如果goPage < 1 或者 goPage 大于totalPages
                    if (goPage > self.options.totalPages || goPage < 1) {
                        $(this).focus();
                    } else {
                        //触发事件信息
                        if (self.fireEvent(goPage, 'change')) {
                            self.switchPage(goPage);//交换page选中效果
                        }
                    }
                }
            });
            //gotoPage 事件
            self.$container.parent().delegate('.show-page-goto-page', "keydown", function (e) {
                if (e.keyCode == 13) {
                    var goPage = $(this).val();
                    //判斷是否是数字类型
                    if (!$.isNumeric(goPage)) {
                        $(this).focus();
                    } else {
                        goPage = parseInt(goPage);
                        //如果goPage < 1 或者 goPage 大于totalPages
                        if (goPage > self.options.totalPages || goPage < 1) {
                            $(this).focus();
                        } else {
                            //触发事件信息
                            if (self.fireEvent(goPage, 'change')) {
                                self.switchPage(goPage);//交换page选中效果
                            }
                        }
                    }
                }
            });
        };

        self.init();

        return self.$container;
    };

    $.jqPaginator.defaultOptions = {
        wrapper: '',
        first: '<li class="first"><a href="javascript:;">First</a></li>',
        prev: '<li class="prev"><a href="javascript:;">Previous</a></li>',
        next: '<li class="next"><a href="javascript:;">Next</a></li>',
        last: '<li class="last"><a href="javascript:;">Last</a></li>',
        page: '<li class="page"><a href="javascript:;">{{page}}</a></li>',
        totalPages: 0,
        totalCounts: 0,
        pageSize: 0,
        currentPage: 1,
        visiblePages: 7,
        disableClass: 'disabled',
        activeClass: 'active',
        onPageChange: null,
        goToPages: false,
        showFirst: false,
        showLast: false
    };

    $.fn.jqPaginator = function () {
        var self = this,
            args = Array.prototype.slice.call(arguments);

        if (typeof args[0] === 'string') {
            var $instance = $(self).data('jqPaginator');
            if (!$instance) {
                throw new Error('[jqPaginator] the element is not instantiated');
            } else {
                return $instance.callMethod(args[0], args[1]);
            }
        } else {
            return new $.jqPaginator(this, args[0]);
        }
    };

})(jQuery);
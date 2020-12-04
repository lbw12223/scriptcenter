define("ace/ext/searchbox", ["require", "exports", "module", "ace/lib/dom", "ace/lib/lang", "ace/lib/event", "ace/keyboard/hash_handler", "ace/lib/keys"], function (require, exports, module) {
    "use strict";

    var dom = require("../lib/dom");
    var lang = require("../lib/lang");
    var event = require("../lib/event");
    var searchboxCss = "\
.ace_search {\
background-color: #363A40;\
color: #666;\
overflow: hidden;\
margin: 0;\
padding: 10px;\
position: absolute;\
top: 0;\
z-index: 99;\
white-space: normal;\
box-shadow: 0 10px 20px 0 rgba(0,0,0,0.50);\
margin-right:5px\
}\
.ace_search.left {\
border-left: 0 none;\
border-radius: 0px 0px 5px 0px;\
left: 0;\
}\
.ace_search.right {\
border-right: 0 none;\
right: 0;\
width: 363px;\
}\
.ace_search_form, .ace_replace_form {\
margin: 0 20px 4px 0;\
overflow: hidden;\
}\
.ace_replace_form {\
margin-right: 0;\
}\
.ace_search_form.ace_nomatch {\
}\
.ace_search_field {\
border: 1px solid #44494F;\
background-color: #272A2F;\
font-family: MicrosoftYaHei;\
color: #A3A9B2;\
box-sizing: border-box!important;\
outline: 0;\
padding: 0;\
font-size: 12px;\
letter-spacing: 0;\
margin-right: 5px;\
line-height: inherit;\
width:200px;\
height:32px;\
padding: 0 6px;\
vertical-align: top;\
}\
.ace_search_field:focus{\
  border-color: #44494F;\
}\
.ace_searchbtn {\
line-height: inherit;\
display: inline-block;\
cursor: pointer;\
margin-left: 5px;\
position: relative;\
color: #A3A9B2;\
width:32px;\
height:32px;\
background: #272A2F;\
border-radius: 3px;\
font-family: MicrosoftYaHe;\
font-size: 13px;\
}\
.ace_searchbtn:last-child {\
    line-height: 29px;\
    text-align: center;\
}\
.ace_searchbtn:disabled {\
background: none;\
cursor: default;\
}\
.ace_searchbtn:hover {\
background: #1E2024;\
box-shadow: 0 0 8px 0 rgba(0,0,0,0.30);\
color:#FFFFFF;\
}\
.ace_searchbtn.prev, .ace_searchbtn.next {\
top:-9px\
}\
.ace_searchbtn:hover:after{\
border-color:white !important;\
}\
.ace_searchbtn.prev:after{\
content: \"\";\
border: solid 2px #A3A9B2;\
width: 10px;\
height: 10px;\
border-width:  2px 0 0 2px;\
display:inline-block;\
transform: rotate(-45deg);\
position:relative;\
top:12px;\
left:13px;\
}\
.ace_searchbtn.next:after {\
content: \"\";\
border: solid 2px #888;\
width: 10px;\
height: 10px;\
border-width:  2px 0 0 2px;\
display:inline-block;\
transform: rotate(-45deg);\
position:relative;\
top:12px;\
left:9px;\
}\
.ace_searchbtn.next:after {\
border-width: 0 2px 2px 0 ;\
}\
.ace_searchbtn_close {\
background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAcCAYAAABRVo5BAAAAZ0lEQVR42u2SUQrAMAhDvazn8OjZBilCkYVVxiis8H4CT0VrAJb4WHT3C5xU2a2IQZXJjiQIRMdkEoJ5Q2yMqpfDIo+XY4k6h+YXOyKqTIj5REaxloNAd0xiKmAtsTHqW8sR2W5f7gCu5nWFUpVjZwAAAABJRU5ErkJggg==) no-repeat 50% 0;\
border-radius: 50%;\
border: 0 none;\
color: #656565;\
cursor: pointer;\
font: 16px/16px Arial;\
padding: 0;\
height: 14px;\
width: 14px;\
top: 9px;\
right: 7px;\
position: absolute;\
}\
.ace_searchbtn_close:hover {\
}\
.ace_replace_form  .ace_searchbtn:nth-child(2){\
width:70px;\
height:32px;\
line-height: 32px;\
position: relative;\
text-align: center;\
}\
.ace_replace_form  .ace_searchbtn:nth-child(3){\
width:58px;\
height:32px;\
line-height: 32px;\
}\
.ace_button{\
cursor: pointer;\
-webkit-user-select: none;\
-moz-user-select: none;\
-o-user-select: none;\
-ms-user-select: none;\
user-select: none;\
overflow: hidden;\
opacity: 1;\
border: 1px solid #44494F;\
box-sizing:    border-box!important;\
color: #A3A9B2;\
width: 32px;\
height:32px;\
background:  #363A40;\
text-align: center;\
line-height: 30px;\
font-size: 16px;\
display: inline-block;\
border-left: none;\
}\
.ace_button:first-child{\
border:none;\
font-size: 22px;\
border-radius: 3px;\
background: #272A2F;\
}\
.ace_button:nth-child(3){\
border-radius: 3px 0 0 3px;\
border-left: 1px solid #44494F;\
}\
.ace_button:last-child{\
border-radius: 0 3px 3px 0;\
}\
.ace_button:first-child:hover {\
background: #1E2024;\
box-shadow: 0 0 8px 0 rgba(30,32,36,0.50);\
color:#FFFFFF\
}\
.ace_button:hover {\
background: #272A2F;\
color:#FFFFFF\
}\
.ace_button:active {\
}\
.ace_button.checked {\
color: #FFFFFF;\
background: #272A2F;\n\
opacity:1;\
}\
.ace_search_options{\
margin-bottom: 3px;\
text-align: right;\
-webkit-user-select: none;\
-moz-user-select: none;\
-o-user-select: none;\
-ms-user-select: none;\
user-select: none;\
clear: both;\
margin-top: 10px;\
}\
.ace_search_counter {\
float: left;\
margin-left: 10px;\
font-family: MicrosoftYaHei;\
font-size: 12px;\
color: #A3A9B2;\
letter-spacing: 0;\
position: relative;\
top:4px;\
}";
    var HashHandler = require("../keyboard/hash_handler").HashHandler;
    var keyUtil = require("../lib/keys");

    var MAX_COUNT = 999;

    dom.importCssString(searchboxCss, "ace_searchbox");

    var html = '<div class="ace_search right">\
    <span action="hide" class="ace_searchbtn_close"></span>\
    <div class="ace_search_form" style="323px">\
        <input class="ace_search_field" placeholder="查询内容" spellcheck="false"></input>\
        <span action="findPrev" class="ace_searchbtn prev"></span>\
        <span action="findNext" class="ace_searchbtn next"></span>\
        <span action="findAll" class="ace_searchbtn" >全部</span>\
    </div>\
    <div class="ace_replace_form">\
        <input class="ace_search_field" placeholder="替换为" spellcheck="false"></input>\
        <span action="replaceAndFindNext" class="ace_searchbtn">替换</span>\
        <span action="replaceAll" class="ace_searchbtn">全部</span>\
    </div>\
    <div class="ace_search_options">\
        <span action="toggleReplace" class="ace_button" title="切换替换模式"\
            style="float:left;margin-top:-2px;padding:0 5px;">+</span>\
        <span class="ace_search_counter"></span>\
        <span action="toggleRegexpMode" class="ace_button" title="使用正则表达式查询">.*</span>\
        <span action="toggleCaseSensitive" class="ace_button" title="区分大小写">Aa</span>\
        <span action="toggleWholeWords" class="ace_button" title="按整词查询">\\b</span>\
        <span action="searchInSelection" class="ace_button" title="在选中范围内查询">S</span>\
    </div>\
</div>'.replace(/> +/g, ">");

    var SearchBox = function (editor, range, showReplaceForm) {
        var div = dom.createElement("div");
        div.innerHTML = html;
        this.element = div.firstChild;

        this.setSession = this.setSession.bind(this);

        this.$init();
        this.setEditor(editor);
    };

    (function () {
        this.setEditor = function (editor) {
            editor.searchBox = this;
            editor.renderer.scroller.appendChild(this.element);
            this.editor = editor;
        };

        this.setSession = function (e) {
            this.searchRange = null;
            this.$syncOptions(true);
        };

        this.$initElements = function (sb) {
            this.searchBox = sb.querySelector(".ace_search_form");
            this.replaceBox = sb.querySelector(".ace_replace_form");
            this.searchOption = sb.querySelector("[action=searchInSelection]");
            this.replaceOption = sb.querySelector("[action=toggleReplace]");
            this.regExpOption = sb.querySelector("[action=toggleRegexpMode]");
            this.caseSensitiveOption = sb.querySelector("[action=toggleCaseSensitive]");
            this.wholeWordOption = sb.querySelector("[action=toggleWholeWords]");
            this.searchInput = this.searchBox.querySelector(".ace_search_field");
            this.replaceInput = this.replaceBox.querySelector(".ace_search_field");
            this.searchCounter = sb.querySelector(".ace_search_counter");
        };

        this.$init = function () {
            var sb = this.element;

            this.$initElements(sb);

            var _this = this;
            event.addListener(sb, "mousedown", function (e) {
                setTimeout(function () {
                    _this.activeInput.focus();
                }, 0);
                event.stopPropagation(e);
            });
            event.addListener(sb, "click", function (e) {
                var t = e.target || e.srcElement;
                var action = t.getAttribute("action");
                if (action && _this[action])
                    _this[action]();
                else if (_this.$searchBarKb.commands[action])
                    _this.$searchBarKb.commands[action].exec(_this);
                event.stopPropagation(e);
            });

            event.addCommandKeyListener(sb, function (e, hashId, keyCode) {
                var keyString = keyUtil.keyCodeToString(keyCode);
                var command = _this.$searchBarKb.findKeyCommand(hashId, keyString);
                if (command && command.exec) {
                    command.exec(_this);
                    event.stopEvent(e);
                }
            });

            this.$onChange = lang.delayedCall(function () {
                _this.find(false, false);
            });

            event.addListener(this.searchInput, "input", function () {
                _this.$onChange.schedule(20);
            });
            event.addListener(this.searchInput, "focus", function () {
                _this.activeInput = _this.searchInput;
                _this.searchInput.value && _this.highlight();
            });
            event.addListener(this.replaceInput, "focus", function () {
                _this.activeInput = _this.replaceInput;
                _this.searchInput.value && _this.highlight();
            });
        };
        this.$closeSearchBarKb = new HashHandler([{
            bindKey: "Esc",
            name: "closeSearchBar",
            exec: function (editor) {
                editor.searchBox.hide();
            }
        }]);
        this.$searchBarKb = new HashHandler();
        this.$searchBarKb.bindKeys({
            "Ctrl-f|Command-f": function (sb) {
                var isReplace = sb.isReplace = !sb.isReplace;
                sb.replaceBox.style.display = isReplace ? "" : "none";
                sb.replaceOption.checked = false;
                sb.$syncOptions();
                sb.searchInput.focus();
            },
            "Ctrl-H|Command-Option-F": function (sb) {
                sb.replaceOption.checked = true;
                sb.$syncOptions();
                sb.replaceInput.focus();
            },
            "Ctrl-G|Command-G": function (sb) {
                sb.findNext();
            },
            "Ctrl-Shift-G|Command-Shift-G": function (sb) {
                sb.findPrev();
            },
            "esc": function (sb) {
                setTimeout(function () {
                    sb.hide();
                });
            },
            "Return": function (sb) {
                if (sb.activeInput == sb.replaceInput)
                    sb.replace();
                sb.findNext();
            },
            "Shift-Return": function (sb) {
                if (sb.activeInput == sb.replaceInput)
                    sb.replace();
                sb.findPrev();
            },
            "Alt-Return": function (sb) {
                if (sb.activeInput == sb.replaceInput)
                    sb.replaceAll();
                sb.findAll();
            },
            "Tab": function (sb) {
                (sb.activeInput == sb.replaceInput ? sb.searchInput : sb.replaceInput).focus();
            }
        });

        this.$searchBarKb.addCommands([{
            name: "toggleRegexpMode",
            bindKey: {win: "Alt-R|Alt-/", mac: "Ctrl-Alt-R|Ctrl-Alt-/"},
            exec: function (sb) {
                sb.regExpOption.checked = !sb.regExpOption.checked;
                sb.$syncOptions();
            }
        }, {
            name: "toggleCaseSensitive",
            bindKey: {win: "Alt-C|Alt-I", mac: "Ctrl-Alt-R|Ctrl-Alt-I"},
            exec: function (sb) {
                sb.caseSensitiveOption.checked = !sb.caseSensitiveOption.checked;
                sb.$syncOptions();
            }
        }, {
            name: "toggleWholeWords",
            bindKey: {win: "Alt-B|Alt-W", mac: "Ctrl-Alt-B|Ctrl-Alt-W"},
            exec: function (sb) {
                sb.wholeWordOption.checked = !sb.wholeWordOption.checked;
                sb.$syncOptions();
            }
        }, {
            name: "toggleReplace",
            exec: function (sb) {
                sb.replaceOption.checked = !sb.replaceOption.checked;
                sb.$syncOptions();
            }
        }, {
            name: "searchInSelection",
            exec: function (sb) {
                sb.searchOption.checked = !sb.searchRange;
                sb.setSearchRange(sb.searchOption.checked && sb.editor.getSelectionRange());
                sb.$syncOptions();
            }
        }]);

        this.setSearchRange = function (range) {
            this.searchRange = range;
            if (range) {
                this.searchRangeMarker = this.editor.session.addMarker(range, "ace_active-line");
            } else if (this.searchRangeMarker) {
                this.editor.session.removeMarker(this.searchRangeMarker);
                this.searchRangeMarker = null;
            }
        };

        this.$syncOptions = function (preventScroll) {
            dom.setCssClass(this.replaceOption, "checked", this.searchRange);
            dom.setCssClass(this.searchOption, "checked", this.searchOption.checked);
            this.replaceOption.textContent = this.replaceOption.checked ? "-" : "+";
            dom.setCssClass(this.regExpOption, "checked", this.regExpOption.checked);
            dom.setCssClass(this.wholeWordOption, "checked", this.wholeWordOption.checked);
            dom.setCssClass(this.caseSensitiveOption, "checked", this.caseSensitiveOption.checked);
            this.replaceBox.style.display = this.replaceOption.checked ? "" : "none";
            this.find(false, false, preventScroll);
        };

        this.highlight = function (re) {
            this.editor.session.highlight(re || this.editor.$search.$options.re);
            this.editor.renderer.updateBackMarkers();
        };
        this.find = function (skipCurrent, backwards, preventScroll) {
            var range = this.editor.find(this.searchInput.value, {
                skipCurrent: skipCurrent,
                backwards: backwards,
                wrap: true,
                regExp: this.regExpOption.checked,
                caseSensitive: this.caseSensitiveOption.checked,
                wholeWord: this.wholeWordOption.checked,
                preventScroll: preventScroll,
                range: this.searchRange
            });
            var noMatch = !range && this.searchInput.value;
            dom.setCssClass(this.searchBox, "ace_nomatch", noMatch);
            this.editor._emit("findSearchBox", {match: !noMatch});
            this.highlight();
            this.updateCounter();
        };
        this.updateCounter = function () {
            var editor = this.editor;
            var regex = editor.$search.$options.re;
            var all = 0;
            var before = 0;
            if (regex) {
                var value = this.searchRange
                    ? editor.session.getTextRange(this.searchRange)
                    : editor.getValue();

                var offset = editor.session.doc.positionToIndex(editor.selection.anchor);
                if (this.searchRange)
                    offset -= editor.session.doc.positionToIndex(this.searchRange.start);

                var last = regex.lastIndex = 0;
                var m;
                while ((m = regex.exec(value))) {
                    all++;
                    last = m.index;
                    if (last <= offset)
                        before++;
                    if (all > MAX_COUNT)
                        break;
                    if (!m[0]) {
                        regex.lastIndex = last += 1;
                        if (last >= value.length)
                            break;
                    }
                }
            }
            this.searchCounter.textContent = before + " / " + (all > MAX_COUNT ? MAX_COUNT + "+" : all);
        };
        this.findNext = function () {
            this.find(true, false);
        };
        this.findPrev = function () {
            this.find(true, true);
        };
        this.findAll = function () {
            var range = this.editor.findAll(this.searchInput.value, {
                regExp: this.regExpOption.checked,
                caseSensitive: this.caseSensitiveOption.checked,
                wholeWord: this.wholeWordOption.checked
            });
            var noMatch = !range && this.searchInput.value;
            dom.setCssClass(this.searchBox, "ace_nomatch", noMatch);
            this.editor._emit("findSearchBox", {match: !noMatch});
            this.highlight();
            // this.hide();
        };
        this.replace = function () {
            if (!this.editor.getReadOnly())
                this.editor.replace(this.replaceInput.value);
        };
        this.replaceAndFindNext = function () {
            if (!this.editor.getReadOnly()) {
                this.editor.replace(this.replaceInput.value);
                this.findNext();
            }
        };
        this.replaceAll = function () {
            if (!this.editor.getReadOnly())
                this.editor.replaceAll(this.replaceInput.value);
        };

        this.hide = function () {
            this.active = false;
            this.setSearchRange(null);
            this.editor.off("changeSession", this.setSession);

            this.element.style.display = "none";
            this.editor.keyBinding.removeKeyboardHandler(this.$closeSearchBarKb);
            this.editor.focus();
        };
        this.show = function (value, isReplace) {
            this.active = true;
            this.editor.on("changeSession", this.setSession);
            this.element.style.display = "";
            this.replaceOption.checked = isReplace;

            if (value)
                this.searchInput.value = value;

            this.searchInput.focus();
            this.searchInput.select();

            this.editor.keyBinding.addKeyboardHandler(this.$closeSearchBarKb);

            this.$syncOptions(true);
        };

        this.isFocused = function () {
            var el = document.activeElement;
            return el == this.searchInput || el == this.replaceInput;
        };
    }).call(SearchBox.prototype);

    exports.SearchBox = SearchBox;

    exports.Search = function (editor, isReplace) {
        var sb = editor.searchBox || new SearchBox(editor);
        sb.show(editor.session.getTextRange(), isReplace);
    };

});
(function () {
    window.require(["ace/ext/searchbox"], function () {
    });
})();
            
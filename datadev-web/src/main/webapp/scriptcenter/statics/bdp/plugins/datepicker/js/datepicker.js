/**
 *
 * Date picker
 * Author: Stefan Petre www.eyecon.ro
 *
 * Dual licensed under the MIT and GPL licenses
 */
//Date.prototype.getDayIndex = function(){
//    return parseInt('6012345'.charAt(this.getDay()));
//};
//Date.prototype.getWeekNumber = function(){
//    var date = new Date(this.getTime()), splitDay = 2, year = date.getFullYear(), firstDate = new Date(year, 0, 1), firstDayInWeek = firstDate.getDayIndex(),
//        firstDateOfLastYear = new Date(year-1, 0, 1),  firstDayInWeekOfLastYear = firstDateOfLastYear.getDayIndex(),
//        ifNowWeekOfLastYear = date.getDayIndex()>=firstDayInWeek&&((date-firstDate)/864e5)<7&&firstDate.getDayIndex()>splitDay,
//        spcialDayNum = ifNowWeekOfLastYear?firstDayInWeekOfLastYear:firstDayInWeek;
//    var weekNum = Math.ceil((date.getTime()-(ifNowWeekOfLastYear?firstDateOfLastYear:firstDate).getTime()+(spcialDayNum==6?0:spcialDayNum+1)*864e5)/6048e5);
//    return (spcialDayNum<=splitDay||spcialDayNum==6)?weekNum:weekNum-1;
//}
/**
 * 日期控件对象定义
 * @param _containerId
 * @param _options
 * @returns {DatePicker}
 * @constructor
 */
var DatePicker = function(_containerId, _options){
    if(this instanceof DatePicker){
        this.containerId = _containerId;
        this.timeInputInt=["00","00"];
        this.options = _options;
        this.renderDatepicker();
    }else{
        return new DatePicker(_containerId, _options);
    }
};
DatePicker.prototype = (function(){
    var _KEY_ = {
            ID: "id",
            NAME: "name",
            LABEL: "label",
            RAPID: "rapid",
            RAPID_SEL: "rapidSel",
            DIMENSION_SEL: "dimensionSel",
            TYPE: "type",
            STYLE: "style",
            PATTERN: "pattern",
            VIEW: "view",
            RANGE: "range",
            HIDDEN_DOM: "hiddenDom",
            DATE_SEG: "dateSeg",
            CALENDARS: "calendars",
            CUSTOM_CHANGE: "customChange",
            CUSTOM_QUERY: "customQuery",
            PARAM: "param",
            IF_VISIBLE: "ifVisible",
            FILTER_NAME: "filterName",
            IF_NOTE: "ifNote",
            DATE: "date",
            MIN_DATE: "minDate",
            MAX_DATE: "maxDate",
            IF_SEPARATE: "ifSeparate",
            IF_SEPARATE_PART: "ifSeparatePart",
            IF_DROPDOWN_DISABLED: "ifDropDownDisabled",
            IF_LEFT_VISIBLE:"ifLeftVisible",
            IF_HOVER_SHOW:"ifHoverShow",
            IF_WIDTH_AUTO:"ifWidthAuto",
            SINGLE: "single",
            IF_FINAL_CONFIRM: "ifFinalConfirm",
            TIME_MODE: "timeMode",
            NO_BTN: "noBtn",
            VDAY: "day",
            VMONTH: "month",
            VWEEK: "week",
            SIMPLE: "simple",
            COMPLEX: "complex",
            VNMONTH: "!month",           //forbid month
            VNWEEK: "!week",           //forbid week
            VODAY: "!month!week",      //only day
            PREFIX_SEASON: '-90',
            PREFIX_MONTH: '-95',
            PREFIX_WEEK: '-99',
            NEXT_DAY: "后一天",
            PREV_DAY: "前一天",
            NEXT_MONTH: "下一月",
            PREV_MONTH: "上一月",
            NEXT_WEEK: "下一周",
            PREV_WEEK: "上一周",
            RESET_DATE: "重置",
            DIMENSION_DATE: {
                "day": "日/周/月",
                "!month!week": "日",
                "week": "周",
                "month": "月",
                "!week": "日/月",
                "!month": "日/周"
            }
        },_KEY_RAPID_ = {
            CURWEEK: "本周",
            PREVWEEK: "上周",
            CURMONTH: "本月",
            PREVMONTH: "上月",
            PREV1: "昨天",
            PREV2: "前天",
            PREV3:"近3天",
            PREV7:"近7天",
            PREV15:"近15天",
            PREV30:"近30天",
            PREV90:"近90天",
            PREVSEASON: "上季",
            PREVMONTH3: "最近3月",
            PREVMONTH6: "最近6月",
            PREVMONTH12: "最近12月",
            PREVHALFYEAR: "近半年",
            PREVYEAR: "近一年"
        },
        _KEY_CACHE_ = {
            CACHE_RAPID: '_cache_rapid_',
            CACHE_SUPER_RAPID: '_cache_super_rapid_',
            /* 需要后缀ID区分双控件 START */
            CACHE_DIMENSION: '_cache_dimension_',
            CACHE_PICKER: '_cache_picker_',
            CACHE_WIDTH_CODE: '_cache_width_code_',
            CACHE_FINAL_DATE: '_cache_final_date_',
            CACHE_FINAL_DATESEG: '_cache_final_dateseg_',
            CACHE_FINAL_FIELD_SHOW: '_cache_final_field_show_',
            CACHE_LAST_DATE: '_cache_last_date_',
            CACHE_LAST_DATESEG: '_cache_last_dateseg_',
            CACHE_LAST_FIELD_SHOW: '_cache_last_field_show_',
            CACHE_TIME: '_cache_time_',
            TIME_VALUE:'_time_value_'
            /* 需要后缀ID区分双控件 END */
        },
        cache = {
            mem: {},
            add: function(_kv, _g){
                var mem = this.mem;
                if(!!_g){
                    $.extend(mem[_g], _kv);
                }else{
                    $.extend(mem, _kv);
                }
            },
            set: function(_k, _v, _g){
                var mem = this.mem;
                if(!!_g){
                    mem[_g] = !!mem[_g]?mem[_g]:{};
                    mem[_g][_k] = _v;
                }else{
                    mem[_k] = _v;
                }
            },
            get: function(_k, _g){
                var mem = this.mem;
                if(!!_g){
                    if(!mem[_g]){
                        mem[_g] = {};
                    }
                    return mem[_g][_k];
                }else{
                    return mem[_k];
                }
            },
            remove: function(_k, _g){
                var mem = this.mem;
                if(_g){
                    delete mem[_g][_k];
                }else{
                    delete mem[_k];
                }
            },
            clr: function(_g){
                var mem = this.mem;
                if(!!_g){
                    mem[_g] = {};
                }else{
                    mem = {};
                }
            }
        };
    $.extend(_KEY_, _KEY_RAPID_,_KEY_CACHE_);       //_KEY_集成_KEY_RAPID_,_KEY_CACHE_
    var tmpl = (function (cache, $) {
        return function (str, data) {
            var fn = !/\s/.test(str)
                ? cache[str] = cache[str]
                || tmpl(document.getElementById(str).innerHTML)

                : function (data) {
                var i, variable = [$], value = [[]];
                for (i in data) {
                    variable.push(i);
                    value.push(data[i]);
                };
                return (new Function(variable, fn.$))
                    .apply(data, value).join("");
            };

            fn.$ = fn.$ || $ + ".push('"
                + str.replace(/\\/g, "\\\\")
                    .replace(/[\r\t\n]/g, " ")
                    .split("<%").join("\t")
                    .replace(/((^|%>)[^\t]*)'/g, "$1\r")
                    .replace(/\t=(.*?)%>/g, "',$1,'")
                    .split("\t").join("');")
                    .split("%>").join($ + ".push('")
                    .split("\r").join("\\'")
                + "');return " + $;

            return data ? fn(data) : fn;
        }
    })({}, '$' + (+ new Date));
    var core = function () {
        var	ids = {},today = new Date(),
            views = {
                year: 'datepickerViewYears',
                month: 'datepickerViewMonths',    //datepickerViewMonths
                day: 'datepickerViewDays',
                week: 'datepickerViewDays',
                "!week": 'datepickerViewDays',
                "!month": 'datepickerViewDays',
                "!month!week": 'datepickerViewDays'
            },
            tpl = {
                wrapper: '<div class="datepicker"><div class="datepickerBorderT" /><div class="datepickerBorderB" /><div class="datepickerBorderL" /><div class="datepickerBorderR" /><div class="datepickerBorderTL" /><div class="datepickerBorderTR" /><div class="datepickerBorderBL" /><div class="datepickerBorderBR" /><div class="datepickerContainer"><table cellspacing="0" cellpadding="0"><tbody><tr></tr></tbody></table></div></div>',
                head: [
                    '<td>',
                    '<table cellspacing="0" cellpadding="0">',
                    '<thead>',
                    '<tr>',
                    '<th class="datepickerGoPrev"><a href="javascript:;"><span><%=prev%></span></a></th>',
                    '<th colspan="6" class="datepickerMonth"><a href="javascript:;"><span></span></a></th>',
                    '<th class="datepickerGoNext"><a href="javascript:;"><span><%=next%></span></a></th>',
                    '</tr>',
                    '<tr class="datepickerDoW">',
                    '<th class="datepickerWeek"><span><%=week%></span></th>',
                    '<th><span><%=day1%></span></th>',
                    '<th><span><%=day2%></span></th>',
                    '<th><span><%=day3%></span></th>',
                    '<th><span><%=day4%></span></th>',
                    '<th><span><%=day5%></span></th>',
                    '<th><span><%=day6%></span></th>',
                    '<th><span><%=day7%></span></th>',
                    '</tr>',
                    '</thead>',
                    '</table></td>'
                ],
                space : '<td class="datepickerSpace"><div></div></td>',
                days: [
                    '<tbody class="datepickerDays">',
                    '<tr>',
                    '<th class="datepickerWeek <%=weeks[0].classname%>"><a href="javascript:;" class="week_range"><span><%=weeks[0].week%></span></a></th>',
                    '<td class="<%=weeks[0].days[0].classname%>"><a class="hand"><span val=<%=weeks[0].week*7%>><%=weeks[0].days[0].text%></span></a></td>',
                    '<td class="<%=weeks[0].days[1].classname%>"><a class="hand"><span val=<%=weeks[0].week*7+1%>><%=weeks[0].days[1].text%></span></a></td>',
                    '<td class="<%=weeks[0].days[2].classname%>"><a class="hand"><span val=<%=weeks[0].week*7+2%>><%=weeks[0].days[2].text%></span></a></td>',
                    '<td class="<%=weeks[0].days[3].classname%>"><a class="hand"><span val=<%=weeks[0].week*7+3%>><%=weeks[0].days[3].text%></span></a></td>',
                    '<td class="<%=weeks[0].days[4].classname%>"><a class="hand"><span val=<%=weeks[0].week*7+4%>><%=weeks[0].days[4].text%></span></a></td>',
                    '<td class="<%=weeks[0].days[5].classname%>"><a class="hand"><span val=<%=weeks[0].week*7+5%>><%=weeks[0].days[5].text%></span></a></td>',
                    '<td class="<%=weeks[0].days[6].classname%>"><a class="hand"><span val=<%=weeks[0].week*7+6%>><%=weeks[0].days[6].text%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<th class="datepickerWeek <%=weeks[1].classname%>"><a href="javascript:;" class="week_range"><span><%=weeks[1].week%></span></a></th>',
                    '<td class="<%=weeks[1].days[0].classname%>"><a class="hand"><span val=<%=weeks[1].week*7%>><%=weeks[1].days[0].text%></span></a></td>',
                    '<td class="<%=weeks[1].days[1].classname%>"><a class="hand"><span val=<%=weeks[1].week*7+1%>><%=weeks[1].days[1].text%></span></a></td>',
                    '<td class="<%=weeks[1].days[2].classname%>"><a class="hand"><span val=<%=weeks[1].week*7+2%>><%=weeks[1].days[2].text%></span></a></td>',
                    '<td class="<%=weeks[1].days[3].classname%>"><a class="hand"><span val=<%=weeks[1].week*7+3%>><%=weeks[1].days[3].text%></span></a></td>',
                    '<td class="<%=weeks[1].days[4].classname%>"><a class="hand"><span val=<%=weeks[1].week*7+4%>><%=weeks[1].days[4].text%></span></a></td>',
                    '<td class="<%=weeks[1].days[5].classname%>"><a class="hand"><span val=<%=weeks[1].week*7+5%>><%=weeks[1].days[5].text%></span></a></td>',
                    '<td class="<%=weeks[1].days[6].classname%>"><a class="hand"><span val=<%=weeks[1].week*7+6%>><%=weeks[1].days[6].text%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<th class="datepickerWeek <%=weeks[2].classname%>"><a href="javascript:;" class="week_range"><span><%=weeks[2].week%></span></a></th>',
                    '<td class="<%=weeks[2].days[0].classname%>"><a class="hand"><span val=<%=weeks[2].week*7%>><%=weeks[2].days[0].text%></span></a></td>',
                    '<td class="<%=weeks[2].days[1].classname%>"><a class="hand"><span val=<%=weeks[2].week*7+1%>><%=weeks[2].days[1].text%></span></a></td>',
                    '<td class="<%=weeks[2].days[2].classname%>"><a class="hand"><span val=<%=weeks[2].week*7+2%>><%=weeks[2].days[2].text%></span></a></td>',
                    '<td class="<%=weeks[2].days[3].classname%>"><a class="hand"><span val=<%=weeks[2].week*7+3%>><%=weeks[2].days[3].text%></span></a></td>',
                    '<td class="<%=weeks[2].days[4].classname%>"><a class="hand"><span val=<%=weeks[2].week*7+4%>><%=weeks[2].days[4].text%></span></a></td>',
                    '<td class="<%=weeks[2].days[5].classname%>"><a class="hand"><span val=<%=weeks[2].week*7+5%>><%=weeks[2].days[5].text%></span></a></td>',
                    '<td class="<%=weeks[2].days[6].classname%>"><a class="hand"><span val=<%=weeks[2].week*7+6%>><%=weeks[2].days[6].text%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<th class="datepickerWeek <%=weeks[3].classname%>"><a href="javascript:;" class="week_range"><span><%=weeks[3].week%></span></a></th>',
                    '<td class="<%=weeks[3].days[0].classname%>"><a class="hand"><span val=<%=weeks[3].week*7%>><%=weeks[3].days[0].text%></span></a></td>',
                    '<td class="<%=weeks[3].days[1].classname%>"><a class="hand"><span val=<%=weeks[3].week*7+1%>><%=weeks[3].days[1].text%></span></a></td>',
                    '<td class="<%=weeks[3].days[2].classname%>"><a class="hand"><span val=<%=weeks[3].week*7+2%>><%=weeks[3].days[2].text%></span></a></td>',
                    '<td class="<%=weeks[3].days[3].classname%>"><a class="hand"><span val=<%=weeks[3].week*7+3%>><%=weeks[3].days[3].text%></span></a></td>',
                    '<td class="<%=weeks[3].days[4].classname%>"><a class="hand"><span val=<%=weeks[3].week*7+4%>><%=weeks[3].days[4].text%></span></a></td>',
                    '<td class="<%=weeks[3].days[5].classname%>"><a class="hand"><span val=<%=weeks[3].week*7+5%>><%=weeks[3].days[5].text%></span></a></td>',
                    '<td class="<%=weeks[3].days[6].classname%>"><a class="hand"><span val=<%=weeks[3].week*7+6%>><%=weeks[3].days[6].text%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<th class="datepickerWeek <%=weeks[4].classname%>"><a href="javascript:;" class="week_range"><span><%=weeks[4].week%></span></a></th>',
                    '<td class="<%=weeks[4].days[0].classname%>"><a class="hand"><span val=<%=weeks[4].week*7%>><%=weeks[4].days[0].text%></span></a></td>',
                    '<td class="<%=weeks[4].days[1].classname%>"><a class="hand"><span val=<%=weeks[4].week*7+1%>><%=weeks[4].days[1].text%></span></a></td>',
                    '<td class="<%=weeks[4].days[2].classname%>"><a class="hand"><span val=<%=weeks[4].week*7+2%>><%=weeks[4].days[2].text%></span></a></td>',
                    '<td class="<%=weeks[4].days[3].classname%>"><a class="hand"><span val=<%=weeks[4].week*7+3%>><%=weeks[4].days[3].text%></span></a></td>',
                    '<td class="<%=weeks[4].days[4].classname%>"><a class="hand"><span val=<%=weeks[4].week*7+4%>><%=weeks[4].days[4].text%></span></a></td>',
                    '<td class="<%=weeks[4].days[5].classname%>"><a class="hand"><span val=<%=weeks[4].week*7+5%>><%=weeks[4].days[5].text%></span></a></td>',
                    '<td class="<%=weeks[4].days[6].classname%>"><a class="hand"><span val=<%=weeks[4].week*7+6%>><%=weeks[4].days[6].text%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<th class="datepickerWeek <%=weeks[5].classname%>"><a href="javascript:;" class="week_range"><span><%=weeks[5].week%></span></a></th>',
                    '<td class="<%=weeks[5].days[0].classname%>"><a class="hand"><span val=<%=weeks[5].week*7%>><%=weeks[5].days[0].text%></span></a></td>',
                    '<td class="<%=weeks[5].days[1].classname%>"><a class="hand"><span val=<%=weeks[5].week*7+1%>><%=weeks[5].days[1].text%></span></a></td>',
                    '<td class="<%=weeks[5].days[2].classname%>"><a class="hand"><span val=<%=weeks[5].week*7+2%>><%=weeks[5].days[2].text%></span></a></td>',
                    '<td class="<%=weeks[5].days[3].classname%>"><a class="hand"><span val=<%=weeks[5].week*7+3%>><%=weeks[5].days[3].text%></span></a></td>',
                    '<td class="<%=weeks[5].days[4].classname%>"><a class="hand"><span val=<%=weeks[5].week*7+4%>><%=weeks[5].days[4].text%></span></a></td>',
                    '<td class="<%=weeks[5].days[5].classname%>"><a class="hand"><span val=<%=weeks[5].week*7+5%>><%=weeks[5].days[5].text%></span></a></td>',
                    '<td class="<%=weeks[5].days[6].classname%>"><a class="hand"><span val=<%=weeks[5].week*7+6%>><%=weeks[5].days[6].text%></span></a></td>',
                    '</tr>',
                    '</tbody>'
                ],
                months: [
                    '<tbody class="<%=className%>">',
                    '<tr>',
                    '<td colspan="2" class="<%=months[0].classname%>"><a href="javascript:;"><span><%=data[0]%></span></a></td>',
                    '<td colspan="2" class="<%=months[1].classname%>"><a href="javascript:;"><span><%=data[1]%></span></a></td>',
                    '<td colspan="2" class="<%=months[2].classname%>"><a href="javascript:;"><span><%=data[2]%></span></a></td>',
                    '<td colspan="2" class="<%=months[3].classname%>"><a href="javascript:;"><span><%=data[3]%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<td colspan="2" class="<%=months[4].classname%>"><a href="javascript:;"><span><%=data[4]%></span></a></td>',
                    '<td colspan="2" class="<%=months[5].classname%>"><a href="javascript:;"><span><%=data[5]%></span></a></td>',
                    '<td colspan="2" class="<%=months[6].classname%>"><a href="javascript:;"><span><%=data[6]%></span></a></td>',
                    '<td colspan="2" class="<%=months[7].classname%>"><a href="javascript:;"><span><%=data[7]%></span></a></td>',
                    '</tr>',
                    '<tr>',
                    '<td colspan="2" class="<%=months[8].classname%>"><a href="javascript:;"><span><%=data[8]%></span></a></td>',
                    '<td colspan="2" class="<%=months[9].classname%>"><a href="javascript:;"><span><%=data[9]%></span></a></td>',
                    '<td colspan="2" class="<%=months[10].classname%>"><a href="javascript:;"><span><%=data[10]%></span></a></td>',
                    '<td colspan="2" class="<%=months[11].classname%>"><a href="javascript:;"><span><%=data[11]%></span></a></td>',
                    '</tr>',
                    '</tbody>'
                ]
            },
            defaults = {
                flat: false,
                starts: 1,
                prev: '&#9664;',
                next: '&#9654;',
                lastSel: false,
                down: false,
                mode: 'single',
                view: 'days',
                calendars: 1,
                format: 'Y-m-d',
                position: 'bottom',
                eventName: 'click',
                onRender: function(){return {};},
                onChange: function(){return true;},
                onShow: function(){return true;},
                onBeforeShow: function(){return true;},
                onHide: function(){return true;},
                locale: {
                    days: ["\u661F\u671F\u65E5", "\u661F\u671F\u4E00", "\u661F\u671F\u4E8C", "\u661F\u671F\u4E09", "\u661F\u671F\u56DB", "\u661F\u671F\u4E94", "\u661F\u671F\u516D", "\u661F\u671F\u65E5"],
                    daysShort: ["\u5468\u65E5", "\u5468\u4E00", "\u5468\u4E8C", "\u5468\u4E09", "\u5468\u56DB", "\u5468\u4E94", "\u5468\u516D", "\u5468\u65E5"],
                    //week starts from Monday
//					daysMin: ["\u65E5", "\u4E00", "\u4E8C", "\u4E09", "\u56DB", "\u4E94", "\u516D", "\u65E5"],
                    daysMin: ["\u4E00", "\u4E8C", "\u4E09", "\u56DB", "\u4E94", "\u516D", "\u65E5", "\u4E00"],
                    months: ["\u4E00\u6708", "\u4E8C\u6708", "\u4E09\u6708", "\u56DB\u6708", "\u4E94\u6708", "\u516D\u6708", "\u4E03\u6708", "\u516B\u6708", "\u4E5D\u6708", "\u5341\u6708", "\u5341\u4E00\u6708", "\u5341\u4E8C\u6708"],
                    monthsShort: ["\u4E00", "\u4E8C", "\u4E09", "\u56DB", "\u4E94", "\u516D", "\u4E03", "\u516B", "\u4E5D", "\u5341", "\u5341\u4E00", "\u5341\u4E8C"],
                    weekMin: "\u5468"
                },
                maxDate:null,
                minDate:null
            },
            fill = function(el) {
                var options = $(el).data('datepicker');
                var cal = $(el);
                var currentCal = Math.floor(options.calendars/2), date, data, dow, month, cnt = 0, week, days, indic, indic2, html, tblCal, calYear;
                var _min,_max;
                if(options.minDate){
                    _min = new Date(options.minDate.replace(/[^0-9]/g, "/"));
                    _min.setHours(0,0,0,0);
                }
                if(options.maxDate){
                    _max = new Date(options.maxDate.replace(/[^0-9]/g, "/"));
                    _max.setHours(0,0,0,0);
                }
                cal.find('td>table tbody').remove();
                cache.set('_tmp_start_date_',options.date[0],'_core_');
                cache.set('_tmp_end_date_',options.date[1],'_core_');
                for (var i = 0; i < options.calendars; i++) {
                    date = new Date(options.current);
                    //if months differ, the first cal show the first month,else the first cal show the previous month
//                  date.addMonths(-currentCal + i);
                    if(options.view!="month"){
                        date.addMonths(i);
                    }
                    tblCal = cal.find('table').eq(i+1);
                    switch (tblCal[0].className) {
                        case 'datepickerViewDays':
                            dow = formatDate(date, 'B, Y');
                            break;
                        case 'datepickerViewMonths':
                            dow = date.getFullYear()+i;
                            /*if(options.change||options.view!="month"){
                             dow = date.getFullYear()+i;
                             }else{
                             var calYear = tblCal.find('thead tr:first th:eq(1) span').text();
                             if(calYear==""){
                             dow = date.getFullYear()+i;
                             }else{
                             if(calYear==date.getFullYear()){
                             dow = date.getFullYear();
                             }else{
                             if(!i){
                             dow = date.getFullYear()-(i+1);
                             }else{
                             dow = date.getFullYear()+i;
                             }
                             }
                             }
                             }*/
                            break;
                        case 'datepickerViewYears':
                            dow = (date.getFullYear()-6) + ' - ' + (date.getFullYear()+5);
                            break;
                    }
                    calYear = dow;
                    tblCal.find('thead tr:first th:eq(1) span').text(dow);
                    dow = date.getFullYear()-6;
                    data = {
                        data: [],
                        className: 'datepickerYears',
                        months: []
                    };
                    for ( var j = 0; j < 12; j++) {
                        data.data.push(dow + j);
                        data.months.push({year: dow+j,classname: []});
                    }
                    for(var y in data.months){
                        if(data.months[y].year==options.curDate.getFullYear()){
                            data.months[y].classname.push("datepickerSelected");
                        }
                    }
                    html = tmpl(tpl.months.join(''), data);
                    date.setDate(1);
                    data = {weeks:[]};
                    month = date.getMonth();

                    //week starts from Monday
//					var dow = (date.getDay() - options.starts) % 7;
                    var dow = (date.getDay() - options.starts) % 7 - 1 ;
                    date.addDays(-(dow + (dow < 0 ? 7 : 0)));
                    week = -1;
                    cnt = 0;
                    while (cnt < 42) {
                        indic = parseInt(cnt/7,10);
                        indic2 = cnt%7;
                        if (!data.weeks[indic]) {
                            week = date.getWeekNumber();
                            data.weeks[indic] = {
                                week: week,
                                days: [],
                                classname: []
                            };
                        }
                        data.weeks[indic].days[indic2] = {
                            text: date.getDate(),
                            classname: []
                        };
                        if (month != date.getMonth()) {
                            data.weeks[indic].days[indic2].classname.push('datepickerNotInMonth');
                        }
                        if (date.getDay() == 0) {
                            data.weeks[indic].days[indic2].classname.push('datepickerSunday');
                        }
                        if (date.getDay() == 6) {
                            data.weeks[indic].days[indic2].classname.push('datepickerSaturday');
                        }

                        var fromUser = options.onRender(date);
                        var val = date.valueOf();
                        if (fromUser.selected || options.date == val || $.inArray(val, options.date) > -1 || (options.mode == 'range' && val >= options.date[0] && val <= options.date[1])) {
                            data.weeks[indic].days[indic2].classname.push('datepickerSelected');
                        }
                        if (fromUser.disabled) {
                            data.weeks[indic].days[indic2].classname.push('datepickerDisabled');
                        }
                        if(_min){
                            if(date<=_min){
                                data.weeks[indic].days[indic2].classname.push('datepickerDisabled');
                            }
                        }
                        if(_max){
                            if(date>=_max){
                                data.weeks[indic].days[indic2].classname.push('datepickerDisabled');
                            }
                        }
                        if (fromUser.className) {
                            data.weeks[indic].days[indic2].classname.push(fromUser.className);
                        }
                        data.weeks[indic].days[indic2].classname = data.weeks[indic].days[indic2].classname.join(' ');
                        cnt++;

                        date.addDays(1);

                        //着色被选中周
                        if(options.view=="week"&&calYear.indexOf(options.curDate.getFullYear())>=0){
                            if(options.curDate.getWeekNumber()==data.weeks[indic].week){
                                data.weeks[indic].classname[0] = "datepickerSelected";
                            }
                        }
                    }
                    html = tmpl(tpl.days.join(''), data) + html;
                    data = {
                        data: options.locale.monthsShort,
                        className: 'datepickerMonths',
                        months: []
                    };
                    for(var m in data.data){
                        data.months.push({month: m,classname: []});
                    }
                    var realMinDate = new Date(_min.getTime()+864e5),
                        realMaxDate = new Date(_max.getTime()-864e5);
                    if(options.view=="month"){
                        if(calYear>realMaxDate.getFullYear()||calYear<realMinDate.getFullYear()){     //判断年份是否小于最小年或者大于最大年
                            for(var m in data.months){
                                //置灰未来月
                                data.months[m].classname.push("datepickerDisabled");
                            }
                        }else if(calYear==realMaxDate.getFullYear()){      //判断与最大日期同年时月份大于最大日期的月份
                            for(var m in data.months){
                                if(data.months[m].month>realMaxDate.getMonth()){
                                    //置灰未来月
                                    data.months[m].classname.push("datepickerDisabled");
                                }
                            }
                        }else if(calYear==realMinDate.getFullYear()){      //判断与最小日期同年时月份小于最小日期的月份
                            for(var m in data.months){
                                if(data.months[m].month<realMinDate.getMonth()){
                                    //置灰未来月
                                    data.months[m].classname.push("datepickerDisabled");
                                }
                            }
                        }
                        if(calYear==options.curDate.getFullYear()){
                            for(var m in data.months){
                                if(data.months[m].month==options.curDate.getMonth()){
                                    data.months[m].classname.push("datepickerSelected");
                                }
                            }
                        }
                    }
                    html = tmpl(tpl.months.join(''), data) + html;
                    tblCal.append(html);
                }
            },
            parseDate = function (date, format) {
                if (date.constructor == Date) {
                    return new Date(date);
                }
                var parts = date.split(/\W+/);
                var against = format.split(/\W+/), d, m, y, h, min, now = new Date();
                for (var i = 0; i < parts.length; i++) {
                    switch (against[i]) {
                        case 'd':
                        case 'e':
                            d = parseInt(parts[i],10);
                            break;
                        case 'm':
                            m = parseInt(parts[i], 10)-1;
                            break;
                        case 'Y':
                        case 'y':
                            y = parseInt(parts[i], 10);
                            y += y > 100 ? 0 : (y < 29 ? 2000 : 1900);
                            break;
                        case 'H':
                        case 'I':
                        case 'k':
                        case 'l':
                            h = parseInt(parts[i], 10);
                            break;
                        case 'P':
                        case 'p':
                            if (/pm/i.test(parts[i]) && h < 12) {
                                h += 12;
                            } else if (/am/i.test(parts[i]) && h >= 12) {
                                h -= 12;
                            }
                            break;
                        case 'M':
                            min = parseInt(parts[i], 10);
                            break;
                    }
                }
                return new Date(
                    y === undefined ? now.getFullYear() : y,
                    m === undefined ? now.getMonth() : m,
                    d === undefined ? now.getDate() : d,
                    h === undefined ? now.getHours() : h,
                    min === undefined ? now.getMinutes() : min,
                    0
                );
            },
            formatDate = function(date, format) {
                var m = date.getMonth();
                var d = date.getDate();
                var y = date.getFullYear();
                var wn = date.getWeekNumber();
                var w = date.getDay();
                var s = {};
                var hr = date.getHours();
                var pm = (hr >= 12);
                var ir = (pm) ? (hr - 12) : hr;
                var dy = date.getDayOfYear();
                if (ir == 0) {
                    ir = 12;
                }
                var min = date.getMinutes();
                var sec = date.getSeconds();
                var parts = format.split(''), part;
                for ( var i = 0; i < parts.length; i++ ) {
                    part = parts[i];
                    switch (parts[i]) {
                        case 'a':
                            part = date.getDayName();
                            break;
                        case 'A':
                            part = date.getDayName(true);
                            break;
                        case 'b':
                            part = date.getMonthName();
                            break;
                        case 'B':
                            part = date.getMonthName(true);
                            break;
                        case 'C':
                            part = 1 + Math.floor(y / 100);
                            break;
                        case 'd':
                            part = (d < 10) ? ("0" + d) : d;
                            break;
                        case 'e':
                            part = d;
                            break;
                        case 'H':
                            part = (hr < 10) ? ("0" + hr) : hr;
                            break;
                        case 'I':
                            part = (ir < 10) ? ("0" + ir) : ir;
                            break;
                        case 'j':
                            part = (dy < 100) ? ((dy < 10) ? ("00" + dy) : ("0" + dy)) : dy;
                            break;
                        case 'k':
                            part = hr;
                            break;
                        case 'l':
                            part = ir;
                            break;
                        case 'm':
                            part = (m < 9) ? ("0" + (1+m)) : (1+m);
                            break;
                        case 'M':
                            part = (min < 10) ? ("0" + min) : min;
                            break;
                        case 'p':
                        case 'P':
                            part = pm ? "PM" : "AM";
                            break;
                        case 's':
                            part = Math.floor(date.getTime() / 1000);
                            break;
                        case 'S':
                            part = (sec < 10) ? ("0" + sec) : sec;
                            break;
                        case 'u':
                            part = w + 1;
                            break;
                        case 'w':
                            part = w;
                            break;
                        case 'y':
                            part = ('' + y).substr(2, 2);
                            break;
                        case 'Y':
                            part = y;
                            break;
                    }
                    parts[i] = part;
                }
                return parts.join('');
            },
            extendDate = function(options) {
                if (Date.prototype.tempDate) {
                    return;
                }
                Date.prototype.tempDate = null;
                Date.prototype.months = options.months;
                Date.prototype.monthsShort = options.monthsShort;
                Date.prototype.days = options.days;
                Date.prototype.daysShort = options.daysShort;
                Date.prototype.getMonthName = function(fullName) {
                    return this[fullName ? 'months' : 'monthsShort'][this.getMonth()];
                };
                Date.prototype.getDayName = function(fullName) {
                    return this[fullName ? 'days' : 'daysShort'][this.getDay()];
                };
                Date.prototype.addDays = function (n) {
                    this.setDate(this.getDate() + n);
                    this.tempDate = this.getDate();
                };
                Date.prototype.addMonths = function (n) {
                    if (this.tempDate == null) {
                        this.tempDate = this.getDate();
                    }
                    this.setDate(1);
                    this.setMonth(this.getMonth() + n);
                    this.setDate(Math.min(this.tempDate, this.getMaxDays()));
                };
                Date.prototype.addYears = function (n) {
                    if (this.tempDate == null) {
                        this.tempDate = this.getDate();
                    }
                    this.setDate(1);
                    this.setFullYear(this.getFullYear() + n);
                    this.setDate(Math.min(this.tempDate, this.getMaxDays()));
                };
                Date.prototype.getMaxDays = function() {
                    var tmpDate = new Date(Date.parse(this)),
                        d = 28, m;
                    m = tmpDate.getMonth();
                    d = 28;
                    while (tmpDate.getMonth() == m) {
                        d ++;
                        tmpDate.setDate(d);
                    }
                    return d - 1;
                };
                Date.prototype.getFirstDay = function() {
                    var tmpDate = new Date(Date.parse(this));
                    tmpDate.setDate(1);
                    return tmpDate.getDay();
                };
//				Date.prototype.getWeekNumber = function() {
//					var tempDate = new Date(this);
//					tempDate.setDate(tempDate.getDate() - (tempDate.getDay() + 6) % 7 + 3);
//					var dms = tempDate.valueOf();
//					tempDate.setMonth(0);
//					tempDate.setDate(4);
//					console.log(tempDate);
//					return Math.round((dms - tempDate.valueOf()) / (604800000)) + 1;
//				};
                Date.prototype.getDayOfYear = function() {
                    var now = new Date(this.getFullYear(), this.getMonth(), this.getDate(), 0, 0, 0);
                    var then = new Date(this.getFullYear(), 0, 0, 0, 0, 0);
                    var time = now - then;
                    return Math.floor(time / 24*60*60*1000);
                };
            },
            layout = function (el) {
                var options = $(el).data('datepicker');
                var cal = $('#' + options.id);
                if (!options.extraHeight) {
                    var divs = $(el).find('div');
                    options.extraHeight = divs.get(0).offsetHeight + divs.get(1).offsetHeight;
                    options.extraWidth = divs.get(2).offsetWidth + divs.get(3).offsetWidth;
                }
                var tbl = cal.find('table:first').get(0);
                var width = tbl.offsetWidth;
                var height = tbl.offsetHeight;
                cal.css({
                    width: width + options.extraWidth + 'px',
                    height: height + options.extraHeight + 'px'
                }).find('div.datepickerContainer').css({
                    width: width + 'px',
                    height: height + 'px'
                });
            },
            click = function(ev) {
                if ($(ev.target).is('span')) {
                    ev.target = ev.target.parentNode;
                }
                var el = $(ev.target);
                if (el.is('a')) {
                    var options = $(this).data('datepicker');
                    options.change = false;
                    options.selVal = $('span', el).attr('val');
                    ev.target.blur();
                    if (el.hasClass('datepickerDisabled')) {
                        return false;
                    }

                    options.curWeek = "";
                    options.curYear = "";
                    options.curMonth = "";
                    var parentEl = el.parent();
                    var tblEl = parentEl.parent().parent().parent();
                    var tblIndex = $('table', this).index(tblEl.get(0)) - 1;
                    var tmp = new Date(options.current);
                    var changed = false;
                    var fillIt = false;
                    if (parentEl.is('th')) {
                        if (parentEl.hasClass('datepickerWeek') && options.mode == 'range' && !parentEl.next().hasClass('datepickerDisabled')) {
                            //if in the mode of !week
                            if(options.view!="!week"&&options.view!="!month!week"&&options.view!="month"){
                                var val = parseInt(parentEl.next().text(), 10);
//								tmp.addMonths(tblIndex - Math.floor(options.calendars/2));
                                tmp.addMonths(tblIndex);
                                if (parentEl.next().hasClass('datepickerNotInMonth')) {
                                    tmp.addMonths(val > 15 ? -1 : 1);
                                }
                                tmp.setDate(val);
                                options.date[0] = (tmp.setHours(0,0,0,0)).valueOf();
                                tmp.setHours(23,59,59,0);
                                tmp.addDays(6);
                                options.date[1] = tmp.valueOf();
                                options.curMonth = "";
                                options.curYear = tmp.getFullYear();
                                options.curWeek = parentEl.text();
                                options.curDate = tmp;
                                fillIt = true;
                                changed = true;
                                options.lastSel = false;
                            }
                        } else if (parentEl.hasClass('datepickerMonth')) {
                            if(options.view!="!month"&&options.view!="!month!week"&&options.view!="week"){
//								tmp.addMonths(tblIndex - Math.floor(options.calendars/2));
                                tmp.addMonths(tblIndex);
                                switch (tblEl.get(0).className) {
                                    //天视图进入月视图
                                    case 'datepickerViewDays':
                                        tblEl.get(0).className = 'datepickerViewMonths';
                                        el.find('span').text(tmp.getFullYear());
                                        break;
                                    //月视图进入年视图
                                    //								case 'datepickerViewMonths':
                                    //									tblEl.get(0).className = 'datepickerViewYears';
                                    //									el.find('span').text((tmp.getFullYear()-6) + ' - ' + (tmp.getFullYear()+5));
                                    //									break;
                                    //年视图进入天视图
                                    //								case 'datepickerViewYears':
                                    //									tblEl.get(0).className = 'datepickerViewDays';
                                    //									el.find('span').text(formatDate(tmp, 'B, Y'));
                                    //									break;
                                }
                            }
                        } else if (parentEl.parent().parent().is('thead')) {
                            switch (tblEl.get(0).className) {
                                case 'datepickerViewDays':
                                    options.current.addMonths(parentEl.hasClass('datepickerGoPrev') ? -1 : 1);
                                    break;
                                case 'datepickerViewMonths':
                                    options.change = true;
                                    options.current.addYears(parentEl.hasClass('datepickerGoPrev') ? -1 : 1);
                                    break;
                                case 'datepickerViewYears':
                                    options.current.addYears(parentEl.hasClass('datepickerGoPrev') ? -12 : 12);
                                    break;
                            }
                            fillIt = true;
                        }
                    } else if (parentEl.is('td') && !parentEl.hasClass('datepickerDisabled')) {
                        switch (tblEl.get(0).className) {
                            //月视图中点击月份
                            case 'datepickerViewMonths':
                                options.current.setDate(1);
                                options.current.setMonth(tblEl.find('tbody.datepickerMonths td').index(parentEl));
                                options.current.setFullYear(parseInt(tblEl.find('thead th.datepickerMonth span').text(), 10));
                                tmp = new Date(options.current);

                                //第二个日期控件显示月份
//								options.current.addMonths(Math.floor(options.calendars/2) - tblIndex);
//								options.current.addMonths(-tblIndex);
                                options.curMonth = tmp.getMonth()+1;

                                options.date[0] = tmp.firstDayOfMonth();
                                options.date[1] = tmp.lastDayOfMonth();


                                options.curYear = tmp.getFullYear();
                                options.curWeek = "";
                                options.curDate = tmp;

//										if(el.is("a")){
//	//										$("td",el.closest(".datepickerContainer")).filter(".datepickerSelected").removeClass("datepickerSelected");
//											el.closest("td").addClass("datepickerSelected");
//										}

                                if(options.view!="week"&&options.view!="month"&&options.view!="!month!week"){
                                    tblEl.get(0).className = 'datepickerViewDays';
                                }

                                options.lastSel = false;
                                break;
                            //年视图中点击年份
//							case 'datepickerViewYears':
//								options.current.setFullYear(parseInt(el.text(), 10));
//								tblEl.get(0).className = 'datepickerViewMonths';
//								break;
                            default:
                                if(options.view!="month"){
                                    var val = parseInt(el.text(), 10);
//									tmp.addMonths(tblIndex - Math.floor(options.calendars/2));
                                    tmp.addMonths(tblIndex);
                                    if (parentEl.hasClass('datepickerNotInMonth')) {
                                        tmp.addMonths(val > 15 ? -1 : 1);
                                    }
                                    tmp.setDate(val);
                                    options.curDate = tmp;
                                    if(options.view=="week"){
                                        if(el.is("td")){
                                            $("a",el.prevAll("th").eq(0)).click();
                                        }else{
                                            $("a",el.closest("td").prevAll("th").eq(0)).click();
                                        }
                                        return;
                                    }
                                    switch (options.mode) {
                                        case 'multiple':
                                            val = (tmp.setHours(0,0,0,0)).valueOf();
                                            if ($.inArray(val, options.date) > -1) {
                                                $.each(options.date, function(nr, dat){
                                                    if (dat == val) {
                                                        options.date.splice(nr,1);
                                                        return false;
                                                    }
                                                });
                                            } else {
                                                options.date.push(val);
                                            }
                                            break;
                                        case 'range':
                                            if (!options.lastSel) {
                                                options.date[0] = (tmp.setHours(0,0,0,0)).valueOf();
                                            }
                                            val = (tmp.setHours(23,59,59,0)).valueOf();
                                            if (val < options.date[0]) {
                                                options.date[1] = options.date[0] + 86399000;
                                                options.date[0] = val - 86399000;
                                            } else {
                                                options.date[1] = val;
                                            }
                                            options.curSel = (tmp.setHours(0,0,0,0)).valueOf();
                                            options.lastSel = !options.lastSel;

                                            break;
                                        default:
                                            options.date = tmp.valueOf();
                                            break;
                                    }
                                }else{
                                    return false;
                                }
                                break;
                        }
                        fillIt = true;
                        changed = true;
                    }

                    if (changed) {
                        options.onChange.apply(this, prepareDate(options));
                    }

                    if (fillIt) {
                        fill(this);
                    }
                }
                return false;
            },
            mousedrag = function(ev){
                var options = $(this).data('datepicker');
                if(!options.down){
                    var el = $(ev.target);
                    if(el.is("a")){
                        el = el.parent();
                    }

                    options.down = true;
                    if (el.hasClass('datepickerDisabled')) {
                        return false;
                    }
                    var tblEl = el.parent().parent();
                    var tblIndex = $('table', this).index(tblEl.get(0)) - 1;
                    var tmp = new Date(options.current);

                    if (el.is('td') && !el.hasClass('datepickerDisabled')) {
                        var val = parseInt(el.text(), 10);

                        options.firstSel = parseInt(el.find("a:eq(0)").find("span:eq(0)").attr("val"),10);options.selDay = options.firstSel;
                        tmp.addMonths(tblIndex - Math.floor(options.calendars/2));
                        if (el.hasClass('datepickerNotInMonth')) {
                            tmp.addMonths(val > 15 ? -1 : 1);
                        }
                        tmp.setDate(val);

                        options.date[0] = (tmp.setHours(0,0,0,0)).valueOf();
                        $('td.datepickerSelected').removeClass('datepickerSelected');
                        $(ev.target).closest('td').addClass('datepickerSelected');

                        var tmpSel,$target;
                        $(this).mousemove(function(e){
                            if(options.down){
                                $target = $(e.target);
                                if($target.is("td")){
                                    tmpSel = parseInt($("a > span", $target).attr('val'),10);
                                }else if($target.is("a")){
                                    tmpSel = parseInt($("span", $target).attr('val'),10);
                                }else if($target.is("span")){
                                    tmpSel = parseInt($target.attr('val'),10);
                                }
                                if(tmpSel!=options.selDay){
                                    options.selDay = tmpSel;
                                    $('td.datepickerSelected').removeClass('datepickerSelected');
                                    if(tmpSel<=options.firstSel){
                                        for(var i=tmpSel;i<=options.firstSel;i++){
                                            $('td > a > span[val="'+i+'"]').closest('td').addClass('datepickerSelected');
                                        }
                                    }else{
                                        for(var i=options.firstSel;i<=tmpSel;i++){
                                            $('td > a > span[val="'+i+'"]').closest('td').addClass('datepickerSelected');
                                        }
                                    }
                                }
                            }else{
                                return false;
                            }
                        });


                        $(this).mouseup(function(ev){
                            options.down = false,tmp = new Date(options.current);
                            var val = (tmp.setHours(23,59,59,0)).valueOf();
                            if (val < options.date[0]) {
                                options.date[1] = options.date[0] + 86399000;
                                options.date[0] = val - 86399000;
                            } else {
                                options.date[1] = val;
                            }
                            options.curSel = (tmp.setHours(0,0,0,0)).valueOf();
                            fillIt = true;
                            changed = true;

                            if (changed) {
                                options.onChange.apply(this, prepareDate(options));
                            }

                            if (fillIt) {
                                fill(this);
                            }
                        });
                    }
                }
            },
            drag = function(e){
                var options = $(this).data('datepicker');
                var tmpSel,$target;
                e.stopPropagation();
                if(options.lastSel){
                    $target = $(e.target);
                    if($target.is("a")){
                        tmpSel = parseInt($("span", $target).attr('val'));
                    }else if($target.is("span")){
                        tmpSel = parseInt($target.attr('val'));
                    }else if($target.is("td")){
                        tmpSel = parseInt($("a > span", $target).attr('val'));
                    }

                    if(tmpSel!=options.selVal){
                        if(tmpSel>options.firstSel){
                            if((tmpSel-options.selVal)==1){
                                options.selVal = tmpSel;
                                $('span[val="'+tmpSel+'"]').closest('td').addClass('datepickerSelected');
                            }else{
                                options.selVal = tmpSel;
                                $('td.datepickerSelected').removeClass('datepickerSelected');
                                for(var i=options.firstSel;i<=tmpSel;i++){
                                    $('span[val="'+i+'"]').closest('td').addClass('datepickerSelected');
                                }
                            }
                        }else{
                            if((options.selVal-tmpSel)==1){
                                options.selVal = tmpSel;
                                $('span[val="'+tmpSel+'"]').closest('td').addClass('datepickerSelected');
                            }else{
                                options.selVal = tmpSel;
                                $('td.datepickerSelected').removeClass('datepickerSelected');
                                for(var i=tmpSel;i<=options.firstSel;i++){
                                    $('span[val="'+i+'"]').closest('td').addClass('datepickerSelected');
                                }
                            }
                        }
                    }
                }else{
                    return false;
                }
            },
            prepareDate = function (options) {
                var tmp;
                if (options.mode == 'single') {
                    tmp = new Date(options.date);
                    return [formatDate(tmp, options.format), tmp, options.el];
                } else {
                    tmp = [[],[], options.el];
                    $.each(options.date, function(nr, val){
                        var date = new Date(val);
                        tmp[0].push(formatDate(date, options.format));
                        tmp[1].push(date);
                    });

                    tmp[0].push(options.curMonth);
                    tmp[0].push(options.curYear);
                    tmp[0].push(options.curWeek);

                    return tmp;
                }
            },
            getViewport = function () {
                var m = document.compatMode == 'CSS1Compat';
                return {
                    l : window.pageXOffset || (m ? document.documentElement.scrollLeft : document.body.scrollLeft),
                    t : window.pageYOffset || (m ? document.documentElement.scrollTop : document.body.scrollTop),
                    w : window.innerWidth || (m ? document.documentElement.clientWidth : document.body.clientWidth),
                    h : window.innerHeight || (m ? document.documentElement.clientHeight : document.body.clientHeight)
                };
            },
            isChildOf = function(parentEl, el, container) {
                if (parentEl == el) {
                    return true;
                }
                if (parentEl.contains) {
                    return parentEl.contains(el);
                }
                if ( parentEl.compareDocumentPosition ) {
                    return !!(parentEl.compareDocumentPosition(el) & 16);
                }
                var prEl = el.parentNode;
                while(prEl && prEl != container) {
                    if (prEl == parentEl)
                        return true;
                    prEl = prEl.parentNode;
                }
                return false;
            },
            show = function (ev) {
                var cal = $('#' + $(this).data('datepickerId'));
                if (!cal.is(':visible')) {
                    var calEl = cal.get(0);
                    fill(calEl);
                    var options = cal.data('datepicker');
                    options.onBeforeShow.apply(this, [cal.get(0)]);
                    var pos = $(this).offset();
                    var viewPort = getViewport();
                    var top = pos.top;
                    var left = pos.left;
                    var oldDisplay = $.curCSS(calEl, 'display');
                    cal.css({
                        visibility: 'hidden',
                        display: 'block'
                    });
                    layout(calEl);
                    switch (options.position){
                        case 'top':
                            top -= calEl.offsetHeight;
                            break;
                        case 'left':
                            left -= calEl.offsetWidth;
                            break;
                        case 'right':
                            left += this.offsetWidth;
                            break;
                        case 'bottom':
                            top += this.offsetHeight;
                            break;
                    }
                    if (top + calEl.offsetHeight > viewPort.t + viewPort.h) {
                        top = pos.top  - calEl.offsetHeight;
                    }
                    if (top < viewPort.t) {
                        top = pos.top + this.offsetHeight + calEl.offsetHeight;
                    }
                    if (left + calEl.offsetWidth > viewPort.l + viewPort.w) {
                        left = pos.left - calEl.offsetWidth;
                    }
                    if (left < viewPort.l) {
                        left = pos.left + this.offsetWidth
                    }
                    cal.css({
                        visibility: 'visible',
                        display: 'block',
                        top: top + 'px',
                        left: left + 'px'
                    });
                    if (options.onShow.apply(this, [cal.get(0)]) != false) {
                        cal.show();
                    }

                    //console.info(cal);

                    $(document).on('mousedown', {cal: cal, trigger: this}, hide);
                }
                return false;
            },
            hide = function (ev) {
                if (ev.target != ev.data.trigger && !isChildOf(ev.data.cal.get(0), ev.target, ev.data.cal.get(0))) {
                    if (ev.data.cal.data('datepicker').onHide.apply(this, [ev.data.cal.get(0)]) != false) {
                        ev.data.cal.hide();
                    }
                    $(document).off('mousedown', hide);
                }
            };
        return {
            init: function(options){
                options = $.extend({}, defaults, options||{});
                extendDate(options.locale);
                options.calendars = Math.max(1, parseInt(options.calendars,10)||1);
                options.mode = /single|multiple|range/.test(options.mode) ? options.mode : 'single';
                //console.info(options.date);
                return this.each(function(){
                    if (!$(this).data('datepicker')) {
                        options.el = this;
                        if (options.date.constructor == String) {
                            options.date = parseDate(options.date, options.format);
                            options.date.setHours(0,0,0,0);
                        }
                        if (options.mode != 'single') {
                            if (options.date.constructor != Array) {
                                options.date = [options.date.valueOf()];
                                if (options.mode == 'range') {
                                    options.date.push(((new Date(options.date[0])).setHours(23,59,59,0)).valueOf());
                                }
                            } else {
                                for (var i = 0; i < options.date.length; i++) {
                                    options.date[i] = (parseDate(options.date[i], options.format).setHours(0,0,0,0)).valueOf();
                                }
                                if (options.mode == 'range') {
                                    options.date[1] = ((new Date(options.date[1])).setHours(23,59,59,0)).valueOf();
                                }
                            }
                        } else {
                            options.date = options.date.valueOf();
                        }
                        if (!options.current) {
                            options.current = new Date();
                        } else {
                            options.current = parseDate(options.current, options.format);
                        }

                        options.curDate = new Date(options.date[0]);
                        options.current.setMonth(options.curDate.getMonth());
                        options.current.setFullYear(options.curDate.getFullYear());
                        options.current.setHours(0,0,0,0);
                        var id = 'datepicker_' + parseInt(Math.random() * 1000), cnt;
                        options.id = id;
                        $(this).data('datepickerId', options.id);
                        var cal;
                        cal = $(tpl.wrapper).attr('id', id).on('click', click).data('datepicker', options);
                        /*if('range'==options.type){
                         cal = $(tpl.wrapper).attr('id', id).on('click', click).on('mousemove', drag).data('datepicker', options);
                         }else{
                         cal = $(tpl.wrapper).attr('id', id).on('click', click).data('datepicker', options);
                         }*/
                        if (options.className) {
                            cal.addClass(options.className);
                        }
                        var html = '';
                        for (var i = 0; i < options.calendars; i++) {
                            cnt = options.starts;
                            if (i > 0) {
                                html += tpl.space;
                            }
                            html += tmpl(tpl.head.join(''), {
                                week: options.locale.weekMin,
                                prev: options.prev,
                                next: options.next,
                                day1: options.locale.daysMin[(cnt++)%7],
                                day2: options.locale.daysMin[(cnt++)%7],
                                day3: options.locale.daysMin[(cnt++)%7],
                                day4: options.locale.daysMin[(cnt++)%7],
                                day5: options.locale.daysMin[(cnt++)%7],
                                day6: options.locale.daysMin[(cnt++)%7],
                                day7: options.locale.daysMin[(cnt++)%7]
                            });
                        }
                        cal
                            .find('tr:first').append(html)
                            .find('table').addClass(views[options.view]);
                        fill(cal.get(0));
                        if (options.flat) {
                            cal.appendTo(this).show().css('position', 'relative');
                            layout(cal.get(0));
                        } else {
                            cal.appendTo(document.body);
                            $(this).on(options.eventName, show);
                        }
                    }
                });
            },
            showPicker: function() {
                return this.each( function () {
                    if ($(this).data('datepickerId')) {
                        show.apply(this);
                    }
                });
            },
            hidePicker: function() {
                return this.each( function () {
                    if ($(this).data('datepickerId')) {
                        $('#' + $(this).data('datepickerId')).hide();
                    }
                });
            },
            setDate: function(date, shiftTo){
                return this.each(function(){
                    if ($(this).data('datepickerId')) {
                        var cal = $('#' + $(this).data('datepickerId'));
                        var options = cal.data('datepicker');
                        options.date = $.jClone(date);
                        if (options.date.constructor == String) {
                            options.date = parseDate(options.date, options.format);
                            options.date.setHours(0,0,0,0);
                        }
                        if (options.mode != 'single') {
                            if (options.date.constructor != Array) {
                                options.date = [options.date.valueOf()];
                                if (options.mode == 'range') {
                                    options.date.push(((new Date(options.date[0])).setHours(23,59,59,0)).valueOf());
                                }
                            } else {
                                for (var i = 0; i < options.date.length; i++) {
                                    options.date[i] = (parseDate(options.date[i], options.format).setHours(0,0,0,0)).valueOf();
                                }
                                if (options.mode == 'range') {
                                    options.date[1] = ((new Date(options.date[1])).setHours(23,59,59,0)).valueOf();
                                }
                            }
                        } else {
                            options.date = options.date.valueOf();
                        }
                        if (shiftTo) {
                            options.current = new Date (options.mode != 'single' ? options.date[0] : options.date);
                        }

                        options.curDate = new Date(options.date[0]);
                        options.current.setMonth(options.curDate.getMonth());
                        options.current.setYear(options.curDate.getFullYear());
                        fill(cal.get(0));
                    }
                });
            },
            getDate: function(formated) {
                if (this.size() > 0) {
                    return prepareDate($('#' + $(this).data('datepickerId')).data('datepicker'))[formated ? 0 : 1];
                }
            },
            clear: function(){
                return this.each(function(){
                    if ($(this).data('datepickerId')) {
                        var cal = $('#' + $(this).data('datepickerId'));
                        var options = cal.data('datepicker');
                        if (options.mode != 'single') {
                            options.date = [];
                            fill(cal.get(0));
                        }
                    }
                });
            },
            fixLayout: function(){
                return this.each(function(){
                    if ($(this).data('datepickerId')) {
                        var cal = $('#' + $(this).data('datepickerId'));
                        var options = cal.data('datepicker');
                        if (options.flat) {
                            layout(cal.get(0));
                        }
                    }
                });
            }
        };
    }();
    var initDatePicker = function(_containerId, _cfg){
            var $container = $('#'+_containerId),
                cfg = _cfg,
                now = new Date(),
                date = new Date(),
                curDate,
                pickerId,
                tmp=[],
                currs=[],
                states=[];
//            if(!!cfg[_KEY_.MAX_DATE]&&cfg[_KEY_.MAX_DATE]>=2){
//                //TODO
//            }else{
//                if(date.getHours()<8){
//                    cfg[_KEY_.MAX_DATE] = 2;
//                }else{
//                    if(!!cache.get(O._KEY_.Cache.LASTDATE)){
//                        cfg[_KEY_.MAX_DATE] = Number(date.Format("yyyyMMdd"))-Number(cache.get(O._KEY_.Cache.LASTDATE).replace(/-/g,""));
//                    }
//                }
//            }

            /* 日期控件计算部分 START */
            {
                pickerId = cfg[_KEY_.ID];
                currs[pickerId] = $("#"+pickerId);
                /* maxDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） START */
                cfg[_KEY_.MAX_DATE] = (function(){
                    var tmpMaxDate = cfg[_KEY_.MAX_DATE]||1;
                    if(/^\d{4}[-\/]\d{2}$/.test(tmpMaxDate)){      //判断maxDate: 2015-01
                        tmpMaxDate = (new Date(tmpMaxDate).lastDayOfMonth()).Format('yyyy-MM-dd');
                    }
                    if(/^\d{4}[-\/]\d{2}[-\/]\d{2}$/.test(tmpMaxDate)){      //判断maxDate: 2015-01-01
                        tmpMaxDate = Math.floor(((new Date())-(new Date(tmpMaxDate)))/864e5);
                    }
                    return tmpMaxDate<0?1:tmpMaxDate;
                })();
                /* maxDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） END */
                /* minDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） START */
                cfg[_KEY_.MIN_DATE] = (function(){
                    var tmpMinDate = cfg[_KEY_.MIN_DATE]||'2000-12-31';
                    if(/^\d{4}[-\/]\d{2}$/.test(tmpMinDate)){
                        tmpMinDate = (new Date(tmpMinDate)).Format('yyyy-MM-dd');
                    }
                    return tmpMinDate;
                })();
                /* minDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） END */
                var paramCfg = cfg[_KEY_.PARAM]
                    ,ifVisible = cfg[_KEY_.IF_VISIBLE]
                    ,type = cfg[_KEY_.TYPE]
                    ,style = cfg[_KEY_.STYLE],bSimple = (style&&style==_KEY_.SIMPLE)
                    ,patt = cfg[_KEY_.PATTERN]
                    ,range = cfg[_KEY_.RANGE]||[]
                    ,view = cfg[_KEY_.VIEW],bMonth = (view==_KEY_.VMONTH),bWeek = (view==_KEY_.VWEEK)
                    ,dateSeg = cfg[_KEY_.DATE_SEG]
                    ,bSingle = (type==_KEY_.SINGLE)
                    ,bRange = (type==_KEY_.RANGE)
                    ,minDate = cfg[_KEY_.MIN_DATE]
                    ,maxDate = cfg[_KEY_.MAX_DATE]?(new Date()).prevDay(cfg[_KEY_.MAX_DATE]).Format("yyyy-MM-dd"):(new Date()).prevDay().Format("yyyy-MM-dd")
                    ,fbdDate = cfg[_KEY_.MAX_DATE]?(new Date()).prevDay(Number(cfg[_KEY_.MAX_DATE])-1).Format("yyyy-MM-dd"):(new Date()).Format("yyyy-MM-dd")
                    ,calendars = cfg[_KEY_.CALENDARS]||2
                    ,inRapid = $(".radiobox.r-checked",currs[pickerId].next(".rapidSel")).length
                    ,customChanges = cfg[_KEY_.CUSTOM_CHANGE]||[]
                    ,ifTimeModeEffective = bSingle && !!cfg[_KEY_.TIME_MODE]
                    ,hmsRegex = new RegExp((cfg[_KEY_.TIME_MODE]||"").replace(/[hms]/g, '\\d'), 'gi');

                /**
                 * setting show width
                 * param _model 1-170,125; 2-285,240; 3-385,340:
                 * 注释掉宽度限制，改为用css设置 自适应
                 */
                var widthSet = function(_model, _pickerId){
                    if(!cfg[_KEY_.IF_WIDTH_AUTO]){
                        _model = bSimple?1:_model;
                        var extWidth = ifTimeModeEffective?40:0;
                        switch(_model){
                            case 1:  $('.widgetField',currs[_pickerId]).css({width: 170+extWidth+'px'});
                                $('.widgetField input',currs[_pickerId]).css({width: 135+extWidth+'px'});
                                break;
                            case 2:  $('.widgetField',currs[_pickerId]).css({width: 285+extWidth+'px'});
                                $('.widgetField input',currs[_pickerId]).css({width: 250+extWidth+'px'});
                                break;
                            case 3:  $('.widgetField',currs[_pickerId]).css({width: 340+extWidth+'px'});
                                $('.widgetField input',currs[_pickerId]).css({width: 305+extWidth+'px'});
                                break;
                            case 4:  $('.widgetField',currs[_pickerId]).css({width: 385+extWidth+'px'});
                                $('.widgetField input',currs[_pickerId]).css({width: 350+extWidth+'px'});
                                break;
                            case 5:  $('.widgetField',currs[_pickerId]).css({width: 212+extWidth+'px'});
                                $('.widgetField input',currs[_pickerId]).css({width: 175+extWidth+'px'});
                                break;
                            default: break;
                        }
                    }
                };

                if(bSingle){
                    var tmpField, tmpSeg, tmpCurDate;
                    if(bMonth){
                        //为应对双十一期间,改成设置显示延迟时间,当延迟天数大于日数（延迟3天，当前为周二）,可能会造成当前月不能选,但会有跨年周的问题,仅对于有周月维度的日期控件
                        date = date.prevDay(cfg[_KEY_.MAX_DATE]);
                        if(!inRapid){
                            widthSet(4, pickerId);
                            cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,4);
                        }else{
                            widthSet(3, pickerId);
                            cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,3);
                        }
                        tmp[0] = date.Format("yyyy-MM");
                        if(!!range&&!$.jIsArray(range)){
                            tmp[0] = range;
                        }
                        $("#"+dateSeg[0]).val(tmp[0]);
                        tmpSeg = [tmp[0]];
                        var yy = parseInt(tmp[0].slice(0,4)),m = tmp[0].slice(5),mm = parseInt(m.slice(0,1)=="0"?m.slice(1):m.slice(0));
                        tmp[1] = new Date(yy, mm-1, 1).curMonth(cfg[_KEY_.MAX_DATE]);
                        curDate = $.jClone(tmp[1]);
                        tmpCurDate = $.jClone(tmp[1]);
                        tmpField = yy+'\u5e74'+m+'\u6708'+(bSimple?"":(" "+tmp[1].join(' \u81F3 ')));
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val(tmpField);
                    }else if(bWeek){
                        if(!inRapid){
                            widthSet(4, pickerId);
                            cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,4);
                        }else{
                            var finalDate = cache.get(_KEY_.CACHE_FINAL_DATE+pickerId);
                            if(finalDate[0]==finalDate[1]){
                                widthSet(5, pickerId);
                                cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,5);
                            }else{
                                widthSet(3, pickerId);
                                cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,3);
                            }
                        }
                        var yy = date.getFullYear(),ww = date.getWeekNumber(),w = "";
                        //为应对双十一期间,改成设置显示延迟时间,当延迟天数大于日数（延迟3天，当前为周二）,可能会造成当前周不能选,但会有跨年周的问题,仅对于有周月维度的日期控件
                        var tmp_weekDay = date.getDay();
                        if(tmp_weekDay<=cfg[_KEY_.MAX_DATE]){
                            ww = ww-1;
                            if(ww == 0){
                                ww=52;
                                yy = yy-1;
                            }
                        }
                        switch(patt){
                            case 0:
                                break;
                            case 2: tmp[0] = yy+"-99"+(ww<10?("0"+ww):ww);
                                break;
                        }
                        if(range&&!$.jIsArray(range)){
                            tmp[0] = range;
                            yy = parseInt(tmp[0].split("-")[0]);
                            w = tmp[0].split("-")[1],ww = parseInt(w.slice(0,1)=="0"?w.slice(1,2):w);
                        }
                        $("#"+dateSeg[0]).val(tmp[0]);
                        tmpSeg = [tmp[0]];
                        tmp[1] = $.jDateByWeek(yy,ww).curWeek(cfg[_KEY_.MAX_DATE]);
                        curDate = $.jClone(tmp[1]);
                        tmpCurDate = $.jClone(tmp[1]);
                        tmpField = yy+'\u5e74'+(ww<10?("0"+ww):ww)+'\u5468'+(bSimple?"":(" "+tmp[1].join(' \u81F3 ')));
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val(tmpField);
                    }else{
                        if(!inRapid){
                            widthSet(1, pickerId);
                            cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,1);
                        }else{
                            var finalDate = cache.get(_KEY_.CACHE_FINAL_DATE+pickerId);
                            if(finalDate[0]==finalDate[1]){
                                widthSet(5, pickerId);
                                cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,5);
                            }else{
                                widthSet(3, pickerId);
                                cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,3);
                            }
                        }
                        //为应对双十一期间,改成设置显示延迟时间,当延迟天数大于日数（延迟3天，当前为周二）,可能会造成当前月不能选,但会有跨年周的问题,仅对于有周月维度的日期控件
                        date = date.prevDay(cfg[_KEY_.MAX_DATE]);
                        //tmp[0] = date.prevDay(1).Format("yyyy-MM-dd");
                        tmp[0] = date.Format("yyyy-MM-dd");
                        if(range&&!$.jIsArray(range)){
                            tmp[0] = range;
                        }
                        tmpSeg = [tmp[0]];
                        tmp[0] = maxDate<tmp[0]?maxDate:tmp[0];
                        $("#"+dateSeg[0]).val(tmp[0]);
                        tmp[1] = [tmp[0],tmp[0]];
                        curDate = $.jClone(tmp[1]);
                        tmpCurDate = $.jClone(tmp[1]);
                        tmpField = tmp[0];
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val(tmpField);
                    }
                }else if(bRange){
                    if(bMonth){
                        widthSet(4, pickerId);
                        cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,4);
                        tmp[0] = date.Format("yyyy-MM");
                        if(!!range&&!$.jIsArray(range)){
                            tmp[0] = range;
                        }
                        $("#"+dateSeg[0]).val(tmp[0]);
                        tmpSeg = [tmp[0]];
                        var yy = parseInt(tmp[0].slice(0,4)),m = tmp[0].slice(5),mm = parseInt(m.slice(0,1)=="0"?m.slice(1):m.slice(0));
                        tmp[1] = new Date(yy, mm-1, 1).curMonth(cfg[_KEY_.MAX_DATE]);
                        curDate = $.jClone(tmp[1]);
                        tmpCurDate = $.jClone(tmp[1]);
                        tmpField=yy+'\u5e74'+m+'\u6708'+(bSimple?"":(" "+tmp[1].join(' \u81F3 ')));
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val(yy+'\u5e74'+m+'\u6708'+(bSimple?"":(" "+tmp[1].join(' \u81F3 '))));
                    }else if(bWeek){
                        widthSet(4, pickerId);
                        cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,4);
                        var yy = date.getFullYear(),ww = date.getWeekNumber(),w = "";
                        tmp[0] = yy+"-"+(ww<10?("0"+ww):ww);
                        if(!!range&&!$.jIsArray(range)){
                            tmp[0] = range;
                            yy = parseInt(tmp[0].split("-")[0]);
                            w = tmp[0].split("-")[1],ww = parseInt(w.slice(0,1)=="0"?w.slice(1,2):w);
                        }
                        $("#"+dateSeg[0]).val(tmp[0]);
                        tmpSeg = [tmp[0]];
                        tmp[1] = $.jDateByWeek(yy,ww).curWeek(cfg[_KEY_.MAX_DATE]);
                        curDate = $.jClone(tmp[1]);
                        tmpCurDate = $.jClone(tmp[1]);
                        tmpField=yy+'\u5e74'+(ww<10?("0"+ww):ww)+'\u5468'+(bSimple?"":(" "+tmp[1].join(' \u81F3 ')));
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val(yy+'\u5e74'+(ww<10?("0"+ww):ww)+'\u5468'+(bSimple?"":(" "+tmp[1].join(' \u81F3 '))));
                    }else{
                        widthSet(2, pickerId);
                        cache.set(_KEY_.CACHE_WIDTH_CODE+pickerId,2);
                        if(!!range[0]&&$.jIsArray(range[0])){
                            tmp[0] = date.prevDay(range[0][0]).Format("yyyy-MM-dd");
                            tmp[1] = date.prevDay(range[0][1]).Format("yyyy-MM-dd");
                        }else{
                            tmp[0] = date.prevDay(range[0]).Format("yyyy-MM-dd");
                            tmp[1] = date.prevDay(1).Format("yyyy-MM-dd");
                        }
                        tmp[1] = maxDate<tmp[1]?maxDate:tmp[1];
                        curDate = [tmp[0],tmp[1]];
                        tmpCurDate = [tmp[0],tmp[1]];
                        $("#"+dateSeg[0]).val(tmp[0]);
                        $("#"+dateSeg[1]).val(tmp[1]);
                        tmpSeg = [tmp[0],tmp[1]];
                        tmpField=tmp[0]+' \u81F3 '+tmp[1];
                        $('.widgetField input[type!=hidden]',$container).val(tmp[0]+' \u81F3 '+tmp[1]);
                    }
                }else{
                    //TODO
                }

                if (ifTimeModeEffective) {
                    if (hmsRegex.test($('.widgetField input[type!=hidden]',$container).val())) {
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val().replace(hmsRegex, $('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val()));
                    } else {
                        $('.widgetField input[type!=hidden]',currs[pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val()+"  "+$('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val());
                    }

                }
                cache.set(_KEY_.CACHE_LAST_DATE+pickerId, tmpCurDate)
                cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, tmpSeg);
                cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+pickerId, tmpField);
                cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, tmpCurDate)
                cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, tmpSeg);
                cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+pickerId, tmpField);
                //控件宽度=日历数*244px
                //这里是为了隐藏面板左侧的选择列表  改变宽度
                if(cfg&&cfg[_KEY_.DIMENSION_SEL]&&(cfg[_KEY_.IF_LEFT_VISIBLE]===false)){
                    $('.widgetCalendar', currs[pickerId]).css({"width": (Number(244*calendars))+"px"});
                }else if(cfg&&cfg[_KEY_.DIMENSION_SEL]&&!cfg[_KEY_.DIMENSION_SEL].ifExternal){
                    $('.widgetCalendar', currs[pickerId]).css({"width": (72+Number(244*calendars))+"px"});
                }else{
                    $('.widgetCalendar', currs[pickerId]).css({"width": (10+Number(244*calendars))+"px"});
                }
                $('.widgetCalendar .Calendar',currs[pickerId]).html("");
                cache.set(_KEY_.CACHE_PICKER+pickerId, $('.widgetCalendar .Calendar',currs[pickerId]).DatePicker({
                    cfg: cfg,
                    flat: true,
                    format: 'Y-m-d',
                    date: curDate,
                    current: date.prevDay(1).Format("yyyy-MM-dd"),
                    minDate:(minDate&&(new Date(minDate)).prevDay().Format('yyyy-MM-dd'))||0,
                    maxDate:(bSingle||range[1])?fbdDate:0,
                    calendars: calendars,//bMonth?1:2,//bSingle?1:(bRange?2:1),
                    type: type,
                    mode: 'range',
                    view: view,
                    starts: 0,
//    					onBeforeShow: function(){
//    						$('.inputDate').DatePickerSetDate($('.inputDate').val(), true);
//    					},
                    onChange: function(formated) {
                        var date = formated.toString(),dates = date.split(","),options = $(this).data('datepicker');
                        var tmpCfg = options.cfg
                            ,tmpPickerId = tmpCfg[_KEY_.ID]
                            ,tmpType = tmpCfg[_KEY_.TYPE]
                            ,tmpPatt = tmpCfg[_KEY_.PATTERN]
                            ,tmpDateSeg = tmpCfg[_KEY_.DATE_SEG]
                            ,tmpBSingle = (tmpType==_KEY_.SINGLE)
                            ,tmpBRange = (tmpType==_KEY_.RANGE)
                            ,tmpCustomChanges = tmpCfg[_KEY_.CUSTOM_CHANGE]||[];
                        var startDate = dates[0],endDate = dates[1],month = dates[2],year = dates[3],week = dates[4],tmpDate,tmpStartDate,tmpEndDate;
                        tmpDate = startDate.split("-");
                        tmpStartDate = new Date(tmpDate[0],tmpDate[1]-1,tmpDate[2]);
                        tmpDate = endDate.split("-");
                        tmpEndDate = new Date(tmpDate[0],tmpDate[1]-1,tmpDate[2]);

                        var isYear = (!!year&&year!=""),isWeek = (!!week&&week!=""),isMonth = (!!month&&month!=""),show = (isYear?(year+'\u5e74'):"");
                        if(tmpBSingle&&!isMonth&&!isWeek){
                            options.date[0] = options.date[1] = options.curSel;
                        }

                        if(tmpBSingle){
                            if(isWeek||isMonth){
                                if(tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                                    cache.set(_KEY_.CACHE_WIDTH_CODE+tmpPickerId,1);
                                }else{
                                    widthSet(1, tmpPickerId);
                                    $('#'+tmpDateSeg[0]).val(endDate);
                                }
                            }else{
                                startDate = endDate = (new Date(options.curSel)).Format("yyyy-MM-dd");
                                if(tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                                    cache.set(_KEY_.CACHE_WIDTH_CODE+tmpPickerId,1);
                                }else{
                                    widthSet(1, tmpPickerId);
                                    $('#'+tmpDateSeg[0]).val(endDate);
                                }
                            }
                        }else if(tmpBRange){
                            if(tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                                cache.set(_KEY_.CACHE_WIDTH_CODE+tmpPickerId,2);
                            }else{
                                widthSet(2, tmpPickerId);
                                $('#'+tmpDateSeg[0]).val(startDate);
                                $('#'+tmpDateSeg[1]).val(endDate);
                            }
                        }else{
                            //TODO
                        }
                        cache.set(_KEY_.CACHE_FINAL_DATE+tmpPickerId, [startDate, endDate]);
                        cache.set(_KEY_.CACHE_FINAL_DATESEG+tmpPickerId, [startDate, endDate]);
                        //非按月模式下设置天数上限
                        if(!tmpBSingle&&!isMonth){
                            if(range[1]){
                                if(tmpStartDate.DateDiff('d', tmpEndDate)>range[1]){
                                    $('.message.warn',currs[tmpPickerId]).html("请将查询区间设置在"+range[1]+"天之内");
                                    options.date[0] = cache.get('_tmp_start_date_','_core_');
                                    options.date[1] = cache.get('_tmp_end_date_','_core_');
                                    options.lastSel = !options.lastSel;
                                    return;
                                }else{
                                    options.firstSel = options.selVal;
                                    $('.message.warn',currs[tmpPickerId]).html("");
                                }
                            }
                        }

                        if(isWeek||isMonth){
                            if(tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                                cache.set(_KEY_.CACHE_WIDTH_CODE+tmpPickerId,4);
                            }else{
                                widthSet(4, tmpPickerId);
                            }
                        }
                        show += (isWeek?(((week<10)?("0"+week):week)+'\u5468'):"")||(isMonth?(((month<10)?("0"+month):month)+'\u6708'):"");
                        if((!isWeek&&!isMonth)||!bSimple){
                            show += " ";
                            if(bSingle && !isMonth && !isWeek) {
                                show += endDate;
                            }else{
                                //根据最大可选日期确定日期显示
                                if(now.Format('yyyy-MM-dd') == (new Date(endDate)).Format('yyyy-MM-dd')){
                                    dates.splice(1, 1, now.prevDay(tmpCfg[_KEY_.MAX_DATE]).Format('yyyy-MM-dd'));
                                }
                                show += dates.slice(0,2).join(' \u81F3 ');
                            }
                        }
                        if(tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                            cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+tmpPickerId, show);
                        }else{
                            $('.widgetField input[type!=hidden]',currs[tmpPickerId]).val(show);
                        }

                        if (ifTimeModeEffective) {
                            if (hmsRegex.test($('.widgetField input[type!=hidden]',$container).val())) {
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val().replace(hmsRegex, $('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val()));
                            } else {
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val()+"  "+$('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val());
                            }

                        }

                        switch(patt){
                            case 0:   break;
                            case 2:
                                if(tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                                    if(isWeek){
                                        cache.set(_KEY_.CACHE_FINAL_DATESEG+tmpPickerId, [year+"-99"+(week<10?("0"+week):week)]);
                                    }
                                    if(isMonth){
                                        cache.set(_KEY_.CACHE_FINAL_DATESEG+tmpPickerId, [year+"-"+(month<10?("0"+month):month)]);
                                    }
                                }else{
                                    if(isWeek){
                                        $('#'+tmpDateSeg[0]).val(year+"-99"+(week<10?("0"+week):week));
                                    }
                                    if(isMonth){
                                        $('#'+tmpDateSeg[0]).val(year+"-"+(month<10?("0"+month):month));
                                    }
                                }
                                break;
                            default:  break;
                        }
                        if(!tmpCfg[_KEY_.IF_FINAL_CONFIRM]){
                            if(tmpCfg[_KEY_.IF_SEPARATE_PART]){
                                var tmpDates = [$('#'+tmpCfg[_KEY_.HIDDEN_DOM][0]).val(), $('#'+tmpCfg[_KEY_.HIDDEN_DOM][1]).val()];
                                cache.set(_KEY_.CACHE_SUPER_RAPID, [new Date(tmpDates[0]), new Date(tmpDates[1])]);
                            }else{
                                cache.set(_KEY_.CACHE_SUPER_RAPID,startDate==endDate?new Date(endDate):[new Date(startDate), new Date(endDate)]);
                            }
                        }
                        //custom function of onchange
                        for(var i in tmpCustomChanges){
                            if(typeof(tmpCustomChanges[i])=="string"){
                                eval(tmpCustomChanges[i]+"()");
                            }else{
                                tmpCustomChanges[i]();
                            }
                        }

                        //去除快查显示状态
                        if(tmpCfg[_KEY_.RAPID_SEL]){
                            if(tmpCfg[_KEY_.RAPID_SEL].ifExternal){
                                currs[tmpPickerId].siblings().find("span.radiobox.r-checked").removeClass("r-checked");
                            }else{
                                $(".quick-chose",currs[tmpPickerId]).find("a.chosen").removeClass("chosen");
                            }
                        }
                        cache.remove(_KEY_.CACHE_RAPID+_containerId);     //清除快查模式
                    }
                }));
                //load param
                if(paramCfg){
                    var paramDate = paramCfg[_KEY_.DATE],paramRapid = paramCfg[_KEY_.RAPID];
                    if(!paramRapid&&paramDate){
                        if(paramDate.length>1){
                            widthSet(2, pickerId);
                            $('#'+dateSeg[0]).val(paramDate[0]);
                            $('#'+dateSeg[1]).val(paramDate[1]);
                            $('.widgetField input[type!=hidden]',currs[pickerId]).val(paramDate[0]+' \u81F3 '+paramDate[1]);
                            cache.get(_KEY_.CACHE_PICKER+pickerId).DatePickerSetDate(paramDate);
                            cache.set(_KEY_.CACHE_SUPER_RAPID, [new Date(paramDate[0], new Date(paramDate[1]))].sort());        //sort确保升序
                            cache.set(_KEY_.CACHE_LAST_DATE+pickerId, [$('#'+dateSeg[0]).val(), $('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, [$('#'+dateSeg[0]).val(), $('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, [$('#'+dateSeg[0]).val(), $('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, [$('#'+dateSeg[0]).val(), $('#'+dateSeg[1]).val()]);
                        }else{
                            paramDate = paramDate[0];
                            //传日
                            if(paramDate.length==10){
                                widthSet(1, pickerId);
                                $('#'+dateSeg[0]).val(paramDate);
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val(paramDate);
                                cache.get(_KEY_.CACHE_PICKER+pickerId).DatePickerSetDate(paramDate);
                                cache.set(_KEY_.CACHE_LAST_DATE+pickerId, [$('#'+dateSeg[0]).val()]);
                                cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, [$('#'+dateSeg[0]).val()]);
                                cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, [$('#'+dateSeg[0]).val()]);
                                cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, [$('#'+dateSeg[0]).val()]);
                                if(_cfg[_KEY_.IF_SEPARATE_PART]){
                                    var tmpDates = [$('#'+_cfg[_KEY_.HIDDEN_DOM][0]).val(), $('#'+_cfg[_KEY_.HIDDEN_DOM][1]).val()];
                                    cache.set(_KEY_.CACHE_SUPER_RAPID, [new Date(tmpDates[0]), new Date(tmpDates[1])]);
                                }else{
                                    cache.set(_KEY_.CACHE_SUPER_RAPID,new Date(paramDate));
                                }
                            }
                            //传周
                            else if((paramDate.length==9||paramDate.length==8)&&paramDate.indexOf("99")>=0){
                                widthSet(4, pickerId);
                                var y=parseInt(paramDate.slice(0,4)),ww=paramDate.slice(paramDate.indexOf("99")).replace("99",""),w=parseInt(ww.indexOf("0")==0?ww.replace("0",""):ww);
                                var tmpParamDate = $.jGetWeekByNum(w, y),fDay=tmpParamDate[0],lDay=tmpParamDate[1];
                                cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(fDay), new Date(lDay)]);
                                if(bSingle){
                                    $('#'+dateSeg[0]).val(paramDate);
                                    cache.set(_KEY_.CACHE_LAST_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, [$('#'+dateSeg[0]).val()]);
                                    cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, [$('#'+dateSeg[0]).val()]);
                                }else if(bRange){
                                    $('#'+dateSeg[0]).val(fDay);
                                    $('#'+dateSeg[1]).val(lDay);
                                    cache.set(_KEY_.CACHE_LAST_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, [$('#'+dateSeg[0]).val(),$('#'+dateSeg[1]).val()]);
                                    cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, [$('#'+dateSeg[0]).val(),$('#'+dateSeg[1]).val()]);
                                }
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val(y+'\u5e74'+ww+'\u5468'+" "+fDay+' \u81F3 '+lDay);
                                cache.get(_KEY_.CACHE_PICKER+pickerId).DatePickerSetDate([fDay, lDay]);
                                cache.set(_KEY_.CACHE_SUPER_RAPID, [new Date(fDay), new Date(lDay)]);
                            }
                            //传月
                            else{
                                widthSet(4, pickerId);
                                var y=parseInt(paramDate.slice(0,4)),m=parseInt(paramDate.slice(5,7).indexOf("0")>=0?paramDate.slice(6,7):paramDate.slice(5,7)),d=1;
                                var tmpParamDate = new Date(y,m-1,d),fDay=tmpParamDate.firstDayOfMonth().Format("yyyy-MM-dd"),lDay=tmpParamDate.lastDayOfMonth().Format("yyyy-MM-dd");
                                cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(fDay), new Date(lDay)]);
                                if(bSingle){
                                    $('#'+dateSeg[0]).val(paramDate);
                                    cache.set(_KEY_.CACHE_LAST_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, [$('#'+dateSeg[0]).val()]);
                                    cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, [$('#'+dateSeg[0]).val()]);
                                }else if(bRange){
                                    $('#'+dateSeg[0]).val(fDay);
                                    $('#'+dateSeg[1]).val(lDay);
                                    cache.set(_KEY_.CACHE_LAST_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, [$('#'+dateSeg[0]).val(),$('#'+dateSeg[1]).val()]);
                                    cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, [lDay, fDay]);
                                    cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, [$('#'+dateSeg[0]).val(),$('#'+dateSeg[1]).val()]);
                                }
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val(tmpParamDate.getFullYear()+'\u5e74'+paramDate.slice(5,7)+'\u6708'+" "+fDay+' \u81F3 '+lDay);
                                cache.get(_KEY_.CACHE_PICKER+pickerId).DatePickerSetDate([tmpParamDate.firstDayOfMonth().Format("yyyy-MM-dd"), tmpParamDate.lastDayOfMonth().Format("yyyy-MM-dd")]);
                                cache.set(_KEY_.CACHE_SUPER_RAPID, [new Date(fDay), new Date(lDay)]);
                            }
                            cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+pickerId, $('.widgetField input[type!=hidden]',currs[pickerId]).val());
                            cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+pickerId, $('.widgetField input[type!=hidden]',currs[pickerId]).val());
                        }

                        if (ifTimeModeEffective) {
                            if (hmsRegex.test($('.widgetField input[type!=hidden]',$container).val())) {
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val().replace(hmsRegex, $('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val()));
                            } else {
                                $('.widgetField input[type!=hidden]',currs[pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val()+"  "+$('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val());
                            }

                        }

                    }

                    /* 根据参数区分日/月/周 同步维度状态 START */
                    var $dimensionLi = $('ul li[val="'+view+'"]',currs[pickerId]);
                    $dimensionLi = $dimensionLi.length>0?$dimensionLi:$('ul li:first',currs[pickerId]);
                    cache.set(_KEY_.CACHE_DIMENSION+_containerId,[$dimensionLi.attr('val'), $dimensionLi.text()]);
                    $dimensionLi.addClass("sel").css({"border-right": "0 none"});
                    $dimensionLi.siblings().removeClass("sel");
                    /* 根据参数区分日/月/周 同步维度状态 END */

                    $.extend(cfg[_KEY_.PARAM], {date: null});        //清除日期传参
                }

                var confirmPicker = function(_valSet,_pickerId){
                    if(_valSet.dateSeg.length==2){
                        for(var i in _valSet.dateSeg){
                            $('#'+_valSet.idSeg[i],currs[_pickerId]).val(_valSet.dateSeg[i]);
                        }
                    }else{
                        $('#'+_valSet.idSeg[0],currs[_pickerId]).val(_valSet.dateSeg[0]);
                    }
                    $('.widgetField input[type!=hidden]',currs[_pickerId]).val(_valSet.field);
                    if (ifTimeModeEffective) {
                        if (hmsRegex.test($('.widgetField input[type!=hidden]',$container).val())) {
                            $('.widgetField input[type!=hidden]',currs[_pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val().replace(hmsRegex, $('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val()));
                        } else {
                            $('.widgetField input[type!=hidden]',currs[_pickerId]).val($('.widgetField input[type!=hidden]',currs[pickerId]).val()+"  "+$('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val());
                        }

                    }
                    cache.get(_KEY_.CACHE_PICKER+_pickerId).DatePickerSetDate(_valSet.date.length>1?_valSet.date:_valSet.date[0]);
                    cache.set(_KEY_.CACHE_SUPER_RAPID, _valSet.date.length>1?[new Date(_valSet.date[0]), new Date(_valSet.date[1])]:(new Date(_valSet.date[0])));
                };
                var _wc_hide=function(_pickerId){
                    if(cfg[_KEY_.RAPID_SEL]){
                        $('.radiobox[desc="'+cache.get(_KEY_.CACHE_RAPID+_containerId)+'"]').addClass('r-checked');      //还原快查状态
                    }
                    if(cfg[_KEY_.IF_FINAL_CONFIRM]){
                        confirmPicker({
                            id: "widget",
                            idSeg: dateSeg,
                            field: cache.get(_KEY_.CACHE_LAST_FIELD_SHOW+pickerId),
                            dateSeg: cache.get(_KEY_.CACHE_LAST_DATESEG+pickerId),
                            date: cache.get(_KEY_.CACHE_LAST_DATE+pickerId)
                        },pickerId);
                        cache.set(_KEY_.CACHE_FINAL_DATE+pickerId, cache.get(_KEY_.CACHE_LAST_DATE+pickerId));
                        cache.set(_KEY_.CACHE_FINAL_DATESEG+pickerId, cache.get(_KEY_.CACHE_LAST_DATESEG+pickerId));
                        cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+pickerId, cache.get(_KEY_.CACHE_LAST_FIELD_SHOW+pickerId));
                    }
                    $('.widgetCalendar',currs[_pickerId]).stop().animate({height:0}, 100,function(){
                        currs[_pickerId].removeClass('widgetField-on');
                    });
                    states[_pickerId] = false;
                    $('.widgetField>a',currs[_pickerId]).removeClass("on");
                },closeState = function(e){
                    states[e.data.pickerId] = false;
                };
                function resizePosition (currentDom,parentDom){
                    if($(window).width()-parentDom.offset().left<currentDom.width()){
                        currentDom.css({right:"0",left:"auto"});
                    }else{
                        currentDom.css({right:"auto",left:"0"});
                    }
                }
                states[pickerId] = false;
                if(ifVisible){
                    $('.widgetCalendar',currs[pickerId]).height($('.widgetCalendar div:first',currs[pickerId]).get(0).offsetHeight);
                    currs[pickerId].addClass('widgetField-on');
                    $('.widgetField>a',currs[pickerId]).addClass("on");
                    resizePosition($('.widgetCalendar',currs[pickerId]),currs[pickerId]);
                }else{
                    if (!cfg[_KEY_.IF_DROPDOWN_DISABLED]) {       //是否禁用面板下拉
                        /*$('.widgetField>a,.widgetField input',currs[pickerId])*/
                        if(cfg[_KEY_.IF_HOVER_SHOW]===false){
                            currs[pickerId].delegate('div.widgetField','click',function(e){
                                var $picker = $(e.target).closest(".widget"),tmpPickerId = $picker.jId();
                                states[tmpPickerId]?$('.widgetField>a',currs[tmpPickerId]).removeClass("on"):$('.widgetField>a',currs[tmpPickerId]).addClass("on");
                                //$('.Calendar td,.calendar th',currs[tmpPickerId]).off('click',closeState).on('click',{pickerId: tmpPickerId},closeState);
                                states[tmpPickerId]=parseInt($('.widgetCalendar',currs[tmpPickerId]).css("height"))==0?true:false;
                                $('.widgetCalendar',currs[tmpPickerId]).stop().animate({height: parseInt($('.widgetCalendar',currs[tmpPickerId]).css("height"))!=0 ? 0 : $('.widgetCalendar div:first',currs[tmpPickerId]).get(0).offsetHeight}, 100);
                                resizePosition($('.widgetCalendar',currs[pickerId]),currs[pickerId]);
                                currs[pickerId].toggleClass('widgetField-on');
                                if(states[tmpPickerId]) {
                                    $('.c-btn',currs[tmpPickerId]).on('click',function(){
                                        _wc_hide(tmpPickerId);
                                    });
                                }
                                return false;
                            })
                        }else{
                            currs[pickerId].off('mouseenter').on('mouseenter', function(e){
                                var $picker = $(e.target).closest(".widget"),tmpPickerId = $picker.jId();
                                states[tmpPickerId]?$('.widgetField>a',currs[tmpPickerId]).removeClass("on"):$('.widgetField>a',currs[tmpPickerId]).addClass("on");
                                currs[tmpPickerId].addClass('widgetField-on');
                                $('.widgetCalendar',currs[tmpPickerId]).stop().animate({height: states[tmpPickerId] ? 0 : $('.widgetCalendar div:first',currs[tmpPickerId]).get(0).offsetHeight}, 100);
                                states[tmpPickerId] = true;
                                resizePosition($('.widgetCalendar',currs[pickerId]),currs[pickerId]);
                                $('.Calendar td,.calendar th',currs[tmpPickerId]).off('click',closeState).on('click',{pickerId: tmpPickerId},closeState);
                                if(states[tmpPickerId]) {
                                    $('.c-btn',currs[tmpPickerId]).on('click',function(){
                                        _wc_hide(tmpPickerId);
                                    });

                                    $(this).off('mouseleave').on('mouseleave',function(){
                                        _wc_hide(tmpPickerId);
                                    });
                                }else {
                                    $(this).off('mouseleave',_wc_hide);
                                }
                                return false;
                            });
                        }
                    }
                }

                /* 初始化快查 START */
                if(cfg[_KEY_.RAPID_SEL]){
                    initDateRapid(_containerId, widthSet, cfg, currs[pickerId]);
                }
                /* 初始化快查 END */
                /* 追加自定义日期面板日期点击事件 START */
                for(var i in customChanges){
                    customChanges[i]();
                }
                /* 追加自定义日期面板日期点击事件 END */
                /**
                 * 拦截页面的onclick，并加入控件click队列
                 * @type {*|jQuery}
                 */
                if(cfg[_KEY_.IF_FINAL_CONFIRM]){
                    var click = $(".btn:first",currs[pickerId]).attr("onclick");
                    if(click!=""){
                        cache.set('_datepicker_'+pickerId+"_click_",click);
                        $(".btn:first",currs[pickerId]).attr("onclick","");
                    }else{
                        click = cache.get('_datepicker_'+pickerId+"_click_");
                    }

                    $(".btn:first",currs[pickerId]).off('click').on("click", function(){
                        if(!cache.get(_KEY_.CACHE_RAPID+_containerId)){      //判断非快查状态
                            confirmPicker({
                                id: "widget",
                                idSeg: dateSeg,
                                dateSeg: cache.get(_KEY_.CACHE_FINAL_DATESEG+pickerId),
                                field: cache.get(_KEY_.CACHE_FINAL_FIELD_SHOW+pickerId),
                                date: cache.get(_KEY_.CACHE_FINAL_DATE+pickerId)
                            },pickerId);
                            cache.set(_KEY_.CACHE_LAST_DATE+pickerId, cache.get(_KEY_.CACHE_FINAL_DATE+pickerId));
                            cache.set(_KEY_.CACHE_LAST_DATESEG+pickerId, cache.get(_KEY_.CACHE_FINAL_DATESEG+pickerId));
                            cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+pickerId, cache.get(_KEY_.CACHE_FINAL_FIELD_SHOW+pickerId));
                        }
                    }).on("click",function(){
                        eval(click);
                    });
                }else{
                    $(" .btn.query",currs[pickerId]).off("click");
                }
                /* 自定义查询事件初始化 START */
                $(" .btn.query",currs[pickerId]).on("click",function(){
                    widthSet(cache.get(_KEY_.CACHE_WIDTH_CODE+pickerId), pickerId);
                    if($.type(cfg[_KEY_.CUSTOM_QUERY])==='function'){
                        cfg[_KEY_.CUSTOM_QUERY]();
                    }else if(cfg[_KEY_.CUSTOM_QUERY] != "null"){
                        eval(cfg[_KEY_.CUSTOM_QUERY]+"()");
                    }
                    /* 连续快查并且确定日期的场景下控制 快查的状态 START */
                    if(cfg[_KEY_.IF_SEPARATE_PART]){        //双日期控件模式
                        if(cfg[_KEY_.IF_FINAL_CONFIRM]){
                            var endDate = $('#'+cfg[_KEY_.HIDDEN_DOM][1]).val();
                            if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.prevDay(1).Format('yyyyMMdd'))){
                                $("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                            }else{
                                $("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                            }
                            if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                                $("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                            }else{
                                $("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                            }
                            if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                                $("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                            }else{
                                $("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                            }
                        }
                    }else{      //单日期控件模式
                        if((cfg[_KEY_.RAPID_SEL]||{}).superItem&&cfg[_KEY_.IF_FINAL_CONFIRM]){
                            var endDate = cache.get(_KEY_.CACHE_FINAL_DATE+pickerId)[1];
                            if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.prevDay(1).Format('yyyyMMdd'))){
                                $("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                            }else{
                                $("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                            }
                            if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                                $("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                            }else{
                                $("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                            }
                            if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                                $("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                            }else{
                                $("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                            }
                        }
                    }
                    /* 连续快查并且确定日期的场景下控制 快查的状态 END */
                });
                /* 自定义查询事件初始化 END */
            }
        },
        initDateRapid = function(_containerId, _widthSet, _cfg, _$datepicker){
            var $_ = $,
                $container = $_('#'+_containerId),
                cfg = _cfg,
                pickerId = cfg[_KEY_.ID],
                paramCfg = cfg[_KEY_.PARAM],
                paramRapid = paramCfg[_KEY_.RAPID],
                specDate,
                type = cfg[_KEY_.TYPE],
                dateSeg = cfg[_KEY_.DATE_SEG],
                bSingle = (type==_KEY_.SINGLE),
                bRange = (type==_KEY_.RANGE),
                bSimple = (cfg[_KEY_.STYLE]==_KEY_.SIMPLE),
                $a,
                now = new Date(),
                date = cache.get(_KEY_.CACHE_SUPER_RAPID)||now.prevDay(cfg[_KEY_.MAX_DATE]),
                ifFinalConfirm = cfg[_KEY_.IF_FINAL_CONFIRM],
                dateIpt="",
                startDate,
                endDate,
                nonDate=false,
                prevDayFirst = false,
                ifTimeModeEffective = bSingle && !!_cfg[_KEY_.TIME_MODE],
                hmsRegex = new RegExp((_cfg[_KEY_.TIME_MODE]||"").replace(/[hms]/g, '\\d'), 'gi');

            if(cfg[_KEY_.RAPID_SEL]&&(cfg[_KEY_.RAPID_SEL].ifExternal||cfg[_KEY_.RAPID_SEL].noPicker)){
                $a = _$datepicker.siblings(".rapidSel").find(".radiobox,.super-rapid");
            }else{
                $a = $_(".widgetCalendar .quick-chose",_$datepicker).find("a");
            }
            var rapidSet = function(_desc, _val, _ref){
                var bSpec = _val==0, bBoth = _val==121, tmpNum;   //设置0-组件自动计算日期和特殊日期格式，如90/95/99;设置121-组件转换为起始和结束
                /* 判断是否触发特殊快查 START */
                var ifSuperItem = (function(){
                        return _desc in cfg[_KEY_.RAPID_SEL].superItem;
                    })(),
                    ifSuperWeekRef = ifSuperItem&&_ref==_KEY_.VWEEK,
                    ifSuperMonthRef = ifSuperItem&&_ref==_KEY_.VMONTH;

                if(!ifSuperItem){       //若普通快查清除面板选中状态
                    $_(".Calendar .datepickerSelected",_$datepicker).removeClass("datepickerSelected");
                }
                /* 判断是否触发特殊快查 END */
                var yesterdayStr = now.prevDay(cfg[_KEY_.MAX_DATE]).Format('yyyyMMdd');
                date = cache.get(_KEY_.CACHE_SUPER_RAPID)||date;
                switch(_desc){
                    case _KEY_.PREVWEEK:   	startDate = now.preWeek(1, 'yyyy-MM-dd')[0];
                        endDate = now.preWeek(1, 'yyyy-MM-dd')[1];
                        tmpNum = now.prevDay(7).getWeekNumber();
                        specDate = ""+now.prevDay(7).getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.CURWEEK:    	startDate = now.curWeek(cfg[_KEY_.MAX_DATE])[0];
                        endDate = now.curWeek(cfg[_KEY_.MAX_DATE])[1];
                        tmpNum = now.getWeekNumber();
                        specDate = ""+now.getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH:  	startDate = now.preMonth(1, 'yyyy-MM-dd')[0];
                        endDate = now.preMonth(1, 'yyyy-MM-dd')[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.CURMONTH:    startDate = now.curMonth(cfg[_KEY_.MAX_DATE])[0];
                        endDate = now.curMonth(cfg[_KEY_.MAX_DATE])[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV1:  specDate = startDate = endDate = now.prevDay().Format("yyyy-MM-dd");
                        cache.set(_KEY_.CACHE_SUPER_RAPID,new Date(endDate));
                        break;
                    case _KEY_.PREV2:  specDate = startDate = endDate = now.prevDay(2).Format("yyyy-MM-dd");
                        cache.set(_KEY_.CACHE_SUPER_RAPID,new Date(endDate));
                        break;
                    case _KEY_.PREV3:   	startDate = now.prevDay3()[0];
                        endDate = now.prevDay3()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV7:    	startDate = now.prevDay7()[0];
                        endDate = now.prevDay(cfg[_KEY_.MAX_DATE]).Format("yyyy-MM-dd");
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV15:    	startDate = now.prevDay15()[0];
                        endDate = now.prevDay15()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV30:    	startDate = now.prevDay30()[0];
                        endDate = now.prevDay30()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV90:    	startDate = now.prevDay90()[0];
                        endDate = now.prevDay90()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVSEASON:
                        startDate = now.prevSeason()[0];
                        endDate = now.prevSeason()[1];
                        var counter = now.prevSeasonCounter();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_SEASON + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH3:
                        startDate = now.prevMonth3()[0];
                        endDate = now.prevMonth3()[1];
                        var counter = now.prevMonthCounter3();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH6:
                        startDate = now.prevMonth6()[0];
                        endDate = now.prevMonth6()[1];
                        var counter = now.prevMonthCounter6();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH12:
                        startDate = now.prevMonth12()[0];
                        endDate = now.prevMonth12()[1];
                        var counter = now.prevMonthCounter12();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        break;
                    case _KEY_.PREVHALFYEAR:
                        startDate = now.prevMonth6()[0];
                        endDate = now.prevMonth6()[1];
                        var counter = now.prevMonthCounter6();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVYEAR:
                        startDate = now.prevMonth12()[0];
                        endDate = now.prevMonth12()[1];
                        var counter = now.prevMonthCounter12();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV_WEEK:
                        date = $.jIsArray(date)?date[0]:date;
                        startDate = date.preWeek(1, 'yyyy-MM-dd')[0];
                        endDate = date.preWeek(1, 'yyyy-MM-dd')[1];
                        tmpNum = date.prevDay(7).getWeekNumber();
                        specDate = ""+date.prevDay(7).getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                        }
                        date = date.preWeek();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.NEXT_WEEK:
                        date = $.jIsArray(date)?date[1]:date;
                        startDate = date.preWeek(-1, 'yyyy-MM-dd')[0];
                        endDate = date.preWeek(-1, 'yyyy-MM-dd')[1];
                        tmpNum = new Date(startDate).getWeekNumber();
                        specDate = ""+date.getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        }
                        date = date.preWeek(-1);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.PREV_DAY:
                        date = $.jIsArray(date)?date[0]:date;
                        specDate = startDate = endDate = date.prevDay().Format("yyyy-MM-dd");
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                        }
                        date = date.prevDay();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.NEXT_DAY:
                        date = $.jIsArray(date)?date[1]:date;
                        specDate = startDate = endDate = date.nextDay().Format("yyyy-MM-dd");
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        }
                        date = date.nextDay();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.PREV_MONTH:
                        date = $.jIsArray(date)?date[0]:date;
                        startDate = date.preMonth(1, 'yyyy-MM-dd')[0];
                        endDate = date.preMonth(1, 'yyyy-MM-dd')[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                        }
                        date = date.preMonth();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.NEXT_MONTH:
                        date = $.jIsArray(date)?date[1]:date;
                        startDate = date.preMonth(-1, 'yyyy-MM-dd')[0];
                        endDate = date.preMonth(-1, 'yyyy-MM-dd')[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        }
                        date = date.preMonth(-1);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.RESET_DATE:
                        cache.set(_KEY_.CACHE_SUPER_RAPID, now.prevDay(cfg[_KEY_.MAX_DATE]));
                        /* 禁用后一天/下一周/下一月快查 START */
                        $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        /* 禁用后一天/下一周/下一月快查 END */
                        return;
                        break;
                    default:	nonDate = true;
                        break;
                }

                //if rapid is !date
                if(!nonDate){
                    if(bRange){
                        _widthSet(3, _$datepicker.jId());
                        //判断是否特殊值（生成固定值由推送数据计算）
                        if(bSpec){
                            $_('#'+dateSeg[0]).val(specDate);
                            $_('#'+dateSeg[1]).val(specDate);
                        }else{
                            if(bBoth){
                                $_('#'+dateSeg[0]).val(startDate);
                                $_('#'+dateSeg[1]).val(endDate);
                            }else{
                                $_('#'+dateSeg[0]).val(_val);
                                $_('#'+dateSeg[1]).val(_val);
                            }
                        }
                        cache.get(_KEY_.CACHE_PICKER+cfg[_KEY_.ID]).DatePickerSetDate([startDate, endDate]);
                        dateIpt += _desc+ " "+startDate+' \u81F3 '+endDate;
                        if(ifFinalConfirm){
                            cache.set(_KEY_.CACHE_FINAL_DATE+cfg[_KEY_.ID], [$_('#'+dateSeg[0]).val(),$_('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_FINAL_DATESEG+cfg[_KEY_.ID], [$_('#'+dateSeg[0]).val(),$_('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+cfg[_KEY_.ID], dateIpt);
                            cache.set(_KEY_.CACHE_LAST_DATE+cfg[_KEY_.ID], [$_('#'+dateSeg[0]).val(),$_('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_LAST_DATESEG+cfg[_KEY_.ID], [$_('#'+dateSeg[0]).val(),$_('#'+dateSeg[1]).val()]);
                            cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+cfg[_KEY_.ID], dateIpt);
                        }
                    }else if(bSingle){
                        //判断是否特殊值（生成固定值由推送数据计算）
                        if(bSpec){
                            $_('#'+dateSeg[0]).val(specDate);
                        }else{
                            $_('#'+dateSeg[0]).val(_val);
                        }
                        //判断是否属于范围日期
                        if(startDate==endDate){
                            if(ifSuperItem){        //判断是否特殊快查中的前一天、后一天
                                _widthSet(1, _$datepicker.jId());
                                dateIpt += startDate;
                            }else{
                                _widthSet(5, _$datepicker.jId());
                                dateIpt += _desc+(bSimple?"":(" "+startDate));
                            }
                            cache.get(_KEY_.CACHE_PICKER+cfg[_KEY_.ID]).DatePickerSetDate([startDate, endDate]);
                        }else{
                            var tmpDate = new Date(startDate);
                            if(ifSuperWeekRef){     //判断是否特殊快查模式下的周视图
                                _widthSet(4, _$datepicker.jId());
                                var ww = tmpDate.getWeekNumber();
                                ww = ww<10?("0"+ww):ww;
                                dateIpt += tmpDate.getCurWeek()[0]+'\u5e74'+ww+'\u5468'+(bSimple?"":(" "+startDate+' \u81F3 '+endDate));
                            }else if(ifSuperMonthRef){     //判断是否特殊快查模式下的月视图
                                _widthSet(4, _$datepicker.jId());
                                dateIpt += tmpDate.Format('yyyy年MM月')+(bSimple?"":(" "+startDate+' \u81F3 '+endDate));
                            }else{      //普通快查模式
                                _widthSet(3, _$datepicker.jId());
                                dateIpt += _desc+(bSimple?"":(" "+startDate+' \u81F3 '+endDate));
                            }
                            cache.get(_KEY_.CACHE_PICKER+cfg[_KEY_.ID]).DatePickerSetDate([startDate, endDate]);
                        }
                        if(ifFinalConfirm){
                            cache.set(_KEY_.CACHE_FINAL_DATE+cfg[_KEY_.ID], [startDate,endDate]);
                            cache.set(_KEY_.CACHE_FINAL_DATESEG+cfg[_KEY_.ID], [$_('#'+dateSeg[0]).val()]);
                            cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+cfg[_KEY_.ID], dateIpt);
                            cache.set(_KEY_.CACHE_LAST_DATE+cfg[_KEY_.ID], [startDate,endDate]);
                            cache.set(_KEY_.CACHE_LAST_DATESEG+cfg[_KEY_.ID], [$_('#'+dateSeg[0]).val()]);
                            cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+cfg[_KEY_.ID], dateIpt);
                        }
                    }
                    $_('.widgetField input[readonly=readonly]',_$datepicker).val(dateIpt);
                    if (ifTimeModeEffective) {
                        if (hmsRegex.test($('.widgetField input[type!=hidden]',_$datepicker).val())) {
                            $('.widgetField input[type!=hidden]',_$datepicker).val($('.widgetField input[type!=hidden]',_$datepicker).val().replace(hmsRegex, $('.time-picker input', $container).val()));
                        } else {
                            $('.widgetField input[type!=hidden]',_$datepicker).val($('.widgetField input[type!=hidden]',_$datepicker).val()+"  "+$('.time-picker input', $container).val());
                        }

                    }
                }
                dateIpt = "";
            }

            /* 快查接收页面跳转参数 START */
            if(paramRapid){
                _widthSet(3,_$datepicker.jId());
                $a.filter('.radiobox').each(function(){
                    if(paramRapid==$_(this).attr("desc")){
                        if(cfg[_KEY_.RAPID_SEL]&&(cfg[_KEY_.RAPID_SEL].ifExternal||cfg[_KEY_.RAPID_SEL].noPicker)){
                            $_(this).siblings().removeClass("r-checked");
                            $_(this).addClass("r-checked");
                        }else{
                            $_(this).addClass("chosen");
                        }
                        rapidSet($_(this).attr("desc"),$_(this).attr("val"),$_(this).attr("ref"));
                    }
                });

                /* 快查接收传参后同步维度状态 START */
                var $dimensionLi = $_('ul li[val="'+_KEY_.VODAY+'"]',_$datepicker);
                $dimensionLi = $dimensionLi.length>0?$dimensionLi:$_('ul li:first',_$datepicker);
                cache.set(_KEY_.CACHE_DIMENSION+_containerId,[$dimensionLi.attr('val'), $dimensionLi.text()]);
                $dimensionLi.addClass("sel").css({"border-right": "0 none"});
                $dimensionLi.siblings().removeClass("sel");
                /* 快查接收传参后同步维度状态 END */

                $.extend(cfg[_KEY_.PARAM], {rapid: null});        //清除快查传参
            }
            /* 快查接收页面跳转参数 END */
            /* 连续快查、普通快查事件初始化 START */
            $a.off("click").on('click', function(_e){
                if($_(this).is('.r-disabled')||$_(this).is('.disabled')){
                    _e.preventDefault();
                    _e.stopPropagation();
                    return;
                }else{
                    /* 普通快查单选按钮渲染 START */
                    if($_(this).is('.radiobox')){        //筛除连续快查部分
                        if(cfg[_KEY_.RAPID_SEL]&&(cfg[_KEY_.RAPID_SEL].ifExternal||cfg[_KEY_.RAPID_SEL].noPicker)){
                            $_(this).siblings().removeClass("r-checked");
                            $_(this).addClass("r-checked");
                        }else{
                            $_(".widgetCalendar .quick-chose",_$datepicker).find("a.chosen").removeClass("chosen");
                            $_(this).addClass("chosen");
                        }
                    }else{      //去除普通快查部分的选中状态
                        $_(this).siblings('.r-checked').removeClass("r-checked");
                    }
                    /* 普通快查单选按钮渲染 END */
                    /* 判断是否有维度选择，点击快查变换相应的维度模式 START */
                    if(cfg[_KEY_.DIMENSION_SEL]&&!cfg[_KEY_.DIMENSION_SEL].ifExternal){
                        var $dimensionLi = $_('ul li[val="'+$_(this).attr("ref")+'"]',_$datepicker);
                        $dimensionLi = $dimensionLi.length>0?$dimensionLi:$_('ul li:first',_$datepicker);
                        cache.set(_KEY_.CACHE_DIMENSION+_containerId,[$dimensionLi.attr('val'), $dimensionLi.text()]);
                        $dimensionLi.addClass("sel").css({"border-right": "0 none"});
                        $dimensionLi.siblings().removeClass("sel");
                        $_(".datepicker",_$datepicker).remove();
                        initDatePicker(_containerId, $_.extend(cfg, {view: $dimensionLi.attr("val")}));
                    }
                    /* 判断是否有维度选择，点击快查变换相应的维度模式 END */

                    var desc = $_(this).attr("desc"),val = $_(this).attr("val"),ref = $_(this).attr("ref");
                    cache.set(_KEY_.CACHE_RAPID+_containerId, [val, desc]);

                    rapidSet(desc,val,ref);
                    $_("#"+_$datepicker.jId()+"RapidSel").val(desc);

                    /* 快查直接请求数据 START */
                    if(_cfg[_KEY_.CUSTOM_QUERY]){
                        _cfg[_KEY_.CUSTOM_QUERY]();
                    }
                    /* 快查直接请求数据 END */
                }
            });
            /* 普通快查事件初始化 END */
        },
        /**
         * 双控件快查逻辑
         */
        initSeparateDateRapid = function(_containerId, _cfg, _pickerIds){
            var $_ = $,
                $container = $_('#'+_containerId),
                cfg = _cfg,
                calendarIds = [_pickerIds[0]+'Calendar', _pickerIds[1]+'Calendar'],
                $datepickers = [$('#'+_pickerIds[0]),$('#'+_pickerIds[1])],
                $pickers = [$('.Calendar', $datepickers[0]),$('.Calendar', $datepickers[1])],
                widthSet = function(_model, _$datepicker){
                    switch(_model){
                        case 1:  $('.widgetField',_$datepicker).css({"width": "170px"});
                            $('.widgetField input',_$datepicker).css({"width": "135px"});
                            break;
                        case 5:  $('.widgetField',_$datepicker).css({"width": "212px"});
                            $('.widgetField input',_$datepicker).css({"width": "175px"});
                            break;
                        default: break;
                    }
                };
            /* maxDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） START */
            cfg[_KEY_.MAX_DATE] = (function(){
                var tmpMaxDate = cfg[_KEY_.MAX_DATE]||1;
                if(/^\d{4}[-\/]\d{2}$/.test(tmpMaxDate)){      //判断maxDate: 2015-01
                    tmpMaxDate = (new Date(tmpMaxDate).lastDayOfMonth()).Format('yyyy-MM-dd');
                }
                if(/^\d{4}[-\/]\d{2}[-\/]\d{2}$/.test(tmpMaxDate)){      //判断maxDate: 2015-01-01
                    tmpMaxDate = Math.floor(((new Date())-(new Date(tmpMaxDate)))/864e5);
                }
                return tmpMaxDate<0?1:tmpMaxDate;
            })();
            /* maxDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） END */
            /* minDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） START */
            cfg[_KEY_.MIN_DATE] = (function(){
                var tmpMinDate = cfg[_KEY_.MIN_DATE]||'2000-12-31';
                if(/^\d{4}[-\/]\d{2}$/.test(tmpMinDate)){
                    tmpMinDate = (new Date(tmpMinDate)).Format('yyyy-MM-dd');
                }
                return tmpMinDate;
            })();
            /* minDate配置兼容2015-02-01（2015/02/01、2015/02、2015-02） END */
            var paramCfg = cfg[_KEY_.PARAM],
                paramRapid = (paramCfg||{})[_KEY_.RAPID],
                bSimple = cfg[_KEY_.STYLE],
                view = cfg[_KEY_.VIEW],
                type = cfg[_KEY_.TYPE],
                dateSeg = cfg[_KEY_.DATE_SEG],
                specDate,
                $a = $_(".rapidSel", $container).find(".radiobox,.super-rapid"),
                now = new Date(),
                date = cache.get(_KEY_.CACHE_SUPER_RAPID)||now.prevDay(cfg[_KEY_.MAX_DATE]),
                ifFinalConfirm = _cfg[_KEY_.IF_FINAL_CONFIRM],
                dateIpt="",
                startDate,
                endDate,
                nonDate=false;

            var rapidSet = function(_desc, _val, _ref){
                var bSpec = _val==0, bBoth = _val==121, tmpNum;   //设置0-组件自动计算日期和特殊日期格式，如90/95/99;
                /* 判断是否触发特殊快查 START */
                var ifSuperItem = (function(){
                        return _desc in cfg[_KEY_.RAPID_SEL].superItem;
                    })(),
                    ifSuperWeekRef = ifSuperItem&&_ref==_KEY_.VWEEK,
                    ifSuperMonthRef = ifSuperItem&&_ref==_KEY_.VMONTH;

                if(!ifSuperItem){       //若普通快查清除面板选中状态
                    $_(".Calendar .datepickerSelected",$container).removeClass("datepickerSelected");
                }
                /* 判断是否触发特殊快查 END */
                var yesterdayStr = now.prevDay(cfg[_KEY_.MAX_DATE]).Format('yyyyMMdd');
                date = cache.get(_KEY_.CACHE_SUPER_RAPID)||date;
                switch(_desc){
                    case _KEY_.PREVWEEK:   	startDate = now.preWeek(1, 'yyyy-MM-dd')[0];
                        endDate = now.preWeek(1, 'yyyy-MM-dd')[1];
                        tmpNum = now.prevDay(7).getWeekNumber();
                        specDate = ""+now.prevDay(7).getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.CURWEEK:    	startDate = now.curWeek(cfg[_KEY_.MAX_DATE])[0];
                        endDate = now.curWeek(cfg[_KEY_.MAX_DATE])[1];
                        tmpNum = now.getWeekNumber();
                        specDate = ""+now.getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH:  	startDate = now.preMonth(1, 'yyyy-MM-dd')[0];
                        endDate = now.preMonth(1, 'yyyy-MM-dd')[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.CURMONTH:    startDate = now.curMonth(cfg[_KEY_.MAX_DATE])[0];
                        endDate = now.curMonth(cfg[_KEY_.MAX_DATE])[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV1:  specDate = startDate = endDate = now.prevDay().Format("yyyy-MM-dd");
                        cache.set(_KEY_.CACHE_SUPER_RAPID,new Date(endDate));
                        break;
                    case _KEY_.PREV2:  specDate = startDate = endDate = now.prevDay(2).Format("yyyy-MM-dd");
                        cache.set(_KEY_.CACHE_SUPER_RAPID,new Date(endDate));
                        break;
                    case _KEY_.PREV3:   	startDate = now.prevDay3()[0];
                        endDate = now.prevDay3()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV7:    	startDate = now.prevDay7()[0];
                        endDate = now.prevDay(cfg[_KEY_.MAX_DATE]).Format("yyyy-MM-dd");
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV15:    	startDate = now.prevDay15()[0];
                        endDate = now.prevDay15()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV30:    	startDate = now.prevDay30()[0];
                        endDate = now.prevDay30()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV90:    	startDate = now.prevDay90()[0];
                        endDate = now.prevDay90()[1];
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVSEASON:
                        startDate = now.prevSeason()[0];
                        endDate = now.prevSeason()[1];
                        var counter = now.prevSeasonCounter();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_SEASON + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        break;
                    case _KEY_.PREVMONTH3:
                        startDate = now.prevMonth3()[0];
                        endDate = now.prevMonth3()[1];
                        var counter = now.prevMonthCounter3();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH6:
                        startDate = now.prevMonth6()[0];
                        endDate = now.prevMonth6()[1];
                        var counter = now.prevMonthCounter6();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVMONTH12:
                        startDate = now.prevMonth12()[0];
                        endDate = now.prevMonth12()[1];
                        var counter = now.prevMonthCounter12();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVHALFYEAR:
                        startDate = now.prevMonth6()[0];
                        endDate = now.prevMonth6()[1];
                        var counter = now.prevMonthCounter6();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREVYEAR:
                        startDate = now.prevMonth12()[0];
                        endDate = now.prevMonth12()[1];
                        var counter = now.prevMonthCounter12();
                        specDate = counter.split(".")[0] + _KEY_.PREFIX_MONTH + (counter.split(".")[1]<10?("0"+counter.split(".")[1]):counter.split(".")[1]);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,[new Date(startDate), new Date(endDate)]);
                        break;
                    case _KEY_.PREV_WEEK:
                        date = $.jIsArray(date)?date[0]:date;
                        startDate = date.preWeek(1, 'yyyy-MM-dd')[0];
                        endDate = date.preWeek(1, 'yyyy-MM-dd')[1];
                        tmpNum = date.prevDay(7).getWeekNumber();
                        specDate = ""+date.prevDay(7).getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                        }
                        date = date.preWeek();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.NEXT_WEEK:
                        date = $.jIsArray(date)?date[1]:date;
                        startDate = date.preWeek(-1, 'yyyy-MM-dd')[0];
                        endDate = date.preWeek(-1, 'yyyy-MM-dd')[1];
                        tmpNum = new Date(startDate).getWeekNumber();
                        specDate = ""+date.getFullYear()+_KEY_.PREFIX_WEEK+(tmpNum<10?("0"+tmpNum):tmpNum);
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        }
                        date = date.preWeek(-1);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.PREV_DAY:
                        date = $.jIsArray(date)?date[0]:date;
                        specDate = startDate = endDate = date.prevDay().Format("yyyy-MM-dd");
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                        }
                        date = date.prevDay();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.NEXT_DAY:
                        date = $.jIsArray(date)?date[1]:date;
                        specDate = startDate = endDate = date.nextDay().Format("yyyy-MM-dd");
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        }
                        date = date.nextDay();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.PREV_MONTH:
                        date = $.jIsArray(date)?date[0]:date;
                        startDate = date.preMonth(1, 'yyyy-MM-dd')[0];
                        endDate = date.preMonth(1, 'yyyy-MM-dd')[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").removeClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) < parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").removeClass('disabled');
                        }
                        date = date.preMonth();
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.NEXT_MONTH:
                        date = $.jIsArray(date)?date[1]:date;
                        startDate = date.preMonth(-1, 'yyyy-MM-dd')[0];
                        endDate = date.preMonth(-1, 'yyyy-MM-dd')[1];
                        specDate = startDate.slice(0, startDate.length-3);
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(yesterdayStr)){
                            $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curWeek(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        }
                        if(parseInt(endDate.replace(/-/g, "")) >= parseInt(now.curMonth(0)[0].replace(/-/g, "")) ){
                            $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        }
                        date = date.preMonth(-1);
                        cache.set(_KEY_.CACHE_SUPER_RAPID,date);
                        break;
                    case _KEY_.RESET_DATE:
                        cache.set(_KEY_.CACHE_SUPER_RAPID, now.prevDay(cfg[_KEY_.MAX_DATE]));
                        /* 禁用后一天/下一周/下一月快查 START */
                        $_("a[desc='"+_KEY_.NEXT_DAY+"']").addClass('disabled');
                        $_("a[desc='"+_KEY_.NEXT_WEEK+"']").addClass('disabled');
                        $_("a[desc='"+_KEY_.NEXT_MONTH+"']").addClass('disabled');
                        /* 禁用后一天/下一周/下一月快查 END */
                        new DatePicker(_containerId, cfg);
                        return;
                        break;
                    default:	nonDate = true;
                        break;
                }

                if(!nonDate){
                    /* 隐藏域值逻辑 START */
                    if(bSpec){      //判断是否特殊值（生成固定值由推送数据计算）
                        $_('#'+dateSeg[0]).val(specDate);
                        $_('#'+dateSeg[1]).val(specDate);       //2014-01、2014-9901等特殊值
                    }else if(bBoth){
                        $_('#'+dateSeg[0]).val(startDate);       //2014-01-01 ~ 2014-01-31
                        $_('#'+dateSeg[1]).val(endDate);
                    }else{
                        $_('#'+dateSeg[0]).val(_val);       //0、1、2等值
                        $_('#'+dateSeg[1]).val(_val);
                    }
                    if(ifFinalConfirm){
                        cache.set(_KEY_.CACHE_LAST_DATESEG+calendarIds[0], [$_('#'+dateSeg[0]).val()]);
                        cache.set(_KEY_.CACHE_LAST_DATESEG+calendarIds[1], [$_('#'+dateSeg[1]).val()]);
                        cache.set(_KEY_.CACHE_FINAL_DATESEG+calendarIds[0], [$_('#'+dateSeg[0]).val()]);
                        cache.set(_KEY_.CACHE_FINAL_DATESEG+calendarIds[1], [$_('#'+dateSeg[1]).val()]);
                    }
                    /* 隐藏域值逻辑 END */
                    /* 显示内容逻辑（只有同时日、月、周）三种视图 START */
                    if(view==_KEY_.VODAY){
                        $pickers[0].DatePickerSetDate(startDate);
                        $pickers[1].DatePickerSetDate(endDate);
                        widthSet(1, _pickerIds[0]);
                        widthSet(1, _pickerIds[1]);
                        $_('.widgetField input[readonly=readonly]',$datepickers[0]).val(startDate);
                        $_('.widgetField input[readonly=readonly]',$datepickers[1]).val(endDate);
                        if(ifFinalConfirm){
                            cache.set(_KEY_.CACHE_WIDTH_CODE+calendarIds[0], 1);
                            cache.set(_KEY_.CACHE_LAST_DATE+calendarIds[0], [startDate]);
                            cache.set(_KEY_.CACHE_FINAL_DATE+calendarIds[0], [startDate]);
                            cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+calendarIds[0], startDate);
                            cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+calendarIds[0], startDate);
                            cache.set(_KEY_.CACHE_WIDTH_CODE+calendarIds[1], 1);
                            cache.set(_KEY_.CACHE_LAST_DATE+calendarIds[1], [endDate]);
                            cache.set(_KEY_.CACHE_FINAL_DATE+calendarIds[1], [endDate]);
                            cache.set(_KEY_.CACHE_LAST_FIELD_SHOW+calendarIds[1], endDate);
                            cache.set(_KEY_.CACHE_FINAL_FIELD_SHOW+calendarIds[1], endDate);
                        }
                    }else if(view==_KEY_.VWEEK){
                        //TODO
                    }else if(view==_KEY_.VMONTH){
                        //TODO
                    }
                }
                dateIpt = "";
                cache.set(_KEY_.CACHE_DIMENSION+_containerId,[_ref]);
            };
            /* 快查接收页面跳转参数 START */
            if(paramRapid){
                widthSet(1, _pickerIds[0]);
                widthSet(1, _pickerIds[1]);
                $a.filter('.radiobox').each(function(){
                    if(paramRapid==$_(this).attr("desc")){
                        if(cfg[_KEY_.RAPID_SEL]&&(cfg[_KEY_.RAPID_SEL].ifExternal||cfg[_KEY_.RAPID_SEL].noPicker)){
                            $_(this).siblings().removeClass("r-checked");
                            $_(this).addClass("r-checked");
                        }else{
                            $_(this).addClass("chosen");
                        }
                        rapidSet($_(this).attr("desc"),$_(this).attr("val"),$_(this).attr("ref"));
                    }
                });

                $.extend(cfg[_KEY_.PARAM], {rapid: null});        //清除快查传参
            }
            /* 快查接收页面跳转参数 END */
            /* 连续快查、普通快查事件初始化 START */
            $a.off("click").on('click', function(_e){
                if($_(this).is('.r-disabled')||$_(this).is('.disabled')){
                    _e.preventDefault();
                    _e.stopPropagation();
                    return;
                }else{
                    /* 普通快查单选按钮渲染 START */
                    if($_(this).is('.radiobox')){        //筛除连续快查部分
                        if(cfg[_KEY_.RAPID_SEL]&&(cfg[_KEY_.RAPID_SEL].ifExternal||cfg[_KEY_.RAPID_SEL].noPicker)){
                            $_(this).siblings().removeClass("r-checked");
                            $_(this).addClass("r-checked");
                        }else{
                            $_(".widgetCalendar .quick-chose",$container).find("a.chosen").removeClass("chosen");
                            $_(this).addClass("chosen");
                        }
                    }else{      //去除普通快查部分的选中状态
                        $_(this).siblings('.r-checked').removeClass("r-checked");
                    }
                    /* 普通快查单选按钮渲染 END */
                    var desc = $_(this).attr("desc"),val = $_(this).attr("val"),ref = $_(this).attr("ref");
                    cache.set(_KEY_.CACHE_RAPID+_containerId, [val, desc]);
                    rapidSet(desc,val,ref);

                    /* 快查直接请求数据 START */
                    if($_(".btn.query").length){
                        $_(".btn.query").eq(0).click();
                    }else{
                        if(_cfg[_KEY_.CUSTOM_QUERY]){
                            _cfg[_KEY_.CUSTOM_QUERY]();
                        }
                    }
                    /* 快查直接请求数据 END */
                }
            });
            /* 普通快查事件初始化 END */
        },
        renderDateRapid = function(_containerId, _options){
            var $_ = $,
                ref = "!month!week",
                items,
                today = new Date(),
                disabled = false,
                counter = 0,
                maxDate = _options[_KEY_.MAX_DATE],
                buf = [],
                ifRapidInitial = false;
            buf.push('<span class="rapidSel ml15"><input type="hidden" id="'+_containerId+'CalendarRapidSel" /><label class="filterLbl">'+(_options[_KEY_.RAPID_SEL].label||"快速查看")+'</label>');
            for(var rapidKey in _options[_KEY_.RAPID_SEL]){     //连续快查和普通快查的位置遵从配置的顺序
                if(rapidKey=='superItem'){     //连续快查
                    items = _options[_KEY_.RAPID_SEL][rapidKey];
                    for(var desc in items){
                        if(desc==_KEY_.PREV_DAY|| desc==_KEY_.NEXT_DAY || desc==_KEY_.RESET_DATE){
                            ref = _KEY_.VODAY;
                        }else if(desc==_KEY_.PREV_WEEK || desc==_KEY_.NEXT_WEEK){
                            ref = _KEY_.VWEEK;
                        }else{
                            ref = _KEY_.VMONTH;
                        }
                        if(desc==_KEY_.NEXT_DAY || desc==_KEY_.NEXT_WEEK || desc==_KEY_.NEXT_MONTH){
                            buf.push('<a class="super-rapid disabled" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'">'+desc+'</a>');
                        }else{
                            buf.push('<a class="super-rapid" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'">'+desc+'</a>');
                        }
                    }
                }else if(rapidKey=='item'){     //普通快查
                    items = _options[_KEY_.RAPID_SEL][rapidKey];
                    for(var desc in items){
                        if(desc.indexOf(_KEY_.DIMENSION_DATE[_KEY_.VMONTH])>=0){
                            ref = _KEY_.VMONTH;
                        }else if(desc.indexOf(_KEY_.DIMENSION_DATE[_KEY_.VWEEK])>=0){
                            ref = _KEY_.VWEEK;
                        }else{
                            ref = _KEY_.VODAY;
                        }
                        if(desc==_KEY_.CURWEEK){
                            disabled = today.getDay()&&today.getDay()<=maxDate;
                            buf.push('<span class="radiobox'+(disabled?' r-disabled':'')+ '" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'"><label class="ml3 mr3 p-ib'+(disabled?'':' pointer')+'">'+desc+'</label></span>');
                        }else if(desc==_KEY_.CURMONTH){
                            disabled = today.getDate()<=maxDate;
                            buf.push('<span class="radiobox'+(disabled?' r-disabled':'')+ '" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'"><label class="ml3 mr3 p-ib'+(disabled?'':' pointer')+'">'+desc+'</label></span>');
                        }else if(desc==_KEY_.PREV1){
                            disabled = 2<=maxDate;
                            buf.push('<span class="radiobox'+(disabled?' r-disabled':'')+ '" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'"><label class="ml3 mr3 p-ib'+(disabled?'':' pointer')+'">'+desc+'</label></span>');
                        }else if(desc==_KEY_.PREV2){
                            disabled = 3<=maxDate;
                            buf.push('<span class="radiobox'+(disabled?' r-disabled':'')+ '" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'"><label class="ml3 mr3 p-ib'+(disabled?'':' pointer')+'">'+desc+'</label></span>');
                        }else if($_.jIsArray(items[desc])){
                            if(items[desc][1]){
                                ifRapidInitial = true;
                            }
                            buf.push('<span class="radiobox '+(items[desc][1]?" r-checked":"")+'" desc="'+desc+'" ref="'+ref+'" val="'+items[desc][0]+'"></span><label class="mr10 ml2 p-ib pointer">'+desc+'</label></span>');
                        }else{
                            buf.push('<span class="radiobox" desc="'+desc+'" ref="'+ref+'" val="'+items[desc]+'"><label class="ml3 mr3 p-ib pointer">'+desc+'</label></span>');
                        }
                    }
                }
            }
            buf.push('</span>');
            return buf.join('');
        },
        renderDatepicker = function(){
            var this_ = this,
                $_ = $,
                containerId = this_.containerId,
                $container = $_('#'+containerId),
                options = this_.options;

            if(options[_KEY_.IF_SEPARATE]&&options[_KEY_.VIEW]!=_KEY_.VODAY){       //双控件判断设定天视图 天视图能和快查联动
                $_.extend(options, {'rapidSel': null});
            }

            cache.set(_KEY_.CACHE_DIMENSION+containerId,[_KEY_.VODAY]);     // 双控件默认按日视图维度
            var buf = [],
                ifRapidInitial = false,
                ifDimensionEmbed = options[_KEY_.DIMENSION_SEL]&&options[_KEY_.DIMENSION_SEL].ifEmbed,
                ifRapidExternal = options[_KEY_.RAPID_SEL]&&(options[_KEY_.RAPID_SEL].ifExternal||options[_KEY_.RAPID_SEL].noPicker),
                frontPickerId = containerId + '_Front',
                backPickerId = containerId + '_Back';
            // $.ajax({
            //     url : context_path+"/getMenuTime.action",
            //     type : "post",
            //     dataType : "json",
            //     async:false,
            //     data : {"menuCode":_KEY_.menuCfg[id]},
            //     success :function(data){
            //     	//将缓存中的最大时间赋值给maxDate_temp
            //     	var result_tmp = data.result;
            //         if(result_tmp != null && result_tmp != ""){
            //         	defaultMaxDate = parseInt(result_tmp);
            //         }
            //     } ,
            //     error :  function(XMLHttpRequest, s, errorThrown) {

            //     }
            // });

            buf.push("<span class='filterLbl'>"+(options[[_KEY_.LABEL]]||"")+"</span>");
            options[_KEY_.MIN_DATE] = options[_KEY_.MIN_DATE]||'2005-01-01';
            options[_KEY_.MAX_DATE] = options[_KEY_.MAX_DATE]||1;
            if(options[_KEY_.IF_SEPARATE]){
                buf.push('<div id="'+frontPickerId+'" class="picker-front"></div>');
                buf.push('<label class="picker-link">至</label>');
                buf.push('<div id="'+backPickerId+'" class="picker-back"></div>');

                /* 快速查询（外置） START */
                if(ifRapidExternal){
                    buf.push(renderDateRapid(containerId, options));
                }
                /* 快速查询（外置） END */
                $_("#"+containerId).addClass('container-separate').html(buf.join(""));
                var separateOptions = $_.extend({}, options, {
                    type: _KEY_.SINGLE,
                    ifSeparate: false,
                    rapidSel: null,
                    style: _KEY_.SIMPLE     //双控件显示设定简单模式
                });

                var startDatepicker = new DatePicker(containerId+'_Front', $_.extend({}, separateOptions, {ifSeparatePart: true, hiddenDom: options.dateSeg, dateSeg: [options.dateSeg[0]]})),
                    endDatepicker = new DatePicker(containerId+'_Back', $_.extend({}, separateOptions, {ifSeparatePart: true, hiddenDom: options.dateSeg, dateSeg: [options.dateSeg[1]]}));

                if(ifRapidExternal){
                    initSeparateDateRapid(
                        containerId,
                        options,
                        [frontPickerId, backPickerId]
                    );
                }
            }else{
                buf.push('<span id="'+containerId+'Calendar" class="widget"><div class="widgetField">');
                /* 配置隐藏域提交的格式 START */
                if($_.jIsArray(options[_KEY_.DATE_SEG])&&options[_KEY_.DATE_SEG].length==2){
                    if(options[_KEY_.FILTER_NAME]){
                        var filterNames = [];
                        for(var i in options[_KEY_.FILTER_NAME]){
                            filterNames.push(options[_KEY_.FILTER_NAME][i].indexOf(".")>=0?options[_KEY_.FILTER_NAME][i]:("filter."+options[_KEY_.FILTER_NAME][i]));
                        }
                        buf.push('<input id="'+options[_KEY_.DATE_SEG][0]+'" type="hidden" name="'+filterNames[0]+'" /><input id="'+options[_KEY_.DATE_SEG][1]+'" type="hidden" name="'+filterNames[1]+'" />');
                    }else{
                        buf.push('<input id="'+options[_KEY_.DATE_SEG][0]+'" type="hidden" name="filter.startDate" /><input id="'+options[_KEY_.DATE_SEG][1]+'" type="hidden" name="filter.endDate" />');
                    }
                }else{
                    if(options[_KEY_.FILTER_NAME]){
                        buf.push('<input id="'+options[_KEY_.DATE_SEG]+'" type="hidden" name="'+(options[_KEY_.FILTER_NAME].indexOf(".")>=0?options[_KEY_.FILTER_NAME]:("filter."+options[_KEY_.FILTER_NAME][0]))+'" />');
                    }else{
                        buf.push('<input id="'+options[_KEY_.DATE_SEG]+'" type="hidden" name="filter.date" />');
                    }
                }
                /* 配置隐藏域提交的格式 END */
                buf.push('<input id="'+containerId+'DateSeg" type="text" class="cursorPointer" readonly="readonly" /><a href="javascript:;"></a></div><div class="widgetCalendar"><div class="c-box">');
                /* 日期选择说明 START */
                if(options[_KEY_.IF_NOTE]){
                    buf.push('<div class="note">');
                    if(options[_KEY_.TYPE]=="single"){
                        switch(options[_KEY_.VIEW]){
                            case _KEY_.VODAY: buf.push("选择整日进行查询");break;
                            case _KEY_.VWEEK: buf.push("选择整周进行查询");break;
                            case _KEY_.VMONTH: buf.push("选择整月进行查询");break;
                            case _KEY_.VDAY: buf.push("选择整日、整周或整月进行查询");break;
                            case _KEY_.VNWEEK: buf.push("选择整日或整月进行查询");break;
                            case _KEY_.VNMONTH: buf.push("选择整日或整周进行查询");break;
                        }
                    }else{
                        switch(options[_KEY_.VIEW]){
                            case _KEY_.VODAY:
                                buf.push("选择时间段进行查询.");
                                if(options[_KEY_.RANGE]){
                                    if($_.jIsArray(options[_KEY_.RANGE])){
                                        buf.push("最长选择"+options[_KEY_.RANGE][1]+"天的数据");
                                    }else{
                                        buf.push("最长选择"+options[_KEY_.RANGE]+"天的数据");
                                    }
                                }
                                break;
                            case _KEY_.VWEEK: buf.push("选择整周进行查询");break;
                            case _KEY_.VMONTH: buf.push("选择整月进行查询");break;
                            case _KEY_.VDAY: buf.push("选择时间段、整周或整月进行查询");;break;
                            case _KEY_.VNWEEK: buf.push("选择时间段或整月进行查询");break;
                            case _KEY_.VNMONTH: buf.push("选择时间段或整周进行查询");break;
                        }
                    }
                    buf.push('</div>');
                }
                /* 日期选择说明 END */
                /* 快速查询（内嵌） START */
                if(options[_KEY_.RAPID_SEL]&&!options[_KEY_.RAPID_SEL].ifExternal&&!options[_KEY_.RAPID_SEL].noPicker){
                    buf.push('<div class="quick-chose"><label>'+(options[_KEY_.RAPID_SEL].label||"快速查看")+'</label>');
                    var today = new Date(),maxDate = options[_KEY_.MAX_DATE]||1;
                    for(var desc in options[_KEY_.RAPID_SEL].item){
                        if(desc==_KEY_.CURWEEK){
                            if(!!today.getDay()&&today.getDay()<=maxDate){
                                continue;
                            }
                        }else if(desc==_KEY_.CURMONTH){
                            if(today.getDate()<=maxDate){
                                continue;
                            }
                        }
                        buf.push('<a href="javascript:void(0)" desc="'+desc+'" val="'+options[_KEY_.RAPID_SEL].item[desc]+'" name="'+containerId+'RapidSel">'+desc+'</a>');
                    }
                    buf.push('</div>');
                }
                /* 快速查询（内嵌） END */
                /* 维度选择（内嵌） START    去掉选择面板左侧选择列表*/
                if(ifDimensionEmbed){
                    if(options[_KEY_.IF_LEFT_VISIBLE]===false){
                        buf.push('<div id="'+containerId+'Dimension" class="embedDimension" style="display:none"><ul>');
                    }else{
                        buf.push('<div id="'+containerId+'Dimension" class="embedDimension"><ul>');
                    }
                    if(options[_KEY_.VIEW] in {'day': '', '!week': '', '!month': ''}){
                        for(var i in options[_KEY_.DIMENSION_SEL].item){
                            buf.push('<li val="'+options[_KEY_.DIMENSION_SEL].item[i]+'">按'+_KEY_.DIMENSION_DATE[options[_KEY_.DIMENSION_SEL].item[i]]+'查看</li>');
                        }
                    }else{
                        buf.push('<li val="'+options[_KEY_.VIEW]+'">按'+_KEY_.DIMENSION_DATE[options[_KEY_.VIEW]]+'查看</li>');
                    }
                    buf.push('</ul></div>');
                }
                /* 维度选择（内嵌） END */
                buf.push('<div class="Picker"><div class="Calendar"></div>');

                // 开启时间选择模式 基于单选模式
                var ifTimeModeEffective = options[_KEY_.TYPE]==='single' && !!options[_KEY_.TIME_MODE],
                    timeInitValue = (options[_KEY_.TIME_MODE]||"").replace(/[hms]/g, "0");
                if (ifTimeModeEffective) {
                    buf.push('<div class="time-picker"><label>选择时间：</label><input type="text" value="'+timeInitValue.split(":")[0]+'" /><span>:</span><input type="text" value="'+timeInitValue.split(":")[1]+'" /></div>');
                    cache.set(_KEY_.CACHE_TIME+containerId, timeInitValue);
                }

                if(!options[_KEY_.NO_BTN]){
                    buf.push('<div class="c-btn"><span class="message warn"></span><a class="btn query" href="javascript:void(0);" id="query_button">确定</a><a class="btn cancel ml5 mr10" href="javascript:void(0);"  id="cancel_button">取消</a></div>');
                }
                buf.push('</div></div></div></span>');
                /* 快速查询（外置） START */
                if(ifRapidExternal){
                    buf.push(renderDateRapid(containerId, options));
                }
                /* 快速查询（外置） END */

                $_("#"+containerId).addClass('container-datepicker').html(buf.join(""));

                // 判断时间模式是否有效
                if (ifTimeModeEffective) {
                    cache.set(_KEY_.TIME_VALUE+containerId,['00','00']);
                        hmsRegex = new RegExp((options[_KEY_.TIME_MODE]||"").replace(/[hms]/g, '\\d'), 'gi');
                    $_('.time-picker', $container).undelegate('input', 'keydown').delegate('input', 'keydown', function(_e) {
                        if(_e.stopPropagation){
                            _e.stopPropagation();
                        }else{
                            _e.cancelBubble = true;
                        }
                        if((65<=_e.which&&_e.which<=90)||_e.which==38||_e.which==40){
                            _e.preventDefault();
                            return;
                        }
                    }).undelegate('input', 'keyup').delegate('input', 'keyup', function(_e){
                        if(_e.stopPropagation){
                            _e.stopPropagation();
                        }else{
                            _e.cancelBubble = true;
                        }
                        var $thisInput = $_(this),
                            thisValue = $thisInput.val(),
                            time,time_val=parseInt(thisValue),
                            index=$_(this).parent().children("input").index(this);
                        switch (index){
                            case 0:time=23;break;
                            case 1:time=59;break;
                            default:break;
                        }
                        time_val=time_val>time?time:thisValue;
                        switch(_e.which){
                            //case 8:     // backspace
                            //case 46:    // delete
                            //    if(!time_val){
                            //        time_val=cache.get(_KEY_.TIME_VALUE+containerId)[index];
                            //        $('.time-picker input', $container).eq(index).change();
                            //    }
                            //    break;
                            case 38:    //up
                                if(time_val<time){
                                    time_val++;
                                    time_val=time_val< 10 ? ("0" + time_val) : time_val + "";
                                }else if(time_val==time){
                                    time_val='00'
                                }
                                break;
                            case 40:    // down
                                if(time_val<time){
                                    time_val--;
                                    time_val=time_val< 10 ? ("0" + time_val) : time_val + "";
                                }else if(time_val==time){
                                    time_val=time;
                                }
                                break;
                            default:
                                break;
                        }
                        $thisInput.val(time_val);
                        cache.set(_KEY_.CACHE_TIME+containerId,$('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val());
                     })
                    $('.time-picker input', $container).change(function() {
                        var index=$(this).parent().children("input").index(this),time_val,
                            parent_id=$container.attr("id");
                        if (ifTimeModeEffective) {
                            if(parseInt($(this).val())<10){
                                $(this).val('0'+parseInt($(this).val()));
                            }else if(parseInt($(this).val())>=10){
                                $(this).val(parseInt($(this).val()));
                            }
                            if(!$(this).val()){
                                time_val=cache.get(_KEY_.TIME_VALUE+parent_id)[index];
                                $(this).val(time_val);
                                cache.set(_KEY_.CACHE_TIME+containerId,cache.get(_KEY_.TIME_VALUE+parent_id).join(":"));
                            }
                            cache.set(_KEY_.CACHE_TIME+containerId,$('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val());
                            $('.widgetField input[type!=hidden]',$container).val($('.widgetField input[type!=hidden]',$container).val().replace(hmsRegex, $('.time-picker input', $container).eq(0).val()+":"+$('.time-picker input', $container).eq(1).val()));
                        }
                    });
                }

                if(options[_KEY_.RAPID_SEL]&&options[_KEY_.RAPID_SEL].noPicker){       //判断是否只有快查没有选择器
                    $_(".widget",$_("#"+containerId)).hide();
                }
                /* 维度查询（外置）事件初始化 START */
                if(options[_KEY_.DIMENSION_SEL]){        //判断日期是否有按维度查询
                    var ifParam = true, dimension = (options[_KEY_.PARAM]||{})[_KEY_.DIMENSION_SEL];
                    $_("ul li",$_("#"+containerId+"Dimension")).off('click').on('click', function(){
                        cache.set(_KEY_.CACHE_DIMENSION+containerId, [$_(this).attr('val'), $_(this).text()]);
                        cache.remove(_KEY_.CACHE_RAPID+containerId);
                        $_(this).addClass("sel").css({"border-right": "0 none"}).siblings().removeClass("sel");
                        $(".datepicker",$("#"+containerId+"Calendar")).remove();
                        $(".radiobox.r-checked",$("#"+containerId+"Calendar").next(".rapidSel")).removeClass("r-checked");
                        initDatePicker(containerId, $_.extend(options, {id: containerId+"Calendar",view: $_(this).attr("val"),param: (ifParam&&options[_KEY_.PARAM])||{}}));
                        ifParam = false;
                    });
                    if(dimension){
                        $_('ul li[val="'+dimension+'"]',$_("#"+containerId+"Dimension")).click();
                    }else{
                        $_("ul li:first",$_("#"+containerId+"Dimension")).click();
                    }
                }else{
                    initDatePicker(containerId, $_.extend(options, {id: containerId+"Calendar",param: options[_KEY_.PARAM]||{}}));
                }
                /* 维度查询（外置）事件初始化 END  隐藏掉左侧选择 同时需要注释掉面板计算宽度 */

                if(options[_KEY_.DIMENSION_SEL]&&(options[_KEY_.IF_LEFT_VISIBLE]!==false)){
                    $('.embedDimension',$('#'+containerId)).height($('.Picker',$('#'+containerId)).outerHeight(true)-Number($('.embedDimension',$('#'+containerId)).css("margin-top").replace('px', ''))-Number($('.embedDimension',$('#'+containerId)).css("border-top-width").replace('px', '')));
                }
            }

            /* 初始化快查选中状态 START */
            if(options[_KEY_.RAPID_SEL]&&(options[_KEY_.RAPID_SEL].ifExternal||options[_KEY_.RAPID_SEL].noPicker)){
                if(ifRapidInitial){
                    $_(".rapidSel span.radiobox.r-checked",$_("#"+containerId)).click();
                }
            }
            /* 初始化快查选中状态 END */
        };
    $.fn.extend({
        DatePicker: core.init,
        DatePickerHide: core.hidePicker,
        DatePickerShow: core.showPicker,
        DatePickerSetDate: core.setDate,
        DatePickerGetDate: core.getDate,
        DatePickerClear: core.clear,
        DatePickerLayout: core.fixLayout
    });
    /**
     * 获取当前日期显示
     * @param _ifShort 是否简写
     */
    var getDateShow = function(_ifShort){
            var $_ = $,containerId = this.containerId, options = this.options, ifSeparate = options[_KEY_.IF_SEPARATE], ifSimple = options[_KEY_.STYLE] === _KEY_.SIMPLE, returnVal;
            if(ifSeparate){
                var buf = [];
                $_('#'+containerId+' input[readonly=readonly]').each(function(_i, _item){
                    buf.push($(_item).val());
                });
                returnVal = $_.unique(buf).sort().join('至');
            }else{
                returnVal = $_('#'+containerId+' input[readonly=readonly]').val();
                if(!ifSimple&&_ifShort){
                    if(returnVal.indexOf(' ')>0){       //排除2015-01-01单日期形式
                        returnVal = returnVal.substr(0, returnVal.indexOf(' '));
                    }
                }
            }
            return returnVal;
        },
        /**
         * 获取当前日期
         * @returns {Array}
         */
        getDate = function(){
            var $_ = $,options = this.options, dateDoms = options[_KEY_.DATE_SEG],dateId=options.id, returnVal = [];
            for(var i in dateDoms){
                returnVal.push($_('#'+dateId).find('#'+dateDoms[i]).val());
            }
            return returnVal;
        },
        /**
         * 获取时间
         * @returns {*}
         */
        getTime = function(){
            var $_ = $,containerId = this.containerId;
            return cache.get(_KEY_.CACHE_TIME+containerId);
        },
        /**
         * 获取当前时间的详细日期集
         */
        getDays = function(){
            var $_ = $,options = this.options,now = new Date(), maxDate = options[_KEY_.MAX_DATE], ifSeparate = options[_KEY_.IF_SEPARATE], view = options[_KEY_.VIEW], ifRange = options[_KEY_.TYPE]===options[_KEY_.RANGE], returnVal = [];
            if(ifSeparate){     //双控件
                var startDateString = this.getDate()[0],
                    endDateString = this.getDate()[1],
                    startDate = new Date(startDateString),
                    endDate = new Date(endDateString),
                    dayNum = (endDate-startDate)/864e5 + 1;
                if(startDateString==endDateString){     //月（2014-01）、周（2014-9901）
                    if(/^\d{4}-\d{2}$/.test(startDateString)){       //匹配2015-01（月）
                        returnVal = new Date(startDateString+'-01').daysOfCurMonth(maxDate, false);
                    }else if(/^\d{4}-?99\d{2}$/.test(startDateString)){       //匹配2015-9901（周）
                        var yy = parseInt(startDateString.substr(0, 4)),
                            ww = parseInt(startDateString.substr(startDateString.length-2, startDateString.length));
                        returnVal = $_.jDateByWeek(yy, ww).daysOfCurWeek(maxDate, false);
                    }else if(/^\d{4}-\d{2}-\d{2}$/.test(startDateString)){      //匹配2015-01-01（单日）
                        returnVal.push(new Date(startDateString));
                    }else if(/^\d+$/.test(startDateString)){        //匹配7、30
                        startDate = now.prevDay(parseInt(startDateString)),endDate = now.prevDay(1);
                        dayNum = (endDate-startDate)/864e5 + 1;
                        for(var i=0;i<dayNum;i++){
                            returnVal.push(new Date(startDate.getTime()+i*864e5));
                        }
                    }
                }else{
                    dayNum = (endDate-startDate)/864e5 + 1;
                    for(var i=0;i<dayNum;i++){
                        returnVal.push(new Date(startDate.getTime()+i*864e5));
                    }
                }
            }else{
                if(!ifRange){        //单选
                    var dateString = this.getDate()[0];
                    if(/^\d{4}-\d{2}$/.test(dateString)){       //匹配2015-01（月）
                        returnVal = new Date(dateString+'-01').daysOfCurMonth(maxDate, false);
                    }else if(/^\d{4}-?99\d{2}$/.test(dateString)){       //匹配2015-9901（周）
                        var yy = parseInt(dateString.substr(0, 4)),
                            ww = parseInt(dateString.substr(dateString.length-2, dateString.length));
                        returnVal = $_.jDateByWeek(yy, ww).daysOfCurWeek(maxDate, false);
                    }else if(/^\d{4}-\d{2}-\d{2}$/.test(dateString)){     //匹配2015-01-01（单日）
                        returnVal.push(new Date(dateString));
                    }else if(/^\d+$/.test(dateString)){        //匹配7、30
                        startDate = now.prevDay(parseInt(dateString)),endDate = now.prevDay(1);
                        dayNum = (endDate-startDate)/864e5 + 1;
                        for(var i=0;i<dayNum;i++){
                            returnVal.push(new Date(startDate.getTime()+i*864e5));
                        }
                    }
                }else{      //范围选
                    startDate = new Date(this.getDate()[0]),
                        endDate = new Date(this.getDate()[1]),
                        dayNum = (endDate-startDate)/864e5 + 1;
                    for(var i=0;i<dayNum;i++){
                        returnVal.push(new Date(startDate.getTime()+i*864e5));
                    }
                }
            }
            return returnVal;
        },
        /**
         * 获取快查信息
         * @returns {Array}
         */
        getRapid = function(){
            return cache.get(_KEY_.CACHE_RAPID+this.containerId);
        },
        /**
         * 获取视图维度信息
         * @returns
         */
        getDimension= function(){
            return cache.get(_KEY_.CACHE_DIMENSION+this.containerId)[0];
        },
        /**
         * 设置当前日期
         */
        setDate = function(_dateRange){
            var this_ = this, $_ = $,containerId = this_.containerId, $container = $_('#'+containerId), options = this_.options, dateDoms = options[_KEY_.DATE_SEG], ifSeparate = options[_KEY_.IF_SEPARATE];
            if(_dateRange){
                if(ifSeparate){
                    if(_dateRange.length==1){
                        _dateRange = [_dateRange[0], _dateRange[0]];
                    }
                    initDatePicker(containerId, $_.extend({}, options, {ifSeparatePart: true, hiddenDom: options.dateSeg, id: [$_('.container-datepicker', $container)[0].id+"Calendar"], rapidSel: null, ifSeparate: false, dateSeg: [dateDoms[0]], view: _KEY_.VODAY, param: {date: [_dateRange[0]]}}));
                    initDatePicker(containerId, $_.extend({}, options, {ifSeparatePart: true, hiddenDom: options.dateSeg, id: [$_('.container-datepicker', $container)[1].id+"Calendar"], rapidSel: null, ifSeparate: false, dateSeg: [dateDoms[1]], view: _KEY_.VODAY, param: {date: [_dateRange[1]]}}));
                }else{
                    initDatePicker(containerId, $_.extend({}, options, {id: [containerId+"Calendar"], view: _KEY_.VODAY, param: {date: _dateRange}}));
                }
            }
        },
        /**
         * 设置周值
         */
        setWeek = function(_weekRange){
            var this_ = this, $_ = $,containerId = this_.containerId,options = this_.options, dateDoms = options[_KEY_.DATE_SEG], ifSeparate = options[_KEY_.IF_SEPARATE];
            if(_weekRange){
                /* 周转换2014-01格式化成2014-9901 */
                var w;
                for(var i in _weekRange){
                    w = _weekRange[i];
                    if(/^\d{4}-\d{2}$/.test(w)){
                        _weekRange.splice(i, 1, w.slice(0, w.indexOf('-')+1)+'99'+ w.slice(w.indexOf('-')+1));
                    }
                }
                if(ifSeparate){
                    initDatePicker(containerId, $_.extend(options, {id: [dateDoms[0]+"Calendar",dateDoms[1]+"Calendar"], view: _KEY_.VWEEK, param: {date: _weekRange}}));
                }else{
                    initDatePicker(containerId, $_.extend(options, {id: [containerId+"Calendar"], view: _KEY_.VWEEK, param: {date: _weekRange}}));
                }
            }
        },
        /**
         * 设置月值
         */
        setMonth = function(_monthRange){
            var this_ = this, $_ = $,containerId = this_.containerId,options = this_.options, dateDoms = options[_KEY_.DATE_SEG], ifSeparate = options[_KEY_.IF_SEPARATE];
            if(_monthRange){
                if(ifSeparate){
                    initDatePicker(containerId, $_.extend(options, {id: [dateDoms[0]+"Calendar",dateDoms[1]+"Calendar"], view: _KEY_.VMONTH, param: {date: _monthRange}}));
                }else{
                    initDatePicker(containerId, $_.extend(options, {id: [containerId+"Calendar"], view: _KEY_.VMONTH, param: {date: _monthRange}}));
                }
            }
        },
        /**
         * 设置当前快查
         * @returns {Array}
         */
        setRapid = function(_rapidVal){
            var this_ = this, $_ = $,containerId = this_.containerId,frontPickerId = containerId + '_Front',backPickerId = containerId + '_Back',options = this_.options, dateDoms = options[_KEY_.DATE_SEG], ifSeparate = options[_KEY_.IF_SEPARATE];
            if(_rapidVal){
                if(ifSeparate){
                    $_.extend(options, {
                        param: {
                            rapid: _rapidVal
                        }
                    });
                    initSeparateDateRapid(
                        containerId,
                        options,
                        [frontPickerId, backPickerId]
                    );
                }else{
                    initDatePicker(containerId, $_.extend(options, {id: [containerId+"Calendar"], param: {rapid: _rapidVal}}));
                }
            }
        },
        /**
         * 设置时间
         * @returns {*}
         */
        setTime = function(_timeVal){
            var $_ = $,containerId = this.containerId, $container = $_('#'+containerId);
            cache.set(_KEY_.TIME_VALUE+containerId, [_timeVal.split(":")[0],_timeVal.split(":")[1]]);
            $_('.time-picker input', $container).eq(0).val(_timeVal.split(":")[0]);
            $_('.time-picker input', $container).eq(1).val(_timeVal.split(":")[1]);
            $_('.time-picker input', $container).eq(0).change();
            $_('.time-picker input', $container).eq(1).change();
            cache.set(_KEY_.CACHE_TIME+containerId, _timeVal);
        }
    return {
        renderDatepicker: renderDatepicker,
        getRapidKey: function(){return _KEY_RAPID_;},
        getDimension: getDimension,
        getDateShow: getDateShow,
        getTime: getTime,
        getDate: getDate,
        getDays: getDays,
        setDate: setDate,
        setTime: setTime,
        setWeek: setWeek,
        setMonth: setMonth,
        getRapid: getRapid,
        setRapid: setRapid
    }
})();

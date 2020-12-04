/**
 * 各类原型扩展
 * @author daipeng
 */
var hasPrototype = true;
/**
 *	String原型扩展
 */

String.prototype.trim = function(){
    return this.replace(/(^\s+)|(\s+$)|(^　+)|(　+$)/g, "");
};

String.prototype.ltrim = function(){
    return this.replace(/(^\s+)|(^　+)/g,"");
};

String.prototype.rtrim = function(){
    return this.replace(/(\s+$)|(　+$)/g,"");
};

String.prototype.isNumber = function() {
    return (this.isInt() || this.isFloat() || this.isPercent());
};

String.prototype.isFloat = function() {
    return /^(?:-?|\+?)\d+(,\d\d\d)*\.\d+$/g.test(this);
};

String.prototype.isInt = function() {
    return /^(?:-?|\+?)\d+(,\d\d\d)*$/g.test(this);
};

String.prototype.isPercent = function() {
    return /^(?:-?|\+?)\d+(,\d\d\d)*(\.\d+)?\%$/g.test(this);
};

String.prototype.isPlus = function() {
    return this.isPlusInt() || this.isPlusFloat();
};

String.prototype.isPlusFloat = function() {
    return /^\+?\d*\.\d+$/g.test(this);
};

String.prototype.isPlusInt = function() {
    return /^\+?\d+$/.test(this);
};

String.prototype.isMinus = function() {
    return this.isMinusInt() || this.isMinusFloat();
};

String.prototype.isMinusFloat = function() {
    return /^-\d*\.\d+$/g.test(this);
};

String.prototype.isMinusInt = function() {
    return /^-\d+$/g.test(this);
};

String.prototype.isLeastCharSet = function() {
    return !(/[^A-Za-z0-9_]/g.test(this));
};

String.prototype.isEmail = function() {
    return /^\w+@.+\.\w+$/g.test(this);
};

String.prototype.isZip = function() {
    return /^\d{6}$/g.test(this);
};

String.prototype.isPhone = function() {
    return /^(\d{2,4}-)?((\(\d{3,5}\))|(\d{3,5}-))?\d{3,18}(-\d+)?$/g.test(this);
};

String.prototype.isMobile = function() {
    return /^((13)|(15))\d{9}$/g.test(this);
};

String.prototype.isTel = function() {
    return this.isMobileTelephone() || this.isFixedTelephone();
};

String.prototype.hasMark = function(_mark) {
    var reg = new RegExp("("+_mark+")","g");
    return ((this.match(reg))?(this.match(reg)).length:0)>0;
};

String.prototype.getLen = function() {
    var cArr = this.match(/[^\x00-\xff]/ig);
    return this.length + (cArr==null?0:cArr.length);
};
String.prototype.encodeHtml = function(){
    var REGX_HTML_ENCODE = /"|&|'|<|>|[\x00-\x20]|[\x7F-\xFF]|[\u0100-\u2700]/g,
    s = this.toString();
    return (typeof s != "string") ? s :
        s.replace(REGX_HTML_ENCODE,
            function($0){
                var c = $0.charCodeAt(0), r = ["&#"];
                c = (c == 0x20) ? 0xA0 : c;
                r.push(c); r.push(";");
                return r.join("");
            });
};
String.prototype.decodeHtml = function(){
    var REGX_HTML_DECODE = /&\w+;|&#(\d+);/g,
    HTML_DECODE = {
        "&lt;" : "<",
        "&gt;" : ">",
        "&amp;" : "&",
        "&nbsp;": " ",
        "&quot;": "\"",
        "&copy;": ""
        // Add more
    },
    s = this.toString();
    return (typeof s != "string") ? s :
        s.replace(REGX_HTML_DECODE,
            function($0, $1){
                var c = HTML_DECODE[$0];
                if(c == undefined){
                    // Maybe is Entity Number
                    if(!isNaN($1)){
                        c = String.fromCharCode(($1 == 160) ? 32:$1);
                    }else{
                        c = $0;
                    }
                }
                return c;
            });
};


/**
 * Date原型扩展
 * Date.prototype.isLeapYear 判断闰年
 * Date.prototype.Format 日期格式化
 * Date.prototype.DateAdd 日期计算
 * Date.prototype.DateDiff 比较日期差
 * Date.prototype.toString 日期转字符串
 * Date.prototype.toArray 日期分割为数组
 * Date.prototype.DatePart 取日期的部分信息
 * Date.prototype.MaxDayOfDate 取日期所在月的最大天数
 * Date.prototype.WeekNumOfYear 判断日期所在年的第几周
 * stringToDate 字符串转日期型
 * nextDay
 * prevDay
 * nextDays
 * prevDays
 * dayOfCurWeek
 * daysOfCurWeek
 * dayOfNextWeek
 * daysOfNextWeek
 * dayOfPrevWeek
 * daysOfPrevWeek
 */
Date.prototype.getWeekNumber = function () {
    var d = new Date(this.getTime());
    return (d<new Date(2012, 11, 31)?d.getWeek():d.getCurWeek()[1]) + 1;
};
Date.prototype.getWeek = function (e) {
    var t = new Date(this.getFullYear(), 0, 1),
        n = parseInt("1065432".charAt(t.getDay()));
    return n = this.getTime() - t.getTime() - n * 24 * 60 * 60 * 1e3, n = Math.ceil(n / 6048e5), e == 1 && t.getDay() != 1 ? n + 1 : n
};
Date.prototype.getCurWeek = function () {
    var n = parseInt("0654321".charAt(this.getDay()));
    n = ((n==0)?n=-1:n);

    var t = new Date(this.getTime()+n * 24 * 60 * 60 * 1e3);
    return [t.getFullYear(),t.getWeek()];
};
/**
 * 获取当前日期下_i天
 * @param {Object} _i
 */
Date.prototype.nextDay = function(_i){
    _i = _i||1;
    var day = new Date(this.getTime());
    day.setDate(day.getDate()+_i);
    return day;
};

/**
 * 获取当前日期以后的n天
 */
Date.prototype.nextDays = function(_n){
    var days = new Array();
    for(var i=1;i<=_n;i++) {
        days[i-1] = this.nextDay(i);
    }
    return days;
};

/**
 * 指定日期的前_i天
 * @param {Object} _i
 */
Date.prototype.prevDay = function(_i){
    var day = this;
    var preDay = new Date(this.getTime());
    if(!!_i||_i==0){
        preDay.setDate(day.getDate()-_i);
    }else{
        preDay.setDate(day.getDate()-1);
    }
    return preDay;
};

/**
 * 取得当前日期的前_n天
 */
Date.prototype.prevDays = function(_n){
    var days = new Array();
    for(var i=_n;i>=1;i--) {
        days[_n-i] = this.prevDay(i);
    }
    return days;
};

/**
 * get the date range of previous 3 days
 */
Date.prototype.prevDay3 = function(){
    return [this.prevDay(3).Format("yyyy-MM-dd"),this.prevDay(1).Format("yyyy-MM-dd")];
};

/**
 * get the date range of previous 7 days
 */
Date.prototype.prevDay7 = function(){
    return [this.prevDay(7).Format("yyyy-MM-dd"),this.prevDay(1).Format("yyyy-MM-dd")];
};

/**
 * get the date range of previous 15 days
 */
Date.prototype.prevDay15 = function(){
    return [this.prevDay(15).Format("yyyy-MM-dd"),this.prevDay(1).Format("yyyy-MM-dd")];
};

/**
 * get the date range of previous 30 days
 */
Date.prototype.prevDay30 = function(){
    return [this.prevDay(30).Format("yyyy-MM-dd"),this.prevDay(1).Format("yyyy-MM-dd")];
};

/**
 * get the date range of previous 90 days
 */
Date.prototype.prevDay90 = function(){
    return [this.prevDay(90).Format("yyyy-MM-dd"),this.prevDay(1).Format("yyyy-MM-dd")];
};

/**
 * 获取当前日期一周内的一天
 * @param {Object} _i
 */
Date.prototype.dayOfCurWeek = function(_i){
    var myDate = this,
        start = new Date(this.getTime());
    /*
     * 取得一周内的第一天、第二天、第三天... (本月的第几天-本周的第几天+下标)
     * myDate.getDay()==0?7:myDate.getDay() 本周表示周一~周日，和标准有区别
     */
    start.setDate(myDate.getDate() - (myDate.getDay()==0?7:myDate.getDay()) + _i);
    return start;
};

/**
 * 获取当前日期一周内的7天
 */
Date.prototype.daysOfCurWeek = function(_maxDate, _ifFull){
    var days = new Array(), dayNum = 7, now = new Date();
    if((now.getFullYear()==this.getFullYear()&&now.getWeekNumber()!=this.getWeekNumber())||_ifFull){        //判断日期是否和今天同一周
        //TODO
    }else{
       dayNum = now.prevDay(_maxDate).getDay();
    }
    for(var i=1;i<=dayNum;i++) {
        days[i-1] = this.dayOfCurWeek(i);
    }
    return days;
};

/**
 * 获取当前日期下一周
 * @param {Object} _i
 */
Date.prototype.dayOfNextWeek = function(_i){
    var day = this;
    day.setDate(day.nextDay(7).getDate());
    return day.dayOfCurWeek(_i);
};

/**
 * 获取当前日期下一周的7天
 */
Date.prototype.daysOfNextWeek = function(){
    var days = new Array();
    for(var i=1;i<=7;i++) {
        days[i-1] = this.dayOfNextWeek(i);
    }
    return days;
};

/**
 * 指定日期的上一周(前七天)
 * @param {Object} _i
 */
Date.prototype.dayOfPrevWeek = function(_i){
    var day = this;
    day.setDate(day.prevDay(7).getDate());
    return day.dayOfCurWeek(_i);
};

/**
 * 取得上一周的日期数(共七天)
 */
Date.prototype.daysOfPrevWeek = function(){
    var days = new Array();
    for(var i=1;i<=7;i--) {
        days[i-1] = this.dayOfPrevWeek(i);
    }
    return days;
};

/**
 * 获取本月的第几天
 * @param {Object} _i
 */
Date.prototype.dayOfCurMonth = function(_i){
    return new Date(this.getFullYear(), this.getMonth(), _i+1);
};
/**
 * 获取月的第一天
 */
Date.prototype.firstDayOfMonth = function(){
    return new Date(this.getFullYear(), this.getMonth(), 1);
};
/**
 * 获取月的最后一天
 */
Date.prototype.lastDayOfMonth = function(){
    var date = new Date(this.getFullYear(),this.getMonth()+1,1);
    return new Date(date.getTime()-864e5);
};
/**
 * 获取昨天的开始和结束日期
 */
Date.prototype.yesterday = function(){
    return [(this.prevDay(1)).Format("yyyy-MM-dd"),(this.prevDay(1)).Format("yyyy-MM-dd")];
};
/**
 * 获取周的第一天
 */
Date.prototype.firstDayOfWeek = function(){
    var date = new Date(this.getTime());
    return date.dayOfCurWeek(1);
};
/**
 * 获取周的最后一天
 */
Date.prototype.lastDayOfWeek = function(){
    var date = new Date(this.getTime());
    return date.dayOfCurWeek(7);
};
/**
 * 获取本周的开始和结束日期
 * @param _prevDayNum  最大日期配置近N天，如2-近2天，默认近1天
 * @returns {Array}
 */
Date.prototype.curWeek = function(_prevDayNum){
    var day = new Date(this.getTime()),today = new Date(),prevDayNum = !!_prevDayNum?_prevDayNum:1;
    if(day.getFullYear()==today.getFullYear()&&day.getWeekNumber()==today.getWeekNumber()){
        var endDay = today.prevDay();
        if(today.getDay()>Number(prevDayNum)){
            endDay = today.prevDay(prevDayNum);
        }else if(today.getDay()==Number(prevDayNum)){
            endDay = today;
        }
        return [(today.dayOfCurWeek(1)).Format("yyyy-MM-dd"),endDay.Format("yyyy-MM-dd")];
    }else{
        return [day.firstDayOfWeek().Format("yyyy-MM-dd"),day.lastDayOfWeek().Format("yyyy-MM-dd")];
    }
};

/**
 * 获取上周的开始和结束日期
 */
Date.prototype.preWeek = function(_n, _format){
    _n = _n||1;
    var date = new Date(this.getTime()),
        day = new Date(date.prevDay(7*_n).getTime());
    if(_format){
        return [(day.dayOfCurWeek(1)).Format(_format),(day.dayOfCurWeek(7)).Format(_format)];
    }else{
        return [day.dayOfCurWeek(1),day.dayOfCurWeek(7)];
    }
};

/**
 * 获取本月的开始和结束日期
 * @param _prevDayNum
 * @returns {Array}
 */
Date.prototype.curMonth = function(_prevDayNum){
    var day = new Date(this.getTime()),today = new Date(),prevDayNum=_prevDayNum||1;
    //judge if in current month
    if(day.getFullYear()==today.getFullYear()&&day.getMonth()==today.getMonth()){
        var endDay = (today.getDate()>=(Number(prevDayNum)+1))?today.prevDay(prevDayNum):(today.prevDay().getMonth()==today.getMonth()?today.prevDay():today);
        return [(new Date(day.getFullYear(), day.getMonth(), 1)).Format("yyyy-MM-dd"),endDay.Format("yyyy-MM-dd")];
    }else{
        return [day.firstDayOfMonth().Format("yyyy-MM-dd"),day.lastDayOfMonth().Format("yyyy-MM-dd")];
    }
};

/**
 * 获取上个月的开始和结束日期
 */
Date.prototype.preMonth = function(_n, _format, _ifMonth){
    _n = _n||1;
    var now = new Date(),
        start = new Date(this.getFullYear(), this.getMonth()-_n, 1),
        end = new Date(this.getFullYear(), this.getMonth()-_n+1, 1),
        end = end>now?now:end,      //截止日期默认为D-1
        days = (end-start)/(1000*60*60*24);
    if(_ifMonth){
        return (new Date(this.getFullYear(), this.getMonth()-_n, 1)).Format("yyyy-MM")
    }else{
        if(_format){
            return [start.Format(_format),(new Date(this.getFullYear(), this.getMonth()-_n, days)).Format(_format)];
        }else{
            return [start,new Date(this.getFullYear(), this.getMonth()-_n, days)];
        }
    }
};
/**
 * 获取最近几个月
 * @param _i
 * @returns {Array}
 */
Date.prototype.prevMonth = function(_i){
    var date = new Date(this.getTime()),curYear = date.getFullYear(),curMonth = date.getMonth();
    if(curMonth-_i<=0){
        curYear--;
        curMonth+=12;
    }
    return [new Date(curYear, curMonth-(_i-1), 1).Format("yyyy-MM-dd"), new Date(this.getTime()).prevDay().Format("yyyy-MM-dd")];
}
/**
 * 获取前X月的天数
 * @param _i
 * @returns {Array}
 */
Date.prototype.daysOfPrevMonth = function(_i){
    _i = _i||1;
    var date = new Date(this.getTime()),curYear = date.getFullYear(),curMonth = date.getMonth()-_i, days = [],count = 0,startDate,endDate;
    if(curMonth-_i<=0){
        curYear--;
        curMonth+=12;
    }
    startDate = new Date(curYear, curMonth, 1);
    if(curMonth+1>11){
        endDate = new Date(curYear+1, 0, 1);
    }else{
        endDate = new Date(curYear, curMonth+1, 1);
    }
    count = (endDate-startDate)/(24*36e5);
    for(var i=1;i<=count;i++){
        days.push(new Date(curYear, curMonth, i));
    }
    return days;
}
/**
 * 获取指定月的最近3个月
 * @returns {*}
 */
Date.prototype.prevMonth3 = function(){
    return new Date(this.getTime()).prevMonth(3);
}
/**
 * 获取指定月的最近6个月
 * @returns {*}
 */
Date.prototype.prevMonth6 = function(){
    return new Date(this.getTime()).prevMonth(6);
}
/**
 * 获取指定月的最近12个月
 * @returns {*}
 */
Date.prototype.prevMonth12 = function(){
    return new Date(this.getTime()).prevMonth(12);
}
/**
 * 获取最近几个月的计数
 * @param _i
 * @returns 2013.3（2013年8月15日近3个月）
 */
Date.prototype.prevMonthCounter = function(_i){
    var date = new Date(this.getTime()),curYear = date.getFullYear(),curMonth = date.getMonth();
    return curYear + "." +_i;
}
/**
 * 获取指定月的最近3个月
 * @returns {*}
 */
Date.prototype.prevMonthCounter3 = function(){
    return new Date(this.getTime()).prevMonthCounter(3);
}
/**
 * 获取指定月的最近6个月
 * @returns {*}
 */
Date.prototype.prevMonthCounter6 = function(){
    return new Date(this.getTime()).prevMonthCounter(6);
}
/**
 * 获取指定月的最近6个月
 * @returns {*}
 */
Date.prototype.prevMonthCounter12 = function(){
    return new Date(this.getTime()).prevMonthCounter(12);
}
/**
 * 获取上季度范围从始月到末月
 * @returns {*}
 */
Date.prototype.prevSeason = function(){
    var date = new Date(this.getTime()),curYear = date.getFullYear(),curMonth = date.getMonth(),step = 3;
    if(curMonth-step<=0){
        curYear--;
    }
    var range = {};
    for(var i=0;i<12;i++){
        if(i-step<0){
            range[i] = [new Date(curYear, 9, 1).Format("yyyy-MM-dd"), new Date(curYear, 11, 1).lastDayOfMonth().Format("yyyy-MM-dd")];
        }else{
            range[i] = [new Date(curYear, Math.floor(i/step-1)*step, 1).Format("yyyy-MM-dd"), new Date(curYear, Math.floor(i/step-1)*step+2, 1).lastDayOfMonth().Format("yyyy-MM-dd")];
        }
    };
    return range[curMonth];
}
/**
 * 获取上季度的序列
 * @returns {*}  2013.4(2013年第4季度）
 */
Date.prototype.prevSeasonCounter = function(){
    var date = new Date(this.getTime()),curYear = date.getFullYear(),curMonth = date.getMonth(),step = 3,counter = 0;
    if(curMonth-step<=0){
        curYear--;
        counter = curYear+"."+4;
    }else{
        counter = curYear+"."+Math.floor(curMonth/step);
    }
    return counter;
}

/**
 * 获取当月的所有日期
 * @param {Object} _maxDate
 * @param {boolean} _ifFull
 */
Date.prototype.daysOfCurMonth = function(_maxDate, _ifFull){
    var days = [], dayNum, now = new Date()
        ,start = new Date(this.getFullYear(), this.getMonth(), 1)
        ,end = new Date(this.getFullYear(), this.getMonth()+1, 1);
    if(this.Format('yyyyMM')!=now.Format('yyyyMM')||_ifFull){       //判断是否和今天同月或者参数为全显
       //TODO
    }else{
        end = _maxDate?now.prevDay(_maxDate):now;
    }
    dayNum = (end-start)/864e5;
    for(var i=0;i<dayNum;i++){
        days.push(new Date(start.getTime()+i*864e5));
    }
    return days;
};

/**
 * 判断闰年
 */
Date.prototype.isLeapYear = function(){
    return (0==this.getFullYear()%4&&((this.getFullYear()%100!=0)||(this.getFullYear()%400==0)));
};

/**
 * 日期格式化
 *  格式 YYYY/yyyy/YY/yy 表示年份
 *  MM/M 月份
 *  W/w 星期
 *  dd/DD/d/D 日期
 *  hh/HH/h/H 时间
 *  mm/m 分钟
 *  ss/SS/s/S 秒
 * @param {Object} formatStr
 */
Date.prototype.Format = function(_formatStr){
    var str = _formatStr;
    var Week = ['日','一','二','三','四','五','六'];

    str=str.replace(/yyyy|YYYY/,this.getFullYear());
    str=str.replace(/yy|YY/,(this.getFullYear() % 100)>9?(this.getFullYear() % 100).toString():'0' + (this.getFullYear() % 100));

    str=str.replace(/MM/,(this.getMonth()+1)>9?(this.getMonth()+1).toString():'0'+(this.getMonth()+1));
    str=str.replace(/M/g,this.getMonth()+1);

    str=str.replace(/w|W/g,Week[this.getDay()]);

    str=str.replace(/dd|DD/,this.getDate()>9?this.getDate().toString():'0' + this.getDate());
    str=str.replace(/d|D/g,this.getDate());

    str=str.replace(/hh|HH/,this.getHours()>9?this.getHours().toString():'0' + this.getHours());
    str=str.replace(/h|H/g,this.getHours());
    str=str.replace(/mm/,this.getMinutes()>9?this.getMinutes().toString():'0' + this.getMinutes());
    str=str.replace(/m/g,this.getMinutes());

    str=str.replace(/ss|SS/,this.getSeconds()>9?this.getSeconds().toString():'0' + this.getSeconds());
    str=str.replace(/s|S/g,this.getSeconds());

    return str;
};

/**
 * 日期计算
 * @param {Object} strInterval
 * @param {Object} Number
 */
Date.prototype.DateAdd = function(strInterval, Number) {
    var dtTmp = this;
    switch (strInterval) {
        case 's' :return new Date(Date.parse(dtTmp) + (1000 * Number));
        case 'n' :return new Date(Date.parse(dtTmp) + (60000 * Number));
        case 'h' :return new Date(Date.parse(dtTmp) + (3600000 * Number));
        case 'd' :return new Date(Date.parse(dtTmp) + (86400000 * Number));
        case 'w' :return new Date(Date.parse(dtTmp) + ((86400000 * 7) * Number));
        case 'q' :return new Date(dtTmp.getFullYear(), (dtTmp.getMonth()) + Number*3, dtTmp.getDate(), dtTmp.getHours(), dtTmp.getMinutes(), dtTmp.getSeconds());
        case 'm' :return new Date(dtTmp.getFullYear(), (dtTmp.getMonth()) + Number, dtTmp.getDate(), dtTmp.getHours(), dtTmp.getMinutes(), dtTmp.getSeconds());
        case 'y' :return new Date((dtTmp.getFullYear() + Number), dtTmp.getMonth(), dtTmp.getDate(), dtTmp.getHours(), dtTmp.getMinutes(), dtTmp.getSeconds());
    }
};

/**
 * 比较日期差 dtEnd 格式为日期型或者 有效日期格式字符串
 * @param {Object} strInterval
 * @param {Object} dtEnd
 */
Date.prototype.DateDiff = function(strInterval, dtEnd) {
    var dtStart = this,dval = 0;
    switch (strInterval) {
        case 's' :dval = parseInt((dtEnd - dtStart) / 1000);
            break;
        case 'n' :dval = parseInt((dtEnd - dtStart) / 60000);
            break;
        case 'h' :dval = parseInt((dtEnd - dtStart) / 3600000);
            break;
        case 'd' :dval = parseInt((dtEnd - dtStart) / 86400000)+1;
            break;
        case 'w' :dval = parseInt((dtEnd - dtStart) / (86400000 * 7));
            break;
        case 'm' :dval = (dtEnd.getMonth()+1)+((dtEnd.getFullYear()-dtStart.getFullYear())*12) - (dtStart.getMonth()+1);
            break;
        case 'y' :dval = dtEnd.getFullYear() - dtStart.getFullYear();
            break;
    }
    return dval;
};

/**
 * 日期输出字符串，重载了系统的toString方法
 * @param {Object} showWeek
 */
/*Date.prototype.toString = function(showWeek){
 var myDate= this;
 var str = myDate.toLocaleDateString();
 if (showWeek){
 var Week = ['日','一','二','三','四','五','六'];
 str += ' 星期' + Week[myDate.getDay()];
 }
 return str;
 }; */

/**
 * 把日期分割成数组
 */
Date.prototype.toArray = function(){
    var myDate = this;
    var myArray = Array();
    myArray[0] = myDate.getFullYear();
    myArray[1] = myDate.getMonth();
    myArray[2] = myDate.getDate();
    myArray[3] = myDate.getHours();
    myArray[4] = myDate.getMinutes();
    myArray[5] = myDate.getSeconds();
    return myArray;
};

/**
 * 取得日期数据信息
 * 参数 interval 表示数据类型
 * y 年 m月 d日 w星期 ww周 h时 n分 s秒
 * @param {Object} interval
 */
Date.prototype.DatePart = function(interval){
    var myDate = this;
    var partStr = '';
    var Week = ['日','一','二','三','四','五','六'];
    switch (interval){
        case 'y' :partStr = myDate.getFullYear()+"";break;
        case 'm' :partStr = (myDate.getMonth()+1)+"";break;
        case 'd' :partStr = myDate.getDate()+"";break;
        case 'w' :partStr = Week[myDate.getDay()]+"";break;
        case 'ww' :partStr = myDate.WeekNumOfYear()+"";break;
        case 'h' :partStr = myDate.getHours()+"";break;
        case 'n' :partStr = myDate.getMinutes()+"";break;
        case 's' :partStr = myDate.getSeconds()+"";break;
    }
    return partStr;
};

/**
 * 取得当前日期所在月的最大天数
 */
Date.prototype.MaxDayOfDate = function()
{
    var myDate = this;
    var ary = myDate.toArray();
    var date1 = (new Date(ary[0],ary[1]+1,1));
    var date2 = date1.dateAdd(1,'m',1);
    var result = dateDiff(date1.Format('yyyy-MM-dd'),date2.Format('yyyy-MM-dd'));
    return result;
};

/**
 * 取得当前日期所在周是一年中的第几周
 */
Date.prototype.WeekNumOfYear = function(){
    var myDate = this;
    var ary = myDate.toArray();
    var year = ary[0];
    var month = ary[1]+1;
    var day = ary[2];
    document.write("<script language='VBScript'\> \n");
    document.write("myDate = DateValue("+month+"-"+day +"-"+year+") \n");
    document.write("result = DatePart('ww', myDate) \n");
    document.write(" \n");
    return result;
};

/**
 * Array扩展
 * 禁用。造成for-in循环时数组读取错误
 * 校验方法移至Tool原型中
 */
//Array.prototype.contains = function(item){
//  return RegExp("\\b"+item+"\\b").test(this);
//};
//
//Array.prototype.max = function(){
//    return Math.max.apply({}, this);
//};

/**
 * 新增自定义jQuery插件
 * @author daipeng
 *
 *
 * $(obj).jColor()     对象加颜色
 * $(obj).jBackground()对象加背景
 * $(obj).jBorder()    对象加边框
 * $(tbl).jAlterTbBg() 表格行变色
 *
 *
 * jQuery.jLtrim(...)  删除左边空格
 * jQuery.jRtrim(...)  删除右边空格
 * jQuery.jIsNum(...)  判断是否数字
 */

var hasExtends = true;

/**
 * 无关业务的公用扩展方法
 */
;(function($){
    $.fn.extend({
        "live": function(_types, _data, _fn, _selector){
            return $(this).on(_types, _selector, _data, _fn);
        },
        "die": function(_types, _fn, _selector){
            return $(this).off(_types, _selector, _fn);
        },
        "jColor": function(_value){
            if(_value==undefined){
                return this.css("color");
            }else{
                return this.css("color", _value);
            }
        },
        "jDisplay": function(_bool){
            if(_bool==undefined){
                return this.css("display");
            }else{
                return this.css("display", _bool&&"block"||"none");
            }
        },
        "jBackground": function(_value){
            if(_value==undefined){
                return this.css("background");
            }else{
                return this.css("background", _value);
            }
        },
        "jBorder": function(_value){
            if(_value==undefined){
                return this.css("border");
            }else{
                return this.css("border", _value);
            }
        },
        "jSrc": function(_value){
            if(_value==undefined){
                return this.attr("src");
            }else{
                return this.attr("src", _value);
            }
        },
        "jHref": function(_value){
            if(_value==undefined){
                return this.attr("href");
            }else{
                return this.attr("href", _value);
            }
        },
        "jTitle": function(_value){
            if(_value==undefined){
                return this.attr("title");
            }else{
                return this.attr("title", _value);
            }
        },
        "jId": function(_value){
            if(_value==undefined){
                return this.attr("id");
            }else{
                return this.attr("id", _value);
            }
        },
        "jName": function(_value){
            if(_value==undefined){
                return this.attr("name");
            }else{
                return this.attr("name", _value);
            }
        },
        "jCheck": function(_value){
            if(_value==undefined){
                return this.attr("checked");
            }else{
                return this.attr("checked", _value);
            }
        },
        "jSize": function(_value){
            if(_value==undefined){
                return this.attr("size");
            }else{
                return this.attr("size", _value);
            }
        },
        "jDisable": function(){
            return this.each(function(){
                if(typeof this.disabled !="undefined") this.disabled = true;
            });
        },
        "jCheckAll": function(){
            return this.each(function(){
                if(typeof this.disabled !="undefined") this.disabled = false;
            });
        },
        "jLeft": function(_value){
            if(_value==undefined){
                return $(this).offset().left;
            }else{
                $(this).css("left", _value);
            }
        },
        "jRight": function(_value){
            if(_value==undefined){
                return $(this).offset().right;
            }else{
                $(this).css("right", _value);
            }
        },
        "jTop": function(_value){
            if(_value==undefined){
                return $(this).offset().top;
            }else{
                $(this).css("top", _value);
            }
        },
        "jBottom": function(_value){
            if(_value==undefined){
                return $(this).offset().bottom;
            }else{
                $(this).css("bottom", _value);
            }
        },
        "jHeight": function(_value){
            if(_value==undefined){
                return $(this).height();
            }else{
                $(this).css("height", _value);
            }
        },
        "jWidth": function(_value){
            if(_value==undefined){
                return $(this).width();
            }else{
                $(this).css("width", _value);
            }
        },
        "jFloat": function(_value){
            if(_value==undefined){
                return $(this).css("float");
            }else{
                $(this).css("float", _value);
            }
        },
        "jAlterBg": function(_options){
            options = $.extend({
                odd: "odd",
                even: "even",
                selected: "selected"
            }, _options);
            $("tbody>tr:odd", this).addClass(options.odd);
            $("tbody>tr:even", this).addClass(options.even);
            $("tbody>tr", this).click(function(){
                //var hasSelected = $(this).is("."+options.selected);
                //$(this)[hasSelected?"removeClass":"addClass"](options.selected).find(":checkbox").attr("checked", !hasSelected);
                //$("tbody>tr:has(:checked)", this).addClass(options.selected);
                return this;
            });
        },
        buttonSlider:function(){
            this.each(function(){
                var _o = new buttonSlider(this);
                _o.init();
            });
        },
        keyInput:function(){
            $(this).each(function(){
                var _o = new keyInput(this);
                _o.init();
            });
            return this;
        },
        "jOnScreen": function(){
            var win = $(window),
                viewport = {
                    top : win.scrollTop(),
                    left : win.scrollLeft()
                };

            viewport.right = viewport.left + win.width();
            viewport.bottom = viewport.top + win.height();

            var bounds = this.offset();
            bounds.right = bounds.left + this.outerWidth();
            bounds.bottom = bounds.top + this.outerHeight();

            return (!(viewport.right < bounds.left || viewport.left > bounds.right || viewport.bottom < bounds.top || viewport.top > bounds.bottom));
        }
    });
    $.extend({
        jBrowser: function(){
            var browser = {};
            var ua = navigator.userAgent.toLowerCase();
            if (window.ActiveXObject || "ActiveXObject" in window)
                browser.ie = (ua.match(/msie ([\d.]+)/)||ua.match(/rv:([\d.]+)/))[1];
            else if (ua.indexOf('firefox')>=0)
                browser.firefox = ua.match(/firefox\/([\d.]+)/)[1];
            else if (window.MessageEvent && !document.getBoxObjectFor){
                if(ua.indexOf('chrome')>=0){
                    browser.chrome = ua.match(/chrome\/([\d.]+)/)[1];
                }else{
                    browser.safari = ua.match(/version\/([\d.]+)/)[1];
                }
            }else if (window.opera)
                browser.opera = ua.match(/opera.([\d.]+)/)[1];
            return browser;
        },
        jTrim: function(_text){
            return (_text||"").trim();
        },
        jLtrim: function(_text){
            return (_text||"").ltrim();
        },
        jRtrim: function(_text){
            return (_text||"").rtrim();
        },
        jIsNumber: function(_text){
            return _text.isNumber();
        },
        jIsInt: function(_text){
            return _text.isInt();
        },
        jIsPlusInt: function(_text){
            return _text.isPlusInt();
        },
        jIsTel: function(_text){
            return _text.isTel();
        },
        jIsMobile: function(_text){
            return _text.isMobile();
        },
        jIsEmail: function(_text){
            return _text.isEmail();
        },
        jIsZip: function(_text){
            return _text.isZip();
        },
        jGetRootPath: function(){
            var curPath = window.document.location.href;
            var pathName = window.document.location.pathname;
            var path = curPath.substring(0,curPath.indexOf(pathName));
            var rootPath = pathName.substring(0,pathName.substr(1).indexOf('/')+1);
            return path+rootPath;
        },
        jString2Date: function(_text, _split){
            var myDate = new Date(Date.parse(_text));
            if (isNaN(myDate)){
                if(_text.indexOf(_split)>=0){
                    var arys= _text.split(_split);
                    switch(arys.length){
                        case 1: myDate = new Date(arys[0],0,1);
                            break;
                        case 2:	myDate = new Date(arys[0],--arys[1],1);
                            break;
                        case 3: myDate = new Date(arys[0],--arys[1],arys[2]);
                            break;
                    }
                }else{
                    var tmp = "";
                    switch(_text.length){
                        case 4: tmp = _text.substr(0, 4) + "/01/01" ;
                            break;
                        case 6: tmp = _text.substr(0, 4) + "/" + _text.substr(4, 2) + "/01" ;
                            break;
                        case 8: tmp = _text.substr(0, 4) + "/" + _text.substr(4, 2) + "/" + _text.substr(6, 2);
                            break;
                    }
                    myDate = new Date(Date.parse(tmp));
                }
            }
            return myDate;
        },
        jFormatDate: function(_obj, _split){
            var date = _obj+"";
            switch(date.length){
                case 4: date = date.substr(0, 4) + _split + "01" + _split + "01" ;
                    break;
                case 6: date = date.substr(0, 4) + _split + date.substr(4, 2) + _split + "01" ;
                    break;
                case 8: date = date.substr(0, 4) + _split + date.substr(4, 2) + _split + date.substr(6, 2);
                    break;
            }
            return date;
        },
        /**
         * 重新构造日期格式
         * 20121010构造为2012年10月10日
         * 20121010构造为2012{-/.}10{-/.}10
         * @param _dateStr
         * @param _split
         * @returns {string}
         */
        jRebuildDate: function(_dateStr, _split){
            var _ = this;
            if(_split&&!_.jIsArray(_split)){
               _split = [_split];
            }
            var split = _split?[_split[0],_split[0],"",_split[1]||":",_split[1]||":",_split[1]||"."]:["年","月","日","时","分","秒"];
            if(_dateStr&&_dateStr.length==8){
                return _dateStr.substr(0,4) + split[0] + _dateStr.substr(4, 2) + split[1] + _dateStr.substr(6, 2) + (!_split?split[2]:"");
            }else if(_dateStr&&_dateStr.length==10){
                return _dateStr.substr(0,4) + split[0] + _dateStr.substr(4, 2) + split[1] + _dateStr.substr(6, 2) + split[2] + " " + _dateStr.substr(8, 2) + (!_split?split[3]:"");
            }else if(_dateStr&&_dateStr.length==12){
                return _dateStr.substr(0,4) + split[0] + _dateStr.substr(4, 2) + split[1] + _dateStr.substr(6, 2) + split[2] + " " + _dateStr.substr(8, 2) + split[3] + _dateStr.substr(10,2) + (!_split?split[4]:"");
            }else if(_dateStr&&_dateStr.length==14){
                return _dateStr.substr(0,4) + split[0] + _dateStr.substr(4, 2) + split[1] + _dateStr.substr(6, 2) + split[2] + " " + _dateStr.substr(8, 2) + split[3] + _dateStr.substr(10,2) + split[4] + _dateStr.substr(12,2) + (!_split?split[5]:"");
            }else if(_dateStr&&_dateStr.length==17){
                return _dateStr.substr(0,4) + split[0] + _dateStr.substr(4, 2) + split[1] + _dateStr.substr(6, 2) + split[2] + " " + _dateStr.substr(8, 2) + split[3] + _dateStr.substr(10,2) + split[4] + _dateStr.substr(12,2) + split[5] + _dateStr.substr(14,3);
            }else if(_dateStr&&_dateStr.length==19){
                return _dateStr.substr(0,4) + split[0] + _dateStr.substr(5, 2) + split[1] + _dateStr.substr(8, 2) + split[2];
            }else{
                return "";
            }
        },
        jParseDateString: function(dateString,formatter){
            var today = new Date();
            if(!formatter){
                formatter = "yyyy-MM-dd";
            }
            if(!dateString){
                return today;
            }

            var yearMarker = formatter.replace(/[^y]/g,''),
            monthMarker = formatter.replace(/[^M]/g,''),
            dayMarker = formatter.replace(/[^d]/g,''),
            hourMarker = formatter.replace(/[^h]/g,''),
            minuteMarker = formatter.replace(/[^m]/g,''),
            secondMarker = formatter.replace(/[^s]/g,''),
            yearPosition = formatter.indexOf(yearMarker),
            yearLength = yearMarker.length,
            year =  dateString.substr(yearPosition ,yearLength)*1;
            if( yearLength == 2){
                if(year < 50){
                    year += 2000;
                }else{
                    year += 1900;
                }
            }
            var monthPosition = formatter.indexOf(monthMarker),
            month = dateString.substr(monthPosition,monthMarker.length) * 1 - 1,
            dayPosition = formatter.indexOf(dayMarker),
            day = dateString.substr( dayPosition,dayMarker.length )* 1;

            var hourPosition = formatter.indexOf(hourMarker),
                hour = dateString.substr(hourPosition,hourMarker.length) * 1;

            var minutePosition = formatter.indexOf(minuteMarker),
                minute = dateString.substr(minutePosition,minuteMarker.length) * 1;
            var secondPosition = formatter.indexOf(secondMarker),
                second = dateString.substr(secondPosition,secondMarker.length) * 1;

            return new Date(year,month,day,hour,minute,second);
        },
        /*
         * 2012-06-01 10转化为1341828000000
         * 20120601 10
         */
        jTime2Utc: function(_timeStr){
            if(_timeStr&&_timeStr!=undefined){
                if(_timeStr.indexOf("-")>=0){
                    var year = _timeStr.substr(0,4);
                    var month = _timeStr.substr(5, 2);
                    month = month.indexOf("0")==0?month.substr(1,1):month;
                    var day = _timeStr.substr(8, 2);
                    day = day.indexOf("0")==0?day.substr(1,1):day;
                    var hour = _timeStr.substr(11, _timeStr.length);
                    hour = (hour.length==2&&hour.indexOf("0")==0)?hour.substr(1,1):hour;
//				this.log(year+","+month+","+day+","+hour);

                    return Date.UTC(year, month-1, day, hour);
                }else if(_timeStr.indexOf("-")<0){
                    var year = _timeStr.substr(0,4);
                    var month = _timeStr.substr(4, 2);
                    month = month.indexOf("0")==0?month.substr(1,1):month;
                    var day = _timeStr.substr(6, 2);
                    day = day.indexOf("0")==0?day.substr(1,1):day;
                    var hour = _timeStr.substr(9, _timeStr.length);
                    hour = (hour.length==2&&hour.indexOf("0")==0)?hour.substr(1,1):hour;
//				this.log(year+","+month+","+day+","+hour);

                    return Date.UTC(year, month-1, day, hour);
                }else{
                    return 0;
                }
            }
        },
        jNum2Date: function(_num, _format){
            var date = new Date();
            if(_num&&$.jIsNumber(_num+"")){
                date.setTime(_num);
                return date.Format(_format?_format:"yyyy-MM-dd");
            }else{
                return "";
            }
        },
        jInArray: function(_array, _item){
            return RegExp("\\b"+item+"\\b").test(_array);
        },
        jBetween: function(_d1, _d2){
            var month1 = _d1.substring(5,_d1.lastIndexOf ('-'));
            var day1 = _d1.substring(_d1.length,_d1.lastIndexOf ('-')+1);
            var year1 = _d1.substring(0,_d1.indexOf ('-'));

            var month2 = _d2.substring(5,_d2.lastIndexOf ('-'));
            var day2 = _d2.substring(_d2.length,_d2.lastIndexOf ('-')+1);
            var year2 = _d2.substring(0,_d2.indexOf ('-'));

            var cha = ((Date.parse(month1+'/'+day1+'/'+year1)- Date.parse(month2+'/'+day2+'/'+year2))/86400000);
            return Math.abs(cha);
        },
        jKeyDown: function(_id, _f){
            $('#'+_id).keydown(_f);
        },
        jKeyPress: function(_id, _f){
            $('#'+_id).keypress(_f);
        },
        jKeyUp: function(_id, _f){
            $('#'+_id).keyup(_f);
        },
        jXml2Json: function(_data) {
            var dom;
            if (typeof(_data)=="object") {
                dom = _data;
            }else if (typeof(_data)=="string"){
                if(this.jIsIE()){                       //IE
                    dom = new ActiveXObject("Microsoft.XmlDom");
                    dom.async = "false";
                    dom.loadXML(_data);			//_data是*.xml或者txt
                }else{					        //FireFox、Chrome
                    if(_data.indexOf(".xml")>=0){
                        dom = document.implementation.createDocument("","",null);    //_data是*.xml
                        dom.async = "false";
                        dom.loadXML(_data);
                    }else{
                        dom = new DOMParser().parseFromString(_data, "text/xml");	  //_data是txt
                        dom.loadXML(_data,"text/xml");
                    }
                }
            }
            var exec = function(ele) {
                var o = {};
                var len = (ele.attributes)?ele.attributes.length:0;
                for(var i = 0; i < len; i++){
                    o["$" + ele.attributes[i].name] = ele.attributes[i].value;
                }

                len = ele.childNodes.length;
                if(len==0){
                    return o;
                }

                var tmp;
                for ( var i = 0; i < len; i++) {
                    tmp = o[ele.childNodes[i].nodeName];
                    if (typeof(tmp)=="undefined"){
                        if (ele.childNodes[i].childNodes.length==0){
                            if (ele.childNodes[i].nodeName == "#text"
                                || ele.childNodes[i].nodeName == "#cdata-section"){
                                o["$$"] = ele.childNodes[i].nodeValue;
                            } else {
                                o[ele.childNodes[i].nodeName] = [exec(ele.childNodes[i])];
                            }

                        } else {
                            o[ele.childNodes[i].nodeName] = [exec(ele.childNodes[i])];
                        }
                    } else {
                        tmp[tmp.length] = exec(ele.childNodes[i]);
                        o[ele.childNodes[i].nodeName] = tmp;
                    }
                }
                return o;
            };

            var json = {};
            json[dom.documentElement.nodeName] = exec(dom.documentElement);
            return json;
        },
        jAlarmClock: function(_flag,_time,_funs){
            setInterval(function(){
                if(_flag){
                    _funs;
                    clearInterval();
                }
            }, _time);
        },
        jClzssName: function(_clzss){
            if(typeof(_clzss)=="string"){
                return _clzss;
            }
            var cl = _clzss.toString();
            if(cl.indexOf("function")<0){
                return null;
            }else{
                cl = cl.replace("function","");
                cl = cl.substring(0, cl.indexOf("("));
                cl = cl.replace(" ", "");
            }
            return cl;
        },
        jRun: function(_name){
            try{
                eval("new "+name+"()");
            }catch(e){
                alert("Wrong Clzss!");
            }
        },
        jMakeArray: function(_obj){
            return Array.prototype.slice.call(_obj,0);
        },
        jArrayMax: function(_array){
            return Math.max.apply({},_array);
        },
        jArrayMin: function(_array){
            return Math.min.apply({},_array);
        },
        jIsArray: function(_obj){
            return _obj instanceof Array;
        },
        jIsString: function(_obj){
            return typeof(_obj)=="string";
        },
        /**
         * get date by '_y-_w'
         * @param _y
         * @param _w
         */
        jDateByWeek: function(_y,_w){
            var date = new Date(_y,0,1), factor;
            if(date.getWeekNumber()>1){     //判断全年第一天是否前一年的最后一周
                factor = _w;
            }else{
                factor = _w-1;
            }
            date.setTime(date.getTime()+factor*6048e5);
            return date;
        },
        /**
         * judge if page exsits dom
         * @param _id
         */
        jDomExist: function(_id, _css){
            if(_css){
                return $("#"+_id).find("."+_css).length>0;
            }else{
                return $("#"+_id).length>0;
            }
        },
        /**
         * add commas
         *   usage: $.jCommaVal(12345678);
         *   result: 12,345,678
         * @param _val
         * @returns {string}
         */
        jCommaVal: function(_val){
            var delimiter = ","; // replace comma if desired
            _val = new String(_val);
            var minus = _val.indexOf("-")>=0?'-':'';
            var a = _val.split('.',2);
            var d ='';
            if(a.length>1) d = a[1];
            var i = parseInt(a[0]);
            if(isNaN(i)) { return ''; }

            if(i < 0) { minus = '-'; }
            i = Math.abs(i);
            var n = new String(i);
            var a = [];
            while(n.length > 3)
            {
                var nn = n.substr(n.length-3);
                a.unshift(nn);
                n = n.substr(0,n.length-3);
            }
            if(n.length > 0) { a.unshift(n); }
            n = a.join(delimiter);
            if(d.length < 1) { _val = n; }
            else { _val = n + '.' + d; }
            _val = minus + _val;
            return _val;
        },
        /**
         * round the value
         *   usage:  $.jRoundVal(12345.678, 2);
         *   result: 12345.68
         * @param _val
         * @param _dec
         * @returns {string}
         */
        jRoundVal: function(_val, _dec){
            if( isNaN(_val) ) _val=0; //如果是非数值数据，则设置为0，防止非法数据产生“NaN.00%”结果
            if(_val==null || _val=='') _val=0;
            var sNum = _val + '';
            var idx = sNum.indexOf(".");
            if(idx<0){ //num是整数的情况
                var str_decimal='.';
                if( _dec == 0 ){
                    str_decimal ='';
                }
                for(n=1; n<=_dec; n++){
                    str_decimal += '0';
                }
                return _val+str_decimal;  //format_number(10,2)   四舍五入为 10.00
            }
            var result=0;
            var n = sNum.length - idx -1;  //n代表原来的小数位
            if(_dec < n){
                var e = Math.pow(10,_dec);
                result = Math.round(_val * (e*10)/10) / e;
                //判断result是否包含小数位 (针对情况: format_number(15.9953,2); )
                if((result+'').indexOf('.')==-1){
                    var str_decimal='.';
                    if( _dec == 0 ){
                        str_decimal ='';
                    }
                    for(n=1; n<=_dec; n++){
                        str_decimal += '0';
                    }
                    result += str_decimal;
                }
            }else if(_dec==n){
                result=_val;
            }else if(_dec>n){ //10.0  要转成 10.00 的情况
                var str_1='';
                for(i=1; i<=_dec-n; i++) str_1+='0';
                result = _val+str_1;
            }
            //补足小数位
            var sresult = result+'';
            var decimal_n = sresult.length - sresult.indexOf(".") -1;
            for(i=1; i<=_dec-decimal_n; i++) sresult+='0';
            return sresult;
        },
        /**
         * 任意小数位去尾法
         * @param _val
         * @param _dec
         * @returns {number}
         */
        jFloorVal: function(_val, _dec){
            var val,power=1;
            if(typeof(_val)=="string"){
                val = Number(_val);
            }else{
                val = _val;
            }
            if(!isNaN(val)){
                if(_dec){
                    power = Math.pow(10, _dec);
                    return Math.floor(val*power)/power;
                }else{
                    return Math.floor(val);
                }
            }else{
                return null;
            }
        },
        /**
         * format the value
         * @param _val
         * @param _type
         DATATYPE_INT: 0,
         DATATYPE_DOUBLE: 1,
         DATATYPE_STRING: 2,
         DATATYPE_BOOL: 3,
         DATATYPE_ARRAY: 4,
         DATATYPE_DATE: 5,
         DATATYPE_PERCENT: 6
         * @param _decimal
         * @returns {*}
         */
        jFormatVal: function(_val, _type, _decimal){
            var d = _val||0;
            if((d+"").indexOf("<")>=0){
                if((!!$(d+"")&&$(d+"").text()=="-")){
                    return d;
                }
            }else{
                if((d+"")=="-"){
                    return d;
                }
            }
            switch(_type){
                case 1:
                    d = $.jCommaVal($.jRoundVal(d,!!_decimal?_decimal:0)); //四舍五入
                    break;
                case 6:
                    //if data type is percent and the value contains %
                    if((d+"").indexOf("%")>=0){
                        return d;
                    }
                    d = $.jRoundVal(d*100,!!_decimal?_decimal:0)+"%"; //四舍五入
                    break;
                case 0:
                    if((d+"").indexOf(".0")>0){
                        d = $.jCommaVal((d+"").substr(0, (d+"").indexOf(".0")));
                    }else{
                        d = $.jCommaVal(d);
                    }
                    break;
                case 5:
                    if((d+"").isNumber()&&(d+"").length>10){
                        d = $.jNum2Date(d, "yyyy-MM-dd hh:mm:ss");
                    }
                    break;
                case 3:
                case 2:
                case 4:
                    break;
                default:	break;
            }
            return d;
        },
        /**
         *  比较两个JSON对象，转换为字符串后比较
         */
        jCompJson: function(_json1, _json2){
            return JSON.stringify(_json1)==JSON.stringify(_json2);
        },
        /**
         * 如果数字小于10，则十位补零，多用于时间显示
         * @param _val
         * @returns {string}
         */
        jZeroFill: function(_val){
            return _val<10?("0"+_val):_val;
        },
        /**
         * 页面导出EXCEL
         * @param _tbl
         * @param _name
         */
        jDnExcel: function(_tbl, _name){
            if($.jIsIE()){
                try{
//                    var WshShell=new ActiveXObject("WScript.Shell");
//                    WshShell.RegWrite("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Zones\\3\\1201","0","REG_DWORD");
                    var $curTbl = $("#"+_tbl);
                    $("th a",$curTbl).each(function(){
                        $(this).remove();
                    });
                    $("img",$curTbl).each(function(){
                        $(this).remove();
                    });
                    $("input:hidden",$curTbl).each(function(){
                        $(this).remove();
                    });
                    var tbl = $curTbl[0]
                        ,oXL = new ActiveXObject("Excel.Application") //创建AX对象excel
                        ,oWB = oXL.Workbooks.Add()  //获取workbook对象
                        ,oSheet = oWB.ActiveSheet;  //激活当前sheet

                    var sel = document.body.createTextRange();
                    sel.moveToElementText(tbl);     //把表格中的内容移到TextRange中
                    sel.execCommand("Copy");     //复制TextRange中内容
                    oSheet.Paste();    //粘贴到活动的EXCEL中
                    oXL.Visible = true;    //设置excel可见属性
                }catch(e){
                    $.jLog(e);
                }finally{
//                    WshShell.RegWrite("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Zones\\3\\1201","3","REG_DWORD");
                }
            }else{
                var uri = 'data:application/vnd.ms-excel;base64,',
                    template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>',
                    base64 = function(s) {
                        var tblCont = s.substring(s.indexOf("<table>"), s.indexOf("</table>")+8)
                            ,$curTbl = $(tblCont);
                        s = s.replace(tblCont, "###");
                        $("th a",$curTbl).each(function(){
                            $(this).remove();
                        });
                        $("img",$curTbl).each(function(){
                            $(this).remove();
                        });
                        $("input:hidden",$curTbl).each(function(){
                            $(this).remove();
                        });
                        s = s.replace("###", $curTbl[0].outerHTML);
                        return window.btoa(unescape(encodeURIComponent(s)));
                    },
                    format = function(s, c) {
                        return s.replace(/{(\w+)}/g,function(m, p) { return c[p]; });
                    };
                if (!_tbl.nodeType) {
                    _tbl = document.getElementById(_tbl);
                }
                var ctx = {worksheet: _name || 'Worksheet', table: _tbl.innerHTML};
                window.location.href = uri + base64(format(template, ctx));
                //            var wnd;
                //            wnd = window.open(uri + base64(format(template, ctx)),"", "width=0, height=0,status=0");
                //            wnd.document.execCommand("SaveAs",false,"测试.xls");
                //            wnd.close();
            }
        },
        /**
         * 生成随机数
         * @param _num
         */
        jRandom: function(_num){
            return Math.floor(Math.random()*_num+1);
        },
        /**
         * 对象的深度克隆
         * @param _obj
         * @returns {*}
         */
        jClone: function(_obj){
            if(!_obj||'object'!=typeof _obj){
                return _obj;
            }
            var p,v,c = Object.prototype.toString.call(_obj) =='[object Array]'?[]:{};
            for(p in _obj){
                if(_obj.hasOwnProperty(p)){
                    v = _obj[p];
                    if(v&&'object'==typeof v){
                        c[p] = this.jClone(v);
                    }else{
                        c[p] = v;
                    }
                }
            }
            return c;
        },
        jLog: function(_args){
            try{
                if("undefined"!=typeof(console)&&"undefined"!=typeof(console.log)){
                    console.log("["+new Date().Format("YYYY-MM-DD hh:mm")+"]"+_args);
                }
            }catch(e){
                // not support console method (ex: IE)
            }
        },
        /**
         * 通过周数获取当前周
         * @param _num  周数
         * @param _y 年份
         * @returns {Array}
         */
        jGetWeekByNum: function(_num, _y){
            var date = new Date(!!_y?_y:date.getFullYear(),0,1),time;
            time = date.getTime();
            return new Date(time+(_num-1)*7*864e5).curWeek();
        },
        /**
         *
         * @param date
         * @param fmt
         * @returns {*}
         */
        jDate2Str:function(date,fmt){//将日期按指定格式forma
            var o = {
                "M+": date.getMonth() + 1, //月份
                "d+": date.getDate(), //日
                "h+": date.getHours(), //小时
                "m+": date.getMinutes(), //分
                "s+": date.getSeconds(), //秒
                "q+": Math.floor((date.getMonth() + 3) / 3), //季度
                "S": date.getMilliseconds() //毫秒
            };
            if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        },
        /**
         * 将日期字符串(yyyy-MM-dd hh:mm:ss /yyyy-MM-dd )转化成日期类型
         * @param _dateStr
         * @returns {*}
         */
        jStr2Date:function(_dateStr){
            if(!!_dateStr){
                return  new Date(Date.parse(_dateStr.replace(/-/g,"/")));
            }else{
                return null;
            }

        },
        /**
         * 判断对象是否方法
         * @param _obj
         * @returns {boolean}
         */
        jIfFunction: function(_obj){
            return (typeof(_obj)!='undefined')&&(_obj instanceof Function);
        },
        /**
         * 判断对象是否对象
         * @param _obj
         * @returns {boolean}
         */
        jIfObject: function(_obj){
            return typeof _obj!='undefined'&&typeof _obj == "object";
        },
        /**
         * 脚本文件的引用路径增加时间戳规避缓存
         * @param url
         * @returns {*}
         */
        jConvertSrc: function(url){
            var timstamp = (new date).valueOf();
            if (url.indexOf("?")>=0){
                url = url + "&t=" + timstamp;
            }else {
                url = url + "?t=" + timstamp;
            };
            return url;
        },
        /**
         * 按规则筛选CSS样式表中的规则
         * @param _cssFileName CSS文件名
         * @param _re 正则式
         */
        jFilterCss: function(_cssFileName, _re){
            var sheets = document.styleSheets,cssRules,rules = [];
            for(var i in sheets){
                if(i.isNumber()){
                    if(sheets[i].href.indexOf(_cssFileName)>=0){
                        cssRules = sheets[i].cssRules||sheets[i].rules;
                        for(var j in cssRules){
                            if(j.isNumber()&&_re.test(cssRules[j].selectorText)){
                                rules.push(cssRules[j].selectorText);
                            }
                        }
                    }
                }
            }
            return rules;
        },
        /**
         *
         * @param _array
         * @param _mode/_callbak
         * @returns {*}
         */
        jSort: function(_array){
            if(arguments[1]&&typeof(arguments[1])=="string"){
                switch(arguments[1]){
                    case "bubble":
                        var i = 0, len = _array.length,
                            j, d;
                        for(; i<len; i++){
                            for(j=0; j<len; j++){
                                if(_array[i] < _array[j]){
                                    d = _array[j];
                                    _array[j] = _array[i];
                                    _array[i] = d;
                                }
                            }
                        }
                        return _array;
                    case "quick":
                        var i = 0,j = _array.length - 1
                            ,Sort = function(i, j){
                            // 结束条件
                            if(i == j ){ return };
                            var key = _array[i]
                                ,tempi = i // 记录开始位置
                                ,tempj = j; // 记录结束位置
                            while(j > i){
                                // j <<-------------- 向前查找
                                if(_array[j] >= key){
                                    j--;
                                }else{
                                    _array[i] = _array[j]
                                    //i++ ------------>>向后查找
                                    while(j > ++i){
                                        if(_array[i] > key){
                                            _array[j] = _array[i];
                                            break;
                                        }
                                    }
                                }
                            }
                            // 如果第一个取出的 key 是最小的数
                            if(tempi == i){
                                Sort(++i, tempj);
                                return ;
                            }
                            // 最后一个空位留给 key
                            _array[i] = key;
                            // 递归
                            Sort(tempi, i);
                            Sort(j, tempj);
                        }
                        Sort(i, j);
                        return _array;
                    case "insert":
                        var i = 1, j, temp, key, len = _array.length;
                        for(; i < len; i++){
                            temp = j = i;
                            key = _array[j];
                            while(--j > -1){
                                if(_array[j] > key){
                                    _array[j+1] = _array[j];
                                }else{
                                    break;
                                }
                            }
                            _array[j+1] = key;
                        }
                        return _array;
                }
            }else{
                if(arguments[1]){
                    return _array.sort(arguments[1]);
                }else{
                    return _array.sort(function(a, b){
                        return a - b;
                    });
                }
            }
        },
        jMask:function(){
            var showLoading = (function () {
                var elm = $("#Loading");
                elm.css({
                    left: $(window).width() / 2 - elm.width() / 2,
                    top: $(window).height() / 2 - elm.height()
                });
                elm.show();
            });
            var hideLoading = (function () {
                $('#Loading').hide();
            });
            var showMask = (function () {
                $("#Mask").css({
                    left: 0,
                    top: 0,
                    width: $(window).width(),
                    height: $(window).height()
                });
                $("#Mask").show();
            });
            var hideMask = (function () {
                $('#Mask').hide();
            });
            var render = (function(){
                var buf = [];
                buf.push('<div id="Mask" class="hidden"></div>');
                buf.push('<div id="Loading" class="spinner hidden">');
                /*buf.push('<div class="spinner-container container1">');
                buf.push('<div class="circle1"></div>');
                buf.push('<div class="circle2"></div>');
                buf.push('<div class="circle3"></div>');
                buf.push('<div class="circle4"></div>');
                buf.push('</div>');
                buf.push('<div class="spinner-container container2">');
                buf.push('<div class="circle1"></div>');
                buf.push('<div class="circle2"></div>');
                buf.push('<div class="circle3"></div>');
                buf.push('<div class="circle4"></div>');
                buf.push('</div>');
                buf.push('<div class="spinner-container container3">');
                buf.push('<div class="circle1"></div>');
                buf.push('<div class="circle2"></div>');
                buf.push('<div class="circle3"></div>');
                buf.push('<div class="circle4"></div>');
                buf.push('</div>');*/
                buf.push('</div>');
                $("body").prepend(buf.join(''));
                showMask();
                showLoading();
            });
            return {
                render:render,
                showMask:showMask,
                showLoading:showLoading,
                hideMask:hideMask,
                hideLoading:hideLoading
            }
        }(),
        /**
         * 水印模拟
         * @param _options｛
         *      dom: svg/table,
         *      mode: full/flag,
         *      full: {
         *          bgcolor: "#000",
         *          copyright: {
             *          name: "BDP.ARD",
             *          cssText: {"color":"#red"}
         *          }
         *      },
         *      flag: {
         *          bgimg: "watermark.png",
         *          width: 200,
         *          height: 200
     *          }
         * ｝
         */
        jWaterMark: function(_settings){
            var fWaterMark = function(_options, _doc){
                var $srcObj,$srcDiv,objs = _doc.getElementsByTagName(_options.dom);
                for(var i=0,len=objs.length;i<len;i++){
                    $srcObj = $(objs[i]);
                    $srcDiv = $srcObj.closest("div");
                    var width = $srcDiv[0].offsetWidth, height = $srcDiv[0].offsetHeight;
                    $srcDiv.css({"position":"relative"});
                    $('.watermark',$srcDiv).remove();
                    $srcDiv.prepend('<div class="watermark"></div>');

                    if(_options.mode==="full"){         //如果满容器水印
                        $(".watermark",$srcDiv).css({
                            "z-index": "1000",
                            "opacity": "0.1",
                            "position": "absolute",
                            "background-color": _options.full.bgcolor,
                            "width": width+"px",
                            "height": height+"px"
                        }).html('<div class="copyright">'+_options.full.copyright.name+'</div>');
                        $(".copyright",$srcDiv).css({"position": "absolute"}).css(_options.full.copyright.cssText);
                    }else if(_options.mode==="flag"){    //标志图标水印
                        if(_options.layer = "underlayer"){
                            $srcObj.css({
                                "opacity":"0.95",
                                "filter": "alpha(opacity=95)",
                                "-moz-opacity" : "0.95"
                            });
                            $srcObj.children().css({
                                "opacity":"0.95",
                                "filter": "alpha(opacity=95)",
                                "-moz-opacity" : "0.95"
                            });
                        }else{
                            $(".watermark",$srcDiv).css({
                                "z-index": "1000",
                                "opacity": "0.1",
                                "filter": "alpha(opacity=10)",
                                "-moz-opacity" : "0.1"
                            });
                        }
                        $(".watermark",$srcDiv).css({
                            "position": "absolute",
                            "background": 'url('+_options.flag.bgimg+') scroll no-repeat 0 0',
                            "width": _options.flag.width+"px",
                            "height": _options.flag.height+"px"
                        });
                        var $watermarkDiv = $('.watermark',$srcDiv), cssStyle = {};
                        if(_options.flag.top||_options.flag.right||_options.flag.bottom||_options.flag.left){
                            if(_options.flag.top){
                                cssStyle.top = _options.flag.top+"px";
                            }
                            if(_options.flag.right){
                                cssStyle.right = _options.flag.right+"px";
                            }
                            if(_options.flag.bottom){
                                cssStyle.bottom = _options.flag.bottom+"px";
                            }
                            if(_options.flag.left){
                                cssStyle.left = _options.flag.left+"px";
                            }
                        }else{
                            cssStyle = {"left": (width-$watermarkDiv[0].offsetWidth)/2+"px","top": (height-$watermarkDiv[0].offsetHeight)/2+"px"};
                        }
                        $watermarkDiv.css(cssStyle);
                    }
                }
            }
            if(_settings.dom=="iframe"){
                var iframes = Array.prototype.slice.call(document.getElementsByTagName(_settings.dom),0)
                for(var i in iframes){
//					if(iframes[i].attachEvent){
//				        iframes[i].attachEvent('onload', function(){
//				        	fWaterMark($.extend({},_settings, {dom:"table"}), iframes[i].contentWindow.document);
//				        	fWaterMark($.extend({},_settings, {dom:"svg"}), iframes[i].contentWindow.document);
//				        });
//				    }else{
//				        iframes[i].onload = function(){
//                            fWaterMark($.extend({},_settings, {dom:"table"}), iframes[i].contentWindow.document);
//                            fWaterMark($.extend({},_settings, {dom:"svg"}), iframes[i].contentWindow.document);
//				        };
//				    }
                    $(iframes[i]).load(function(){
                        fWaterMark($.extend({},_settings, {dom:"table"}), this.contentWindow.document);
                        fWaterMark($.extend({},_settings, {dom:"svg"}), this.contentWindow.document);
                    });
                }

            }else{
                fWaterMark(_settings, document);
            }
        },
        /**
         * 格式化为千分位
         * @param _number
         */
        jAddComma: function(_number) {
            var num = _number + "";
            num = num.replace(new RegExp(",","g"),"");
            // 正负号处理
            var symble = "";
            if(/^([-+]).*$/.test(num)) {
                symble = num.replace(/^([-+]).*$/,"$1");
                num = num.replace(/^([-+])(.*)$/,"$2");
            }

            if(/^[0-9]+(\.[0-9]+)?$/.test(num)) {
                var num = num.replace(new RegExp("^[0]+","g"),"");
                if(/^\./.test(num)) {
                    num = "0" + num;
                }

                var decimal = num.replace(/^[0-9]+(\.[0-9]+)?$/,"$1");
                var integer= num.replace(/^([0-9]+)(\.[0-9]+)?$/,"$1");

                var re=/(\d+)(\d{3})/;

                while(re.test(integer)){
                    integer = integer.replace(re,"$1,$2");
                }
                return symble + integer + decimal;

            } else {
                return number;
            }
        },
        /**
         * 文本域操作光标位置
         * @returns {{add: add, set: set, get: get}}
         */
        jCursorPos: function(){
            var get = function (textarea) {
                var rangeData = {text: "", start: 0, end: 0 };

                if (textarea.setSelectionRange) { // W3C
                    textarea.focus();
                    rangeData.start= textarea.selectionStart;
                    rangeData.end = textarea.selectionEnd;
                    rangeData.text = (rangeData.start != rangeData.end) ? textarea.value.substring(rangeData.start, rangeData.end): "";
                } else if (document.selection) { // IE
                    textarea.focus();
                    var i,
                        oS = document.selection.createRange(),
                        oR = document.body.createTextRange();
                    oR.moveToElementText(textarea);

                    rangeData.text = oS.text;
                    rangeData.bookmark = oS.getBookmark();

                    for (i = 0; oR.compareEndPoints('StartToStart', oS) < 0 && oS.moveStart("character", -1) !== 0; i ++) {
                        if (textarea.value.charAt(i) == '\r' ) {
                            i ++;
                        }
                    }
                    rangeData.start = i;
                    rangeData.end = rangeData.text.length + rangeData.start;
                }

                return rangeData;
            },

            set = function (textarea, rangeData) {
                var oR;
                if(!rangeData) {
                    alert("You must get cursor position first.")
                }
                textarea.focus();
                if (textarea.setSelectionRange) { // W3C
                    textarea.setSelectionRange(rangeData.start, rangeData.end);
                } else if (textarea.createTextRange) { // IE
                    oR = textarea.createTextRange();

// Fixbug : ues moveToBookmark()
// In IE, if cursor position at the end of textarea, the set function don't work
                    if(textarea.value.length === rangeData.start) {
//alert('hello')
                        oR.collapse(false);
                        oR.select();
                    } else {
                        oR.moveToBookmark(rangeData.bookmark);
                        oR.select();
                    }
                }
            },

            add = function (textarea, rangeData, text) {
                var oValue, nValue, sR, nStart, nEnd, st;
                this.set(textarea, rangeData);

                if (textarea.setSelectionRange) { // W3C
                    oValue = textarea.value;
                    nValue = oValue.substring(0, rangeData.start) + text + oValue.substring(rangeData.end);
                    nStart = nEnd = rangeData.start + text.length;
                    st = textarea.scrollTop;
                    textarea.value = nValue;
                    if(textarea.scrollTop != st) {
                        textarea.scrollTop = st;
                    }
                    textarea.setSelectionRange(nStart, nEnd);
                } else if (textarea.createTextRange) { // IE
                    sR = document.selection.createRange();
                    sR.text = text;
                    sR.setEndPoint('StartToEnd', sR);
                    sR.select();
                }
            };

            return {
                add: add,
                set: set,
                get: get
            }
        }
    });
    $.extend({
        browser: {
            msie: !!$.jBrowser().safari,
            webkit: !!$.jBrowser().webkit,
            mozilla: !!$.jBrowser().mozilla,
            safari: !!$.jBrowser().safari,
            opera: !!$.jBrowser().opera
        },
        jIsIE: function(_ver){
            if(_ver){
                return navigator.appName=="Microsoft Internet Explorer"&&navigator.appVersion.indexOf("MSIE "+_ver+".0")>=0;
            }else{
                return navigator.appName=="Microsoft Internet Explorer";
            }
        },
        jIsSA: function(){
            return $.browser.safari?true:false;
        },
        jIsFF: function(){
            return $.browser.mozilla?true:false;
        },
        jIsOP: function(){
            return $.browser.opera?true:false;
        }
    });
})(jQuery);





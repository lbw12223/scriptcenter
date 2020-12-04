$.extend({
    currentIndex: 0,
    initFlag: false,
    totalStep: 0,
    steps: [],
    params: {},
    initEvent: function () {
        if (!this.initFlag) {
            var _this = this;
            $("#guide-next").click(function () {
                _this.currentIndex = _this.currentIndex + 1;
                _this.guideNext();
            })
            $("#guide-pre").click(function () {
                _this.currentIndex = _this.currentIndex - 1;
                _this.guideNext();
            })
            $("#guide-ikown").click(function () {
                _this.params.kown && _this.params.kown();
            })
            this.initFlag = true;
        }
    },
    guide: function (params, index) {
        this.params = params;
        this.steps = params.steps;
        this.totalStep = params.steps.length;
        this.currentIndex = index;
        this.initEvent();
        this.guideNext();
    },
    guideNext: function () {
        var guide = this.steps[this.currentIndex - 1];
        if (guide) {
            this.showGuide(guide);
        } else {
            $("#guideBackground").hide();
            $("#guideBody").hide();
        }
    },
    showPreNext: function () {
        if (this.currentIndex == 1) {
            $("#guide-pre").css("display", "none");
            $("#guide-ikown").css("display", "none");
            $("#showCfLink").css("display","none");
        } else if (this.currentIndex == this.totalStep) {
            $("#guide-pre").css("display", "inline-block");
            $("#guide-ikown").css("display", "inline-block");
            $("#guide-next").css("display", "none");
            $("#showCfLink").css("display","inline-block");
        } else {
            $("#guide-ikown").css("display", "none");
            $("#guide-pre").css("display", "inline-block");
            $("#guide-next").css("display", "inline-block");
            $("#showCfLink").css("display","none");
        }
    },
    showGuide: function (guide) {
        guide && guide.beforeShowEvent && guide.beforeShowEvent(guide);
        this.showPreNext();
        var guideBody = $("#guideBody");
        $("#guideBackground").show();

        $(".guide-selected").removeClass("guide-selected");
        var node = $(guide.masterDoms);
        for (var index = 0; index < guide.doms.split(",").length; index++) {
            var dom = guide.doms.split(",")[index];
            var $dom = $(dom);
            $dom.addClass("guide-selected");
        }

        var nodeLocation = node.position();
        var nodeWidth = node.outerWidth();
        var nodeHeight = node.outerHeight();

        $("#guide-title-content", guideBody).html("").html(guide.guide_title);
        if (guide.stepIndex == 1) {
            $("#guideBody").addClass("firstGuideBody");
            $(".guide-title").removeClass("guide-title").addClass("firstGuideBodyTitle");
            $(".leftFlag").css("display", "none");
        } else {
            $("#guideBody").removeClass("firstGuideBody");
            $(".firstGuideBodyTitle").removeClass("firstGuideBodyTitle").addClass("guide-title");
            $(".leftFlag").css("display", "block");

        }
        var guideDescriptionContent = $("#guide-description-content", guideBody);
        guideDescriptionContent.html("");
        for (var index = 0; index < guide.description.length; index++) {
            var value = guide.description[index];
            if ($.trim(value).length > 0) {
                var div = $("<p/>");
                div.addClass("guide-description-content").html(value);
                guideDescriptionContent.append(div);
            }
        }
        guideBody.show();

        var guideBodyWidth = $("#guideBody").outerWidth();
        var guideBodyHeight = $("#guideBody").outerHeight();

        //设置浏览器Y滚动条位置
        var windowHeight = window.screen.height;
        var windowWidth = window.screen.width;

        var nodeBottom = $(document).scrollTop() + nodeLocation.top * 1 + nodeHeight;

        if (nodeBottom > windowHeight) {
            $(document).scrollTop(nodeBottom - windowHeight);
        }
        if (guide.position === "right") {
            var caculateBodyLeft = (nodeLocation.left + nodeWidth + 10);
            var caculateBodyTop = (nodeLocation.top + (nodeHeight - guideBodyHeight) / 2 );
            guideBody.css("left", caculateBodyLeft + "px").css("top", caculateBodyTop + "px");
        } else if (guide.position === "left") {
            var caculateBodyLeft = (nodeLocation.left - guideBodyWidth - 10);
            var caculateBodyTop = (nodeLocation.top + (nodeHeight - guideBodyHeight) / 2);
            guideBody.css("left", caculateBodyLeft + "px").css("top", caculateBodyTop + "px");
        } else if (guide.position === "bottom") {
            var caculateBodyLeft = (nodeLocation.left + ( nodeWidth - guideBodyWidth ) / 2 );
            var caculateBodyTop = (nodeLocation.top + nodeHeight + 10   );
            guideBody.css("left", caculateBodyLeft + "px").css("top", caculateBodyTop + "px");
        } else if (guide.position === "top") {
            var caculateBodyLeft = (nodeLocation.left + ( nodeWidth - guideBodyWidth ) / 2 );
            var caculateBodyTop = (nodeLocation.top - guideBodyHeight - 10  );
            guideBody.css("left", caculateBodyLeft + "px").css("top", caculateBodyTop + "px");
        } else if (guide.position === "windowcenter") {

            guideBody.css("left", (windowWidth - guideBodyWidth) / 2 + "px").css("top", 50 + "px");
        } else {
            guideBody.css("left", guide.position.left).css("top", guide.position.top);
        }
        this.params.eachStepEvent && this.params.eachStepEvent(guide);
        guide && guide.afterShowEvent && guide.afterShowEvent(guide);
    }
});



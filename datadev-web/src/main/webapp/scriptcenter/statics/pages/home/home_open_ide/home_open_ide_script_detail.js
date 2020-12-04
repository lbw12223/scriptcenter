
var selectRows = [], dirScope;

function initGrace() {
    var grace = angular.module('grace.bootstrap.demo', ['grace.bootstrap'], angular.injector(['grace.bootstrap.bootConfigs']).get('bootConfig'));
    grace.controller('GridCommonCtrl', ["$scope", "$http", "$gDataAdapterFactory", "$gDataAdapterModelManagerFactory", function ($scope, $http, dataAdapter, ModelManager) {
        function generateData() {
            var data = {
                //1.定义table列属性、及表格展示相关配置
                columnsDef: [
                    {field: "args", name: "ID"},
                    {field: "args", name: "名称",columnType: 'html',width:100},
                    {field: "accountCode", name: "大小"},
                    {field: "clusterCode", name: "上传人"},
                    {field: "created", name: "上传时间"}
                ]
            };
            data = $.extend({}, _pageDataSimpleConfig, data);
            //2.grid数据请求配置
            data.ajaxConfig = ajaxEvent.getListPage();
            return data;
        }

        $scope.gridOptions = generateData();
        $scope.gridOptions.onReady = function gridReady(api) {
            $scope.gridApi = api;
            api.event.on("rowSelect", function () {
                selectRows = arguments[3];
                //计算上面的按钮是否可用 , 添加修改删除
                caculateBtnsDisable(selectRows);
            });
            api.event.on("rowUnSelect", function () {
                selectRows = arguments[3];
                //计算上面的按钮是否可用 , 添加修改删除
                caculateBtnsDisable(selectRows)
            });
            api.event.on('rowSelectAll', rowSelectAll);
            api.event.on('rowUnSelectAll', rowUnSelectAll);
        }
        dirScope = $scope;
    }]);
    angular.bootstrap($("#GridCommonCtrl"), ['grace.bootstrap.demo']);
}
function caculateBtnsDisable(selectRows) {
    selectRows = fixSelectRows(selectRows);
    deleteBtn(selectRows);
}
/**
 * 删除按钮
 * @param selectRows
 */
function deleteBtn(selectRows) {
    var deleteBtn = $("#deleteBtn");
    var available = $("#available").val() * 1 ;
    if(available == 1 && selectRows.length > 0){
        deleteBtn.removeClass("bdp-disabled");
    }else{
        deleteBtn.addClass("bdp-disabled");
    }
}
function rowUnSelectAll(evt, selectedIndex, selectedData) {
    selectRows = [];
    caculateBtnsDisable(selectRows);
}

function rowSelectAll(evt, selectedIndex, selectedData) {
    selectRows = selectedData;
    caculateBtnsDisable(selectRows);
}
var ajaxEvent = {
    getListPage: function () {
        var  dirId = $("#dirId").val();
        var obj = {
            url: "/scriptcenter/test/list.ajax",
            method: 'post',
            dataType: 'json',
            resultParser: function (res) {
                return res;
            },
            externalData: function () {
                return {
                    fileName: $.trim($("#queryFileName").val()),
                    dirId:dirId
                }
            },
            resultParser: function (res) {
                selectRows = [];
                caculateBtnsDisable(selectRows);
                return res;
            }
        };
        return obj;
    }
}
 initGrace();

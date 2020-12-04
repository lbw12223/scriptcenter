var _pageDataSimpleConfig = {
    //1.定义table列属性、及表格展示相关配置
    enablePagination: true,
    paginationConfig: {
        pageIcon: true,
        pageSize: 10,
        pageSizes: [10, 50, 100],
        enableRefresh: true,
    },
    //表格师是否启用排序
    enableSort: false,
    enablePinning: true,
    enableSelection: false,
    enableSelectionMulti: false,
    multiSelectionConfig: {
        checkboxSelect: false
    },
    expandableTable: false,
    enableColumnResize: true,
    rowHeight: 40,//行高度
    headerRowHeight: 40,//表头高度
    minColumnWidth: 90,//最小列宽度
    enableAutoResize: true,//是否自适应列宽度
    enableSelectAll: true,//是否支持选择所有
    useRemoteData: true,//是否支持远程后台数据
    enableScreen:true,
    //列筛选的相关配置
    screenConfig:{
        //筛选器的label文字
        screenLabel:"显示/隐藏:",
        //筛选器的类型，in-line：行内显示，drop-down：下拉显示
        type:"drop-down",   //drop-down
        //如果是下拉显示，下拉框的宽度
        dropDownWidth:300,
        //如果是下拉显示，下来框中显示的列数
        dropDownColumnCount:3
    },
    lockHeader: true,//是否固定Header表头
    lockHeaderType: "top",//固定Header表头属性；
};



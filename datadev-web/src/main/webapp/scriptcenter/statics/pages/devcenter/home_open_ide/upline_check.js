



jQuery(function (){


    var page  = {

        init  : function (){

            $('#gridTable').GM({
                gridManagerName: 'gridTable',
                supportAjaxPage: true,
                ajaxData: '/scriptcenter/diff/scriptTaskList.ajax',
                ajaxType: 'POST',
                supportDrag: false,
                supportCheckbox: false,
                supportAutoOrder: false,
                height: "100%",
                width: "100%",
                query: {
                    projectSpaceId : top.projectSpaceId,
                    scriptName:
                },
                supportMenu: false,
                columnData: [
                    {
                        key: 'id',
                        text: 'id',
                    },
                    {
                        key: 'jobName',
                        text: '名称',

                    }
                ]
            });
        }
    }

    page.init();

});

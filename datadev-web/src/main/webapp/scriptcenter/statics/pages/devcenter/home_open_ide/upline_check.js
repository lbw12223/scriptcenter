



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
                    scriptName:""
                },
                supportMenu: false,
                columnData: [
                    {
                        key: 'creator',
                        text: 'creator',
                    },
                    {
                        key: 'appGroupId',
                        text: 'appGroupId',

                    }
                ]
            });
        }
    }

    page.init();

});

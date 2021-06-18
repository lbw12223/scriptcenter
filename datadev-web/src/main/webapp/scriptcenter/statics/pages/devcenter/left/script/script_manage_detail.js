$(function () {
    initTableData(top.$.dialog.data("info"));

    $("#copyFunc").click(function () {
        var textarea = document.getElementById("expText");
        var text = textarea.value;
        const input = document.createElement('input');
        input.setAttribute('readonly', 'readonly');
        input.setAttribute('value', text);
        document.body.appendChild(input);
        input.focus();
        input.setSelectionRange(0, input.value.length);
        if (document.execCommand('copy')) {
            document.execCommand('copy');
        }
        document.body.removeChild(input);
        $.successMsg("已复制至剪贴板！");
    })
});

function initTableData(data) {
    $("#funcName").text(data.name);
    $("#funcTrName").text(data.name);
    $("#funcTrOwner").text(data.owner);
    $("#funcTrType").text(data.typeStr);
    $("#funcTrMender").text(data.mender);
    $("#funcTrModified").text(data.modifiedStr);
    $("#expText").val(data.format);
    $("#descText").val(data.comment);
}

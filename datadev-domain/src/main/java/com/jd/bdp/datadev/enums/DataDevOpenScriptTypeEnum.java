package com.jd.bdp.datadev.enums;

public enum DataDevOpenScriptTypeEnum {

    Normal(0, " 只打开主页"),
    BuffaloImport(1, "只传调度脚本id，没有传开发平台项目与脚本 -> 调度导入"),
    ReBuild(2,"项目存在，但是开发平台脚本被删除 -> 重建脚本"),
    UserNoAuth(3,"项目存在，但是当前用户无权限 ->重新添加权限"),
    IdeNoAuth(4,"bdp_ide虚拟账号已经拉不到该项目 ->重新添加权限或者重建项目重建脚本"),
    OpenScript(5,"项目与脚本都存在，并且有项目权限 -> 正常打开"),
    MergeScript(6,"从调度过来打开，并且不是开发平台最新版本 -> merge弹窗");


    private Integer code;
    private String desc;

    DataDevOpenScriptTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer toCode() {
        return this.code;
    }

    public String toDesc() {
        return this.desc;
    }


}

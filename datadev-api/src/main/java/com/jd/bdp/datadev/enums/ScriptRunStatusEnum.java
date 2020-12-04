package com.jd.bdp.datadev.enums;

public enum ScriptRunStatusEnum {
    Success(0,"脚本执行成功"),
    Delay(1,"脚本待执行"),
    Running(2,"脚本执行中"),
    Error(3,"脚本执行异常"),
    Stop(4,"手动停止");
    private Integer code;//运行状态吗
    private String desc;//运行描述
    ScriptRunStatusEnum(Integer code, String desc){
        this.code=code;
        this.desc=desc;
    }
    public Integer toCode() {
        return code;
    }
}

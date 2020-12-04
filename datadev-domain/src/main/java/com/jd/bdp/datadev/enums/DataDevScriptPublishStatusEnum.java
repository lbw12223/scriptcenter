package com.jd.bdp.datadev.enums;

public enum DataDevScriptPublishStatusEnum {
    Publishing(0, "发布中"),
    Auditing(1, "审核中"),
    Reject(2, "审批驳回"),
    Cancel(3, "审批撤回"),
    Success(4, "发布成功"),
    Failure(5, "发布失败"); //上传过程中失败
    private Integer code;
    private String desc;

    private DataDevScriptPublishStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer toCode() {
        return this.code;
    }
    public String toDesc(){
        return this.desc;
    }
    public static DataDevScriptPublishStatusEnum enumValueOf(Integer code) {
        if (code != null) {
            DataDevScriptPublishStatusEnum[] enums = DataDevScriptPublishStatusEnum.values();
            for (DataDevScriptPublishStatusEnum statusEnum : enums) {
                if (statusEnum.toCode().equals(code)) {
                    return statusEnum;
                }
            }
        }
        return null;
    }
}

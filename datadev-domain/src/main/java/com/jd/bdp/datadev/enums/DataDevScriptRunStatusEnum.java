package com.jd.bdp.datadev.enums;

import com.jd.bdp.datadev.domain.DataDevScriptRunDetail;

public enum DataDevScriptRunStatusEnum {
    Success(0, "成功"),
    Delay(1, "待执行"),
    Running(2, "执行中"),
    Error(3, "失败"),
    Stop(4, "强制终止");
    private Integer code;//运行状态吗
    private String desc;//运行描述

    DataDevScriptRunStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer toCode() {
        return code;
    }

    public String toDesc() {
        return this.desc;
    }

    public static DataDevScriptRunStatusEnum enumValueOf(Integer code) {
        DataDevScriptRunStatusEnum[] enums = DataDevScriptRunStatusEnum.values();
        for (DataDevScriptRunStatusEnum statusEnum : enums) {
            if (statusEnum.toCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static boolean isRunDetailFinish(DataDevScriptRunDetail scriptRunDetail) {
        int status = scriptRunDetail.getStatus();
        if (status == Success.code || status == Error.code || status == Stop.code) {
            return true;
        }
        return false;
    }
}

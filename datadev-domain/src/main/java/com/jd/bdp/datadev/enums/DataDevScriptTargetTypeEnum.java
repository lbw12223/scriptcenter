package com.jd.bdp.datadev.enums;

/**
 * plumber目标数据库类型
 */
public enum DataDevScriptTargetTypeEnum {
    Mysql(1,"mysql","mysql"),
    Phoenix(2,"phoenix","phoenix"),
    Elasticsearch(3,"elasticsearch","elasticsearch");
    private Integer code;
    private String name;
    private String desc;
    DataDevScriptTargetTypeEnum(Integer code, String name, String desc){
        this.code   =   code;
        this.name   =   name;
        this.desc   =   desc;
    }
    public static DataDevScriptTargetTypeEnum valuesOf(String name){
        DataDevScriptTargetTypeEnum[] enums=values();
        for(DataDevScriptTargetTypeEnum targetTypeEnum:enums){
            if(targetTypeEnum.toName().equals(name)){
                return targetTypeEnum;
            }
        }
        return null;
    }


    public Integer toCode() {
        return code;
    }
    public String toName(){
        return name;
    }
}

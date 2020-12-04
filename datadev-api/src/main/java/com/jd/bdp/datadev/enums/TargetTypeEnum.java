package com.jd.bdp.datadev.enums;

/**
 * plumber目标数据库类型
 */
public enum TargetTypeEnum {
    Mysql(1,"mysql","mysql"),
    Phoenix(2,"phoenix","phoenix"),
    Elasticsearch(3,"elasticsearch","elasticsearch");
    private Integer code;
    private String name;
    private String desc;
    TargetTypeEnum(Integer code,String name,String desc){
        this.code   =   code;
        this.name   =   name;
        this.desc   =   desc;
    }

    public String toName(){
        return name;
    }
}

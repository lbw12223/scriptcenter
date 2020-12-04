package com.jd.bdp.datadev.enums;

import org.apache.commons.lang.StringUtils;

public enum DataDevScriptGitStatusEnum {
    MOD("MOD","修改文件"),
    ADD("ADD","增加文件"),
    DEL("DEL","git上已删除"),
    NON("NON","无变化");
    private String code;
    private String desc;
    DataDevScriptGitStatusEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }
    public String toCode(){
        return this.code;
    }

    public static DataDevScriptGitStatusEnum getGitStatus(String gitVersion,String lastGitVersion,String md5,String lastGitVersionMd5,Integer deleted){
        DataDevScriptGitStatusEnum gitStatus = DataDevScriptGitStatusEnum.NON;
        try{
            if(StringUtils.isBlank(gitVersion)){
                gitStatus = DataDevScriptGitStatusEnum.ADD;
            }else if(deleted!=null && deleted == 1){
                gitStatus = DataDevScriptGitStatusEnum.DEL;
            }else if(!gitVersion.equals(lastGitVersion) || (StringUtils.isNotBlank(md5) && StringUtils.isNotBlank(lastGitVersionMd5) && !md5.equals(lastGitVersionMd5) )){
                gitStatus = DataDevScriptGitStatusEnum.MOD;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return gitStatus;
    }
    public static boolean canPush(DataDevScriptGitStatusEnum target){
        return target==null || target.code.equals(DataDevScriptGitStatusEnum.ADD.toCode()) || target.code.equals(DataDevScriptGitStatusEnum.DEL.toCode()) || target.code.equals(DataDevScriptGitStatusEnum.MOD.toCode());
    }

}

package com.jd.bdp.datadev.model;

import com.jd.bdp.datadev.enums.ScriptJdqMessageEnum;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/3/10.
 * <p>
 * storm jdq 消息解析过后的Model
 */
public class ScriptJdqMessage implements Serializable {

    private Integer messageType; //消息类型

    private Long scriptRunDetailId;

    private Integer type;   //脚本类型

    private String value;

    private String messageIndex;

    private String hbaseKey;

    private Long gitProjectId ;

    private String gitProjectFilePath;


    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Long getScriptRunDetailId() {
        return scriptRunDetailId;
    }

    public void setScriptRunDetailId(Long scriptRunDetailId) {
        this.scriptRunDetailId = scriptRunDetailId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMessageIndex() {
        return messageIndex;
    }

    public void setMessageIndex(String messageIndex) {
        this.messageIndex = messageIndex;
    }

    public String getHbaseKey() {
        return hbaseKey;
    }

    public void setHbaseKey(String hbaseKey) {
        this.hbaseKey = hbaseKey;
    }

    @Override
    public String toString() {
        return "DataDevJdqMessage{" +
                "messageType=" + messageType +
                ", scriptRunDetailId=" + scriptRunDetailId +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", messageIndex='" + messageIndex + '\'' +
                ", hbaseKey='" + hbaseKey + '\'' +
                '}';
    }

    public Long getGitProjectId() {
        return gitProjectId;
    }

    public void setGitProjectId(Long gitProjectId) {
        this.gitProjectId = gitProjectId;
    }

    public String getGitProjectFilePath() {
        return gitProjectFilePath;
    }

    public void setGitProjectFilePath(String gitProjectFilePath) {
        this.gitProjectFilePath = gitProjectFilePath;
    }
}

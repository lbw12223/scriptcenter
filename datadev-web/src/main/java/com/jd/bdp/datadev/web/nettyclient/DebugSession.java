package com.jd.bdp.datadev.web.nettyclient;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.netty.common.DebugMessage;
import com.jd.bdp.datadev.netty.common.NettyMessage;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Created by zhangrui25 on 2018/11/13.
 */
public class DebugSession {

    private static final Logger logger = Logger.getLogger(DebugSession.class);

    private String debugSessionId;
    private WebSocketSession webSocketSession;
    private DebugClient debugClient;
    private Long runDetailId;
    private Integer pythonVersion;


    public DebugSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
        this.debugSessionId = webSocketSession.getId();
    }


    public void setDebugClient(DebugClient debugClient) {
        System.out.println("=============sen debugClient");
        this.debugClient = debugClient;
    }


    /**
     * 发送指令 将本地的信息发送给netty
     * <p>
     * 解析debugSessionCmd
     *
     * @param debugMessage
     */
    public void sendDebugCmd(DebugMessage debugMessage) throws Exception {
        debugClient.sendMsg(new NettyMessage(debugMessage));
    }


    /**
     * flushWebServerDebugMessage
     *
     * @param message
     * @throws Exception
     */
    public void flushPageDebugErrorResult(String message) throws Exception {
        DebugMessage debugMessage = new DebugMessage();
        debugMessage.setMessage(message != null ? message : "请求失败！");
        debugMessage.setCode(1);
        debugMessage.setSuccess(false);
        debugMessage.setRunDetailId(this.getRunDetailId());
        flushPageDebugResult(debugMessage);
    }

    /**
     * websocket 给浏览器发送消息
     *
     * @param debugMessage
     * @throws IOException
     */
    public void flushPageDebugResult(DebugMessage debugMessage) throws IOException {
        debugMessage.setRunDetailId(this.getRunDetailId());
        flushPageDebugResult(JSONObject.toJSONString(debugMessage));
    }

    /**
     * websocket 给浏览器发送消息
     *
     * @param msg
     * @throws IOException
     */
    private void flushPageDebugResult(String msg) throws IOException {
        webSocketSession.sendMessage(new TextMessage(msg));
    }

    /**
     * releaseSession
     *
     * @throws Exception
     */
    public void releaseSession() throws Exception {
        DebugSessionManager.removeSession(webSocketSession);
        if (debugClient != null && debugClient.isActive()) {
            debugClient.shutDown();
        }
        webSocketSession.close();
    }


    public String getChannelId() {
        if(debugClient == null){
            return null ;
        }
        return debugClient.getChannelId();
    }

    public Integer getPythonVersion() {
        return pythonVersion;
    }

    public void setPythonVersion(Integer pythonVersion) {
        this.pythonVersion = pythonVersion;
    }

    public String getDebugSessionId() {
        return debugSessionId;
    }

    public void setDebugSessionId(String debugSessionId) {
        this.debugSessionId = debugSessionId;
    }

    public Long getRunDetailId() {
        return runDetailId;
    }

    public void setRunDetailId(Long runDetailId) {
        this.runDetailId = runDetailId;
    }

}

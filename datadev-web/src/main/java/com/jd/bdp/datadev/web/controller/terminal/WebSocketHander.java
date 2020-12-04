package com.jd.bdp.datadev.web.controller.terminal;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

/**
 * Created by zhangrui25 on 2018/10/27.
 */
public class WebSocketHander extends TextWebSocketHandler {

    public static Map<String, JSchClient> sshClients = Collections.synchronizedMap(new HashMap<String, JSchClient>());


    /**
     * 建立连接时候处理
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        JSchClient jSchClient = new JSchClient("192.168.144.103", 22, "test", "root", "Bdp_prd!", session);
        new Thread(new JshClientWriteThread(session, jSchClient)).start();
        sshClients.put(session.getId(), jSchClient);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String cmd = message.getPayload();
        sshClients.get(session.getId()).write(cmd.getBytes());
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sshClients.get(session.getId()).close();
        sshClients.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }
}

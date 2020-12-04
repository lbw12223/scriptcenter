package com.jd.bdp.datadev.web.controller.terminal;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.InputStream;

/**
 * Created by zhangrui25 on 2018/10/29.
 */
public class JshClientWriteThread implements Runnable {


    WebSocketSession webSocketSession;
    JSchClient jSchClient;
    InputStream inputStream;


    public JshClientWriteThread(WebSocketSession webSocketSession, JSchClient jSchClient) {
        this.webSocketSession = webSocketSession;
        this.jSchClient = jSchClient;
        inputStream = jSchClient.inputStream;
    }

    @Override
    public void run() {
        jSchClient.read();
    }



}

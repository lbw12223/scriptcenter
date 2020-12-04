package com.jd.bdp.datadev.web.controller.terminal;


import com.jcraft.jsch.*;
import com.jd.bdp.datadev.web.jsch.Shell;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhangrui25 on 2018/10/29.
 */
public class JSchClient {


    public String host;
    public Integer port;
    private String clientId;
    public String loginUser;
    public String password;
    public InputStream inputStream;
    public OutputStream outputStream;
    public Channel channel;
    public WebSocketSession webSocketSession;

    public JSchClient(String host, Integer port, String clientId, String loginUser, String password, WebSocketSession webSocketSession) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.loginUser = loginUser;
        this.password = password;
        this.webSocketSession = webSocketSession;
        initSession();

    }

    public static void main(String[] args) throws Exception {
        JSchClient jSchClient = new JSchClient("192.168.144.103", 22, "test", "zhangrui", "zhangrui156", null);
        jSchClient.read();
    }

    private void initSession() {
        try {
            JSch jsch = new JSch();
            Session session =  jsch.getSession(loginUser, host, port);
            session.setPassword(password);
            UserInfo ui = new Shell.MyUserInfo() {
                public void showMessage(String message) {
                    try {
                        if (webSocketSession != null) {
                            webSocketSession.sendMessage(new TextMessage(message));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public boolean promptYesNo(String message) {
                    try {
                        if (webSocketSession != null) {
                            webSocketSession.sendMessage(new TextMessage(message));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };

            session.setUserInfo(ui);
            session.connect(30000);   // making a connection with timeout.

            channel = session.openChannel("shell");

            ChannelShell channelShell = ((ChannelShell) channel);
            channelShell.setPtySize(100, 100, 1000, 2000);
            inputStream = channel.getInputStream();
            outputStream = channel.getOutputStream();

            channel.connect(3 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 写入stream中
     *
     * @param bytes
     */
    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取流中的数据
     */
    public void read() {
        try {
            byte[] tmp = new byte[1024];
            while (true) {
                if (webSocketSession != null && !webSocketSession.isOpen()) {
                    System.out.println("closed....................................结束当前方法");
                    break;
                }
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024);
                    if (i < 0) break;
                    if (webSocketSession != null) {
                        webSocketSession.sendMessage(new TextMessage(new String(tmp, 0, i)));
                    } else {
                        System.out.println(new TextMessage(new String(tmp, 0, i)));
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (Exception ee) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    public void close() {
        try {
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            webSocketSession.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            channel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

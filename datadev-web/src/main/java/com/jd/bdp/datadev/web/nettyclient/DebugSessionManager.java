package com.jd.bdp.datadev.web.nettyclient;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangrui25 on 2018/11/13.
 */
public class DebugSessionManager {

    public static Map<String, DebugSession> sessions = Collections.synchronizedMap(new HashMap<String, DebugSession>());

    /**
     * 获取session
     *
     * @param webSocketSession
     * @return
     */
    public static DebugSession attachmentSession(WebSocketSession webSocketSession) {
        String sessionId = webSocketSession.getId();
        if (sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        } else {
            DebugSession debugSession = new DebugSession(webSocketSession);
            sessions.put(sessionId, debugSession);
            return debugSession;
        }
    }

    public static void removeSession(WebSocketSession webSocketSession) {
        String sessionId = webSocketSession.getId();
        sessions.remove(sessionId);
    }

    /**
     * 通过ChannelId获取DebugSession
     *
     * @param
     * @return
     */
    public static DebugSession getDebugSession(ChannelHandlerContext channelHandlerContext) {
        for (String key : sessions.keySet()) {
            DebugSession session = sessions.get(key);
            if (session.getChannelId() != null && session.getChannelId().equalsIgnoreCase(channelHandlerContext.channel().toString())) {
                return session;
            }
        }
        return null;
    }

}

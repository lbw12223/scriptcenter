package com.jd.bdp.datadev.web.websocket;

import com.jd.bdp.datadev.web.controller.terminal.WebSocketHander;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by zhangrui25 on 2018/10/27.
 */
@Configuration
@EnableWebSocket
public class WebSocketDebugAdapter extends WebMvcConfigurerAdapter implements WebSocketConfigurer {


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(new WebSocketRunDeBugHander(), "/scriptcenter/runDebug");
    }

}

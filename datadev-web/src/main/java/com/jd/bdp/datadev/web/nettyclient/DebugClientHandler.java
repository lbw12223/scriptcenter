package com.jd.bdp.datadev.web.nettyclient;


import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.model.ScriptJdqMessage;
import com.jd.bdp.datadev.netty.common.DebugCmd;
import com.jd.bdp.datadev.netty.common.DebugMessage;
import com.jd.bdp.datadev.netty.common.NettyMessage;
import com.jd.bdp.datadev.util.ScriptJdqMessageUtil;
import com.jd.bdp.datadev.web.controller.script.ScriptApiController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by zhangrui25 on 2018/11/5.
 */
public class DebugClientHandler extends SimpleChannelInboundHandler<NettyMessage> {

    private static final Logger logger = Logger.getLogger(DebugClientHandler.class);


    /**
     * 连接上 Netty 服务
     * <p>
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.error("================= channelActive " + ctx );

    }

    /**
     * 关闭netty 连接 通知Web页面
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("================= channelInactive " + ctx );
        DebugSession debugSession = DebugSessionManager.getDebugSession(ctx);
        if(debugSession != null){
            debugSession.releaseSession();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessage nettyMessage) throws Exception {
        try{
            System.out.println("revive ; " + new String(nettyMessage.getDatas(), "utf-8"));
            DebugMessage debugMessage = JSONObject.parseObject(new String(nettyMessage.getDatas(), "utf-8"), DebugMessage.class);
            if (debugMessage.getDebugCmd().equals(DebugCmd.LogMsg)) {
                ScriptJdqMessage scriptJdqMessage = ScriptJdqMessageUtil.covertStringJdqMessage(debugMessage.getData().getString("logMsg"));
                debugMessage.getData().put("logMsg", scriptJdqMessage.getValue());
            }
            DebugSession debugSession = DebugSessionManager.getDebugSession(ctx);
            debugSession.flushPageDebugResult(debugMessage);
        }catch (Exception e){
            logger.error(e);
        }

    }

    /**
     * 处理异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        for (StackTraceElement e : cause.getStackTrace()) {
            System.out.println(e);
        }
        DebugSession debugSession = DebugSessionManager.getDebugSession(ctx);
        debugSession.flushPageDebugErrorResult(cause.getMessage());
    }
}


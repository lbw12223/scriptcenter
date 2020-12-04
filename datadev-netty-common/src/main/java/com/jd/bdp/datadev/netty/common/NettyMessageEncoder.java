package com.jd.bdp.datadev.netty.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * --header--length---data
 * Created by zhangrui25 on 2018/11/12.
 */
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {

    /**
     * @param channelHandlerContext
     * @param nettyMessage
     * @param byteBuf
     * @throws Exception
     */
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(nettyMessage.getHeader());
        byteBuf.writeInt(nettyMessage.getLength());
        byteBuf.writeBytes(nettyMessage.getDatas());

    }
}

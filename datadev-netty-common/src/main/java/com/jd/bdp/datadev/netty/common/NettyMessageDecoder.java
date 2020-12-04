package com.jd.bdp.datadev.netty.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * --header--length---data
 * Created by zhangrui25 on 2018/11/12.
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {

    private final static int HEAD_LENGTH = 4 + 4; //(int)header + (int)length  :  头部信息 4 + 4 总共8个字节

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < HEAD_LENGTH) {    //还未读取head长度信息
            return;
        }
        int beginReader;
        while (true) {
            // 获取包头开始的index
            beginReader = byteBuf.readerIndex();
            byteBuf.markReaderIndex();
            if (byteBuf.readInt() == NettyMessage.CONTANTS_HEADER) {
                break;
            }
            byteBuf.resetReaderIndex();
            byteBuf.readByte();
            if (byteBuf.readableBytes() < HEAD_LENGTH) {
                return;
            }
        }

        Integer dataLength = byteBuf.readInt(); //长度
        if (byteBuf.readableBytes() < dataLength) {
            //数据还未达到发送时候的长度
            byteBuf.readerIndex(beginReader);
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        NettyMessage nettyMessage = new NettyMessage(data);
        list.add(nettyMessage);
    }
}

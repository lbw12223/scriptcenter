package com.jd.bdp.datadev.web.nettyclient;

import com.alibaba.fastjson.JSONObject;
import com.jd.bdp.datadev.netty.common.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

/**
 * Created by zhangrui25 on 2018/11/7.
 */
public class DebugClient {

    private Bootstrap bootstrap = new Bootstrap();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    private String url;
    private Integer port;


    public String getChannelId() {
        return channel.toString();
    }

    public DebugClient(String url, Integer port) {
        this.url = url;
        this.port = port;
    }


    public void init() {

        bootstrap.group(workerGroup);

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new NettyMessageDecoder());
                socketChannel.pipeline().addLast(new NettyMessageEncoder());
                socketChannel.pipeline().addLast(new DebugClientHandler());
            }
        });
    }

    public void connect() throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(url, port).await();
        channel = channelFuture.channel();

    }

    /**
     * 发送指令
     *
     * @param nettyMessage
     */
    public void sendMsg(NettyMessage nettyMessage) {
        channel.writeAndFlush(nettyMessage);
    }

    /**
     * 是否active
     *
     * @return
     */
    public boolean isActive() {
        return channel.isActive();
    }

    public void shutDown() {
        channel.close();
        workerGroup.shutdownGracefully();
    }


 /*   public static void main(String[] args) throws Exception {
        DebugClient debugClient = new DebugClient("192.168.144.103", 45535);
        debugClient.init();
        debugClient.connect();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Send cmd : ");
            String cmd = scanner.next();
            DebugMessage debugMessage = new DebugMessage();
            if (cmd.equalsIgnoreCase("n")) {
                debugMessage.setDebugCmd(DebugCmd.StepNext);
            }

            NettyMessage nettyMessage = new NettyMessage(JSONObject.toJSONString(debugMessage).getBytes());
            debugClient.sendMsg(nettyMessage);
        }
    }*/
}

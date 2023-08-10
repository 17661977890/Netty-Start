package com.netty.base.eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * @author sunbin
 * @date 2023年08月09日 10:21:00
 * @description TODO
 */
public class EventLoopClient {

    public static void main(String[] args) throws InterruptedException {

        Channel channel = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost",8080))
                .sync()
                .channel();
        System.out.println(channel);
        // 这里打断点执行表达式发送数据
        System.out.println("");
    }
}

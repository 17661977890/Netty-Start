package com.netty.netty.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author sunbin
 * 使用回调方法
 */
@Slf4j
public class ChannelFutureClient2 {

    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                // 连接服务器，异步非阻塞，main发起调用，真正执行connect是nio线程
                .connect(new InetSocketAddress("localhost",8080));

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                // 这里执行回调方法的线程是nio线程
                Channel channel = channelFuture.channel();
                log.info("{}",channel);
                channel.writeAndFlush("hello");
            }
        });
    }
}

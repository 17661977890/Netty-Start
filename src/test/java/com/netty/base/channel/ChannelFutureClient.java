package com.netty.base.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author sunbin
 * 为什么需要调用sync
 */
@Slf4j
public class ChannelFutureClient {

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
        // 把这里的链式调用拆开来分析 为什么需要sync()
//                .sync()
//                .channel()
//                .writeAndFlush("hello world");

        // 1、当不调用sync时候，服务器是收不到数据的 ---- sync():阻塞当前线程，直到nio线程建立连接完毕
//        channelFuture.sync();
        // 2、因为上面无阻塞向下执行，此时channel是获取不到的,所以客户端无法写数据。
        Channel channel = channelFuture.channel();
        log.info("{}",channel);
        channel.writeAndFlush("hello");
    }
}

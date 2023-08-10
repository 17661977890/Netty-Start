package com.netty.base.hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author sunbin
 * @date 2023年08月09日 10:17:00
 * @description TODO
 */
public class HelloServer2 {

    public static void main(String[] args) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                                nioSocketChannel.pipeline().addLast(new StringEncoder());
                                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                        System.out.println(o);
                                    }

                                });
                            }
                        }
                )
                .bind(8080);

    }
}

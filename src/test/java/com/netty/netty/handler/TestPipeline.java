package com.netty.netty.handler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sunbin
 * @date 2023年08月09日 15:59:00
 * @description TODO
 */
@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        // 入栈处理器 h1-->h2-->h3 (每个处理器可以对数据进行干预处理，下一个处理器就是拿到上一个处理后的数据)
                        pipeline.addLast("h1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("1");
                                super.channelRead(ctx, msg);// 等于执行ctx.fireChannelRead(msg);把数据传递到下一个处理器
                            }
                        });
                        pipeline.addLast("h2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("2");
                                super.channelRead(ctx, msg);
                            }
                        });
                        pipeline.addLast("h3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("3");
//                                super.channelRead(ctx, msg);// 这里没必要执行力，后面没有入栈处理器了
                                // 向客户端写入数据 触发出栈处理器（channel是从尾部找出栈处理器）
                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("ssss".getBytes()));
                                // ctx写入不会触发出栈处理器，ctx向前找出栈处理器（即h3之前的）
//                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("ssss".getBytes()));
                            }
                        });
                        // 出栈
                        pipeline.addLast("H4",new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
                                log.info("4");
                                super.write(channelHandlerContext,o,channelPromise);
                            }
                        });
                        pipeline.addLast("H5",new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
                                log.info("5");
                                super.write(channelHandlerContext,o,channelPromise);
                            }
                        });
                        pipeline.addLast("H6",new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
                                log.info("6");
                                super.write(channelHandlerContext,o,channelPromise);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}

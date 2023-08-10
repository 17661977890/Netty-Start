package com.netty.base.eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @author sunbin
 * @date 2023年08月09日 10:44:00
 * @description TODO
 */
@Slf4j
public class EventLoopServer {

    public static void main(String[] args) {
        // v3：
        // worker某个线程管理channel很多，如果handler耗时很长，会很影响效率
        // 创建一个独立的eventLoop,专门处理耗时较长的handler任务
        EventLoopGroup group = new DefaultEventLoopGroup();
        new ServerBootstrap()
                // v1:
//                .group(new NioEventLoopGroup())

                // v2:
                // boss 和 worker
                // boss 只负责ServerSocketChannel 上 accept事件， worker只负责socketChannel 上的读写
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handler1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                log.info(byteBuf.toString(Charset.defaultCharset()));
                                // 自定义handler：将消息传递给下一个handler，需要调用下面的方法
                                ctx.fireChannelRead(msg);
                            }
                        });
                        // v3: 这里handler绑定专门的group，专门处理耗时任务
                        nioSocketChannel.pipeline().addLast(group,"handler2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                log.info(byteBuf.toString(Charset.defaultCharset()));
                            }
                        });

                    }
                })
                .bind(8080);
    }
}

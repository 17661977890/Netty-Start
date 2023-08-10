package com.netty.highlevel.linebase;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class LineBaseClient {

    static final Logger log = LoggerFactory.getLogger(LineBaseClient.class);


    public static void main(String[] args) {
        send();
    }

    static void send(){
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        // channel连接建立好之后 出发 channelActive() 时间
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("sending...");
                            final ByteBuf buf = ctx.alloc().buffer();

                            final Random r = new Random();
                            char c = 'a';

                            for (int i = 0; i < 5; i++) {
                                final StringBuilder tmpStr = getStr(c, r.nextInt(256) + 1); // 随机生成 1-257 长度的 字符串
                                c++;
                                buf.writeBytes(tmpStr.toString().getBytes());
                            }

                            ctx.writeAndFlush(buf);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

    public static StringBuilder getStr(char c, int len){
        final StringBuilder s = new StringBuilder(len + 2);
        for (int i = 0; i < len; i++) {
            s.append(c);
        }
        s.append('\n');
        return s;
    }


}

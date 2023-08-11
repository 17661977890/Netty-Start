package com.netty.highlevel.protocol;


import io.netty.handler.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;

/**
 ### 以redis协议 为例                        【首先phpStudy 开启redis】
 <b>命令</b>    ：set key value
 <b>命令内容</b> ：set name zhangsan
 首先 把整个看成一个数组
<content>
 协议内容：
     ```
     *3   数组个数
     $3   key命令长度
     set
     $4   key值 内容长度
     name
     $8   value值 内容长度
     zhangsan
     ```
 </content>

 */
public class TestRedis {
    /*
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
     */
    static final Logger log = LoggerFactory.getLogger(TestRedis.class);

    public static void main(String[] args) {

        // 定义 回车换行
//        final byte[] LINE = {13,10};
        final byte[] LINE = {'\r', '\n'};

        final NioEventLoopGroup worker = new NioEventLoopGroup();
        try{
            final Bootstrap bs = new Bootstrap();
            bs.channel(NioSocketChannel.class);
            bs.group(worker);
            bs.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                        // 连接建立 运行 ，模拟redis协议 发送数据
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 分配 ByteBuf
                            final ByteBuf buf = ctx.alloc().buffer();
                            buf.writeBytes("*3".getBytes());buf.writeBytes(LINE);
                            buf.writeBytes("$3".getBytes());buf.writeBytes(LINE);
                            buf.writeBytes("set".getBytes());buf.writeBytes(LINE);
                            buf.writeBytes("$4".getBytes());buf.writeBytes(LINE);
                            buf.writeBytes("name".getBytes());buf.writeBytes(LINE);
                            buf.writeBytes("$8".getBytes());buf.writeBytes(LINE);
                            buf.writeBytes("zhanglisi".getBytes());buf.writeBytes(LINE);

                            ctx.writeAndFlush(buf);
                        }

                        // read事件 接受redis的返回结果
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf buf = (ByteBuf) msg;
                            final String s = buf.toString(Charset.defaultCharset());
                            System.out.println("... ... redis返回结果 = " + s);
                        }
                    });
                }
            });

            final ChannelFuture channelFuture = bs.connect("localhost", 6379).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e)
        {
            log.error("Client error", e);
        } finally {
            worker.shutdownGracefully();
        }

    }
}

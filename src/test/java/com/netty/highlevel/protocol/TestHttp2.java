package com.netty.highlevel.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;

/**
 * v2
 * 测试 netty提供的 Http协议接口
 */
public class TestHttp2 {
    /*
     import org.slf4j.Logger;
     import org.slf4j.LoggerFactory;
     */
    static final Logger log = LoggerFactory.getLogger(TestHttp2.class);
    public static void main(String[] args) {

        final NioEventLoopGroup boss = new NioEventLoopGroup();
        final NioEventLoopGroup worker = new NioEventLoopGroup();

        try{
            final ServerBootstrap bs = new ServerBootstrap();
            bs.channel(NioServerSocketChannel.class);
            bs.group(boss, worker);
            bs.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));

                    // 做 http服务器端 , 编解码器对请求进行解码
                    ch.pipeline().addLast(new HttpServerCodec());
                    // head<<<<<<<<<
                    /*############################################################################*/
                    /*##        SimpleChannelInboundHandler 可以指定 只关心某一种类型的Handler     ##*/
                    /*############################################################################*/
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() { // 或者：HttpContent
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            log.debug("******************************************************** Get请求");
                            // 请求行
                            log.debug("msg.uri() = " + msg.uri());
                            log.debug("msg.headers() = " + msg.headers());

                            // 返回 响应   new DefaultFullHttpResponse(Http协议版本，Http状态码)
                            final DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);

                            final byte[] bytes = "<h1>Hello, World</h1>".getBytes();
                            response.headers().setInt(CONTENT_LENGTH, bytes.length ); // 【告诉客户端 消息长度，防止一直转圈】
                            response.content().writeBytes(bytes);
                            // 写回响应
                            ctx.writeAndFlush(response);


                        }
                    });
                    // 下面自己随便写的
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpContent>() {

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
                            log.debug("########################################################## Post请求");
                            // 请求行
                            log.debug("msg.toString() = " + msg.toString());

                        }
                    });
                    // end>>>>>>>>>>
                }
            });

            final ChannelFuture channelFuture = bs.bind(8080).sync();
            /*
            让线程进入wait状态，也就是main线程暂时不会执行到finally里面，
            nettyserver也持续运行，如果监听到关闭事件，可以优雅的关闭通道和nettyserver
             */
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Server error", e);

        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }


}

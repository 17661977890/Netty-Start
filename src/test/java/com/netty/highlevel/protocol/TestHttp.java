package com.netty.highlevel.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * v1：
 * 测试 netty提供的 Http协议接口
 * 启动服务端。浏览器输入localhost:8080 测试查看服务端日志
 */
public class TestHttp {
    /*
     import org.slf4j.Logger;
     import org.slf4j.LoggerFactory;
     */
    static final Logger log = LoggerFactory.getLogger(TestHttp.class);
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
                    // 对上面 解码器结果进行处理
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println("------------------------------"+msg.getClass());
                            /*
                            ------------------------------class io.netty.handler.codec.http.DefaultHttpRequest
                            ------------------------------class io.netty.handler.codec.http.LastHttpContent$1
                            */
                            log.debug("{}", msg.getClass());

                            // 可以看出，后期会针对 get请求和post请求判断 【也可以使用 两个 SimpleChannelInboundHandler分别指定 HttpRequest 和 HttpContent】
                            if(msg instanceof HttpRequest){

                            } else if(msg instanceof HttpContent){

                            }
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

package com.netty.netty.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * @author sunbin
 * closeFuture: 处理关闭之后的操作
 */
@Slf4j
public class CloseFutureClient {

    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 增加一个loghandler 方便排查执行流程
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                // 连接服务器，异步非阻塞，main发起调用，真正执行connect是nio线程
                .connect(new InetSocketAddress("localhost",8080));
        Channel channel = channelFuture.sync().channel();
        new Thread(()->{
            Scanner scanner=new Scanner(System.in);
            while (true){
                String line = scanner.nextLine();
                if("q".equals(line)){
                    // close 也是异步操作
                    channel.close();
                    // 错误示范：
//                    log.info("处理关闭之后的操作");
                    break;
                }
                channel.writeAndFlush(line);
            }
        },"input").start();
        // 在这里写 日志是在关闭之前打印。因为close不是在主线程执行的
        // 错误示范：
//        log.info("处理关闭之后的操作");

        // 正确示范：同步
        ChannelFuture closeFuture = channel.closeFuture();
//        System.out.println("waiting close....");
//        closeFuture.sync();
//        log.info("同步方式：处理关闭之后的操作");
        // 正确示范：异步
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("同步方式：处理关闭之后的操作");
            }
        });
    }
}

package com.netty.base.hello;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * 客户端
 *   netty 不能工作在 @Test 里
 */
public class HelloClient {

    public static void main(String[] args) throws InterruptedException{

        // 1. 客户端 启动类 ，组装 协调 下面很多组件 进行启动
        new Bootstrap()
                // 2. 添加 EventLoop, 包含线程 和 选择器
                .group(new NioEventLoopGroup())
                // 3. 选择客户端 channel 实现
                .channel(NioSocketChannel.class)
                // 4. 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    // 5. 连接建立后 才执行
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        //  StringEncoder 把 下面发送的字符串 转为 ByteBuf  【发送到 服务端的 EventLoopGroup】
                        ch.pipeline().addLast(new StringEncoder());


                    }
                })
                // 5. 连接 服务器 【 connect 是异步操作 】
                .connect(new InetSocketAddress("localhost", 8080))
                // 6. sync为了让客户端先同步的方式连上，然后再执行后面信息发送逻辑
                //    sync 是一个同步方法 是一个阻塞方法， 直到连接建立 往下执行
                .sync()
                // 底层就是： 代表 与 服务端 之间 建立的 SocketChannel
                .channel()
                /**
                 * 7. 向服务器 发送信息
                 * 注意 只要是 收发 数据 都会走 handler方法里
                 */
                .writeAndFlush("hello.word");


    }




}

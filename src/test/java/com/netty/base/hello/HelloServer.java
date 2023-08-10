package com.netty.base.hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 首个 netty 测试 helloword
 *  netty 不能工作在 @Test 里
 */
public class HelloServer {

    public static void main(String[] args) {


        // 1. 服务端 的 启动器， 组装 协调 下面很多组件 进行启动
        new ServerBootstrap()
                /**
                 * 2. 添加 事件循环组NioEventLoopGroup 组件 ( 相当于加 BossEventLoop 和 WorkerEventLoop  )
                 *  一个 NioEvenLoopGroup 包含 多个 NioEventLoop
                 *  NioEventLoop 简单理解：包含了线程 和 选择器 selector 管理 accept read等事件
                 *  NioEventLoop 流程   : accept事件进来交给a EventLoop, read事件进来 交给 b EventLoop 接收到ByteBuf
                 *                       然后a b 都会 交给 处理器 childHandler
                 *        BossEventLoop
                 *                    作用：处理可连接事件
                 *        WorkerEventLoop
                 *                    作用：处理可读事件
                 *                    解释：Loop表示循环，循环处理 事件 Event
                 *                    组件：包含 selector 进行监测 各种事件
                 *                    包含 thread
                 *
                 */
                .group(new NioEventLoopGroup())
                /**
                 * 3. 选择一个 通用的 基于 NIO  ServerSocketChannel的 实现 （netty支持好几种实现的）
                 *      还支持 OIO 就是 BIO 阻塞IO 的实现
                 *      还支持 某种操作系统 特别优化的 实现 等
                 *
                 */
                .channel(NioServerSocketChannel.class)
                /**
                 * 4. boss 负责处理连接的，worker(child) 负责读写的 他决定了worker(child) 能执行哪些操作(handler)
                 *      这里的 childHandler 添加的各种处理器 给 每个子线程的 SocketChannel用的
                 *
                 */
                .childHandler(
                        /**
                         * 5. channel 代表与客户端进行数据读写通道
                         *
                         * ChannelInitializer 触发处理器(SocketChannel连接后 才执行)，负责添加别的 handler;
                         */
                        new ChannelInitializer<NioSocketChannel>() {

                            // initChannel可以添加更多处理器
                            protected void initChannel(NioSocketChannel ch) throws Exception {

                                /**
                                 *  6. 添加具体 handler ( ch.pipeline().addLast(添加一道道的工序 )
                                 *
                                 *      StringDecoder 把 传输来的 ByteBuf 转换成字符串
                                 *
                                 */
                                ch.pipeline().addLast(new StringDecoder());

                                /**
                                 * 7. ChannelInboundHandlerAdapter 自定义的 handler
                                 * @param ctx
                                 * @param msg          上一步 ByteBuf 转换的字符串
                                 * @throws Exception
                                 */
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //                                super.channelRead(ctx, msg);
                                        System.out.println(msg);
                                    }

                                });

                            }
                        })
                // 8. 绑定 监听端口
                .bind(8080);

    }













}

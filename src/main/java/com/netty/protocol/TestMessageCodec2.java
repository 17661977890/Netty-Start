package com.netty.protocol;

import com.netty.protocol.message.LoginRequestMessage;
import com.netty.protocol.protocol.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * 测试黏包半包问题
 * 可以通过观察 LOGGIN_HANDLER 打印的日志 已经经过 LengthFieldBasedFrameDecoder 处理后的的日志可以看到半包问题
 */
public class TestMessageCodec2 {

    /**
     * ###########################################################
     * ########    LengthFieldBasedFrameDecoder 不能抽离  #########
     * ###########################################################
     * 1. 两个 Eventloop 线程， 使用同一个对象，会产生线程安全的共享资源
     * 2. 抽离出来的 LengthFieldBasedFrameDecoder对象 主要记录了多次消息之间的状态就是线程不安全的【半包黏包那样保存上一个信息】 ，就不能在多个Eventloop下使用同一个Handler
     */
    public static void main(String[] args) throws Exception {
        /**
         * 这里 注意顺序
         * 帧解码器 处理完，【是完整信息 才会向下一个 handler传递】
         * 抽离出来的 LOGGING_HANDLER  是没有状态信息的Handler, 不会出现这样的问题，来多少数据 就打印多少数据
         */
        final LoggingHandler LOGGIN_HANDLER = new LoggingHandler();
        final MessageCodec MESSAGE_CODEC = new MessageCodec();
        final SimpleChannelInboundHandler<LoginRequestMessage> channelInboundHandler = new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                // 这里的 Object msg 已经 【被 MessageCodec 解码成了 Message类型了】
                LoginRequestMessage loginRequestMessage = (LoginRequestMessage) msg;
                System.out.println(loginRequestMessage.getNickname() + "==================" + loginRequestMessage.getPassword());

            }
        };
        final EmbeddedChannel channel = new EmbeddedChannel(
                // 【移动到流水线的最上方 可以 打印出 半包情况】
                LOGGIN_HANDLER,
                // 解决黏包半包的问题
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                MESSAGE_CODEC,
                channelInboundHandler
        );
        // 出站测试 encode 【出站 自动编码】
        LoginRequestMessage message = new LoginRequestMessage("张三", "123456", "zs");
//        channel.writeOutbound(message);

        // 入站测试 decode  【入站 自动解码】  新建一个buf 【编码 入站】
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);
//        channel.writeInbound(buf);



        // 半包测试
        final ByteBuf s1 = buf.slice(0, 100);
        final ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);
        // 【引用计数 + 1】 才能 调用两次 writeInbound() 保证不会释放内容 ， writeInbound() 会调用release方法
        s1.retain();               // retain + 1 = 2
        channel.writeInbound(s1);  // release 1
        channel.writeInbound(s2);  // release 0

    }

}

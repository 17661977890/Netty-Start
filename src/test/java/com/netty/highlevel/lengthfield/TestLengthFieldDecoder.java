package com.netty.highlevel.lengthfield;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;
/**
 * ################################################
 * ######       基于长度字段的 帧解码器       ########
 * ################################################

 * #################### 案例1 ###################
 * 注意：内容读完后，再读到字节就当成 Length 如果不规范会报错
 *
 *  * <pre>
 *  * <b>lengthFieldOffset</b>   = <b>0</b>
 *  * <b>lengthFieldLength</b>   = <b>2</b> (000c 两个字节)
 *  * lengthAdjustment    = 0
 *  * initialBytesToStrip = 0 (= do not strip header)
 *  *
 *  * BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
 *  * +--------+----------------+      +--------+----------------+
 *  * | Length | Actual Content |----->| Length | Actual Content |
 *  * | 0x000C | "HELLO, WORLD" |      | 0x000C | "HELLO, WORLD" |
 *  * +--------+----------------+      +--------+----------------+
 *  服务器：获取内容长度，从0(lengthFieldOffset=0)开始，先读两(lengthFieldLength=2)个字节，000c = 12，知道内容长度是12
 *         再读 12个字节
 *  * </pre>
 * LengthFieldBasedFrameDecoder(
 *          int maxFrameLength,     限制最大长度，超过他没找到分隔符报错
 *          int lengthFieldOffset,  长度字段 偏移量
 *          int lengthFieldLength,  长度字段 本身长度
 *          int lengthAdjustment,   长度字段 为基准，跳过几个字节 才是内容
 *          int initialBytesToStrip 从头玻璃 几个字节，解析后将不出现
 * )
 *
 * #################### 案例2 【从头剥离几个字节】 ############
 *  * <pre>
 *  * lengthFieldOffset   = 0
 *  * lengthFieldLength   = 2
 *  * lengthAdjustment    = 0
 *  * <b>initialBytesToStrip</b> = <b>2</b> (= the length of the Length field) 从头剥离几个字节，剩下的都是内容
 *  *
 *  * BEFORE DECODE (14 bytes)         AFTER DECODE (12 bytes)
 *  * +--------+----------------+      +----------------+
 *  * | Length | Actual Content |----->| Actual Content |
 *  * | 0x000C | "HELLO, WORLD" |      | "HELLO, WORLD" |
 *  * +--------+----------------+      +----------------+
 *  * </pre>
 *
 *  #################### 案例3 【带消息头】 ###################
 *  <h3>3字节长度字段位于5字节头的末尾，不带头</h3>
 *
 *  * <pre>
 *  * <b>lengthFieldOffset</b>   = <b>2</b> (= the length of Header 1) 长度字段偏移量 长度= 2 = 头部长度
 *  * <b>lengthFieldLength</b>   = <b>3</b> 指定长度字段本身 为 3字节
 *  * lengthAdjustment    = 0
 *  * initialBytesToStrip = 0     不剥离字节长度，解析过后还是17字节
 *  *
 *  * BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
 *  * +-- 2字节 --+-- 3字节 --+---- 12字节 -----+      +----------+----------+----------------+
 *  * | Header 1 |  Length  | Actual Content |----->| Header 1 |  Length  | Actual Content |
 *  * |  0xCAFE  | 0x00000C | "HELLO, WORLD" |      |  0xCAFE  | 0x00000C | "HELLO, WORLD" |
 *  * +----------+----------+----------------+      +----------+----------+----------------+
 *  * </pre>
 *
 *   #################### 案例4 【指定长度和长度调整(头部长度)跳过头部内容】 ###################
 *   <h3>3字节长度字段位于5字节头的开头，不带头</h3>
 *
 *  * <pre>
 *  * lengthFieldOffset   = 0
 *  * lengthFieldLength   = 3
 *  * <b>lengthAdjustment</b>    = <b>2</b> (= the length of Header 1)
 *  * initialBytesToStrip = 0
 *  *
 *  * BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
 *  * +-- 3字节 --+-- 2字节 --+---- 12字节 ----+       +----------+----------+---------------+
 *  * |  Length  | Header 1 | Actual Content |----->|  Length  | Header 1 | Actual Content |
 *  * | 0x00000C |  0xCAFE  | "HELLO, WORLD" |      | 0x00000C |  0xCAFE  | "HELLO, WORLD" |
 *  * +----------+----------+----------------+      +----------+----------+----------------+
 *  Length = 12 表示内容长度
 *  lengthAdjustment = 2 指跳过两个字节长度的头部内容后 ，有 Length = 12 字节长度的主体消息内容
 *  * </pre>
 *
 *   #################### 案例5 【偏移1后的长度字段 调整1字节后 才是内容字段 在丢掉从头开始数3字节】 ########
 *   <h3>在4字节头部中间偏移1的2字节长度字段，带头头字段和长度字段</h3>
 *
 *  * <pre>
 *  * lengthFieldOffset   = 1 (= the length of HDR1)
 *  * lengthFieldLength   = 2
 *  * <b>lengthAdjustment</b>    = <b>1</b> (= the length of HDR2)
 *  * <b>initialBytesToStrip</b> = <b>3</b> (= the length of HDR1 + LEN)
 *  *
 *  * BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
 *  * + 1字节 +- 2字节 -+ 1字节 +--- 12字节  ----+      +------+----------------+
 *  * | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
 *  * | 0xCA | 0x000C | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
 *  * +------+--------+------+----------------+      +------+----------------+
 *  lengthFieldOffset = 1 ：HDR1长度是1， 偏移1后才是长度字段Length
 *  lengthFieldLength = 2 : 长度字段Length长2 ，内容长度=12
 *  lengthAdjustment  = 1 : 长度字节Length调整1个字节，过后才是内容字段
 *  initialBytesToStrip = 3 : 要从头开始 剥离的字节长度，头3个字节不想要了
 *  * </pre>
 *
 */
/**
 * 快捷键
 *
 * Ctrl + Alt + M  封装方法
 * Ctrl + Alt + p  抽成参数
 */
public class TestLengthFieldDecoder {

    public static void main(String[] args) {

        /*###############################*/
        /*###       当成 服务器端       ###*/
        /*###############################*/
        final EmbeddedChannel ch = new EmbeddedChannel(
                // 注意：解码器 放debug上面
                new LengthFieldBasedFrameDecoder(
                        1024, 0, 4, 3, 0),
                new LoggingHandler(LogLevel.DEBUG)
        );

        /*###############################*/
        /*###       当成 客户端        ###*/
        /*###############################*/
        // 指定：长度字段-4字节；
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        send(buf, "hello, world");
        send(buf, "hi");

        // 将消息 写入 Channel
        ch.writeInbound(buf);

    }

    private static void send(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes();        // 实际内容
        final int length = bytes.length;    // 实际内容长度

        buf.writeInt(length);               // 指定长度 和 存储模式为： 大端模式  【writeInt本身长度 4字节】 【服务端 lengthFieldLength设为4】

        buf.writeBytes(new byte[]{'1','0','0'}); // 版本号 100，  【服务端 lengthAdjustment 设为 3】 【注意：如果设0 会报错(除非主体内容最后四位都是0)】

        buf.writeBytes(bytes);
    }

    /**
     * 高低位测试
     */
    @Test
    public void test1()
    {
        int num = 0x12345678;

//        "%d\n", num
    }

}

package com.netty.base.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * @author sunbin
 * @date 2023年08月09日 16:59:00
 * @description ByteBuf
 */
public class TestByteBuf {

    public static void main(String[] args) {
        // 1、ByteBuf 创建
//        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
//        log(byteBuf);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 32; i++) {
//            sb.append("a");
//        }
//        byteBuf.writeBytes(sb.toString().getBytes());
//        log(byteBuf);

        // 2、切片（零拷贝体现之一）
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
        byteBuf.writeBytes(new byte[]{1,2,3,4,5,6,7,8,9,10});
        log(byteBuf);

        ByteBuf byteBuf1 = byteBuf.slice(0,5);
        byteBuf1.retain();
        ByteBuf byteBuf2 = byteBuf.slice(5,5);
        byteBuf2.retain();
        log(byteBuf1);
        log(byteBuf2);

        // 3、对于原有的buf释放，需要对切片retain，否则会报错io.netty.util.IllegalReferenceCountException: refCnt: 0，切片单独进行释放。
        // 调用 release 方法计数减 1，如果计数为 0，ByteBuf 内存被回收
        // 调用 retain 方法计数加 1，表示调用者没用完之前，其它 handler 即使调用了 release 也不会造成回收
        System.out.println("释放原有 bytebuf 内存");
        byteBuf.release();
        log(byteBuf1);

        byteBuf1.release();
        byteBuf2.release();

    }


    /**
     * 日志
     * @param buffer
     */
    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }
}

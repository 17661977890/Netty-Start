package com.netty.netty.bytebuf;

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
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        log(byteBuf);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append("a");
        }
        byteBuf.writeBytes(sb.toString().getBytes());
        log(byteBuf);
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

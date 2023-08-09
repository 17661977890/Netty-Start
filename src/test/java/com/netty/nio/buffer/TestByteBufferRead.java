package com.netty.nio.buffer;


import java.nio.ByteBuffer;

import static com.netty.nio.ByteBufferUtil.debugAll;

/**
 * byteBuffer 其他方法
 * get(int i)
 * rewind
 * mark
 * reset
 */

public class TestByteBufferRead {


    public static void main(String[] args) {
        // 分配空间 allocate
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        // 写入数据 put
        byteBuffer.put(new byte[]{'b','c','d','e'});
        debugAll(byteBuffer);
        // 切换读模式 flip
        byteBuffer.flip();

        System.out.println((char)byteBuffer.get(3));
        // rewind (rewind 方法将 position 重新置为 0)
//        // 一次读取3个字节
//        byteBuffer.get(new byte[3]);
//        debugAll(byteBuffer);
//        // 重头在读
//        byteBuffer.rewind();
//        System.out.println((char)byteBuffer.get());
//        System.out.println((char)byteBuffer.get());

        // mark reset (mark 是在读取时，做一个标记，即使 position 改变，只要调用 reset 就能回到 mark 的位置)
        System.out.println((char)byteBuffer.get());
        System.out.println((char)byteBuffer.get());
        byteBuffer.mark();
        System.out.println((char)byteBuffer.get());
        System.out.println((char)byteBuffer.get());
        byteBuffer.reset();
        System.out.println((char)byteBuffer.get());
        System.out.println((char)byteBuffer.get());
    }
}

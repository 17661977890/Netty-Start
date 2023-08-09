package com.netty.nio.buffer;


import java.nio.ByteBuffer;

import static com.netty.nio.ByteBufferUtil.debugAll;

/**
 * buffer 读写测试
 * allocate
 * put
 * get
 * flip
 * compact
 */

public class TestByteBufferReadWrite {


    public static void main(String[] args) {
        // 分配空间 allocate
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        // 写入数据 put
        byteBuffer.put((byte) 'a');
        debugAll(byteBuffer);
        byteBuffer.put(new byte[]{'b','c','d'});
        debugAll(byteBuffer);

        // 读取数据 get （get无参方法 是从当前位置读取，此时position为4 当前位置没有数据，则读取为空）
//        byte b = byteBuffer.get();
//        System.out.println(b);

        // 切换读模式 flip
        byteBuffer.flip();
        byte b2 = byteBuffer.get();
        System.out.println(b2);
        debugAll(byteBuffer);

        // 切换写模式 compact 未读取的内容前移压缩
        byteBuffer.compact();
        debugAll(byteBuffer);

        byteBuffer.put(new byte[]{'e','f'});
        debugAll(byteBuffer);

    }
}

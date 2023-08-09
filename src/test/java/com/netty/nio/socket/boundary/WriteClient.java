package com.netty.nio.socket.boundary;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 客户端
 */
public class WriteClient {

    @Test
    public void testClient1() throws IOException {

        SocketChannel sc = SocketChannel.open();
        // 指定要连接的 服务器 和 端口号
        sc.connect(new InetSocketAddress("localhost", 8080));
        // 接收数据
        int count = 0;
        while (true){
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024*1024);
            count += sc.read(byteBuffer);
            System.out.println(count);
            byteBuffer.clear();
        }
    }


}

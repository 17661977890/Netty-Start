package com.netty.nio.socket.multithread;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 客户端
 */
public class Client {

    @Test
    public void testClient1() throws IOException {

        SocketChannel sc = SocketChannel.open();
        // 指定要连接的 服务器 和 端口号
        sc.connect(new InetSocketAddress("localhost", 8080));
        SocketAddress address = sc.getLocalAddress();
        // 不需要扩容buffer的内容
        sc.write(Charset.defaultCharset().encode("0123456789abcdef"));
        System.in.read();

    }


}

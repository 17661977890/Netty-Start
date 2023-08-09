package com.netty.nio.socket.nioblock;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * 客户端
 */
public class Client {


    /**
     *
     * 执行到断点后， 进行 Evaluate , 输入 ：
     *      sc.write(Charset.defaultCharset.encode("1111 a"));
     * @throws IOException
     */
    @Test
    public void testClient1() throws IOException {

        SocketChannel sc = SocketChannel.open();

        // 指定要连接的 服务器 和 端口号
        sc.connect(new InetSocketAddress("localhost", 8080));

        // 为了让 代码 不结束 【这里要打 断点】
        System.out.println("waiting ... ");

    }


}

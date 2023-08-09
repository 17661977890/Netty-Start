package com.netty.nio.socket.selector;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * 服务端
 *  这里用 selector 管理 多个 channel ( 可管理 ServerSocketChannel、SocketChannel )
 *
 *  多路复用 : 单线程可以配合 Selector 完成对多个 Channel 可读写事件的监控
 *
 *
 * SelectionKey  通过它可以知道事件 和 知道哪个channel通道
 * SelectionKey  四种事件：
 *        accept  -客户端连接请求触发 (服务端 事件)
 *        connect -服务端连接建立触发 (客户端 事件)
 *        read    -可读事件
 *        wirte   -可写事件
 *
 *  1）服务器连接通道 注册 到 selector 后
 *  2）将来 selector 可以监听到所有事件
 *  3）如果这个事件发生了，将把这个 SelectionKey 放到 selectedKeys 集合里
 *  4）然后 遍历selectedKeys 集合，每拿出一个 SelectionKey 就知道发生了什么事件
 *
 *  SelectionKey.channel() 可以获取 channel,因为每个key都关联一个channel，可能是服务端或客户端的
 *
 *  Selector.select() 监听事件，详细的是：
 *     有未处理的未取消事件，不阻塞
 *     取消了 或 已处理了 则阻塞
 *     # 所以 事件要么处理 要么取消
 */
@Slf4j
public class Server {

    @Test
    public void testServer1() throws IOException {

        // 1. 创建 selector 来管理多个channel
        Selector selector = Selector.open();
        // 创建一个 服务器 对象 通道
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // selector 必须工作在非阻塞模式下  影响 accept 变成 非阻塞方法
        ssc.configureBlocking(false);

        // 2. 建立 selector 和 服务器 的连接 (将服务器连接通道 注册 到 selector)
        // 通过SelectionKey 可以知道事件 和 知道哪个channel通道，第二参数：0 不关注任何事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // 绑定事件： 指明 SelectionKey  绑定的事件 selector 才会关心
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register sscKey: {}", sscKey);

        // 绑定一个 监听端口
        ssc.bind(new InetSocketAddress(8080));

        while(true) {
            System.out.println("before 循环 ......");
            // 3. 选择器，有未处理或未取消事件，不阻塞 继续运行 ； 否则 阻塞。
            // select 在事件未处理时，他不会阻塞，事件发生后要么处理，要么取消，不能置之不理。
            selector.select();

            // 4. 处理事件
            //  selectKeys 内部包含所有发生的事件，譬如两个客户端连上了 会有两个key
            //  如果在遍历里 还可以删除 必须用迭代器
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();
                log.debug("--------------------------- 有事件 进来 了，whatKey --------------------------------");

                // 每次迭代完 要移除掉，不然  可读事件 进来 循环时， 先判断肯定是ssc accept事件，
                // 但是此时没有连接事件(这个事件还是上一次的,事件不会自己删除)，所以在处理时 sc=channel.accept() 是null ，
                // 下面进一步处理时，就报空指针异常
                // selectedKeys 里删除
                iter.remove();

                // 5. 根据 事件类型 处理
                // accept  -客户端连接请求触发 (服务端 事件)
                if(key.isAcceptable()) {
                    log.debug("acceptKey: {}", key);
                    // 获取 服务器对象通道
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    // 获取 读写通道
                    // 【【【调用accept方法 就意味着把事件 处理掉了，或者 取消  key.cancel();】】】
                    SocketChannel sc = channel.accept();
                    // selector 必须工作在非阻塞模式下   影响 read 变成 非阻塞方法
                    sc.configureBlocking(false);

                    // 读写通道 SocketChannel 注册到 selector 上
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);

                    log.debug("SocketChannel sc : {}", sc);
                    // read    -可读事件
                }else if (key.isReadable()) {
                    log.debug("readKey: {}", key);
                    // try 处理 客户端的 read事件(异常关闭触发) 的 read 异常。 ---手动关闭和异常关闭客户端链接，都会断开连接报错
                    // 不cancel取消的话，即使remove，下次还 select出来
                    try {
                        // SocketChannel才有 读权限
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 分配 16字节的一个内存 来 存放接收的数据
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        // 客户端 正常断开产生一个read事件，但是没数据， 返回的是 -1
                        int read = channel.read(buffer);
                        if(read == -1) {
                            //  取消掉，让  select() 阻塞
                            key.cancel();
                        }else{
                            buffer.flip();
                            System.out.println(Charset.defaultCharset().decode(buffer));
//                            debugAll(buffer);
                            buffer.clear();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //  取消掉，让  select() 阻塞，从selector 的 key中删除
                        key.cancel();
                    }
                }

            }
            System.out.println("end 循环 ......");
        }
    }

}

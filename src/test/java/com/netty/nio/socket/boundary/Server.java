package com.netty.nio.socket.boundary;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import static com.netty.nio.ByteBufferUtil.debugAll;


/**
 * 服务端---处理消息边界
 *  这里主要 处理 容量超出 问题
 *
 * 程序里 '\n' 分隔 处理的缺点是：
 *      首先要知道的是：主程序里 channel.read(buffer)读数据时， 超出buffer分配内存长度 将再次或多次触发读事件 进行循环
 *      所以当发送数据超过 分配内存长度时，第一次读取将会 丢失，因为没有 '\n'
 *
 * 解决思路：
 *      1.  首先将 channel.read(buffer) 里，这个 buffer 局部变量 改为全局变量 (方便一次性读取 客户端数据)
 *      2.  在每个客户端SocketChannel 注册Selector时，第三参数 附件里 加入 开辟的内存ByteBuffer buffer (这时的 buffer 的生命周期 将和SelectionKey 一样了)
 *      3.  然后在 每次读事件 里 attachment获取这个附件参数强转回 ByteBuffer
 *      4.  子方法compact 结束后，主方法 对比position==limit检查是否超出内存，超出内存说明当前没有读取到将 触发读事件再次循环
 *      5.  如果超出内存：开辟原内存字节长度 改为两倍，进入下一次循环继续判断，直到满足长度 处理完
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

                    // todo 1、多次读取使用同一个byteBuffer（扩容）同一个channal 绑定同一个buffer
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    // 将一个 byteBuffer 作为附件关联到 selectionKey 上
                    SelectionKey scKey = sc.register(selector, 0, buffer);
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
                        // todo 2、获取 selectionKey 上关联的附件
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        // 客户端 正常断开产生一个read事件，但是没数据， 返回的是 -1
                        int read = channel.read(buffer);
                        if(read == -1) {
                            //  取消掉，让  select() 阻塞
                            key.cancel();
                        }else{
                            // todo 3、处理消息边界,利用分隔符获取完整消息
                            split(buffer);

                            // todo 4、如果拿不到完整消息需要扩容buffer
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();
                                newBuffer.put(buffer); // 0123456789abcdef3333\n
                                // todo 5、绑定新的buffer到scKey的附件，准备接收下一次的读取
                                key.attach(newBuffer);
                            }


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


    /**
     * 根据分隔符获取一条完整的消息
     * @param source
     */
    private static void split(ByteBuffer source) {
        // 读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            byte b = source.get(i);
            if (b == '\n') {
                int position = source.position();
                int length = i + 1 - position;
                // 完整 消息 存入 新 buffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读 ，写入 target
                for (int j = 0; j < length; j++) {
                    target.put(source.get()); // 每一次 get 时， position++
                }
                debugAll(target);
                System.out.println("------------------------------------ 有 \\n 的一个循环 --------------------------------------------------");
            }
        }
        // 未读完部分向前压缩,如果 position和limit一样 说明一个完整消息没有被读取掉
        source.compact();

    }


}

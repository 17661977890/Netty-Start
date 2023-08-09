package com.netty.nio.socket.boundary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author sunbin
 * @date 2023年08月03日 14:56:00
 * @description 可写事件
 */
public class WriteServer {

    /**
     * 大批量写入事件
     * 方式1：死循环持续写入 （注册一次写入事件）
     * 方式2：注册多次写入事件（只要没写完就注册写入事件）
     */
    public static void main(String[] args) throws IOException {
        //1、创建服务器对象非阻塞channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        // 2、创建Selector来管理channel,并绑定accept事件
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        // 服务器channel绑定端口
        ssc.bind(new InetSocketAddress(8080));

        while (true){
            // 3、监听事件：利用selector阻塞，没有事件的时候
            selector.select();
            // 迭代 SelectionKey 事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                System.out.println("key:"+key);
                // 移出当前进行处理的事件，防止null报错
                iterator.remove();
                // 4、处理事件：
                if(key.isAcceptable()){
                    // 监听到accept事件,创建 客户端channel，处理写请求
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    // 关注可读事件
                    SelectionKey scKey = sc.register(selector,0,null);
                    scKey.interestOps(SelectionKey.OP_READ);

                    // 5、向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i=0;i<3000000;i++) {
                        sb.append("a");
                    }
                    ByteBuffer byteBuffer = Charset.defaultCharset().encode(sb.toString());
                    // todo 不能很好的体现非阻塞思想效率低
//                    while (byteBuffer.hasRemaining()){
//                        int write = sc.write(byteBuffer);
//                        System.out.println(write);// 返回实际写入的字节数
//                    }
                    // 6、如果写不完继续 关注可写事件
                    int write = sc.write(byteBuffer);
                    System.out.println(write);// 返回实际写入的字节数
                    if(byteBuffer.hasRemaining()){
                        System.out.println(scKey.interestOps());
                        // 关注可写事件（这里因为channel可能不止关注一种事件，在上面我们增加关注一个可读事件，如果这里只关注写事件，会覆盖）
                        // read 1  write 4 如果是5 说明即关注读也关注写 (两种写法)
                        scKey.interestOps(scKey.interestOps()+SelectionKey.OP_WRITE);
//                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);
                        // 把没写完的数据挂到scKey上
                        scKey.attach(byteBuffer);
                    }
                }else if(key.isWritable()){
                    // 7、持续处理可写事件，向客户端写入数据
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(byteBuffer);
                    System.out.println(write);// 返回实际写入的字节数
                    // 8、如果写完后，要清理buffer,不需要再关注可写事件
                    if(!byteBuffer.hasRemaining()){
                        key.attach(null);
                        key.interestOps(key.interestOps()-SelectionKey.OP_WRITE);
                    }
                }
            }
        }

    }
}

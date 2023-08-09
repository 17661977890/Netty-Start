package com.netty.nio.socket.multithread;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static com.netty.nio.ByteBufferUtil.debugAll;

/**
 * 多个worker
 */
@Slf4j
public class MultiThreadServer3 {

    /**
     * 多线程版本的nio 服务器
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // boss主线程处理accept事件，其他事件由其他work线程处理
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey selectionKey = ssc.register(boss,0,null);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        // 多个worker
        Worker[] workers = new Worker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-"+i);
        }
        // 计数器
        AtomicInteger index = new AtomicInteger();
        while (true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.info("connected........");
                    // 计数器轮询
                    workers[index.getAndIncrement() % workers.length].register(sc);
                    log.info("worker register deal read........");
                }
            }
        }
    }


    /**
     * 创建worker线程,处理写事件
     * ConcurrentLinkedQueue 存储执行方法任务，可以在不同的线程间进行通信
     * 问题起源：
     *  一开始在main方法中进行worker线程的创建，并且执行sc.register() 这样不能完全避免阻塞的问题，即select()先执行，会阻塞事件的注册或者后面客户端的链接
     *  考虑放在内部类worker.register方法中，也不行，因为还是由boss线程执行的，问题不能避免。
     *  selector.select();阻塞是在worker线程中执行的。
     *  sc.register(); 是在boss线程中注册的
     *
     *  所以想办法把 两个方法放在同一个线程中处理
     */
    static class Worker implements Runnable{
        private Thread thread;
        private Selector selector;
        private String name;
        // worker就初始化一次线程就行
        private volatile boolean flag;


        public Worker(String name){
            this.name=name;
        }
        // 初始化线程和selector
        public void register(SocketChannel sc) throws IOException {
            if(!flag){
                selector=Selector.open();
                thread = new Thread(this,name);
                thread.start();
                flag=true;
            }

            // boss线程唤醒 worker中的select方法，避免下面run 先执行select直接阻塞没法注册read事件
            selector.wakeup();
            sc.register(selector,SelectionKey.OP_READ,null);

        }
        // worker线程处理读写事件
        @Override
        public void run() {
            while (true){
                try {
                    selector.select();

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey key=iterator.next();
                        iterator.remove();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                        SocketChannel sc = (SocketChannel) key.channel();
                        try{
                            if(key.isReadable()){
                                int read = sc.read(byteBuffer);
                                if(read!=-1){
                                    byteBuffer.flip();
                                    debugAll(byteBuffer);
                                }else {
                                    key.cancel();
                                    sc.close();
                                }
                            }
                        }catch (IOException e){
                            key.cancel();
                            sc.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

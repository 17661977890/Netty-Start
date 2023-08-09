package com.netty.nio.socket.multithread;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.netty.nio.ByteBufferUtil.debugAll;

/**
 * 利用队列解决多线程间执行阻塞问题
 */
@Slf4j
public class MultiThreadServer {

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
        // 创建一个worker线程（固定数量，不能放在循环里，不是有多少链接就有多少worker）
        Worker worker = new Worker("worker-0");

        // todo 问题1： 这里创建worker线程，内部执行run---select()阻塞,导致下面 sc.register无法绑定事件
        // 就是sc.register() 和 select() 方法的执行顺序的问题（sc.register要在select之前）
//        worker.register();

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
                    // todo 问题2: 放在这里当有一个客户端连接是没有问题，先注册了事件，select不会阻塞，但是当前连接事件处理完继续阻塞，再有新的客户端连接时候就会继续阻塞，因为woker线程只有这一个。
//                    worker.register();
//                    sc.register(worker.selector,SelectionKey.OP_READ,null);

                    // todo 3: 把sc.register 考虑放到创建worker线程内部逻辑中，最好是worker线程中，即run()中，保证slect()和sc.register() 在同一个线程中执行
                    worker.register(sc);
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
        // 任务队列，线程安全
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

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
            // 向队列中添加任务，但是这个任务并没有执行
            queue.add(()->{
                try{
                    // 关联worder线程的selector，绑定读事件
                    sc.register(selector,SelectionKey.OP_READ,null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // 唤醒select方法，避免下面run 先执行select直接阻塞没法注册read事件
            selector.wakeup();

        }
        // worker线程处理读写事件
        @Override
        public void run() {
            while (true){
                try {
                    selector.select();
                    // 从队列中获取任务 执行任务，在worker线程中 执行 sc.register(selector,SelectionKey.OP_READ,null);
                    // 为什么不放在select之前先注册read？ 我觉得可能是因为task可能是空 还是会直接阻塞 所以直接在前面唤醒wakeUp
                    Runnable task = queue.poll();
                    if(task!=null){
                        task.run();
                    }
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

package com.netty.base.future;



import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author sunbin
 * netty Future
 */
@Slf4j
public class TestNettyFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.info("线程池中的线程执行任务");
                Thread.sleep(1000);
                return 10;
            }
        });
        // 3、主线程可以通过future获取结果（同步）
//        log.info("主线程等待结果");
//        Integer result = future.get();
//        log.info("结果:{}",result);
        // 异步获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                Integer result = (Integer) future.get();
                log.info("异步接收结果:{}",result);
            }
        });
    }
}

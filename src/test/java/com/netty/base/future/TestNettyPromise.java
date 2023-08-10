package com.netty.base.future;


import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @author sunbin
 * netty promise
 */
@Slf4j
public class TestNettyPromise {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 1、定义结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        new Thread(()->{
            // 2、执行任务
            log.info("开始计算");
            try {
                int i=1/0;
                Thread.sleep(1000);
                // 3、填充结果
                promise.setSuccess(80);
            } catch (Exception e) {
                e.printStackTrace();
                promise.setFailure(e);
            }
        }).start();

        // 4、接收结果
        log.info("结果：{}",promise.getNow());
        log.info("结果：{}",promise.get());

    }
}

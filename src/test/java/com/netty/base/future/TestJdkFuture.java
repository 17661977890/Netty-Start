package com.netty.base.future;



import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author sunbin
 * jdk Future
 */
@Slf4j
public class TestJdkFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、创建固定数量线程池
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 2、提交任务
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.info("线程池中的线程执行任务");
                Thread.sleep(1000);
                return 10;
            }
        });
        // 3、主线程可以通过future获取结果
        log.info("主线程等待结果");
        Integer result = future.get();
        log.info("结果:{}",result);
    }
}

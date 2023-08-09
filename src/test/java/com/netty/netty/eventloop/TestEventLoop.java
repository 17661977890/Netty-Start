package com.netty.netty.eventloop;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author sunbin
 * @date 2023年08月09日 10:44:00
 * @description TODO
 */
@Slf4j
public class TestEventLoop {

    public static void main(String[] args) {
        // 创建事件循环组（指定线程数）
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2); //可以处理 io事件、普通人物，定时任务
//        EventLoopGroup eventLoopGroup1 = new DefaultEventLoopGroup();//可以处理 普通任务，定时任务 不能处理io事件
        // 获取下一个事件循环对象
        System.out.println(eventLoopGroup.next()); // 第一个对象
        System.out.println(eventLoopGroup.next()); // 第二个对象
        System.out.println(eventLoopGroup.next()); // 第一个对象


        // 执行普通任务
//        eventLoopGroup.next().submit(()->{
//            log.info("执行普通任务");
//        });

        eventLoopGroup.next().scheduleAtFixedRate(()->{
            System.out.println("执行定时任务");
        },0,1, TimeUnit.SECONDS);

        log.info("main线程");
    }
}

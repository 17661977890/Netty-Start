package com.netty.nio.socket.aio;


import com.netty.nio.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 异步 IO 测试
 *  多线程才能实现 异步
 *  这里系统自动创建的 多线程是 守护线程， 当 进程中 没有任何非守护线程在运行，那么 进程不会 等待 守护线程， 会终止 守护线程
 */
@Slf4j
public class AioFileChannel {

    @Test
    public void test1() throws IOException {

        /**
         *
         * 第三参数：线程池，因为异步io必须多线程，如：一个线程发起read 送结果的线程要另一个线程
         */
        // try() 里的 程序 可以被 程序自动释放资源
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"), StandardOpenOption.READ)){
            final ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin ..........................................");
            /**
             * 参数1：ByteBuffer
             * 参数2：起始位置
             * 参数3：附件，一个读不完 需要另一个 byteBuffer
             * 参数4：包含两个回调方法 的 回调对象CompletionHandler ，回调 接收结果的操作
             *       当然这两个回调方法 一定是 另一个线程操作的
             *
             */
            channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                /**
                 *  一次 read成功了的 回调方法
                 * @param result       读到的 实际字节数
                 * @param attachment   传过来的 buffer对象
                 */
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("读取成功 ... 读取字节数 = {}",  result);
                    attachment.flip();
                    ByteBufferUtil.debugAll(attachment);

                }
                @Override // read 过程中 异常了，将调用
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            log.debug("read   end ..........................................");

        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("主线程 做其他事 ...");
        System.in.read();
    }
}

package com.netty.nio.buffer;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 首次 测试
 * 内容：
 * 1. allocate 分配内存
 * 2. read 从文件中 读取
 * 3. flip 切换读模式
 * 4. get  获取字节 并转字符 打印
 * 5. compact 或 clear 调整 position 和 limit 进行 写模式
 *
 */
@Slf4j
public class TestByteBuffer {

    /**
     *
     * 注意：文件可能 很大， 缓冲区不能跟随文件的大小 设置的很大
     * 可以分多次读取
     * 文件输入流获取channal，channal读取文件数据并写入buffer，然后再从buffer读出来打印
     * @param args
     */
    public static void main(String[] args) {

        // FIleChannel   一个数据的读写通道
        // 1. 输入输出流， 2. RandomAccessFIle 随机读写文件类
        try(FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 准备缓冲区 存储 读取数据
            // 划分一个十个字节 的内存【单位：字节】
            ByteBuffer buffer = ByteBuffer.allocate(10);

            while(true) {
                // 从 channel 里读取数据，准备 向 buffer 写入
                int len = channel.read(buffer); 
                // 返回值：读到的实际字节数 ， -1则是没有数据了
                log.debug("此次 读取到的字节长度是 {}", len);
                if(len == -1)break;

                // 切换至 读模式 [ position指针指向开头， limit指向写入的最后位置 (内存长度) ]
                buffer.flip();
                // 检查是否有剩余的数据
                while(buffer.hasRemaining()) {
                    byte b = buffer.get();  // 一次读一个字节
                    System.out.println((char) b); // 强转字符 并打印
                }

                // 一次 循环后  切换为 写 模式 [ 复位了 position limit 指针 (动不了 实际数据) ] 两种不同的切换写模式方法：clear compact
                // buffer.clear();
                // 从 上次未读完的地方 向前移动，并沿着 未读完数据的最后一位往后写
                buffer.compact();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}

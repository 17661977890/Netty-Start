package com.netty.nio.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.netty.nio.ByteBufferUtil.debugAll;

public class TestByteBufferString {


    public static void main(String[] args) {

        // 1. 字符串 转 字节数组 ===> put 进去
        ByteBuffer bu1 = ByteBuffer.allocate(16);
        bu1.put("ByteBuffer.put".getBytes());
        debugAll(bu1);

        // 2. 使用 字符集类 Charset , 它本身就支持 和 ByteBuffer 相互转换
        //    自动切换为 读模式了
        ByteBuffer bu2 = StandardCharsets.UTF_8.encode("StandardCharsets.UTF_8.encode");
        debugAll(bu2);

        // 3. nio提供的工具类 wrap 方法， 将字节数组 ===> 包装成ByteBuffer
        //    自动切换为 读模式了
        ByteBuffer bu3 = ByteBuffer.wrap("ByteBuffer.wrap".getBytes());
        debugAll(bu3);

        // 4. 使用 字符集类 Charset
        ByteBuffer bu4 = Charset.forName("utf-8").encode("Charset.format()");
        debugAll(bu3);

        // 一、 内存里的 ByteBuffer 转为 字符串
        String Str_bu2 = StandardCharsets.UTF_8.decode(bu2).toString();
        System.out.println("KKKKKKKKKKKKKKK " + Str_bu2);

        String Str_bu3 = StandardCharsets.UTF_8.decode(bu3).toString();
        System.out.println("KKKKKKKKKKKKKKK " + Str_bu3);

        bu1.flip();
        String Str_bu1 = StandardCharsets.UTF_8.decode(bu1).toString();
        System.out.println("KKKKKKKKKKKKKKK " + Str_bu1);

        CharBuffer Str_bu4 = Charset.forName("utf-8").decode(bu4);
        System.out.println("KKKKKKKKKKKKKKK " + Str_bu4);
    }



}
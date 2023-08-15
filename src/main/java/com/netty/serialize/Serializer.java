package com.netty.serialize;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 自定义序列化算法接口
 */
public interface Serializer {

    // 反序列化方法
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    // 序列化方法
    <T> byte[] serialize(T object);

    /**
     * 自定义序列化算法枚举，实现多种实现方式：
     * 1、原生java
     * 2、json
     */
    enum Algorithm implements Serializer {
        // Java 实现
        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream in =
                        new ObjectInputStream(new ByteArrayInputStream(bytes));
                    Object object = in.readObject();
                    return (T) object;
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("SerializerAlgorithm.Java 反序列化错误", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    new ObjectOutputStream(out).writeObject(object);
                    return out.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("SerializerAlgorithm.Java 序列化错误", e);
                }
            }
        },
        // Json 实现(引入了 Gson 依赖)
        Json {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                return new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                return new Gson().toJson(object).getBytes(StandardCharsets.UTF_8);
            }
        };

        // 需要从协议的字节中得到是哪种序列化算法
        public static Algorithm getByInt(int type) {
            Algorithm[] array = Algorithm.values();
            if (type < 0 || type > array.length - 1) {
                throw new IllegalArgumentException("超过 SerializerAlgorithm 范围");
            }
            return array[type];
        }
    }
}
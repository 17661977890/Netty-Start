package com.netty.protocol.protocol;

import com.netty.protocol.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * #########################################################################################
 * ##########   【自定义】消息 编解码 类  【 不支持@Sharable 】   不能被共享               ########
 * ##########   ByteToMessageCodec 不支持 @Sharable注解 因为进来的消息，可能会是不完整的   ########
 * ########################################################################################
 * 相当于两个handler合二为一，既能入站 也能做出站处理
 *  <b>魔数     </b>，用来在第一时间判定是否是无效数据包
 *  <b>版本号   </b>，可以支持协议的升级
 *  <b>序列化算法</b>，消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
 *  <b>指令类型  </b>，是登录、注册、单聊、群聊... 跟业务相关
 *  <b>请求序号  </b>，为了双工通信，提供异步能力
 *  <b>正文长度  </b>
 *  <b>消息正文  </b>
 *  我们的编解码器是线程安全的，但是因为父类的限制，不能增加共享注解
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {

    /**
     * 编码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {

        out.writeBytes(new byte[]{1,2,3,4}); // 4字节的 魔数
        out.writeByte(1);                    // 1字节的 版本
        out.writeByte(0);                    // 1字节的 序列化方式 0-jdk,1-json
        out.writeByte(msg.getMessageType()); // 1字节的 指令类型
        out.writeInt(msg.getSequenceId());   // 4字节的 请求序号
        out.writeByte(0xff);                 // 1字节的 对其填充，只为了非消息内容 是2的整数倍(计算下共15个字节，不专业)

        // jdk序列化（输入）
        // (java对象---> byteArray) 处理内容 用对象流包装字节数组 并写入
        ByteArrayOutputStream bos = new ByteArrayOutputStream(); // 访问数组
        ObjectOutputStream oos = new ObjectOutputStream(bos);    // 用对象流 包装
        oos.writeObject(msg);

        byte[] bytes = bos.toByteArray();

        // 写入内容 长度(因为是int : 4字节)
        out.writeInt(bytes.length);
        // 写入内容
        out.writeBytes(bytes);


    }

    /**
     * 解码
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int magicNum = in.readInt();        // 大端4字节的 魔数
        byte version = in.readByte();       // 版本
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();

        int length = in.readInt();
        final byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // jdk反序列化（字节数组-->java对象）
        // 处理内容
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bis);
        // 转成 Message类型
        Message message = (Message) ois.readObject();

        log.debug("{},{},{},{},{},{}",magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);


        // 将message给下一个handler使用
        out.add(message);


    }
}

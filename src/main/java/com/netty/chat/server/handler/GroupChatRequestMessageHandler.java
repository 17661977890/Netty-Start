package com.netty.chat.server.handler;

import com.netty.chat.message.GroupChatRequestMessage;
import com.netty.chat.message.GroupChatResponseMessage;
import com.netty.chat.server.session.GroupSession;
import com.netty.chat.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * 群聊---管理器
 */
@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {

        final GroupSession groupSession = GroupSessionFactory.getGroupSession();
        final List<Channel> channelList = groupSession.getMembersChannel(msg.getGroupName());

        for (Channel  channel : channelList){

            channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(),msg.getContent()));

        }

    }

}

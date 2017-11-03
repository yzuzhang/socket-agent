package com.feicent.agent.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feicent.agent.entity.AgentEntity;

/**
 * 业务处理类
 * @author yzuzhang
 * @date 2017年10月29日 下午4:50:36
 */
public class NettyServerHandler extends ChannelHandlerAdapter{
	protected static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object evt)throws Exception {
		if(evt instanceof AgentEntity) {
			AgentEntity entity = (AgentEntity)evt;
			System.out.println("接收到Client消息--->" + entity);
		}
		
		Channel incoming = ctx.channel();
		//业务处理
		ctx.channel().write(evt.toString());
		ctx.channel().writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "]" + evt.toString()));
	}
	
	/**
	 * channel被激活时调用(Client连接时调用)
	 */
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("客户端["+ ctx.channel().remoteAddress() +"]成功建立连接...");
		try {
			ctx.channel().writeAndFlush("恭喜你,连接成功...");
			System.out.println("客户端连接成功...");
		} catch (Exception e) {
			System.err.println(ctx.channel().remoteAddress() + "连接失败");
		}
	}

	/**
	 * 心跳监听
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state().equals(IdleState.READER_IDLE)) { //读超时
				// 超时关闭channel
				ctx.close();
			} else if (event.state().equals(IdleState.WRITER_IDLE)) { //写超时
				// 超时关闭channel
				ctx.close();
			} else if (event.state().equals(IdleState.ALL_IDLE)) { //
				// 发送心跳
				ctx.channel().writeAndFlush("ping\n");
			}
		}
		super.userEventTriggered(ctx, evt);
	}

	/**
	 * Client失去连接时调用
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)throws Exception {
		if(ctx.channel().isOpen() && ctx.channel().isActive() ){
			System.out.println("客户端["+ ctx.channel().remoteAddress()+"]连接已关闭>>>>");
			ctx.close();
		}
	}

}

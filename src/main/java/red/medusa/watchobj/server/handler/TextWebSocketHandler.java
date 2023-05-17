package red.medusa.watchobj.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import red.medusa.watchobj.core.web.PropertyValueChanelEventHandler;
import red.medusa.watchobj.core.Logger;

/**
 * 处理 WebService Server 事件,交互 MutableJson 事件
 *
 * @author GHHu
 * @date 2023/5/17
 * @see red.medusa.watchobj.core.MutableJson
 * @see red.medusa.watchobj.core.MutableJsonService
 */
public class TextWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    static final PropertyValueChanelEventHandler handler = new PropertyValueChanelEventHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String text = msg.text();
        Channel channel = ctx.channel();
        ctx.executor().submit(() -> handler.writeDataToChannel(text, channel));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Logger.debug("handlerAdded: " + ctx.channel());
        handler.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // System.out.println("handlerAdded:" + ctx.channel().id().asLongText());
        handler.remove(ctx.channel());
        Logger.debug("移除Channel -> " + ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
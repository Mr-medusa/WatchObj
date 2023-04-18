package red.medusa.watchobj.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            byte[] htmlContentBytes = null;
            Object[] contentTypeAndIsAssets = contentType(msg.uri());
            if (msg.method() == HttpMethod.GET && msg.uri().equals("/websocket.html")) {
                htmlContentBytes = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("websocket.html").toURI()));
            } else if (msg.method() == HttpMethod.GET) {
                URL systemResource;
                if ((boolean) contentTypeAndIsAssets[1]) {
                    systemResource = ClassLoader.getSystemResource("dist/" + msg.uri());
                } else {
                    systemResource = ClassLoader.getSystemResource("dist/index.html");
                }
                htmlContentBytes = Files.readAllBytes(Paths.get(systemResource.toURI()));
            }
            if (htmlContentBytes == null) {
                return;
            }
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set("content-type", contentTypeAndIsAssets[0]);
            StringBuilder bufResponse = new StringBuilder();
            bufResponse.append(new String(htmlContentBytes, StandardCharsets.UTF_8));
            ByteBuf buffer = Unpooled.copiedBuffer(bufResponse, CharsetUtil.UTF_8);
            response.content().writeBytes(buffer);
            buffer.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object[] contentType(String uri) {
        int lastIndexOf = uri.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return new Object[]{"text/html; charset=UTF-8", false};
        }
        String substring = uri.substring(lastIndexOf + 1);
        switch (substring) {
            case "gif":
                return new Object[]{"image/gif", true};
            case "png":
                return new Object[]{"image/png", true};
            case "jpg":
                return new Object[]{"image/jpg", true};
            case "jpeg":
                return new Object[]{"image/jpeg", true};
            case "icon":
                return new Object[]{"image/x-icon", true};
            case "css":
                return new Object[]{"text/css", true};
            case "js":
                return new Object[]{"application/javascript", true};
            default:
                return new Object[]{"text/html; charset=UTF-8", false};
        }
    }
}
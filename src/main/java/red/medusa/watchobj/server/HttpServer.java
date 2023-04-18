package red.medusa.watchobj.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import red.medusa.watchobj.core.Logger;
import red.medusa.watchobj.server.handler.HttpServerHandler;
import red.medusa.watchobj.server.handler.TextWebSocketHandler;

public class HttpServer {
    public final int httpServerPost;
    public final int websocketServerPost;

    public HttpServer(int httpServerPost, int websocketServerPost) {
        this.httpServerPost = httpServerPost;
        this.websocketServerPost = websocketServerPost;
    }

    public void bind() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (httpServerPost == ch.localAddress().getPort()) {
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new HttpObjectAggregator(61024));
                                pipeline.addLast(new HttpServerHandler());
                            } else {
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new ChunkedWriteHandler());
                                pipeline.addLast(new HttpObjectAggregator(8192));
                                pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                                pipeline.addLast(new TextWebSocketHandler());
                            }
                        }
                    });
            sb.bind(httpServerPost).sync().channel().closeFuture().addListener(ChannelFutureListener.CLOSE);
            sb.bind(websocketServerPost).sync().channel().closeFuture().addListener(ChannelFutureListener.CLOSE);
            if (Logger.isDebug()) {
                Logger.debug("Server started with http port: " + httpServerPost + " and websocket port " + websocketServerPost);
                Logger.debug("http://localhost:" + httpServerPost);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new HttpServer(8888, 9999).bind();
    }
}





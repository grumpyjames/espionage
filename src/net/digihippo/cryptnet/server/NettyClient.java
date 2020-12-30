package net.digihippo.cryptnet.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.digihippo.cryptnet.roadmap.LatLn;

public class NettyClient {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 7890;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch)
                {
                    ch.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(1024, 0, 2),
                            new LengthFieldPrepender(2, false),
                            new EspionageHandler());

                    ch.pipeline().addLast(new EspionageHandler());
                }
            });

            ChannelFuture f = b.connect(host, port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static class EspionageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx)
        {
            ClientToServer clientToServer = ProtocolV1.clientToServer(byteBuf ->
            {
                ByteBuf buffer = ctx.alloc().buffer();
                byteBuf.accept(buffer);
                ctx.writeAndFlush(buffer);
            });

            clientToServer.onLocation(new LatLn(0.67D, 0.32D));
            clientToServer.requestGame();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ((ByteBuf) msg).release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}

package net.digihippo.cryptnet.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.digihippo.cryptnet.roadmap.LatLn;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyServer {
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        final AtomicInteger clientCounter = new AtomicInteger(0);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        int onlyOneThread = 1;
        EventLoopGroup workerGroup = new NioEventLoopGroup(onlyOneThread);
        try {
            ServerBootstrap publicBootstrap = new ServerBootstrap();
            publicBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                        {
                            int clientId = clientCounter.getAndIncrement();
                            System.out.println("Client " + clientId + " connected");
                            ch.pipeline()
                                    .addLast(
                                            new LengthFieldBasedFrameDecoder(1024, 0, 2),
                                            new DiscardServerHandler(clientId, new ProtocolV1()));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Bootstrap timeBootstrap = new Bootstrap();
            timeBootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<>()
                    {
                        @Override
                        public void initChannel(Channel ch)
                        {
                            int clientId = clientCounter.getAndIncrement();
                            System.out.println("Client " + clientId + " connected");
                            ch.pipeline().addLast(new UdpHandler());
                        }
                    });


            ChannelFuture publicFuture = publicBootstrap.bind(port).sync();
            ChannelFuture timeFuture = timeBootstrap.bind("127.0.0.1", port + 1).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            publicFuture.channel().closeFuture().sync();
            timeFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 7890;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new NettyServer(port).run();
    }

    private static class UdpHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            DatagramPacket packet = (DatagramPacket) msg;
            ByteBuf message = packet.content();
            byte[] bytes = new byte[message.readableBytes()];
            message.readBytes(bytes);

            System.out.println(Thread.currentThread().getName() + ": UDP received: " + new String(bytes, StandardCharsets.UTF_8));

            packet.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // We basically never want this to close?
            cause.printStackTrace();
            ctx.close();
        }
    }

    private static class DiscardServerHandler extends ChannelInboundHandlerAdapter {
        private final int clientId;
        private final ProtocolV1 protocolV1;
        private final ClientIdPrinter printer = new ClientIdPrinter();

        public DiscardServerHandler(int clientId, ProtocolV1 protocolV1)
        {
            this.clientId = clientId;
            this.protocolV1 = protocolV1;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf message = (ByteBuf) msg;
            message.readShort(); // evict the length
            protocolV1.dispatch(message, printer);
            message.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        private class ClientIdPrinter implements ClientToServer
        {
            @Override
            public void onLocation(LatLn location)
            {
                System.out.println("Client " + clientId + ": onLocation " + location);
            }

            @Override
            public void requestGame()
            {
                System.out.println("Client " + clientId + ": requestGame");
            }

            @Override
            public void startGame(String gameId)
            {
                System.out.println("Client " + clientId + ": startGame " + gameId);
            }

            @Override
            public void quit()
            {
                System.out.println("Client " + clientId + ": quit");
            }
        }
    }
}

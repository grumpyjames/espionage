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
import io.netty.handler.codec.LengthFieldPrepender;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.OsmSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NettyServer {

    public static Stoppable runServer(
            int port,
            VectorSource vectorSource,
            StayAliveRules rules) throws Exception
    {
        ScheduledExecutorService pulseThread =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("pulse"));
        pulseThread.scheduleAtFixedRate(PulseClient.defaultPulseClient(), 0, 20, TimeUnit.MILLISECONDS);

        int onlyOneThread = 1;
        EventLoopGroup bossGroup = new NioEventLoopGroup(onlyOneThread, new NamedThreadFactory("acceptor"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(onlyOneThread, new NamedThreadFactory("event-loop-main"));

        ExecutorService gamePrepThread = Executors.newSingleThreadExecutor(new NamedThreadFactory("game-prep"));
        GameIndex gameIndex =
                new GameIndex(gamePrepThread, vectorSource, workerGroup, rules, System.currentTimeMillis());

        ServerBootstrap publicBootstrap = new ServerBootstrap();
        publicBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                    {
                        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(
                                ProtocolV1.serverToClient(new ChannelMessageSender(ch)));
                        ch.pipeline()
                                .addLast(
                                        new LengthFieldBasedFrameDecoder(1024, 0, 4),
                                        new LengthFieldPrepender(4, false),
                                        new ClientHandler(clientToServer, new ProtocolV1()));
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
                        ch.pipeline().addLast(new UdpHandler(gameIndex));
                    }
                });


        publicBootstrap.bind(port).sync();
        timeBootstrap.bind("127.0.0.1", port + 1).sync();

        return () ->
        {
            System.out.println("Shutting down");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            gamePrepThread.shutdown();
            pulseThread.shutdown();
        };
    }

    public static void main(String[] args) throws Exception {
        int port = 7890;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Stoppable stoppable =
                NettyServer.runServer(
                        port,
                        OsmSource::fetchWays,
                        new StayAliveRules(
                                Integer.getInteger("sentry.count"),
                                Double.parseDouble(System.getProperty("sentry.distance")),
                                Double.parseDouble(System.getProperty("sentry.speed")),
                                Integer.getInteger("game.duration.millis")));
        Runtime.getRuntime().addShutdownHook(new Thread(stoppable::stop));
    }

    private static class UdpHandler extends ChannelInboundHandlerAdapter {
        private final GameIndex gameIndex;

        public UdpHandler(GameIndex gameIndex)
        {
            this.gameIndex = gameIndex;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            DatagramPacket packet = (DatagramPacket) msg;
            gameIndex.tick(System.currentTimeMillis());
            packet.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // We basically never want this to close?
            cause.printStackTrace();
            ctx.close();
        }
    }

    private static class ClientHandler
            extends ChannelInboundHandlerAdapter {
        private final GameIndex.LocalClientToServer inbound;
        private final ProtocolV1 protocolV1;

        public ClientHandler(
                GameIndex.LocalClientToServer inbound,
                ProtocolV1 protocolV1)
        {
            this.inbound = inbound;
            this.protocolV1 = protocolV1;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf message = (ByteBuf) msg;
            message.readInt(); // evict the length
            protocolV1.dispatch(message, inbound);
            message.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception
        {
            inbound.sessionEnded();
            super.channelInactive(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
        {
            super.channelUnregistered(ctx);
        }
    }

    private static final class ChannelMessageSender implements ProtocolV1.MessageSender
    {
        private final SocketChannel ch;

        public ChannelMessageSender(SocketChannel ch)
        {
            this.ch = ch;
        }

        @Override
        public void withByteBuf(Consumer<ByteBuf> byteBuf)
        {
            ByteBuf buffer = ch.alloc().buffer();
            byteBuf.accept(buffer);
            ch.writeAndFlush(buffer);
        }
    }
}

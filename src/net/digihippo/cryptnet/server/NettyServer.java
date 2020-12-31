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
import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.OsmSource;
import net.digihippo.cryptnet.roadmap.Way;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class NettyServer {

    private static final class GameIndex
    {
        private final AtomicInteger gameId = new AtomicInteger(0);
        private final Map<String, Model> games = new HashMap<>();

        public String registerGame(Model model)
        {
            String gameId = "game-" + this.gameId.getAndIncrement();

            games.put(gameId, model);

            return gameId;
        }

        public void tick(long timeMillis)
        {
            games.values().forEach(m -> m.time(timeMillis));
        }
    }

    interface VectorSource
    {
        Collection<Way> fetchWays(LatLn.BoundingBox boundingBox) throws IOException;
    }

    private static final class GamePrep
    {
        private final ExecutorService executor;
        private final VectorSource vectorSource;
        private final StayAliveRules rules;

        private GamePrep(
                ExecutorService executor,
                VectorSource vectorSource,
                StayAliveRules rules)
        {
            this.executor = executor;
            this.vectorSource = vectorSource;
            this.rules = rules;
        }

        void prepareGame(LatLn latln, BiConsumer<Model, FrameDispatcher> modelReady)
        {
            executor.execute(() ->
            {
                try
                {
                    LatLn.BoundingBox boundingBox = latln.boundingBox(1_000);
                    Collection<Way> ways = vectorSource.fetchWays(boundingBox);
                    FrameDispatcher dispatcher = new FrameDispatcher();
                    Model model = Model.createModel(
                            Paths.from(ways),
                            rules,
                            new Random(),
                            new FrameCollector(dispatcher));
                    modelReady.accept(model, dispatcher);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    private static final class FrameDispatcher implements FrameConsumer
    {
        private final List<ServerToClient> clients = new ArrayList<>();

        public void subscribe(ServerToClient client)
        {
            this.clients.add(client);
        }

        @Override
        public void gameStarted()
        {
            clients.forEach(ServerToClient::gameStarted);
        }

        @Override
        public void onFrame(FrameCollector.Frame frame)
        {
            clients.forEach(c -> c.onFrame(frame));
        }
    }

    public static Stoppable runServer(
            int port,
            VectorSource vectorSource,
            StayAliveRules rules) throws Exception
    {
        ExecutorService gamePrepThread = Executors.newSingleThreadExecutor(new NamedThreadFactory("game-prep"));
        GamePrep gamePrep = new GamePrep(gamePrepThread, vectorSource, rules);

        ScheduledExecutorService pulseThread =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("pulse"));
        pulseThread.scheduleAtFixedRate(PulseClient.defaultPulseClient(), 0, 20, TimeUnit.MILLISECONDS);

        GameIndex gameIndex = new GameIndex();

        AtomicInteger clientCounter = new AtomicInteger(0);
        int onlyOneThread = 1;
        EventLoopGroup bossGroup = new NioEventLoopGroup(onlyOneThread, new NamedThreadFactory("acceptor"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(onlyOneThread, new NamedThreadFactory("event-loop-main"));

        ServerBootstrap publicBootstrap = new ServerBootstrap();
        publicBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                    {
                        int clientId = clientCounter.getAndIncrement();
                        ch.pipeline()
                                .addLast(
                                        new LengthFieldBasedFrameDecoder(1024, 0, 4),
                                        new LengthFieldPrepender(4, false),
                                        new DiscardServerHandler(
                                                workerGroup,
                                                clientId,
                                                new ProtocolV1(),
                                                gamePrep,
                                                gameIndex));
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

        Stoppable stoppable = NettyServer.runServer(port, OsmSource::fetchWays, new StayAliveRules(4, 250, 1.3, 30_000));
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

    private static class DiscardServerHandler extends ChannelInboundHandlerAdapter implements ClientToServer {
        private final EventLoopGroup elg;
        private final int clientId;
        private final ProtocolV1 protocolV1;
        private final GamePrep gamePrep;
        private final GameIndex gameIndex;

        private Model model;
        private boolean gamePreparing = false;
        private LatLn location = null;
        private ServerToClient response;

        public DiscardServerHandler(
                EventLoopGroup elg,
                int clientId,
                ProtocolV1 protocolV1,
                GamePrep gamePrep,
                GameIndex gameIndex)
        {
            this.elg = elg;
            this.clientId = clientId;
            this.protocolV1 = protocolV1;
            this.gamePrep = gamePrep;
            this.gameIndex = gameIndex;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (this.response == null)
            {
                // FIXME: surely there's a better way?
                this.response = ProtocolV1.serverToClient(byteBuf ->
                {
                    ByteBuf buffer = ctx.alloc().buffer();
                    byteBuf.accept(buffer);
                    ctx.writeAndFlush(buffer).syncUninterruptibly();
                });
            }

            ByteBuf message = (ByteBuf) msg;
            message.readInt(); // evict the length
            protocolV1.dispatch(message, this);
            message.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void onLocation(LatLn location)
        {
            if (model != null)
            {
                model.setPlayerLocation(location);
            }
            this.location = location;
        }

        @Override
        public void requestGame()
        {
            // reject if in game.
            // otherwise queue up a game request (it'll have to be done asynchronously)
            // How do we do async work and then get called back on the event loop?!
            if (model == null && location != null && !gamePreparing)
            {
                gamePreparing = true;
                gamePrep.prepareGame(location, (m, f) -> elg.submit(() ->
                {
                    System.out.println("Callback received...");
                    String gameId = gameIndex.registerGame(m);
                    this.model = m;
                    this.model.setPlayerLocation(location);
                    response.gameReady(gameId, m.parameters());
                    f.subscribe(this.response);
                }));
            }
        }

        @Override
        public void startGame(String gameId)
        {
            if (model != null)
            {
                model.startGame(System.currentTimeMillis());
            }
        }

        @Override
        public void quit()
        {
            // ???
        }
    }
}

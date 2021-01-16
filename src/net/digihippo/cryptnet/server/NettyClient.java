package net.digihippo.cryptnet.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.concurrent.CompletableFuture;

public final class NettyClient implements Stoppable, ClientToServer {
    private final EventLoopGroup workerGroup;
    private final ClientToServer clientToServer;

    public static void main(String[] args) throws Exception {
        final CompletableFuture<String> gameIdFut = new CompletableFuture<>();
        NettyClient client = NettyClient.connect(new ServerToClient()
        {
            @Override
            public void gameReady(String gameId, GameParameters gameParameters)
            {
                gameIdFut.complete(gameId);
                System.out.println("Game " + gameId + " is ready with parameters " + gameParameters);
            }

            @Override
            public void gameStarted()
            {
                System.out.println("The game is afoot");
            }

            @Override
            public void onFrame(FrameCollector.Frame frame)
            {
                System.out.println("Frame " + frame.gameOver + ", " + frame.victory);
            }

            @Override
            public void sessionEstablished(String sessionKey)
            {
                System.out.println("Session established: " + sessionKey);
            }

            @Override
            public void error(String errorCode)
            {
                System.out.println("Error " + errorCode);
            }
        });

        client.newSession();
        client.onLocation(new LatLn(0.67D, 0.32D));
        client.requestGame();
        client.startGame(gameIdFut.get());
        client.stop();
    }

    public static NettyClient connect(ServerToClient serverToClient) throws InterruptedException
    {
        String host = "localhost";
        int port = 7890;
        EventLoopGroup workerGroup = new NioEventLoopGroup(1, new NamedThreadFactory("client-loop"));

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch)
            {
                ch.pipeline().addLast(
                        new LengthFieldBasedFrameDecoder(65536, 0, 4),
                        new LengthFieldPrepender(4, false),
                        new EspionageHandler(serverToClient));

                ch.pipeline().addLast(new EspionageHandler(serverToClient));
            }
        });

        ChannelFuture f = b.connect(host, port).sync();
        return new NettyClient(workerGroup, ProtocolV1.clientToServer(byteBuf ->
        {
            ByteBuf buffer = f.channel().alloc().buffer();
            byteBuf.accept(buffer);
            f.channel().writeAndFlush(buffer).syncUninterruptibly();
        }));
    }

    public void newSession()
    {
        clientToServer.newSession();
    }

    public void resumeSession(String sessionId)
    {
        clientToServer.resumeSession(sessionId);
    }

    @Override
    public void onLocation(LatLn location)
    {
        clientToServer.onLocation(location);
    }

    @Override
    public void requestGame()
    {
        clientToServer.requestGame();
    }

    @Override
    public void startGame(String gameId)
    {
        clientToServer.startGame(gameId);
    }

    @Override
    public void quit()
    {
        clientToServer.quit();
    }

    @Override
    public void stop()
    {
        workerGroup.shutdownGracefully();
    }

    private NettyClient(EventLoopGroup workerGroup, ClientToServer clientToServer)
    {
        this.workerGroup = workerGroup;
        this.clientToServer = clientToServer;
    }

    private static class EspionageHandler extends ChannelInboundHandlerAdapter {

        private final ServerToClient serverToClient;
        private final ProtocolV1 protocolV1 = new ProtocolV1();

        public EspionageHandler(ServerToClient serverToClient)
        {
            this.serverToClient = serverToClient;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byteBuf.readInt(); // evict the length

            protocolV1.dispatch(byteBuf, serverToClient);

            byteBuf.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}

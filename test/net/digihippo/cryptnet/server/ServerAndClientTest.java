package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class ServerAndClientTest
{
    @Test
    @Ignore("slow, and not a unit - asks the internet for 'ways'")
    public void endToEnd() throws Exception
    {
        Stoppable server = NettyServer.runServer(7890);
        final CompletableFuture<String> gameIdFut = new CompletableFuture<>();
        final CompletableFuture<FrameCollector.Frame> frameFut = new CompletableFuture<>();
        NettyClient client = NettyClient.connect(new ServerToClient()
        {
            @Override
            public void gameReady(String gameId, GameParameters gameParameters)
            {
                System.err.println("Game " + gameId + " with params " + gameParameters);
                gameIdFut.complete(gameId);
            }

            @Override
            public void gameStarted()
            {
                System.out.println("Game is afoot");
            }

            @Override
            public void onFrame(FrameCollector.Frame frame)
            {
                System.out.println("Frame: " + frame);
                if (frame.gameOver || frame.victory)
                {
                    frameFut.complete(frame);
                }
            }
        });

        LatLn hampstead = LatLn.toRads(51.556615299043486, -0.17851485725770533);
        client.onLocation(hampstead);
        client.requestGame();
        String gameId = gameIdFut.get();
        client.startGame(gameId);

        FrameCollector.Frame f = frameFut.get();

        client.stop();
        server.stop();
    }
}

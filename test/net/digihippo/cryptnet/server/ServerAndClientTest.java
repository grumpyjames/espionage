package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.Way;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;

public class ServerAndClientTest
{
    public static NettyServer.VectorSource fixedWays()
    {
        return boundingBox ->
        {
            Node hampstead = Node.node(0, LatLn.toRads(51.5566285998552, -0.17851675163245212));
            Way hollyRise = Way.way(
                    hampstead,
                    Node.node(1, LatLn.toRads(51.5572764631672, -0.17934676084403262)),
                    Node.node(2, LatLn.toRads(51.55775043524266, -0.17966703613763157)),
                    Node.node(3, LatLn.toRads(51.55791309908667, -0.17979334191299526))
            );
            Node perrinsLaneWest = Node.node(6, LatLn.toRads(51.555170172455234, -0.17828669469775046));
            Way fitzjohn = Way.way(
                    hampstead,
                    Node.node(4, LatLn.toRads(51.555975117976885, -0.1787468085463757)),
                    Node.node(5, LatLn.toRads(51.55561331495947, -0.17858441543318676)),
                    perrinsLaneWest
            );
            Node perrinsLaneEast = Node.node(8, LatLn.toRads(51.55584329859152, -0.17684320041674095));
            Way perrinsLane = Way.way(
                    perrinsLaneWest,
                    Node.node(7, LatLn.toRads(51.555725502236776, -0.17691537513079147)),
                    perrinsLaneEast
            );

            Way rosslyn = Way.way(
                    hampstead,
                    Node.node(8, LatLn.toRads(51.55619668583993, -0.1777814717122849)),
                    perrinsLaneEast
            );

            Way heathSt = Way.way(
                    hampstead,
                    Node.node(9, LatLn.toRads(51.55724929237607, -0.17825671007556068)),
                    Node.node(10, LatLn.toRads(51.55791677865633, -0.17860405088692857)),
                    Node.node(11, LatLn.toRads(51.558623518174926, -0.17862660548848852))
            );


            return Way.ways(
                    hollyRise,
                    fitzjohn,
                    perrinsLane,
                    rosslyn,
                    heathSt
            );
        };
    }

    @Test
    public void endToEnd() throws Exception
    {
        Stoppable server = NettyServer.runServer(7890, fixedWays(), new StayAliveRules(4, 250, 1.3, 100));
        try
        {
            CompletableFuture<String> gameIdFut = new CompletableFuture<>();
            CompletableFuture<FrameCollector.Frame> frameFut = new CompletableFuture<>();
            NettyClient client = NettyClient.connect(new ServerToClient()
            {
                @Override
                public void gameReady(String gameId, GameParameters gameParameters)
                {
                    gameIdFut.complete(gameId);
                }

                @Override
                public void gameStarted()
                {

                }

                @Override
                public void onFrame(FrameCollector.Frame frame)
                {
                    if (frame.gameOver || frame.victory)
                    {
                        frameFut.complete(frame);
                    }
                }
            });
            try
            {
                LatLn hampstead = LatLn.toRads(51.556615299043486, -0.17851485725770533);
                client.onLocation(hampstead);
                client.requestGame();
                String gameId = gameIdFut.get();
                client.startGame(gameId);

                FrameCollector.Frame f = frameFut.get();
                assertTrue(f.victory);
            }
            finally
            {

                client.stop();
            }
        }
        finally
        {
            server.stop();
        }
    }
}

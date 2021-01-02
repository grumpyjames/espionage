package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.Way;
import org.hamcrest.CoreMatchers;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertTrue;

public class ServerAndClientTest
{
    private final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final MyServerToClient serverToClient = new MyServerToClient(events);

    @Test
    public void playOneGame() throws Exception
    {
        withClient(this::playAGame);
    }

    @Test
    public void playTwoGamesConsecutively() throws Exception
    {
        withClient(c -> {
            playAGame(c);
            playAGame(c);
        });
    }

    private void withClient(final Consumer<NettyClient> action) throws Exception
    {
        Stoppable server = NettyServer.runServer(7890, fixedWays(), new StayAliveRules(4, 250, 1.3, 100));
        try
        {
            NettyClient client = NettyClient.connect(serverToClient);
            try
            {
                action.accept(client);
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

    private void playAGame(NettyClient client)
    {
        LatLn hampstead = LatLn.toRads(51.556615299043486, -0.17851485725770533);
        client.onLocation(hampstead);
        client.requestGame();
        OnGameReady onGameReady = waitFor(events, any(OnGameReady.class));
        client.startGame(onGameReady.gameId);

        OnFrame frame = waitFor(events,
                new FeatureMatcher<>(CoreMatchers.is(true), "finished", "finished")
                {
                    @Override
                    protected Boolean featureValueOf(OnFrame onFrame)
                    {
                        return onFrame.frame.victory || onFrame.frame.gameOver;
                    }
                });

        FrameCollector.Frame f = frame.frame;
        assertTrue(f.victory);
    }

    private static <T extends Event> T waitFor(LinkedBlockingQueue<Event> events, Matcher<T> matcher)
    {
        long now = System.nanoTime();
        long tooLate = now + TimeUnit.MILLISECONDS.toNanos(500);
        final List<Event> allTheEvents = new ArrayList<>();
        while (System.nanoTime() < tooLate)
        {
            ArrayList<Event> e = new ArrayList<>();
            events.drainTo(e);
            for (Event ev: e)
            {
                if (matcher.matches(ev))
                {
                    //noinspection unchecked
                    return (T) ev;
                }
            }
            allTheEvents.addAll(e);
        }
        throw new WaitTimeout("Did not receive event matching " + matcher + ". " +
                "Saw events: \n\t" + allTheEvents.stream().map(Object::toString).collect(Collectors.joining("\n\t")));
    }

    interface Event {}

    static class OnGameReady implements Event {
        public final String gameId;
        public final GameParameters gameParameters;

        OnGameReady(String gameId, GameParameters gameParameters)
        {
            this.gameId = gameId;
            this.gameParameters = gameParameters;
        }
    }
    static class OnGameStarted implements Event {

    }
    static class OnFrame implements Event {
        public final FrameCollector.Frame frame;

        OnFrame(FrameCollector.Frame frame)
        {
            this.frame = frame;
        }
    }

    private static class MyServerToClient implements ServerToClient
    {
        private final BlockingQueue<Event> queue;

        public MyServerToClient(BlockingQueue<Event> queue)
        {
            this.queue = queue;
        }

        @Override
        public void gameReady(String gameId, GameParameters gameParameters)
        {
            enqueue(new OnGameReady(gameId, gameParameters));
        }

        @Override
        public void gameStarted()
        {
            enqueue(new OnGameStarted());
        }

        @Override
        public void onFrame(FrameCollector.Frame frame)
        {
            enqueue(new OnFrame(frame));
        }

        private void enqueue(Event e)
        {
            try
            {
                queue.put(e);
            }
            catch (InterruptedException ie)
            {
                throw new RuntimeException(ie);
            }
        }
    }

    private static class WaitTimeout extends RuntimeException
    {
        public WaitTimeout(String msg)
        {
            super(msg);
        }
    }

    public static VectorSource fixedWays()
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
}

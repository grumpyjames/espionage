package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.hamcrest.CoreMatchers;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertTrue;

public class ServerAndClientTest
{
    private static final LatLn HAMPSTEAD = LatLn.toRads(51.556615299043486, -0.17851485725770533);

    public final @Rule TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private final MyServerToClient serverToClient = new MyServerToClient(events);
    private final List<Stoppable> stoppables = new ArrayList<>();

    public void startServer(StayAliveRules rules) throws Exception
    {
        stoppables.add(NettyServer.runServer(
                7890,
                FixedVectorSources.hampsteadWays(),
                rules,
                temporaryFolder.getRoot()));
    }

    @After
    public void stopEverything()
    {
        stoppables.forEach(Stoppable::stop);
    }

    @Test
    public void playOneGame() throws Exception
    {
        startServer(new StayAliveRules(4, 250, 1.3, 100));
        NettyClient nettyClient = newClient();
        playAGame(nettyClient);
    }

    @Test
    public void playTwoGamesConsecutively() throws Exception
    {
        startServer(new StayAliveRules(4, 250, 1.3, 100));
        NettyClient nettyClient = newClient();
        playAGame(nettyClient);
        playAGame(nettyClient);
    }

    @Test
    public void disconnectionCausesGamePauseAndResumesOnReconnection() throws Exception
    {
        startServer(new StayAliveRules(4, 250, 1.3, 1000));

        NettyClient nettyClient = newClient();
        nettyClient.newSession();
        SessionStarted sessionStarted = waitFor(events, any(SessionStarted.class), 500);
        nettyClient.onLocation(HAMPSTEAD);
        nettyClient.requestGame();
        OnGameReady onGameReady = waitFor(events, any(OnGameReady.class), 500);
        nettyClient.startGame(onGameReady.gameId);
        nettyClient.stop();

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        // Need to wait a couple of seconds here to prove the game pauses.
        // Might be a test for a level lower?

        NettyClient freshClient = newClient();
        freshClient.resumeSession(sessionStarted.sessionId);
        OnFrame frame = waitFor(events, new GameCompleteFrame(), 1500);
        FrameCollector.Frame f = frame.frame;
        assertTrue(f.victory);
    }

    @Test
    public void cannotStartAGameBeforeEstablishingASession() throws Exception
    {
        startServer(new StayAliveRules(4, 250, 1.3, 1000));

        NettyClient nettyClient = newClient();

        nettyClient.requestGame();

        waitFor(events, errorCode("SNE"), 100);
    }

    @Test
    public void loseOneGame() throws Exception
    {
        startServer(new StayAliveRules(4, 10, 4, 2500));
        NettyClient nettyClient = newClient();
        nettyClient.newSession();
        nettyClient.onLocation(HAMPSTEAD);
        nettyClient.requestGame();
        OnGameReady onGameReady = waitFor(events, any(OnGameReady.class), 500);
        nettyClient.startGame(onGameReady.gameId);

        waitFor(events, new GameOverFrame(), 2500);
    }

    private NettyClient newClient() throws Exception
    {
        NettyClient client = NettyClient.connect(serverToClient);
        stoppables.add(client);

        return client;
    }

    private void playAGame(NettyClient client)
    {
        client.newSession();
        client.onLocation(HAMPSTEAD);
        client.requestGame();
        OnGameReady onGameReady = waitFor(events, any(OnGameReady.class), 500);
        client.startGame(onGameReady.gameId);

        OnFrame frame = waitFor(events, new GameCompleteFrame(), 500);

        FrameCollector.Frame f = frame.frame;
        assertTrue(f.victory);
    }

    private static <T extends Event> T waitFor(
            LinkedBlockingQueue<Event> events,
            Matcher<T> matcher,
            int millisecondTimeout)
    {
        long now = System.nanoTime();
        long tooLate = now + TimeUnit.MILLISECONDS.toNanos(millisecondTimeout);
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

    private static final class OnGameReady implements Event {
        public final String gameId;

        OnGameReady(String gameId)
        {
            this.gameId = gameId;
        }
    }

    private static final class OnGameStarted implements Event {

    }

    private static final class OnFrame implements Event {
        public final FrameCollector.Frame frame;

        OnFrame(FrameCollector.Frame frame)
        {
            this.frame = frame;
        }
    }

    private static final class SessionStarted implements Event
    {
        public final String sessionId;

        private SessionStarted(String sessionId)
        {
            this.sessionId = sessionId;
        }
    }

    private static final class ErrorCode implements Event
    {
        private final String errorCode;

        public ErrorCode(String errorCode)
        {
            this.errorCode = errorCode;
        }

        @Override
        public String toString()
        {
            return "ErrorCode{" +
                    "errorCode='" + errorCode + '\'' +
                    '}';
        }
    }

    private static final class MyServerToClient implements ServerToClient
    {
        private final BlockingQueue<Event> queue;

        public MyServerToClient(BlockingQueue<Event> queue)
        {
            this.queue = queue;
        }

        @Override
        public void gameReady(String gameId)
        {
            enqueue(new OnGameReady(gameId));
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

        @Override
        public void sessionEstablished(String sessionKey)
        {
            enqueue(new SessionStarted(sessionKey));
        }

        @Override
        public void rules(StayAliveRules rules)
        {

        }

        @Override
        public void path(Path path)
        {

        }

        @Override
        public void error(String errorCode)
        {
            enqueue(new ErrorCode(errorCode));
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

    private static class GameOverFrame extends FeatureMatcher<OnFrame, Boolean>
    {
        public GameOverFrame()
        {
            super(CoreMatchers.is(true), "gameOver", "game over");
        }

        @Override
        protected Boolean featureValueOf(OnFrame onFrame)
        {
            return onFrame.frame.gameOver;
        }
    }

    private static class GameCompleteFrame extends FeatureMatcher<OnFrame, Boolean>
    {
        public GameCompleteFrame()
        {
            super(CoreMatchers.is(true), "finished", "finished");
        }

        @Override
        protected Boolean featureValueOf(OnFrame onFrame)
        {
            return onFrame.frame.victory || onFrame.frame.gameOver;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private Matcher<ErrorCode> errorCode(String errorCode)
    {
        return new FeatureMatcher<>(equalTo(errorCode), "error code", "error")
        {
            @Override
            protected String featureValueOf(ErrorCode errorCode)
            {
                return errorCode.errorCode;
            }
        };
    }
}

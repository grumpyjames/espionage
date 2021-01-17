package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class GameIndexTest
{
    private static final LatLn HAMPSTEAD = LatLn.toRads(51.556615299043486, -0.17851485725770533);

    public final @Rule TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final StayAliveRules rules = new StayAliveRules(8, 125, 1, 1000);
    private GameIndex gameIndex;

    @Before
    public void setup()
    {
        newGameIndex(rules);
    }

    @Test
    public void refuseResumptionOfInvalidSessionId()
    {
        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);

        clientToServer.resumeSession("n'existe pas");
        assertThat(serverToClient.lastError, equalTo(ErrorCodes.NO_SUCH_SESSION.code()));
    }

    @Test
    public void refuseResumptionOfValidButNotPresentSessionId()
    {
        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);

        clientToServer.resumeSession(UUID.randomUUID().toString());
        assertThat(serverToClient.lastError, equalTo(ErrorCodes.NO_SUCH_SESSION.code()));
    }

    @Test
    public void resumeThatGame()
    {
        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);
        clientToServer.newSession();

        clientToServer.onLocation(HAMPSTEAD);

        clientToServer.requestGame();
        clientToServer.startGame(serverToClient.lastGameId);

        gameIndex.tick(epochMilli("2021-01-16T14:50:01.211Z"));

        clientToServer.sessionEnded();

        // This is well past when the game should have ended by
        gameIndex.tick(epochMilli("2021-01-16T14:50:03.211Z"));

        GameIndex.LocalClientToServer resumption = gameIndex.newClient(serverToClient);
        resumption.resumeSession(serverToClient.lastSessionId);

        gameIndex.tick(epochMilli("2021-01-16T14:50:03.319Z"));
        assertFalse(serverToClient.lastFrame.victory || serverToClient.lastFrame.gameOver);

        gameIndex.tick(epochMilli("2021-01-16T14:50:04.319Z"));
        assertTrue(serverToClient.lastFrame.victory || serverToClient.lastFrame.gameOver);
    }

    @Test
    public void stopTickingOldGamesAfterLoss()
    {
        newGameIndex(new StayAliveRules(1, 50, 20, 10_000));

        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);
        clientToServer.newSession();

        clientToServer.onLocation(HAMPSTEAD);

        clientToServer.requestGame();
        clientToServer.startGame(serverToClient.lastGameId);

        gameIndex.tick(epochMilli("2021-01-16T14:50:07.111Z"));

        assertTrue(serverToClient.lastFrame.gameOver);

        serverToClient.lastFrame = null;

        gameIndex.tick(epochMilli("2021-01-16T14:50:10.111Z"));

        assertNull(serverToClient.lastFrame);
    }

    @Test
    public void stopTickingOldGamesAfterVictory()
    {
        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);
        clientToServer.newSession();

        clientToServer.onLocation(HAMPSTEAD);

        clientToServer.requestGame();
        clientToServer.startGame(serverToClient.lastGameId);
        gameIndex.tick(epochMilli("2021-01-16T14:50:02.111Z"));

        assertTrue(serverToClient.lastFrame.victory);

        serverToClient.lastFrame = null;

        gameIndex.tick(epochMilli("2021-01-16T14:50:02.111Z"));

        assertNull(serverToClient.lastFrame);
    }

    @Test
    public void journallingIsGood()
    {
        AtomicInteger frameCount = new AtomicInteger();
        MyServerToClient serverToClient = new MyServerToClient();
        DelegatingServerToClient frameCounter = new FrameCounter(serverToClient, frameCount);

        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(frameCounter);
        clientToServer.newSession();

        clientToServer.onLocation(HAMPSTEAD);

        clientToServer.requestGame();
        clientToServer.startGame(serverToClient.lastGameId);
        gameIndex.tick(epochMilli("2021-01-16T14:50:02.111Z"));

        assertTrue(serverToClient.lastFrame.victory);

        //noinspection ConstantConditions
        File journal = temporaryFolder.getRoot().listFiles()[0];
        AtomicInteger journalFrameCount = new AtomicInteger();
        DelegatingServerToClient journalFrameCounter = new FrameCounter(serverToClient, journalFrameCount);
        Journal.playJournal(journal, journalFrameCounter);

        assertEquals(frameCount.get(), journalFrameCount.get());
    }

    private void newGameIndex(StayAliveRules rules)
    {
        this.gameIndex = new GameIndex(
                Runnable::run,
                FixedVectorSources.hampsteadWays(),
                Runnable::run,
                rules,
                temporaryFolder.getRoot(),
                epochMilli("2021-01-16T14:50:01.011Z")
        );
    }

    private static final class MyServerToClient implements ServerToClient
    {
        private String lastGameId;
        private String lastSessionId;
        private FrameCollector.Frame lastFrame;
        private String lastError;

        @Override
        public void gameReady(String gameId)
        {
            this.lastGameId = gameId;
        }

        @Override
        public void gameStarted()
        {

        }

        @Override
        public void onFrame(FrameCollector.Frame frame)
        {
            this.lastFrame = frame;
        }

        @Override
        public void sessionEstablished(String sessionKey)
        {
            this.lastSessionId = sessionKey;
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
            this.lastError = errorCode;
        }
    }

    private static long epochMilli(String instant)
    {
        return Instant.parse(instant).toEpochMilli();
    }

    private static class FrameCounter extends DelegatingServerToClient
    {
        private final AtomicInteger frameCount;

        public FrameCounter(MyServerToClient serverToClient, AtomicInteger frameCount)
        {
            super(serverToClient);
            this.frameCount = frameCount;
        }

        @Override
        public void onFrame(FrameCollector.Frame frame)
        {
            frameCount.incrementAndGet();
            super.onFrame(frame);
        }
    }
}
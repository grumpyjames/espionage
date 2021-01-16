package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class GameIndexTest
{
    private static final LatLn HAMPSTEAD = LatLn.toRads(51.556615299043486, -0.17851485725770533);

    private final StayAliveRules rules = new StayAliveRules(8, 125, 1, 1000);
    private GameIndex gameIndex = newGameIndex(rules);

    private GameIndex newGameIndex(StayAliveRules rules)
    {
        return new GameIndex(
                Runnable::run,
                FixedVectorSources.hampsteadWays(),
                Runnable::run,
                rules,
                epochMilli("2021-01-16T14:50:01.011Z")
                );
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
    public void stopTickingOldGamesAfterVictory()
    {
        gameIndex = newGameIndex(new StayAliveRules(1, 10, 1, 10_000));

        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);
        clientToServer.newSession();

        clientToServer.onLocation(HAMPSTEAD);

        clientToServer.requestGame();
        clientToServer.startGame(serverToClient.lastGameId);

        gameIndex.tick(epochMilli("2021-01-16T14:50:05.111Z"));

        assertTrue(serverToClient.lastFrame.gameOver);

        serverToClient.lastFrame = null;

        gameIndex.tick(epochMilli("2021-01-16T14:50:10.111Z"));

        assertNull(serverToClient.lastFrame);
    }

    @Test
    public void stopTickingOldGamesAfterLoss()
    {
        MyServerToClient serverToClient = new MyServerToClient();
        GameIndex.LocalClientToServer clientToServer = gameIndex.newClient(serverToClient);
        clientToServer.newSession();

        clientToServer.onLocation(HAMPSTEAD);

        clientToServer.requestGame();
        clientToServer.startGame(serverToClient.lastGameId);
//                                "2021-01-16T14:50:01.011Z"
        gameIndex.tick(epochMilli("2021-01-16T14:50:02.111Z"));

        assertTrue(serverToClient.lastFrame.victory);

        serverToClient.lastFrame = null;

        gameIndex.tick(epochMilli("2021-01-16T14:50:02.111Z"));

        assertNull(serverToClient.lastFrame);
    }

    private static final class MyServerToClient implements ServerToClient
    {
        private String lastGameId;
        private String lastSessionId;
        private FrameCollector.Frame lastFrame;
        private String lastError;

        @Override
        public void gameReady(String gameId, GameParameters gameParameters)
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
        public void error(String errorCode)
        {
            this.lastError = errorCode;
        }
    }

    private static long epochMilli(String instant)
    {
        return Instant.parse(instant).toEpochMilli();
    }
}
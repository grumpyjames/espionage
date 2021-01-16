package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameIndexTest
{
    private static final LatLn HAMPSTEAD = LatLn.toRads(51.556615299043486, -0.17851485725770533);

    @Test
    public void resumeThatGame()
    {
        GameIndex gameIndex = new GameIndex(
                Runnable::run,
                FixedVectorSources.hampsteadWays(),
                Runnable::run,
                new StayAliveRules(8, 125, 1, 1000),
                epochMilli("2021-01-16T14:50:01.011Z")
                );
        HmmServerToClient serverToClient = new HmmServerToClient();
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

    private static final class HmmServerToClient implements ServerToClient
    {
        public String lastGameId;
        public String lastSessionId;
        private FrameCollector.Frame lastFrame;

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
    }

    private static long epochMilli(String instant)
    {
        return Instant.parse(instant).toEpochMilli();
    }
}
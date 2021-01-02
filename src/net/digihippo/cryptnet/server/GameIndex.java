package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.model.Model;
import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

final class GameIndex
{
    private final AtomicInteger gameId = new AtomicInteger(0);
    private final Map<String, Model> games = new HashMap<>();
    private final GamePreparationService gamePreparationService;

    public GameIndex(GamePreparationService gamePreparationService)
    {
        this.gamePreparationService = gamePreparationService;
    }

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

    public ClientToServer newClient(ServerToClient serverToClient)
    {
        return new BidirectionalClient(serverToClient, this);
    }

    private static final class BidirectionalClient implements ClientToServer, ServerToClient
    {
        private final ServerToClient serverToClient;
        private final GameIndex gameIndex;
        private Model model = null;
        private LatLn location = null;
        private boolean gamePreparing = false;

        public BidirectionalClient(
                ServerToClient serverToClient,
                GameIndex gameIndex)
        {
            this.serverToClient = serverToClient;
            this.gameIndex = gameIndex;
        }

        // Not sure - could be telling us the outbound API isn't quite right?
        public void gameRequestComplete(String gameId, Model model)
        {
            this.model = model;
            this.gamePreparing = false;
            this.gameReady(gameId, model.parameters());
        }

        // In -->
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
            if (model == null && location != null && !gamePreparing)
            {
                gamePreparing = true;
                gameIndex.requestGame(this);
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

        }

        // <-- out
        @Override
        public void gameReady(String gameId, GameParameters gameParameters)
        {
            serverToClient.gameReady(gameId, gameParameters);
        }

        @Override
        public void gameStarted()
        {
            serverToClient.gameStarted();
        }

        @Override
        public void onFrame(FrameCollector.Frame frame)
        {
            if (frame.gameOver || frame.victory)
            {
                this.model = null;
                this.gamePreparing = false;
            }
            serverToClient.onFrame(frame);
        }
    }

    private void requestGame(BidirectionalClient bidirectionalClient)
    {
        final LatLn playerLocation = bidirectionalClient.location;
        gamePreparationService.prepareGame(playerLocation, (m, f) ->
        {
            String gameId = registerGame(m);
            bidirectionalClient.gameRequestComplete(gameId, m);
            m.setPlayerLocation(playerLocation);
            f.subscribe(bidirectionalClient);
        });
    }
}

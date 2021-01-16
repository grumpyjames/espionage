package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Way;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

final class GameIndex
{
    private final AtomicInteger gameId = new AtomicInteger(0);
    private final Map<String, Model> games = new HashMap<>();

    private final Executor offEventLoop;
    private final VectorSource vectorSource;
    private final Executor onEventLoop;
    private final StayAliveRules rules;

    public GameIndex(
            Executor offEventLoop,
            VectorSource vectorSource,
            Executor onEventLoop,
            StayAliveRules rules)
    {
        this.offEventLoop = offEventLoop;
        this.vectorSource = vectorSource;
        this.onEventLoop = onEventLoop;
        this.rules = rules;
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
        prepareGame(playerLocation, (m, f) ->
        {
            String gameId = registerGame(m);
            bidirectionalClient.gameRequestComplete(gameId, m);
            m.setPlayerLocation(playerLocation);
            f.subscribe(bidirectionalClient);
        });
    }

    // This is a potentially blocking operation
    // It needs to happen off event loop, and then call back to the event loop with results.
    private void prepareGame(LatLn latln, BiConsumer<Model, FrameDispatcher> modelReady)
    {
        offEventLoop.execute(() ->
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
                onEventLoop.execute(() -> modelReady.accept(model, dispatcher));
            }
            catch (IOException e)
            {
                // FIXME: tell the event loop about the failure
                e.printStackTrace();
            }
        });
    }
}

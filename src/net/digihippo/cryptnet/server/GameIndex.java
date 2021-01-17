package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Way;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

final class GameIndex
{
    private final AtomicInteger gameId = new AtomicInteger(0);
    private final Map<String, Model> games = new HashMap<>();

    private final Executor offEventLoop;
    private final VectorSource vectorSource;
    private final Executor onEventLoop;
    private final StayAliveRules rules;
    private final File baseDirectory;

    private final Map<UUID, Session> sessions = new HashMap<>();
    private long currentTimeMillis;


    public GameIndex(
            Executor offEventLoop,
            VectorSource vectorSource,
            Executor onEventLoop,
            StayAliveRules rules,
            File baseDirectory,
            long initialTimeMillis)
    {
        this.offEventLoop = offEventLoop;
        this.vectorSource = vectorSource;
        this.onEventLoop = onEventLoop;
        this.rules = rules;
        this.baseDirectory = baseDirectory;
        this.currentTimeMillis = initialTimeMillis;
    }

    public String registerGame(Model model)
    {
        String gameId = "game-" + this.gameId.getAndIncrement();

        games.put(gameId, model);

        return gameId;
    }

    public void tick(long timeMillis)
    {
        this.currentTimeMillis = timeMillis;
        games.values().forEach(m -> m.time(timeMillis));
    }

    interface LocalClientToServer extends ClientToServer
    {
        void sessionEnded();
    }

    public LocalClientToServer newClient(ServerToClient serverToClient)
    {
        return new SessionClient(serverToClient, this);
    }

    Optional<Session> resumeSession(String sessionId, ServerToClient serverToClient)
    {
        try
        {
            Session session = sessions.get(UUID.fromString(sessionId));
            if (session == null)
            {
                return Optional.empty();
            }

            session.resume(serverToClient);

            return Optional.of(session);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            return Optional.empty();
        }
    }

    final class Session implements ServerToClient
    {
        private ServerToClient serverToClient;

        private Model model;
        private FrameDispatcher frameDispatcher;
        private LatLn location;
        private boolean gamePreparing = false;

        private Session(
                ServerToClient serverToClient)
        {
            this.serverToClient = serverToClient;
        }

        @SuppressWarnings("unused")
        public void startGame(String gameId)
        {
            if (model != null)
            {
                model.startGame(GameIndex.this.currentTimeMillis);
            }
        }

        public void gameRequestComplete(
                String gameId,
                Model model,
                FrameDispatcher frameDispatcher)
        {
            this.model = model;
            this.frameDispatcher = frameDispatcher;
            this.gamePreparing = false;
            this.rules(model.parameters().rules);
            model.parameters().paths.forEach(this::path);
            this.gameReady(gameId);
        }

        public void onLocation(LatLn location)
        {
            if (model != null)
            {
                model.setPlayerLocation(location);
            }
            this.location = location;
        }

        public void requestGame()
        {
            if (model == null && location != null && !gamePreparing)
            {
                gamePreparing = true;
                GameIndex.this.requestGame(this);
            }
        }

        @Override
        public void gameReady(String gameId)
        {
            serverToClient.gameReady(gameId);
        }

        @Override
        public void rules(StayAliveRules rules)
        {
            serverToClient.rules(rules);
        }

        @Override
        public void path(Path path)
        {
            serverToClient.path(path);
        }

        @Override
        public void gameStarted()
        {
            serverToClient.gameStarted();
        }

        @Override
        public void onFrame(Frame frame)
        {
            if (frame.gameOver || frame.victory)
            {
                this.model = null;
                this.gamePreparing = false;
            }

            serverToClient.onFrame(frame);
        }

        @Override
        public void sessionEstablished(String sessionKey)
        {
            serverToClient.sessionEstablished(sessionKey);
        }

        @Override
        public void error(String errorCode)
        {
            serverToClient.error(errorCode);
        }

        public void ended()
        {
            if (this.model != null)
            {
                this.model.playerDisconnected();
                this.frameDispatcher.unsubscribe(this);
            }
        }

        public void resume(ServerToClient serverToClient)
        {
            this.serverToClient = serverToClient;
            if (this.model != null)
            {
                this.model.playerRejoined();
                this.frameDispatcher.subscribe(this);
            }
        }

        public void quit()
        {

        }
    }

    Session createSession(ServerToClient serverToClient)
    {
        UUID sessionKey = UUID.randomUUID();
        Session session = new Session(serverToClient);

        sessions.put(sessionKey, session);
        serverToClient.sessionEstablished(sessionKey.toString());

        return session;
    }

    private void requestGame(Session session)
    {
        final LatLn playerLocation = session.location;
        prepareGame(playerLocation, session);
    }

    private void gameReady(
            Session session,
            LatLn playerLocation,
            Model model,
            FrameDispatcher frameDispatcher)
    {
        String gameId = registerGame(model);
        ServerToClient journal = Journal.forGame(baseDirectory, gameId, currentTimeMillis);
        GameParameters parameters = model.parameters();
        journal.rules(parameters.rules);
        parameters.paths.forEach(journal::path);
        journal.gameReady(gameId);

        session.gameRequestComplete(gameId, model, frameDispatcher);

        model.setPlayerLocation(playerLocation);
        frameDispatcher.subscribe(session);
        frameDispatcher.subscribe(journal);
    }

    // This is a potentially blocking operation
    // It needs to happen off event loop, and then call back to the event loop with results.
    private void prepareGame(LatLn latln, Session session)
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
                onEventLoop.execute(() -> gameReady(session, latln, model, dispatcher));
            }
            catch (IOException e)
            {
                // TODO: We should record how often this is happening and why.
                // Users will be frustrated if it happens continually without explanation
                onEventLoop.execute(() -> session.error(ErrorCodes.GAME_REQUEST_FAILED.code()));
            }
        });
    }
}

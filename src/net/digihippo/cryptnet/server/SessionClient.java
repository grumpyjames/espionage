package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.Optional;
import java.util.function.Consumer;

final class SessionClient implements GameIndex.LocalClientToServer
{
    private final ServerToClient serverToClient;
    private final GameIndex gameIndex;

    private GameIndex.Session session;

    public SessionClient(
            ServerToClient serverToClient,
            GameIndex gameIndex)
    {
        this.serverToClient = serverToClient;
        this.gameIndex = gameIndex;
    }

    @Override
    public void newSession()
    {
        this.session = this.gameIndex.createSession(serverToClient);
    }

    @Override
    public void resumeSession(String sessionId)
    {
        Optional<GameIndex.Session> maybeSession = this.gameIndex.resumeSession(sessionId, serverToClient);
        if (maybeSession.isPresent())
        {
            this.session = maybeSession.get();
        }
        else
        {
            serverToClient.error(ErrorCodes.NO_SUCH_SESSION.code());
        }
    }

    @Override
    public void onLocation(LatLn location)
    {
        withSession(s -> s.onLocation(location));
    }

    @Override
    public void requestGame()
    {
        withSession(GameIndex.Session::requestGame);
    }

    @Override
    public void startGame(String gameId)
    {
        withSession(s -> s.startGame(gameId));
    }

    @Override
    public void resumeGame()
    {
        withSession(s -> s.resumeGame());
    }

    @Override
    public void quit()
    {
        withSession(GameIndex.Session::quit);
    }

    @Override
    public void sessionEnded()
    {
        withSession(GameIndex.Session::ended);
    }

    private void withSession(Consumer<GameIndex.Session> s)
    {
        if (this.session == null)
        {
            this.serverToClient.error(ErrorCodes.SESSION_NOT_ESTABLISHED.code());
        }
        else
        {
            s.accept(this.session);
        }
    }
}

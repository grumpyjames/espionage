package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.roadmap.LatLn;

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
        this.session = this.gameIndex.resumeSession(sessionId, serverToClient);
    }

    @Override
    public void onLocation(LatLn location)
    {
        if (this.session != null)
        {
            this.session.onLocation(location);
        }
    }

    @Override
    public void requestGame()
    {
        if (this.session != null)
        {
            this.session.requestGame();
        }
    }

    @Override
    public void startGame(String gameId)
    {
        if (this.session != null)
        {
            session.startGame(gameId);
        }
    }

    @Override
    public void quit()
    {
        if (this.session != null)
        {
            session.quit();
        }
    }

    @Override
    public void sessionEnded()
    {
        session.ended();
    }
}

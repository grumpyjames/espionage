package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.Frame;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.StayAliveRules;

public class DelegatingServerToClient implements ServerToClient
{
    private final ServerToClient delegate;

    public DelegatingServerToClient(ServerToClient delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void sessionEstablished(String sessionKey)
    {
        delegate.sessionEstablished(sessionKey);
    }

    @Override
    public void rules(StayAliveRules rules)
    {
        delegate.rules(rules);
    }

    @Override
    public void path(Path path)
    {
        delegate.path(path);
    }

    @Override
    public void gameReady(String gameId)
    {
        delegate.gameReady(gameId);
    }

    @Override
    public void gameStarted()
    {
        delegate.gameStarted();
    }

    @Override
    public void onFrame(Frame frame)
    {
        delegate.onFrame(frame);
    }

    @Override
    public void error(String errorCode)
    {
        delegate.error(errorCode);
    }
}

package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.Frame;
import net.digihippo.cryptnet.model.GameEvents;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.StayAliveRules;

import java.util.HashSet;
import java.util.Set;

final class FrameDispatcher implements GameEvents
{
    private final Set<ServerToClient> clients = new HashSet<>();

    public void subscribe(ServerToClient client)
    {
        clients.add(client);
    }

    @Override
    public void rules(StayAliveRules rules)
    {
        clients.forEach(c -> c.rules(rules));
    }

    @Override
    public void path(Path path)
    {
        clients.forEach(c -> c.path(path));
    }

    @Override
    public void gameReady(String gameId)
    {
        clients.forEach(c -> c.gameReady(gameId));
    }

    @Override
    public void gameStarted()
    {
        clients.forEach(ServerToClient::gameStarted);
    }

    @Override
    public void onFrame(Frame frame)
    {
        clients.forEach(c -> c.onFrame(frame));
    }

    public void unsubscribe(ServerToClient session)
    {
        clients.remove(session);
    }
}

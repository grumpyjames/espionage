package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.Frame;
import net.digihippo.cryptnet.model.FrameConsumer;

import java.util.HashSet;
import java.util.Set;

final class FrameDispatcher implements FrameConsumer
{
    private final Set<ServerToClient> clients = new HashSet<>();

    public void subscribe(ServerToClient client)
    {
        clients.add(client);
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

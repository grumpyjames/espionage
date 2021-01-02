package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.FrameConsumer;

import java.util.ArrayList;
import java.util.List;

final class FrameDispatcher implements FrameConsumer
{
    private final List<ServerToClient> clients = new ArrayList<>();

    public void subscribe(ServerToClient client)
    {
        this.clients.add(client);
    }

    @Override
    public void gameStarted()
    {
        clients.forEach(ServerToClient::gameStarted);
    }

    @Override
    public void onFrame(FrameCollector.Frame frame)
    {
        clients.forEach(c -> c.onFrame(frame));
    }
}

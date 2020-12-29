package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;

public interface ServerToClient
{
    void gameReady(String gameId, GameParameters gameParameters);

    void gameStarted(String gameId);

    void onFrame(FrameCollector.Frame frame);
}

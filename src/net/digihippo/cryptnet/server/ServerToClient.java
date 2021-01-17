package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.StayAliveRules;

public interface ServerToClient
{
    void sessionEstablished(String sessionKey);

    void rules(StayAliveRules rules);

    void path(Path path);

    void gameReady(String gameId);

    void gameStarted();

    void onFrame(FrameCollector.Frame frame);

    void error(String errorCode);
}

package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.GameEvents;

public interface ServerToClient extends GameEvents
{
    void sessionEstablished(String sessionKey);

    void error(String errorCode);
}

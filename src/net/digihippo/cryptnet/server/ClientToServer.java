package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.roadmap.LatLn;

public interface ClientToServer
{
    void newSession();

    void resumeSession(String sessionId);

    void onLocation(LatLn location);

    void requestGame();

    void startGame(String gameId);

    void resumeGame();

    void quit();
}

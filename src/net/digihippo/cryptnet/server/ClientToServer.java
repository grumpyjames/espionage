package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.roadmap.LatLn;

public interface ClientToServer
{
    void onLocation(LatLn location);

    void requestGame();

    void startGame(String gameId);

    void quit();
}

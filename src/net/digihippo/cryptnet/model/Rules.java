package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;

public interface Rules
{
    // metres / second
    double sentrySpeed();

    State gameState(long timeMillis, LatLn playerLocation, List<LatLn> sentryLocations);

    enum State
    {
        GameOver,
        Victory,
        Continue;
    }

    String gameType();
}

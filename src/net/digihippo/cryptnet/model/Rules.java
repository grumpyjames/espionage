package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;

public interface Rules
{
    int sentryCount();

    // metres
    double initialSentryDistance();

    // metres / second
    double sentrySpeed();

    State gameState(long timeMillis, LatLn playerLocation, List<LatLn> sentryLocations);

    int gameDuration();

    enum State
    {
        GameOver,
        Victory,
        Continue;
    }

    String gameType();
}

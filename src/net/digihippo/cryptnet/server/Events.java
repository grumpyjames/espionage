package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.roadmap.LatLn;

// Immutable values only, this is likely to become a thread hop.
public interface Events
{
    void gameRejected(
            String gameIdentifier,
            String message);

    void gameStarted(
            String gameIdentifier);

    void playerPositionChanged(
            String gameIdentifier,
            LatLn location);

    void sentryPositionChanged(
            String gameIdentifier,
            String patrolIdentifier,
            LatLn location,
            LatLn orientation);

    void gameOver(
            String gameIdentifier
    );

    void victory(
            String gameIdentifier
    );
}

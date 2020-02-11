package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.dimtwo.DoublePoint;

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
            DoublePoint location);

    void sentryPositionChanged(
            String gameIdentifier,
            String patrolIdentifier,
            DoublePoint location,
            DoublePoint orientation);

    void gameOver(
            String gameIdentifier
    );

    void victory(
            String gameIdentifier
    );
}

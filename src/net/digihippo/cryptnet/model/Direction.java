package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public enum Direction {
    Forwards,
    Backwards;

    public LatLn orient(LatLn direction)
    {
        throw new UnsupportedOperationException();
    }

    public boolean turnsAt(Path path, int lineIndex, LatLn intersectionPoint) {
        return false;
    }
}

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public interface ModelActions
{
    void joined(
        JoiningSentry sentry,
        LatLn location,
        Path path,
        Segment segment,
        LatLn velocity,
        Direction direction);
}

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

final class Player
{
    Segment segment;
    LatLn position;

    Player(
        Segment segment,
        LatLn position)
    {
        this.segment = segment;
        this.position = position;
    }

    void tick(Model.Events events)
    {
        events.playerPositionChanged(position);
    }

}

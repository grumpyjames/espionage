package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public final class Player
{
    Path path;
    Segment segment;
    public LatLn position;

    private LatLn velocity = new LatLn(0, 0);
    private Direction direction = Direction.Forwards;
    private transient int lineIndex;


    Player(
        Path path,
        Segment segment,
        LatLn position)
    {
        this.path = path;
        this.segment = segment;
        this.position = position;
        this.lineIndex = path.indexOf(segment);
    }

    void tick(Model.Events events)
    {
        events.playerPositionChanged(position);
    }

}

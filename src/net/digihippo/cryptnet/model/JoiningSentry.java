package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public final class JoiningSentry
{
    public final String identifier;
    public final Connection connection;
    public final double speed; // in m/tick (a tick is 40ms)

    public LatLn location;

    JoiningSentry(
            String identifier,
            Connection connection,
            LatLn location,
            double speed)
    {
        this.identifier = identifier;
        this.connection = connection;
        this.location = location;
        this.speed = speed;
    }

    void tick(
        final ModelActions modelActions, Model.Events events)
    {
        this.connection.move(modelActions, this, events);
    }
}

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public final class JoiningSentry
{
    public final String identifier;
    public final Connection connection;

    public LatLn location;

    JoiningSentry(
            String identifier,
            Connection connection,
            LatLn location)
    {
        this.identifier = identifier;
        this.connection = connection;
        this.location = location;
    }

    void tick(
        final ModelActions modelActions, Model.Events events)
    {
        this.connection.path().move(this);
    }
}

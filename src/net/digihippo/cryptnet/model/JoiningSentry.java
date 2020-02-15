package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public final class JoiningSentry
{
    final String identifier;
    public final Connection connection;
    public final LatLn velocity;
    public LatLn location;

    JoiningSentry(
        String identifier,
        Connection connection,
        LatLn location,
        LatLn velocity)
    {
        this.identifier = identifier;
        this.connection = connection;
        this.location = location;
        this.velocity = velocity;
    }

    void tick(
        final ModelActions modelActions, Model.Events events)
    {
        this.location = this.velocity.applyTo(this.location);
        if (this.connection.endsNear(this.location))
        {
            modelActions.joined(
                    this,
                    this.connection.location(),
                    this.connection.path(),
                    this.connection.line(),
                    this.connection.joinVelocity(),
                    this.connection.joinDirection());
        }
        else
        {
            events.sentryPositionChanged(
                    identifier, this.location, this.velocity);
        }
    }
}

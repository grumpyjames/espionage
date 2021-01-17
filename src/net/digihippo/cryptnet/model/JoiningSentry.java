package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.Optional;

final class JoiningSentry
{
    final String identifier;
    final Connection connection;
    final double speed; // in m/tick (a tick is 40ms)

    LatLn location;

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

    Optional<Patrol> tick()
    {
        return this.connection.move(this);
    }
}

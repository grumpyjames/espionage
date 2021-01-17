package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Random;

final class Patrol {
    final String identifier;
    private final double speed; // in m/tick (a tick is 40ms)

    Path path;
    Segment segment;
    UnitVector velocity;
    LatLn location;
    Direction direction;


    Patrol(
            String identifier,
            double speed,
            Path path,
            Segment segment,
            UnitVector velocity,
            LatLn doublePoint,
            Direction direction) {
        this.identifier = identifier;
        this.speed = speed;
        this.path = path;
        this.segment = segment;
        this.velocity = velocity;
        this.location = doublePoint;
        this.direction = direction;
    }

    void tick(final Random random) {
        this.path.move(this, random, this.speed);
    }
}

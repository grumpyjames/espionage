package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Random;

final class Patrol {
    final String identifier;
    UnitVector velocity;
    LatLn location;

    private Segment segment;
    private final double speed; // in m/tick (a tick is 40ms)
    private Direction direction;


    Patrol(
            String identifier,
            double speed,
            Segment segment,
            UnitVector velocity,
            LatLn doublePoint,
            Direction direction) {
        this.identifier = identifier;
        this.speed = speed;
        this.segment = segment;
        this.velocity = velocity;
        this.location = doublePoint;
        this.direction = direction;
    }

    void tick(final Random random) {
        move(random, this.speed);
    }

    // Thoughts:
    // If we know the segment, all we really need to know
    // is the patrol's speed and direction along this path.
    // The path can have prior knowledge of intersections and
    // its own boundaries, and can act accordingly, reorienting the
    // patrol as necessary.
    // We will probably require a random here to choose between possible
    // options.
    void move(Random random, double distanceToMove)
    {
        Segment segment = this.segment;
        UnitVector stepChange = direction.orient(segment.direction());
        Vertex lineEnd = direction.pickBound(this.segment);
        if (lineEnd.location.distanceTo(location) < distanceToMove)
        {
            Vertex.Link link = lineEnd.pickLink(random);
            location = lineEnd.location;
            this.segment = link.segment;
            direction = link.end == Vertex.End.Head ? Direction.Forwards : Direction.Backwards;

            double distanceRemaining = distanceToMove - lineEnd.location.distanceTo(location);
            move(random, distanceRemaining);
        }
        else
        {
            location = stepChange.applyWithScalar(location, distanceToMove);
        }
    }
}

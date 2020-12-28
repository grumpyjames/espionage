package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Random;

public final class Patrol {
    private final String identifier;
    Path path;
    Segment segment;
    public UnitVector velocity;
    public LatLn location;
    public Direction direction;
    private Intersection previous;
    private LatLn lastVertex;
    public final double speed; // in m/tick (a tick is 40ms)

    private transient int lineIndex;

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
        this.lineIndex = path.indexOf(segment);
        this.velocity = velocity;
        this.location = doublePoint;
        this.direction = direction;
    }

    private void snapToLine(
            LatLn location,
            Path path,
            Segment segment,
            Direction direction)
    {
//        this.path = path;
//        this.segment = segment;
//        this.lineIndex = path.indexOf(segment);
//        this.velocity = direction.orient(segment.direction());
//        this.location = location;
//        this.direction = direction;
    }

    void tick(
            final Random random,
            final Model.Events events) {

        this.path.move(this, random);

        events.patrolPositionChanged(this.identifier, this.location, this.velocity);
    }

    private void turn(LatLn turnLocation, Path path, Segment segment, Direction dir) {
        snapToLine(turnLocation, path, segment, dir);
        turnComplete(turnLocation);
    }

    private void turnComplete(LatLn vertexLocation) {
        this.previous = null;
        this.lastVertex = vertexLocation;
    }

    private void intersection(Random random, Intersection intersection) {
        IntersectionEntry entry = intersection.pickEntry(random);

        snapToLine(this.location, entry.path, entry.segment, entry.direction);

        this.previous = intersection;
        this.lastVertex = null;
    }
}

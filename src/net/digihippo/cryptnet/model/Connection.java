package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;

public class Connection {
    public final Segment segment;

    private Connection(Segment segment)
    {
        this.segment = segment;
    }

    static Connection nearestConnection(
            List<Path> paths,
            LatLn location) {

        double bestDistance = Double.MAX_VALUE;
        Vertex bestVertex = null;

        for (Path path : paths)
        {
            for (Segment segment : path.segments())
            {
                double distance = segment.head.location.distanceTo(location);
                if (distance < bestDistance)
                {
                    bestDistance = distance;
                    bestVertex = segment.head;
                }
            }
        }

        assert bestVertex != null;
        final Segment segment = new Segment(new Vertex(location), bestVertex);

        return new Connection(segment);
    }

    public LatLn snapVelocityFrom(LatLn location) {
        // best.connectionPoint.minus(point.asDoublePoint()).over(5))
        throw new UnsupportedOperationException();
    }

    public boolean endsNear(LatLn location) {
        throw new UnsupportedOperationException();
    }

    public LatLn location() {
        return segment.tail.location;
    }

    public Segment line() {
        return segment;
    }

    public LatLn joinVelocity() {
        throw new UnsupportedOperationException();
    }

    public Direction joinDirection() {
        throw new UnsupportedOperationException();
    }

    void move(ModelActions modelActions, JoiningSentry joiningSentry, Model.Events events)
    {
        LatLn movedTo = this.segment.direction().applyWithScalar(joiningSentry.location, joiningSentry.speed);

        double distance = this.segment.tail.distanceTo(movedTo);
        events.sentryPositionChanged(
                true,
                joiningSentry.identifier,
                movedTo,
                this.segment.direction(),
                joiningSentry.connection.location()
        );
        if (distance < 5)
        {
            movedTo = this.segment.tail.location;
            // FIXME:                                            v random required
            final Vertex.Link link = this.segment.tail.links.get(0);
            modelActions.joined(
                    joiningSentry,
                    movedTo,
                    link);

        }

        joiningSentry.location = movedTo;
    }
}

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;

final class Connection {
    final Segment segment;

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

    LatLn location() {
        return segment.tail.location;
    }

    Segment line() {
        return segment;
    }

    void move(ModelActions modelActions, JoiningSentry joiningSentry, Model.Events events)
    {
        LatLn movedTo = this.segment.direction().applyWithScalar(joiningSentry.location, joiningSentry.speed);

        double distance = this.segment.tail.distanceTo(movedTo);
        events.joiningPatrolPositionChanged(
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

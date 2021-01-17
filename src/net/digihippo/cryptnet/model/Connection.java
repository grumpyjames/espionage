package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;
import java.util.Optional;

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

    Optional<Patrol> move(JoiningSentry joiningSentry)
    {
        double distance = this.segment.tail.distanceTo(joiningSentry.location);
        if (distance < joiningSentry.speed)
        {
            // FIXME:                                            v random required
            final Vertex.Link link = this.segment.tail.links.get(0);
            return Optional.of(new Patrol(
                    joiningSentry.identifier,
                    joiningSentry.speed,
                    link.path,
                    link.segment,
                    link.segment.direction(),
                    this.segment.tail.location,
                    link.end == Vertex.End.Head ? Direction.Forwards : Direction.Backwards
            ));
        }
        else
        {
            joiningSentry.location =
                    this.segment.direction().applyWithScalar(joiningSentry.location, joiningSentry.speed);
            return Optional.empty();
        }
    }
}

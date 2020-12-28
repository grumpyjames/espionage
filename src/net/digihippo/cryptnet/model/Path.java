package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Path {
    private final List<Segment> segments;

    public Path(List<Segment> segments) {
        this.segments = segments;
    }

    public int indexOf(Segment segment) {
        for (int i = 0; i < segments.size(); i++) {
            final Segment candidate = segments.get(i);
            if (candidate.sameAs(segment))
            {
                return i;
            }
        }

        throw new IllegalArgumentException();
    }

    public boolean startsAt(LatLn location) {
        return initialSegment().startsAt(location);
    }

    public boolean endsAt(LatLn location) {
        return lastSegment().endsAt(location);
    }

    Segment initialSegment() {
        return segments.get(0);
    }

    Segment lastSegment() {
        return segments.get(segments.size() - 1);
    }

    boolean circularWithOrigin(LatLn origin) {
        return startsAt(origin) && endsAt(origin);
    }

    public Segment nextLine(Direction direction, int lineIndex) {
        throw new UnsupportedOperationException();
    }

    public Collection<? extends Segment> segments() {
        return segments;
    }

    void move(Patrol patrol, Random random) {
        Segment segment = patrol.segment;
        UnitVector stepChange = patrol.direction.orient(segment.direction());
        Vertex lineEnd = patrol.direction.pickBound(patrol.segment);
        LatLn newLocation = stepChange.applyWithScalar(patrol.location, patrol.speed);
        if (lineEnd.location.distanceTo(newLocation) < 1.6)
        {
            Vertex.Link link = lineEnd.pickLink(random);
            patrol.location = lineEnd.location;
            patrol.segment = link.segment;
            patrol.direction = link.end == Vertex.End.Head ? Direction.Forwards : Direction.Backwards;
        }
        else
        {
            patrol.location = newLocation;
        }




        // Thoughts:
        // If we know the segment, all we really need to know
        // is the patrol's speed and direction along this path.
        // The path can have prior knowledge of intersections and
        // its own boundaries, and can act accordingly, reorienting the
        // patrol as necessary.
        // We will probably require a random here to choose between possible
        // options.
    }

    void move(JoiningSentry joiningSentry)
    {
//        final LatLn newLocation = joiningSentry.location

        // Previously:
//        this.location = this.velocity.applyTo(this.location);
//        if (this.connection.endsNear(this.location))
//        {
//            modelActions.joined(
//                    this,
//                    this.connection.location(),
//                    this.connection.path(),
//                    this.connection.line(),
//                    this.connection.joinVelocity(),
//                    this.connection.joinDirection());
//        }
//        else
//        {
//            events.sentryPositionChanged(
//                    identifier, this.location, this.velocity);
//        }
    }

    void visitVertices() {
        for (Segment segment : segments) {
            segment.visitVertices(this);
        }
    }

    public Vertex vertexAt(int i)
    {
        return segments.get(i - 1).tail;
    }
}

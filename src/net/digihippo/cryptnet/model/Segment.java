package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public class Segment {
    private final Vertex head;
    private final Vertex tail;

    public Segment(Vertex head, Vertex tail) {
        this.head = head;
        this.tail = tail;
    }

    // How to get from the head to the tail
    // Should this be a unit vector or what?
    // Should return a velocity or a vector type, really...
    public LatLn direction() {
        return tail.directionFrom(head);
    }

    boolean sameAs(Segment segment) {
        return segment.head.sameAs(this.head) &&
                segment.tail.sameAs(this.tail);
    }

    boolean startsAt(LatLn location) {
        return this.head.hasLocation(location);
    }

    boolean endsAt(LatLn location) {
        return this.tail.hasLocation(location);
    }

    void visitVertices(Path path) {
        head.onLink(Vertex.End.Head, path, this);
        head.onLink(Vertex.End.Tail, path, this);
    }

    Vertex tail()
    {
        return tail;
    }
}

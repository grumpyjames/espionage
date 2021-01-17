package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Objects;

public class Segment {
    public final Vertex head;
    public final Vertex tail;

    public Segment(Vertex head, Vertex tail) {
        assert !head.equals(tail);
        this.head = head;
        this.tail = tail;
    }

    // How to get from the head to the tail
    // Should this be a unit vector or what?
    // Should return a velocity or a vector type, really...
    // Or maybe a bearing? And what if tail == head?
    UnitVector direction() {
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
        tail.onLink(Vertex.End.Tail, path, this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return Objects.equals(head, segment.head) &&
                Objects.equals(tail, segment.tail);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(head, tail);
    }

    @Override
    public String toString()
    {
        return "Segment{" +
                "head=" + head +
                ", tail=" + tail +
                '}';
    }
}

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Path {
    private final List<Segment> segments;

    public Path(List<Segment> segments) {
        this.segments = segments;
    }

    int indexOf(Segment segment) {
        for (int i = 0; i < segments.size(); i++) {
            final Segment candidate = segments.get(i);
            if (candidate.sameAs(segment))
            {
                return i;
            }
        }

        throw new IllegalArgumentException();
    }

    boolean startsAt(LatLn location) {
        return initialSegment().startsAt(location);
    }

    boolean endsAt(LatLn location) {
        return lastSegment().endsAt(location);
    }

    Segment initialSegment() {
        return segments.get(0);
    }

    Segment lastSegment() {
        return segments.get(segments.size() - 1);
    }

    public Collection<? extends Segment> segments() {
        return segments;
    }

    void visitVertices() {
        for (Segment segment : segments) {
            segment.visitVertices(this);
        }
    }

    @SuppressWarnings("SameParameterValue")
    Vertex vertexAt(int i)
    {
        return segments.get(i - 1).tail;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(segments, path.segments);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(segments);
    }

    @Override
    public String toString()
    {
        return "Path{" +
                "segments=" + segments +
                '}';
    }
}

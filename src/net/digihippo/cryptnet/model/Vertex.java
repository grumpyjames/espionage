package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.ArrayList;
import java.util.List;

class Vertex
{
    final LatLn location;
    final List<Link> links = new ArrayList<>();

    public Vertex(LatLn location) {
        this.location = location;
    }

    void onLink(End end, Path path, Segment segment)
    {

    }

    LatLn directionFrom(Vertex other) {
        return this.location.directionFrom(other.location);
    }

    public boolean sameAs(Vertex other) {
        return other.location.sameAs(this.location);
    }

    public boolean hasLocation(LatLn location) {
        return this.location.sameAs(location);
    }

    enum End
    {
        Head,
        Tail
    }

    static final class Link
    {
        private final End end;
        private final Path path;
        private final Segment segment;

        Link(End end, Path path, Segment segment) {
            this.end = end;
            this.path = path;
            this.segment = segment;
        }
    }
}

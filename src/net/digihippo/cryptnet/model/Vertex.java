package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.ArrayList;
import java.util.List;

public class Vertex
{
    public final LatLn location;
    final List<Link> links = new ArrayList<>();

    public Vertex(LatLn location) {
        this.location = location;
    }

    void onLink(End end, Path path, Segment segment)
    {
        links.add(new Link(end, path, segment));
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

    public double distanceTo(Vertex other)
    {
        return this.location.distanceTo(other.location);
    }

    public double distanceTo(LatLn location)
    {
        return this.location.distanceTo(location);
    }

    enum End
    {
        Head,
        Tail
    }

    static final class Link
    {
        public final End end;
        public final Path path;
        public final Segment segment;

        Link(End end, Path path, Segment segment) {
            this.end = end;
            this.path = path;
            this.segment = segment;
        }
    }
}

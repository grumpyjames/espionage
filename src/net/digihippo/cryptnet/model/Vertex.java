package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Vertex
{
    public final LatLn location;
    transient final List<Link> links = new ArrayList<>();

    public Vertex(LatLn location) {
        this.location = location;
    }

    void onLink(End end, Path path, Segment segment)
    {
        links.add(new Link(end, path, segment));
    }

    UnitVector directionFrom(Vertex other) {
        return this.location.directionFrom(other.location);
    }

    public boolean sameAs(Vertex other) {
        return other.location.sameAs(this.location);
    }

    public boolean hasLocation(LatLn location) {
        return this.location.sameAs(location);
    }

    public double distanceTo(LatLn location)
    {
        return this.location.distanceTo(location);
    }

    public Link pickLink(Random random)
    {
        return links.get(random.nextInt(links.size()));
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

    // Deliberately excluding the links!
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(location, vertex.location);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(location);
    }

    @Override
    public String toString()
    {
        return "Vertex{" +
                "location=" + location +
                '}';
    }
}

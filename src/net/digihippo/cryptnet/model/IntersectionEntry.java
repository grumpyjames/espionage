package net.digihippo.cryptnet.model;

public class IntersectionEntry {
    public final Path path;
    public final Segment segment;
    public final Direction direction;

    public IntersectionEntry(Path path, Segment segment, Direction direction) {
        this.path = path;
        this.segment = segment;
        this.direction = direction;
    }
}

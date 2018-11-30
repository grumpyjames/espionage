package net.digihippo.cryptnet;

import java.util.HashSet;
import java.util.Set;

class Intersection
{
    final Set<IntersectionEntry> entries = new HashSet<>();
    final Point point;

    public Intersection(Point point)
    {
        this.point = point;
    }

    public void addAll(Line...lines)
    {
        for (Line line : lines)
        {
            this.entries.add(new IntersectionEntry(line, line.startsAt(point), line.endsAt(point)));
        }
    }
}

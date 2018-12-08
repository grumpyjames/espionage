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

    public void add(Path pathOne, Line lineOne, Path pathTwo, Line lineTwo)
    {
        addEntries(pathOne, lineOne);
        addEntries(pathTwo, lineTwo);
    }

    private void addEntries(Path pathOne, Line lineOne)
    {
        if (lineOne.endsAt(point))
        {
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Backwards, pathOne.startsAt(point), pathOne.endsAt(point)));
            if (!pathOne.endsAt(point))
            {
                this.entries.add(
                    new IntersectionEntry(
                        pathOne, pathOne.lineAfter(lineOne, Direction.Forwards),
                        Direction.Forwards, pathOne.startsAt(point), pathOne.endsAt(point)));
            }
        }
        else if (lineOne.startsAt(point))
        {
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Forwards, pathOne.startsAt(point), pathOne.endsAt(point)));

            if (!pathOne.startsAt(point))
            {
                this.entries.add(
                    new IntersectionEntry(
                        pathOne, pathOne.lineAfter(lineOne, Direction.Backwards),
                        Direction.Backwards, pathOne.startsAt(point), pathOne.endsAt(point)));
            }
        }
        else
        {
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Backwards, pathOne.startsAt(point), pathOne.endsAt(point)));
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Forwards, pathOne.startsAt(point), pathOne.endsAt(point)));
        }
    }

    @Override
    public String toString()
    {
        return "Intersection{" +
            "entries=" + entries +
            ", point=" + point +
            '}';
    }
}

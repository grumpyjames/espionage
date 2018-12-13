package net.digihippo.cryptnet;

import java.util.HashSet;
import java.util.Set;

class Intersection
{
    final Point point;
    final Set<IntersectionEntry> entries = new HashSet<>();

    public Intersection(Point point)
    {
        this.point = point;
    }

    public void add(Path pathOne, Line lineOne)
    {
        addEntries(pathOne, lineOne);
    }

    private void addEntries(Path pathOne, Line lineOne)
    {
        if (lineOne.endsAt(point))
        {
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Backwards));
            if (!pathOne.endsAt(point))
            {
                this.entries.add(
                    new IntersectionEntry(
                        pathOne, pathOne.lineAfter(lineOne, Direction.Forwards),
                        Direction.Forwards));
            }
        }
        else if (lineOne.startsAt(point))
        {
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Forwards));

            if (!pathOne.startsAt(point))
            {
                this.entries.add(
                    new IntersectionEntry(
                        pathOne, pathOne.lineAfter(lineOne, Direction.Backwards),
                        Direction.Backwards));
            }
        }
        else
        {
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Backwards));
            this.entries.add(
                new IntersectionEntry(
                    pathOne, lineOne, Direction.Forwards));
        }
    }

    @Override
    public String toString()
    {
        boolean first = true;
        String result = "[";
        for (IntersectionEntry entry : entries)
        {
            if (!first)
            {
                result += ", ";
            }
            result += entry.toString();
            first = false;
        }
        result += "]";

        return result + "@" + point.toString();
    }

    public static Intersection parse(String string)
    {
        String[] parts = string.split("@");
        Intersection result = new Intersection(Point.parse(parts[1]));

        final Set<IntersectionEntry> intersectionEntries = new HashSet<>();
        String[] entries = parts[0].substring(1, parts[0].length() - 1).split(", ");
        for (String entry : entries)
        {
            intersectionEntries.add(IntersectionEntry.parse(entry));
        }

        result.entries.addAll(intersectionEntries);

        return result;
    }
}

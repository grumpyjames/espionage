package net.digihippo.cryptnet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

class Intersection
{
    final Point point;
    final Set<IntersectionEntry> entries = new HashSet<>();

    public Intersection(Point point)
    {
        this.point = point;
    }

    public static Map<Point, Intersection> intersections(List<Path> paths)
    {
        final Map<Point, Intersection> results = new HashMap<>();
        for (int i = 0; i < paths.size(); i++)
        {
            final Path pathOne = paths.get(i);
            for (int j = i + 1; j < paths.size(); j++)
            {
                final Path pathTwo = paths.get(j);
                if (pathOne != pathTwo)
                {
                    for (final Line lineOne : pathOne.lines())
                    {
                        for (final Line lineTwo : pathTwo.lines())
                        {
                            lineOne.intersectionWith(lineTwo).visit(new Consumer<Point>()
                            {
                                @Override
                                public void accept(Point point)
                                {
                                    Intersection intersection = results.computeIfAbsent(
                                        point,
                                        new Function<Point, Intersection>()
                                        {
                                            @Override
                                            public Intersection apply(Point point)
                                            {
                                                return new Intersection(point);
                                            }
                                        });
                                    intersection.add(pathOne, lineOne);
                                    intersection.add(pathTwo, lineTwo);
                                }
                            });
                        }
                    }
                }
            }
        }

        return results;
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

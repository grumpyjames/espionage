package net.digihippo.cryptnet.dimtwo;

import net.digihippo.cryptnet.compat.Consumer;
import net.digihippo.cryptnet.compat.Function;
import net.digihippo.cryptnet.compat.Maps;

import java.util.*;

public class Intersection
{
    public final Pixel point;
    public final Set<IntersectionEntry> entries = new HashSet<>();

    Intersection(Pixel point)
    {
        this.point = point;
    }

    public static Map<Pixel, Intersection> intersections(List<Path> paths)
    {
        final Map<Pixel, Intersection> results = new HashMap<>();
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
                            lineOne.intersectionWith(lineTwo).visit(new Consumer<Pixel>()
                            {
                                @Override
                                public void consume(Pixel point)
                                {
                                    Intersection intersection = Maps.computeIfAbsent(
                                        results,
                                        point,
                                        new Function<Pixel, Intersection>()
                                        {
                                            @Override
                                            public Intersection apply(Pixel point)
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
        StringBuilder result = new StringBuilder("[");
        for (IntersectionEntry entry : entries)
        {
            if (!first)
            {
                result.append(", ");
            }
            result.append(entry.toString());
            first = false;
        }
        result.append("]");

        return result + "@" + point.toString();
    }

    public static Intersection parse(String string)
    {
        String[] parts = string.split("@");
        Intersection result = new Intersection(Pixel.parse(parts[1]));

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

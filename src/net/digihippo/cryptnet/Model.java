package net.digihippo.cryptnet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

final class Model
{
    final Map<Point, Intersection> intersections;
    final List<Line> lines;
    final List<Sentry> sentries = new ArrayList<>();

    public static Model createModel(List<Line> lines)
    {
        return new Model(lines, intersections(lines));
    }

    private Model(List<Line> lines, Map<Point, Intersection> intersections)
    {
        this.lines = lines;
        this.intersections = intersections;
    }

    private static Map<Point, Intersection> intersections(List<Line> lines)
    {
        final Map<Point, Intersection> results = new HashMap<>();
        for (int i = 0; i < lines.size(); i++)
        {
            final Line lineOne = lines.get(i);
            for (int j = i + 1; j < lines.size(); j++)
            {
                final Line lineTwo = lines.get(j);
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
                        intersection.addAll(lineOne, lineTwo);
                    }
                });
            }
        }

        return results;
    }

    public void tick(Random random)
    {
        for (Sentry sentry : sentries)
        {
            sentry.tick(intersections, random);
        }
    }

    public void addSentry(int x, int y)
    {
        Connection best = Connection.nearestConnection(lines, new Point(x, y));

        sentries.add(new Sentry(new Point(x, y), best));
    }

    public int size()
    {
        return 250;
    }
}

package net.digihippo.cryptnet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

final class Model
{
    final Map<Point, Intersection> intersections;
    final List<Line> lines;
    final List<JoiningSentry> joiningSentries = new ArrayList<>();
    final List<Patrol> patrols = new ArrayList<>();

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
        DeferredModelActions modelActions = new DeferredModelActions();
        for (JoiningSentry sentry : joiningSentries)
        {
            sentry.tick(modelActions);
        }
        for (Patrol patrol : patrols)
        {
            patrol.tick(intersections, random);
        }

        modelActions.enact(this);
    }

    public void addSentry(int x, int y)
    {
        Connection best = Connection.nearestConnection(lines, new Point(x, y));

        joiningSentries.add(new JoiningSentry(new Point(x, y), best));
    }

    public int size()
    {
        return 250;
    }

    public void removeJoining(List<JoiningSentry> outgoing)
    {
        this.joiningSentries.removeAll(outgoing);
    }

    public void addPatrols(List<Patrol> incoming)
    {
        this.patrols.addAll(incoming);
    }
}

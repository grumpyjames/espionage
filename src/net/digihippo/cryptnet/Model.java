package net.digihippo.cryptnet;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

final class Model
{
    final Map<Point, Intersection> intersections;
    final List<JoiningSentry> joiningSentries = new ArrayList<>();
    final List<Patrol> patrols = new ArrayList<>();
    private final int size;
    final BufferedImage image;
    final List<Path> paths;
    final List<Line> lines;
    DoublePoint player = null;

    public static Model createModel(List<Path> paths, int size, BufferedImage image)
    {
        return new Model(paths, intersections(paths), lines(paths), size, image);
    }

    private Model(
        List<Path> paths,
        Map<Point, Intersection> intersections,
        List<Line> lines,
        int size,
        BufferedImage image)
    {
        this.paths = paths;
        this.intersections = intersections;
        this.lines = lines;
        this.size = size;
        this.image = image;
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

            if (player != null)
            {
                double distanceToPlayer = DoublePoint.distanceBetween(patrol.point, player);
                if (distanceToPlayer < 5)
                {
                    System.out.println("Game over man!");
                }
            }
        }

        modelActions.enact(this);
    }

    public void addSentry(int x, int y)
    {
        Connection best =
            Connection.nearestConnection(paths, new Point(x, y));

        joiningSentries.add(new JoiningSentry(new Point(x, y), best));
    }

    static List<Line> lines(List<Path> paths)
    {
        final List<Line> lines = new ArrayList<>();

        for (Path path : paths)
        {
            lines.addAll(path.lines);
        }

        return lines;
    }

    public void addPlayer(int x, int y)
    {
        Connection connection =
            Connection.nearestConnection(paths, new Point(x, y));

        player = connection.connectionPoint;
    }

    public int size()
    {
        return size;
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

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Way;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public final class Model
{
    public final List<JoiningSentry> joiningSentries = new ArrayList<>();
    public final List<Patrol> patrols = new ArrayList<>();
    public final List<Path> paths;
    public final List<Segment> segments;
    public final int width;
    public final int height;

    private int sentryIndex = 0;
    public Player player = null;

    public interface Events
    {
        void playerPositionChanged(
                LatLn location);

        void sentryPositionChanged(
                String patrolIdentifier,
                LatLn location,
                LatLn orientation);

        void gameOver();

        void victory();

        void gameRejected(String message);

        void gameStarted();
    }

    public static Model createModel(
        Collection<Way> ways,
        int width,
        int height)
    {
        List<Path> paths = Paths.from(ways);
        return new Model(
                paths,
                segments(paths),
                width,
                height);
    }

    private Model(
            List<Path> paths,
            List<Segment> segments,
            int width,
            int height)
    {
        this.paths = paths;
        this.segments = segments;
        this.width = width;
        this.height = height;
    }

    public void tick(Random random, Events events)
    {
        DeferredModelActions modelActions = new DeferredModelActions();

        for (JoiningSentry sentry : joiningSentries)
        {
            sentry.tick(modelActions, events);
        }

        for (Patrol patrol : patrols)
        {
            patrol.tick(random, events);

            if (player != null)
            {
                double distanceToPlayer =
                        patrol.location.distanceTo(player.position);
                if (distanceToPlayer < 5)
                {
                    events.gameOver();
                }
            }
        }
        if (player != null)
        {
            player.tick(events);
        }

        modelActions.enact(this, events);
    }

    void addSentry(final LatLn location)
    {
        Connection best =
            Connection.nearestConnection(paths, location);

        joiningSentries.add(
            new JoiningSentry(
                "sentry-" + sentryIndex++,
                best,
                location,
                best.snapVelocityFrom(location)));
    }

    private static List<Segment> segments(List<Path> paths)
    {
        final List<Segment> segments = new ArrayList<>();

        for (Path path : paths)
        {
            segments.addAll(path.segments());
        }

        return segments;
    }

    public void setPlayerLocation(final LatLn latLn)
    {
        Connection connection =
            Connection.nearestConnection(paths, latLn);

        player = new Player(
                connection.path(),
                connection.line(),
                connection.location());
    }

    void removeJoining(List<JoiningSentry> outgoing)
    {
        this.joiningSentries.removeAll(outgoing);
    }

    void addPatrols(List<Patrol> incoming)
    {
        this.patrols.addAll(incoming);
    }

    public void click(LatLn location)
    {
        if (joiningSentries.size() + patrols.size() > 3)
        {
            if (player == null)
            {
                setPlayerLocation(location);
            }
        }
        else
        {
            addSentry(location);
        }
    }
}

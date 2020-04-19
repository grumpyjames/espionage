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

    public static Model createModel(Collection<Way> ways)
    {
        List<Path> paths = Paths.from(ways);
        return new Model(paths);
    }

    private Model(List<Path> paths)
    {
        this.paths = paths;
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
        final Connection best =
            Connection.nearestConnection(paths, location);

        joiningSentries.add(
                new JoiningSentry(
                        "sentry-" + sentryIndex++,
                        best,
                        location));
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

package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.*;
import java.util.stream.Collectors;

public final class Model
{
    private final List<JoiningSentry> joiningSentries = new ArrayList<>();
    private final List<Patrol> patrols = new ArrayList<>();
    private final List<Path> paths;
    private final Rules rules;
    private final Random random;
    private final Events events;

    private int sentryIndex = 0;
    private Player player = null;
    private long time;
    private long nextTick;
    private boolean gameOn = false;
    private long startTime;
    private final double ticksPerSecond = 25;

    public interface Events
    {
        // This happens outside a frame?
        void gameStarted();

        void frameStart(int frameCounter);

        void playerPositionChanged(
                LatLn location);

        void sentryPositionChanged(
                boolean joining,
                String patrolIdentifier,
                LatLn location,
                UnitVector orientation,
                LatLn connectionLocation);

        void gameOver();

        void victory();

        void frameEnd(int frameCounter);

        void gameRejected(String message);
    }

    public static Model createModel(
            List<Path> paths,
            Rules rules,
            Random random,
            Events events)
    {
        return new Model(paths, rules, random, events);
    }

    private Model(
            List<Path> paths,
            Rules rules,
            Random random,
            Events events)
    {
        this.paths = paths;
        this.rules = rules;
        this.random = random;
        this.events = events;
    }

    public void startGame(long timeMillis)
    {
        this.startTime = timeMillis;
        this.time = timeMillis;
        this.nextTick = this.time + 40;
        this.gameOn = true;
    }

    public void time(long timeMillis)
    {
        // we tick 25 times per second, i.e once every 40ms...
        while (this.nextTick < timeMillis && gameOn)
        {
            events.frameStart(frameCounter);
            this.tick();
            Rules.State state = this.rules.gameState(
                    this.nextTick - this.startTime,
                    this.player.position,
                    this.patrols.stream().map(p -> p.location).collect(Collectors.toList()));
            switch (state)
            {
                case GameOver:
                    events.gameOver();
                    events.frameEnd(frameCounter);
                    this.gameOn = false;
                    return;
                case Victory:
                    events.victory();
                    events.frameEnd(frameCounter);
                    this.gameOn = false;
                    return;
                case Continue:
                default:
                    // carry on
            }
            events.frameEnd(frameCounter);

            frameCounter++;
            this.nextTick += 40;

        }
        this.time = timeMillis;
    }

    int frameCounter = 0;

    public void tick()
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
                        location,
                        rules.sentrySpeed() / this.ticksPerSecond));
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

    public void snapPlayerLocationToNearestVertex(final LatLn latLn)
    {
        Connection connection =
            Connection.nearestConnection(paths, latLn);

        this.player = new Player(
                connection.line(),
                connection.location());
    }

    public void setPlayerLocation(final LatLn latLn)
    {
        this.player = new Player(
                null,
                latLn);
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
                snapPlayerLocationToNearestVertex(location);
            }
        }
        else
        {
            addSentry(location);
        }
    }
}

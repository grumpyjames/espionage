package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class Model
{
    private static final double TICKS_PER_SECOND = 25;
    private static final int MILLISECONDS_PER_TICK = 40;

    private final List<JoiningSentry> joiningSentries = new ArrayList<>();
    private final List<Patrol> patrols = new ArrayList<>();
    private final List<Path> paths;
    private final StayAliveRules rules;
    private final Random random;
    private final Events events;

    private int sentryIndex = 0;
    private Player player = null;
    private long time;
    private long nextTick;
    private long startTime;
    private int frameCounter = 0;

    enum GameState
    {
        BEFORE_START,
        PLAYING,
        PAUSED,
        UNPAUSED,
        COMPLETE
    }

    private GameState gameState = GameState.BEFORE_START;

    public void transmitGameReady(String gameId)
    {
        events.rules(rules);
        paths.forEach(events::path);
        events.gameReady(gameId);
    }

    public void playerDisconnected()
    {
        this.gameState = GameState.PAUSED;
    }

    public void playerRejoined()
    {
        this.gameState = GameState.UNPAUSED;
    }

    public interface Events
    {
        void rules(StayAliveRules rules);

        void path(Path path);

        void gameReady(String gameId);

        void gameStarted();

        void frameStart(int frameCounter);

        void playerPositionChanged(
                LatLn location);

        void patrolPositionChanged(
                String patrolIdentifier,
                LatLn location,
                UnitVector orientation);

        void joiningPatrolPositionChanged(
                String identifier,
                LatLn movedTo,
                UnitVector direction,
                LatLn joiningLocation);

        void gameOver();

        void victory();

        void frameEnd(int frameCounter);
    }

    public static Model createModel(
            List<Path> paths,
            StayAliveRules rules,
            Random random,
            Events events)
    {
        return new Model(paths, rules, random, events);
    }

    private Model(
            List<Path> paths,
            StayAliveRules rules,
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
        this.nextTick = this.time + MILLISECONDS_PER_TICK;
        this.gameState = GameState.PLAYING;
        double bearingFraction = (2d * Math.PI) / (rules.sentryCount());
        for (int i = 0; i < rules.sentryCount(); i++)
        {
            double bearing = bearingFraction * i;
            LatLn sentryLocation = player.position.move(rules.initialSentryDistance(), bearing);
            addSentry(sentryLocation);
        }
        this.events.gameStarted();
    }

    public void time(long timeMillis)
    {
        if (gameState == GameState.UNPAUSED)
        {
            this.nextTick = timeMillis + MILLISECONDS_PER_TICK;
            this.gameState = GameState.PLAYING;
        }

        while (this.gameState == GameState.PLAYING && this.nextTick < timeMillis)
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
                    this.gameState = GameState.COMPLETE;
                    break;
                case Victory:
                    events.victory();
                    this.gameState = GameState.COMPLETE;
                    break;
                case Continue:
                default:
                    // carry on
            }
            events.frameEnd(frameCounter);

            frameCounter++;
            this.nextTick += MILLISECONDS_PER_TICK;

        }
        this.time = timeMillis;
    }

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
                        rules.sentrySpeed() / TICKS_PER_SECOND));
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
}

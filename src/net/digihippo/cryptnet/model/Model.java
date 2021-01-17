package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.*;
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
    private final FrameCollector frameCollector;

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
        frameCollector.rules(rules);
        paths.forEach(frameCollector::path);
        frameCollector.gameReady(gameId);
    }

    public void playerDisconnected()
    {
        this.gameState = GameState.PAUSED;
    }

    public void playerRejoined()
    {
        this.gameState = GameState.UNPAUSED;
    }

    public static Model createModel(
            List<Path> paths,
            StayAliveRules rules,
            Random random,
            GameEvents gameEvents)
    {
        return new Model(paths, rules, random, new FrameCollector(gameEvents));
    }

    private Model(
            List<Path> paths,
            StayAliveRules rules,
            Random random,
            FrameCollector frameCollector)
    {
        this.paths = paths;
        this.rules = rules;
        this.random = random;
        this.frameCollector = frameCollector;
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
        this.frameCollector.gameStarted();
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
            frameCollector.frameStart(frameCounter);
            this.tick();
            State state = this.rules.gameState(
                    this.nextTick - this.startTime,
                    this.player.position,
                    this.patrols.stream().map(p -> p.location).collect(Collectors.toList()));
            switch (state)
            {
                case GameOver:
                    frameCollector.gameOver();
                    this.gameState = GameState.COMPLETE;
                    break;
                case Victory:
                    frameCollector.victory();
                    this.gameState = GameState.COMPLETE;
                    break;
                case Continue:
                default:
                    // carry on
            }
            frameCollector.frameEnd(frameCounter);

            frameCounter++;
            this.nextTick += MILLISECONDS_PER_TICK;

        }
        this.time = timeMillis;
    }

    public void tick()
    {
        tickPatrols();
        tickJoiningSentries();
        tickPlayer();
    }

    private void tickPlayer()
    {
        if (player != null)
        {
            frameCollector.playerPositionChanged(player.position);
        }
    }

    private void tickPatrols()
    {
        for (Patrol patrol: patrols)
        {
            patrol.tick(random);
            frameCollector.patrolPositionChanged(patrol.identifier, patrol.location, patrol.velocity);
        }
    }

    private void tickJoiningSentries()
    {
        for (Iterator<JoiningSentry> iterator = joiningSentries.iterator(); iterator.hasNext(); )
        {
            JoiningSentry sentry = iterator.next();
            Optional<Patrol> maybePatrol = sentry.tick();
            if (maybePatrol.isPresent())
            {
                iterator.remove();
                Patrol newPatrol = maybePatrol.get();
                patrols.add(newPatrol);
                frameCollector.patrolPositionChanged(newPatrol.identifier, newPatrol.location, newPatrol.velocity);
            }
            else
            {
                frameCollector.joiningPatrolPositionChanged(
                        sentry.identifier,
                        sentry.location,
                        sentry.connection.segment.direction(),
                        sentry.connection.location()
                );
            }
        }
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
}
